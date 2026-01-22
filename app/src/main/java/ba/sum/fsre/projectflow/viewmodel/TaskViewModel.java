package ba.sum.fsre.projectflow.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ba.sum.fsre.projectflow.model.Comment;
import ba.sum.fsre.projectflow.model.Document;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.repository.CommentRepository;
import ba.sum.fsre.projectflow.repository.DocumentRepository;
import ba.sum.fsre.projectflow.repository.TaskRepository;
import ba.sum.fsre.projectflow.storage.TokenManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;

public class TaskViewModel extends AndroidViewModel {
    private TaskRepository repository;
    private CommentRepository commentRepository;
    private SupabaseApi api;

    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private MutableLiveData<Task> taskDetails = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private MutableLiveData<List<Comment>> commentsList = new MutableLiveData<>();
    private DocumentRepository documentRepository;
    private MutableLiveData<List<Document>> documentsList = new MutableLiveData<>();

    public TaskViewModel(Application application) {
        super(application);
        TokenManager tokenManager = new TokenManager(application);
        api = RetrofitClient.getClient(application).create(SupabaseApi.class);
        repository = new TaskRepository(api);
        commentRepository = new CommentRepository(api);
        documentRepository = new DocumentRepository(api, application);
    }

    public LiveData<List<Task>> getTasks() { return tasks; }
    public LiveData<Task> getTaskDetails() { return taskDetails; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<List<Comment>> getComments() { return commentsList; }
    public LiveData<List<Document>> getDocuments() { return documentsList; }

    public void loadTasks(String projectId) {
        loading.setValue(true);
        repository.getTasksByProject(projectId, new TaskRepository.DataCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> data) {
                loading.setValue(false);
                tasks.setValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void loadMyTasks(String userId) {
        loading.setValue(true);
        repository.getMyTasks(userId, new TaskRepository.DataCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> data) {
                loading.setValue(false);
                tasks.setValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void loadTaskById(String taskId) {
        loading.setValue(true);
        repository.getTaskById(taskId, new TaskRepository.DataCallback<Task>() {
            @Override
            public void onSuccess(Task data) {
                loading.setValue(false);
                taskDetails.setValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void createTask(String projectId, String title, String description,
                           String assignedTo, String status, String priority,
                           String dueDate, Double estimatedHours,
                           String startTime, String endTime) {
        loading.setValue(true);

        Task task = new Task(projectId, title, description, assignedTo,
                status, priority, dueDate, estimatedHours,
                startTime, endTime);

        repository.createTask(task, new TaskRepository.DataCallback<Task>() {
            @Override
            public void onSuccess(Task data) {
                loading.setValue(false);
                navigateBack.setValue(true);
                navigateBack.setValue(null);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void updateTask(String taskId, String title, String description,
                           String assignedTo, String status, String priority,
                           String dueDate, Double estimatedHours,
                           String startTime, String endTime) {
        loading.setValue(true);

        Task task = new Task();
        task.title = title;
        task.description = description;
        task.assignedTo = assignedTo;
        task.status = status;
        task.priority = priority;
        task.dueDate = dueDate;
        task.estimatedHours = estimatedHours;
        task.startTime = startTime;
        task.endTime = endTime;

        repository.updateTask(taskId, task, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                navigateBack.setValue(true);
                navigateBack.setValue(null);
                operationSuccess.setValue(true);
                operationSuccess.setValue(null);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void deleteTask(String taskId) {
        loading.setValue(true);
        repository.deleteTask(taskId, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
                operationSuccess.setValue(null);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void updateTaskStatus(String taskId, String status) {
        loading.setValue(true);

        Task task = new Task();
        task.status = status;

        repository.updateTask(taskId, task, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
                operationSuccess.setValue(null);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }


    public void loadComments(String taskId) {
        commentRepository.getComments(taskId, new CommentRepository.DataCallback<List<Comment>>() {
            @Override
            public void onSuccess(List<Comment> data) {
                commentsList.setValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                error.setValue(errorMsg);
            }
        });
    }

    public void addComment(Comment comment) {
        loading.setValue(true);
        commentRepository.createComment(comment, new CommentRepository.DataCallback<Comment>() {
            @Override
            public void onSuccess(Comment data) {
                loading.setValue(false);
                loadComments(comment.taskId);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }

    public void deleteComment(String commentId, String taskId) {
        loading.setValue(true);
        api.deleteComment("eq." + commentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                loading.setValue(false);
                if (response.isSuccessful()) {
                    loadComments(taskId);
                } else {
                    error.setValue("Failed to delete comment");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void loadDocuments(String taskId) {
        documentRepository.getDocuments(taskId, new DocumentRepository.DocumentCallback<List<Document>>() {
            @Override
            public void onSuccess(List<Document> data) {
                documentsList.setValue(data);
            }
            @Override
            public void onError(String errorMsg) {
                error.setValue(errorMsg);
            }
        });
    }

    public void uploadDocument(Uri fileUri, String taskId, String projectId, String userId) {
        loading.setValue(true);
        documentRepository.uploadDocument(fileUri, taskId, projectId, userId, new DocumentRepository.DocumentCallback<Document>() {
            @Override
            public void onSuccess(Document data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
                loadDocuments(taskId);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue("Upload failed: " + errorMsg);
            }
        });
    }

    public void deleteDocument(Document document) {
        loading.setValue(true);
        documentRepository.deleteDocument(document, new DocumentRepository.DocumentCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.setValue(false);
                operationSuccess.setValue(true);
                loadDocuments(document.taskId);
            }

            @Override
            public void onError(String errorMsg) {
                loading.setValue(false);
                error.setValue(errorMsg);
            }
        });
    }
}