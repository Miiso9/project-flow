package ba.sum.fsre.projectflow.project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout;

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
import ba.sum.fsre.projectflow.ui.TaskFragment; // Make sure this import is correct
import ba.sum.fsre.projectflow.viewmodel.ProjectViewModel;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_list);

        teamId = getIntent().getStringExtra("team_id");
        teamName = getIntent().getStringExtra("team_name");

        viewModel = new ViewModelProvider(this).get(ProjectViewModel.class);

        // 1. Initialize views
        mainContentLayout = findViewById(R.id.mainContentLayout);
        fabCreate = findViewById(R.id.fabCreateProject);
        fragmentContainer = findViewById(R.id.fragmentContainer);

        setupToolbar();
        setupUI();
        setupFilters();
        observeViewModel();

        // 2. Add BackStack Listener to handle UI toggling
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
        // Handle back button on toolbar
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
                // 3. Open TaskFragment
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

                // The addOnBackStackChangedListener will handle hiding the toolbar automatically
            }

            @Override
            public void onProjectLongClick(Project project) {
                showProjectOptionsDialog(project);
            }
        });
        rvProjects.setAdapter(adapter);

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateProjectActivity.class);
            intent.putExtra("team_id", teamId);
            startActivity(intent);
        });
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
        String[] options = {"Edit", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(project.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(this, CreateProjectActivity.class);
                        intent.putExtra("team_id", teamId);
                        intent.putExtra("project", project);
                        intent.putExtra("is_edit", true);
                        startActivity(intent);
                    } else {
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