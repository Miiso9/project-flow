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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.adapter.TaskAdapter;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.task.TaskDetailActivity;
import ba.sum.fsre.projectflow.viewmodel.TaskViewModel;
import ba.sum.fsre.projectflow.storage.TokenManager;

public class HomeFragment extends Fragment {

    private TaskViewModel viewModel;
    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private List<Task> allTasks = new ArrayList<>();
    private TextView headerSubtitle;
    private TextView[] dateViews = new TextView[7];
    private Calendar currentWeek = Calendar.getInstance();
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        viewModel = new ViewModelProvider(this).get(TaskViewModel.class);

        // Get current user ID from TokenManager
        TokenManager tokenManager = new TokenManager(requireContext());
        currentUserId = tokenManager.getUserId();

        initializeViews(view);
        setupRecyclerView();
        setupCalendar(view);
        observeViewModel();
        loadMyTasks();

        return view;
    }

    private void initializeViews(View view) {
        tasksRecyclerView = view.findViewById(R.id.tasksRecyclerView);
        headerSubtitle = view.findViewById(R.id.headerSubtitle);
        progressBar = view.findViewById(R.id.progressBar);
        emptyState = view.findViewById(R.id.emptyState);

        // Initialize date views
        dateViews[0] = view.findViewById(R.id.date7);
        dateViews[1] = view.findViewById(R.id.date8);
        dateViews[2] = view.findViewById(R.id.date9);
        dateViews[3] = view.findViewById(R.id.date10);
        dateViews[4] = view.findViewById(R.id.date11);
        dateViews[5] = view.findViewById(R.id.date12);
        dateViews[6] = view.findViewById(R.id.date13);
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
        String[] options = {"Mark as Todo", "Mark as In Progress", "Mark as Done", "Mark as Blocked", "Delete"};

        new AlertDialog.Builder(requireContext())
                .setTitle(task.title)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            viewModel.updateTaskStatus(task.id, "todo");
                            break;
                        case 1:
                            viewModel.updateTaskStatus(task.id, "in_progress");
                            break;
                        case 2:
                            viewModel.updateTaskStatus(task.id, "done");
                            break;
                        case 3:
                            viewModel.updateTaskStatus(task.id, "blocked");
                            break;
                        case 4:
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
                    viewModel.deleteTask(task.id);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupCalendar(View view) {
        updateCalendarDisplay();

        for (int i = 0; i < dateViews.length; i++) {
            final int index = i;
            dateViews[i].setOnClickListener(v -> {
                selectDate(index);
                loadTasksForSelectedDate();
            });
        }

        // Select today by default
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < dateViews.length; i++) {
            Calendar day = (Calendar) currentWeek.clone();
            day.add(Calendar.DATE, i - 3); // Center today
            if (isSameDay(day, today)) {
                selectDate(i);
                break;
            }
        }
    }

    private void updateCalendarDisplay() {
        SimpleDateFormat dayFormat = new SimpleDateFormat("d", Locale.getDefault());
        SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());

        Calendar today = Calendar.getInstance();

        for (int i = 0; i < dateViews.length; i++) {
            Calendar day = (Calendar) currentWeek.clone();
            day.add(Calendar.DATE, i - 3); // Center the week

            String dayNumber = dayFormat.format(day.getTime());
            String dayName = dayNameFormat.format(day.getTime());
            dateViews[i].setText(dayNumber + "\n" + dayName);

            // Reset styles
            dateViews[i].setBackground(null);
            dateViews[i].setTextColor(getResources().getColor(R.color.text_secondary));
            dateViews[i].setTypeface(null, android.graphics.Typeface.NORMAL);

            // Highlight today
            if (isSameDay(day, today)) {
                dateViews[i].setTextColor(getResources().getColor(R.color.colorPrimary));
                dateViews[i].setTypeface(null, android.graphics.Typeface.BOLD);
            }
        }
    }

    private void selectDate(int index) {
        for (TextView dateView : dateViews) {
            dateView.setBackground(null);
            dateView.setTextColor(getResources().getColor(R.color.text_secondary));
            dateView.setTypeface(null, android.graphics.Typeface.NORMAL);
        }

        dateViews[index].setBackgroundResource(R.drawable.bg_calendar_selected);
        dateViews[index].setTextColor(getResources().getColor(R.color.colorPrimary));
        dateViews[index].setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void loadMyTasks() {
        if (currentUserId != null) {
            viewModel.loadMyTasks(currentUserId);
        } else {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadTasksForSelectedDate() {
        // Find selected date
        int selectedIndex = -1;
        for (int i = 0; i < dateViews.length; i++) {
            if (dateViews[i].getBackground() != null) {
                selectedIndex = i;
                break;
            }
        }

        if (selectedIndex == -1) return;

        Calendar selectedDate = (Calendar) currentWeek.clone();
        selectedDate.add(Calendar.DATE, selectedIndex - 3);

        // Filter tasks for the selected date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String selectedDateStr = dateFormat.format(selectedDate.getTime());

        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (task.dueDate != null && task.dueDate.equals(selectedDateStr)) {
                filteredTasks.add(task);
            }
        }

        taskAdapter.updateTasks(filteredTasks);
        updateHeader(filteredTasks.size(), selectedDate);

        if (emptyState != null) {
            emptyState.setVisibility(filteredTasks.isEmpty() ? View.VISIBLE : View.GONE);
            tasksRecyclerView.setVisibility(filteredTasks.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private void observeViewModel() {
        viewModel.getTasks().observe(getViewLifecycleOwner(), tasks -> {
            allTasks = tasks != null ? tasks : new ArrayList<>();
            loadTasksForSelectedDate();
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
                loadMyTasks(); // Refresh tasks
            }
        });
    }

    private void updateHeader(int taskCount, Calendar date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String dateStr = dateFormat.format(date.getTime());

        headerSubtitle.setText(dateStr + " (" + taskCount + " Task" + (taskCount != 1 ? "s" : "") + ")");
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyTasks();
    }
}