package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class RestaurantLoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "RestaurantLoginActivity";

    private static final int REQ_SIGN_IN = 9001;
    private static final int G_REQ_SIGN_IN = 9002;

    public static RestaurantLoginActivity rla;

    private EditText inputEmail;
    private EditText inputPassword;
    private Button loginButton;
    private TextView signUpActivity;
    private TextView resetPassword;

    private ProgressBar progressBar;

    private SignInButton gSignInButton;

    private FirebaseAuth auth;
    private GoogleSignInOptions gso;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: called");

        rla = this;

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            startActivity(new Intent(RestaurantLoginActivity.this, ReservationActivity.class));
            finish();
        }

        setContentView(R.layout.activity_login);

        inputEmail = findViewById(R.id.loginEmail);
        inputPassword = findViewById(R.id.loginPwd);
        resetPassword = findViewById(R.id.resetPwdLink);
        loginButton = findViewById(R.id.loginButton);
        signUpActivity = findViewById(R.id.signUpLink);

        progressBar = findViewById(R.id.progressBar);

        gSignInButton = findViewById(R.id.googleSignInButton);
        gSignInButton.setSize(SignInButton.SIZE_WIDE);

        resetPassword.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        signUpActivity.setOnClickListener(this);

        gSignInButton.setOnClickListener(this);

        gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.loginButton:
                boolean flag = validateForm();

                if (flag) {
                    signIn(REQ_SIGN_IN);
                }
                break;

            case R.id.signUpLink:
                startActivity(new Intent(RestaurantLoginActivity.this,
                        RestaurantSignUpActivity.class));
                break;

            case R.id.resetPwdLink:
                startActivity(new Intent(RestaurantLoginActivity.this,
                        RestaurantResetPwdActivity.class));
                break;

            case R.id.googleSignInButton:
                signIn(G_REQ_SIGN_IN);
                break;
        }

    }

    private boolean validateForm() {
        boolean flag = true;

        String email = inputEmail.getText().toString();
        final String pwd = inputPassword.getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "Missing email address or password!", Toast.LENGTH_SHORT).show();
            flag = false;
        }

        if (pwd.length() < 8) {
            inputPassword.setError(getString(R.string.minimum_password_length));
            flag = false;
        }

        return flag;
    }

    private void signIn(final int req) {
        Log.d(TAG, "signIn: called");
        switch (req) {
            case REQ_SIGN_IN:
                fireBaseAuthWithEmailAndPassword();
                break;
            case G_REQ_SIGN_IN:
                Intent gSignInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(gSignInIntent, req);
                break;
        }
    }

    private void fireBaseAuthWithEmailAndPassword() {
        progressBar.setVisibility(View.VISIBLE);

        String email = inputEmail.getText().toString();
        final String pwd = inputPassword.getText().toString();

        auth.signInWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(RestaurantLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        /* If sign in fails, display a message to the user. If sign in succeeds
                         * the auth state listener will be notified and logic to handle the
                         * signed in user can be handled in the listener.
                         */
                        if (!task.isSuccessful()) {
                            // there was an error
                            Toast.makeText(RestaurantLoginActivity.this,
                                    getString(R.string.auth_failed), Toast.LENGTH_LONG).show();

                        } else {
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                            Intent intent = new Intent(RestaurantLoginActivity.this, ReservationActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if(user != null) {
            SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = preferences.edit();

            editor.putBoolean("login", true);

            if (!preferences.getString("Uid", "userId").equals(user.getUid())) {
                editor.putString("Uid", auth.getCurrentUser().getUid());
            }
            editor.apply();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == G_REQ_SIGN_IN) {
            Log.d(TAG, "onActivityResult: callled");
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                fireBaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w(TAG, "onActivityResult: ", e);
            }
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount account) {
        progressBar.setVisibility(View.VISIBLE);
        
        Log.d(TAG, "fireBaseAuthWithGoogle: called");
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: task.isSuccessful() called");
                            startActivity(new Intent(RestaurantLoginActivity.this, ReservationActivity.class));
                            finish();
                        } else {

                            Toast.makeText(RestaurantLoginActivity.this,
                                    getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public interface RestaurantLoginInterface{
        void logout();
    }
}
