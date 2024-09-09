package com.example.proiect_dsdm.task;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.proiect_dsdm.MainActivity;
import com.example.proiect_dsdm.R;
import com.example.proiect_dsdm.ReminderBroadcastReceiver;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;

public class EditTaskFragment extends Fragment {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button saveButton;
    private Button backButton;
    private int taskId;
    private FirebaseAuth mAuth;
    private TextView dateText;
    private long selectedDateTime = 0;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Button dateButton;
    private Button deleteButton;

    public static EditTaskFragment newInstance(int taskId) {
        EditTaskFragment fragment = new EditTaskFragment();
        Bundle args = new Bundle();
        args.putInt("taskId", taskId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskId = getArguments().getInt("taskId");
        }
        mAuth = FirebaseAuth.getInstance();
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {

            } else {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_task, container, false);

        titleEditText = view.findViewById(R.id.edit_task_title);
        descriptionEditText = view.findViewById(R.id.edit_task_description);
        saveButton = view.findViewById(R.id.save_task_button);
        backButton = view.findViewById(R.id.back_button);
        dateButton = view.findViewById(R.id.button_date);
        dateText = view.findViewById(R.id.text_view_date);
        deleteButton = view.findViewById(R.id.delete_button);

        loadTask();

        updateReminderDisplay();

        saveButton.setOnClickListener(v -> saveTask());

        backButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        dateButton.setOnClickListener(v -> showDateTimePicker());

        deleteButton.setOnClickListener(v -> deleteTask());

        return view;
    }

    private void loadTask() {
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final TaskEntity task = MainActivity.database.taskDao().getTaskByIdForUser(taskId, userId);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (task != null) {
                            titleEditText.setText(task.getTitle());
                            descriptionEditText.setText(task.getDescription());
                            if(task.getReminderDateTime()!=0){
                            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());
                            dateText.setText("Reminder set for: " + sdf.format(new Date(task.getReminderDateTime())));
                            }

                        } else {
                            // task doesnt belong to current user
                            Toast.makeText(getContext(), "Task not found", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        }
                    }
                });
            }
        });
    }

    private void saveTask() {
        final String newTitle = titleEditText.getText().toString();
        final String newDescription = descriptionEditText.getText().toString();
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                TaskEntity task = MainActivity.database.taskDao().getTaskByIdForUser(taskId, userId);
                if (task != null) {
                    task.setTitle(newTitle);
                    task.setDescription(newDescription);
                    if(selectedDateTime!=0) {
                        task.setReminderDateTime(selectedDateTime);
                        setReminder(task);
                    }
                    MainActivity.database.taskDao().updateTask(task);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getParentFragmentManager().popBackStack();
                        }
                    });
                }
            }
        });
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this.getContext(),
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    showTimePicker(calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker(Calendar calendar) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this.getContext(),
                (view, hourOfDay, minute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendar.set(Calendar.MINUTE, minute);
                    selectedDateTime = calendar.getTimeInMillis();
                    updateReminderDisplay();
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
        );
        timePickerDialog.show();
    }

    private void updateReminderDisplay() {
        if(selectedDateTime!=0) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());
            dateText.setText("Reminder set for: " + sdf.format(new Date(selectedDateTime)));
        }
    }

    private void scheduleReminder(TaskEntity task) {
        Intent intent = new Intent(requireContext(), ReminderBroadcastReceiver.class);
        intent.putExtra("TASK_ID", task.getId());
        intent.putExtra("TASK_TITLE", task.getTitle());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                task.getId(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getReminderDateTime(), pendingIntent);
                } else {
                    // Request permission to schedule exact alarms
                    Intent intent2 = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    startActivity(intent2);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.getReminderDateTime(), pendingIntent);
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, task.getReminderDateTime(), pendingIntent);
            }
        }
    }

    private void setReminder(TaskEntity task) {
        checkNotificationPermission();
        scheduleReminder(task);
    }
    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                    // Show an explanation to the user *asynchronously*
                    showNotificationPermissionRationale();
                } else {
                    // No explanation needed; request the permission
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                }
            }
        }
    }

    private void showNotificationPermissionRationale() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Notification Permission Required")
                .setMessage("This app needs to send notifications for task reminders. Please grant the permission.")
                .setPositiveButton("OK", (dialog, which) -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTask() {
        new AlertDialog.Builder(this.getContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (taskId != -1) {
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.database.taskDao().deleteTask(taskId);
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            cancelReminder(taskId);
                                            Toast.makeText(requireContext(), "Task deleted", Toast.LENGTH_SHORT).show();
                                            requireActivity().onBackPressed();
                                        }
                                    });

                            }
                        });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void cancelReminder(int taskId) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), ReminderBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                requireContext(),
                taskId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }


}
