package ba.sum.fsre.projectflow.team;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.viewmodel.TeamViewModel;

public class CreateTeamActivity extends AppCompatActivity {

    private TeamViewModel viewModel;
    private TextInputEditText etTeamName, etTeamDescription;
    private Button btnCreateTeam;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_team);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        etTeamName = findViewById(R.id.etTeamName);
        etTeamDescription = findViewById(R.id.etTeamDescription);
        btnCreateTeam = findViewById(R.id.btnCreateTeam);
        progressBar = findViewById(R.id.progressBar);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        btnCreateTeam.setOnClickListener(v -> {
            String name = etTeamName.getText().toString().trim();
            String description = etTeamDescription.getText().toString().trim();

            if (name.isEmpty()) {
                etTeamName.setError("Name is required");
                return;
            }

            viewModel.createTeam(this, name, description);
        });

        observeViewModel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void observeViewModel() {
        viewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Team created successfully", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnCreateTeam.setEnabled(!isLoading);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
