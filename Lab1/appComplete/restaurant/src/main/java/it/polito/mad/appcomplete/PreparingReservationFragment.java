package it.polito.mad.appcomplete;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static it.polito.mad.data_layer_access.FirebaseUtils.*;


public class PreparingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener{

    private static final String TAG = "PreparingReservation";

    private ArrayList<ReservationInfo> reservationPreparingList;

    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

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

        setupFirebase();

        initializeReservation();

        removeDailyFoodIfRequired();

        return view;
    }

    private void removeDailyFoodIfRequired() {
        branchDailyFood.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()){
                    if (data.child("quantity").getValue()!= null && data.child("quantity").getValue().equals("0")){
                        branchDailyFood.child(data.getKey()).removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeReservation() {

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
        try {
            ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                    ItemTouchHelper.RIGHT, this, getActivity(), false);

            new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
        } catch (NullPointerException e) {
            Log.w(TAG, "initializeRecyclerViewReservation: ", e);
        }
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ReservationViewHolder) {

            String name = reservationPreparingList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationPreparingList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            final String deletedReservationId = deletedItem.getOrderID();
            Log.d(TAG, "onSwiped: deletedOrderId " + deletedReservationId);

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
        res.setRestaurantId(Uid);
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setOrderList(reservationInfo.getOrderList());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        if (reservationInfo.getNote() != null) {
            res.setNote(reservationInfo.getNote());
        }

        return res;
    }

}
