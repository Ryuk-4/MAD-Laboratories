package it.polito.mad.appcomplete;

import android.content.Context;
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
import java.util.Map;

public class RecyclerViewAdapterReservation extends
        RecyclerView.Adapter<RecyclerViewAdapterReservation.ReservationViewHolder> {

    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<ReservationInfo> reservationInfoList;
    private OnReservationClickListener onReservationClickListener;
    private Boolean flag;

    public RecyclerViewAdapterReservation(Context myContext, List<ReservationInfo> reservationInfoList,
                                          OnReservationClickListener onReservationClickListener, Boolean flag) {
        this.myContext = myContext;
        this.reservationInfoList = reservationInfoList;
        this.onReservationClickListener = onReservationClickListener;
        this.flag = flag;
    }

    @Override
    public ReservationViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reservation_card_layout, viewGroup, false);
        ReservationViewHolder holder = new ReservationViewHolder(view, onReservationClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterReservation.ReservationViewHolder viewHolder,
                                 final int i) {
        Log.d(TAG, "onBindViewHolder: called");

        viewHolder.name.setText(reservationInfoList.get(i).getNamePerson());
        viewHolder.time.setText(reservationInfoList.get(i).getTimeReservation());
        Map<String, FoodInfo> foodInfos = reservationInfoList.get(i).getOrderList();

        String orders = "";
        int j = 0;

        for (String key : foodInfos.keySet()) {
            orders = orders.concat(foodInfos.get(key).getQuantity() + " " + foodInfos.get(key).getName());

            if (++j < foodInfos.size()) {
                orders = orders.concat(", ");
            }
        }

        viewHolder.order.setText(orders);
        viewHolder.note.setText(reservationInfoList.get(i).getNote());
    }

    @Override
    public int getItemCount() {
        return reservationInfoList.size();
    }

    public class ReservationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView time;
        TextView order;
        TextView note;
        OnReservationClickListener onReservationClickListener;
        FrameLayout reservationLayoutItem;

        public ReservationViewHolder(@NonNull View itemView,
                                     OnReservationClickListener onReservationClickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.person_name);
            time = itemView.findViewById(R.id.reservation_time);
            order = itemView.findViewById(R.id.reservation_plate);
            note = itemView.findViewById(R.id.reservation_note);
            this.onReservationClickListener = onReservationClickListener;
            reservationLayoutItem = itemView.findViewById(R.id.layout_reservationCardView_item);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RelativeLayout relativeLayout;
            if (flag) {
                onReservationClickListener.reservationClickListener(getAdapterPosition());
                relativeLayout = v.findViewById(R.id.overlappedCard);
//                TODO
                relativeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    public interface OnReservationClickListener {
        void reservationClickListener(int position);
    }
}
