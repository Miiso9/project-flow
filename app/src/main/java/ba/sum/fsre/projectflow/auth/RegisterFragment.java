package ba.sum.fsre.projectflow.auth;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import ba.sum.fsre.projectflow.R;

public class RegisterFragment extends Fragment {

    private AuthViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        com.google.android.material.textfield.TextInputEditText firstName = view.findViewById(R.id.firstName);
        com.google.android.material.textfield.TextInputEditText lastName = view.findViewById(R.id.lastName);
        com.google.android.material.textfield.TextInputEditText email = view.findViewById(R.id.email);
        com.google.android.material.textfield.TextInputEditText password = view.findViewById(R.id.password);
        com.google.android.material.textfield.TextInputEditText confirmPassword = view.findViewById(R.id.confirmPassword);
        com.google.android.material.button.MaterialButton registerBtn = view.findViewById(R.id.registerBtn);
        TextView goLoginText = view.findViewById(R.id.goLoginText);

        viewModel = new AuthViewModel();

        registerBtn.setOnClickListener(v -> {
            String fName = firstName.getText().toString().trim();
            String lName = lastName.getText().toString().trim();
            String mail = email.getText().toString().trim();
            String pass = password.getText().toString();
            String confirmPass = confirmPassword.getText().toString();

            if (fName.isEmpty() || lName.isEmpty() || mail.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pass.equals(confirmPass)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(getContext(), mail, pass, fName, lName);

            viewModel.getAuthSuccess().observe(getViewLifecycleOwner(), success -> {
                if (success) {
                    Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    requireActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.auth_container, new LoginFragment())
                            .commit();
                } else {
                    Toast.makeText(getContext(), "Registration failed", Toast.LENGTH_SHORT).show();
                }
            });
        });

        goLoginText.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_container, new LoginFragment())
                        .commit()
        );

        return view;
    }
}
