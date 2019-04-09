package it.polito.mad.appcomplete;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import java.util.List;


public class PreparingReservationFragment extends Fragment
        implements RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {

    private static final String TAG = "PreparingReservation";

    private List<ReservationInfo> reservationPreparingList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;

    private ReservationActivityInterface resActivityInterface;

    public PreparingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + TAG);
        reservationPreparingList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);
        Log.d(TAG, "onCreateView: " + TAG);

        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);
        initializeRecyclerViewReservation();

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        resActivityInterface = (ReservationActivityInterface) getActivity();
        Log.d(TAG, "onAttach: " + TAG);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        resActivityInterface = null;
    }

    public void newReservationHasSent(ReservationInfo reservation) {
        //if(reservationPreparingList.size())
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
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.RIGHT, this, getActivity(), false);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    // TODO: Add dialog to confirm the action of the user
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
}
