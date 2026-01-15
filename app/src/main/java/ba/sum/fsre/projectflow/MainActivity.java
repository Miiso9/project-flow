package ba.sum.fsre.projectflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ba.sum.fsre.projectflow.adapter.TaskAdapter;
import ba.sum.fsre.projectflow.auth.AuthActivity;
import ba.sum.fsre.projectflow.model.Task;
import ba.sum.fsre.projectflow.storage.TokenManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView tasksRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> allTasks;
    private TextView filterAll, filterOngoing, filterCompleted, filterPostponed;
    private TextView headerSubtitle;
    private String currentFilter = "all";
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);
        
        // Provjeri autentikaciju prije nego što se UI učita
        if (!checkAuthentication()) {
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        setupRecyclerView();
        setupFilters();
        setupCalendar();
        setupButtons();
        loadSampleTasks();
        updateHeader();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Provjeri autentikaciju kada aktivnost postane vidljiva
        if (!checkAuthentication()) {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Provjeri autentikaciju kada aktivnost dođe u fokus
        if (!checkAuthentication()) {
            return;
        }
    }

    /**
     * Provjerava da li je korisnik autentifikovan.
     * Ako nije, preusmjerava na AuthActivity i završava trenutnu aktivnost.
     * @return true ako je korisnik autentifikovan, false inače
     */
    private boolean checkAuthentication() {
        if (tokenManager == null) {
            tokenManager = new TokenManager(this);
        }
        
        if (tokenManager.getToken() == null) {
            // Korisnik nije autentifikovan - preusmjeri na login
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return false;
        }
        return true;
    }

    private void setupButtons() {
        Button logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(v -> {
            tokenManager.clearTokens();
            startActivity(new Intent(this, AuthActivity.class));
            finish();
        });

        Button btnManageTeams = findViewById(R.id.btnManageTeams);
        btnManageTeams.setOnClickListener(v -> {
            startActivity(new Intent(this, ba.sum.fsre.projectflow.team.TeamListActivity.class));
        });
    }

    private void initializeViews() {
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView);
        filterAll = findViewById(R.id.filterAll);
        filterOngoing = findViewById(R.id.filterOngoing);
        filterCompleted = findViewById(R.id.filterCompleted);
        filterPostponed = findViewById(R.id.filterPostponed);
        headerSubtitle = findViewById(R.id.headerSubtitle);
    }

    private void setupRecyclerView() {
        tasksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        allTasks = new ArrayList<>();
        taskAdapter = new TaskAdapter(allTasks);
        tasksRecyclerView.setAdapter(taskAdapter);
    }

    private void setupFilters() {
        filterAll.setOnClickListener(v -> applyFilter("all", filterAll));
        filterOngoing.setOnClickListener(v -> applyFilter("ongoing", filterOngoing));
        filterCompleted.setOnClickListener(v -> applyFilter("completed", filterCompleted));
        filterPostponed.setOnClickListener(v -> applyFilter("postponed", filterPostponed));
    }

    private void applyFilter(String filter, TextView selectedView) {
        currentFilter = filter;
        
        // Update filter button styles
        resetFilterStyles();
        selectedView.setBackgroundResource(R.drawable.bg_filter_selected);
        selectedView.setTextColor(getColor(R.color.white));
        
        // Filter tasks
        List<Task> filteredTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if ("all".equals(filter) || filter.equalsIgnoreCase(task.status)) {
                filteredTasks.add(task);
            }
        }
        taskAdapter.updateTasks(filteredTasks);
        updateHeader();
    }

    private void resetFilterStyles() {
        filterAll.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterAll.setTextColor(getColor(R.color.text_primary));
        filterOngoing.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterOngoing.setTextColor(getColor(R.color.text_primary));
        filterCompleted.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterCompleted.setTextColor(getColor(R.color.text_primary));
        filterPostponed.setBackgroundResource(R.drawable.bg_filter_unselected);
        filterPostponed.setTextColor(getColor(R.color.text_primary));
    }

    private void setupCalendar() {
        // Set up calendar date click listeners
        TextView date10 = findViewById(R.id.date10);
        date10.setOnClickListener(v -> selectDate(date10));
        
        // You can add more date click listeners here
    }

    private void selectDate(TextView dateView) {
        // Reset all dates
        resetCalendarStyles();
        
        // Highlight selected date
        dateView.setBackgroundResource(R.drawable.bg_calendar_selected);
        dateView.setTextColor(getColor(R.color.priority_high_text));
        dateView.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void resetCalendarStyles() {
        TextView date7 = findViewById(R.id.date7);
        TextView date8 = findViewById(R.id.date8);
        TextView date9 = findViewById(R.id.date9);
        TextView date10 = findViewById(R.id.date10);
        TextView date11 = findViewById(R.id.date11);
        TextView date12 = findViewById(R.id.date12);
        TextView date13 = findViewById(R.id.date13);
        
        date7.setBackground(null);
        date7.setTextColor(getColor(R.color.text_secondary));
        date7.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date8.setBackground(null);
        date8.setTextColor(getColor(R.color.text_secondary));
        date8.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date9.setBackground(null);
        date9.setTextColor(getColor(R.color.text_secondary));
        date9.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date10.setBackground(null);
        date10.setTextColor(getColor(R.color.text_secondary));
        date10.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date11.setBackground(null);
        date11.setTextColor(getColor(R.color.text_secondary));
        date11.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date12.setBackground(null);
        date12.setTextColor(getColor(R.color.text_secondary));
        date12.setTypeface(null, android.graphics.Typeface.NORMAL);
        
        date13.setBackground(null);
        date13.setTextColor(getColor(R.color.text_secondary));
        date13.setTypeface(null, android.graphics.Typeface.NORMAL);
    }

    private void loadSampleTasks() {
        allTasks.clear();
        
        // Sample task 1 - High Priority
        allTasks.add(new Task(
            "1",
            "Project UI/UX Design",
            "Let's start your project website with this brief: you can read first to understanding the project",
            "high",
            "12:00",
            "25 October",
            40,
            "ongoing",
            Arrays.asList("A", "B", "C")
        ));
        
        // Sample task 2 - Medium Priority
        allTasks.add(new Task(
            "2",
            "Project UI/UX Design",
            "Let's start your project website with this brief: you can read first to understanding the project",
            "medium",
            "12:00",
            "25 October",
            60,
            "ongoing",
            Arrays.asList("D", "E", "F")
        ));
        
        // Sample task 3 - High Priority Completed
        allTasks.add(new Task(
            "3",
            "Website Development",
            "Complete the frontend development for the main dashboard",
            "high",
            "14:30",
            "20 October",
            100,
            "completed",
            Arrays.asList("A", "B")
        ));
        
        // Sample task 4 - Low Priority
        allTasks.add(new Task(
            "4",
            "Documentation Review",
            "Review and update project documentation",
            "low",
            "10:00",
            "28 October",
            25,
            "ongoing",
            Arrays.asList("C")
        ));
        
        // Sample task 5 - Medium Priority Postponed
        allTasks.add(new Task(
            "5",
            "Team Meeting",
            "Schedule and conduct team meeting for project updates",
            "medium",
            "15:00",
            "22 October",
            0,
            "postponed",
            Arrays.asList("A", "B", "C", "D")
        ));
        
        applyFilter(currentFilter, filterAll);
    }

    private void updateHeader() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        String date = dateFormat.format(calendar.getTime());
        
        int taskCount = taskAdapter.getItemCount();
        headerSubtitle.setText(date + " (" + taskCount + " Task" + (taskCount != 1 ? "s" : "") + ")");
    }
}
