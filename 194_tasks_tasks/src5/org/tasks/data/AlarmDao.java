package org.tasks.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface AlarmDao {

  @Query(
      "SELECT alarms.* FROM alarms INNER JOIN tasks ON tasks._id = alarms.task "
          + "WHERE tasks.completed = 0 AND tasks.deleted = 0 AND tasks.lastNotified < alarms.time "
          + "ORDER BY time ASC")
  List<Alarm> getActiveAlarms();

  @Query(
      "SELECT alarms.* FROM alarms INNER JOIN tasks ON tasks._id = alarms.task "
          + "WHERE tasks._id = :taskId AND tasks.completed = 0 AND tasks.deleted = 0 AND tasks.lastNotified < alarms.time "
          + "ORDER BY time ASC")
  List<Alarm> getActiveAlarms(long taskId);

  @Query("SELECT * FROM alarms WHERE task = :taskId ORDER BY time ASC")
  List<Alarm> getAlarms(long taskId);

  @Delete
  void delete(Alarm alarm);

  @Insert
  long insert(Alarm alarm);
}
