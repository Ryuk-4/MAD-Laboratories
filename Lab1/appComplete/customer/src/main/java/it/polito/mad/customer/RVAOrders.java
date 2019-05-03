package it.polito.mad.customer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        Map<String, Integer> productQuantity = ordersInfos.get(i).getFoodAmount();
        Map<String, Float> productPrice = ordersInfos.get(i).getFoodPrice();

        for (String s : productPrice.keySet())
        {
            LinearLayout linearLayout = new LinearLayout(myContext);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView name = new TextView(myContext);
            name.setText(s);

            EditText quantity = new EditText(myContext);
            quantity.setHint(productQuantity.get(s).toString());

            TextView textView = new TextView(myContext);
            textView.setText("pcs");

            linearLayout.addView(name);
            linearLayout.addView(quantity);
            linearLayout.addView(textView);

            viewHolder.foodOrderList.addView(linearLayout);
        }

        Button button = new Button(myContext);
        button.setTag(ordersInfos.get(i).getRestaurantId() + " " + ordersInfos.get(i).getOrderId());
        button.setText("SUBMIT");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tag = v.getTag().toString();
                String restaurantId = tag.split(" ")[0];
                String orderId = tag.split(" ")[1];

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("restaurants").child(restaurantId).child("Orders").child("Incoming").child(orderId);

                int count = viewHolder.foodOrderList.getChildCount();
                StringBuffer stringBuffer = new StringBuffer("");

                for (int i = 0 ; i < count ; i++)
                {
                    LinearLayout view = (LinearLayout) viewHolder.foodOrderList.getChildAt(i);

                    String name = ((TextView) view.getChildAt(0)).getText().toString();
                    String quantity = ((EditText) view.getChildAt(1)).getText().toString();

                    for (int j = 0 ; j < Integer.parseInt(quantity) ; j++)
                        stringBuffer.append(name+", ");

                }

                databaseReference.child("personOrder").setValue(stringBuffer.toString());
            }
        });

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


    // inner clas to manage the view
    public class ViewHolder extends RecyclerView.ViewHolder{ //implements View.OnClickListener {
        LinearLayout foodOrderList;
        TextView restaurantName;
        TextView orderTime;
        TextView orderAddress;
        CardView cardView;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            foodOrderList = itemView.findViewById(R.id.orders_list);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            orderAddress = itemView.findViewById(R.id.order_address);
            orderTime = itemView.findViewById(R.id.order_time);
            cardView = itemView.findViewById(R.id.cv_order);

            cardView.setOnClickListener(new View.OnClickListener() {
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
}
