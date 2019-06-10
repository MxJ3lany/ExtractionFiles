package org.tasks.ui;

import static com.todoroo.andlib.sql.Field.field;
import static com.todoroo.andlib.utility.AndroidUtilities.assertMainThread;
import static com.todoroo.astrid.activity.TaskListFragment.CALDAV_METADATA_JOIN;
import static com.todoroo.astrid.activity.TaskListFragment.GTASK_METADATA_JOIN;
import static com.todoroo.astrid.activity.TaskListFragment.TAGS_METADATA_JOIN;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource.Factory;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import androidx.sqlite.db.SimpleSQLiteQuery;
import com.google.common.collect.Lists;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.api.CaldavFilter;
import com.todoroo.astrid.api.Filter;
import com.todoroo.astrid.api.GtasksFilter;
import com.todoroo.astrid.api.PermaSql;
import com.todoroo.astrid.api.TagFilter;
import com.todoroo.astrid.core.SortHelper;
import com.todoroo.astrid.dao.Database;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Task;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.tasks.data.CaldavTask;
import org.tasks.data.GoogleTask;
import org.tasks.data.Tag;
import org.tasks.data.TaskContainer;
import org.tasks.preferences.Preferences;
import timber.log.Timber;

public class TaskListViewModel extends ViewModel implements Observer<PagedList<TaskContainer>> {

  private static final PagedList.Config PAGED_LIST_CONFIG =
      new PagedList.Config.Builder().setPageSize(20).build();

  private static final Field TASKS = field("tasks.*");
  private static final Field GTASK = field(GTASK_METADATA_JOIN + ".*");
  private static final StringProperty CALDAV =
      new StringProperty(null, CALDAV_METADATA_JOIN + ".calendar").as("caldav");
  private static final Field CHILDREN = field("children");
  private static final Field SIBLINGS = field("siblings");
  private static final Field PRIMARY_SORT = field("primary_sort").as("primarySort");
  private static final Field SECONDARY_SORT = field("secondary_sort").as("secondarySort");
  private static final StringProperty TAGS =
      new StringProperty(null, "group_concat(distinct(" + TAGS_METADATA_JOIN + ".tag_uid)" + ")")
          .as("tags");
  @Inject Preferences preferences;
  @Inject TaskDao taskDao;
  @Inject Database database;
  private MutableLiveData<List<TaskContainer>> tasks = new MutableLiveData<>();
  private Filter filter;
  private boolean manualSort;
  private CompositeDisposable disposable = new CompositeDisposable();
  private LiveData<PagedList<TaskContainer>> internal;

  public void setFilter(@NonNull Filter filter, boolean manualSort) {
    if (!filter.equals(this.filter)
        || !filter.getSqlQuery().equals(this.filter.getSqlQuery())
        || this.manualSort != manualSort) {
      this.filter = filter;
      this.manualSort = manualSort;
      tasks = new MutableLiveData<>();
      invalidate();
    }
  }

  public void observe(LifecycleOwner owner, Observer<List<TaskContainer>> observer) {
    tasks.observe(owner, observer);
  }

  private String getQuery(Filter filter) {
    List<Field> fields = Lists.newArrayList(TASKS, TAGS, GTASK, CALDAV);

    Criterion tagsJoinCriterion = Criterion.and(Task.ID.eq(field(TAGS_METADATA_JOIN + ".task")));
    Criterion gtaskJoinCriterion =
        Criterion.and(
            Task.ID.eq(field(GTASK_METADATA_JOIN + ".gt_task")),
            field(GTASK_METADATA_JOIN + ".gt_deleted").eq(0));
    Criterion caldavJoinCriterion =
        Criterion.and(
            Task.ID.eq(field(CALDAV_METADATA_JOIN + ".task")),
            field(CALDAV_METADATA_JOIN + ".deleted").eq(0));
    if (filter instanceof TagFilter) {
      String uuid = ((TagFilter) filter).getUuid();
      tagsJoinCriterion =
          Criterion.and(tagsJoinCriterion, field(TAGS_METADATA_JOIN + ".tag_uid").neq(uuid));
    } else if (filter instanceof GtasksFilter) {
      if (manualSort) {
        fields.add(CHILDREN);
        fields.add(SIBLINGS);
        fields.add(PRIMARY_SORT);
        fields.add(SECONDARY_SORT);
      }
    } else if (filter instanceof CaldavFilter) {
      String uuid = ((CaldavFilter) filter).getUuid();
      caldavJoinCriterion =
          Criterion.and(caldavJoinCriterion, field(CALDAV_METADATA_JOIN + ".calendar").neq(uuid));
    }

    // TODO: For now, we'll modify the query to join and include the things like tag data here.
    // Eventually, we might consider restructuring things so that this query is constructed
    // elsewhere.
    String joinedQuery =
        Join.left(Tag.TABLE.as(TAGS_METADATA_JOIN), tagsJoinCriterion).toString() // $NON-NLS-1$
            + Join.left(GoogleTask.TABLE.as(GTASK_METADATA_JOIN), gtaskJoinCriterion).toString()
            + Join.left(CaldavTask.TABLE.as(CALDAV_METADATA_JOIN), caldavJoinCriterion).toString()
            + filter.getSqlQuery();

    String query =
        SortHelper.adjustQueryForFlagsAndSort(preferences, joinedQuery, preferences.getSortMode());

    String groupedQuery =
        query.contains("ORDER BY")
            ? query.replace("ORDER BY", "GROUP BY " + Task.ID + " ORDER BY")
            : query + " GROUP BY " + Task.ID;

    return Query.select(fields.toArray(new Field[0]))
        .withQueryTemplate(PermaSql.replacePlaceholdersForQuery(groupedQuery))
        .from(Task.TABLE)
        .toString();
  }

  public void searchByFilter(Filter filter) {
    this.filter = filter;
    invalidate();
  }

  private void removeObserver() {
    if (internal != null) {
      internal.removeObserver(this);
    }
  }

  public void invalidate() {
    assertMainThread();

    removeObserver();

    SimpleSQLiteQuery query = new SimpleSQLiteQuery(getQuery(filter));
    Timber.v(query.getSql());
    if (manualSort) {
      disposable.add(
          Single.fromCallable(() -> taskDao.fetchTasks(query))
              .subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(tasks::setValue, Timber::e));
    } else {
      Factory<Integer, TaskContainer> factory = taskDao.getTaskFactory(query);
      LivePagedListBuilder<Integer, TaskContainer> builder = new LivePagedListBuilder<>(
          factory, PAGED_LIST_CONFIG);
      List<TaskContainer> current = tasks.getValue();
      if (current instanceof PagedList) {
        Object lastKey = ((PagedList<TaskContainer>) current).getLastKey();
        if (lastKey instanceof Integer) {
          builder.setInitialLoadKey((Integer) lastKey);
        }
      }
      internal = builder.build();
      internal.observeForever(this);
    }
  }

  @Override
  protected void onCleared() {
    disposable.dispose();

    removeObserver();
  }

  public List<TaskContainer> getValue() {
    List<TaskContainer> value = tasks.getValue();
    return value != null ? value : Collections.emptyList();
  }

  @Override
  public void onChanged(PagedList<TaskContainer> taskContainers) {
    tasks.setValue(taskContainers);
  }
}
