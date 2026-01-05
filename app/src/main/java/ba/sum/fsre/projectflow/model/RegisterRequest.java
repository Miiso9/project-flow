package ba.sum.fsre.projectflow.model;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest {

    private String email;
    private String password;

    @SerializedName("data")
    private Map<String, String> userData;

    public RegisterRequest(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.userData = new HashMap<>();
        this.userData.put("first_name", firstName);
        this.userData.put("last_name", lastName);
    }
}
