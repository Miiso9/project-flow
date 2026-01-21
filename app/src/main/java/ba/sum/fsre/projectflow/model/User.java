package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class User implements Serializable {
    public String id;
    public String email;

    @SerializedName("first_name")
    public String first_name;

    @SerializedName("last_name")
    public String last_name;

    public String phone;
    public String gender;
    public String role;
    public String city;

    @SerializedName("date_of_birth")
    public String date_of_birth;

    @SerializedName("profile_completed")
    public Boolean profile_completed;
}