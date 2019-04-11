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


public class PreparingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "PreparingReservation";

    private ArrayList<ReservationInfo> reservationPreparingList;

    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private ReservationActivityInterface resActivityInterface;

    public PreparingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            reservationPreparingList = new ArrayList<>();
            //;
        } else {
            reservationPreparingList = (ArrayList<ReservationInfo>) savedInstanceState.getSerializable("prepReservations");
        }
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

        initializeRecyclerViewReservation();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("prepReservations", reservationPreparingList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        resActivityInterface = (ReservationActivityInterface) getActivity();
        Log.d(TAG, "onAttach: called");
    }

    @Override
    public void onDetach() {
        super.onDetach();

        resActivityInterface = null;
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

    public void newReservationHasSent(ReservationInfo reservation) {
        reservationPreparingList.add(reservation);
        Log.d(TAG, "newReservationHasSent: ");
        initializeRecyclerViewReservation();

    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(),
                reservationPreparingList);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                ItemTouchHelper.RIGHT, this, getActivity(), false);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = reservationPreparingList.get(viewHolder.getAdapterPosition()).getNamePerson();

            // backup of removed item for undo purpose
            final ReservationInfo deletedItem = reservationPreparingList.get(viewHolder.getAdapterPosition());
            Log.d(TAG, "onSwiped: deletedItem " + deletedItem);
            final int deletedIndex = viewHolder.getAdapterPosition();
            Log.d(TAG, "onSwiped: deletedIndex " + deletedIndex);
            // remove the item from recycler view

            myAdapter.removeItem(viewHolder.getAdapterPosition());


            resActivityInterface.processReservation(getString(R.string.tab_preparation), deletedItem);

            Snackbar snackbar = Snackbar
                    .make(recyclerView, name + "\'s reservation ready to go", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // undo is selected, restore the deleted item
                    myAdapter.restoreItem(deletedItem, deletedIndex);
                    resActivityInterface.undoOperation(getString(R.string.tab_preparation));
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    public void removeItem() {
        reservationPreparingList.remove(reservationPreparingList.size() - 1);
        initializeRecyclerViewReservation();
    }

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }
}
