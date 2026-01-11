package ba.sum.fsre.projectflow.team;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.TeamInvitation;
import ba.sum.fsre.projectflow.storage.TokenManager;
import ba.sum.fsre.projectflow.viewmodel.TeamViewModel;

public class MyInvitationsActivity extends AppCompatActivity {

    private TeamViewModel viewModel;
    private MyInvitationsAdapter adapter;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_list);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);
        

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("My Invitations");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
             getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView rvTeams = findViewById(R.id.rvTeams);
        progressBar = findViewById(R.id.progressBar);
        findViewById(R.id.fabCreateTeam).setVisibility(View.GONE);

        rvTeams.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MyInvitationsAdapter(new MyInvitationsAdapter.OnInvitationActionListener() {
            @Override
            public void onAccept(TeamInvitation invitation) {
                viewModel.acceptInvitation(MyInvitationsActivity.this, invitation);
            }

            @Override
            public void onDecline(TeamInvitation invitation) {
                viewModel.respondToInvitation(MyInvitationsActivity.this, invitation.id, "rejected");
            }
        });
        rvTeams.setAdapter(adapter);

        observeViewModel();
        
        viewModel.loadMyInvitations(this);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void observeViewModel() {
        viewModel.getTeamInvitations().observe(this, invitations -> {
             adapter.setInvitations(invitations);
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
        
         viewModel.getOperationSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Operation successful", Toast.LENGTH_SHORT).show();
                viewModel.loadMyInvitations(this);
            }
        });
    }
}
