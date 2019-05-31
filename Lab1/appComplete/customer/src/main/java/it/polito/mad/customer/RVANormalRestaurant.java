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

import com.mikhaellopez.circularimageview.CircularImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class RVANormalRestaurant extends RecyclerView.Adapter<RVANormalRestaurant.ViewHolder>{

    private Context myContext;
    private List<RestaurantInfo> restaurantInfoList;
    private OnRestaurantListener onRestaurantListener;
    private RVAFavoriteRestaurant rvaFavoriteRestaurant;
    private RVASuggestedRestaurant rvaSuggestedRestaurant;

    public RVANormalRestaurant(Context myContext, OnRestaurantListener restaurantListener, RVAFavoriteRestaurant rvaFavoriteRestaurant){
        this.myContext = myContext;
        this.restaurantInfoList = new ArrayList<>();
        this.onRestaurantListener = restaurantListener;
        this.rvaFavoriteRestaurant = rvaFavoriteRestaurant;

        FirebaseUtils.setupFirebaseCustomer();
    }

    @NonNull
    @Override
    public RVANormalRestaurant.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_normal_restaurant, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RVANormalRestaurant.ViewHolder viewHolder, final int i) {
        List<String> typeFood = restaurantInfoList.get(i).getTypeOfFood();

        viewHolder.name.setText(restaurantInfoList.get(i).getName());
        viewHolder.review.setText(restaurantInfoList.get(i).getVotesString());

        viewHolder.star.setTag(restaurantInfoList.get(i).getId());
        if (restaurantInfoList.get(i).isFavorite())
        {
            Bitmap bitmap = ((BitmapDrawable) Objects.requireNonNull(myContext.getDrawable(R.drawable.baseline_star_black_36))).getBitmap();
            (viewHolder.star).setImageBitmap(bitmap);
        } else
        {
            Bitmap bitmap = ((BitmapDrawable) Objects.requireNonNull(myContext.getDrawable(R.drawable.baseline_star_border_black_36))).getBitmap();
            (viewHolder.star).setImageBitmap(bitmap);
        }

        viewHolder.star.setOnClickListener(new customOnClick(restaurantInfoList.get(i)));

        if (viewHolder.type.getChildCount() == 0)
        {
            for (String s : typeFood)
            {
                TextView t = new TextView(this.myContext);
                t.setText(s);
                t.setTextColor(this.myContext.getColor(R.color.colorPrimary));
                t.setTypeface(null, Typeface.BOLD);
                t.setPadding(10, 10, 10, 10);

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 5, 0);
                viewHolder.type.addView(t, layoutParams);
            }
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

    public void restoreItem(RestaurantInfo item, int position) {
        restaurantInfoList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    public void addAdapter(RVASuggestedRestaurant myAdapterSuggested) {
        rvaSuggestedRestaurant = myAdapterSuggested;
    }

    public void setItemFavorite(String id) {
        for (RestaurantInfo restaurantInfo : restaurantInfoList)
        {
            if (restaurantInfo.getId().compareTo(id) == 0)
            {
                restaurantInfo.setFavorite(true);
            }
        }
    }

    public void setItemNotFavorite(String id) {
        for (RestaurantInfo restaurantInfo : restaurantInfoList)
        {
            if (restaurantInfo.getId().compareTo(id) == 0)
            {
                restaurantInfo.setFavorite(false);
            }
        }
    }


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

            cv.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onRestaurantListener.OnRestaurantClick(itemView.findViewById(R.id.restaurant_image_normal).getContentDescription().toString(), ((TextView) itemView.findViewById(R.id.restaurant_name)).getText().toString());
        }
    }

    class customOnClick implements View.OnClickListener
    {
        private RestaurantInfo restaurantInfo;

        customOnClick(RestaurantInfo restaurantInfo)
        {
            this.restaurantInfo = restaurantInfo;
        }

        @Override
        public void onClick(View v) {
            Bitmap bitmap = ((BitmapDrawable)((CircularImageView)v).getDrawable()).getBitmap();
            Bitmap bitmap2 = ((BitmapDrawable) Objects.requireNonNull(myContext.getDrawable(R.drawable.baseline_star_border_black_36))).getBitmap();
            String restId = v.getTag().toString();

            if(bitmap == bitmap2)
            {
                ((CircularImageView) v).setImageBitmap(((BitmapDrawable) Objects.requireNonNull(myContext.getDrawable(R.drawable.baseline_star_black_36))).getBitmap());
                FirebaseUtils.branchCustomerFavoriteRestaurant.child(restId).setValue("true");
                restaurantInfo.setFavorite(true);
                rvaFavoriteRestaurant.restoreItem(restaurantInfo, rvaFavoriteRestaurant.getItemCount());
                rvaSuggestedRestaurant.setItemFavorite(restaurantInfo.getId());

            } else
            {
                ((CircularImageView) v).setImageBitmap(((BitmapDrawable) Objects.requireNonNull(myContext.getDrawable(R.drawable.baseline_star_border_black_36))).getBitmap());
                FirebaseUtils.branchCustomerFavoriteRestaurant.child(restId).removeValue();
                restaurantInfo.setFavorite(false);
                rvaFavoriteRestaurant.removeItem(restaurantInfo);
                rvaSuggestedRestaurant.setItemNotFavorite(restaurantInfo.getId());
            }

            rvaFavoriteRestaurant.notifyDataSetChanged();
            rvaSuggestedRestaurant.notifyDataSetChanged();
        }
    }
}
