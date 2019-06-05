package it.polito.mad.appcomplete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecyclerViewAdapterPopFood extends
        RecyclerView.Adapter<RecyclerViewAdapterPopFood.PopFoodViewHolder> {
    private static final String TAG = "RecyclerViewAdapterPopF";

    private Context myContext;
    private List<FoodInfo> popFood;
    private List<Integer> progress;

    public RecyclerViewAdapterPopFood(Context myContext, List<FoodInfo> popFood, List<Integer> progress) {
        this.myContext = myContext;
        this.popFood = popFood;
        this.progress = progress;
    }

    @NonNull
    @Override
    public PopFoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pop_food_card_layout,
                viewGroup, false);

        PopFoodViewHolder holder = new PopFoodViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull PopFoodViewHolder popFoodViewHolder, int i) {
        try{
            Picasso.get().load(popFood.get(i).getImage()).into(popFoodViewHolder.pic);
            popFoodViewHolder.name.setText(popFood.get(i).getName());
            popFoodViewHolder.progressBar.setProgress(progress.get(i));
            popFoodViewHolder.progress.setText(String.valueOf(progress.get(i)));
        } catch (NullPointerException nEx){
            Log.w(TAG, "onBindViewHolder: ", nEx);
        }
    }

    @Override
    public int getItemCount() {
        return popFood.size();
    }

    public class PopFoodViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        ProgressBar progressBar;
        ImageView pic;
        TextView progress;
        RelativeLayout popFoodLayoutItem;

        public PopFoodViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.popFoodName);
            pic = itemView.findViewById(R.id.popFoodPicture);
            progressBar = itemView.findViewById(R.id.horizontalProgressBar);
            progress = itemView.findViewById(R.id.progress);
            popFoodLayoutItem = itemView.findViewById(R.id.layout_popFoodCardView_item);
        }
    }
}
