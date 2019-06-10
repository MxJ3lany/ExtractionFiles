package io.plactal.eoscommander.ui.suggestion;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;

import io.plactal.eoscommander.R;
import io.plactal.eoscommander.app.EosCommanderApp;
import io.plactal.eoscommander.data.EoscDataManager;
import io.plactal.eoscommander.util.StringUtils;
import io.plactal.eoscommander.util.rx.EoscSchedulerProvider;
import io.plactal.eoscommander.util.rx.SchedulerProvider;
import io.reactivex.Completable;

/**
 * Created by swapnibble on 2018-01-15.
 */

public class AccountAdapter extends ArrayAdapter<String> implements View.OnClickListener {

    private List<String> mData = new ArrayList<>();
    private AccountFilter mFilter;


    public AccountAdapter(@NonNull Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    @Override
    public @Nullable
    String getItem(int position) {
        return mData.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if ( null == convertView ) {
            LayoutInflater inflater = (LayoutInflater) getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView =  inflater.inflate( R.layout.account_suggestion, parent, false );

            holder = new ViewHolder(convertView);
            convertView.setTag( holder );


            holder.delete.setOnClickListener( this );
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        String eosAccountName = getItem( position );
        holder.delete.setTag( eosAccountName );

        if ( null == eosAccountName ) {
            holder.account_name.setText("");
            holder.delete.setOnClickListener( null );
            return convertView;
        }

        holder.account_name.setText( eosAccountName );

        return convertView;
    }

    private EoscDataManager getDataMgr() {
        return ((EosCommanderApp) getContext().getApplicationContext())
                .getAppComponent().dataManager();
    }

    private void deleteAccountHistory(String accountName) {

        SchedulerProvider schedulerProvider = new EoscSchedulerProvider();

        Completable.fromAction (() -> getDataMgr().deleteAccountHistory(accountName)) // delete history item
                .subscribeOn( schedulerProvider.io())
                .observeOn( schedulerProvider.ui())
                .subscribe(() -> {
                            Toast.makeText(getContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                        }
                        , e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public @NonNull Filter getFilter() {
        if (mFilter == null) {
            mFilter = new AccountFilter();
        }
        return mFilter;
    }

    @Override
    public void onClick(View view) {
        if ( ( null != view) && (view.getId() == R.id.del_history) && ( null != view.getTag())) {
            String accountName = (String) view.getTag();//getItem( (Integer)view.getTag());
            if ( StringUtils.isEmpty( accountName)) {
                return;
            }


            AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext())
                    .setTitle( R.string.delete_confirm)
                    .setMessage( String.format( getContext().getString(R.string.ru_sure_delete), accountName))
                    .setPositiveButton(android.R.string.yes, ( dlg, btnId) -> deleteAccountHistory( accountName))
                    .setNegativeButton(android.R.string.no, null);

            alertDialog.create().show();
        }
    }

    private class ViewHolder {
        private TextView account_name;
        private View delete;
        public ViewHolder(View itemView) {
            account_name = itemView.findViewById(R.id.eos_account);
            delete = itemView.findViewById(R.id.del_history);
        }
    }


    private class AccountFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (StringUtils.isEmpty(constraint) ){
                return results;
            }

            List<String> list = getDataMgr().searchAccount( constraint.toString());
            results.values = list;
            results.count = list.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            //noinspection unchecked
            mData =(List<String>) filterResults.values;
            if (filterResults.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
