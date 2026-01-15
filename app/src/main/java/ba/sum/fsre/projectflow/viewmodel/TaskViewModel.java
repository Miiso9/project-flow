package ba.sum.fsre.projectflow.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.repository.TaskRepository;
import ba.sum.fsre.projectflow.storage.TokenManager;

public class TaskViewModel extends ViewModel {

    private MutableLiveData<List<Task>> tasks = new MutableLiveData<>();
    private MutableLiveData<Task> selectedTask = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    private TaskRepository repository;

    public LiveData<List<Task>> getTasks() {
        return tasks;
    }

    public LiveData<Task> getSelectedTask() {
        return selectedTask;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getOperationSuccess() {
        return operationSuccess;
    }

    public LiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    private void initRepository(Context context) {
        if (repository == null) {
            SupabaseApi api = RetrofitClient.getClient(context).create(SupabaseApi.class);
            repository = new TaskRepository(api);
        }
    }

    public void loadMyTasks(Context context) {
        initRepository(context);
        loading.setValue(true);

        String userId = new TokenManager(context).getUserId();
        if (userId == null) {
            loading.setValue(false);
            error.setValue("User not logged in");
            return;
        }

        repository.getTasksAssignedToUser(userId, new TaskRepository.DataCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> data) {
                loading.postValue(false);
                tasks.postValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void loadTasksForProject(Context context, String projectId) {
        initRepository(context);
        loading.setValue(true);

        repository.getTasksForProject(projectId, new TaskRepository.DataCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> data) {
                loading.postValue(false);
                tasks.postValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void createTask(Context context, String projectId, String title, String description,
                           String priority, String dueDate, Double estimatedHours) {
        initRepository(context);
        loading.setValue(true);

        String userId = new TokenManager(context).getUserId();
        Task task = new Task(projectId, title, description, userId, "pending", priority, dueDate, estimatedHours);

        repository.createTask(task, new TaskRepository.DataCallback<Task>() {
            @Override
            public void onSuccess(Task data) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                navigateBack.postValue(true);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void updateTaskStatus(Context context, String taskId, String newStatus) {
        initRepository(context);
        loading.setValue(true);

        Task task = new Task();
        task.status = newStatus;

        repository.updateTask(taskId, task, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadMyTasks(context);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void updateTask(Context context, String taskId, String title, String description,
                           String priority, String status, String dueDate) {
        initRepository(context);
        loading.setValue(true);

        Task task = new Task();
        task.title = title;
        task.description = description;
        task.priority = priority;
        task.status = status;
        task.dueDate = dueDate;

        repository.updateTask(taskId, task, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                navigateBack.postValue(true);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void deleteTask(Context context, String taskId) {
        initRepository(context);
        loading.setValue(true);

        repository.deleteTask(taskId, new TaskRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadMyTasks(context);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
}
