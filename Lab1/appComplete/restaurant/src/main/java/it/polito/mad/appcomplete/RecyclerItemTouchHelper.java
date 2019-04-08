package it.polito.mad.appcomplete;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private RecyclerItemTouchHelperListener listener;
    private Context myContext;
    private Drawable icon_delete;
    private Drawable icon_insert;
    private TextPaint textPaintDelete;

    private final ColorDrawable background_insert;
    private final String delete_text;
    private final String preparation_text;
    private final ColorDrawable background_delete;

    /*
     *  TODO: Fix the margin of LeftSwipe image
     */
    public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener,
                                   Context myContext) {
        super(dragDirs, swipeDirs);
        this.listener = listener;
        this.myContext = myContext;

        background_delete = new ColorDrawable(ContextCompat.getColor(myContext, R.color.bg_row_background));
        background_insert = new ColorDrawable(ContextCompat.getColor(myContext, R.color.green_row_background));

        icon_delete = ContextCompat.getDrawable(myContext, R.drawable.baseline_delete_white_24dp);
        delete_text = myContext.getString(R.string.delete);

        icon_insert = ContextCompat.getDrawable(myContext, R.drawable.baseline_move_to_inbox_white_24dp);
        preparation_text = myContext.getString(R.string.cook);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder viewHolder1) {
        // used for up and down movements
        return false;
    }

    @Override
    public void onChildDrawOver(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 60;
        int backgroundTopOffset = 20;
        int backgroundBottomOffset = 20;


        int textSize = 25;

        if(actionState != ItemTouchHelper.ACTION_STATE_SWIPE) return;

        if (dX > 0){ // Swipe: Left ----> Right
                     // Action: Move one element in another tab

            int iconSize = icon_insert.getIntrinsicHeight();
            int halfIcon = iconSize / 2;
            int iconTop = viewHolder.itemView.getTop() + ((viewHolder.itemView.getBottom() - viewHolder.itemView.getTop()) / 2 - halfIcon);

            //int iconTop = itemView.getTop() + (itemView.getHeight() - icon_insert.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon_insert.getIntrinsicHeight();
            //int iconMargin = icon_insert.getIntrinsicHeight();
            //int iconLeft = 100;
            //int iconRight = 100 + icon_insert.getIntrinsicWidth();



            background_insert.setBounds(viewHolder.itemView.getLeft() + backgroundCornerOffset,
                    viewHolder.itemView.getTop() + backgroundTopOffset,
                    viewHolder.itemView.getLeft() + (int) dX + backgroundCornerOffset,
                    viewHolder.itemView.getBottom() - backgroundBottomOffset);

            background_insert.draw(c);

            icon_insert.setBounds(50, iconTop, icon_insert.getIntrinsicWidth(), iconBottom);
            icon_insert.draw(c);



            TextPaint textPaint = new TextPaint();
            textPaint.setAntiAlias(true);
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 25, myContext.getResources().getDisplayMetrics()));
            textPaint.setColor(ContextCompat.getColor(myContext, R.color.swipe_text));
            textPaint.setTypeface(Typeface.SANS_SERIF);

            int textTop = (int) (viewHolder.itemView.getTop() + ((viewHolder.itemView.getBottom() - viewHolder.itemView.getTop()) / 2 ) + textPaint.getTextSize()/2);
            c.drawText(preparation_text, 25 + iconSize + (iconSize > 0 ? 8 : 0), textTop,textPaint);

        } else if(dX < 0){

            int iconTop = itemView.getTop() + (itemView.getHeight() - icon_delete.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon_delete.getIntrinsicHeight();
            int iconMargin = icon_delete.getIntrinsicHeight()/2;
            int iconLeft = itemView.getRight() - iconMargin - icon_delete.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;

            background_delete.setBounds(viewHolder.itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    viewHolder.itemView.getTop() + backgroundTopOffset, viewHolder.itemView.getRight() - backgroundCornerOffset,
                    viewHolder.itemView.getBottom() - backgroundBottomOffset);

            background_delete.draw(c);

            icon_delete.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            icon_delete.draw(c);

            textPaintDelete = new TextPaint();
            textPaintDelete.setAntiAlias(true);
            textPaintDelete.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, myContext.getResources().getDisplayMetrics()));
            textPaintDelete.setColor(ContextCompat.getColor(myContext, R.color.swipe_text));
            textPaintDelete.setTypeface(Typeface.SANS_SERIF);

            float width = textPaintDelete.measureText(delete_text);
            int textTop = (int) (viewHolder.itemView.getTop() + ((viewHolder.itemView.getBottom() - viewHolder.itemView.getTop()) / 2) + textPaintDelete.getTextSize() / 2);

            c.drawText(delete_text, iconLeft - width - ( iconLeft == viewHolder.itemView.getRight() ? 16 : 8 ), textTop, textPaintDelete);
        }


    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
    }

    public interface RecyclerItemTouchHelperListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
    }
}
