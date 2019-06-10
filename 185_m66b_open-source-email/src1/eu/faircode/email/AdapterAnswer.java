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
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AdapterAnswer extends RecyclerView.Adapter<AdapterAnswer.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<EntityAnswer> items = new ArrayList<>();

    private boolean composable = false;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private TextView tvName;

        private TwoStateOwner powner = new TwoStateOwner(owner, "RulePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(EntityAnswer answer) {
            view.setAlpha(answer.hide ? Helper.LOW_LIGHT : 1.0f);
            tvName.setText(answer.name);
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            EntityAnswer answer = items.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_EDIT_ANSWER)
                            .putExtra("id", answer.id));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final EntityAnswer answer = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            if (composable)
                popupMenu.getMenu().add(Menu.NONE, R.string.title_compose, 1, R.string.title_compose);
            popupMenu.getMenu().add(Menu.NONE, R.string.title_answer_hide, 2, R.string.title_answer_hide)
                    .setCheckable(true).setChecked(answer.hide);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_compose:
                            onActionCompose();
                            return true;
                        case R.string.title_answer_hide:
                            onActionHide(!item.isChecked());
                            return true;
                        default:
                            return false;
                    }
                }

                private void onActionCompose() {
                    context.startActivity(new Intent(context, ActivityCompose.class)
                            .putExtra("action", "new")
                            .putExtra("answer", answer.id));
                }

                private void onActionHide(boolean hide) {
                    Bundle args = new Bundle();
                    args.putLong("id", answer.id);
                    args.putBoolean("hide", hide);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean hide = args.getBoolean("hide");

                            DB db = DB.getInstance(context);
                            db.answer().setAnswerHidden(id, hide);

                            return hide;
                        }

                        @Override
                        protected void onException(Bundle args, Throwable ex) {
                            Helper.unexpectedError(context, owner, ex);
                        }
                    }.execute(context, owner, args, "rule:enable");
                }
            });

            popupMenu.show();

            return true;
        }
    }

    AdapterAnswer(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);

        new SimpleTask<Boolean>() {
            @Override
            protected Boolean onExecute(Context context, Bundle args) {
                DB db = DB.getInstance(context);
                return (db.identity().getComposableIdentities(null).size() > 0);
            }

            @Override
            protected void onExecuted(Bundle args, Boolean composable) {
                AdapterAnswer.this.composable = composable;
            }

            @Override
            protected void onException(Bundle args, Throwable ex) {
                Helper.unexpectedError(AdapterAnswer.this.context, AdapterAnswer.this.owner, ex);
            }
        }.execute(context, owner, new Bundle(), "answer:composable");
    }

    public void set(@NonNull List<EntityAnswer> answers) {
        Log.i("Set answers=" + answers.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, answers), false);

        items = answers;

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
        private List<EntityAnswer> prev = new ArrayList<>();
        private List<EntityAnswer> next = new ArrayList<>();

        DiffCallback(List<EntityAnswer> prev, List<EntityAnswer> next) {
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
            EntityAnswer a1 = prev.get(oldItemPosition);
            EntityAnswer a2 = next.get(newItemPosition);
            return a1.id.equals(a2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            EntityAnswer a1 = prev.get(oldItemPosition);
            EntityAnswer a2 = next.get(newItemPosition);
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
        return new ViewHolder(inflater.inflate(R.layout.item_answer, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        EntityAnswer answer = items.get(position);
        holder.bindTo(answer);
        holder.wire();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
