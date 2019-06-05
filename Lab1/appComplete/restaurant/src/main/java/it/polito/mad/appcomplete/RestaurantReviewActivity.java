package it.polito.mad.appcomplete;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class RestaurantReviewActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantReviewActivit";

    private List<Comment> comments;
    private RecyclerView mRecyclerView;
    private RatingBar overAllRating;
    private TextView overAllText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.recyclerViewRestaurantComment);

        overAllRating = findViewById(R.id.overAllRating);

        overAllText = findViewById(R.id.overAllRatingText);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupFirebase();

        fetchOverallRating();

        fetchCommets();
    }

    private void fetchOverallRating() {

        branchOverallRating.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {

                    Float rating = dataSnapshot.child("total").getValue(Float.class);

                    overAllRating.setRating(rating);

                    overAllText.setText(String.valueOf(rating));

                } catch (Exception e){
                    Log.w(TAG, "onDataChange: ", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void fetchCommets() {

        branchComment.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments = new ArrayList<>();

                try {
                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                        Comment c = dataSnapshot1.getValue(Comment.class);

                        comments.add(new Comment(c.getDate(), c.getTitle(), c.getStars(), c.getDescription()));
                    }

                    displayComments();
                } catch (NullPointerException nEx) {
                    Log.w(TAG, "onDataChange: ", nEx);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void displayComments() {
        Collections.sort(comments, Comment.BY_STAR_DESCENDING);

        RecyclerViewAdapterComment myAdapter = new RecyclerViewAdapterComment(RestaurantReviewActivity.this,
                comments);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(myAdapter);
    }
}
