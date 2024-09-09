package com.example.proiect_dsdm.task;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proiect_dsdm.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private List<TaskEntity> taskList = new ArrayList<>();;
    private OnTaskClickListener listener;

    public void setTaskList(List<TaskEntity> taskList) {
        this.taskList = taskList;
        notifyDataSetChanged();

    }

    public interface OnTaskClickListener {
        void onTaskClick(TaskEntity task);
        void onTaskCompletionToggle(TaskEntity task);
    }

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskEntity task = taskList.get(position);
        holder.bind(task,listener);
    }



    @Override
    public int getItemCount() {
        return taskList != null ? taskList.size() : 0;
    }

    class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, date;
        CheckBox checkBox;

        TaskViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.text_view_title);
            description = itemView.findViewById(R.id.text_view_description);
            date = itemView.findViewById(R.id.text_view_date);
            checkBox = itemView.findViewById(R.id.task_completed);

        }

        public void bind(final TaskEntity task, final OnTaskClickListener listener){
            title.setText(task.getTitle());
            description.setText(task.getDescription());
            //date.setText(task.getReminderDateTime());
            if(task.getReminderDateTime()!=0) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm", Locale.getDefault());
                String currentDateAndTime = sdf.format(task.getReminderDateTime());
                date.setText(currentDateAndTime);
            }
            else{
                date.setText("");
            }
            checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                title.setPaintFlags(title.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onTaskClick(taskList.get(position));
                }
            });

            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onTaskCompletionToggle(task);
                    }
                }
            });

        }
    }


}
