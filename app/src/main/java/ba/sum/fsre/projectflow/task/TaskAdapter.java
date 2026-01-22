package ba.sum.fsre.projectflow.task;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskLongClick(Task task);
        void onMenuClick(View view, Task task);
    }

    public TaskAdapter(List<Task> tasks, OnTaskClickListener listener) {
        this.tasks = tasks;
        this.listener = listener;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.taskTitle.setText(task.title);
        holder.taskDescription.setText(task.description != null ? task.description : "");

        String priority = task.priority != null ? task.priority : "medium";
        holder.priorityLabel.setText(priority.toUpperCase());
        setPriorityBackground(holder.priorityLabel, priority);

        String status = task.status != null ? task.status : "todo";
        holder.statusLabel.setText(getStatusDisplayName(status));
        setStatusBackground(holder.statusLabel, status);

        if (task.assignedUser != null) {
            String userName = task.assignedUser.email;
            if (task.assignedUser.first_name != null && !task.assignedUser.first_name.isEmpty()) {
                userName = task.assignedUser.first_name + " " + task.assignedUser.last_name;
            }
            holder.assignedUser.setText("Assigned to: " + userName);
        } else {
            holder.assignedUser.setText("Unassigned");
        }

        if (task.dueDate != null && !task.dueDate.isEmpty()) {
            holder.dueDate.setText(task.dueDate);
            holder.dueDateContainer.setVisibility(View.VISIBLE);
        } else {
            holder.dueDateContainer.setVisibility(View.GONE);
        }

        if (task.estimatedHours != null && task.estimatedHours > 0) {
            holder.estimatedHours.setText("Estimated: " + task.estimatedHours + "h");
        } else {
            holder.estimatedHours.setText("");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(task);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTaskLongClick(task);
                return true;
            }
            return false;
        });

        holder.menuIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMenuClick(v, task);
            }
        });
    }

    private void setPriorityBackground(TextView textView, String priority) {
        int backgroundRes;
        int textColorRes;

        switch (priority.toLowerCase()) {
            case "high":
                backgroundRes = R.drawable.bg_priority_high;
                textColorRes = R.color.priority_high_text;
                break;
            case "medium":
                backgroundRes = R.drawable.bg_priority_medium;
                textColorRes = R.color.priority_medium_text;
                break;
            case "low":
                backgroundRes = R.drawable.bg_priority_low;
                textColorRes = R.color.priority_low_text;
                break;
            default:
                backgroundRes = R.drawable.bg_priority_medium;
                textColorRes = R.color.priority_medium_text;
        }

        textView.setBackgroundResource(backgroundRes);
        textView.setTextColor(textView.getContext().getResources().getColor(textColorRes, null));
    }

    private void setStatusBackground(TextView textView, String status) {
        int backgroundRes;

        switch (status.toLowerCase()) {
            case "todo":
                backgroundRes = R.drawable.bg_status_todo;
                break;
            case "in_progress":
                backgroundRes = R.drawable.bg_status_inprogress;
                break;
            case "done":
                backgroundRes = R.drawable.bg_status_done;
                break;
            case "blocked":
                backgroundRes = R.drawable.bg_status_blocked;
                break;
            default:
                backgroundRes = R.drawable.bg_status_todo;
        }

        textView.setBackgroundResource(backgroundRes);
    }

    private String getStatusDisplayName(String status) {
        switch (status.toLowerCase()) {
            case "todo": return "TO DO";
            case "in_progress": return "IN PROGRESS";
            case "done": return "DONE";
            case "blocked": return "BLOCKED";
            default: return "TO DO";
        }
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView priorityLabel;
        TextView statusLabel;
        TextView taskTitle;
        TextView taskDescription;
        TextView assignedUser;
        TextView dueDate;
        TextView estimatedHours;
        LinearLayout dueDateContainer;
        ImageView menuIcon;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityLabel = itemView.findViewById(R.id.priorityLabel);
            statusLabel = itemView.findViewById(R.id.statusLabel);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            assignedUser = itemView.findViewById(R.id.assignedUser);
            dueDate = itemView.findViewById(R.id.dueDate);
            estimatedHours = itemView.findViewById(R.id.estimatedHours);
            dueDateContainer = itemView.findViewById(R.id.dueDateContainer);
            menuIcon = itemView.findViewById(R.id.menuIcon);
        }
    }
}