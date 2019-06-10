/**
 * ownCloud Android client application
 *
 * @author Bartek Przybylski
 * @author Tobias Kaminsky
 * @author David A. Velasco
 * @author masensio
 * @author Christian Schabesberger
 * @author David González Verdugo
 * @author Shashvat Kedia
 * @author Abel García de Prada
 * Copyright (C) 2011  Bartek Przybylski
 * Copyright (C) 2019 ownCloud GmbH.
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2,
 * as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.owncloud.android.ui.adapter;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.owncloud.android.R;
import com.owncloud.android.authentication.AccountUtils;
import com.owncloud.android.datamodel.FileDataStorageManager;
import com.owncloud.android.datamodel.OCFile;
import com.owncloud.android.datamodel.ThumbnailsCacheManager;
import com.owncloud.android.db.PreferenceManager;
import com.owncloud.android.files.services.FileDownloader.FileDownloaderBinder;
import com.owncloud.android.files.services.FileUploader.FileUploaderBinder;
import com.owncloud.android.services.OperationsService.OperationsServiceBinder;
import com.owncloud.android.ui.activity.ComponentsGetter;
import com.owncloud.android.utils.DisplayUtils;
import com.owncloud.android.utils.FileStorageUtils;
import com.owncloud.android.utils.MimetypeIconUtil;
import com.owncloud.android.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * This Adapter populates a ListView with all files and folders in an ownCloud
 * instance.
 */
public class FileListListAdapter extends BaseAdapter implements ListAdapter {

    private Context mContext;
    private Vector<OCFile> mImmutableFilesList = null; // List containing the database files, doesn't change with search
    private Vector<OCFile> mFiles = null; // List that can be changed when using search
    private boolean mJustFolders;
    private boolean mOnlyAvailableOffline;

    private FileDataStorageManager mStorageManager;
    private Account mAccount;
    private ComponentsGetter mTransferServiceGetter;

    private enum ViewType {LIST_ITEM, GRID_IMAGE, GRID_ITEM}

    public FileListListAdapter(
            boolean justFolders,
            boolean onlyAvailableOffline,
            Context context,
            ComponentsGetter transferServiceGetter
    ) {

        mJustFolders = justFolders;
        mOnlyAvailableOffline = onlyAvailableOffline;
        mContext = context;
        mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);

        mTransferServiceGetter = transferServiceGetter;

        // Read sorting order, default to sort by name ascending
        FileStorageUtils.mSortOrderFileDisp = PreferenceManager.getSortOrder(mContext,
                FileStorageUtils.FILE_DISPLAY_SORT);
        FileStorageUtils.mSortAscendingFileDisp = PreferenceManager.getSortAscending(mContext,
                FileStorageUtils.FILE_DISPLAY_SORT);

        // initialise thumbnails cache on background thread
        new ThumbnailsCacheManager.InitDiskCacheTask().execute();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return mFiles != null ? mFiles.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        if (mFiles == null || mFiles.size() <= position) {
            return null;
        }
        return mFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        if (mFiles == null || mFiles.size() <= position) {
            return 0;
        }
        return mFiles.get(position).getFileId();
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        OCFile file = null;
        LayoutInflater inflator = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mFiles != null && mFiles.size() > position) {
            file = mFiles.get(position);
        }

        // Find out which layout should be displayed
        final ViewType viewType;
        if (parent instanceof GridView) {
            if (file != null && file.isImage()) {
                viewType = ViewType.GRID_IMAGE;
            } else {
                viewType = ViewType.GRID_ITEM;
            }
        } else {
            viewType = ViewType.LIST_ITEM;
        }

        // create view only if differs, otherwise reuse
        if (convertView == null || convertView.getTag() != viewType) {
            switch (viewType) {
                case GRID_IMAGE:
                    view = inflator.inflate(R.layout.grid_image, parent, false);
                    view.setTag(ViewType.GRID_IMAGE);
                    break;
                case GRID_ITEM:
                    view = inflator.inflate(R.layout.grid_item, parent, false);
                    view.setTag(ViewType.GRID_ITEM);
                    break;
                case LIST_ITEM:
                    view = inflator.inflate(R.layout.list_item, parent, false);
                    view.setTag(ViewType.LIST_ITEM);
                    // Allow or disallow touches with other visible windows
                    view.setFilterTouchesWhenObscured(
                            PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(mContext)
                    );
                    break;
            }
        }

        if (file != null) {
            final ImageView localStateView = view.findViewById(R.id.localFileIndicator);
            final ImageView fileIcon = view.findViewById(R.id.thumbnail);

            fileIcon.setTag(file.getFileId());
            TextView fileName;
            String name = file.getFileName();

            final LinearLayout linearLayout = view.findViewById(R.id.ListItemLayout);
            linearLayout.setContentDescription("LinearLayout-" + name);

            // Allow or disallow touches with other visible windows
            linearLayout.setFilterTouchesWhenObscured(
                    PreferenceUtils.shouldDisallowTouchesWithOtherVisibleWindows(mContext)
            );

            switch (viewType) {
                case LIST_ITEM:
                    TextView fileSizeV = view.findViewById(R.id.file_size);
                    TextView fileSizeSeparatorV = view.findViewById(R.id.file_separator);
                    TextView lastModV = view.findViewById(R.id.last_mod);
                    lastModV.setVisibility(View.VISIBLE);

                    if (!mOnlyAvailableOffline) {
                        lastModV.setText(DisplayUtils.getRelativeTimestamp(mContext, file.getModificationTimestamp()));
                        fileSizeSeparatorV.setVisibility(View.VISIBLE);
                        fileSizeV.setVisibility(View.VISIBLE);
                        fileSizeV.setText(DisplayUtils.bytesToHumanReadable(
                                file.getFileLength(), mContext
                        ));
                    } else {
                        lastModV.setText(file.getRemotePath());
                        lastModV.setSingleLine(true);
                        lastModV.setEllipsize(TextUtils.TruncateAt.MIDDLE);
                        fileSizeSeparatorV.setVisibility(View.GONE);
                        fileSizeV.setVisibility(View.GONE);
                    }

                case GRID_ITEM:
                    // filename
                    fileName = view.findViewById(R.id.Filename);
                    name = file.getFileName();
                    fileName.setText(name);

                case GRID_IMAGE:
                    // sharedIcon
                    ImageView sharedIconV = view.findViewById(R.id.sharedIcon);
                    if (file.isSharedViaLink()) {
                        sharedIconV.setImageResource(R.drawable.shared_via_link);
                        sharedIconV.setVisibility(View.VISIBLE);
                        sharedIconV.bringToFront();
                    } else if (file.isSharedWithSharee() || file.isSharedWithMe()) {
                        sharedIconV.setImageResource(R.drawable.shared_via_users);
                        sharedIconV.setVisibility(View.VISIBLE);
                        sharedIconV.bringToFront();
                    } else {
                        sharedIconV.setVisibility(View.GONE);
                    }
                    break;
            }

            // For all Views
            setIconPinAcordingToFilesLocalState(localStateView, file);

            final ImageView checkBoxV = view.findViewById(R.id.custom_checkbox);
            checkBoxV.setVisibility(View.GONE);
            view.setBackgroundColor(Color.WHITE);

            AbsListView parentList = (AbsListView) parent;
            if (parentList.getChoiceMode() != AbsListView.CHOICE_MODE_NONE &&
                    parentList.getCheckedItemCount() > 0
            ) {
                if (parentList.isItemChecked(position)) {
                    view.setBackgroundColor(mContext.getResources().getColor(
                            R.color.selected_item_background));
                    checkBoxV.setImageResource(
                            R.drawable.ic_checkbox_marked);
                } else {
                    view.setBackgroundColor(Color.WHITE);
                    checkBoxV.setImageResource(
                            R.drawable.ic_checkbox_blank_outline);
                }
                checkBoxV.setVisibility(View.VISIBLE);
            }

            if (file.isFolder()) {
                // Folder
                fileIcon.setImageResource(
                        MimetypeIconUtil.getFolderTypeIconId(
                                file.isSharedWithMe() || file.isSharedWithSharee(),
                                file.isSharedViaLink()));
            } else {
                if (file.isImage() && file.getRemoteId() != null) {
                    // Thumbnail in Cache?
                    Bitmap thumbnail = ThumbnailsCacheManager.getBitmapFromDiskCache(
                            String.valueOf(file.getRemoteId())
                    );
                    if (thumbnail != null && !file.needsUpdateThumbnail()) {
                        fileIcon.setImageBitmap(thumbnail);
                    } else {
                        // generate new Thumbnail
                        if (ThumbnailsCacheManager.cancelPotentialThumbnailWork(file, fileIcon)) {
                            final ThumbnailsCacheManager.ThumbnailGenerationTask task =
                                    new ThumbnailsCacheManager.ThumbnailGenerationTask(
                                            fileIcon, mStorageManager, mAccount
                                    );
                            if (thumbnail == null) {
                                thumbnail = ThumbnailsCacheManager.mDefaultImg;
                            }
                            final ThumbnailsCacheManager.AsyncThumbnailDrawable asyncDrawable =
                                    new ThumbnailsCacheManager.AsyncThumbnailDrawable(
                                            mContext.getResources(),
                                            thumbnail,
                                            task
                                    );
                            fileIcon.setImageDrawable(asyncDrawable);
                            task.execute(file);
                        }
                    }

                    if (file.getMimetype().equalsIgnoreCase("image/png")) {
                        fileIcon.setBackgroundColor(mContext.getResources()
                                .getColor(R.color.background_color));
                    }

                } else {
                    fileIcon.setImageResource(MimetypeIconUtil.getFileTypeIconId(file.getMimetype(),
                            file.getFileName()));
                }

            }
        }
        return view;
    }

    private void setIconPinAcordingToFilesLocalState(ImageView localStateView, OCFile file) {
        // local state
        localStateView.bringToFront();
        final FileDownloaderBinder downloaderBinder =
                mTransferServiceGetter.getFileDownloaderBinder();
        final FileUploaderBinder uploaderBinder =
                mTransferServiceGetter.getFileUploaderBinder();
        final OperationsServiceBinder opsBinder =
                mTransferServiceGetter.getOperationsServiceBinder();

        localStateView.setVisibility(View.INVISIBLE);   // default first

        if (opsBinder != null && opsBinder.isSynchronizing(mAccount, file)) {
            //syncing
            localStateView.setImageResource(R.drawable.sync_pin);
            localStateView.setVisibility(View.VISIBLE);
        } else if (downloaderBinder != null && downloaderBinder.isDownloading(mAccount, file)) {
            // downloading
            localStateView.setImageResource(R.drawable.sync_pin);
            localStateView.setVisibility(View.VISIBLE);
        } else if (uploaderBinder != null && uploaderBinder.isUploading(mAccount, file)) {
            // uploading
            localStateView.setImageResource(R.drawable.sync_pin);
            localStateView.setVisibility(View.VISIBLE);
        } else if (file.getEtagInConflict() != null) {
            // conflict
            localStateView.setImageResource(R.drawable.error_pin);
            localStateView.setVisibility(View.VISIBLE);
        } else {
            if (file.isDown()) {
                localStateView.setVisibility(View.VISIBLE);
                localStateView.setImageResource(R.drawable.downloaded_pin);
            }

            if (file.isAvailableOffline()) {
                localStateView.setVisibility(View.VISIBLE);
                localStateView.setImageResource(R.drawable.offline_available_pin);
            }
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return (mFiles == null || mFiles.isEmpty());
    }

    /**
     * Change the adapted directory for a new one
     *
     * @param folder                New folder to adapt. Can be NULL, meaning
     *                              "no content to adapt".
     * @param updatedStorageManager Optional updated storage manager; used to replace
     *                              mStorageManager if is different (and not NULL)
     */
    public void swapDirectory(OCFile folder, FileDataStorageManager updatedStorageManager) {
        if (updatedStorageManager != null && updatedStorageManager != mStorageManager) {
            mStorageManager = updatedStorageManager;
            mAccount = AccountUtils.getCurrentOwnCloudAccount(mContext);
        }

        if (mStorageManager != null) {
            if (mOnlyAvailableOffline && (folder.equals(updatedStorageManager.getFileByPath(OCFile.ROOT_PATH)) ||
                    !folder.isAvailableOffline())) {
                mImmutableFilesList = updatedStorageManager.getAvailableOfflineFilesFromCurrentAccount();
            } else {
                mImmutableFilesList = mStorageManager.getFolderContent(folder, mOnlyAvailableOffline);
            }

            mFiles = mImmutableFilesList;

            if (mJustFolders) {
                mFiles = getFolders(mFiles);
            }
        } else {
            mFiles = null;
        }

        mFiles = FileStorageUtils.sortFolder(mFiles, FileStorageUtils.mSortOrderFileDisp,
                FileStorageUtils.mSortAscendingFileDisp);
        notifyDataSetChanged();
    }

    /**
     * Filter for getting only the folders
     *
     * @param files Collection of files to filter
     * @return Folders in the input
     */
    public Vector<OCFile> getFolders(Vector<OCFile> files) {
        Vector<OCFile> ret = new Vector<>();
        OCFile current;
        for (int i = 0; i < files.size(); i++) {
            current = files.get(i);
            if (current.isFolder()) {
                ret.add(current);
            }
        }
        return ret;
    }

    public void setSortOrder(Integer order, boolean ascending) {

        PreferenceManager.setSortOrder(order, mContext, FileStorageUtils.FILE_DISPLAY_SORT);
        PreferenceManager.setSortAscending(ascending, mContext, FileStorageUtils.FILE_DISPLAY_SORT);

        FileStorageUtils.mSortOrderFileDisp = order;
        FileStorageUtils.mSortAscendingFileDisp = ascending;

        mFiles = FileStorageUtils.sortFolder(mFiles, FileStorageUtils.mSortOrderFileDisp,
                FileStorageUtils.mSortAscendingFileDisp);
        notifyDataSetChanged();
    }

    public ArrayList<OCFile> getCheckedItems(AbsListView parentList) {
        SparseBooleanArray checkedPositions = parentList.getCheckedItemPositions();
        ArrayList<OCFile> files = new ArrayList<>();
        Object item;
        for (int i = 0; i < checkedPositions.size(); i++) {
            if (checkedPositions.valueAt(i)) {
                item = getItem(checkedPositions.keyAt(i));
                if (item != null) {
                    files.add((OCFile) item);
                }
            }
        }
        return files;
    }

    public void filterBySearch(String query) {
        query = query.toLowerCase();

        clearFilterBySearch();

        List<OCFile> filteredList = new ArrayList<>();

        // Gather files matching the query
        for (OCFile fileToAdd : mFiles) {
            final String nameOfTheFileToAdd = fileToAdd.getFileName().toLowerCase();
            if (nameOfTheFileToAdd.contains(query)) {
                filteredList.add(fileToAdd);
            }
        }

        // Remove not matching files from the filelist
        for (int i = mFiles.size() - 1; i >= 0; i--) {
            if (!filteredList.contains(mFiles.get(i))) {
                mFiles.remove(i);
            }
        }

        // Add matching files to the filelist
        for (int i = 0; i < filteredList.size(); i++) {
            if (!mFiles.contains(filteredList.get(i))) {
                mFiles.add(i, filteredList.get(i));
            }
        }

        notifyDataSetChanged();
    }

    public void clearFilterBySearch() {
        mFiles = (Vector<OCFile>) mImmutableFilesList.clone();
        notifyDataSetChanged();
    }
}