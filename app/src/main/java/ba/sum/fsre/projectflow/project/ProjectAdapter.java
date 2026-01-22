package ba.sum.fsre.projectflow.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private List<Project> projects = new ArrayList<>();
    private OnProjectClickListener listener;

    public interface OnProjectClickListener {
        void onProjectClick(Project project);
        void onProjectLongClick(Project project);
        void onMenuClick(View view, Project project);
    }

    public ProjectAdapter(OnProjectClickListener listener) {
        this.listener = listener;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects != null ? projects : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Ensure this layout name matches your XML (e.g., item_project_card or item_task_project_card)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        Project project = projects.get(position);
        holder.bind(project, listener);
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        private TextView statusLabel;
        private ImageView menuIcon;
        private TextView projectName;
        private TextView projectDescription;
        private TextView startDate;
        private TextView endDate;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            statusLabel = itemView.findViewById(R.id.statusLabel);
            menuIcon = itemView.findViewById(R.id.menuIcon);
            projectName = itemView.findViewById(R.id.projectName);
            projectDescription = itemView.findViewById(R.id.projectDescription);
            startDate = itemView.findViewById(R.id.startDate);
            endDate = itemView.findViewById(R.id.endDate);
        }

        public void bind(Project project, OnProjectClickListener listener) {
            projectName.setText(project.name);
            projectDescription.setText(project.description != null ? project.description : "");

            startDate.setText(project.startDate != null ? project.startDate : "-");
            endDate.setText(project.endDate != null ? project.endDate : "-");

            String status = project.status != null ? project.status : "active";
            switch (status.toLowerCase()) {
                case "completed":
                    statusLabel.setText("Completed");
                    statusLabel.setBackgroundResource(R.drawable.bg_status_completed);
                    statusLabel.setTextColor(itemView.getContext().getColor(R.color.status_completed_text));
                    break;
                case "on_hold":
                    statusLabel.setText("On Hold");
                    statusLabel.setBackgroundResource(R.drawable.bg_status_on_hold);
                    statusLabel.setTextColor(itemView.getContext().getColor(R.color.status_on_hold_text));
                    break;
                case "cancelled":
                    statusLabel.setText("Cancelled");
                    statusLabel.setBackgroundResource(R.drawable.bg_status_cancelled);
                    statusLabel.setTextColor(itemView.getContext().getColor(R.color.status_cancelled_text));
                    break;
                default: // active
                    statusLabel.setText("Active");
                    statusLabel.setBackgroundResource(R.drawable.bg_status_active);
                    statusLabel.setTextColor(itemView.getContext().getColor(R.color.status_active_text));
                    break;
            }

            // Ensure the menu icon is visible
            menuIcon.setImageResource(R.drawable.ic_menu_dots);

            // Click listener for the card body
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProjectClick(project);
                }
            });

            // NEW: Click listener specifically for the 3-dots menu icon
            menuIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onMenuClick(v, project);
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onProjectLongClick(project);
                    return true;
                }
                return false;
            });
        }
    }
}