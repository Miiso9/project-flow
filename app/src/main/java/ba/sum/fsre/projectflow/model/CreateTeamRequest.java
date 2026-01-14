package ba.sum.fsre.projectflow.model;

public class CreateTeamRequest {
    public String name;
    public String description;

    public CreateTeamRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
