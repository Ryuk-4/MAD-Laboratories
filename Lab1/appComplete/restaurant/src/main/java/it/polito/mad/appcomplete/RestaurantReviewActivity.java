package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestaurantReviewActivity extends AppCompatActivity {

    private static final String TAG = "RestaurantReviewActivit";

    private SharedPreferences preferences;
    private DatabaseReference database;
    private DatabaseReference commentBranch;

    private List<Comment> comments;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant_review);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = findViewById(R.id.recyclerViewRestaurantComment);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setFirebase();

        fetchCommets();
    }

    private void setFirebase() {
        Log.d(TAG, "setFirebase: called");
        preferences = getSharedPreferences("loginState", Context.MODE_PRIVATE);
        String Uid = preferences.getString("Uid", "");

        database = FirebaseDatabase.getInstance().getReference().child("restaurants/" + Uid);

        commentBranch = database.child("review_description");
    }

    private void fetchCommets(){

        commentBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                comments = new ArrayList<>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){
                    Comment c = dataSnapshot1.getValue(Comment.class);

                    comments.add(new Comment(c.getDate() ,c.getTitle(), c.getStars(), c.getDescription()));
                }

                displayComments();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });
    }

    private void displayComments(){
        Collections.sort(comments, Comment.BY_STAR_DESCENDING);

        RecyclerViewAdapterComment myAdapter = new RecyclerViewAdapterComment(RestaurantReviewActivity.this,
                comments);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(myAdapter);
    }
}
