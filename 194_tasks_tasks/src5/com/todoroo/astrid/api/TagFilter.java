package com.todoroo.astrid.api;

import android.os.Parcel;
import android.os.Parcelable;
import com.todoroo.andlib.sql.Criterion;
import com.todoroo.andlib.sql.Field;
import com.todoroo.andlib.sql.Join;
import com.todoroo.andlib.sql.QueryTemplate;
import com.todoroo.astrid.dao.TaskDao;
import com.todoroo.astrid.data.Task;
import java.util.HashMap;
import java.util.Map;
import org.tasks.R;
import org.tasks.data.Tag;
import org.tasks.data.TagData;

public class TagFilter extends Filter {

  /** Parcelable Creator Object */
  public static final Parcelable.Creator<TagFilter> CREATOR =
      new Parcelable.Creator<TagFilter>() {

        /** {@inheritDoc} */
        @Override
        public TagFilter createFromParcel(Parcel source) {
          TagFilter item = new TagFilter();
          item.readFromParcel(source);
          return item;
        }

        /** {@inheritDoc} */
        @Override
        public TagFilter[] newArray(int size) {
          return new TagFilter[size];
        }
      };

  private static final int TAG = R.drawable.ic_outline_label_24px;
  private TagData tagData;

  private TagFilter() {
    super();
  }

  public TagFilter(TagData tagData) {
    super(tagData.getName(), queryTemplate(tagData.getRemoteId()), getValuesForNewTask(tagData));
    this.tagData = tagData;
    tint = tagData.getColor();
    icon = TAG;
  }

  private static QueryTemplate queryTemplate(String uuid) {
    return new QueryTemplate()
        .join(Join.inner(Tag.TABLE.as("mtags"), Task.UUID.eq(Field.field("mtags.task_uid"))))
        .where(
            Criterion.and(
                Field.field("mtags.tag_uid").eq(uuid), TaskDao.TaskCriteria.activeAndVisible()));
  }

  private static Map<String, Object> getValuesForNewTask(TagData tagData) {
    Map<String, Object> values = new HashMap<>();
    values.put(Tag.KEY, tagData.getName());
    return values;
  }

  public String getUuid() {
    return tagData.getRemoteId();
  }

  public TagData getTagData() {
    return tagData;
  }

  /** {@inheritDoc} */
  @Override
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeParcelable(tagData, 0);
  }

  @Override
  protected void readFromParcel(Parcel source) {
    super.readFromParcel(source);
    tagData = source.readParcelable(getClass().getClassLoader());
  }

  @Override
  public boolean supportsSubtasks() {
    return true;
  }

  @Override
  public int getMenu() {
    return R.menu.menu_tag_view_fragment;
  }
}
