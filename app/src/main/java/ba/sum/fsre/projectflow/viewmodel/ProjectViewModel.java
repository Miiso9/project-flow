package ba.sum.fsre.projectflow.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ba.sum.fsre.projectflow.model.Project;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.repository.ProjectRepository;

public class ProjectViewModel extends ViewModel {

    private MutableLiveData<List<Project>> projects = new MutableLiveData<>();
    private MutableLiveData<Boolean> loading = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    private ProjectRepository repository;

    public LiveData<List<Project>> getProjects() {
        return projects;
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
            repository = new ProjectRepository(api);
        }
    }

    public void loadProjects(Context context, String teamId) {
        initRepository(context);
        loading.setValue(true);
        
        repository.getProjects(teamId, new ProjectRepository.DataCallback<List<Project>>() {
            @Override
            public void onSuccess(List<Project> data) {
                loading.postValue(false);
                projects.postValue(data);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void createProject(Context context, String teamId, String name, String description, 
                              String startDate, String endDate, String status) {
        initRepository(context);
        loading.setValue(true);

        Project project = new Project(teamId, name, description, startDate, endDate, status);
        
        repository.createProject(project, new ProjectRepository.DataCallback<Project>() {
            @Override
            public void onSuccess(Project data) {
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

    public void updateProject(Context context, String projectId, String name, String description,
                              String startDate, String endDate, String status) {
        initRepository(context);
        loading.setValue(true);

        Project project = new Project();
        project.name = name;
        project.description = description;
        project.startDate = startDate;
        project.endDate = endDate;
        project.status = status;

        repository.updateProject(projectId, project, new ProjectRepository.DataCallback<Void>() {
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

    public void deleteProject(Context context, String teamId, String projectId) {
        initRepository(context);
        loading.setValue(true);

        repository.deleteProject(projectId, new ProjectRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadProjects(context, teamId);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }
}
