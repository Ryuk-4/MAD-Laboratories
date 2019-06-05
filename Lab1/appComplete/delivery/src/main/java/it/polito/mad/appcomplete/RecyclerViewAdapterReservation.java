package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecyclerViewAdapterReservation extends RecyclerView.Adapter<RecyclerViewAdapterReservation.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapterRese";
    SharedPreferences preferences;
    private Context myContext;
    private List<ReservationInfo> reservationInfoList;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList){
        this.myContext = myContext;
        this.reservationInfoList = reservationInfoList;

        if (myContext != null){
            preferences = myContext.getSharedPreferences("loginState", Context.MODE_PRIVATE);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reservation_card_layout, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Log.d(TAG, "onBindViewHolder: called");

        viewHolder.name.setText(reservationInfoList.get(i).getNamePerson());
//        viewHolder.time.setText(reservationInfoList.get(i).getTimeReservation());
        viewHolder.restaurantAddress.setText(reservationInfoList.get(i).getRestaurantAddress());
        //viewHolder.custommerAddress.setText(reservationInfoList.get(i).getAddressOrder());


        Geocoder geocoder = new Geocoder(myContext, Locale.getDefault());


        List<Address> addresses = new ArrayList<>();
        try {
            addresses = geocoder.getFromLocation( Double.parseDouble( reservationInfoList.get(i).getcLatitude()), Double.parseDouble( reservationInfoList.get(i).getcLongitude()),1);
            viewHolder.custommerAddress.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e){
            Log.w(TAG, "onDataChange: ", e);
        }


        Geocoder geocoder1 = new Geocoder(myContext, Locale.getDefault());
        List<Address> addresses1 = new ArrayList<>();
        try {
            addresses1 = geocoder.getFromLocation( Double.parseDouble( reservationInfoList.get(i).getrLatitude()),Double.parseDouble( reservationInfoList.get(i).getrLongitude()), 1);
            viewHolder.restaurantAddress.setText(addresses1.get(0).getAddressLine(0));
        } catch (Exception e){
            Log.w(TAG, "onDataChange: ", e);
        }


        //viewHolder.restaurantAddress.setText(reservationInfoList.get(i).getRestaurantAddress());


        viewHolder.cardContainer.setOnClickListener(new customOnClickListener(viewHolder));
    }

    @Override
    public int getItemCount() {
        return reservationInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{ //implements View.OnClickListener {

        TextView name;
        TextView time;
        TextView order;
        TextView note;
        RelativeLayout reservationLayoutItem;
        TextView restaurantAddress;
        TextView custommerAddress;
        CardView cardContainer;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.person_name);
            restaurantAddress = itemView.findViewById(R.id.restaurantAddress);
            custommerAddress = itemView.findViewById(R.id.custommerAddress);
            cardContainer = itemView.findViewById(R.id.cardViewReservation);

            reservationLayoutItem = itemView.findViewById(R.id.layout_reservationCardView_item);
        }
    }

    class customOnClickListener implements View.OnClickListener
    {
        private ViewHolder viewHolder;

        customOnClickListener(ViewHolder viewHolder){
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {

            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());  // backup of removed item for undo purpose

            Bundle extras = new Bundle();

            extras.putString("res_Lat",deletedItem.getrLatitude());
            extras.putString("res_Lon",deletedItem.getrLongitude());
            extras.putString("cus_Lat",deletedItem.getcLatitude());
            extras.putString("cus_Lon",deletedItem.getcLongitude());
            Intent intent = new Intent (v.getContext(), MapsActivity.class);
            intent.putExtras(extras);
            myContext.startActivity(intent);
        }
    }


}
