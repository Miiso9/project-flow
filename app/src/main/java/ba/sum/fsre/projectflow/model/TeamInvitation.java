package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TeamInvitation implements Serializable {
    public String id;

    @SerializedName("team_id")
    public String teamId;

    @SerializedName("invited_email")
    public String invitedEmail;

    @SerializedName("invited_by")
    public String invitedBy;

    public String status;
    public String token;

    @SerializedName("created_at")
    public String createdAt;

    public Team teams;

    @SerializedName("users")
    public ba.sum.fsre.projectflow.model.User invitedByUser;

    public String role;

    public TeamInvitation() {}

    public TeamInvitation(String teamId, String invitedEmail, String invitedBy, String status, String role) {
        this.teamId = teamId;
        this.invitedEmail = invitedEmail;
        this.invitedBy = invitedBy;
        this.status = status;
        this.role = role;
        this.token = java.util.UUID.randomUUID().toString();
    }
}
