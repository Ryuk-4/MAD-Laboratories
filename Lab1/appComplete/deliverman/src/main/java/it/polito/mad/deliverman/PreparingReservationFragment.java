package it.polito.mad.deliverman;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class PreparingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "PreparingReservation";

    private ArrayList<ReservationInfo> reservationPreparingList;

    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;


    private SharedPreferences preferences;
    private DatabaseReference database;
    private DatabaseReference branchOrdersInPreparation;
    private DatabaseReference branchOrdersReady;

    public PreparingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_preparing_reservation, container, false);
        Log.d(TAG, "onCreateView: called");

        recyclerView = view.findViewById(R.id.recyclerViewPreparingReservation);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(this);

        initializeReservation();

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        if (id == R.id.menu_refresh) {

            /*
             * TODO: mySwipeRefreshLayout.setRefreshing(true);
             * TODO: myUpdateOP.
             */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeReservation() {
        preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);
        String Uid = preferences.getString("Uid", " ");

        database = FirebaseDatabase.getInstance().getReference();
        branchOrdersInPreparation = database.child("restaurants/" + Uid + "/Orders/In_Preparation");

        branchOrdersInPreparation.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationPreparingList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationPreparingList.add(restoreItem(value));
                }

                initializeRecyclerViewReservation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationPreparingList);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                ItemTouchHelper.RIGHT, this, getActivity(), false);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
            // get the removed item name to display it in snack bar
            preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

            String name = reservationPreparingList.get(viewHolder.getAdapterPosition()).getNamePerson();
            String Uid = preferences.getString("Uid", " ");

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationPreparingList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            final String deletedReservationId = deletedItem.getOrderID();
            Log.d(TAG, "onSwiped: deletedOrderId " + deletedReservationId);

            database = FirebaseDatabase.getInstance().getReference();

            branchOrdersInPreparation = database.child("restaurants/" + Uid + "/Orders/In_Preparation");

            branchOrdersReady = database.child("restaurants/" + Uid + "/Orders/Ready_To_Go");

            branchOrdersInPreparation.child(deletedReservationId).removeValue();
            branchOrdersReady.child(deletedReservationId).setValue(restoreItem(deletedItem));

            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + "\'s reservation ready to go", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    branchOrdersInPreparation.child(deletedReservationId).setValue(restoreItem(deletedItem));
                    branchOrdersReady.child(deletedReservationId).removeValue();
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setPersonOrder(reservationInfo.getPersonOrder());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }

        return res;
    }

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }
}
