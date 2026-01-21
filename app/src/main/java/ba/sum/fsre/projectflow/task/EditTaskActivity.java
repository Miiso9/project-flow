package ba.sum.fsre.projectflow.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.model.TeamMemberWithDetails;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditTaskActivity extends AppCompatActivity {

    private TextInputEditText titleInput, descriptionInput, dueDateInput, estimatedHoursInput;
    private AutoCompleteTextView assignedToInput, statusInput, priorityInput;
    private Button updateButton;
    private TaskViewModel viewModel;
    private Task task;
    private Calendar dueDateCalendar = Calendar.getInstance();

    // For storing team members
    private List<TeamMemberWithDetails> teamMembers = new ArrayList<>();
    private Map<String, String> displayNameToUserIdMap = new HashMap<>();

    private final String[] statusOptions = {"todo", "in_progress", "done", "blocked"};
    private final String[] priorityOptions = {"low", "medium", "high"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupViewModel();
        setupDropdowns();
        setupDatePicker();
        loadProjectAndTeamMembers();
        populateForm();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Task");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        assignedToInput = findViewById(R.id.assignedToInput);
        statusInput = findViewById(R.id.statusInput);
        priorityInput = findViewById(R.id.priorityInput);
        dueDateInput = findViewById(R.id.dueDateInput);
        estimatedHoursInput = findViewById(R.id.estimatedHoursInput);
        updateButton = findViewById(R.id.createButton);

        updateButton.setText("Update Task");
        updateButton.setOnClickListener(v -> updateTask());
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            updateButton.setEnabled(!isLoading);
            updateButton.setText(isLoading ? "Updating..." : "Update Task");
        });
    }

    private void setupDropdowns() {
        // Status dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusOptions
        );
        statusInput.setAdapter(statusAdapter);

        // Priority dropdown
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorityOptions
        );
        priorityInput.setAdapter(priorityAdapter);
    }

    private void setupDatePicker() {
        dueDateInput.setOnClickListener(v -> showDatePicker());
        dueDateInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    dueDateCalendar.set(Calendar.YEAR, year);
                    dueDateCalendar.set(Calendar.MONTH, month);
                    dueDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dueDateInput.setText(sdf.format(dueDateCalendar.getTime()));
                },
                dueDateCalendar.get(Calendar.YEAR),
                dueDateCalendar.get(Calendar.MONTH),
                dueDateCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void loadProjectAndTeamMembers() {
        if (task == null || task.projectId == null) {
            Toast.makeText(this, "Task project not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // First, get the project to get teamId
        SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);

        // Get project with team info
        String select = "*,team:teams(*)";
        api.getProjectById("eq." + task.projectId, select).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Project project = response.body().get(0);
                    String teamId = project.teamId;

                    if (teamId != null) {
                        // Now load team members for this team
                        loadTeamMembers(teamId);
                    } else {
                        Toast.makeText(EditTaskActivity.this,
                                "Project team not found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(EditTaskActivity.this,
                            "Failed to load project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                Toast.makeText(EditTaskActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeamMembers(String teamId) {
        SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);

        // Get team members using the view
        api.getTeamMembersWithDetails("eq." + teamId).enqueue(new Callback<List<TeamMemberWithDetails>>() {
            @Override
            public void onResponse(Call<List<TeamMemberWithDetails>> call,
                                   Response<List<TeamMemberWithDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teamMembers.clear();
                    displayNameToUserIdMap.clear();
                    List<String> memberDisplayNames = new ArrayList<>();

                    for (TeamMemberWithDetails teamMember : response.body()) {
                        teamMembers.add(teamMember);
                        String displayName = teamMember.getFullName() + " (" + teamMember.email + ")";
                        memberDisplayNames.add(displayName);
                        displayNameToUserIdMap.put(displayName, teamMember.userId);
                    }

                    // Setup dropdown adapter
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            EditTaskActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            memberDisplayNames
                    );
                    assignedToInput.setAdapter(adapter);

                    // Set item click listener
                    assignedToInput.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedDisplayName = adapter.getItem(position);
                        String selectedUserId = displayNameToUserIdMap.get(selectedDisplayName);
                        assignedToInput.setTag(selectedUserId);
                    });

                    // Pre-select the current assigned user
                    if (task.assignedUser != null) {
                        for (int i = 0; i < teamMembers.size(); i++) {
                            TeamMemberWithDetails member = teamMembers.get(i);
                            if (member.userId.equals(task.assignedTo)) {
                                String displayName = member.getFullName() + " (" + member.email + ")";
                                assignedToInput.setText(displayName, false);
                                assignedToInput.setTag(member.userId);
                                break;
                            }
                        }
                    }

                    // Show count of loaded members
                    if (!memberDisplayNames.isEmpty()) {
                        Toast.makeText(EditTaskActivity.this,
                                "Loaded " + memberDisplayNames.size() + " team members",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        assignedToInput.setHint("No team members available");
                    }
                } else {
                    Toast.makeText(EditTaskActivity.this,
                            "Failed to load team members", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeamMemberWithDetails>> call, Throwable t) {
                Toast.makeText(EditTaskActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateForm() {
        titleInput.setText(task.title);
        descriptionInput.setText(task.description);

        if (task.status != null) {
            statusInput.setText(task.status, false);
        }

        if (task.priority != null) {
            priorityInput.setText(task.priority, false);
        }

        if (task.dueDate != null && !task.dueDate.isEmpty()) {
            dueDateInput.setText(task.dueDate);
        }

        if (task.estimatedHours != null && task.estimatedHours > 0) {
            estimatedHoursInput.setText(String.valueOf(task.estimatedHours));
        }

        // Store the current assigned user ID
        if (task.assignedTo != null) {
            assignedToInput.setTag(task.assignedTo);
        }
    }

    private void updateTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();
        String estimatedHoursStr = estimatedHoursInput.getText().toString().trim();
        String status = statusInput.getText().toString().trim();
        String priority = priorityInput.getText().toString().trim();

        // Get assigned user ID from tag or keep the original if not changed
        String assignedTo = assignedToInput.getTag() != null ?
                assignedToInput.getTag().toString() : task.assignedTo;

        // Validation
        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            titleInput.requestFocus();
            return;
        }

        Double estimatedHours = task.estimatedHours;
        if (!estimatedHoursStr.isEmpty()) {
            try {
                estimatedHours = Double.parseDouble(estimatedHoursStr);
                if (estimatedHours < 0) {
                    estimatedHoursInput.setError("Must be positive");
                    return;
                }
            } catch (NumberFormatException e) {
                estimatedHoursInput.setError("Invalid number format");
                return;
            }
        }

        // Update task using viewModel
        viewModel.updateTask(
                task.id,
                title,
                description,
                assignedTo,
                status,
                priority,
                dueDate.isEmpty() ? null : dueDate,
                estimatedHours,
                task.startTime,
                task.endTime
        );
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}