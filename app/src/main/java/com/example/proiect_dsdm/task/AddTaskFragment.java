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

public class AddTaskFragment extends Fragment {

    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button saveButton;
    private Button cancelButton;
    private Button dateButton;
    private TextView dateText;
    private FirebaseAuth mAuth;
    private long selectedDateTime = 0;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {

            } else {

            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        titleEditText = view.findViewById(R.id.add_task_title);
        descriptionEditText = view.findViewById(R.id.add_task_description);
        saveButton = view.findViewById(R.id.save_new_task_button);
        cancelButton = view.findViewById(R.id.cancel_add_task_button);
        dateButton = view.findViewById(R.id.button_date);
        dateText = view.findViewById(R.id.text_view_date);

        updateReminderDisplay();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewTask();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().popBackStack();
            }
        });

        dateButton.setOnClickListener(v -> showDateTimePicker());

        return view;
    }

    private void saveNewTask() {
        final String title = titleEditText.getText().toString().trim();
        final String description = descriptionEditText.getText().toString().trim();

        if (title.isEmpty()) {
            titleEditText.setError("Title cannot be empty");
            return;
        }

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {

                TaskEntity newTask;
                if(selectedDateTime!=0) {
                    newTask = new TaskEntity(title, description, selectedDateTime, userId);
                    setReminder(newTask);
                }
                else{
                    newTask = new TaskEntity(title, description, 0, userId);
                }

                MainActivity.database.taskDao().insertTask(newTask);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getParentFragmentManager().popBackStack();
                    }
                });
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
        if(selectedDateTime!=0){
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
                    showNotificationPermissionRationale();
                } else {
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


}
