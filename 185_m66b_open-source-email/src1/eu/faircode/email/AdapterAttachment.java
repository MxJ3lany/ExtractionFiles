package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterAttachment extends RecyclerView.Adapter<AdapterAttachment.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private boolean readonly;
    private boolean debug;

    private List<EntityAttachment> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private ImageButton ibDelete;
        private TextView tvName;
        private TextView tvSize;
        private ImageView ivStatus;
        private ImageButton ibSave;
        private TextView tvType;
        private TextView tvDebug;
        private ProgressBar progressbar;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ibDelete = itemView.findViewById(R.id.ibDelete);
            tvName = itemView.findViewById(R.id.tvName);
            tvSize = itemView.findViewById(R.id.tvSize);
            ivStatus = itemView.findViewById(R.id.ivStatus);
            ibSave = itemView.findViewById(R.id.ibSave);
            tvType = itemView.findViewById(R.id.tvType);
            tvDebug = itemView.findViewById(R.id.tvDebug);
            progressbar = itemView.findViewById(R.id.progressbar);
        }

        private void wire() {
            view.setOnClickListener(this);
            ibDelete.setOnClickListener(this);
            ibSave.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            ibDelete.setOnClickListener(null);
            ibSave.setOnClickListener(null);
        }

        private void bindTo(EntityAttachment attachment) {
            view.setAlpha(attachment.isInline() ? Helper.LOW_LIGHT : 1.0f);

            ibDelete.setVisibility(readonly ? View.GONE : attachment.isInline() ? View.INVISIBLE : View.VISIBLE);
            tvName.setText(attachment.name);

            if (attachment.size != null)
                tvSize.setText(Helper.humanReadableByteCount(attachment.size, true));
            tvSize.setVisibility(attachment.size == null ? View.GONE : View.VISIBLE);

            if (attachment.available) {
                ivStatus.setImageResource(R.drawable.baseline_visibility_24);
                ivStatus.setVisibility(View.VISIBLE);
            } else {
                if (attachment.progress == null) {
                    ivStatus.setImageResource(R.drawable.baseline_cloud_download_24);
                    ivStatus.setVisibility(View.VISIBLE);
                } else
                    ivStatus.setVisibility(View.GONE);
            }

            ibSave.setVisibility(readonly && attachment.available ? View.VISIBLE : View.GONE);

            if (attachment.progress != null)
                progressbar.setProgress(attachment.progress);
            progressbar.setVisibility(
                    attachment.progress == null || attachment.available ? View.GONE : View.VISIBLE);

            tvType.setText(attachment.type + " " + attachment.disposition + " " + attachment.cid + "/" + attachment.encryption);
            tvType.setVisibility(debug || BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

            tvDebug.setText(attachment.error);
            tvDebug.setVisibility(attachment.error == null ? View.GONE : View.VISIBLE);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;
            final EntityAttachment attachment = items.get(pos);

            if (view.getId() == R.id.ibDelete)
                onDelete(attachment);
            else if (view.getId() == R.id.ibSave)
                onSave(attachment);
            else {
                if (attachment.available)
                    onShare(attachment);
                else {
                    if (attachment.progress == null)
                        onDownload(attachment);
                }
            }
        }

        private void onDelete(final EntityAttachment attachment) {
            Bundle args = new Bundle();
            args.putLong("id", attachment.id);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) throws IOException {
                    long id = args.getLong("id");

                    DB db = DB.getInstance(context);
                    EntityAttachment attachment = db.attachment().getAttachment(id);
                    if (attachment == null)
                        return null;
                    db.attachment().setDownloaded(id, null);
                    attachment.getFile(context).delete();
                    db.attachment().deleteAttachment(id);

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "attachment:delete");
        }

        private void onSave(EntityAttachment attachment) {
            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_STORE_ATTACHMENT)
                            .putExtra("id", attachment.id)
                            .putExtra("name", Helper.sanitizeFilename(attachment.name))
                            .putExtra("type", attachment.type));
        }

        private void onShare(EntityAttachment attachment) {
            // https://developer.android.com/reference/android/support/v4/content/FileProvider
            File file = attachment.getFile(context);
            final Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
            Log.i("uri=" + uri);

            // Build intent
            final Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, attachment.type);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (!TextUtils.isEmpty(attachment.name))
                intent.putExtra(Intent.EXTRA_TITLE, Helper.sanitizeFilename(attachment.name));
            Log.i("Intent=" + intent + " type=" + attachment.type);

            // Get targets
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> ris = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo ri : ris) {
                Log.i("Target=" + ri);
                context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            // Check if viewer available
            if (ris.size() == 0)
                Toast.makeText(context, context.getString(R.string.title_no_viewer, attachment.type), Toast.LENGTH_LONG).show();
            else
                context.startActivity(intent);
        }

        private void onDownload(EntityAttachment attachment) {
            Bundle args = new Bundle();
            args.putLong("id", attachment.id);
            args.putLong("message", attachment.message);
            args.putInt("sequence", attachment.sequence);

            new SimpleTask<Void>() {
                @Override
                protected Void onExecute(Context context, Bundle args) {
                    long id = args.getLong("id");
                    long mid = args.getLong("message");

                    DB db = DB.getInstance(context);
                    try {
                        db.beginTransaction();

                        EntityMessage message = db.message().getMessage(mid);
                        if (message == null || message.uid == null)
                            return null;

                        db.attachment().setProgress(id, 0);

                        EntityOperation.queue(context, message, EntityOperation.ATTACHMENT, id);

                        db.setTransactionSuccessful();
                    } finally {
                        db.endTransaction();
                    }

                    return null;
                }

                @Override
                protected void onException(Bundle args, Throwable ex) {
                    Helper.unexpectedError(context, owner, ex);
                }
            }.execute(context, owner, args, "attachment:fetch");
        }

        private class NameResolveInfo {
            Drawable icon;
            String name;
            ResolveInfo info;

            NameResolveInfo(Drawable icon, String name, ResolveInfo info) {
                this.icon = icon;
                this.name = name;
                this.info = info;
            }
        }
    }

    AdapterAttachment(Context context, LifecycleOwner owner, boolean readonly) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
        this.readonly = readonly;
        this.debug = prefs.getBoolean("debug", false);
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set attachments=" + attachments.size());

        Collections.sort(attachments, new Comparator<EntityAttachment>() {
            @Override
            public int compare(EntityAttachment a1, EntityAttachment a2) {
                return a1.sequence.compareTo(a2.sequence);
            }
        });

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, attachments), false);

        items = attachments;

        diff.dispatchUpdatesTo(new ListUpdateCallback() {
            @Override
            public void onInserted(int position, int count) {
                Log.i("Inserted @" + position + " #" + count);
            }

            @Override
            public void onRemoved(int position, int count) {
                Log.i("Removed @" + position + " #" + count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                Log.i("Moved " + fromPosition + ">" + toPosition);
            }

            @Override
            public void onChanged(int position, int count, Object payload) {
                Log.i("Changed @" + position + " #" + count);
            }
        });
        diff.dispatchUpdatesTo(this);
    }

    private class DiffCallback extends DiffUtil.Callback {
        private List<EntityAttachment> prev = new ArrayList<>();
        private List<EntityAttachment> next = new ArrayList<>();

        DiffCallback(List<EntityAttachment> prev, List<EntityAttachment> next) {
            this.prev.addAll(prev);
            this.next.addAll(next);
        }

        @Override
        public int getOldListSize() {
            return prev.size();
        }

        @Override
        public int getNewListSize() {
            return next.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAttachment a1 = prev.get(oldItemPosition);
            EntityAttachment a2 = next.get(newItemPosition);
            return a1.equals(a2);
        }
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).id;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.item_attachment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = items.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }
}
