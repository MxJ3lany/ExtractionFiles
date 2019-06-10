package org.tasks.filters;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;

import android.content.Context;
import android.content.res.Resources;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.Query;
import com.todoroo.astrid.api.CustomFilterCriterion;
import com.todoroo.astrid.api.MultipleSelectCriterion;
import com.todoroo.astrid.api.PermaSql;
import com.todoroo.astrid.api.TextInputCriterion;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Task;
import com.todoroo.astrid.data.Task.Priority;
import com.todoroo.astrid.gtasks.GtasksListService;
import com.todoroo.astrid.tags.TagService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.data.CaldavCalendar;
import org.tasks.data.CaldavDao;
import org.tasks.data.CaldavTask;
import org.tasks.data.GoogleTask;
import org.tasks.data.GoogleTaskList;
import org.tasks.data.GoogleTaskListDao;
import org.tasks.data.Tag;
import org.tasks.data.TagData;
import org.tasks.injection.ForApplication;
import org.tasks.sync.SyncAdapters;

public class FilterCriteriaProvider {

  private static final String IDENTIFIER_TITLE = "title"; // $NON-NLS-1$
  private static final String IDENTIFIER_IMPORTANCE = "importance"; // $NON-NLS-1$
  private static final String IDENTIFIER_DUEDATE = "dueDate"; // $NON-NLS-1$
  private static final String IDENTIFIER_GTASKS = "gtaskslist"; // $NON-NLS-1$
  private static final String IDENTIFIER_CALDAV = "caldavlist"; // $NON-NLS-1$
  private static final String IDENTIFIER_TAG_IS = "tag_is"; // $NON-NLS-1$
  private static final String IDENTIFIER_TAG_CONTAINS = "tag_contains"; // $NON-NLS-1$

  private final Context context;
  private final TagService tagService;
  private final Resources r;
  private final GoogleTaskListDao googleTaskListDao;
  private final CaldavDao caldavDao;

  @Inject
  public FilterCriteriaProvider(
      @ForApplication Context context,
      TagService tagService,
      GtasksListService gtasksListService,
      SyncAdapters syncAdapters,
      GoogleTaskListDao googleTaskListDao,
      CaldavDao caldavDao) {
    this.context = context;
    this.tagService = tagService;

    r = context.getResources();
    this.googleTaskListDao = googleTaskListDao;
    this.caldavDao = caldavDao;
  }

  public List<CustomFilterCriterion> getAll() {
    List<CustomFilterCriterion> result = newArrayList();

    result.add(getTagFilter());
    result.add(getTagNameContainsFilter());
    result.add(getDueDateFilter());
    result.add(getPriorityFilter());
    result.add(getTaskTitleContainsFilter());
    if (!googleTaskListDao.getAccounts().isEmpty()) {
      result.add(getGtasksFilterCriteria());
    }
    if (!caldavDao.getAccounts().isEmpty()) {
      result.add(getCaldavFilterCriteria());
    }
    return result;
  }

  private CustomFilterCriterion getTagFilter() {
    // TODO: adding to hash set because duplicate tag name bug hasn't been fixed yet
    List<String> tags =
        newArrayList(newLinkedHashSet(transform(tagService.getTagList(), TagData::getName)));
    String[] tagNames = tags.toArray(new String[tags.size()]);
    Map<String, Object> values = new HashMap<>();
    values.put(Tag.KEY, "?");
    return new MultipleSelectCriterion(
        IDENTIFIER_TAG_IS,
        context.getString(R.string.CFC_tag_text),
        Query.select(Field.field("task"))
            .from(Tag.TABLE)
            .join(Join.inner(Task.TABLE, Field.field("task").eq(Task.ID)))
            .where(
                Criterion.and(TaskDao.TaskCriteria.activeAndVisible(), Field.field("name").eq("?")))
            .toString(),
        values,
        tagNames,
        tagNames,
        null,
        context.getString(R.string.CFC_tag_name));
  }

  private CustomFilterCriterion getTagNameContainsFilter() {
    return new TextInputCriterion(
        IDENTIFIER_TAG_CONTAINS,
        context.getString(R.string.CFC_tag_contains_text),
        Query.select(Field.field("task"))
            .from(Tag.TABLE)
            .join(Join.inner(Task.TABLE, Field.field("task").eq(Task.ID)))
            .where(
                Criterion.and(
                    TaskDao.TaskCriteria.activeAndVisible(), Field.field("name").like("%?%")))
            .toString(),
        context.getString(R.string.CFC_tag_contains_name),
        "",
        null,
        context.getString(R.string.CFC_tag_contains_name));
  }

  private CustomFilterCriterion getDueDateFilter() {
    String[] entryValues =
        new String[] {
          "0",
          PermaSql.VALUE_EOD_YESTERDAY,
          PermaSql.VALUE_EOD,
          PermaSql.VALUE_EOD_TOMORROW,
          PermaSql.VALUE_EOD_DAY_AFTER,
          PermaSql.VALUE_EOD_NEXT_WEEK,
          PermaSql.VALUE_EOD_NEXT_MONTH,
        };
    Map<String, Object> values = new HashMap<>();
    values.put(Task.DUE_DATE.name, "?");
    return new MultipleSelectCriterion(
        IDENTIFIER_DUEDATE,
        r.getString(R.string.CFC_dueBefore_text),
        Query.select(Task.ID)
            .from(Task.TABLE)
            .where(
                Criterion.and(
                    TaskDao.TaskCriteria.activeAndVisible(),
                    Criterion.or(Field.field("?").eq(0), Task.DUE_DATE.gt(0)),
                    Task.DUE_DATE.lte("?")))
            .toString(),
        values,
        r.getStringArray(R.array.CFC_dueBefore_entries),
        entryValues,
        null,
        r.getString(R.string.CFC_dueBefore_name));
  }

  private CustomFilterCriterion getPriorityFilter() {
    String[] entryValues =
        new String[] {
          Integer.toString(Priority.HIGH),
          Integer.toString(Priority.MEDIUM),
          Integer.toString(Priority.LOW),
          Integer.toString(Priority.NONE),
        };
    String[] entries = new String[] {"!!!", "!!", "!", "o"};
    Map<String, Object> values = new HashMap<>();
    values.put(Task.IMPORTANCE.name, "?");
    return new MultipleSelectCriterion(
        IDENTIFIER_IMPORTANCE,
        r.getString(R.string.CFC_importance_text),
        Query.select(Task.ID)
            .from(Task.TABLE)
            .where(Criterion.and(TaskDao.TaskCriteria.activeAndVisible(), Task.IMPORTANCE.lte("?")))
            .toString(),
        values,
        entries,
        entryValues,
        null,
        r.getString(R.string.CFC_importance_name));
  }

  private CustomFilterCriterion getTaskTitleContainsFilter() {
    return new TextInputCriterion(
        IDENTIFIER_TITLE,
        r.getString(R.string.CFC_title_contains_text),
        Query.select(Task.ID)
            .from(Task.TABLE)
            .where(Criterion.and(TaskDao.TaskCriteria.activeAndVisible(), Task.TITLE.like("%?%")))
            .toString(),
        r.getString(R.string.CFC_title_contains_name),
        "",
        null,
        r.getString(R.string.CFC_title_contains_name));
  }

  private CustomFilterCriterion getGtasksFilterCriteria() {
    List<GoogleTaskList> lists = googleTaskListDao.getAllLists();

    String[] listNames = new String[lists.size()];
    String[] listIds = new String[lists.size()];
    for (int i = 0; i < lists.size(); i++) {
      listNames[i] = lists.get(i).getTitle();
      listIds[i] = lists.get(i).getRemoteId();
    }

    Map<String, Object> values = new HashMap<>();
    values.put(GoogleTask.KEY, "?");

    return new MultipleSelectCriterion(
        IDENTIFIER_GTASKS,
        context.getString(R.string.CFC_gtasks_list_text),
        Query.select(Field.field("gt_task"))
            .from(GoogleTask.TABLE)
            .join(Join.inner(Task.TABLE, Field.field("gt_task").eq(Task.ID)))
            .where(
                Criterion.and(
                    TaskDao.TaskCriteria.activeAndVisible(), Field.field("gt_list_id").eq("?")))
            .toString(),
        values,
        listNames,
        listIds,
        null,
        context.getString(R.string.CFC_gtasks_list_name));
  }

  private CustomFilterCriterion getCaldavFilterCriteria() {
    List<CaldavCalendar> calendars = caldavDao.getCalendars();

    String[] names = new String[calendars.size()];
    String[] ids = new String[calendars.size()];
    for (int i = 0; i < calendars.size(); i++) {
      names[i] = calendars.get(i).getName();
      ids[i] = calendars.get(i).getUuid();
    }
    Map<String, Object> values = new HashMap<>();
    values.put(CaldavTask.KEY, "?");

    return new MultipleSelectCriterion(
        IDENTIFIER_CALDAV,
        context.getString(R.string.CFC_gtasks_list_text),
        Query.select(Field.field("task"))
            .from(CaldavTask.TABLE)
            .join(Join.inner(Task.TABLE, Field.field("task").eq(Task.ID)))
            .where(
                Criterion.and(
                    TaskDao.TaskCriteria.activeAndVisible(), Field.field("calendar").eq("?")))
            .toString(),
        values,
        names,
        ids,
        null,
        context.getString(R.string.CFC_caldav_list_name));
  }
}
