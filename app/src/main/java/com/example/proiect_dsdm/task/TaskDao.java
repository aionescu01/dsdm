package com.example.proiect_dsdm.task;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;
@Dao
public interface TaskDao {

    @Insert
    void insertTask(TaskEntity task);

    @Query("SELECT * FROM task_table")
    LiveData<List<TaskEntity>> getAllTasks();

    @Query("SELECT * FROM task_table WHERE id = :taskId")
    TaskEntity getTaskById(int taskId);

    @Query("DELETE FROM task_table WHERE id = :taskId")
    void deleteTask(int taskId);
    @Update
    void updateTask(TaskEntity task);
    @Query("SELECT * FROM task_table ORDER BY isCompleted ASC, id DESC")
    List<TaskEntity> getAllTasksOrderedByCompletion();

    @Query("SELECT * FROM task_table WHERE userId = :userId ORDER BY isCompleted ASC, id DESC")
    List<TaskEntity> getAllTasksForUser(String userId);

    @Delete
    void deleteTask(TaskEntity task);

    @Query("SELECT * FROM task_table WHERE id = :taskId AND userId = :userId")
    TaskEntity getTaskByIdForUser(int taskId, String userId);

}
