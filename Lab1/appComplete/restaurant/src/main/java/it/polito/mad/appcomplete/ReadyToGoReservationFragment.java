package it.polito.mad.appcomplete;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class ReadyToGoReservationFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "ReservationReadyToGo";

    private ArrayList<ReservationInfo> reservationReadyToGoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private ReservationActivityInterface resActivityInterface;

    public ReadyToGoReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            reservationReadyToGoList = new ArrayList<>();
        } else {
            reservationReadyToGoList = (ArrayList<ReservationInfo>) savedInstanceState.getSerializable("readyReservations");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_readytogo_reservation, container, false);
        Log.d(TAG, "onCreateView: called");

        recyclerView = view.findViewById(R.id.recyclerViewReadyToGoReservation);

        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(this);

        initializeRecyclerViewReservation();

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable("readyReservations", reservationReadyToGoList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        resActivityInterface = (ReservationActivityInterface)getActivity();
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

        if(id == R.id.menu_refresh){

            /*
             * TODO: mySwipeRefreshLayout.setRefreshing(true);
             * TODO: myUpdateOP.
             */
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void newReservationHasSent(ReservationInfo reservation) {
        reservationReadyToGoList.add(reservation);
        Log.d(TAG, "newReservationHasSent: ");
        initializeRecyclerViewReservation();
    }

    private void initializeRecyclerViewReservation() {
        myAdapter = new RecyclerViewAdapterReservation(getActivity(),
                reservationReadyToGoList);

        recyclerView.setAdapter(myAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

    }

    public void removeItem(){
        reservationReadyToGoList.remove(reservationReadyToGoList.size() - 1);
        initializeRecyclerViewReservation();
    }

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }
}
