package org.tasks.reminders;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import com.todoroo.astrid.dao.TaskDao;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import org.tasks.injection.ActivityComponent;
import org.tasks.injection.InjectingAppCompatActivity;
import org.tasks.intents.TaskIntents;
import org.tasks.notifications.NotificationManager;
import org.tasks.receivers.CompleteTaskReceiver;
import timber.log.Timber;

public class NotificationActivity extends InjectingAppCompatActivity
    implements NotificationDialog.NotificationHandler {

  public static final String EXTRA_TITLE = "extra_title";
  public static final String EXTRA_TASK_ID = "extra_task_id";
  private static final String FRAG_TAG_NOTIFICATION_FRAGMENT = "frag_tag_notification_fragment";
  @Inject NotificationManager notificationManager;
  @Inject TaskDao taskDao;

  private long taskId;
  private CompositeDisposable disposables;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setup(getIntent());
  }

  @Override
  public void inject(ActivityComponent component) {
    component.inject(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    setup(intent);
  }

  private void setup(Intent intent) {

    taskId = intent.getLongExtra(EXTRA_TASK_ID, 0L);

    FragmentManager fragmentManager = getSupportFragmentManager();
    NotificationDialog fragment =
        (NotificationDialog) fragmentManager.findFragmentByTag(FRAG_TAG_NOTIFICATION_FRAGMENT);
    if (fragment == null) {
      fragment = new NotificationDialog();
      fragment.show(fragmentManager, FRAG_TAG_NOTIFICATION_FRAGMENT);
    }
    fragment.setTitle(intent.getStringExtra(EXTRA_TITLE));
  }

  @Override
  public void dismiss() {
    finish();
  }

  @Override
  protected void onResume() {
    super.onResume();

    disposables = new CompositeDisposable();
  }

  @Override
  protected void onPause() {
    super.onPause();

    disposables.dispose();
  }

  @Override
  public void edit() {
    notificationManager.cancel(taskId);
    disposables.add(
        Single.fromCallable(() -> taskDao.fetch(taskId))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                task -> {
                  startActivity(TaskIntents.getEditTaskIntent(this, task));
                  finish();
                },
                e -> Timber.e("Task not found: %s", taskId)));
  }

  @Override
  public void snooze() {
    finish();
    Intent intent = new Intent(this, SnoozeActivity.class);
    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
    intent.putExtra(SnoozeActivity.EXTRA_TASK_ID, taskId);
    startActivity(intent);
  }

  @Override
  public void complete() {
    Intent intent = new Intent(this, CompleteTaskReceiver.class);
    intent.putExtra(CompleteTaskReceiver.TASK_ID, taskId);
    intent.putExtra(CompleteTaskReceiver.TOGGLE_STATE, false);
    sendBroadcast(intent);
    finish();
  }
}
