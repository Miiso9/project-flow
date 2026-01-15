package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class TeamMember implements Serializable {
    public String id;

    @SerializedName("team_id")
    public String teamId;

    @SerializedName("user_id")
    public String userId;

    public String role;

    @SerializedName("joined_at")
    public String joinedAt;

    @SerializedName("users") 
    public User user;
    
    public Team teams;


    public TeamMember() {}

    public TeamMember(String teamId, String userId, String role) {
        this.teamId = teamId;
        this.userId = userId;
        this.role = role;
    }
}
