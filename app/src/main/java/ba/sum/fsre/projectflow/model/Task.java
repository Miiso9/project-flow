package ba.sum.fsre.projectflow.model;

import java.util.List;

public class Task {
    public String id;
    public String title;
    public String description;
    public String priority; // "high", "medium", "low"
    public String deadlineTime;
    public String deadlineDate;
    public int progress;
    public String status; // "all", "ongoing", "completed", "postponed"
    public List<String> tags; // List of tag letters like "A", "B", "C"

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
}
