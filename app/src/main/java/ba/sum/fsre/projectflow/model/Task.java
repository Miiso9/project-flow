package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

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

    public transient String deadlineTime;
    public transient String deadlineDate;
    public transient int progress;
    public transient List<String> tags;
    
    public Project projects;

    public Task() {}

    public Task(String projectId, String title, String description, String assignedTo,
                String status, String priority, String dueDate, Double estimatedHours) {
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.status = status;
        this.priority = priority;
        this.dueDate = dueDate;
        this.estimatedHours = estimatedHours;
    }

    public Task(String id, String title, String description, String priority, 
                String deadlineTime, String deadlineDate, int progress, 
                String status, List<String> tags) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.deadlineTime = deadlineTime;
        this.deadlineDate = deadlineDate;
        this.progress = progress;
        this.status = status;
        this.tags = tags;
    }
    
    public int getProgressValue() {
        if (progress > 0) return progress;
        if ("completed".equalsIgnoreCase(status)) return 100;
        if ("in_progress".equalsIgnoreCase(status)) return 50;
        if ("pending".equalsIgnoreCase(status)) return 0;
        return 0;
    }
    
    public String getDisplayDate() {
        if (deadlineDate != null) return deadlineDate;
        if (dueDate != null) {
            try {
                String[] parts = dueDate.split("-");
                if (parts.length == 3) {
                    String[] months = {"January", "February", "March", "April", "May", "June",
                            "July", "August", "September", "October", "November", "December"};
                    int monthIndex = Integer.parseInt(parts[1]) - 1;
                    return parts[2] + " " + months[monthIndex];
                }
            } catch (Exception e) {
                return dueDate;
            }
        }
        return "-";
    }
    
    public String getDisplayTime() {
        if (deadlineTime != null) return deadlineTime;
        if (startTime != null) {
            try {
                return startTime.substring(11, 16);
            } catch (Exception e) {
                return "-";
            }
        }
        return "-";
    }
}
