package ba.sum.fsre.projectflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.model.TeamMember;
import ba.sum.fsre.projectflow.team.CreateTeamActivity;
import ba.sum.fsre.projectflow.team.MyInvitationsActivity;
import ba.sum.fsre.projectflow.team.TeamAdapter;
import ba.sum.fsre.projectflow.team.TeamDetailsActivity;
import ba.sum.fsre.projectflow.viewmodel.TeamViewModel;

public class TeamsFragment extends Fragment {

    private TeamViewModel viewModel;
    private TeamAdapter adapter;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView headerSubtitle;
    private View viewBadge;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        viewModel = new ViewModelProvider(this).get(TeamViewModel.class);

        setupUI(view);
        observeViewModel();

        return view;
    }

    private void setupUI(View view) {
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        headerSubtitle = view.findViewById(R.id.headerSubtitle);
        viewBadge = view.findViewById(R.id.viewBadge);

        RecyclerView rvTeams = view.findViewById(R.id.rvTeams);
        rvTeams.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TeamAdapter(team -> {
            Intent intent = new Intent(getActivity(), TeamDetailsActivity.class);
            intent.putExtra("team_id", team.id);
            intent.putExtra("team_name", team.name);
            intent.putExtra("team_description", team.description);
            startActivity(intent);
        });
        rvTeams.setAdapter(adapter);

        FloatingActionButton fabCreate = view.findViewById(R.id.fabCreateTeam);
        fabCreate.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CreateTeamActivity.class));
        });

        view.findViewById(R.id.btnInvitations).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MyInvitationsActivity.class));
        });
    }

    private void observeViewModel() {
        viewModel.getMyTeams().observe(getViewLifecycleOwner(), teams -> {
            adapter.setTeams(teams);
            updateHeader(teams);
            updateEmptyState(teams == null || teams.isEmpty());
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHasPendingInvitations().observe(getViewLifecycleOwner(), hasPending -> {
            if (hasPending != null && hasPending) {
                viewBadge.setVisibility(View.VISIBLE);
            } else {
                viewBadge.setVisibility(View.GONE);
            }
        });
    }

    private void updateHeader(List<TeamMember> teams) {
        int count = teams != null ? teams.size() : 0;
        headerSubtitle.setText(count + " Team" + (count != 1 ? "s" : ""));
    }

    private void updateEmptyState(boolean isEmpty) {
        emptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadMyTeams(requireContext());
        viewModel.checkPendingInvitations(requireContext());
    }
}
