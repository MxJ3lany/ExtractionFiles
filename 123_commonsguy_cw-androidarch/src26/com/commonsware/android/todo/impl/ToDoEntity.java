/***
 Copyright (c) 2017 CommonsWare, LLC
 Licensed under the Apache License, Version 2.0 (the "License"); you may not
 use this file except in compliance with the License. You may obtain a copy
 of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
 by applicable law or agreed to in writing, software distributed under the
 License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 OF ANY KIND, either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 Covered in detail in the book _Android's Architecture Components_
 https://commonsware.com/AndroidArch
 */

package com.commonsware.android.todo.impl;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;
import java.util.Calendar;
import java.util.List;
import io.reactivex.Single;

@Entity(tableName="todos", indices=@Index(value="id"))
public class ToDoEntity {
  @PrimaryKey
  @NonNull final String id;
  @NonNull final String description;
  final String notes;
  final boolean isCompleted;
  @NonNull final Calendar createdOn;

  public static ToDoEntity fromModel(ToDoModel model) {
    return(new ToDoEntity(model.id(), model.description(), model.isCompleted(),
      model.notes(), model.createdOn()));
  }

  ToDoEntity(@NonNull String id, @NonNull String description, boolean isCompleted,
             String notes, @NonNull Calendar createdOn) {
    this.id=id;
    this.description=description;
    this.isCompleted=isCompleted;
    this.notes=notes;
    this.createdOn=createdOn;
  }

  public ToDoModel toModel() {
    return(ToDoModel.builder()
      .id(id)
      .description(description)
      .isCompleted(isCompleted)
      .notes(notes)
      .createdOn(createdOn)
      .build());
  }

  @Dao
  public interface Store {
    @Query("SELECT * FROM todos ORDER BY description ASC")
    Single<List<ToDoEntity>> all();

    @Insert
    void insert(ToDoEntity... entities);

    @Update
    void update(ToDoEntity... entities);

    @Delete
    void delete(ToDoEntity... entities);

    @Delete
    void delete(List<ToDoEntity> entities);

    @Query("DELETE FROM todos")
    void deleteAll();
  }
}
