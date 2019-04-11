package it.polito.mad.appcomplete;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

    private ReservationActivityInterface resActivityInterface;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: called");

        if (savedInstanceState == null) {
            initializeReservation();

        } else {

            reservationInfoList = (ArrayList<ReservationInfo>) savedInstanceState.getSerializable("incReservations");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(this);

        initializeRecyclerViewReservation(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        resActivityInterface = (ReservationActivityInterface) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        resActivityInterface = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("incReservations", reservationInfoList);
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
        reservationInfoList = new ArrayList<>();

        reservationInfoList.add(new ReservationInfo("John", "20:00",
                "pizza"));

        reservationInfoList.add(new ReservationInfo("Jane", "20:30",
                "diavola", "With french fries"));

        reservationInfoList.add(new ReservationInfo("Lucy", "19:30",
                "pasta", "no tomatoes"));
        reservationInfoList.add(new ReservationInfo("Mary", "19:45",
                "Chicken"));
    }

    private void initializeRecyclerViewReservation(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);

        myAdapter = new RecyclerViewAdapterReservation(getActivity(), reservationInfoList);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, getActivity(), true);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    /*
     * callback when recycler view is swiped
     * item will be removed on swiped
     * undo option will be provided in snackbar to restore the item
     */
    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position) {
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

            if (direction == ItemTouchHelper.RIGHT) {

                resActivityInterface.processReservation(getString(R.string.tab_incoming), deletedItem);

                Snackbar snackbar = Snackbar
                        .make(recyclerView, name + "\'s reservation in preparation", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        myAdapter.restoreItem(deletedItem, deletedIndex);
                        resActivityInterface.undoOperation(getString(R.string.tab_incoming));
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            } else if (direction == ItemTouchHelper.LEFT) {
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

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }

}
