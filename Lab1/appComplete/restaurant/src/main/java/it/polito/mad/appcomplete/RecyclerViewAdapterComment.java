package it.polito.mad.appcomplete;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class RecyclerViewAdapterComment extends
        RecyclerView.Adapter<RecyclerViewAdapterComment.CommentViewHolder> {

    private Context myContext;
    private List<Comment> comments;

    public RecyclerViewAdapterComment(Context myContext, List<Comment> comments) {
        this.myContext = myContext;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_restaurant_card_layout,
                viewGroup, false);

        CommentViewHolder holder = new CommentViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder commentViewHolder, int i) {
        commentViewHolder.title.setText(comments.get(i).getTitle());
        commentViewHolder.ratingBar.setRating((Float.valueOf(comments.get(i).getStars())));
        commentViewHolder.date.setText(comments.get(i).getDate());
        commentViewHolder.description.setText(comments.get(i).getDescription());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {

        TextView title, date, description;
        RatingBar ratingBar;
        RelativeLayout commentLayoutItem;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.titleReview);
            ratingBar = itemView.findViewById(R.id.restRatingBar);
            date = itemView.findViewById(R.id.dateReview);
            description = itemView.findViewById(R.id.commentReview);
            commentLayoutItem = itemView.findViewById(R.id.layout_restCommentCardView_item);
        }
    }
}
