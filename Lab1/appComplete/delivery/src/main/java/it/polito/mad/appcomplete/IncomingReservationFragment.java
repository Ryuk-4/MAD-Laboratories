package it.polito.mad.appcomplete;

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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class IncomingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "IncomingReservation";

    private ArrayList<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private SharedPreferences preferences;
    private DatabaseReference database;

    // for notification
    private SharedPreferences.Editor editor;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

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

        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference branchOrdersIncoming = database.child("delivery/" +
                preferences.getString("Uid", "") + "/Orders/Incoming");

        branchOrdersIncoming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationInfoList = new ArrayList<>();

                for (DataSnapshot data :  dataSnapshot.getChildren()){
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationInfoList.add(restoreItem(value));
                }

                /////for notification
                DatabaseReference branchOrders = database.child("delivery/" +
                        preferences.getString("Uid", "") + "/Orders/");

                if (reservationInfoList.size() == 0){
                    branchOrders.child("IncomingReservationFlag").setValue(false);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", false);
                    editor.apply();
                } else {
                    branchOrders.child("IncomingReservationFlag").setValue(true);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", true);
                    editor.apply();
                }

                try {
                    getActivity().invalidateOptionsMenu();
                } catch (NullPointerException e){
                    Log.w(TAG, "onDataChange: ", e);
                }
                ///////////////////
                initializeRecyclerViewReservation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationInfoList);
        Log.d(TAG, "initializeRecyclerViewReservation: called");

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);


        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(myAdapter);

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, getActivity(), true);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position)
    {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder)
        {
            //String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());  // backup of removed item for undo purpose
            final String deletedReservationId = deletedItem.getOrderID();
            preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

            if (direction == ItemTouchHelper.RIGHT)
            {
                final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                ValueEventListener postListener = new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // Add to finished branch:
                            final DatabaseReference branchOrdersInPreparation = mDatabase.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("finished");
                            branchOrdersInPreparation.child(deletedReservationId).setValue(restoreItem(deletedItem));

                        // Add delivered flag to restaurant:
                            //String restaurantID= "EeEfwV4KAPRYrUk4NJXj052LqXh1";
                            String restaurantID = dataSnapshot.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("Incoming").child(deletedReservationId).child("restaurantId").getValue(String.class);
                            final DatabaseReference branchOrdersInRestaurant = database.child("restaurants").child(restaurantID).child("Orders").child("Ready_To_Go");
                            branchOrdersInRestaurant.child(deletedReservationId).child("status_order").setValue("delivered");

                        //Removing order from incoming branch:
                            final DatabaseReference branchOrdersIncoming = database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming");
                            branchOrdersIncoming.child(deletedReservationId).removeValue();

                        // Show undo message
                            Snackbar snackbar = Snackbar.make(recyclerView,   " delivery finished", Snackbar.LENGTH_LONG);
                            snackbar.setAction("UNDO", new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view)
                                {
                                    //branchOrdersInRestaurant.child(deletedReservationId).setValue(restoreItem(deletedItem));
                                    branchOrdersIncoming.child(deletedReservationId).setValue(restoreItem(deletedItem));
                                    branchOrdersInPreparation.child(deletedReservationId).removeValue();

                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                    ValueEventListener postListener = new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            String restaurantID = dataSnapshot.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("Incoming").child(deletedReservationId).child("restaurantId").getValue(String.class);
                                            // return delivery flag to in_delivery for restaurant branch:
                                            final DatabaseReference branchOrdersInRestaurant = database.child("restaurants").child(restaurantID).child("Orders").child("Ready_To_Go");
                                            branchOrdersInRestaurant.child(deletedReservationId).child("status_order").setValue("in_delivery");
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {  }
                                    };
                                    mDatabase.addValueEventListener(postListener);

                                }
                            });
                            snackbar.setActionTextColor(Color.YELLOW);
                            snackbar.show();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {  }
                };
                mDatabase.addValueEventListener(postListener);



                //====================================================
                //Snackbar.make(recyclerView,  "resID"+restaurantID, Snackbar.LENGTH_LONG).show();

                // Add to finished branch:
                /*final DatabaseReference branchOrdersInPreparation = database.child("delivery")
                        .child(preferences.getString("Uid", " ")).child("Orders").child("finished");
                branchOrdersInPreparation.child(deletedReservationId).setValue(restoreItem(deletedItem));*/

                // Add delivered flag to restaurant:
                //String restaurantID= "EeEfwV4KAPRYrUk4NJXj052LqXh1";
             /*   final DatabaseReference branchOrdersInRestaurant = database.child("restaurants")
                        .child(restaurantID).child("Orders").child("Ready_To_Go");
                branchOrdersInRestaurant.child(deletedReservationId).child("status_order").setValue("delivered");*/

                //Removing order from incoming branch:
                //branchOrdersIncoming.child(deletedReservationId).removeValue();

                // Show undo message
               // Snackbar snackbar = Snackbar.make(recyclerView, name + "\'s delivery finished", Snackbar.LENGTH_LONG);
               /* snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // undo is selected, restore the deleted item
                        //branchOrdersInRestaurant.child(deletedReservationId).setValue(restoreItem(deletedItem));
                        branchOrdersIncoming.child(deletedReservationId).setValue(restoreItem(deletedItem));
                        branchOrdersInPreparation.child(deletedReservationId).removeValue();
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();*/



            } else if (direction == ItemTouchHelper.LEFT)
            {
                String restaurantID= "EeEfwV4KAPRYrUk4NJXj052LqXh1";
                final DatabaseReference branchOrdersInRestaurant1 = database.child("restaurants").child(restaurantID).child("Orders").child("Ready_To_Go");
                branchOrdersInRestaurant1.child(deletedReservationId).child("status_order").setValue("in_delivery");

                // Show undo message
                Snackbar snackbar = Snackbar.make(recyclerView,   " in delivery", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {

                        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                        ValueEventListener postListener = new ValueEventListener()
                        {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                String restaurantID= "EeEfwV4KAPRYrUk4NJXj052LqXh1";
                                final DatabaseReference branchOrdersInRestaurant1 = database.child("restaurants").child(restaurantID).child("Orders").child("Ready_To_Go");
                                branchOrdersInRestaurant1.child(deletedReservationId).child("status_order").setValue(".....");
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {  }
                        };
                        mDatabase.addValueEventListener(postListener);

                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }



        }
    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo){
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setPersonOrder(reservationInfo.getPersonOrder());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setTimeReservation(reservationInfo.getTimeReservation());

        res.setRestaurantAddress(reservationInfo.getRestaurantAddress());
        res.setAddressOrder(reservationInfo.getAddressOrder());
        res.setRestaurantId(reservationInfo.getRestaurantId());

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
