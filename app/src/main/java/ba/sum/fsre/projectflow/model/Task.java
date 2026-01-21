package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Task implements Serializable {
    public String id;

    @SerializedName("project_id")
    public String projectId;

    public String title;
    public String description;

    @SerializedName("assigned_to")
    public String assignedTo;

    public String status;
    public String priority;

    @SerializedName("due_date")
    public String dueDate;

    @SerializedName("estimated_hours")
    public Double estimatedHours;

    @SerializedName("start_time")
    public String startTime;

    @SerializedName("end_time")
    public String endTime;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public User assignedUser;

    public Task() {}

    public Task(String projectId, String title, String description, String assignedTo,
                String status, String priority, String dueDate, Double estimatedHours,
                String startTime, String endTime) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}