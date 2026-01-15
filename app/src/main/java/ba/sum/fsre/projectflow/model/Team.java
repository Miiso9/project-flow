package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Team implements Serializable {
    public String id;
    public String name;
    public String description;

    @SerializedName("created_at")
    public String createdAt;

    @SerializedName("updated_at")
    public String updatedAt;

    public Team() {}

    public Team(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
