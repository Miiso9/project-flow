package ba.sum.fsre.projectflow.task;

import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;

public class TaskDetailActivity extends AppCompatActivity {

    private TaskViewModel viewModel;
    private Task task;

    private TextView priorityLabel;
    private TextView taskTitle;
    private TextView projectName;
    private TextView taskDescription;
    private TextView dueDate;
    private TextView statusLabel;
    private TextView estimatedHours;
    private ProgressBar progressBar;
    private TextView progressText;
    private MaterialButton btnMarkComplete;
    private MaterialButton btnEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        task = (Task) getIntent().getSerializableExtra("task");

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        setupToolbar();
        setupUI();
        observeViewModel();

        if (task != null) {
            populateData();
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task Details");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupUI() {
        priorityLabel = findViewById(R.id.priorityLabel);
        taskTitle = findViewById(R.id.taskTitle);
        projectName = findViewById(R.id.projectName);
        taskDescription = findViewById(R.id.taskDescription);
        dueDate = findViewById(R.id.dueDate);
        statusLabel = findViewById(R.id.statusLabel);
        estimatedHours = findViewById(R.id.estimatedHours);
        progressBar = findViewById(R.id.progressBar);
        progressText = findViewById(R.id.progressText);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnEdit = findViewById(R.id.btnEdit);

        btnMarkComplete.setOnClickListener(v -> {
            if (task != null) {
                String newStatus = "completed".equals(task.status) ? "pending" : "completed";
                viewModel.updateTaskStatus(this, task.id, newStatus);
            }
        });

        btnEdit.setOnClickListener(v -> {
            showEditStatusDialog();
        });
    }

    private void showEditStatusDialog() {
        String[] statuses = {"Pending", "In Progress", "Completed", "Postponed"};
        String[] statusValues = {"pending", "in_progress", "completed", "postponed"};

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setItems(statuses, (dialog, which) -> {
                    if (task != null) {
                        viewModel.updateTaskStatus(this, task.id, statusValues[which]);
                    }
                })
                .show();
    }

    private void populateData() {
        taskTitle.setText(task.title);
        taskDescription.setText(task.description != null ? task.description : "No description");

        if (task.projects != null && task.projects.name != null) {
            projectName.setText("Project: " + task.projects.name);
        } else {
            projectName.setText("Project: -");
        }

        dueDate.setText(task.getDisplayDate());

        String status = task.status != null ? task.status : "pending";
        statusLabel.setText(formatStatus(status));

        if (task.estimatedHours != null) {
            estimatedHours.setText(task.estimatedHours + " hours");
        } else {
            estimatedHours.setText("-");
        }

        int progress = task.getProgressValue();
        progressBar.setProgress(progress);
        progressText.setText(progress + "%");

        String priority = task.priority != null ? task.priority : "low";
        if ("high".equalsIgnoreCase(priority)) {
            priorityLabel.setText("High Priority");
            priorityLabel.setBackgroundResource(R.drawable.bg_priority_high);
            priorityLabel.setTextColor(getColor(R.color.priority_high_text));
        } else if ("medium".equalsIgnoreCase(priority)) {
            priorityLabel.setText("Medium Priority");
            priorityLabel.setBackgroundResource(R.drawable.bg_priority_medium);
            priorityLabel.setTextColor(getColor(R.color.priority_medium_text));
        } else {
            priorityLabel.setText("Low Priority");
            priorityLabel.setBackgroundResource(R.drawable.bg_priority_low);
            priorityLabel.setTextColor(getColor(R.color.priority_low_text));
        }

        if ("completed".equals(status)) {
            btnMarkComplete.setText("Reopen Task");
            btnMarkComplete.setBackgroundTintList(getColorStateList(R.color.status_on_hold_text));
        } else {
            btnMarkComplete.setText("Mark Complete");
            btnMarkComplete.setBackgroundTintList(getColorStateList(R.color.progress_green));
        }
    }

    private String formatStatus(String status) {
        switch (status.toLowerCase()) {
            case "in_progress":
                return "In Progress";
            case "completed":
                return "Completed";
            case "postponed":
                return "Postponed";
            default:
                return "Pending";
        }
    }

    private void observeViewModel() {
        viewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Task updated", Toast.LENGTH_SHORT).show();
                if (task != null) {
                    if ("completed".equals(task.status)) {
                        task.status = "pending";
                    } else {
                        task.status = "completed";
                    }
                    populateData();
                }
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
