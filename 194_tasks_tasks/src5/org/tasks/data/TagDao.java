package org.tasks.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TagDao {

  @Query("UPDATE tags SET name = :name WHERE tag_uid = :tagUid")
  void rename(String tagUid, String name);

  @Query("DELETE FROM tags WHERE tag_uid = :tagUid")
  void deleteTag(String tagUid);

  @Insert
  void insert(Tag tag);

  @Insert
  void insert(List<Tag> tags);

  @Query("DELETE FROM tags WHERE task = :taskId AND tag_uid in (:tagUids)")
  void deleteTags(long taskId, List<String> tagUids);

  @Query("SELECT name FROM tags WHERE task = :taskId ORDER BY UPPER(name) ASC")
  List<String> getTagNames(long taskId);

  @Query("SELECT tag_uid FROM tags WHERE task = :taskId")
  List<String> getTagUids(long taskId);

  @Query("SELECT tag_uid FROM tags WHERE task_uid = :taskUid")
  List<String> getTagUids(String taskUid);

  @Query("SELECT * FROM tags WHERE tag_uid = :tagUid")
  List<Tag> getByTagUid(String tagUid);

  @Query("SELECT * FROM tags WHERE task = :taskId")
  List<Tag> getTagsForTask(long taskId);

  @Query("SELECT * FROM tags WHERE task = :taskId AND tag_uid = :tagUid")
  Tag getTagByTaskAndTagUid(long taskId, String tagUid);

  @Query("DELETE FROM tags WHERE _id = :id")
  void deleteById(long id);
}
