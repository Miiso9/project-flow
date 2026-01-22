package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Comment implements Serializable {
    public String id;

    @SerializedName("task_id")
    public String taskId;

    @SerializedName("user_id")
    public String userId;

    public String comment;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("user")
    public User user;

    public Comment() {}

    public Comment(String taskId, String userId, String comment) {
        this.taskId = taskId;
        this.userId = userId;
        this.comment = comment;
    }
}