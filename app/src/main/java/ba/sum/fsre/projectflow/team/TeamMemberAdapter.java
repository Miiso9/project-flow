package ba.sum.fsre.projectflow.team;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.model.TeamMember;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.ViewHolder> {

    private List<TeamMember> members = new ArrayList<>();
    private OnMemberActionListener listener;
    private boolean isCurrentUserOwner;
    private String currentUserId;

    public interface OnMemberActionListener {
        void onUpdateRole(TeamMember member, String newRole);
        void onRemoveMember(TeamMember member);
    }

    public TeamMemberAdapter(OnMemberActionListener listener, boolean isCurrentUserOwner, String currentUserId) {
        this.listener = listener;
        this.isCurrentUserOwner = isCurrentUserOwner;
        this.currentUserId = currentUserId;
    }

    public void setMembers(List<TeamMember> members) {
        this.members = members;
        notifyDataSetChanged();
    }
    
    public void setCurrentUserOwner(boolean isOwner) {
        this.isCurrentUserOwner = isOwner;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TeamMember member = members.get(position);
        holder.bind(member, listener, isCurrentUserOwner, currentUserId);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberRole;
        ImageButton btnMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberRole = itemView.findViewById(R.id.tvMemberRole);
            btnMoreOptions = itemView.findViewById(R.id.btnMoreOptions);
        }

        public void bind(TeamMember member, OnMemberActionListener listener, boolean isCurrentUserOwner, String currentUserId) {
            if (member.user != null) {
                String name = member.user.first_name + " " + member.user.last_name;
                if (member.user.first_name == null) name = member.user.email;
                if (name == null) name = "User " + member.userId;
                
                tvMemberName.setText(name);
            } else {
                tvMemberName.setText("User: " + member.userId);
            }

            tvMemberRole.setText(member.role);

            if (isCurrentUserOwner && !member.userId.equals(currentUserId)) {
                 btnMoreOptions.setVisibility(View.VISIBLE);
                 btnMoreOptions.setOnClickListener(v -> {
                     PopupMenu popup = new PopupMenu(v.getContext(), v);
                     popup.getMenu().add("Make Owner");
                     popup.getMenu().add("Make Admin");
                     popup.getMenu().add("Make Member");
                     popup.getMenu().add("Make Viewer");
                     popup.getMenu().add("Remove");
                     
                     popup.setOnMenuItemClickListener(item -> {
                         String title = item.getTitle().toString();
                         if (title.equals("Remove")) {
                             listener.onRemoveMember(member);
                         } else {
                             String role = "member";
                             if (title.contains("Owner")) role = "owner";
                             else if (title.contains("Admin")) role = "admin";
                             else if (title.contains("Viewer")) role = "viewer";
                             
                             listener.onUpdateRole(member, role);
                         }
                         return true;
                     });
                     popup.show();
                 });
            } else {
                btnMoreOptions.setVisibility(View.GONE);
            }
        }
    }
}
