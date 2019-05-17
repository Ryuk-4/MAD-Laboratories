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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeReservation() {
        preferences = getActivity().getSharedPreferences("loginState", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference branchOrdersIncoming = database.child("delivery/" + preferences.getString("Uid", "") + "/Orders/Incoming");

        //Seed db
        //Row1
            /*String orderID="-Le15r_browa374g4qzn";
            branchOrdersIncoming.child(orderID).child("restaurantId").setValue("EeEfwV4KAPRYrUk4NJXj052LqXh1");
            branchOrdersIncoming.child(orderID).child("orderID").setValue(orderID);
            branchOrdersIncoming.child(orderID).child("timeReservation").setValue("12:89");
            branchOrdersIncoming.child(orderID).child("addressOrder").setValue("custAdd");
            branchOrdersIncoming.child(orderID).child("restaurantAddress").setValue("restAdd");*/

        branchOrdersIncoming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationInfoList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationInfoList.add(restoreItem(value));
                }

                //for notification
                DatabaseReference branchOrders = database.child("delivery/" + preferences.getString("Uid", "") + "/Orders/");

                if (reservationInfoList.size() == 0) {
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
                } catch (NullPointerException e) {
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

    private void initializeRecyclerViewReservation()
    {
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

            //final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            //final DatabaseReference IncomingBranch = mDatabase.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("Incoming");

            /*final DatabaseReference branchOrdersIncoming = database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming");
            branchOrdersIncoming.child(deletedReservationId).removeValue();*/

            if (direction == ItemTouchHelper.RIGHT)
            {
                // Add to finished branch:
                database.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("finished").child(deletedReservationId).setValue(restoreItem(deletedItem));

                // Add delivered flag to restaurant:
                database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("delivered");

                //Removing order from incoming branch:
                database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming").child(deletedReservationId).removeValue();

                // Show undo message
                Snackbar snackbar = Snackbar.make(recyclerView,   " delivery finished", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Add to incoming branch:
                        database.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("finished").child(deletedReservationId).removeValue();

                        // Add return back flag to it's default:
                        database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("in_delivery");

                        //Removing order from finished branch:
                        database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming").child(deletedReservationId).setValue(restoreItem(deletedItem));
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
            else if (direction == ItemTouchHelper.LEFT)
            {
                // Add delivered flag to restaurant:
                database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("in_delivery");

                initializeRecyclerViewReservation();
                // Show undo message
                Snackbar snackbar = Snackbar.make(recyclerView,   " in delivery", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Add delivered flag to restaurant:
                        database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("Ready_To_Go");
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }


        }
    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setPersonOrder(reservationInfo.getPersonOrder());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setTimeReservation(reservationInfo.getTimeReservation());

       // res.setRestaurantAddress(reservationInfo.getRestaurantAddress());
        //res.setAddressOrder(reservationInfo.getAddressOrder());
        res.setRestaurantId(reservationInfo.getRestaurantId());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setrLatitude(reservationInfo.getrLatitude());
        res.setrLongitude(reservationInfo.getrLongitude());


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
