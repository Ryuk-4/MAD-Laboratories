package it.polito.mad.customer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.List;
import java.util.Map;

public class RVAOrders extends RecyclerView.Adapter<RVAOrders.ViewHolder>{

    private static final String TAG = "RecyclerViewAdapterRese";

    private Context myContext;
    private List<OrdersInfo> ordersInfos;


    public RVAOrders(Context myContext, List<OrdersInfo> ordersInfos){
        this.myContext = myContext;
        this.ordersInfos = ordersInfos;
    }

    @Override
    public RVAOrders.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_orders, viewGroup, false);
        RVAOrders.ViewHolder holder = new RVAOrders.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RVAOrders.ViewHolder viewHolder, final int i) {

        viewHolder.orderTime.setText(ordersInfos.get(i).getTime());
        viewHolder.orderAddress.setText(ordersInfos.get(i).getAddress());
        viewHolder.restaurantName.setText(ordersInfos.get(i).getRestaurantName());

        if (ordersInfos.get(i).getState() == OrderState.PENDING)
        {
            Log.d("tttt", "onBindViewHolder: ");
            viewHolder.orderState.setText("PENDING");
            //viewHolder.orderState.setTextColor(0xFF9800);
            viewHolder.orderStateImage.setBackground(myContext.getDrawable(R.drawable.orange_oval));
        } else if (ordersInfos.get(i).getState() == OrderState.ACCEPTED)
        {
            viewHolder.orderState.setText("IN PREPARATION");
            //viewHolder.orderState.setTextColor(0x59ff00);
            viewHolder.orderStateImage.setBackground(myContext.getDrawable(R.drawable.green_oval));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERED)
        {
            viewHolder.orderState.setText("DELIVERED");
            //viewHolder.orderState.setTextColor(0x000000);
            viewHolder.orderStateImage.setBackground(myContext.getDrawable(R.drawable.black_oval));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERING)
        {
            viewHolder.orderState.setText("IN DELIVERY");
            //viewHolder.orderState.setTextColor(0x000000);
            viewHolder.orderStateImage.setBackground(myContext.getDrawable(R.drawable.black_oval));
        }

        Map<String, Integer> productQuantity = ordersInfos.get(i).getFoodAmount();
        Map<String, Float> productPrice = ordersInfos.get(i).getFoodPrice();

        for (String s : productPrice.keySet())
        {
            LinearLayout linearLayout = new LinearLayout(myContext);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            TextView name = new TextView(myContext);
            name.setText(s);
            name.setTextSize(18);
            name.setTypeface(null, Typeface.BOLD);
            name.setTag(ordersInfos.get(i).getFoodId().get(s));
            name.setPadding(12, 6, 0, 0);

            LinearLayout ll = new LinearLayout(myContext);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(40, 0, 0, 0);

            EditText quantity = new EditText(myContext);
            quantity.setHint(productQuantity.get(s).toString());

            if (ordersInfos.get(i).getState() != OrderState.PENDING)
                quantity.setKeyListener(null);

            TextView textView = new TextView(myContext);
            textView.setText("pcs");

            ll.addView(quantity);
            ll.addView(textView);

            linearLayout.addView(name);
            linearLayout.addView(ll);


            viewHolder.foodOrderList.addView(linearLayout);
        }

        Button button = new Button(myContext);
        button.setTag(ordersInfos.get(i).getRestaurantId() + " " + ordersInfos.get(i).getOrderId());
        button.setText("SUBMIT");

        button.setOnClickListener(new customOnClickListener(viewHolder) );

        if (ordersInfos.get(i).getState() == OrderState.PENDING)
            viewHolder.foodOrderList.addView(button);

    }

    @Override
    public int getItemCount() {
        return ordersInfos.size();
    }

    public void removeItem(int position) {
        ordersInfos.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        notifyItemRemoved(position);
    }

    public void restoreItem(OrdersInfo item, int position) {
        ordersInfos.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }


    // inner class to manage the view
    public class ViewHolder extends RecyclerView.ViewHolder{ //implements View.OnClickListener {
        LinearLayout foodOrderList;
        TextView restaurantName;
        TextView orderTime;
        TextView orderAddress;
        LinearLayout orderView;
        TextView orderState;
        ImageView orderStateImage;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            foodOrderList = itemView.findViewById(R.id.orders_list);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            orderAddress = itemView.findViewById(R.id.order_address);
            orderTime = itemView.findViewById(R.id.order_time);
            orderView = itemView.findViewById(R.id.ll_order);
            orderState = itemView.findViewById(R.id.tv_order_state);
            orderStateImage = itemView.findViewById(R.id.iv_order_state);

            orderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapseExpandListView();
                }
            });

        }

        void collapseExpandListView() {
            if (foodOrderList.getVisibility() == View.GONE) {
                // it's collapsed - expand it
                foodOrderList.setVisibility(View.VISIBLE);
                //expandCollapse.setImageResource(R.drawable.round_expand_less_black_48);
            } else {
                // it's expanded - collapse it
                foodOrderList.setVisibility(View.GONE);
                //expandCollapse.setImageResource(R.drawable.round_expand_more_black_48);
            }

            //ObjectAnimator animation = ObjectAnimator.ofInt(foodOrderList, "maxLines", foodOrderList.getMaxLines());
            //animation.setDuration(400).start();
        }
    }

    class customOnClickListener implements View.OnClickListener
    {
        private ViewHolder viewHolder;

        public customOnClickListener(ViewHolder viewHolder)
        {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            String restaurantId = tag.split(" ")[0];
            String orderId = tag.split(" ")[1];

            saveDataToRestaurant(restaurantId, orderId);
            saveDataToCustomer(orderId);
        }

        private void saveDataToCustomer(String orderId) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("customers").child(FirebaseAuth.getInstance().getUid()).child("previous_order").child(orderId).child("food");

            int count = viewHolder.foodOrderList.getChildCount();
            for (int i = 0 ; i < count-1 ; i++)
            {
                LinearLayout view = (LinearLayout) viewHolder.foodOrderList.getChildAt(i);

                //String name = ((TextView) view.getChildAt(0)).getText().toString();
                String quantity = ((EditText) ((LinearLayout)view.getChildAt(1)).getChildAt(0)).getText().toString();
                String id = ((TextView) view.getChildAt(0)).getTag().toString();

                databaseReference.child(id).child("foodQuantity").setValue(quantity);
            }
        }

        private void saveDataToRestaurant(String restaurantId, String orderId) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(restaurantId).child("Orders").child("Incoming").child(orderId);

            int count = viewHolder.foodOrderList.getChildCount();
            StringBuffer stringBuffer = new StringBuffer("");

            for (int i = 0 ; i < count-1 ; i++)
            {
                LinearLayout view = (LinearLayout) viewHolder.foodOrderList.getChildAt(i);

                String name = ((TextView) view.getChildAt(0)).getText().toString();
                String quantity = ((EditText) view.getChildAt(1)).getText().toString();

                for (int j = 0 ; j < Integer.parseInt(quantity) ; j++)
                    stringBuffer.append(name+", ");

            }

            databaseReference.child("personOrder").setValue(stringBuffer.toString());
        }
    }
}
