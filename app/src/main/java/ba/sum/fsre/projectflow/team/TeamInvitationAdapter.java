package ba.sum.fsre.projectflow.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.TeamInvitation;

public class TeamInvitationAdapter extends RecyclerView.Adapter<TeamInvitationAdapter.ViewHolder> {

    private List<TeamInvitation> invitations = new ArrayList<>();
    private OnInvitationActionListener listener;

    public interface OnInvitationActionListener {
        void onDeleteInvitation(TeamInvitation invitation);
    }

    public TeamInvitationAdapter(OnInvitationActionListener listener) {
        this.listener = listener;
    }

    public void setInvitations(List<TeamInvitation> invitations) {
        this.invitations = invitations;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_invitation, parent, false);
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
        TextView tvInvitedEmail, tvStatus;
        ImageButton btnDeleteInvitation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInvitedEmail = itemView.findViewById(R.id.tvInvitedEmail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnDeleteInvitation = itemView.findViewById(R.id.btnDeleteInvitation);
        }

        public void bind(TeamInvitation invitation, OnInvitationActionListener listener) {
            tvInvitedEmail.setText(invitation.invitedEmail);
            tvStatus.setText(invitation.status);

            if (listener != null) {
                btnDeleteInvitation.setVisibility(View.VISIBLE);
                btnDeleteInvitation.setOnClickListener(v -> listener.onDeleteInvitation(invitation));
            } else {
                btnDeleteInvitation.setVisibility(View.GONE);
            }
        }
    }
}
