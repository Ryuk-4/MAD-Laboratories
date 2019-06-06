package it.polito.mad.customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword, inputName, inputSurname, inputPhoneNo;
    private ProgressBar progressBar;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        FirebaseUtils.setupFirebaseCustomer();

        getLayoutReference();

        addListenerToButtons();
    }

    private void addListenerToButtons() {
        Button btnSignIn = findViewById(R.id.sign_in_button);
        Button btnSignUp = findViewById(R.id.sign_up_button);
        Button btnResetPassword = findViewById(R.id.btn_reset_password);

        btnResetPassword.setOnClickListener(v -> startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class)));

        btnSignIn.setOnClickListener(v -> finish());

        btnSignUp.setOnClickListener(v -> {

            String email = inputEmail.getText().toString().trim();
            String password = inputPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(inputName.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Enter name!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(inputSurname.getText().toString().trim())) {
                Toast.makeText(getApplicationContext(), "Enter surname!", Toast.LENGTH_SHORT).show();
                return;
            }


            if (password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                return;
            }

            SignupActivity.this.getSharedPreferences("userinfo", Context.MODE_PRIVATE).edit().putString("name", inputName.getText().toString().trim()).commit();
            SignupActivity.this.getSharedPreferences("userinfo", Context.MODE_PRIVATE).edit().putString("surname", inputSurname.getText().toString().trim()).commit();

            FirebaseUtils.setupFirebaseCustomer();

            progressBar.setVisibility(View.VISIBLE);
            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(SignupActivity.this, task -> {
                        Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);

                        if (!task.isSuccessful()) {
                            Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            saveProfileInfoLocal(inputName.getText().toString(), inputSurname.getText().toString(), inputEmail.getText().toString(), inputPhoneNo.getText().toString());
                            saveProfileInfoFirebase(auth.getCurrentUser().getUid(), inputName.getText().toString(), inputSurname.getText().toString(), inputEmail.getText().toString(), inputPhoneNo.getText().toString());
                            startActivity(new Intent(SignupActivity.this, MainActivity.class));
                            finish();
                        }
                    });

        });
    }

    private void getLayoutReference() {
        inputEmail = findViewById(R.id.email);
        inputName = findViewById(R.id.name);
        inputSurname = findViewById(R.id.surname);
        inputPhoneNo = findViewById(R.id.number);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    private void saveProfileInfoLocal(String name, String surname, String email, String phoneNumber)
    {
        SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);;
        SharedPreferences.Editor e = sharedPreferences.edit();

        e.putString("name", name);
        e.putString("phone", phoneNumber);
        e.putString("email", email);
        e.putString("surname", surname);

        e.apply();
    }

    private void saveProfileInfoFirebase(String uId, String name, String surname, String email, String phoneNumber)
    {
        DatabaseReference databaseReference = FirebaseUtils.branchCustomerProfile;
        databaseReference.child("name").setValue(name);
        databaseReference.child("surname").setValue(surname);
        databaseReference.child("email").setValue(email);
        databaseReference.child("phone").setValue(phoneNumber);
    }
}