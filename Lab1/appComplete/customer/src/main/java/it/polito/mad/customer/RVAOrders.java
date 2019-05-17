package it.polito.mad.customer;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
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
            viewHolder.orderState.setText("PENDING");
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.colorPrimary));
        } else if (ordersInfos.get(i).getState() == OrderState.ACCEPTED)
        {
            viewHolder.orderState.setText("IN PREPARATION");
            viewHolder.orderStateView.setBackground(myContext.getDrawable(android.R.color.holo_blue_light));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERED)
        {
            viewHolder.orderState.setText("DELIVERED");
            viewHolder.orderStateView.setBackground(myContext.getDrawable(android.R.color.black));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERING)
        {
            viewHolder.orderState.setText("IN DELIVERY");
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.green));
        } else if (ordersInfos.get(i).getState() == OrderState.CANCELLED)
        {
            viewHolder.orderState.setText("REJECTED");
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.red));
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
            name.setPadding(20, 6, 0, 0);

            LinearLayout ll = new LinearLayout(myContext);
            ll.setOrientation(LinearLayout.HORIZONTAL);
            ll.setPadding(50, 0, 0, 0);

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



        if (ordersInfos.get(i).getState() == OrderState.PENDING)
        {
            Button button = new Button(myContext);
            button.setTag(ordersInfos.get(i).getRestaurantId() + " " + ordersInfos.get(i).getOrderId());
            button.setText("SUBMIT");

            button.setOnClickListener(new customOnClickListener(viewHolder) );

            viewHolder.foodOrderList.addView(button);
        }

        if (ordersInfos.get(i).getState() == OrderState.DELIVERED && !ordersInfos.get(i).isReview())
        {
            Button button = new Button(myContext);
            button.setTag(ordersInfos.get(i).getRestaurantId()+" "+ordersInfos.get(i).getOrderId());
            button.setText("REVIEW YOUR ORDER");

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String restId = v.getTag().toString().split(" ")[0];
                    String orderId = v.getTag().toString().split(" ")[1];

                    Intent intent = new Intent(myContext, ReviewActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("restId", restId);
                    bundle.putString("orderId", orderId);
                    intent.putExtras(bundle);
                    myContext.startActivity(intent);
                }
            });

            viewHolder.foodOrderList.addView(button);
        }

        if (ordersInfos.get(i).getState() == OrderState.DELIVERING)
        {
            FirebaseDatabase.getInstance().getReference("riders_position").child(ordersInfos.get(i).getDeliverymanId()).addListenerForSingleValueEvent(new myValueEventListener(viewHolder));
            viewHolder.mapView.setVisibility(View.VISIBLE);
        } else
        {
            viewHolder.mapView.setVisibility(View.GONE);
        }


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
    public class ViewHolder     extends RecyclerView.ViewHolder
                                implements OnMapReadyCallback { //implements View.OnClickListener {
        LinearLayout foodOrderList, ll_orders;
        TextView restaurantName;
        TextView orderTime;
        TextView orderAddress;
        LinearLayout orderView;
        TextView orderState;
        View orderStateView;
        DraggableMapView mapView;
        GoogleMap map;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            foodOrderList = itemView.findViewById(R.id.orders_list);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            orderAddress = itemView.findViewById(R.id.order_address);
            orderTime = itemView.findViewById(R.id.order_time);
            orderView = itemView.findViewById(R.id.ll_order);
            orderState = itemView.findViewById(R.id.tv_order_state);
            orderStateView = itemView.findViewById(R.id.color_order_status);
            mapView = itemView.findViewById(R.id.map);
            ll_orders = itemView.findViewById(R.id.ll_orders);

            if (mapView != null) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            }

            orderView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collapseExpandListView();
                }
            });

        }

        void collapseExpandListView() {
            if (ll_orders.getVisibility() == View.GONE) {
                // it's collapsed - expand it
                ll_orders.setVisibility(View.VISIBLE);
            } else {
                // it's expanded - collapse it
                ll_orders.setVisibility(View.GONE);
            }
        }

        @Override
        public void onMapReady(GoogleMap googleMap) {
            MapsInitializer.initialize(myContext);
            map = googleMap;
            setMapLocation();
        }

        private void setMapLocation() {
            if (map == null) return;

            LatLng location = (LatLng) mapView.getTag();
            if (location == null) return;

            // Add a marker for this item and set the camera
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f));
            map.addMarker(new MarkerOptions().position(location));

            // Set the map type back to normal.
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void bindView(LatLng latLng) {
            // Store a reference to the item in the mapView's tag. We use it to get the
            // coordinate of a location, when setting the map location.
            mapView.setTag(latLng);
            setMapLocation();
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

            Log.d(TAG, "saveDataToRestaurant: " +restaurantId);
            int count = viewHolder.foodOrderList.getChildCount();
            StringBuffer stringBuffer = new StringBuffer("");

            for (int i = 0 ; i < count -1 ; i++)
            {
                LinearLayout view = (LinearLayout) viewHolder.foodOrderList.getChildAt(i);

                String name = ((TextView) view.getChildAt(0)).getText().toString();
                String quantity = ((EditText)((LinearLayout) view.getChildAt(1)).getChildAt(0)).getText().toString();

                for (int j = 0 ; j < Integer.parseInt(quantity) ; j++)
                    stringBuffer.append(name+", ");

            }

            databaseReference.child("personOrder").setValue(stringBuffer.toString());
        }
    }

    class myValueEventListener implements ValueEventListener {

        private ViewHolder viewHolder;

        public myValueEventListener(ViewHolder viewHolder)
        {
            this.viewHolder = viewHolder;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            String lat, lon;

            lat = dataSnapshot.child("l").child("0").getValue().toString();
            lon = dataSnapshot.child("l").child("1").getValue().toString();

            viewHolder.bindView(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }
}
