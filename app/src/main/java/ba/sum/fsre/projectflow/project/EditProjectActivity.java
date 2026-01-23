package ba.sum.fsre.projectflow.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View; // Added import

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

    private final String[] statusOptions = {"Active", "Completed", "On Hold", "Cancelled"};
    private final String[] statusValues = {"active", "completed", "on_hold", "cancelled"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_project);

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

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, statusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapter);

        etStartDate.setOnClickListener(v -> showDatePicker(etStartDate));
        etEndDate.setOnClickListener(v -> showDatePicker(etEndDate));

        btnSubmit.setOnClickListener(v -> performUpdate());
    }

    private void setupUIForEditMode() {
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

        if (projectToEdit.status != null) {
            int spinnerPosition = getStatusIndex(projectToEdit.status);
            spinnerStatus.setSelection(spinnerPosition);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnSubmit.setEnabled(!isLoading);
            }
        });

        viewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Project updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getError().observe(this, errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performUpdate() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        String startDateRaw = etStartDate.getText() != null ? etStartDate.getText().toString().trim() : "";
        String endDateRaw = etEndDate.getText() != null ? etEndDate.getText().toString().trim() : "";

        int statusIndex = spinnerStatus.getSelectedItemPosition();
        String status = statusValues[statusIndex];

        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        String startDate = startDateRaw.isEmpty() ? null : startDateRaw;
        String endDate = endDateRaw.isEmpty() ? null : endDateRaw;

        viewModel.updateProject(
                this,
                projectToEdit.id,
                name,
                description,
                startDate,
                endDate,
                status
        );
    }

    private void showDatePicker(TextInputEditText targetField) {
        final Calendar c = Calendar.getInstance();

        if (targetField.getText() != null && !targetField.getText().toString().isEmpty()) {
            try {
                String[] parts = targetField.getText().toString().split("-");
                c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
            } catch (Exception e) {
            }
        }

        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%04d-%02d-%02d", year1, monthOfYear + 1, dayOfMonth);
                    targetField.setText(selectedDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private int getStatusIndex(String status) {
        for (int i = 0; i < statusValues.length; i++) {
            if (statusValues[i].equalsIgnoreCase(status)) {
                return i;
            }
        }
        return 0;
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