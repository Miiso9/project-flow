package ba.sum.fsre.projectflow.repository;

import java.util.List;

import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProjectRepository {
    private SupabaseApi api;

    public ProjectRepository(SupabaseApi api) {
        this.api = api;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getProjects(String teamId, DataCallback<List<Project>> callback) {
        api.getProjects("eq." + teamId, "*", "created_at.desc").enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to fetch projects: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to fetch projects: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void createProject(Project project, DataCallback<Project> callback) {
        api.createProject(project, "return=representation").enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to create project: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to create project: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateProject(String projectId, Project project, DataCallback<Void> callback) {
        api.updateProject("eq." + projectId, project).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to update project: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to update project: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteProject(String projectId, DataCallback<Void> callback) {
        api.deleteProject("eq." + projectId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to delete project: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to delete project: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void getProjectById(String projectId, DataCallback<Project> callback) {
        api.getProjectById("eq." + projectId, "*").enqueue(new Callback<List<Project>>() {
            @Override
            public void onResponse(Call<List<Project>> call, Response<List<Project>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Project not found");
                }
            }

            @Override
            public void onFailure(Call<List<Project>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}
