package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RestaurantSignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RestaurantSignUpActivit";

    private FirebaseAuth auth;

    private  EditText inputName;
    private EditText inputEmail;
    private EditText inputPwd;
    private EditText inputPwd2;
    private TextView login_link;
    private Button signUpBtn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_sign_up);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();

        inputName = findViewById(R.id.signUpName);
        inputEmail = findViewById(R.id.signUpEmail);
        inputPwd = findViewById(R.id.signUpPwd);
        inputPwd2 = findViewById(R.id.signUpRePwd);
        login_link = findViewById(R.id.loginLink);
        signUpBtn = findViewById(R.id.signUpBtn);

        progressBar = findViewById(R.id.progressBar);

        login_link.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.loginLink:
                finish();
                break;

            case R.id.signUpBtn:
                boolean flag = validate();

                if (flag) {
                    signUp();
                }

                break;
        }
    }

    private void signUp() {
        progressBar.setVisibility(View.VISIBLE);

        final String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String pwd = inputPwd.getText().toString();

        //create user
        auth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(RestaurantSignUpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Toast.makeText(RestaurantSignUpActivity.this,
                                "createUserWithEmail:onComplete:" + task.isSuccessful(),
                                Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);

                        /* If sign in fails, display a message to the user. If sign in succeeds
                         * the auth state listener will be notified and logic to handle the
                         * signed in user can be handled in the listener.
                         */
                        if (!task.isSuccessful()) {
                            Toast.makeText(RestaurantSignUpActivity.this,
                                    getString(R.string.auth_failed) + task.getException(),
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            RestaurantLoginActivity.rla.finish();

                            updateUI();
                            createDataBase(name);

                            startActivity(new Intent(RestaurantSignUpActivity.this, ReservationActivity.class));
                            finish();
                        }
                    }
                });
    }

    private void updateUI() {

        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putBoolean("login", true);

        editor.apply();
    }

    private void createDataBase(String name) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        final String Uid = auth.getCurrentUser().getUid();
        final String EmailUser = auth.getCurrentUser().getEmail();

        SharedPreferences preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();

        editor.putString("Uid", Uid);

        editor.apply();

        DatabaseReference branchProfile = database.child("restaurants/"+Uid+"/Profile");
        //DatabaseReference branchDailyFood = database.child("restaurants").child(Uid).child("Daily_Food");
        //DatabaseReference branchFavorites = database.child("restaurants").child(Uid).child("Favorites");

        branchProfile.child("name").setValue(name);
        branchProfile.child("firstTime").setValue(true);
        branchProfile.child("email").setValue(EmailUser);
/*
        branchDailyFood.child("Description").setValue("prova");
        branchDailyFood.child("Name").setValue("prova");
        branchDailyFood.child("Photo").setValue("Url");

        branchDailyFood.child("Food3").child("Description").setValue("prova");
        branchDailyFood.child("Food3").child("Name").setValue("prova");

        branchDailyFood.child("Review").child("1star").setValue("21");
        branchDailyFood.child("Review").child("2star").setValue("45");
        branchDailyFood.child("Review").child("3star").setValue("12");
        branchDailyFood.child("Review").child("4star").setValue("33");
        branchDailyFood.child("Review").child("5star").setValue("56");

        branchDailyFood.child("Type_Food").child("type1").setValue("italian");

        branchFavorites.child("Description").setValue("prova");
        branchFavorites.child("Name").setValue("prova");
        branchFavorites.child("Photo").setValue("Url");

        branchFavorites.child("Food3").child("Description").setValue("prova");
        branchFavorites.child("Food3").child("Name").setValue("prova");
        branchFavorites.child("Type_Food").child("type1").setValue("italian");
        */
    }

    private boolean validate() {
        boolean flag = true;

        String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        final String pwd = inputPwd.getText().toString();
        final String pwd2 = inputPwd2.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(pwd2)) {
            Toast.makeText(getApplicationContext(), "One or more fields may be empty!",
                    Toast.LENGTH_LONG).show();
            flag = false;
        } else if (!TextUtils.equals(pwd, pwd2)) {
            Toast.makeText(getApplicationContext(), "The two passwords must be equal!",
                    Toast.LENGTH_LONG).show();
            flag = false;
        }

        return flag;
    }
}
