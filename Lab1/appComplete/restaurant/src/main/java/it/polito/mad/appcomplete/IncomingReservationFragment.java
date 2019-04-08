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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IncomingReservationFragment extends Fragment
        implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener{

    private static final String TAG = "IncomingReservation";
    
    private List<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.incoming_reservation_fragment, container, false);

        initializeReservation(view);

        return view;
    }

    private void initializeReservation(View view) {
        reservationInfoList = new ArrayList<>();

        reservationInfoList.add(new ReservationInfo("John", "20:00",
                "pizza"));

        reservationInfoList.add(new ReservationInfo("Jane", "20:30",
               "diavola", "With french fries"));

        reservationInfoList.add(new ReservationInfo("Lucy", "19:30",
                "pasta", "no tomatoes"));
        reservationInfoList.add(new ReservationInfo("Mary", "19:45",
                "Chicken"));

        initializeRecyclerViewReservation(view);
    }

    private void initializeRecyclerViewReservation(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);

        myAdapter = new RecyclerViewAdapterReservation(getActivity(),
                reservationInfoList/*, this*/);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, getActivity());

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    /*
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */

    // TODO: Add dialog to confirm the action of the user
    // TODO: Distinguish which direction is the swipe in order to perform the right action
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            final int deletedIndex = viewHolder.getAdapterPosition();
            Log.d(TAG, "onSwiped: deletedIndex " + deletedIndex);
            // remove the item from recycler view

            myAdapter.removeItem(viewHolder.getAdapterPosition());

            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + "\'s reservation removed", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    myAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
