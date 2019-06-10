package org.tasks.injection;

import com.todoroo.astrid.activity.TaskEditFragment;
import com.todoroo.astrid.activity.TaskListFragment;
import com.todoroo.astrid.files.FilesControlSet;
import com.todoroo.astrid.repeats.RepeatControlSet;
import com.todoroo.astrid.tags.TagsControlSet;
import com.todoroo.astrid.timers.TimerControlSet;
import com.todoroo.astrid.ui.EditTitleControlSet;
import com.todoroo.astrid.ui.HideUntilControlSet;
import com.todoroo.astrid.ui.ReminderControlSet;
import dagger.Subcomponent;
import org.tasks.fragments.CommentBarFragment;
import org.tasks.ui.CalendarControlSet;
import org.tasks.ui.DeadlineControlSet;
import org.tasks.ui.DescriptionControlSet;
import org.tasks.ui.LocationControlSet;
import org.tasks.ui.NavigationDrawerFragment;
import org.tasks.ui.PriorityControlSet;
import org.tasks.ui.RemoteListFragment;

@Subcomponent(modules = FragmentModule.class)
public interface FragmentComponent {

  void inject(TimerControlSet timerControlSet);

  void inject(TaskEditFragment taskEditFragment);

  void inject(NavigationDrawerFragment navigationDrawerFragment);

  void inject(PriorityControlSet priorityControlSet);

  void inject(RepeatControlSet repeatControlSet);

  void inject(CommentBarFragment commentBarFragment);

  void inject(EditTitleControlSet editTitleControlSet);

  void inject(FilesControlSet filesControlSet);

  void inject(TagsControlSet tagsControlSet);

  void inject(HideUntilControlSet hideUntilControlSet);

  void inject(ReminderControlSet reminderControlSet);

  void inject(DeadlineControlSet deadlineControlSet);

  void inject(DescriptionControlSet descriptionControlSet);

  void inject(CalendarControlSet calendarControlSet);

  void inject(TaskListFragment taskListFragment);

  void inject(RemoteListFragment remoteListFragment);

  void inject(LocationControlSet locationControlSet);
}
