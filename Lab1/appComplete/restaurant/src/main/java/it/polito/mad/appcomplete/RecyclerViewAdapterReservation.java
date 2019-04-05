package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapterReservation extends RecyclerView.Adapter<RecyclerViewAdapterReservation.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<ReservationInfo> reservationInfoList;
    private OnReservationListener onReservationListener;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList,
                                          OnReservationListener onReservationListener) {
        this.myContext = myContext;
        this.reservationInfoList = reservationInfoList;
        this.onReservationListener = onReservationListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reservation_card_layout, viewGroup, false);
        ViewHolder holder = new ViewHolder(view, onReservationListener);
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
    }

    @Override
    public int getItemCount() {
        return reservationInfoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView time;
        TextView address;
        TextView phone;
        TextView email;
        RelativeLayout viewBackground;
        FrameLayout reservationLayoutItem;

        OnReservationListener onReservationListener;

        public ViewHolder(@NonNull View itemView, OnReservationListener onReservationListener) {
            super(itemView);

            name = itemView.findViewById(R.id.person_name);
            time = itemView.findViewById(R.id.reservation_time);
            address = itemView.findViewById(R.id.person_address);
            phone = itemView.findViewById(R.id.person_phone);
            email = itemView.findViewById(R.id.person_email);
            viewBackground = itemView.findViewById(R.id.view_background);
            reservationLayoutItem = itemView.findViewById(R.id.cardViewReservation);
            this.onReservationListener = onReservationListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onReservationListener.OnReservationClick(getAdapterPosition());
        }
    }

    public void addItem(ReservationInfo item, int position){
        reservationInfoList.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
           reservationInfoList.remove(position);
            // notify the item removed by position
            // to perform recycler view delete animations
            notifyItemRemoved(position);
    }

    public void restoreItem(ReservationInfo item, int position) {
        reservationInfoList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    public interface OnReservationListener {
        void OnReservationClick(int position);
    }
}
