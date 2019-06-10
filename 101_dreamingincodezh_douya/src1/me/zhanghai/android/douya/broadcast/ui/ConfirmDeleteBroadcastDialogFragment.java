/*
 * Copyright (c) 2015 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.broadcast.ui;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import me.zhanghai.android.douya.R;

public class ConfirmDeleteBroadcastDialogFragment extends AppCompatDialogFragment {

    /**
     * @deprecated Use {@link #newInstance()} instead.
     */
    public ConfirmDeleteBroadcastDialogFragment() {}

    public static ConfirmDeleteBroadcastDialogFragment newInstance() {
        //noinspection deprecation
        return new ConfirmDeleteBroadcastDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity(), getTheme())
                .setMessage(R.string.broadcast_delete_confirm)
                .setPositiveButton(R.string.ok, (dialog, which) -> getListener().deleteBroadcast())
                .setNegativeButton(R.string.cancel, null)
                .create();
    }

    private Listener getListener() {
        return (Listener) getParentFragment();
    }

    public static void show(Fragment fragment) {
        ConfirmDeleteBroadcastDialogFragment.newInstance()
                .show(fragment.getChildFragmentManager(), null);
    }

    public interface Listener {
        void deleteBroadcast();
    }
}
