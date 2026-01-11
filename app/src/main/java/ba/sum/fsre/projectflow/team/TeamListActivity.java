package ba.sum.fsre.projectflow.team;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.viewmodel.TeamViewModel;

public class TeamListActivity extends AppCompatActivity {

    private TeamViewModel viewModel;
    private TeamAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        RecyclerView rvTeams = findViewById(R.id.rvTeams);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabCreateTeam = findViewById(R.id.fabCreateTeam);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        findViewById(R.id.btnInvitations).setOnClickListener(v -> {
            startActivity(new android.content.Intent(this, MyInvitationsActivity.class));
        });

        rvTeams.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TeamAdapter(team -> {
             Intent intent = new Intent(TeamListActivity.this, TeamDetailsActivity.class);
             intent.putExtra("team_id", team.id);
             intent.putExtra("team_name", team.name);
             startActivity(intent);
        });
        rvTeams.setAdapter(adapter);

        fabCreateTeam.setOnClickListener(v -> {
            startActivity(new Intent(TeamListActivity.this, CreateTeamActivity.class));
        });

        observeViewModel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void observeViewModel() {
        viewModel.getMyTeams().observe(this, teams -> {
            adapter.setTeams(teams);
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getHasPendingInvitations().observe(this, hasPending -> {
            View badge = findViewById(R.id.viewBadge);
            if (hasPending != null && hasPending) {
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadMyTeams(this);
        viewModel.checkPendingInvitations(this);
    }
}
