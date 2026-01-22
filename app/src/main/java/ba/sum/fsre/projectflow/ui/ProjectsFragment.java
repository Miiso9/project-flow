package ba.sum.fsre.projectflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.project.CreateProjectActivity;
import ba.sum.fsre.projectflow.project.EditProjectActivity;
import ba.sum.fsre.projectflow.project.ProjectAdapter;
import ba.sum.fsre.projectflow.storage.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectsFragment extends Fragment {

    private ProjectAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView headerSubtitle;
    private TextView filterAll, filterActive, filterCompleted, filterOnHold;

    private List<Project> allProjects = new ArrayList<>();

    // Map to store the current user's role for each team (Key: TeamID, Value: Role)
    private Map<String, String> userTeamRoles = new HashMap<>();

    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        setupUI(view);
        setupFilters(view);

        return view;
    }

    private void setupUI(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        headerSubtitle = view.findViewById(R.id.headerSubtitle);

        filterAll = view.findViewById(R.id.filterAll);
        filterActive = view.findViewById(R.id.filterActive);
        filterCompleted = view.findViewById(R.id.filterCompleted);
        filterOnHold = view.findViewById(R.id.filterOnHold);

        RecyclerView rvProjects = view.findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProjectAdapter(new ProjectAdapter.OnProjectClickListener() {
            @Override
            public void onProjectClick(Project project) {
                TaskFragment taskFragment = TaskFragment.newInstance(project.id, project.name);
                requireActivity().getSupportFragmentManager().beginTransaction()
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
    }

    /**
     * UPDATED: Conditionally shows "Delete" only if the user is an owner.
     */
    private void showPopupMenu(View view, Project project) {
        PopupMenu popup = new PopupMenu(requireContext(), view);

        // Always add Edit
        popup.getMenu().add(0, 1, 0, "Edit");

        // Check Role: Only add "Delete" if user is owner
        String myRole = userTeamRoles.get(project.teamId);
        if (myRole != null && myRole.equalsIgnoreCase("owner")) {
            popup.getMenu().add(0, 2, 1, "Delete");
        }

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if (title.equals("Edit")) {
                Intent intent = new Intent(getActivity(), EditProjectActivity.class);
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

    private void confirmDeleteProject(Project project) {
        // Double check just to be safe, though UI hides it
        String myRole = userTeamRoles.get(project.teamId);
        if (myRole == null || !myRole.equalsIgnoreCase("owner")) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Project")
                .setMessage("Are you sure you want to delete '" + project.name + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteProject(project))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProject(Project project) {
        progressBar.setVisibility(View.VISIBLE);
        SupabaseApi api = RetrofitClient.getClient(requireContext()).create(SupabaseApi.class);

        api.deleteProject("eq." + project.id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Project deleted", Toast.LENGTH_SHORT).show();
                    allProjects.remove(project);
                    applyFilter(currentFilter, getCurrentFilterView());
                } else {
                    Toast.makeText(getContext(), "Failed to delete project", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllProjects() {
        progressBar.setVisibility(View.VISIBLE);
        allProjects.clear();
        userTeamRoles.clear();

        String userId = new TokenManager(requireContext()).getUserId();
        SupabaseApi api = RetrofitClient.getClient(requireContext()).create(SupabaseApi.class);

        api.getMyTeams("eq." + userId, "*,teams(*)").enqueue(new Callback<List<TeamMember>>() {
            @Override
            public void onResponse(Call<List<TeamMember>> call, Response<List<TeamMember>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TeamMember> teams = response.body();

                    if (teams.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        applyFilter(currentFilter, filterAll);
                        return;
                    }

                    final int[] pendingRequests = {teams.size()};

                    for (TeamMember tm : teams) {
                        // Store the role for UI logic later
                        if (tm.teams != null && tm.role != null) {
                            userTeamRoles.put(tm.teams.id, tm.role);
                        }

                        if (tm.teams != null) {
                            loadProjectsForTeam(tm.teams.id, pendingRequests);
                        } else {
                            pendingRequests[0]--;
                            if (pendingRequests[0] == 0) {
                                progressBar.setVisibility(View.GONE);
                                applyFilter(currentFilter, filterAll);
                            }
                        }
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load teams", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<TeamMember>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProjectsForTeam(String teamId, final int[] pendingRequests) {
        SupabaseApi api = RetrofitClient.getClient(requireContext()).create(SupabaseApi.class);

        api.getProjects("eq." + teamId, "*", "created_at.desc").enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allProjects.addAll(response.body());
                }

                pendingRequests[0]--;
                if (pendingRequests[0] == 0) {
                    progressBar.setVisibility(View.GONE);
                    applyFilter(currentFilter, filterAll);
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                pendingRequests[0]--;
                if (pendingRequests[0] == 0) {
                    progressBar.setVisibility(View.GONE);
                    applyFilter(currentFilter, filterAll);
                }
            }
        });
    }

    private TextView getCurrentFilterView() {
        switch (currentFilter) {
            case "active": return filterActive;
            case "completed": return filterCompleted;
            case "on_hold": return filterOnHold;
            default: return filterAll;
        }
    }

    private void setupFilters(View view) {
        filterAll.setOnClickListener(v -> applyFilter("all", filterAll));
        filterActive.setOnClickListener(v -> applyFilter("active", filterActive));
        filterCompleted.setOnClickListener(v -> applyFilter("completed", filterCompleted));
        filterOnHold.setOnClickListener(v -> applyFilter("on_hold", filterOnHold));
    }

    private void applyFilter(String filter, TextView selectedView) {
        currentFilter = filter;
        resetFilterStyles();

        selectedView.setBackgroundResource(R.drawable.bg_filter_selected);
        selectedView.setTextColor(requireContext().getColor(R.color.white));

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
        filterAll.setTextColor(requireContext().getColor(R.color.text_primary));
        filterActive.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterActive.setTextColor(requireContext().getColor(R.color.text_primary));
        filterCompleted.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterCompleted.setTextColor(requireContext().getColor(R.color.text_primary));
        filterOnHold.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterOnHold.setTextColor(requireContext().getColor(R.color.text_primary));
    }

    private void updateHeader(int count) {
        headerSubtitle.setText(count + " Project" + (count != 1 ? "s" : ""));
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showProjectOptionsDialog(Project project) {
        // Fallback method for long click - we should check role here too if used
        String myRole = userTeamRoles.get(project.teamId);
        boolean isOwner = myRole != null && myRole.equalsIgnoreCase("owner");

        List<String> optionsList = new ArrayList<>();
        optionsList.add("Edit");
        if (isOwner) optionsList.add("Delete");

        String[] options = optionsList.toArray(new String[0]);

        new AlertDialog.Builder(requireContext())
                .setTitle(project.name)
                .setItems(options, (dialog, which) -> {
                    String selected = options[which];
                    if (selected.equals("Edit")) {
                        Intent intent = new Intent(getActivity(), EditProjectActivity.class);
                        intent.putExtra("project_data", project);
                        startActivity(intent);
                    } else if (selected.equals("Delete")) {
                        confirmDeleteProject(project);
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllProjects();
    }
}