package ba.sum.fsre.projectflow.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Task;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
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
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView priorityLabel;
        private ImageView menuIcon;
        private TextView taskTitle;
        private TextView taskDescription;
        private TextView deadlineTime;
        private TextView deadlineDate;
        private TextView tagA;
        private TextView tagB;
        private TextView tagC;
        private ProgressBar progressBar;
        private TextView progressText;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            priorityLabel = itemView.findViewById(R.id.priorityLabel);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            taskTitle = itemView.findViewById(R.id.taskTitle);
            taskDescription = itemView.findViewById(R.id.taskDescription);
            deadlineTime = itemView.findViewById(R.id.deadlineTime);
            deadlineDate = itemView.findViewById(R.id.deadlineDate);
            tagA = itemView.findViewById(R.id.tagA);
            tagB = itemView.findViewById(R.id.tagB);
            tagC = itemView.findViewById(R.id.tagC);
            progressBar = itemView.findViewById(R.id.progressBar);
            progressText = itemView.findViewById(R.id.progressText);
        }

        public void bind(Task task) {
            taskTitle.setText(task.title);
            taskDescription.setText(task.description);
            deadlineTime.setText(task.deadlineTime);
            deadlineDate.setText(task.deadlineDate);
            progressBar.setProgress(task.progress);
            progressText.setText("Progress Project " + task.progress + "%");

            // Set priority label
            if ("high".equalsIgnoreCase(task.priority)) {
                priorityLabel.setText("High Priority");
                priorityLabel.setBackgroundResource(R.drawable.bg_priority_high);
                priorityLabel.setTextColor(itemView.getContext().getColor(R.color.priority_high_text));
            } else if ("medium".equalsIgnoreCase(task.priority)) {
                priorityLabel.setText("Medium Priority");
                priorityLabel.setBackgroundResource(R.drawable.bg_priority_medium);
                priorityLabel.setTextColor(itemView.getContext().getColor(R.color.priority_medium_text));
            } else {
                priorityLabel.setText("Low Priority");
                priorityLabel.setBackgroundResource(R.drawable.bg_priority_low);
                priorityLabel.setTextColor(itemView.getContext().getColor(R.color.priority_low_text));
            }

            // Set tags visibility and colors
            if (task.tags != null && task.tags.size() > 0) {
                tagA.setVisibility(View.VISIBLE);
                tagA.setText(task.tags.get(0));
                tagA.setBackgroundResource(getTagBackground(task.tags.get(0)));
                
                if (task.tags.size() > 1) {
                    tagB.setVisibility(View.VISIBLE);
                    tagB.setText(task.tags.get(1));
                    tagB.setBackgroundResource(getTagBackground(task.tags.get(1)));
                } else {
                    tagB.setVisibility(View.GONE);
                }
                if (task.tags.size() > 2) {
                    tagC.setVisibility(View.VISIBLE);
                    tagC.setText(task.tags.get(2));
                    tagC.setBackgroundResource(getTagBackground(task.tags.get(2)));
                } else {
                    tagC.setVisibility(View.GONE);
                }
            } else {
                tagA.setVisibility(View.GONE);
                tagB.setVisibility(View.GONE);
                tagC.setVisibility(View.GONE);
            }

            // Set menu icon or checkmark based on status
            if ("completed".equalsIgnoreCase(task.status)) {
                menuIcon.setImageResource(R.drawable.ic_checkmark);
            } else {
                menuIcon.setImageResource(R.drawable.ic_menu_dots);
            }
        }

        private int getTagBackground(String tag) {
            // Map tags to colors: A=red, B=orange, C=blue, D=pink, E=green, F=blue
            switch (tag.toUpperCase()) {
                case "A":
                    return R.drawable.bg_tag_red;
                case "B":
                    return R.drawable.bg_tag_orange;
                case "C":
                case "F":
                    return R.drawable.bg_tag_blue;
                case "D":
                    return R.drawable.bg_tag_pink;
                case "E":
                    return R.drawable.bg_tag_green;
                default:
                    return R.drawable.bg_tag_blue;
            }
        }
    }
}
