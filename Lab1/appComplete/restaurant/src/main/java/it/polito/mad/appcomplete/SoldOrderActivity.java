package it.polito.mad.appcomplete;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;

public class SoldOrderActivity extends AppCompatActivity {

    private static final String TAG = "SoldOrderActivity";

    private List<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private ImageButton calendarButton;
    private TextView soldOrderDate;

    private ValueEventListener valueEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sold_order);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerViewSoldOrders);
        soldOrderDate = findViewById(R.id.dateOfSold);

        calendarButton = findViewById(R.id.iconOpenCalendar);

        setupFirebase();

        setDate();
    }

    private void setDate() {
        Calendar c = Calendar.getInstance();
        final int mYear = c.get(Calendar.YEAR);
        final int mMonth = c.get(Calendar.MONTH);
        final int mDay = c.get(Calendar.DAY_OF_MONTH);

        soldOrderDate.setText(mDay + "-" + (mMonth+1) + "-" + mYear);

        showOrders();

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                datePickerDialog = new DatePickerDialog(SoldOrderActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                soldOrderDate.setText(dayOfMonth + "-" + (month + 1) + "-" + year);

                                if (reservationInfoList != null){
                                    Log.d(TAG, "onDateSet: called");
                                    reservationInfoList.clear();
                                    database.removeEventListener(valueEventListener);
                                    setAdapter();
                                }

                                showOrders();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

    }

    private void showOrders() {
        reservationInfoList = new ArrayList<>();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    ReservationInfo res = dataSnapshot1.getValue(ReservationInfo.class);

                    if (res.getDate().equals(soldOrderDate.getText().toString())) {
                        ReservationInfo reservationInfo = new ReservationInfo();

                        reservationInfo.setNamePerson(res.getNamePerson());
                        reservationInfo.setTimeReservation(res.getTimeReservation());
                        reservationInfo.setOrderList(res.getOrderList());
                        reservationInfo.setDate(res.getDate());

                        reservationInfoList.add(reservationInfo);

                    }
                }

                if (reservationInfoList.size() != 0)
                    setAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        branchSoldOrders.addListenerForSingleValueEvent(valueEventListener);
    }

    public void setAdapter() {
        myAdapter = new RecyclerViewAdapterReservation(this, reservationInfoList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(myAdapter);

    }
}
