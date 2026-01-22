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
    private TextInputLayout assignedToLayout, statusLayout, priorityLayout, dueDateLayout;
    private Button updateButton;
    private Button cancelButton;
    private TaskViewModel viewModel;
    private Task task;
    private Calendar dueDateCalendar = Calendar.getInstance();

    private List<TeamMemberWithDetails> teamMembers = new ArrayList<>();
    private Map<String, String> displayNameToUserIdMap = new HashMap<>();

    // CHANGED: Status Mapping Arrays
    private final String[] statusDisplayOptions = {"Todo", "In Progress", "Done", "Blocked"};
    private final String[] statusValueOptions = {"todo", "in_progress", "done", "blocked"};

    private final String[] priorityOptions = {"low", "medium", "high"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        task = (Task) getIntent().getSerializableExtra("task");
        if (task == null) {
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
        updateButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        assignedToLayout = findViewById(R.id.assignedToLayout);
        statusLayout = findViewById(R.id.statusLayout);
        priorityLayout = findViewById(R.id.priorityLayout);
        dueDateLayout = findViewById(R.id.dueDateLayout);

        TextView formTitle = findViewById(R.id.formTitle);
        TextView formSubtitle = findViewById(R.id.formSubtitle);
        formTitle.setText("Edit Task");
        formSubtitle.setText("Update the task details");

        updateButton.setText("Update Task");
        updateButton.setOnClickListener(v -> updateTask());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Task");
        }
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
            if (error != null && !error.isEmpty()) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getLoading().observe(this, isLoading -> {
            updateButton.setEnabled(!isLoading);
            updateButton.setText(isLoading ? "Updating..." : "Update Task");
        });
    }

    private void setupDropdowns() {
        // CHANGED: Use Status Display Options
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                statusDisplayOptions
        );
        statusInput.setAdapter(statusAdapter);

        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                priorityOptions
        );
        priorityInput.setAdapter(priorityAdapter);
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
            if (hasFocus) showDatePicker();
        });

        if (task.dueDate != null && !task.dueDate.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                dueDateCalendar.setTime(sdf.parse(task.dueDate));
            } catch (Exception e) { e.printStackTrace(); }
        }
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
        api.getProjectById("eq." + task.projectId, select).enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Project project = response.body().get(0);
                    if (project.teamId != null) loadTeamMembers(project.teamId);
                    else populateForm();
                } else {
                    populateForm();
                }
            }
            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) { populateForm(); }
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
                            EditTaskActivity.this,
                            android.R.layout.simple_dropdown_item_1line,
                            memberDisplayNames
                    );
                    assignedToInput.setAdapter(adapter);

                    assignedToInput.setOnItemClickListener((parent, view, position, id) -> {
                        String selectedDisplayName = adapter.getItem(position);
                        String selectedUserId = displayNameToUserIdMap.get(selectedDisplayName);
                        assignedToInput.setTag(selectedUserId);
                    });
                    populateForm();
                } else { populateForm(); }
            }
            @Override
            public void onFailure(Call<List<TeamMemberWithDetails>> call, Throwable t) { populateForm(); }
        });
    }

    private void populateForm() {
        titleInput.setText(task.title);
        descriptionInput.setText(task.description);

        // CHANGED: Map Database Status back to Display Status
        if (task.status != null) {
            String displayStatus = "Todo"; // Default
            for (int i = 0; i < statusValueOptions.length; i++) {
                if (statusValueOptions[i].equals(task.status)) {
                    displayStatus = statusDisplayOptions[i];
                    break;
                }
            }
            statusInput.setText(displayStatus, false);
        }

        if (task.priority != null) priorityInput.setText(task.priority, false);
        if (task.dueDate != null) dueDateInput.setText(task.dueDate);
        if (task.estimatedHours != null && task.estimatedHours > 0) estimatedHoursInput.setText(String.valueOf(task.estimatedHours));

        if (task.assignedUser != null && task.assignedUser.email != null) {
            String displayName = task.assignedUser.first_name + " " + task.assignedUser.last_name + " (" + task.assignedUser.email + ")";
            assignedToInput.setText(displayName, false);
            assignedToInput.setTag(task.assignedTo);
        } else if (task.assignedTo != null) {
            assignedToInput.setText("Unknown", false);
            assignedToInput.setTag(task.assignedTo);
        } else {
            assignedToInput.setText("Unassigned", false);
            assignedToInput.setTag(null);
        }
    }

    private void updateTask() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String dueDate = dueDateInput.getText().toString().trim();
        String estimatedHoursStr = estimatedHoursInput.getText().toString().trim();
        String priority = priorityInput.getText().toString().trim();

        // CHANGED: Map Display Status to Database Value
        String selectedStatusDisplay = statusInput.getText().toString().trim();
        String status = "todo";
        for (int i = 0; i < statusDisplayOptions.length; i++) {
            if (statusDisplayOptions[i].equals(selectedStatusDisplay)) {
                status = statusValueOptions[i];
                break;
            }
        }

        String assignedTo = assignedToInput.getTag() != null ? assignedToInput.getTag().toString() : task.assignedTo;
        if ("Unassigned".equals(assignedToInput.getText().toString())) assignedTo = null;

        if (title.isEmpty()) {
            titleInput.setError("Title is required");
            return;
        }

        Double estimatedHours = task.estimatedHours;
        if (!estimatedHoursStr.isEmpty()) {
            try {
                estimatedHours = Double.parseDouble(estimatedHoursStr);
            } catch (NumberFormatException e) {
                estimatedHoursInput.setError("Invalid number format");
                return;
            }
        }

        viewModel.updateTask(task.id, title, description, assignedTo, status, priority,
                dueDate.isEmpty() ? null : dueDate, estimatedHours, task.startTime, task.endTime);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}