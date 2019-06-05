package it.polito.mad.appcomplete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapterRider extends
        RecyclerView.Adapter<RecyclerViewAdapterRider.RiderViewHolder> {

    private static final String TAG = "RecyclerViewRidersAdapt";

    private Context myContext;
    private List<Riders> riders;
    private OnRiderClickListener onRiderListener;

    public RecyclerViewAdapterRider(Context myContext, List<Riders> riders, OnRiderClickListener onRiderListener) {
        this.myContext = myContext;
        this.riders = riders;
        this.onRiderListener = onRiderListener;
    }

    @Override
    public RiderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.riders_card_layout,
                viewGroup, false);

        RiderViewHolder holder = new RiderViewHolder(view, onRiderListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapterRider.RiderViewHolder viewHolder, int i) {
        Picasso.get().load(riders.get(i).getPic()).into(viewHolder.pic);
        viewHolder.name.setText(riders.get(i).getName());
        viewHolder.address.setText(riders.get(i).getAddress());
    }

    @Override
    public int getItemCount() {
        return riders.size();
    }

    public void setRiders(List<Riders> list) {
        riders = list;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public class RiderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView name;
        TextView address;
        ImageView pic;
        RelativeLayout riderLayoutItem;
        OnRiderClickListener riderClickListener;

        public RiderViewHolder(@NonNull View itemView, OnRiderClickListener riderClickListener) {
            super(itemView);

            name = itemView.findViewById(R.id.riderName);
            pic = itemView.findViewById(R.id.riderPicture);
            address = itemView.findViewById(R.id.riderAddress);
            riderLayoutItem = itemView.findViewById(R.id.layout_ridersCardView_item);
            this.riderClickListener = riderClickListener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRiderListener.riderClickListener(getAdapterPosition());
        }
    }

    public interface OnRiderClickListener {
        void riderClickListener(int position);
    }
}
