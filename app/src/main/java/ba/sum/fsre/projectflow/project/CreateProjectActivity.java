package ba.sum.fsre.projectflow.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.viewmodel.ProjectViewModel;

public class CreateProjectActivity extends AppCompatActivity {

    private ProjectViewModel viewModel;
    private TextInputEditText etName, etDescription, etStartDate, etEndDate;
    private Spinner spinnerStatus;
    private MaterialButton btnSubmit;
    private ProgressBar progressBar;

    private String teamId;
    private Project existingProject;
    private boolean isEditMode = false;

    private Calendar startCalendar = Calendar.getInstance();
    private Calendar endCalendar = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    private String[] statusOptions = {"Active", "Completed", "On Hold", "Cancelled"};
    private String[] statusValues = {"active", "completed", "on_hold", "cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

        teamId = getIntent().getStringExtra("team_id");
        isEditMode = getIntent().getBooleanExtra("is_edit", false);
        existingProject = (Project) getIntent().getSerializableExtra("project");

        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        setupToolbar();
        setupUI();
        setupDatePickers();
        observeViewModel();

        if (isEditMode && existingProject != null) {
            populateForm();
        }
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Project" : "Create Project");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupUI() {
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        TextView formTitle = findViewById(R.id.formTitle);
        formTitle.setText(isEditMode ? "Edit Project" : "Project Details");
        btnSubmit.setText(isEditMode ? "Update Project" : "Create Project");

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                statusOptions
        );
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        btnSubmit.setOnClickListener(v -> submitForm());
    }

    private void setupDatePickers() {
        etStartDate.setOnClickListener(v -> showDatePicker(true));
        etEndDate.setOnClickListener(v -> showDatePicker(false));
    }

    private void showDatePicker(boolean isStartDate) {
        Calendar calendar = isStartDate ? startCalendar : endCalendar;
        
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    String formattedDate = dateFormat.format(calendar.getTime());
                    if (isStartDate) {
                        etStartDate.setText(formattedDate);
                    } else {
                        etEndDate.setText(formattedDate);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.show();
    }

    private void populateForm() {
        if (existingProject == null) return;

        etName.setText(existingProject.name);
        etDescription.setText(existingProject.description);
        etStartDate.setText(existingProject.startDate);
        etEndDate.setText(existingProject.endDate);

        String status = existingProject.status != null ? existingProject.status.toLowerCase() : "active";
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equals(status)) {
                spinnerStatus.setSelection(i);
                break;
            }
        }
    }

    private void submitForm() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String startDate = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDate = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";
        String status = statusValues[spinnerStatus.getSelectedItemPosition()];

        if (name.isEmpty()) {
            etName.setError("Project name is required");
            etName.requestFocus();
            return;
        }

        if (isEditMode && existingProject != null) {
            viewModel.updateProject(this, existingProject.id, name, description, startDate, endDate, status);
        } else {
            viewModel.createProject(this, teamId, name, description, startDate, endDate, status);
        }
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnSubmit.setEnabled(!isLoading);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigateBack().observe(this, shouldNavigate -> {
            if (shouldNavigate != null && shouldNavigate) {
                Toast.makeText(this, isEditMode ? "Project updated!" : "Project created!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
