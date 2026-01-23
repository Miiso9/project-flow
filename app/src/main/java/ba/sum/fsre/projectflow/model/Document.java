package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Document implements Serializable {
    public String id;

    @SerializedName("task_id")
    public String taskId;

    @SerializedName("project_id")
    public String projectId;

    @SerializedName("uploaded_by")
    public String uploadedBy;

    @SerializedName("file_url")
    public String fileUrl;

    @SerializedName("file_name")
    public String fileName;

    @SerializedName("uploaded_at")
    public String uploadedAt;

    @SerializedName("uploader")
    public User uploader;

    public Document() {}

    public Document(String taskId, String projectId, String uploadedBy, String fileUrl, String fileName) {
        this.taskId = taskId;
        this.projectId = projectId;
        this.uploadedBy = uploadedBy;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }
}