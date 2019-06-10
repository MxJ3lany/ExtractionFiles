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
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AdapterImage extends RecyclerView.Adapter<AdapterImage.ViewHolder> {
    private Context context;
    private LayoutInflater inflater;
    private LifecycleOwner owner;

    private List<EntityAttachment> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View view;
        private ImageView ivImage;
        private TextView tvCaption;
        private TextView tvType;

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            ivImage = itemView.findViewById(R.id.ivImage);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvType = itemView.findViewById(R.id.tvType);
        }

        private void wire() {
            view.setOnClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
        }

        private void bindTo(EntityAttachment attachment) {
            if (attachment.available) {
                Bitmap bm = Helper.decodeImage(attachment.getFile(context),
                        context.getResources().getDisplayMetrics().widthPixels);
                if (bm == null)
                    ivImage.setImageResource(R.drawable.baseline_broken_image_24);
                else
                    ivImage.setImageBitmap(bm);
            } else
                ivImage.setImageResource(attachment.progress == null
                        ? R.drawable.baseline_image_24 : R.drawable.baseline_hourglass_empty_24);

            tvCaption.setVisibility(TextUtils.isEmpty(attachment.name) ? View.GONE : View.VISIBLE);

            tvCaption.setText(attachment.name);
            tvType.setText(attachment.type);
        }

        @Override
        public void onClick(View view) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityAttachment attachment = items.get(pos);
            if (attachment.available) {
                // Build file name
                File file = attachment.getFile(context);

                // https://developer.android.com/reference/android/support/v4/content/FileProvider
                final Uri uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID, file);
                Log.i("uri=" + uri);

                // Build intent
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, attachment.type);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (!TextUtils.isEmpty(attachment.name))
                    intent.putExtra(Intent.EXTRA_TITLE, attachment.name);
                Log.i("Sharing " + file + " type=" + attachment.type);
                Log.i("Intent=" + intent);

                // Get targets
                PackageManager pm = context.getPackageManager();
                List<ResolveInfo> ris = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                for (ResolveInfo ri : ris) {
                    Log.i("Target=" + ri);
                    context.grantUriPermission(ri.activityInfo.packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }

                // Check if viewer available
                if (ris.size() == 0) {
                    Toast.makeText(context, context.getString(R.string.title_no_viewer, attachment.type), Toast.LENGTH_LONG).show();
                    return;
                }

                context.startActivity(intent);
            } else {
                if (attachment.progress == null) {
                    Bundle args = new Bundle();
                    args.putLong("id", attachment.id);
                    args.putLong("message", attachment.message);

                    new SimpleTask<Void>() {
                        @Override
                        protected Void onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            long message = args.getLong("message");

                            DB db = DB.getInstance(context);
                            try {
                                db.beginTransaction();

                                db.attachment().setProgress(id, 0);

                                EntityMessage msg = db.message().getMessage(message);
                                EntityOperation.queue(context, msg, EntityOperation.ATTACHMENT, id);

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
                    }.execute(context, owner, args, "image:fetch");
                }
            }
        }
    }

    AdapterImage(Context context, LifecycleOwner owner) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.owner = owner;
        setHasStableIds(true);
    }

    public void set(@NonNull List<EntityAttachment> attachments) {
        Log.i("Set images=" + attachments.size());

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
        return new ViewHolder(inflater.inflate(R.layout.item_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();

        EntityAttachment attachment = items.get(position);
        holder.bindTo(attachment);

        holder.wire();
    }
}
