package ba.sum.fsre.projectflow.team;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.TeamInvitation;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.project.ProjectListActivity;
import ba.sum.fsre.projectflow.storage.TokenManager;
import ba.sum.fsre.projectflow.viewmodel.TeamViewModel;

public class TeamDetailsActivity extends AppCompatActivity {

    private TeamViewModel viewModel;
    private String teamId;
    private String teamName;
    private TeamMemberAdapter memberAdapter;
    private TeamInvitationAdapter invitationAdapter;
    private ProgressBar progressBar;
    private String currentUserId;

    private java.util.List<TeamInvitation> allInvitations;
    private android.widget.LinearLayout llInvitationsSection;

    private String teamDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_details);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbarDetails);
        setSupportActionBar(toolbar);

        teamId = getIntent().getStringExtra("team_id");
        teamName = getIntent().getStringExtra("team_name");
        teamDescription = getIntent().getStringExtra("team_description");
        currentUserId = new TokenManager(this).getUserId();

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        setupUI();
        observeViewModel();
        
        viewModel.loadTeamDetails(this, teamId);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Team Details");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupUI() {
        TextView tvName = findViewById(R.id.tvTeamNameDetails);
        TextView tvDesc = findViewById(R.id.tvTeamDescriptionDetails);
        
        tvName.setText(teamName);
        if (teamDescription != null && !teamDescription.isEmpty()) {
            tvDesc.setText(teamDescription);
        } else {
            tvDesc.setText("");
        }

        progressBar = findViewById(R.id.progressBarDetails);
        llInvitationsSection = findViewById(R.id.llInvitationsSection);
        Button btnInvite = findViewById(R.id.btnInviteMember);
        Button btnHistory = findViewById(R.id.btnInvitationHistory);

        RecyclerView rvMembers = findViewById(R.id.rvMembers);
        rvMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new TeamMemberAdapter(new TeamMemberAdapter.OnMemberActionListener() {
            @Override
            public void onUpdateRole(TeamMember member, String newRole) {
                 viewModel.updateRole(TeamDetailsActivity.this, teamId, member.id, newRole);
            }

            @Override
            public void onRemoveMember(TeamMember member) {
                new AlertDialog.Builder(TeamDetailsActivity.this)
                        .setTitle("Remove Member")
                        .setMessage("Are you sure you want to remove this member?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            viewModel.removeMember(TeamDetailsActivity.this, teamId, member.id);
                        })
                        .setNegativeButton("No", null)
                        .show();
            }
        }, false, currentUserId);
        rvMembers.setAdapter(memberAdapter);

        RecyclerView rvInvitations = findViewById(R.id.rvInvitations);
        rvInvitations.setLayoutManager(new LinearLayoutManager(this));
        invitationAdapter = new TeamInvitationAdapter(invitation -> {
             new AlertDialog.Builder(TeamDetailsActivity.this)
                        .setTitle("Delete Invitation")
                        .setMessage("Are you sure you want to cancel this invitation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            viewModel.deleteInvitation(TeamDetailsActivity.this, teamId, invitation.id);
                        })
                        .setNegativeButton("No", null)
                        .show();
        });
        rvInvitations.setAdapter(invitationAdapter);

        btnInvite.setOnClickListener(v -> showInviteDialog());
        btnHistory.setOnClickListener(v -> showHistoryDialog());

        Button btnLeave = findViewById(R.id.btnLeaveTeam);
        Button btnDelete = findViewById(R.id.btnDeleteTeam);

        btnLeave.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Leave Team")
                    .setMessage("Are you sure you want to leave this team?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        viewModel.leaveTeam(this, teamId);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Team")
                    .setMessage("Are you sure you want to delete this team? This action cannot be undone.")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        viewModel.deleteTeam(this, teamId);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        // Projects button
        findViewById(R.id.btnViewProjects).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, ProjectListActivity.class);
            intent.putExtra("team_id", teamId);
            intent.putExtra("team_name", teamName);
            startActivity(intent);
        });
    }

    private void showInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite Member");

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setHint("Enter email address");
        layout.addView(input);

        final android.widget.Spinner roleSpinner = new android.widget.Spinner(this);
        String[] roles = new String[]{"Member", "Admin", "Viewer"};
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        layout.addView(roleSpinner);

        builder.setView(layout);

        builder.setPositiveButton("Invite", (dialog, which) -> {
            String email = input.getText().toString().trim();
            String selectedRole = roleSpinner.getSelectedItem().toString().toLowerCase();
            if (!email.isEmpty()) {
                viewModel.inviteMember(this, teamId, email, selectedRole);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
    
    private void showHistoryDialog() {
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(32, 32, 32, 32);

        RecyclerView historyRv = new RecyclerView(this);
        historyRv.setLayoutManager(new LinearLayoutManager(this));
        
        TeamInvitationAdapter historyAdapter = new TeamInvitationAdapter(null); 
        java.util.List<TeamInvitation> historyList = new java.util.ArrayList<>();
        if (allInvitations != null) {
            for (TeamInvitation inv : allInvitations) {
                if (!"pending".equals(inv.status)) {
                    historyList.add(inv);
                }
            }
        }
        historyAdapter.setInvitations(historyList);
        historyRv.setAdapter(historyAdapter);
        
        layout.addView(historyRv);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invitation History");
        builder.setView(layout);
        
        builder.setPositiveButton("Close", null);
        
        if (!historyList.isEmpty()) {
            builder.setNeutralButton("Clear History", (dialog, which) -> {
                 new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to delete all past invitations?")
                    .setPositiveButton("Yes", (d, w) -> {
                        viewModel.clearInvitationHistory(this, teamId);
                    })
                    .setNegativeButton("No", null)
                    .show();
            });
        }
        
        builder.show();
    }

    private void observeViewModel() {
        viewModel.getTeamMembers().observe(this, members -> {
            memberAdapter.setMembers(members);
            checkCurrentUserRole(members);
        });

        viewModel.getTeamInvitations().observe(this, invitations -> {
            allInvitations = invitations;
            
            java.util.List<TeamInvitation> pendingList = new java.util.ArrayList<>();
            if (invitations != null) {
                for (TeamInvitation inv : invitations) {
                    if ("pending".equals(inv.status)) {
                        pendingList.add(inv);
                    }
                }
            }
            invitationAdapter.setInvitations(pendingList);
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
            }
        });

        viewModel.getNavigateBack().observe(this, shouldFinish -> {
             if (shouldFinish != null && shouldFinish) {
                 finish();
             }
        });
    }

    private void checkCurrentUserRole(List<TeamMember> members) {
        boolean isOwner = false;
        boolean isAdmin = false;

        for (TeamMember member : members) {
            if (member.userId.equals(currentUserId)) {
                if ("owner".equalsIgnoreCase(member.role)) {
                    isOwner = true;
                } else if ("admin".equalsIgnoreCase(member.role)) {
                    isAdmin = true;
                }
                break;
            }
        }
        memberAdapter.setCurrentUserOwner(isOwner);
        
        if (isOwner || isAdmin) {
            llInvitationsSection.setVisibility(View.VISIBLE);
        } else {
            llInvitationsSection.setVisibility(View.GONE);
        }
        
        Button btnLeave = findViewById(R.id.btnLeaveTeam);
        Button btnDelete = findViewById(R.id.btnDeleteTeam);
        
        if (isOwner) {
            btnDelete.setVisibility(View.VISIBLE);
            btnLeave.setVisibility(View.GONE); 
        } else {
            btnDelete.setVisibility(View.GONE);
            btnLeave.setVisibility(View.VISIBLE);
        }
    }
}
