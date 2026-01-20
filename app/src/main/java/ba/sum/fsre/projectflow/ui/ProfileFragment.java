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

import java.util.List;

import ba.sum.fsre.projectflow.R;
import ba.sum.fsre.projectflow.auth.AuthActivity;
import ba.sum.fsre.projectflow.model.User;
import ba.sum.fsre.projectflow.network.RetrofitClient;
import ba.sum.fsre.projectflow.network.SupabaseApi;
import ba.sum.fsre.projectflow.profile.CompleteProfileActivity;
import ba.sum.fsre.projectflow.storage.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvUserName;
    private TextView tvUserEmail;
    private ProgressBar progressBar;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tokenManager = new TokenManager(requireContext());

        setupUI(view);
        loadUserProfile();

        return view;
    }

    private void setupUI(View view) {
        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        progressBar = view.findViewById(R.id.progressBar);

        view.findViewById(R.id.btnEditProfile).setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), CompleteProfileActivity.class));
        });

        view.findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        logout();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void loadUserProfile() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            tvUserName.setText("Guest");
            tvUserEmail.setText("Not logged in");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        SupabaseApi api = RetrofitClient.getClient(requireContext()).create(SupabaseApi.class);
        api.getUserProfile("eq." + userId).enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    User user = response.body().get(0);
                    String fullName = "";
                    if (user.first_name != null) fullName = user.first_name;
                    if (user.last_name != null) fullName += " " + user.last_name;
                    if (fullName.trim().isEmpty()) fullName = "User";

                    tvUserName.setText(fullName.trim());
                    tvUserEmail.setText(user.email != null ? user.email : "");
                } else {
                    tvUserName.setText("User");
                    tvUserEmail.setText("");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        tokenManager.clearTokens();
        Intent intent = new Intent(getActivity(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }
}
