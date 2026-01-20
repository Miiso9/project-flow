package ba.sum.fsre.projectflow.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ba.sum.fsre.projectflow.profile.CompleteProfileActivity;
import ba.sum.fsre.projectflow.MainActivity;
import ba.sum.fsre.projectflow.R;

public class LoginFragment extends Fragment {

    private AuthViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        com.google.android.material.textfield.TextInputEditText email = view.findViewById(R.id.email);
        com.google.android.material.textfield.TextInputEditText password = view.findViewById(R.id.password);
        com.google.android.material.button.MaterialButton login = view.findViewById(R.id.loginBtn);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        login.setOnClickListener(v ->
                viewModel.login(
                        requireContext(),
                        email.getText().toString(),
                        password.getText().toString()
                )
        );

        viewModel.getAuthSuccess().observe(getViewLifecycleOwner(), success -> {
            if (!success) {
                Toast.makeText(getContext(), "Login failed", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getProfileCompleted().observe(getViewLifecycleOwner(), isCompleted -> {
            if (viewModel.getAuthSuccess().getValue() != null && viewModel.getAuthSuccess().getValue()) {
                if (isCompleted) {
                    startActivity(new Intent(getActivity(), MainActivity.class));
                    getActivity().finish();
                } else {
                    startActivity(new Intent(getActivity(), CompleteProfileActivity.class));
                    getActivity().finish();
                }
            }
        });

        TextView goRegister = view.findViewById(R.id.goRegisterText);

        goRegister.setOnClickListener(v ->
                requireActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_container, new RegisterFragment())
                        .addToBackStack(null)
                        .commit()
        );


        return view;
    }
}
