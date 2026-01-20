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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.adapter.TaskAdapter;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.task.TaskDetailActivity;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;

public class HomeFragment extends Fragment {

    private TaskViewModel viewModel;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private List<Task> allTasks = new ArrayList<>();
    private TextView filterAll, filterOngoing, filterCompleted, filterPostponed;
    private TextView headerSubtitle;
    private String currentFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        initializeViews(view);
        setupRecyclerView();
        setupFilters();
        setupCalendar(view);
        observeViewModel();

        return view;
    }

    private void initializeViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        filterAll = view.findViewById(R.id.filterAll);
        filterOngoing = view.findViewById(R.id.filterOngoing);
        filterCompleted = view.findViewById(R.id.filterCompleted);
        filterPostponed = view.findViewById(R.id.filterPostponed);
        headerSubtitle = view.findViewById(R.id.headerSubtitle);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);
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

    private void showTaskOptionsDialog(Task task) {
        String[] options = {"Mark as Completed", "Mark as In Progress", "Mark as Postponed", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle(task.title)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            viewModel.updateTaskStatus(requireContext(), task.id, "completed");
                            break;
                        case 1:
                            viewModel.updateTaskStatus(requireContext(), task.id, "in_progress");
                            break;
                        case 2:
                            viewModel.updateTaskStatus(requireContext(), task.id, "postponed");
                            break;
                        case 3:
                            confirmDeleteTask(task);
                            break;
                    }
                })
                .show();
    }

    private void confirmDeleteTask(Task task) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete \"" + task.title + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteTask(requireContext(), task.id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> applyFilter("all", filterAll));
        filterOngoing.setOnClickListener(v -> applyFilter("in_progress", filterOngoing));
        filterCompleted.setOnClickListener(v -> applyFilter("completed", filterCompleted));
        filterPostponed.setOnClickListener(v -> applyFilter("postponed", filterPostponed));
    }

    private void applyFilter(String filter, TextView selectedView) {
        currentFilter = filter;

        resetFilterStyles();
        selectedView.setBackgroundResource(R.drawable.bg_filter_selected);
        selectedView.setTextColor(requireContext().getColor(R.color.white));

        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            String status = task.status != null ? task.status.toLowerCase() : "pending";
            if ("all".equals(filter)) {
                filteredTasks.add(task);
            } else if (filter.equals(status)) {
                filteredTasks.add(task);
            } else if ("in_progress".equals(filter) && "pending".equals(status)) {
                filteredTasks.add(task);
            }
        }
        taskAdapter.updateTasks(filteredTasks);
        updateHeader(filteredTasks.size());
        
        if (emptyState != null) {
            emptyState.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
            tasksRecyclerView.setVisibility(filteredTasks.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void resetFilterStyles() {
        filterAll.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterAll.setTextColor(requireContext().getColor(R.color.text_primary));
        filterOngoing.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterOngoing.setTextColor(requireContext().getColor(R.color.text_primary));
        filterCompleted.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterCompleted.setTextColor(requireContext().getColor(R.color.text_primary));
        filterPostponed.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterPostponed.setTextColor(requireContext().getColor(R.color.text_primary));
    }

    private void setupCalendar(View view) {
        TextView date10 = view.findViewById(R.id.date10);
        date10.setOnClickListener(v -> selectDate(view, date10));
    }

    private void selectDate(View root, TextView dateView) {
        resetCalendarStyles(root);
        dateView.setBackgroundResource(R.drawable.bg_calendar_selected);
        dateView.setTextColor(requireContext().getColor(R.color.priority_high_text));
        dateView.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetCalendarStyles(View root) {
        int[] dateIds = {R.id.date7, R.id.date8, R.id.date9, R.id.date10, R.id.date11, R.id.date12, R.id.date13};
        for (int id : dateIds) {
            TextView dateView = root.findViewById(id);
            dateView.setBackground(null);
            dateView.setTextColor(requireContext().getColor(R.color.text_secondary));
            dateView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    private void observeViewModel() {
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            applyFilter(currentFilter, getSelectedFilterView());
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null && isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(getContext(), "Task updated", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private TextView getSelectedFilterView() {
        switch (currentFilter) {
            case "in_progress":
                return filterOngoing;
            case "completed":
                return filterCompleted;
            case "postponed":
                return filterPostponed;
            default:
                return filterAll;
        }
    }

    private void updateHeader(int taskCount) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());

        headerSubtitle.setText(date + " (" + taskCount + " Task" + (taskCount != 1 ? "s" : "") + ")");
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadMyTasks(requireContext());
    }
}
