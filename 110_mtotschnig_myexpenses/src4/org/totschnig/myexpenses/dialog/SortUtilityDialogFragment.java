package org.totschnig.myexpenses.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

import com.annimon.stream.Stream;

import org.totschnig.myexpenses.R;
import org.totschnig.myexpenses.adapter.RecyclerListAdapter;
import org.totschnig.myexpenses.adapter.helper.OnStartDragListener;
import org.totschnig.myexpenses.adapter.helper.SimpleItemTouchHelperCallback;

import java.util.AbstractMap;
import java.util.ArrayList;

public class SortUtilityDialogFragment extends CommitSafeDialogFragment implements OnStartDragListener, DialogInterface.OnClickListener {
  private static final String KEY_ITEMS = "items";
  private ItemTouchHelper mItemTouchHelper;
  private OnConfirmListener callback;
  private RecyclerListAdapter adapter;

  public interface OnConfirmListener {
    void onSortOrderConfirmed(long[] sortedIds);
  }

  public static SortUtilityDialogFragment newInstance(ArrayList<AbstractMap.SimpleEntry<Long, String>> items) {
    final SortUtilityDialogFragment fragment = new SortUtilityDialogFragment();
    Bundle args = new Bundle(1);
    args.putSerializable(KEY_ITEMS, items);
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    try {
      callback = (OnConfirmListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString()
          + " must implement OnConfirmListener");
    }
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle args = savedInstanceState != null ? savedInstanceState : getArguments();
    adapter = new RecyclerListAdapter( this, (ArrayList<AbstractMap.SimpleEntry<Long, String>>) args.getSerializable(KEY_ITEMS));

    RecyclerView recyclerView = new RecyclerView(getActivity());
    recyclerView.setHasFixedSize(true);
    recyclerView.setAdapter(adapter);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recyclerView);
    return new AlertDialog.Builder(getActivity())
        .setTitle(R.string.sort_order)
        .setPositiveButton(android.R.string.ok, this)
        .setNegativeButton(android.R.string.cancel, null)
        .setView(recyclerView)
        .create();
  }

  @Override
  public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
    mItemTouchHelper.startDrag(viewHolder);
  }

  @Override
  public void onClick(DialogInterface dialog, int which) {
    callback.onSortOrderConfirmed(Stream.of(adapter.getItems()).mapToLong(AbstractMap.SimpleEntry::getKey).toArray());
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(KEY_ITEMS, adapter.getItems());
  }
}
