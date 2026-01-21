package ba.sum.fsre.projectflow.repository;

import java.util.List;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {
    private SupabaseApi api;

    public TaskRepository(SupabaseApi api) {
        this.api = api;
    }

    public interface DataCallback<T> {
        void onSuccess(T data);
        void onError(String error);
    }

    public void getTasksByProject(String projectId, DataCallback<List<Task>> callback) {
        String select = "*,assignedUser:users!tasks_assigned_to_fkey(*)";
        api.getTasksByProject("eq." + projectId, select, "created_at.desc")
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response.body());
                        } else {
                            try {
                                String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                                callback.onError("Failed to fetch tasks: " + response.code() + " - " + errorBody);
                            } catch (Exception e) {
                                callback.onError("Failed to fetch tasks: " + response.code());
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void getMyTasks(String userId, DataCallback<List<Task>> callback) {
        String select = "*,project:projects!tasks_project_id_fkey(name),assignedUser:users!tasks_assigned_to_fkey(*)";
        api.getMyTasks("eq." + userId, select, "due_date.asc,priority.desc")
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful()) {
                            callback.onSuccess(response.body());
                        } else {
                            callback.onError("Failed to fetch your tasks");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void getTaskById(String taskId, DataCallback<Task> callback) {
        String select = "*,assignedUser:users!tasks_assigned_to_fkey(*)";
        api.getTaskById("eq." + taskId, select)
                .enqueue(new Callback<List<Task>>() {
                    @Override
                    public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                        if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                            callback.onSuccess(response.body().get(0));
                        } else {
                            callback.onError("Task not found");
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Task>> call, Throwable t) {
                        callback.onError("Network error: " + t.getMessage());
                    }
                });
    }

    public void createTask(Task task, DataCallback<Task> callback) {
        api.createTask(task, "return=representation").enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to create task: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to create task: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void updateTask(String taskId, Task task, DataCallback<Void> callback) {
        api.updateTask("eq." + taskId, task).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to update task: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to update task: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    public void deleteTask(String taskId, DataCallback<Void> callback) {
        api.deleteTask("eq." + taskId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onError("Failed to delete task: " + response.code() + " - " + errorBody);
                    } catch (Exception e) {
                        callback.onError("Failed to delete task: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }
}