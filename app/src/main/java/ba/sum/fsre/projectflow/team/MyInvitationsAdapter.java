package ba.sum.fsre.projectflow.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.TeamInvitation;

public class MyInvitationsAdapter extends RecyclerView.Adapter<MyInvitationsAdapter.ViewHolder> {

    private List<TeamInvitation> invitations = new ArrayList<>();
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onAccept(TeamInvitation invitation);
        void onDecline(TeamInvitation invitation);
    }

    public MyInvitationsAdapter(OnInvitationActionListener listener) {
        this.listener = listener;
    }

    public void setInvitations(List<TeamInvitation> invitations) {
        this.invitations = invitations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_invitation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamInvitation invitation = invitations.get(position);
        holder.bind(invitation, listener);
    }

    @Override
    public int getItemCount() {
        return invitations.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTeamName, tvInvitedBy;
        Button btnAccept, btnDecline;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTeamName = itemView.findViewById(R.id.tvTeamNameInv);
            tvInvitedBy = itemView.findViewById(R.id.tvInvitedBy);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnDecline = itemView.findViewById(R.id.btnDecline);
        }

        public void bind(TeamInvitation invitation, OnInvitationActionListener listener) {
            if (invitation.teams != null) {
                tvTeamName.setText(invitation.teams.name);
            } else {
                tvTeamName.setText("Unknown Team");
            }
            if (invitation.invitedByUser != null) {
                String name = invitation.invitedByUser.first_name + " " + invitation.invitedByUser.last_name;
                tvInvitedBy.setText("Invited by: " + name);
            } else {
                tvInvitedBy.setText("Invited by: " + invitation.invitedBy);
            }

            btnAccept.setOnClickListener(v -> listener.onAccept(invitation));
            btnDecline.setOnClickListener(v -> listener.onDecline(invitation));
        }
    }
}
