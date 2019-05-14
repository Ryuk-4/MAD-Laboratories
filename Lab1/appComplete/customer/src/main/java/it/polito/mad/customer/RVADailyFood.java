package it.polito.mad.customer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class RVADailyFood extends RecyclerView.Adapter<RVADailyFood.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<SuggestedFoodInfo> foodInfoList;
    private RVANormalRestaurant.updateRestaurantList updateRestaurantList;

    public RVADailyFood(Context myContext, List<SuggestedFoodInfo> foodInfoList){
        this.myContext = myContext;
        this.foodInfoList = foodInfoList;
    }

    @Override
    public RVADailyFood.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_daily_food, viewGroup, false);
        RVADailyFood.ViewHolder holder = new RVADailyFood.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RVADailyFood.ViewHolder viewHolder, final int i) {
        viewHolder.name.setText(foodInfoList.get(i).getName());
        viewHolder.price.setText(foodInfoList.get(i).getPrice());
        viewHolder.mItemDescription.setText("''"+foodInfoList.get(i).getDescription()+"''");

        SharedPreferences sharedPreferences = myContext.getSharedPreferences("orders_info", Context.MODE_PRIVATE);
        int n_food = sharedPreferences.getInt("n_food", 0);

        for (int iter = 0 ; iter < n_food ; iter++)
        {
            String name = sharedPreferences.getString("food"+iter, "");

            if (name.compareTo(foodInfoList.get(i).getName()) == 0)
            {
                String amount = sharedPreferences.getString("amount"+iter, "");
                viewHolder.amount.setText(amount);
            }
        }

        Picasso.get().load(foodInfoList.get(i).getImageUrl()).into(viewHolder.photo);
    }

    @Override
    public int getItemCount() {

        return foodInfoList.size();
    }

    public void removeItem(int position) {
        foodInfoList.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        notifyItemRemoved(position);
    }

    public void restoreItem(SuggestedFoodInfo item, int position) {
        foodInfoList.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }


    // inner clas to manage the view
    public class ViewHolder extends RecyclerView.ViewHolder{ //implements View.OnClickListener {

        TextView name;
        TextView price;
        TextView mItemDescription;
        ImageView photo, expandCollapse;
        TextView amount;
        ImageButton plus_btn, minus_btn;


        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.food_name);
            photo = itemView.findViewById(R.id.daily_food_image);
            amount = itemView.findViewById(R.id.amount_food);
            plus_btn = itemView.findViewById(R.id.plus_button);
            minus_btn = itemView.findViewById(R.id.minus_button);
            price = itemView.findViewById(R.id.food_price);
            expandCollapse = itemView.findViewById(R.id.item_description_img);
            mItemDescription = itemView.findViewById(R.id.food_description);

            expandCollapse.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapseExpandTextView();
                }
            });

            plus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = Integer.parseInt(amount.getText().toString());
                    i++;
                    amount.setText(Integer.toString(i));

                    SharedPreferences sharedPreferences = myContext.getSharedPreferences("orders_info", Context.MODE_PRIVATE);
                    int n_food = sharedPreferences.getInt("n_food", 0);
                    int iter = 0;
                    String food;

                    for (; iter < n_food ; iter++)
                    {
                        food = sharedPreferences.getString("food"+iter, "");
                        if (food.compareTo(name.getText().toString()) == 0)
                            break;
                    }

                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("food"+iter, name.getText().toString());
                    editor.putString("amount"+iter, amount.getText().toString());
                    editor.putString("price"+iter, price.getText().toString());

                    if (iter == n_food)
                    {
                        n_food++;
                        editor.putInt("n_food", n_food);
                    }

                    editor.apply();
                }
            });

            minus_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = Integer.parseInt(amount.getText().toString());
                    if (i != 0)
                    {
                        i--;
                        amount.setText(Integer.toString(i));

                        SharedPreferences sharedPreferences = myContext.getSharedPreferences("orders_info", Context.MODE_PRIVATE);
                        int n_food = sharedPreferences.getInt("n_food", 0);
                        int iter = 0;
                        String food;
                        SharedPreferences.Editor editor = sharedPreferences.edit();

                        for (; iter < n_food ; iter++)
                        {
                            food = sharedPreferences.getString("food"+iter, "");
                            if (food.compareTo(name.getText().toString()) == 0)
                            {
                                editor.putString("amount"+iter, amount.getText().toString());
                                editor.commit();
                                break;
                            }
                        }
                    }


                }
            });
        }

        void collapseExpandTextView() {
            if (mItemDescription.getVisibility() == View.GONE) {
                // it's collapsed - expand it
                mItemDescription.setVisibility(View.VISIBLE);
                expandCollapse.setImageResource(R.drawable.round_expand_less_black_48);
            } else {
                // it's expanded - collapse it
                mItemDescription.setVisibility(View.GONE);
                expandCollapse.setImageResource(R.drawable.round_expand_more_black_48);
            }

            ObjectAnimator animation = ObjectAnimator.ofInt(mItemDescription, "maxLines", mItemDescription.getMaxLines());
            animation.setDuration(400).start();
        }
    }
}
