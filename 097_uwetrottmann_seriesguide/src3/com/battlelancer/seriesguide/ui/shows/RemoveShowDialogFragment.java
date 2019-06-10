package com.battlelancer.seriesguide.ui.shows;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.FragmentManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.battlelancer.seriesguide.R;
import com.battlelancer.seriesguide.provider.SeriesGuideContract.Shows;
import com.battlelancer.seriesguide.sync.SgSyncAdapter;
import com.battlelancer.seriesguide.util.DialogTools;
import com.battlelancer.seriesguide.util.tasks.RemoveShowTask;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Dialog asking if a show should be removed from the database. On confirmation launches {@link
 * RemoveShowTask}.
 */
public class RemoveShowDialogFragment extends AppCompatDialogFragment {

    private static final String KEY_SHOW_TVDB_ID = "show_tvdb_id";

    public static class ShowTitleEvent {
        public String showTitle;
    }

    /**
     * If no current sync is active and it is safe shows the dialog.
     */
    public static boolean show(Context context, FragmentManager fragmentManager, int showTvdbId) {
        if (SgSyncAdapter.isSyncActive(context, true)) {
            return false;
        }

        RemoveShowDialogFragment f = new RemoveShowDialogFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_SHOW_TVDB_ID, showTvdbId);
        f.setArguments(args);

        return DialogTools.safeShow(f, fragmentManager, "removeShowDialog");
    }

    @BindView(R.id.progressBarRemove) View progressBar;
    @BindView(R.id.textViewRemove) TextView dialogText;
    @BindView(R.id.buttonNegative) Button negativeButton;
    @BindView(R.id.buttonPositive) Button positiveButton;

    private Unbinder unbinder;
    private int showTvdbId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showTvdbId = getArguments().getInt(KEY_SHOW_TVDB_ID);
        if (showTvdbId <= 0) {
            dismiss();
        }

        // hide title, use custom theme
        setStyle(STYLE_NO_TITLE, 0);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_remove, container, false);
        unbinder = ButterKnife.bind(this, view);

        showProgressBar(true);
        negativeButton.setText(android.R.string.cancel);
        negativeButton.setOnClickListener(v -> dismiss());
        positiveButton.setText(R.string.delete_show);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        EventBus.getDefault().register(this);

        new GetShowTitleTask(getActivity())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, showTvdbId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        EventBus.getDefault().unregister(this);
        unbinder.unbind();
    }

    private static class GetShowTitleTask extends AsyncTask<Integer, Void, ShowTitleEvent> {

        @SuppressLint("StaticFieldLeak") // using application context
        private final Context context;

        public GetShowTitleTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected ShowTitleEvent doInBackground(Integer... params) {
            int showTvdbId = params[0];

            ShowTitleEvent result = new ShowTitleEvent();

            // get show title
            final Cursor show = context.getContentResolver().query(
                    Shows.buildShowUri(showTvdbId),
                    new String[] {
                            Shows.TITLE
                    }, null, null, null
            );
            if (show != null) {
                if (show.moveToFirst()) {
                    result.showTitle = show.getString(0);
                }
                show.close();
            }

            return result;
        }

        @Override
        protected void onPostExecute(ShowTitleEvent result) {
            EventBus.getDefault().post(result);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ShowTitleEvent event) {
        if (event.showTitle == null) {
            // failed to find show
            Toast.makeText(getContext(), R.string.delete_error, Toast.LENGTH_LONG).show();
            dismiss();
            return;
        }

        dialogText.setText(getString(R.string.confirm_delete, event.showTitle));
        positiveButton.setOnClickListener(v -> {
            RemoveShowTask.execute(getContext(), showTvdbId);
            dismiss();
        });

        showProgressBar(false);
    }

    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        dialogText.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        positiveButton.setEnabled(!isVisible);
    }
}
