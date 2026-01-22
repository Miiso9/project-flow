package ba.sum.fsre.projectflow.auth;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import ba.sum.fsre.projectflow.MainActivity;
import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.storage.TokenManager;

public class AuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TokenManager tokenManager = new TokenManager(this);

        if (tokenManager.hasValidSession()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.auth_container, new LoginFragment())
                    .commit();
        }
    }
}
