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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.lifecycle.LifecycleOwner;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListUpdateCallback;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AdapterRule extends RecyclerView.Adapter<AdapterRule.ViewHolder> {
    private Context context;
    private LifecycleOwner owner;
    private LayoutInflater inflater;

    private List<TupleRuleEx> items = new ArrayList<>();

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private View view;
        private TextView tvName;
        private TextView tvOrder;
        private ImageView ivStop;
        private TextView tvCondition;
        private TextView tvAction;

        private TwoStateOwner powner = new TwoStateOwner(owner, "RulePopup");

        ViewHolder(View itemView) {
            super(itemView);

            view = itemView.findViewById(R.id.clItem);
            tvName = itemView.findViewById(R.id.tvName);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            ivStop = itemView.findViewById(R.id.ivStop);
            tvCondition = itemView.findViewById(R.id.tvCondition);
            tvAction = itemView.findViewById(R.id.tvAction);
        }

        private void wire() {
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        private void unwire() {
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
        }

        private void bindTo(TupleRuleEx rule) {
            view.setActivated(!rule.enabled);
            tvName.setText(rule.name);
            tvOrder.setText(Integer.toString(rule.order));
            ivStop.setVisibility(rule.stop ? View.VISIBLE : View.INVISIBLE);

            try {
                List<String> condition = new ArrayList<>();
                JSONObject jcondition = new JSONObject(rule.condition);
                if (jcondition.has("sender"))
                    condition.add(context.getString(R.string.title_rule_sender));
                if (jcondition.has("recipient"))
                    condition.add(context.getString(R.string.title_rule_recipient));
                if (jcondition.has("subject"))
                    condition.add(context.getString(R.string.title_rule_subject));
                if (jcondition.has("header"))
                    condition.add(context.getString(R.string.title_rule_header));
                tvCondition.setText(TextUtils.join(" & ", condition));
            } catch (Throwable ex) {
                tvCondition.setText(ex.getMessage());
            }

            try {
                JSONObject jaction = new JSONObject(rule.action);
                int type = jaction.getInt("type");
                switch (type) {
                    case EntityRule.TYPE_SEEN:
                        tvAction.setText(R.string.title_rule_seen);
                        break;
                    case EntityRule.TYPE_UNSEEN:
                        tvAction.setText(R.string.title_rule_unseen);
                        break;
                    case EntityRule.TYPE_SNOOZE:
                        tvAction.setText(R.string.title_rule_snooze);
                        break;
                    case EntityRule.TYPE_FLAG:
                        tvAction.setText(R.string.title_rule_flag);
                        break;
                    case EntityRule.TYPE_MOVE:
                        tvAction.setText(R.string.title_rule_move);
                        break;
                    case EntityRule.TYPE_COPY:
                        tvAction.setText(R.string.title_rule_copy);
                        break;
                    case EntityRule.TYPE_ANSWER:
                        tvAction.setText(R.string.title_rule_answer);
                        break;
                    case EntityRule.TYPE_AUTOMATION:
                        tvAction.setText(R.string.title_rule_automation);
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown action type=" + type);
                }
            } catch (Throwable ex) {
                tvAction.setText(ex.getMessage());
            }
        }

        @Override
        public void onClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return;

            TupleRuleEx rule = items.get(pos);

            LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(context);
            lbm.sendBroadcast(
                    new Intent(ActivityView.ACTION_EDIT_RULE)
                            .putExtra("id", rule.id)
                            .putExtra("account", rule.account)
                            .putExtra("folder", rule.folder));
        }

        @Override
        public boolean onLongClick(View v) {
            int pos = getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION)
                return false;

            final TupleRuleEx rule = items.get(pos);

            PopupMenuLifecycle popupMenu = new PopupMenuLifecycle(context, powner, view);

            popupMenu.getMenu().add(Menu.NONE, R.string.title_rule_enabled, 1, R.string.title_rule_enabled)
                    .setCheckable(true).setChecked(rule.enabled);

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.string.title_rule_enabled:
                            onActionEnabled(!item.isChecked());
                            return true;
                        default:
                            return false;
                    }
                }

                private void onActionEnabled(boolean enabled) {
                    Bundle args = new Bundle();
                    args.putLong("id", rule.id);
                    args.putBoolean("enabled", enabled);

                    new SimpleTask<Boolean>() {
                        @Override
                        protected Boolean onExecute(Context context, Bundle args) {
                            long id = args.getLong("id");
                            boolean enabled = args.getBoolean("enabled");

                            DB db = DB.getInstance(context);
                            db.rule().setRuleEnabled(id, enabled);

                            return enabled;
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

    AdapterRule(Context context, LifecycleOwner owner) {
        this.context = context;
        this.owner = owner;
        this.inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public void set(@NonNull List<TupleRuleEx> rules) {
        Log.i("Set rules=" + rules.size());

        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new DiffCallback(items, rules), false);

        items = rules;

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
        private List<TupleRuleEx> prev = new ArrayList<>();
        private List<TupleRuleEx> next = new ArrayList<>();

        DiffCallback(List<TupleRuleEx> prev, List<TupleRuleEx> next) {
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
            TupleRuleEx r1 = prev.get(oldItemPosition);
            TupleRuleEx r2 = next.get(newItemPosition);
            return r1.id.equals(r2.id);
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            TupleRuleEx r1 = prev.get(oldItemPosition);
            TupleRuleEx r2 = next.get(newItemPosition);
            return r1.equals(r2);
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
        return new ViewHolder(inflater.inflate(R.layout.item_rule, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.unwire();
        TupleRuleEx rule = items.get(position);
        holder.bindTo(rule);
        holder.wire();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.powner.recreate();
    }
}
