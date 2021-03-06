package it.polito.mad.customer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset, btnLoginGoole;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        // set the view now
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_restaurant);
        setSupportActionBar(toolbar);

        getLayoutReference();

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        btnLoginGoole.setOnClickListener(v -> signInWithGoogle());

        btnSignup.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        btnReset.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class)));

        btnLogin.setOnClickListener(v -> {
            String email = inputEmail.getText().toString();
            final String password = inputPassword.getText().toString();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            //authenticate user
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            // there was an error
                            if (password.length() < 6) {
                                inputPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        });
    }

    private void getLayoutReference() {
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        btnSignup = findViewById(R.id.btn_signup);
        btnLogin = findViewById(R.id.btn_login);
        btnReset = findViewById(R.id.btn_reset_password);
        btnLoginGoole = findViewById(R.id.btn_login_google);
    }

    private void signInWithGoogle()
    {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("TAG", "onActivityResult: result received");
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Toast.makeText(this, "impossible to sign in with google", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        Log.d("TAG", "firebaseAuthWithGoogle: in function");
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        saveProfileInfo();

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Sign In Failed!", Toast.LENGTH_SHORT).show();
                        // If sign in fails, display a message to the user.
                    }

                    // ...
                });
    }

    private void saveProfileInfo() {
        //save profile info in the shared preference
        saveProfileInfoLocal(auth.getCurrentUser());

        //save profile info in Firebase
        saveProfileInfoFirebase(auth.getCurrentUser());
    }

    private void saveProfileInfoLocal(FirebaseUser user)
    {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String phoneNumber = user.getPhoneNumber();

            String photo = "";
            if (user.getPhotoUrl() != null)
                photo = user.getPhotoUrl().toString();

            SharedPreferences sharedPreferences = getSharedPreferences("userinfo", Context.MODE_PRIVATE);;
            SharedPreferences.Editor e = sharedPreferences.edit();

            e.putString("name", name.split(" ")[0]);
            e.putString("surname", name.split(" ")[1]);
            e.putString("phone", phoneNumber);
            e.putString("email", email);
            e.putString("imageEncoded", photo);

            e.apply();
        }
    }

    private void saveProfileInfoFirebase(FirebaseUser user)
    {
        if (user != null) {
            String name = user.getDisplayName().split(" ")[0];
            String surname = user.getDisplayName().split(" ")[1];
            String email = user.getEmail();
            String phoneNumber = user.getPhoneNumber();

            String photo = "";
            if (user.getPhotoUrl() != null)
                photo = user.getPhotoUrl().toString();

            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("Profile");
            databaseReference.child("name").setValue(name);
            databaseReference.child("surname").setValue(surname);
            databaseReference.child("email").setValue(email);
            databaseReference.child("phone").setValue(phoneNumber);
            databaseReference.child("photo").setValue(photo);
        }
    }
}