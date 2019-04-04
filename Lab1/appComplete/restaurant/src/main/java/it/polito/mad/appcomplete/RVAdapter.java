package it.polito.mad.appcomplete;


import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RVAdapter extends RecyclerView.Adapter<RVAdapter.FoodInfoHolder>{

    List<FoodInfo> persons;

    @NonNull
    @Override
    public FoodInfoHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.single_daily_offer, viewGroup, false);
        FoodInfoHolder pvh = new FoodInfoHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(FoodInfoHolder personViewHolder, int i) {
        personViewHolder.foodName.setText(persons.get(i).Name);
        personViewHolder.foodPrice.setText(Integer.toString(persons.get(i).price));

        personViewHolder.foodDescription.setText(persons.get(i).description);
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

    public static class FoodInfoHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView foodName;
        TextView foodPrice;
        TextView foodDescription;
        TextView Quantity;
        ImageView foodImage;


        FoodInfoHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.card_view);
            foodName = (TextView)itemView.findViewById(R.id.foodName);
            foodPrice = (TextView)itemView.findViewById(R.id.foodPrice);
            foodDescription = (TextView)itemView.findViewById(R.id.foodDescription);
            Quantity = (TextView)itemView.findViewById(R.id.Quantity);
            foodImage = (ImageView) itemView.findViewById(R.id.foodImage);

        }
    }

    RVAdapter(List<FoodInfo> persons){
        this.persons = persons;
    }

}