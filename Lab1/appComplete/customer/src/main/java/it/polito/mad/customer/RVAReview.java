package it.polito.mad.customer;

import android.content.Context;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;


import java.util.List;

public class RVAReview extends RecyclerView.Adapter<RVAReview.ViewHolder> {

    private Context myContext;
    private List<ReviewInfo> reviewInfos;


    public RVAReview(Context myContext, List<ReviewInfo> reviewInfos){
        this.myContext = myContext;
        this.reviewInfos = reviewInfos;
    }

    @NonNull
    @Override
    public RVAReview.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_review, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RVAReview.ViewHolder viewHolder, final int i) {
        viewHolder.date.setText(reviewInfos.get(i).getDate());
        viewHolder.title.setText(reviewInfos.get(i).getTitle());
        viewHolder.description.setText(reviewInfos.get(i).getDescription());
        viewHolder.rating.setRating(Float.parseFloat(reviewInfos.get(i).getRate()));
    }

    @Override
    public int getItemCount() {
        return reviewInfos.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        RatingBar rating;
        TextView date;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            rating = itemView.findViewById(R.id.ratingBar);
            date = itemView.findViewById(R.id.date);

        }
    }
}
