package ba.sum.fsre.projectflow.task;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle; // Removed SharedPreferences import
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.comment.CommentAdapter;
import ba.sum.fsre.projectflow.document.DocumentAdapter;
import ba.sum.fsre.projectflow.model.Comment;
import ba.sum.fsre.projectflow.model.Document;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.storage.TokenManager; // Import TokenManager
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private TaskViewModel viewModel;
    private Task task;
    private String projectName = "Unknown Project";

    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView priorityLabel, taskTitle, projectNameTextView, taskDescription;
    private TextView dueDate, statusLabel, estimatedHours, progressText;
    private Button btnMarkComplete, btnEdit;

    private RecyclerView recyclerComments;
    private EditText etComment;
    private ImageButton btnSendComment;
    private CommentAdapter commentAdapter;
    private String currentUserId;
    private TokenManager tokenManager;
    private RecyclerView recyclerDocuments;
    private View btnUploadDocument;
    private DocumentAdapter documentAdapter;
    private ActivityResultLauncher<String> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tokenManager = new TokenManager(this);
        currentUserId = tokenManager.getUserId();

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadFile(uri);
                    }
                }
        );

        initViews();
        setupToolbar();
        setupViewModel();
        fetchProjectName();
        setupButtons();
        setupComments();
        setupDocuments();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        priorityLabel = findViewById(R.id.priorityLabel);
        taskTitle = findViewById(R.id.taskTitle);
        projectNameTextView = findViewById(R.id.projectName);
        taskDescription = findViewById(R.id.taskDescription);
        dueDate = findViewById(R.id.dueDate);
        statusLabel = findViewById(R.id.statusLabel);
        estimatedHours = findViewById(R.id.estimatedHours);
        progressText = findViewById(R.id.progressText);
        btnMarkComplete = findViewById(R.id.btnMarkComplete);
        btnEdit = findViewById(R.id.btnEdit);

        recyclerComments = findViewById(R.id.recyclerComments);
        etComment = findViewById(R.id.etComment);
        btnSendComment = findViewById(R.id.btnSendComment);

        progressBar.setVisibility(View.GONE);
        recyclerDocuments = findViewById(R.id.recyclerDocuments);
        btnUploadDocument = findViewById(R.id.btnUploadDocument);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task Details");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        viewModel.getTaskDetails().observe(this, task -> {
            if (task != null) {
                this.task = task;
                populateTaskDetails();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                if (btnMarkComplete != null) btnMarkComplete.setEnabled(!isLoading);
                if (btnEdit != null) btnEdit.setEnabled(!isLoading);
                if (btnSendComment != null) btnSendComment.setEnabled(!isLoading);
            }
        });

        viewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Operation successful", Toast.LENGTH_SHORT).show();
                loadTaskDetails();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getComments().observe(this, comments -> {
            if (comments != null) {
                commentAdapter.setComments(comments);
            }
        });

        viewModel.getDocuments().observe(this, docs -> {
            if (docs != null) {
                documentAdapter.setDocuments(docs);
            }
        });
    }

    private void setupComments() {
        commentAdapter = new CommentAdapter(currentUserId, comment -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Comment")
                    .setMessage("Are you sure you want to delete this comment?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteComment(comment.id, task.id);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));
        recyclerComments.setAdapter(commentAdapter);
        recyclerComments.setNestedScrollingEnabled(false);

        btnSendComment.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) return;

            if (currentUserId == null) {
                Toast.makeText(this, "You must be logged in to comment (ID not found)", Toast.LENGTH_SHORT).show();
                return;
            }

            Comment newComment = new Comment(task.id, currentUserId, content);
            viewModel.addComment(newComment);

            etComment.setText("");
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        });
    }

    private void fetchProjectName() {
        if (task != null && task.projectId != null) {
            SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);
            api.getProjectById("eq." + task.projectId, "name").enqueue(new Callback<List<Project>>() {
                @Override
                public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                    if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                        projectName = response.body().get(0).name;
                        populateTaskDetails();
                    } else {
                        populateTaskDetails();
                    }
                }

                @Override
                public void onFailure(Call<List<Project>> call, Throwable t) {
                    populateTaskDetails();
                }
            });
        } else {
            populateTaskDetails();
        }
    }

    private void populateTaskDetails() {
        if (task == null) return;

        taskTitle.setText(task.title != null ? task.title : "Untitled Task");

        if (task.priority != null) {
            priorityLabel.setText(task.priority.toUpperCase());
            setPriorityBackground(priorityLabel, task.priority);
            priorityLabel.setVisibility(View.VISIBLE);
        } else {
            priorityLabel.setVisibility(View.GONE);
        }

        projectNameTextView.setText("Project: " + projectName);
        projectNameTextView.setVisibility(View.VISIBLE);

        if (task.description != null && !task.description.isEmpty()) {
            taskDescription.setText(task.description);
        } else {
            taskDescription.setText("No description provided");
        }

        if (task.dueDate != null && !task.dueDate.isEmpty()) {
            dueDate.setText(task.dueDate);
        } else {
            dueDate.setText("No due date");
        }

        if (task.status != null) {
            String statusDisplay = getStatusDisplayName(task.status);
            statusLabel.setText(statusDisplay);

            if ("done".equalsIgnoreCase(task.status)) {
                btnMarkComplete.setText("Reopen Task");
            } else {
                btnMarkComplete.setText("Mark Complete");
            }
        }

        if (task.estimatedHours != null && task.estimatedHours > 0) {
            estimatedHours.setText(String.format("%.1f hours", task.estimatedHours));
        } else {
            estimatedHours.setText("Not estimated");
        }

        int progress = calculateProgress(task.status);
        progressText.setText(progress + "%");
        if (progressBar != null) {
            progressBar.setProgress(progress);
        }
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
        textView.setTextColor(getResources().getColor(textColorRes));
    }

    private String getStatusDisplayName(String status) {
        switch (status.toLowerCase()) {
            case "todo": return "To Do";
            case "in_progress": return "In Progress";
            case "done": return "Completed";
            case "blocked": return "Blocked";
            default: return "To Do";
        }
    }

    private int calculateProgress(String status) {
        switch (status.toLowerCase()) {
            case "todo": return 0;
            case "in_progress": return 50;
            case "done": return 100;
            case "blocked": return 25;
            default: return 0;
        }
    }

    private void setupButtons() {
        btnMarkComplete.setOnClickListener(v -> {
            if (task != null) {
                String newStatus = "done".equalsIgnoreCase(task.status) ? "todo" : "done";
                viewModel.updateTaskStatus(task.id, newStatus);
            }
        });

        btnEdit.setOnClickListener(v -> {
            if (task != null) {
                Intent intent = new Intent(TaskDetailActivity.this, EditTaskActivity.class);
                intent.putExtra("task", task);
                startActivity(intent);
            }
        });
    }

    private void loadTaskDetails() {
        if (task != null && task.id != null) {
            viewModel.loadTaskById(task.id);
            viewModel.loadComments(task.id);
            viewModel.loadDocuments(task.id);
        }
    }

    private void setupDocuments() {
        documentAdapter = new DocumentAdapter(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDownloadClick(Document document) {
                if (document.fileUrl != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(document.fileUrl));
                    startActivity(intent);
                }
            }

            @Override
            public void onDeleteClick(Document document) {
                new AlertDialog.Builder(TaskDetailActivity.this)
                        .setTitle("Delete Document")
                        .setMessage("Are you sure you want to delete " + document.fileName + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            viewModel.deleteDocument(document);
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        recyclerDocuments.setLayoutManager(new LinearLayoutManager(this));
        recyclerDocuments.setAdapter(documentAdapter);
        recyclerDocuments.setNestedScrollingEnabled(false);

        btnUploadDocument.setOnClickListener(v -> {
            filePickerLauncher.launch("*/*");
        });
    }

    private void uploadFile(Uri uri) {
        if (task != null && currentUserId != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
            viewModel.uploadDocument(uri, task.id, task.projectId, currentUserId);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTaskDetails();
    }
}