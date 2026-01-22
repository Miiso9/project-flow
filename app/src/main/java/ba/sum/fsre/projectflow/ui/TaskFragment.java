package ba.sum.fsre.projectflow.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.adapter.TaskAdapter;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.task.CreateTaskActivity;
import ba.sum.fsre.projectflow.task.TaskDetailActivity;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;

public class TaskFragment extends Fragment {

    private TaskViewModel viewModel;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private TextView emptyState;
    private TextView titleTextView;

    private TextView filterAll, filterTodo, filterInProgress, filterDone, filterBlocked;

    private List<Task> allTasks = new ArrayList<>();
    private String projectId;
    private String projectName;
    private String currentFilter = "all";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            projectId = getArguments().getString("projectId");
            projectName = getArguments().getString("projectName");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tasks, container, false);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        initializeViews(view);
        setupRecyclerView();
        setupFilters(view);
        setupFAB(view);
        observeViewModel();

        if (projectId != null) {
            loadTasks();
        }

        return view;
    }

    private void initializeViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
        titleTextView = view.findViewById(R.id.title);

        filterAll = view.findViewById(R.id.filterAll);
        filterTodo = view.findViewById(R.id.filterTodo);
        filterInProgress = view.findViewById(R.id.filterInProgress);
        filterDone = view.findViewById(R.id.filterDone);
        filterBlocked = view.findViewById(R.id.filterBlocked);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) requireActivity();

        activity.setSupportActionBar(toolbar);
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        if (projectName != null) {
            titleTextView.setText(projectName + " Tasks");
            toolbar.setTitle(projectName);
        }
    }

    private void setupFilters(View view) {
        filterAll.setOnClickListener(v -> applyFilter("all", filterAll));
        filterTodo.setOnClickListener(v -> applyFilter("todo", filterTodo));
        filterInProgress.setOnClickListener(v -> applyFilter("in_progress", filterInProgress));
        filterDone.setOnClickListener(v -> applyFilter("done", filterDone));
        filterBlocked.setOnClickListener(v -> applyFilter("blocked", filterBlocked));
    }

    private void applyFilter(String filter, TextView selectedView) {
        currentFilter = filter;

        resetFilterStyles();

        selectedView.setBackgroundResource(R.drawable.bg_filter_selected);
        selectedView.setTextColor(requireContext().getColor(R.color.white));

        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            String status = task.status != null ? task.status.toLowerCase() : "todo";
            if ("all".equals(filter) || filter.equals(status)) {
                filteredTasks.add(task);
            }
        }

        taskAdapter.updateTasks(filteredTasks);

        if (filteredTasks.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            tasksRecyclerView.setVisibility(View.GONE);
            emptyState.setText(allTasks.isEmpty() ? "No tasks in project" : "No tasks match filter");
        } else {
            emptyState.setVisibility(View.GONE);
            tasksRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void resetFilterStyles() {
        TextView[] filters = {filterAll, filterTodo, filterInProgress, filterDone, filterBlocked};
        for (TextView view : filters) {
            view.setBackgroundResource(R.drawable.bg_filter_unselected);
            view.setTextColor(requireContext().getColor(R.color.text_primary));
        }
    }

    private void setupRecyclerView() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
                intent.putExtra("task", task);
                startActivity(intent);
            }

            @Override
            public void onTaskLongClick(Task task) {
                showTaskOptionsDialog(task);
            }
        });
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupFAB(View view) {
        FloatingActionButton fabAddTask = view.findViewById(R.id.fabAddTask);
        if (fabAddTask != null) {
            fabAddTask.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), CreateTaskActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("projectName", projectName);
                startActivity(intent);
            });
        }
    }

    private void showTaskOptionsDialog(Task task) {
        String[] options = {"Mark as Todo", "Mark as In Progress", "Mark as Done", "Mark as Blocked", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle(task.title)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: viewModel.updateTaskStatus(task.id, "todo"); break;
                        case 1: viewModel.updateTaskStatus(task.id, "in_progress"); break;
                        case 2: viewModel.updateTaskStatus(task.id, "done"); break;
                        case 3: viewModel.updateTaskStatus(task.id, "blocked"); break;
                        case 4: confirmDeleteTask(task); break;
                    }
                })
                .show();
    }

    private void confirmDeleteTask(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteTask(task.id))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadTasks() {
        viewModel.loadTasks(projectId);
    }

    private void observeViewModel() {
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            TextView currentView = filterAll;
            switch(currentFilter) {
                case "todo": currentView = filterTodo; break;
                case "in_progress": currentView = filterInProgress; break;
                case "done": currentView = filterDone; break;
                case "blocked": currentView = filterBlocked; break;
            }
            applyFilter(currentFilter, currentView);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null && isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
                loadTasks();
            }
        });
    }

    public static TaskFragment newInstance(String projectId, String projectName) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putString("projectId", projectId);
        args.putString("projectName", projectName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (projectId != null) {
            loadTasks();
        }
    }
}