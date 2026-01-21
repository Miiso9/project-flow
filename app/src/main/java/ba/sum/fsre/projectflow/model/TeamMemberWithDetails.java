package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;

public class TeamMemberWithDetails {
    @SerializedName("id")
    public String id;

    @SerializedName("team_id")
    public String teamId;

    @SerializedName("user_id")
    public String userId;

    @SerializedName("role")
    public String role;

    @SerializedName("joined_at")
    public String joinedAt;

    @SerializedName("user_data_id")
    public String userDataId;

    @SerializedName("email")
    public String email;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("phone")
    public String phone;

    @SerializedName("gender")
    public String gender;

    @SerializedName("user_role")
    public String userRole;

    @SerializedName("city")
    public String city;

    @SerializedName("date_of_birth")
    public String dateOfBirth;

    @SerializedName("profile_completed")
    public Boolean profileCompleted;

    @SerializedName("team_name")
    public String teamName;

    @SerializedName("team_description")
    public String teamDescription;

    public TeamMemberWithDetails() {}

    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        } else {
            return email != null ? email : "Unknown";
        }
    }
}