package ba.sum.fsre.projectflow.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.storage.TokenManager;
import ba.sum.fsre.projectflow.ui.TaskFragment;
import ba.sum.fsre.projectflow.viewmodel.ProjectViewModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectListActivity extends AppCompatActivity {

    private ProjectViewModel viewModel;
    private ProjectAdapter adapter;

    // UI References
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView headerSubtitle;
    private TextView filterAll, filterActive, filterCompleted, filterOnHold;

    // Views to toggle visibility when fragment opens
    private View mainContentLayout;
    private FloatingActionButton fabCreate;
    private FrameLayout fragmentContainer;

    private String teamId;
    private String teamName;
    private List<Project> allProjects = new ArrayList<>();
    private String currentFilter = "all";

    // Default to member until we confirm otherwise
    private String myRole = "member";
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        teamId = getIntent().getStringExtra("team_id");
        teamName = getIntent().getStringExtra("team_name");

        // Get User ID for role checking
        currentUserId = new TokenManager(this).getUserId();

        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        // 1. Initialize views
        mainContentLayout = findViewById(R.id.mainContentLayout);
        fabCreate = findViewById(R.id.fabCreateProject);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        setupToolbar();
        setupUI();
        setupFilters();
        observeViewModel();

        // 2. Fetch role immediately so the menu knows if it should show "Delete"
        fetchUserRole();

        // 3. Add BackStack Listener to handle UI toggling
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                // Fragment is OPEN: Hide Activity UI, Show Fragment Container
                mainContentLayout.setVisibility(View.GONE);
                fabCreate.hide();
                fragmentContainer.setVisibility(View.VISIBLE);
            } else {
                // Fragment is CLOSED: Show Activity UI, Hide Fragment Container
                mainContentLayout.setVisibility(View.VISIBLE);
                fabCreate.show();
                fragmentContainer.setVisibility(View.GONE);

                // Refresh title
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(teamName != null ? teamName + " - Projects" : "Projects");
                }
            }
        });
    }

    private void fetchUserRole() {
        if (teamId == null || currentUserId == null) return;

        SupabaseApi api = RetrofitClient.getClient(this).create(SupabaseApi.class);

        // CORRECTED CALL: We only pass the teamId filter and the select string
        // We fetch all members and filter in Java because the API method signature doesn't support userId filtering
        api.getTeamMembers("eq." + teamId, "*").enqueue(new Callback<List<TeamMember>>() {
            @Override
            public void onResponse(Call<List<TeamMember>> call, Response<List<TeamMember>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamMember> members = response.body();

                    // Loop through members to find the current user
                    for (TeamMember member : members) {
                        // Check if this member record belongs to the current user
                        // Note: Ensure your TeamMember model has a field 'userId' or 'user_id'
                        if (member.userId != null && member.userId.equals(currentUserId)) {
                            myRole = member.role;
                            break; // Found our role, stop looping
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<TeamMember>> call, Throwable t) {
                // Keep default role "member"
            }
        });
    }

    private void setupToolbar() {
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(teamName != null ? teamName + " - Projects" : "Projects");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
        return true;
    }

    private void setupUI() {
        progressBar = findViewById(R.id.progressBar);
        emptyState = findViewById(R.id.emptyState);
        headerSubtitle = findViewById(R.id.headerSubtitle);

        filterAll = findViewById(R.id.filterAll);
        filterActive = findViewById(R.id.filterActive);
        filterCompleted = findViewById(R.id.filterCompleted);
        filterOnHold = findViewById(R.id.filterOnHold);

        RecyclerView rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProjectAdapter(new ProjectAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                TaskFragment taskFragment = TaskFragment.newInstance(project.id, project.name);

                getSupportFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                android.R.anim.fade_in,
                                android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out
                        )
                        .replace(R.id.fragmentContainer, taskFragment)
                        .addToBackStack("projects")
                        .commit();
            }

            @Override
            public void onProjectLongClick(Project project) {
                showProjectOptionsDialog(project);
            }

            @Override
            public void onMenuClick(View view, Project project) {
                showPopupMenu(view, project);
            }
        });
        rvProjects.setAdapter(adapter);

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateProjectActivity.class);
            intent.putExtra("team_id", teamId);
            startActivity(intent);
        });
    }

    private void showPopupMenu(View view, Project project) {
        android.widget.PopupMenu popup = new android.widget.PopupMenu(this, view);

        // 1. Always add Edit
        popup.getMenu().add(0, 1, 0, "Edit");

        // 2. Only add "Delete" if role is 'owner'
        if (myRole != null && myRole.equalsIgnoreCase("owner")) {
            popup.getMenu().add(0, 2, 1, "Delete");
        }

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();

            if (title.equals("Edit")) {
                Intent intent = new Intent(ProjectListActivity.this, EditProjectActivity.class);
                intent.putExtra("project_data", project);
                startActivity(intent);
                return true;
            } else if (title.equals("Delete")) {
                confirmDeleteProject(project);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> applyFilter("all", filterAll));
        filterActive.setOnClickListener(v -> applyFilter("active", filterActive));
        filterCompleted.setOnClickListener(v -> applyFilter("completed", filterCompleted));
        filterOnHold.setOnClickListener(v -> applyFilter("on_hold", filterOnHold));
    }

    private void applyFilter(String filter, TextView selectedView) {
        currentFilter = filter;
        resetFilterStyles();

        selectedView.setBackgroundResource(R.drawable.bg_filter_selected);
        selectedView.setTextColor(getColor(R.color.white));

        List<Project> filteredProjects = new ArrayList<>();
        for (Project project : allProjects) {
            String status = project.status != null ? project.status.toLowerCase() : "active";
            if ("all".equals(filter) || filter.equals(status)) {
                filteredProjects.add(project);
            }
        }

        adapter.setProjects(filteredProjects);
        updateHeader(filteredProjects.size());
        updateEmptyState(filteredProjects.isEmpty());
    }

    private void resetFilterStyles() {
        filterAll.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterAll.setTextColor(getColor(R.color.text_primary));
        filterActive.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterActive.setTextColor(getColor(R.color.text_primary));
        filterCompleted.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterCompleted.setTextColor(getColor(R.color.text_primary));
        filterOnHold.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterOnHold.setTextColor(getColor(R.color.text_primary));
    }

    private void updateHeader(int count) {
        headerSubtitle.setText(count + " Project" + (count != 1 ? "s" : ""));
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showProjectOptionsDialog(Project project) {
        // Fallback for long click - check role here too
        boolean isOwner = myRole != null && myRole.equalsIgnoreCase("owner");

        List<String> optionsList = new ArrayList<>();
        optionsList.add("Edit");
        if (isOwner) optionsList.add("Delete");

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(this)
                .setTitle(project.name)
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (selected.equals("Edit")) {
                        Intent intent = new Intent(this, CreateProjectActivity.class);
                        intent.putExtra("team_id", teamId);
                        intent.putExtra("project", project);
                        intent.putExtra("is_edit", true);
                        startActivity(intent);
                    } else if (selected.equals("Delete")) {
                        confirmDeleteProject(project);
                    }
                })
                .show();
    }

    private void confirmDeleteProject(Project project) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete \"" + project.name + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteProject(this, teamId, project.id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        viewModel.getProjects().observe(this, projects -> {
            allProjects = projects != null ? projects : new ArrayList<>();
            applyFilter(currentFilter, getSelectedFilterView());
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
    }

    private TextView getSelectedFilterView() {
        switch (currentFilter) {
            case "active": return filterActive;
            case "completed": return filterCompleted;
            case "on_hold": return filterOnHold;
            default: return filterAll;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.loadProjects(this, teamId);
    }
}