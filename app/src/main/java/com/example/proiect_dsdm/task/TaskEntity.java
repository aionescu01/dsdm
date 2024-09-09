package com.example.proiect_dsdm.task;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "task_table")
public class TaskEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String description;
    private long reminderDateTime;
    private boolean isCompleted;
    private String userId;

    public TaskEntity(String title, String description, long reminderDateTime, String userId) {
        this.title = title;
        this.description = description;
        this.reminderDateTime = reminderDateTime;
        this.userId = userId;
        this.isCompleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getReminderDateTime() {
        return reminderDateTime;
    }

    public void setReminderDateTime(long reminderDateTime) {
        this.reminderDateTime = reminderDateTime;
    }

    public boolean isCompleted() {
        return isCompleted;
    }
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

}
