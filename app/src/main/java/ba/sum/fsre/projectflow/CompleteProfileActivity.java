package ba.sum.fsre.projectflow;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import ba.sum.fsre.projectflow.storage.TokenManager;

public class CompleteProfileActivity extends AppCompatActivity {

    private CompleteProfileViewModel viewModel;
    private TextInputEditText phoneNumberEditText;
    private TextInputEditText cityEditText;
    private TextInputEditText dateOfBirthEditText;
    private AutoCompleteTextView genderSpinner;
    private Button submitBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_complete_profile);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        phoneNumberEditText = findViewById(R.id.phoneNumber);
        cityEditText = findViewById(R.id.city);
        dateOfBirthEditText = findViewById(R.id.dateOfBirth);
        genderSpinner = findViewById(R.id.genderSpinner);
        submitBtn = findViewById(R.id.submitBtn);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_list_item_1
        );
        genderSpinner.setAdapter(genderAdapter);
        genderSpinner.setText(genderAdapter.getItem(0).toString(), false);

        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(calendar.getTime());
            dateOfBirthEditText.setText(formattedDate);
        };

        dateOfBirthEditText.setOnClickListener(v -> {
            new DatePickerDialog(
                    this,
                    dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        viewModel = new ViewModelProvider(this).get(CompleteProfileViewModel.class);

        TokenManager tokenManager = new TokenManager(this);
        String userId = tokenManager.getUserId();

        if (userId == null) {
            Toast.makeText(this, "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel.getUpdateSuccess().observe(this, success -> {
            progressBar.setVisibility(View.GONE);
            submitBtn.setEnabled(true);
            
            if (success) {
                Toast.makeText(this, "Profile completed successfully!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        submitBtn.setOnClickListener(v -> {
            String phone = phoneNumberEditText.getText().toString().trim();
            String city = cityEditText.getText().toString().trim();
            String dateOfBirth = dateOfBirthEditText.getText().toString().trim();
            String gender = genderSpinner.getText().toString().trim().toLowerCase();

            if (phone.isEmpty() || city.isEmpty() || dateOfBirth.isEmpty() || gender.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!dateOfBirth.matches("\\d{4}-\\d{2}-\\d{2}")) {
                Toast.makeText(this, "Date format should be YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            submitBtn.setEnabled(false);

            viewModel.updateProfile(this, userId, phone, city, dateOfBirth, gender);
        });
    }
}