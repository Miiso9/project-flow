package ba.sum.fsre.projectflow.auth;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ba.sum.fsre.projectflow.R;

public class RegisterFragment extends Fragment {

    private AuthViewModel viewModel;
    private com.google.android.material.checkbox.MaterialCheckBox termsCheckbox;

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

        termsCheckbox = view.findViewById(R.id.termsCheckbox);
        setupTermsAndConditionsLinks();

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

            if (!termsCheckbox.isChecked()) {
                Toast.makeText(getContext(), "You must agree to the Terms and Privacy Policy to register", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(getContext(), mail, pass, fName, lName);
        });

        viewModel.getAuthSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_container, new LoginFragment())
                        .commit();
            }
        });

        viewModel.getAuthError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(getContext(), errorMsg, Toast.LENGTH_LONG).show();
            }
        });

        goLoginText.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_container, new LoginFragment())
                        .commit()
        );

        return view;
    }

    private void setupTermsAndConditionsLinks() {
        String text = "I agree to the Terms of Conditions and Privacy Policy";
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "https://sites.google.com/fsre.sum.ba/project-flow-tac";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
                ds.setColor(Color.BLUE);
            }
        };

        ClickableSpan privacyClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                String url = "https://sites.google.com/fsre.sum.ba/project-flow-privacy-policy";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
                ds.setColor(Color.BLUE);
            }
        };

        spannableString.setSpan(termsClickableSpan, 15, 34, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(privacyClickableSpan, 39, 53, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termsCheckbox.setText(spannableString);
        termsCheckbox.setMovementMethod(LinkMovementMethod.getInstance());
        termsCheckbox.setHighlightColor(Color.TRANSPARENT);
    }
}