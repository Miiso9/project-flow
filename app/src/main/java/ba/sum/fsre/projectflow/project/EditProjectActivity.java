package ba.sum.fsre.projectflow.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.viewmodel.ProjectViewModel;

public class EditProjectActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription, etStartDate, etEndDate;
    private Spinner spinnerStatus;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private Toolbar toolbar;
    private TextView formTitle;

    private ProjectViewModel viewModel;
    private Project projectToEdit;

    // Status options matching your switch-case in the adapter
    private final String[] statusOptions = {"Active", "Completed", "On_Hold", "Cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Reusing the create layout
        setContentView(R.layout.activity_create_project);

        // 1. Get the Project object passed from the previous screen
        if (getIntent().hasExtra("project_data")) {
            projectToEdit = (Project) getIntent().getSerializableExtra("project_data");
        } else {
            Toast.makeText(this, "Error: No project data found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupUIForEditMode();
        setupViewModel();
        populateFields();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        formTitle = findViewById(R.id.formTitle);
        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        spinnerStatus = findViewById(R.id.spinnerStatus);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Setup Toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Setup Spinner Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        // Date Pickers
        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        // Submit Listener
        btnSubmit.setOnClickListener(v -> performUpdate());
    }

    private void setupUIForEditMode() {
        // Change UI text to reflect "Edit" mode
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Edit Project");
        }
        formTitle.setText("Edit Project Details");
        btnSubmit.setText("Save Changes");
    }

    private void populateFields() {
        if (projectToEdit == null) return;

        etName.setText(projectToEdit.name);
        etDescription.setText(projectToEdit.description);
        etStartDate.setText(projectToEdit.startDate);
        etEndDate.setText(projectToEdit.endDate);

        // Select the correct status in the spinner
        if (projectToEdit.status != null) {
            int spinnerPosition = getStatusIndex(projectToEdit.status);
            spinnerStatus.setSelection(spinnerPosition);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        // Observe Loading State
        viewModel.getLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnSubmit.setEnabled(!isLoading);
        });

        // Observe Success
        viewModel.getOperationSuccess().observe(this, success -> {
            if (success) {
                Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                finish(); // Close activity and go back
            }
        });

        // Observe Error
        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performUpdate() {
        String name = etName.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String startDate = etStartDate.getText().toString().trim();
        String endDate = etEndDate.getText().toString().trim();
        String status = spinnerStatus.getSelectedItem().toString();

        if (name.isEmpty()) {
            etName.setError("Name is required");
            return;
        }

        // Call ViewModel update
        viewModel.updateProject(
                this,
                projectToEdit.id, // ID is crucial for update
                name,
                description,
                startDate,
                endDate,
                status
        );
    }

    private void showDatePicker(TextInputEditText targetField) {
        final Calendar c = Calendar.getInstance();

        // If field already has a date, parse it to set the picker
        if (targetField.getText() != null && !targetField.getText().toString().isEmpty()) {
            // Simple parsing logic or leave as current date if format mismatch
            // Assuming format YYYY-MM-DD
            try {
                String[] parts = targetField.getText().toString().split("-");
                c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception e) {
                // Ignore, use current date
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    // Format: YYYY-MM-DD (Supabase standard date format)
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    targetField.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < statusOptions.length; i++) {
            if (statusOptions[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return 0; // Default to first item if not found
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}