package it.polito.mad.customer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RVANormalRestaurant extends RecyclerView.Adapter<RVANormalRestaurant.ViewHolder>{
    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<RestaurantInfo> restaurantInfoList;
    private OnRestaurantListener onRestaurantListener;

    public RVANormalRestaurant(Context myContext, OnRestaurantListener restaurantListener){
        this.myContext = myContext;
        this.restaurantInfoList = new ArrayList<>();
        this.onRestaurantListener = restaurantListener;
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
        List<String> typeFood = restaurantInfoList.get(i).getTypeOfFood();

        viewHolder.name.setText(restaurantInfoList.get(i).getName());
        viewHolder.review.setText(restaurantInfoList.get(i).getVotesString());

        viewHolder.star.setTag(restaurantInfoList.get(i).getId());
        if (restaurantInfoList.get(i).isFavorite())
        {
            Bitmap bitmap = ((BitmapDrawable)myContext.getDrawable(R.drawable.baseline_star_black_24)).getBitmap();
            ((CircularImageView) viewHolder.star).setImageBitmap(bitmap);
        } else
        {
            Bitmap bitmap = ((BitmapDrawable)myContext.getDrawable(R.drawable.baseline_star_border_black_24)).getBitmap();
            ((CircularImageView) viewHolder.star).setImageBitmap(bitmap);
        }

        for (String s : typeFood)
        {
            TextView t = new TextView(this.myContext);
            t.setText(s);
            t.setTextColor(this.myContext.getColor(R.color.colorPrimary));
            t.setTypeface(null, Typeface.BOLD);
            //t.setBackground(this.myContext.getResources().getDrawable(R.drawable.rounded_corner));
            t.setPadding(10, 10, 10, 10);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 5, 0);
            viewHolder.type.addView(t, layoutParams);
        }

        viewHolder.photo.setContentDescription(restaurantInfoList.get(i).getId());

        if (restaurantInfoList.get(i).getPhoto().compareTo("") != 0)
            Picasso.get().load(restaurantInfoList.get(i).getPhoto()).into(viewHolder.photo);

        viewHolder.ratingBar.setRating(restaurantInfoList.get(i).getValueRatinBar());
    }

    @Override
    public int getItemCount() {

        return restaurantInfoList.size();
    }

    public void clearAll()
    {
        restaurantInfoList = new ArrayList<>();
    }

    public void removeItem(int position) {
        restaurantInfoList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        notifyItemRemoved(position);
    }

    public void restoreItem(RestaurantInfo item, int position) {
        restaurantInfoList.add(position, item);
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
        CircularImageView star;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.restaurant_name);
            review = itemView.findViewById(R.id.restaurant_review);
            photo = itemView.findViewById(R.id.restaurant_image_normal);
            type = itemView.findViewById(R.id.restaurant_type);
            cv = itemView.findViewById(R.id.cv_normal_card);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            star = itemView.findViewById(R.id.star);

            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bitmap bitmap = ((BitmapDrawable)((CircularImageView)v).getDrawable()).getBitmap();
                    Bitmap bitmap2 = ((BitmapDrawable)myContext.getDrawable(R.drawable.baseline_star_border_black_24)).getBitmap();
                    String restId = v.getTag().toString();

                    if(bitmap == bitmap2)
                    {
                        ((CircularImageView) v).setImageBitmap(((BitmapDrawable)myContext.getDrawable(R.drawable.baseline_star_black_24)).getBitmap());
                        FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("favorite_restaurant").child(restId).setValue("true");
                    } else
                    {
                        ((CircularImageView) v).setImageBitmap(((BitmapDrawable)myContext.getDrawable(R.drawable.baseline_star_border_black_24)).getBitmap());
                        FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("favorite_restaurant").child(restId).removeValue();

                    }
                }
            });

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
