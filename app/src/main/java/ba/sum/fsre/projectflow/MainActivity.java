package ba.sum.fsre.projectflow;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ba.sum.fsre.projectflow.auth.AuthActivity;
import ba.sum.fsre.projectflow.storage.TokenManager;
import ba.sum.fsre.projectflow.ui.HomeFragment;
import ba.sum.fsre.projectflow.ui.ProfileFragment;
import ba.sum.fsre.projectflow.ui.ProjectsFragment;
import ba.sum.fsre.projectflow.ui.TeamsFragment;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = new TokenManager(this);

        if (!checkAuthentication()) {
            return;
        }

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            
            bottomNavigation.setPadding(0, 0, 0, systemBars.bottom);
            return insets;
        });

        setupBottomNavigation();

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (itemId == R.id.nav_teams) {
                fragment = new TeamsFragment();
            } else if (itemId == R.id.nav_projects) {
                fragment = new ProjectsFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!checkAuthentication()) {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkAuthentication()) {
            return;
        }
    }

    /**
     * Check if user is authenticated.
     * If not, redirect to AuthActivity and finish current activity.
     * @return true if authenticated, false otherwise
     */
    private boolean checkAuthentication() {
        if (tokenManager == null) {
            tokenManager = new TokenManager(this);
        }

        if (!tokenManager.hasValidSession()) {
            startActivity(new Intent(this, AuthActivity.class));
            finish();
            return false;
        }
        return true;
    }
}
