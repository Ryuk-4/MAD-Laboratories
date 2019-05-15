package it.polito.mad.appcomplete;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

    private Context myContext;
    private List<ReservationInfo> reservationInfoList;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList){
        this.myContext = myContext;
        this.reservationInfoList = reservationInfoList;
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

        viewHolder.cardContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*private DatabaseReference database;
                database = FirebaseDatabase.getInstance().getReference();*/

                Intent intent = new Intent (v.getContext(), MapsActivity.class);
                myContext.startActivity(intent);
               //new c().CallActivity();
            }
        });
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

    public class c extends Activity
    {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        public void CallActivity()
        {
            //reservationInfoList.get(1).getPersonOrder();
            Intent intent;
            //intent = new Intent(c.getContext(), MapsActivity.class);
            /*Bundle extras = new Bundle();
            extras.putString("res_Lat","my_username");
            extras.putString("res_Lon","my_password");
            extras.putString("cus_Lat","my_username");
            extras.putString("cus_Lon","my_password");
            intent.putExtras(extras);*/
           // startActivity(intent);
        }
    }
}
