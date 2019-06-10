package org.tasks.data;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.todoroo.andlib.data.Property.StringProperty;
import com.todoroo.andlib.data.Table;
import org.tasks.backup.XmlReader;

@Entity(tableName = "tags")
public class Tag {

  public static final String KEY = "tags-tag"; // $NON-NLS-1$

  @Deprecated public static final Table TABLE = new Table("tags");

  public static final StringProperty TASK_UID = new StringProperty(TABLE, "task_uid");
  public static final StringProperty NAME = new StringProperty(TABLE, "name");

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "_id")
  private transient long id;

  @ColumnInfo(name = "task")
  private transient long task;

  @ColumnInfo(name = "name")
  private String name;

  @ColumnInfo(name = "tag_uid")
  private String tagUid;

  @ColumnInfo(name = "task_uid")
  private transient String taskUid;

  public Tag() {}

  @Ignore
  public Tag(long task, String taskUid, String name, String tagUid) {
    this.task = task;
    this.taskUid = taskUid;
    this.name = name;
    this.tagUid = tagUid;
  }

  @Ignore
  public Tag(XmlReader xmlReader) {
    xmlReader.readString("name", this::setName);
    xmlReader.readString("tag_uid", this::setTagUid);
    xmlReader.readString("task_uid", this::setTaskUid);
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public long getTask() {
    return task;
  }

  public void setTask(long task) {
    this.task = task;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTagUid() {
    return tagUid;
  }

  public void setTagUid(String tagUid) {
    this.tagUid = tagUid;
  }

  @NonNull
  public String getTaskUid() {
    return taskUid;
  }

  public void setTaskUid(@NonNull String taskUid) {
    this.taskUid = taskUid;
  }
}
