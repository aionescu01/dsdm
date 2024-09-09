package com.example.proiect_dsdm.task;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proiect_dsdm.MainActivity;
import com.example.proiect_dsdm.R;
import com.example.proiect_dsdm.ReminderBroadcastReceiver;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;
import java.util.concurrent.Executors;

public class TaskListFragment extends Fragment implements TaskAdapter.OnTaskClickListener{

    private TaskAdapter taskAdapter;
    private FloatingActionButton addTaskButton;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        taskAdapter = new TaskAdapter();
        recyclerView.setAdapter(taskAdapter);
        taskAdapter.setOnTaskClickListener(this);

        addTaskButton = view.findViewById(R.id.add_task_button);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAddTaskFragment();
            }
        });

        loadTasks();

        return view;
    }

    private void loadTasks() {
        String userId = mAuth.getCurrentUser().getUid();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final List<TaskEntity> tasks = MainActivity.database.taskDao().getAllTasksForUser(userId);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        taskAdapter.setTaskList(tasks);
                    }
                });
            }
        });
    }

    @Override
    public void onTaskClick(TaskEntity task) {
        // Navigate to EditTaskFragment
        EditTaskFragment editFragment = EditTaskFragment.newInstance(task.getId());
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, editFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    @Override
    public void onTaskCompletionToggle(final TaskEntity task) {
        String userId = mAuth.getCurrentUser().getUid();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (task.getUserId().equals(userId)) {
                    boolean newCompletionStatus = !task.isCompleted();
                    task.setCompleted(newCompletionStatus);
                    MainActivity.database.taskDao().updateTask(task);

                    if (newCompletionStatus) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                cancelReminder(task.getId());
                            }
                        });
                    }

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadTasks();
                        }
                    });
                }
            }
        });
    }

    private void navigateToAddTaskFragment() {
        AddTaskFragment addTaskFragment = new AddTaskFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, addTaskFragment)
                .addToBackStack(null)
                .commit();
    }

    private void cancelReminder(int taskId) {
        Context context = requireContext();
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

}
