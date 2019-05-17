package it.polito.mad.appcomplete;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class RecyclerViewAdapterReservation extends RecyclerView.Adapter<RecyclerViewAdapterReservation.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapterRese";
    SharedPreferences preferences;
    private Context myContext;
    private List<ReservationInfo> reservationInfoList;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList){
        this.myContext = myContext;
        this.reservationInfoList = reservationInfoList;

        preferences = myContext.getSharedPreferences("loginState", Context.MODE_PRIVATE);
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
        viewHolder.time.setText(reservationInfoList.get(i).getTimeReservation());
        viewHolder.order.setText(reservationInfoList.get(i).getPersonOrder());
        viewHolder.note.setText(reservationInfoList.get(i).getNote());
        viewHolder.restaurantAddress.setText(reservationInfoList.get(i).getRestaurantAddress());
        viewHolder.custommerAddress.setText(reservationInfoList.get(i).getAddressOrder());

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
            time = itemView.findViewById(R.id.reservation_time);
            order = itemView.findViewById(R.id.reservation_plate);
            note = itemView.findViewById(R.id.reservation_note);
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
            /*if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder) {
                final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());  // backup of removed item for undo purpose
                final String deletedReservationId = deletedItem.getOrderID()
                ;



                DatabaseReference database;
                database = FirebaseDatabase.getInstance().getReference();
                database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("delivered");
                database.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("Incoming").child(deletedReservationId).get);

            }*/
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());  // backup of removed item for undo purpose

            Bundle extras = new Bundle();
            /*String res_Lat ="45.0608524";
            String res_Lon ="7.5810127";
            String cus_Lat ="45.0576305";
            String cus_Lon ="7.6896999";*/
            String res_Lat =deletedItem.getOrderID();
            String res_Lon =deletedItem.getrLongitude();
            String cus_Lat =deletedItem.getcLatitude();
            String cus_Lon =deletedItem.getcLongitude();
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
