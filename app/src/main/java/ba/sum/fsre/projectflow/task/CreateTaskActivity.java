package ba.sum.fsre.projectflow.task;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.model.TeamMemberWithDetails;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTaskActivity extends AppCompatActivity {

    private TextInputEditText titleInput, descriptionInput, dueDateInput, estimatedHoursInput;
    private AutoCompleteTextView assignedToInput, statusInput, priorityInput;
    private TextInputLayout assignedToLayout, statusLayout, priorityLayout, dueDateLayout;
    private Button createButton;
    private Button cancelButton;
    private TaskViewModel viewModel;
    private String projectId;
    private String projectName;
    private Calendar dueDateCalendar = Calendar.getInstance();

    private List<TeamMemberWithDetails> teamMembers = new ArrayList<>();
    private Map<String, String> displayNameToUserIdMap = new HashMap<>();

    private final String[] statusDisplayOptions = {"Todo", "In Progress", "Done", "Blocked"};
    private final String[] statusValueOptions = {"todo", "in_progress", "done", "blocked"};

    private final String[] priorityOptions = {"low", "medium", "high"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        projectId = getIntent().getStringExtra("projectId");
        projectName = getIntent().getStringExtra("projectName");

        if (projectId == null) {
            Toast.makeText(this, "Project ID is required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        setupViewModel();
        setupDropdowns();
        setupDatePicker();
        setupClickListeners();
        loadProjectAndTeamMembers();
    }

    private void initViews() {
        titleInput = findViewById(R.id.titleInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        assignedToInput = findViewById(R.id.assignedToInput);
        statusInput = findViewById(R.id.statusInput);
        priorityInput = findViewById(R.id.priorityInput);
        dueDateInput = findViewById(R.id.dueDateInput);
        estimatedHoursInput = findViewById(R.id.estimatedHoursInput);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        assignedToLayout = findViewById(R.id.assignedToLayout);
        statusLayout = findViewById(R.id.statusLayout);
        priorityLayout = findViewById(R.id.priorityLayout);
        dueDateLayout = findViewById(R.id.dueDateLayout);

        TextView formTitle = findViewById(R.id.formTitle);
        if (projectName != null) {
            formTitle.setText("Create Task for " + projectName);
        }

        createButton.setOnClickListener(v -> createTask());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Task");
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                Toast.makeText(this, "Task created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            createButton.setEnabled(!isLoading);
            createButton.setText(isLoading ? "Creating..." : "Create Task");
        });
    }

    private void setupDropdowns() {
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusDisplayOptions
        );
        statusInput.setAdapter(statusAdapter);
        statusInput.setText(statusDisplayOptions[0], false);

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorityOptions
        );
        priorityInput.setAdapter(priorityAdapter);
        priorityInput.setText(priorityOptions[1], false);
    }

    private void setupClickListeners() {
        assignedToLayout.setEndIconOnClickListener(v -> assignedToInput.showDropDown());
        assignedToLayout.setOnClickListener(v -> assignedToInput.showDropDown());

        statusLayout.setEndIconOnClickListener(v -> statusInput.showDropDown());
        statusLayout.setOnClickListener(v -> statusInput.showDropDown());

        priorityLayout.setEndIconOnClickListener(v -> priorityInput.showDropDown());
        priorityLayout.setOnClickListener(v -> priorityInput.showDropDown());

        dueDateLayout.setEndIconOnClickListener(v -> showDatePicker());
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
        SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);
        String select = "*,team:teams(*)";
        api.getProjectById("eq." + projectId, select).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Project project = response.body().get(0);
                    String teamId = project.teamId;
                    if (teamId != null) loadTeamMembers(teamId);
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                Toast.makeText(CreateTaskActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeamMembers(String teamId) {
        SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);
        api.getTeamMembersWithDetails("eq." + teamId).enqueue(new Callback<List<TeamMemberWithDetails>>() {
            @Override
            public void onResponse(Call<List<TeamMemberWithDetails>> call, Response<List<TeamMemberWithDetails>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    teamMembers.clear();
                    displayNameToUserIdMap.clear();
                    List<String> memberDisplayNames = new ArrayList<>();
                    memberDisplayNames.add("Unassigned");
                    displayNameToUserIdMap.put("Unassigned", null);

                    for (TeamMemberWithDetails teamMember : response.body()) {
                        teamMembers.add(teamMember);
                        String displayName = teamMember.getFullName() + " (" + teamMember.email + ")";
                        memberDisplayNames.add(displayName);
                        displayNameToUserIdMap.put(displayName, teamMember.userId);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            CreateTaskActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            memberDisplayNames
                    );
                    assignedToInput.setAdapter(adapter);

                    assignedToInput.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedDisplayName = adapter.getItem(position);
                        String selectedUserId = displayNameToUserIdMap.get(selectedDisplayName);
                        assignedToInput.setTag(selectedUserId);
                    });
                    assignedToInput.setText("Unassigned", false);
                }
            }

            @Override
            public void onFailure(Call<List<TeamMemberWithDetails>> call, Throwable t) {
                Toast.makeText(CreateTaskActivity.this, "Failed to load members", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();
        String estimatedHoursStr = estimatedHoursInput.getText().toString().trim();
        String priority = priorityInput.getText().toString().trim();

        String selectedStatusDisplay = statusInput.getText().toString().trim();
        String status = "todo";
        for (int i = 0; i < statusDisplayOptions.length; i++) {
            if (statusDisplayOptions[i].equals(selectedStatusDisplay)) {
                status = statusValueOptions[i];
                break;
            }
        }

        String assignedTo = assignedToInput.getTag() != null ? assignedToInput.getTag().toString() : null;
        if ("Unassigned".equals(assignedToInput.getText().toString())) {
            assignedTo = null;
        }

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }

        Double estimatedHours = 0.0;
        if (!estimatedHoursStr.isEmpty()) {
            try {
                estimatedHours = Double.parseDouble(estimatedHoursStr);

                if (estimatedHours < 0) {
                    estimatedHoursInput.setError("Must be positive");
                    return;
                }

                if (estimatedHours >= 100) {
                    estimatedHoursInput.setError("Hours must be less than 100");
                    return;
                }

            } catch (NumberFormatException e) {
                estimatedHoursInput.setError("Invalid number format");
                return;
            }
        }

        viewModel.createTask(projectId, title, description, assignedTo, status, priority,
                dueDate.isEmpty() ? null : dueDate, estimatedHours, null, null);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}