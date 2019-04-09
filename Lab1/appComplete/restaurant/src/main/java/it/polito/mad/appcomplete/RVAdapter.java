package it.polito.mad.appcomplete;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.FoodInfoHolder>{

    List<FoodInfo> persons;
    private OnFoodListener onFoodListener;
    private Context myContext;

    public RVAdapter(List<FoodInfo> persons, OnFoodListener onFoodListener, Context myContext) {
        this.persons = persons;
        this.onFoodListener = onFoodListener;
        this.myContext = myContext;
    }

    @NonNull
    @Override
    public FoodInfoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_daily_offer, viewGroup, false);
        FoodInfoHolder pvh = new FoodInfoHolder(v, onFoodListener);
        return pvh;
    }

    @Override
    public void onBindViewHolder(FoodInfoHolder personViewHolder, int i) {
        personViewHolder.foodName.setText(persons.get(i).Name);
        personViewHolder.foodPrice.setText(Integer.toString(persons.get(i).price) + myContext.getString(R.string.current_money));

        //personViewHolder.foodDescription.setText(persons.get(i).description);
        personViewHolder.Quantity.setText(Integer.toString(persons.get(i).quantity));

        if (persons.get(i).image != null)
            personViewHolder.foodImage.setImageBitmap(persons.get(i).image);

    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return persons.size();
    }

    public static class FoodInfoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cv;
        TextView foodName;
        TextView foodPrice;
        TextView foodDescription;
        TextView Quantity;
        ImageView foodImage;
        RelativeLayout background;
        FrameLayout foodLayoutItem;

        OnFoodListener onFoodListener;

        FoodInfoHolder(View itemView, OnFoodListener onFoodListener) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            foodName = (TextView)itemView.findViewById(R.id.foodName);
            foodPrice = (TextView)itemView.findViewById(R.id.foodPrice);
            //foodDescription = (TextView)itemView.findViewById(R.id.foodDescription);
            Quantity = (TextView)itemView.findViewById(R.id.Quantity);
            foodImage = (ImageView) itemView.findViewById(R.id.foodImage);
            background = itemView.findViewById(R.id.view_background_food);
            foodLayoutItem = itemView.findViewById(R.id.card_view);
            this.onFoodListener = onFoodListener;

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onFoodListener.OnFoodClickFood(getAdapterPosition());
        }
    }

    public void removeItem(int position) {
        persons.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        notifyItemRemoved(position);
    }

    public void restoreItem(FoodInfo item, int position) {
        persons.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }

    RVAdapter(List<FoodInfo> persons){
        this.persons = persons;
    }
    public interface OnFoodListener{
        void OnFoodClickFood(int position);
    }
}