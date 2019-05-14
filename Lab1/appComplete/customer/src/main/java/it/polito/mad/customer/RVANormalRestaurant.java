package it.polito.mad.customer;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RVANormalRestaurant extends RecyclerView.Adapter<RVANormalRestaurant.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<RestaurantInfo> reservationInfoList;
    private OnRestaurantListener onRestaurantListener;
    private updateRestaurantList updateRestaurantList;

    public RVANormalRestaurant(Context myContext, OnRestaurantListener restaurantListener, updateRestaurantList updateRestaurantList){
        this.myContext = myContext;
        this.reservationInfoList = new ArrayList<>();
        this.onRestaurantListener = restaurantListener;
        this.updateRestaurantList = updateRestaurantList;
    }

    @Override
    public RVANormalRestaurant.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_normal_restaurant, viewGroup, false);
        RVANormalRestaurant.ViewHolder holder = new RVANormalRestaurant.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RVANormalRestaurant.ViewHolder viewHolder, final int i) {
        //Log.d(TAG, "onBindViewHolder: bind");
        List<String> typeFood = reservationInfoList.get(i).getTypeOfFood();

        viewHolder.name.setText(reservationInfoList.get(i).getName());
        viewHolder.review.setText(reservationInfoList.get(i).getVotesString());

        for (String s : typeFood)
        {
            TextView t = new TextView(this.myContext);
            t.setText(s);
            t.setTextColor(this.myContext.getResources().getColor(R.color.white));
            t.setBackground(this.myContext.getResources().getDrawable(R.drawable.rounded_corner));
            t.setPadding(10, 10, 10, 10);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 5, 0);
            viewHolder.type.addView(t, layoutParams);
        }

        viewHolder.photo.setContentDescription(reservationInfoList.get(i).getId());

        if (reservationInfoList.get(i).getPhoto().compareTo("") != 0)
            Picasso.get().load(reservationInfoList.get(i).getPhoto()).into(viewHolder.photo);

        viewHolder.ratingBar.setRating(reservationInfoList.get(i).getValueRatinBar());
        //TODO set the stars in the review
    }

    @Override
    public int getItemCount() {

        return reservationInfoList.size();
    }

    public void clearAll()
    {
        reservationInfoList = new ArrayList<>();
    }

    public void removeItem(int position) {
        reservationInfoList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        notifyItemRemoved(position);
    }

    public void restoreItem(RestaurantInfo item, int position) {
        reservationInfoList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }


    // inner clas to manage the view
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{ //implements View.OnClickListener {

        TextView name;
        TextView review;
        ImageView photo;
        RatingBar ratingBar;
        LinearLayout type;
        MaterialCardView cv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.restaurant_name);
            review = itemView.findViewById(R.id.restaurant_review);
            photo = itemView.findViewById(R.id.restaurant_image_normal);
            type = itemView.findViewById(R.id.restaurant_type);
            cv = itemView.findViewById(R.id.cv_normal_card);
            ratingBar = itemView.findViewById(R.id.ratingBar);

            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRestaurantListener.OnRestaurantClick(itemView.findViewById(R.id.restaurant_image_normal).getContentDescription().toString(), ((TextView) itemView.findViewById(R.id.restaurant_name)).getText().toString());
        }
    }


    public interface updateRestaurantList
    {
        void onUpdateListNormal();
    }
}
