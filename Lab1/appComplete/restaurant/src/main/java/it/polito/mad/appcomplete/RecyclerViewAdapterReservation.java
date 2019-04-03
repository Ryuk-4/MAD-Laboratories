package it.polito.mad.appcomplete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapterReservation extends RecyclerView.Adapter<RecyclerViewAdapterReservation.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<ReservationInfo> reservationInfoList;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList) {
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
        viewHolder.address.setText(reservationInfoList.get(i).getAddressPerson());
        viewHolder.phone.setText(reservationInfoList.get(i).getPhonePerson());
        viewHolder.email.setText(reservationInfoList.get(i).getEmail());


        viewHolder.reservationLayoutItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: clicked on" + reservationInfoList.get(i));
            }
        });

    }

    @Override
    public int getItemCount() {
        return reservationInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView time;
        TextView address;
        TextView phone;
        TextView email;
        LinearLayout reservationLayoutItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.person_name);
            time = itemView.findViewById(R.id.reservation_time);
            address = itemView.findViewById(R.id.person_address);
            phone = itemView.findViewById(R.id.person_phone);
            email = itemView.findViewById(R.id.person_email);
            reservationLayoutItem = itemView.findViewById(R.id.layout_reservationCardView_item);
        }
    }
}
