package it.polito.mad.customer;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import it.polito.mad.data_layer_access.FirebaseUtils;

public class RVAOrders extends RecyclerView.Adapter<RVAOrders.ViewHolder>{

    private Context myContext;
    private List<OrdersInfo> ordersInfos;


    public RVAOrders(Context myContext, List<OrdersInfo> ordersInfos){
        this.myContext = myContext;
        this.ordersInfos = ordersInfos;

        FirebaseUtils.setupFirebaseCustomer();
    }

    @NonNull
    @Override
    public RVAOrders.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.cardview_orders, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RVAOrders.ViewHolder viewHolder, final int i) {
        viewHolder.orderTime.setText(ordersInfos.get(i).getTime());
        viewHolder.orderAddress.setText(ordersInfos.get(i).getAddress());
        viewHolder.restaurantName.setText(ordersInfos.get(i).getRestaurantName());

        if (ordersInfos.get(i).getState() == OrderState.PENDING)
        {
            viewHolder.orderState.setText(myContext.getString(R.string.pending));
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.colorPrimary));
        } else if (ordersInfos.get(i).getState() == OrderState.ACCEPTED)
        {
            viewHolder.orderState.setText(myContext.getString(R.string.in_preparation));
            viewHolder.orderStateView.setBackground(myContext.getDrawable(android.R.color.holo_blue_light));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERED)
        {
            viewHolder.orderState.setText(myContext.getString(R.string.delivered));
            viewHolder.orderStateView.setBackground(myContext.getDrawable(android.R.color.black));
        } else if (ordersInfos.get(i).getState() == OrderState.DELIVERING)
        {
            viewHolder.orderState.setText(myContext.getString(R.string.in_delivery));
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.green));
        } else if (ordersInfos.get(i).getState() == OrderState.CANCELLED)
        {
            viewHolder.orderState.setText(myContext.getString(R.string.rejected));
            viewHolder.orderStateView.setBackground(myContext.getDrawable(R.color.red));
        }

        Map<String, Integer> productQuantity = ordersInfos.get(i).getFoodAmount();
        Map<String, Float> productPrice = ordersInfos.get(i).getFoodPrice();

        if (viewHolder.foodOrderList.getChildCount() == 0)
        {
            for (String s : productPrice.keySet())
            {
                LinearLayout linearLayout = new LinearLayout(myContext);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                TextView name = new TextView(myContext);
                name.setText(s);
                name.setTextSize(18);
                name.setTextColor(myContext.getColor(android.R.color.black));
                name.setTypeface(null, Typeface.BOLD);
                name.setTag(ordersInfos.get(i).getFoodId().get(s));
                name.setPadding(20, 6, 0, 0);

                LinearLayout ll = new LinearLayout(myContext);
                ll.setOrientation(LinearLayout.HORIZONTAL);
                ll.setPadding(60, 0, 0, 0);

                EditText quantity = new EditText(myContext);
                quantity.setText(productQuantity.get(s).toString());
                quantity.setInputType(InputType.TYPE_CLASS_NUMBER);

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
        }


        if (ordersInfos.get(i).getState() == OrderState.DELIVERED && !ordersInfos.get(i).isReview())
        {
            Button button = new Button(myContext);
            button.setTag(ordersInfos.get(i).getRestaurantId()+" "+ordersInfos.get(i).getOrderId());
            button.setText(myContext.getString(R.string.review_your_order));
            button.setTextColor(myContext.getColor(R.color.white));
            button.setGravity(Gravity.CENTER_HORIZONTAL);
            button.setBackgroundColor(myContext.getColor(R.color.colorPrimary));

            button.setOnClickListener(v -> {
                String restId = v.getTag().toString().split(" ")[0];
                String orderId = v.getTag().toString().split(" ")[1];

                LayoutInflater li = LayoutInflater.from(myContext);
                View view = li.inflate(R.layout.activity_review, null);

                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(myContext);
                alertDialogBuilder.setView(view);

                AlertDialog alertDialogCongratulations = alertDialogBuilder.create();

                Button b = view.findViewById(R.id.button);
                b.setOnClickListener(new CustomReviewClickListener(view, orderId, restId, alertDialogCongratulations));

                alertDialogCongratulations.show();
            });

            viewHolder.foodOrderList.addView(button);
        }

        if (ordersInfos.get(i).getState() == OrderState.DELIVERING)
        {
            FirebaseUtils.branchRiderPosition.child(ordersInfos.get(i).getDeliverymanId()).addListenerForSingleValueEvent(new myValueEventListener(viewHolder));
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


    public class ViewHolder

            extends RecyclerView.ViewHolder

            implements OnMapReadyCallback {

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

            orderView.setOnClickListener(v -> collapseExpandListView());

        }

        void collapseExpandListView() {
            if (ll_orders.getVisibility() == View.GONE) {
                ll_orders.setVisibility(View.VISIBLE);
            } else {
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

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 13f));
            map.addMarker(new MarkerOptions().position(location));

            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        private void bindView(LatLng latLng) {
            mapView.setTag(latLng);
            setMapLocation();
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

            Object o = dataSnapshot.child("l").child("0").getValue();
            lat = "0.0";

            if (o != null)
            {
                lat = o.toString();
            }

            o = dataSnapshot.child("l").child("1").getValue();
            lon = "0.0";

            if (o != null)
            {
                lon = o.toString();
            }

            viewHolder.bindView(new LatLng(Double.parseDouble(lat), Double.parseDouble(lon)));
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    }

    class CustomReviewClickListener implements View.OnClickListener
    {
        private View view;
        private String orderId;
        private String restId;
        private AlertDialog alertDialog;
        private String title;
        private String description;
        private int rating;

        CustomReviewClickListener(View view, String orderId, String restId, AlertDialog alertDialog)
        {
           this.view = view;
           this.restId = restId;
           this.orderId = orderId;
           this.alertDialog = alertDialog;
        }

        @Override
        public void onClick(View v) {
            RatingBar ratingBar = view.findViewById(R.id.ratingBar);
            EditText textTitle = view.findViewById(R.id.textTitle);
            EditText textDescription = view.findViewById(R.id.textDescription);

            title = textTitle.getText().toString();
            description = textDescription.getText().toString();
            rating = (int) ratingBar.getRating();

            addNewReview(title, description, rating);
            incrementStarReview(rating);
            setOrderReviewed();

            alertDialog.cancel();
        }

        private void setOrderReviewed() {
            DatabaseReference databaseReference = FirebaseUtils.branchCustomerPreviousOrder.child(orderId);
            databaseReference.child("reviewed").setValue("true");
        }

        private void incrementStarReview(final int rating) {
            DatabaseReference databaseReference1 = FirebaseUtils.branchRestaurant.child(restId).child("review");

            databaseReference1.runTransaction( new Transaction.Handler(){

                @NonNull
                @Override
                public Transaction.Result doTransaction(@NonNull MutableData currentData){
                    double nVotes=0, nStars=0;
                    for (int i = 1 ; i <= 5 ; i++)
                    {
                        Object o = currentData.child(i+"star").getValue();

                        if (o != null)
                        {
                            nVotes += Double.parseDouble(o.toString());
                            nStars += (Double.parseDouble(o.toString())*i);
                        }

                        if (i == rating)
                        {
                            nVotes++;
                            nStars += i;
                        }
                    }

                    double totalRating;
                    if (nVotes != 0)
                    {
                        totalRating = nStars/nVotes;
                    } else
                    {
                        totalRating = 0;
                    }

                    FirebaseUtils.branchRestaurant.child(restId).child("review").child("total").setValue(totalRating);

                    Object o = currentData.child(rating+"star").getValue();

                    if (o != null)
                    {
                        int value = Integer.parseInt(o.toString());
                        value++;
                        FirebaseUtils.branchRestaurant.child(restId).child("review").child(rating+"star").setValue(value);
                    }

                    return Transaction.success(currentData);
                }

                @Override
                public void onComplete(DatabaseError databaseError,
                                       boolean committed, DataSnapshot currentData){
                }
            });
        }

        private void addNewReview(String title, String description, float rating) {
            Date c = Calendar.getInstance().getTime();

            SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
            String formattedDate = df.format(c);

            DatabaseReference databaseReference = FirebaseUtils.branchRestaurant.child(restId).child("review_description").push();
            databaseReference.child("title").setValue(title);
            databaseReference.child("description").setValue(description);
            databaseReference.child("stars").setValue(rating);
            databaseReference.child("date").setValue(formattedDate);

        }
    }
}
