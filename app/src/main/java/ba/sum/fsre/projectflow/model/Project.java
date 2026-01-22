package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Project implements Serializable {
    public String id;
    
    @SerializedName("team_id")
    public String teamId;
    
    public String name;
    public String description;
    
    @SerializedName("start_date")
    public String startDate;
    
    @SerializedName("end_date")
    public String endDate;
    
    public String status;
    
    @SerializedName("created_at")
    public String createdAt;
    
    @SerializedName("updated_at")
    public String updatedAt;

    public String teamName;

    public Project() {}

    public Project(String teamId, String name, String description, String startDate, String endDate, String status) {
        this.teamId = teamId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }
}
