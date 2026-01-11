package ba.sum.fsre.projectflow.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.Team;
import ba.sum.fsre.projectflow.model.TeamMember;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamViewHolder> {

    private List<TeamMember> teamMembers = new ArrayList<>();
    private OnTeamClickListener listener;

    public interface OnTeamClickListener {
        void onTeamClick(Team team);
    }

    public TeamAdapter(OnTeamClickListener listener) {
        this.listener = listener;
    }

    public void setTeams(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        TeamMember member = teamMembers.get(position);
        if (member.teams != null) {
            holder.bind(member.teams, listener);
        }
    }

    @Override
    public int getItemCount() {
        return teamMembers.size();
    }

    static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName;
        TextView tvTeamDescription;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamName);
            tvTeamDescription = itemView.findViewById(R.id.tvTeamDescription);
        }

        public void bind(final Team team, final OnTeamClickListener listener) {
            tvTeamName.setText(team.name);
            tvTeamDescription.setText(team.description);
            itemView.setOnClickListener(v -> {
                android.content.Intent intent = new android.content.Intent(itemView.getContext(), TeamDetailsActivity.class);
                intent.putExtra("team_id", team.id);
                intent.putExtra("team_name", team.name);
                intent.putExtra("team_description", team.description);
                itemView.getContext().startActivity(intent);
            });
        }
    }
}
