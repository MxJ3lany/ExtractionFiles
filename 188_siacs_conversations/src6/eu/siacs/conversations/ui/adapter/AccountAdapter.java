package eu.siacs.conversations.ui.adapter;

import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import eu.siacs.conversations.Config;
import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.AccountRowBinding;
import eu.siacs.conversations.entities.Account;
import eu.siacs.conversations.ui.XmppActivity;
import eu.siacs.conversations.ui.util.AvatarWorkerTask;
import eu.siacs.conversations.ui.util.StyledAttributes;
import eu.siacs.conversations.utils.UIHelper;

public class AccountAdapter extends ArrayAdapter<Account> {

    private XmppActivity activity;
    private boolean showStateButton;

    public AccountAdapter(XmppActivity activity, List<Account> objects, boolean showStateButton) {
        super(activity, 0, objects);
        this.activity = activity;
        this.showStateButton = showStateButton;
    }

    public AccountAdapter(XmppActivity activity, List<Account> objects) {
        super(activity, 0, objects);
        this.activity = activity;
        this.showStateButton = true;
    }

    @Override
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        final Account account = getItem(position);
        final ViewHolder viewHolder;
        if (view == null) {
            AccountRowBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.account_row, parent, false);
            view = binding.getRoot();
            viewHolder = new ViewHolder(binding);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        if (Config.DOMAIN_LOCK != null) {
            viewHolder.binding.accountJid.setText(account.getJid().getLocal());
        } else {
            viewHolder.binding.accountJid.setText(account.getJid().asBareJid().toString());
        }
        AvatarWorkerTask.loadAvatar(account, viewHolder.binding.accountImage, R.dimen.avatar);
        viewHolder.binding.accountStatus.setText(getContext().getString(account.getStatus().getReadableId()));
        switch (account.getStatus()) {
            case ONLINE:
                viewHolder.binding.accountStatus.setTextColor(StyledAttributes.getColor(activity, R.attr.TextColorOnline));
                break;
            case DISABLED:
            case CONNECTING:
                viewHolder.binding.accountStatus.setTextColor(StyledAttributes.getColor(activity, android.R.attr.textColorSecondary));
                break;
            default:
                viewHolder.binding.accountStatus.setTextColor(StyledAttributes.getColor(activity, R.attr.TextColorError));
                break;
        }
        final boolean isDisabled = (account.getStatus() == Account.State.DISABLED);
        viewHolder.binding.tglAccountStatus.setOnCheckedChangeListener(null);
        viewHolder.binding.tglAccountStatus.setChecked(!isDisabled);
        if (this.showStateButton) {
            viewHolder.binding.tglAccountStatus.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.tglAccountStatus.setVisibility(View.GONE);
        }
        viewHolder.binding.tglAccountStatus.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b == isDisabled && activity instanceof OnTglAccountState) {
                ((OnTglAccountState) activity).onClickTglAccountState(account, b);
            }
        });
        return view;
    }

    private static class ViewHolder {
        private final AccountRowBinding binding;

        private ViewHolder(AccountRowBinding binding) {
            this.binding = binding;
        }
    }



    public interface OnTglAccountState {
        void onClickTglAccountState(Account account, boolean state);
    }

}
