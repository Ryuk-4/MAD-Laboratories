package it.polito.mad.customer;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ReviewActivity extends AppCompatActivity {

    private String restId, orderId;
    private Button button;
    private RatingBar ratingBar;
    private EditText textTitle, textDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        if (savedInstanceState == null)
        {
            Bundle bundle = getIntent().getExtras();
            restId = bundle.getString("restId");
            orderId = bundle.getString("orderId");
        }

        button = findViewById(R.id.button);
        ratingBar = findViewById(R.id.ratingBar);
        textTitle = findViewById(R.id.textTitle);
        textDescription = findViewById(R.id.textDescription);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = textTitle.getText().toString();
                String description = textDescription.getText().toString();
                final int rating = (int) ratingBar.getRating();

                //Log.d("TAG", "onClick: "+rating);

                addNewReview(title, description, rating);
                incrementStarReview(rating);
                setOrderReviewed();
            }
        });
    }

    private void setOrderReviewed() {
        Log.d("TAG", "setOrderReviewed: ");
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order").child(orderId);
        databaseReference.child("reviewed").setValue("true");
    }

    private void incrementStarReview(final int rating) {
        DatabaseReference databaseReference1 = FirebaseDatabase.getInstance().getReference("restaurants").child(restId).child("review").child(rating+"star");
        databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("TAG", "onDataChange: ");
                int value = Integer.parseInt(dataSnapshot.getValue().toString());
                value++;
                FirebaseDatabase.getInstance().getReference("restaurants").child(restId).child("review").child(rating+"star").setValue(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addNewReview(String title, String description, float rating) {
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        String formattedDate = df.format(c);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(restId).child("review_description").push();
        databaseReference.child("title").setValue(title);
        databaseReference.child("description").setValue(description);
        databaseReference.child("stars").setValue(rating);
        databaseReference.child("date").setValue(formattedDate);

        //TODO add user to be displayed
    }
}
