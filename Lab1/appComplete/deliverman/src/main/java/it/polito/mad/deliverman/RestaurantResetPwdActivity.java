package it.polito.mad.deliverman;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RestaurantResetPwdActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantResetPwdActiv";

    private EditText emailInput;
    private Button resetButton;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_reset_pwd);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.targetResetMail);
        resetButton = findViewById(R.id.resetBtn);

        progressBar = findViewById(R.id.progressBar);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                String email = emailInput.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_LONG).show();
                    return;
                }

                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Toast.makeText(RestaurantResetPwdActivity.this,
                                            "We have sent you instructions to reset your password!",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(RestaurantResetPwdActivity.this,
                                            "Failed to send reset email!", Toast.LENGTH_LONG).show();
                                }

                            }
                        });
            }

        });
        finish();
    }

}
