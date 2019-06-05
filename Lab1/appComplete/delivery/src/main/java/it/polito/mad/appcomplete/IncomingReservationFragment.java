package it.polito.mad.appcomplete;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import it.polito.mad.appcomplete.DurationHelpers.DirectionsJSONParser;

public class IncomingReservationFragment extends Fragment
        implements RecyclerItemTouchHelperReservation.RecyclerItemTouchHelperListener,
        SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "IncomingReservation";

    private ArrayList<ReservationInfo> reservationInfoList;
    private RecyclerViewAdapterReservation myAdapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout mySwipeRefreshLayout;

    private SharedPreferences preferences;
    private DatabaseReference database;

    // for notification
    private SharedPreferences.Editor editor;
    private Context context;

    public IncomingReservationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_incoming_reservation, container, false);
        mySwipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        mySwipeRefreshLayout.setOnRefreshListener(this);
        recyclerView = view.findViewById(R.id.recyclerViewIncomingReservation);
        initializeReservation();
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();
        if (id == R.id.menu_refresh)
            return true;

        return super.onOptionsItemSelected(item);
    }

    private void initializeReservation() {
        preferences = context.getSharedPreferences("loginState", Context.MODE_PRIVATE);

        database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference branchOrdersIncoming = database.child("delivery/" + preferences.getString("Uid", "") + "/Orders/Incoming");


        branchOrdersIncoming.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                reservationInfoList = new ArrayList<>();

                for (DataSnapshot data : dataSnapshot.getChildren())
                {
                    ReservationInfo value = data.getValue(ReservationInfo.class);
                    value.setOrderID(data.getKey());

                    reservationInfoList.add(restoreItem(value));
                }

                //for notification
                DatabaseReference branchOrders = database.child("delivery/" + preferences.getString("Uid", "") + "/Orders/");

                if (reservationInfoList.size() == 0) {
                    branchOrders.child("IncomingReservationFlag").setValue(false);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", false);
                    editor.apply();
                } else {
                    branchOrders.child("IncomingReservationFlag").setValue(true);

                    editor = preferences.edit();
                    editor.putBoolean("IncomingReservation", true);
                    editor.apply();
                }

                try {
                    getActivity().invalidateOptionsMenu();
                } catch (NullPointerException e) {
                    Log.w(TAG, "onDataChange: ", e);
                }
                ///////////////////
                initializeRecyclerViewReservation();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "onCancelled: The read failed: " + databaseError.getMessage());
            }
        });

    }

    private void initializeRecyclerViewReservation()
    {
        myAdapter = new RecyclerViewAdapterReservation(context, reservationInfoList);
        Log.d(TAG, "initializeRecyclerViewReservation: called");

        Collections.sort(reservationInfoList, ReservationInfo.BY_TIME_ASCENDING);


        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(myAdapter);

        // adding item touch helper
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelperReservation(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, this, context, true);

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);
    }

    String distance = " ";
    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction, int position)
    {
        if (viewHolder instanceof RecyclerViewAdapterReservation.ViewHolder)
        {
            //String name = reservationInfoList.get(viewHolder.getAdapterPosition()).getNamePerson();
            final ReservationInfo deletedItem = reservationInfoList.get(viewHolder.getAdapterPosition());  // backup of removed item for undo purpose
            final String deletedReservationId = deletedItem.getOrderID();
            preferences = context.getSharedPreferences("loginState", Context.MODE_PRIVATE);

            //final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            //final DatabaseReference IncomingBranch = mDatabase.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("Incoming");

            /*final DatabaseReference branchOrdersIncoming = database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming");
            branchOrdersIncoming.child(deletedReservationId).removeValue();*/

            if (direction == ItemTouchHelper.RIGHT)
            {
                // Add to finished branch:
                database.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("finished").child(deletedReservationId).setValue(restoreItem(deletedItem));

                // Add delivered flag to restaurant:
                database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("delivered");
                // Add delivered flag to customer:
                database.child("customers").child(deletedItem.getIdPerson()).child("previous_order").child(deletedReservationId).child("order_status").setValue("delivered");

                //Removing order from incoming branch:
                database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming").child(deletedReservationId).removeValue();

                //1. Saving passed distance
                    MarkerOptions customer = new MarkerOptions().position(new LatLng(Float.parseFloat(deletedItem.getcLatitude()) , Float.parseFloat(deletedItem.getcLongitude()) )).title("Customer");
                    MarkerOptions restaurant = new MarkerOptions().position(new LatLng(Float.parseFloat(deletedItem.getrLatitude()) , Float.parseFloat(deletedItem.getrLongitude()) )).title("Restaurant");
                    //1.1 calculate distance
                        //1.1.1 Get duration
                        String url2 = getUrl(customer.getPosition(), restaurant.getPosition(), "driving");
                        DownloadTask downloadTask2 = new DownloadTask();
                        //1.1.2 Start downloading json data from Google Directions API
                        downloadTask2.execute(url2);

                    //1.2 get all distances

                    //1.3 add new distance to all distances
                    //database.child("delivery").child(preferences.getString("Uid", " ")).child("totaldistance").setValue(distance);

                // Show undo message
                Snackbar snackbar = Snackbar.make(recyclerView,   " delivery finished", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Add to incoming branch:
                        database.child("delivery").child(preferences.getString("Uid", " ")).child("Orders").child("finished").child(deletedReservationId).removeValue();

                        // Add return back flag to it's default:
                        database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("in_delivery");
                        database.child("customers").child(deletedItem.getIdPerson()).child("previous_order").child(deletedReservationId).child("status_order").setValue("in_delivery");

                        //Removing order from finished branch:
                        database.child("delivery/" + preferences.getString("Uid", " ") + "/Orders/Incoming").child(deletedReservationId).setValue(restoreItem(deletedItem));
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }
            else if (direction == ItemTouchHelper.LEFT)
            {
                // Add delivered flag to restaurant:
                database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("in_delivery");
                database.child("customers").child(deletedItem.getIdPerson()).child("previous_order").child(deletedReservationId).child("status_order").setValue("in_delivery");

                initializeRecyclerViewReservation();
                // Show undo message
                Snackbar snackbar = Snackbar.make(recyclerView,   " in delivery", Snackbar.LENGTH_LONG);
                snackbar.setAction("UNDO", new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        // Add delivered flag to restaurant:
                        database.child("restaurants").child(deletedItem.getRestaurantId()).child("Orders").child("Ready_To_Go").child(deletedReservationId).child("status_order").setValue("ready");
                        database.child("customers").child(deletedItem.getIdPerson()).child("previous_order").child(deletedReservationId).child("status_order").setValue("ready");
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();
            }


        }
    }

    public ReservationInfo restoreItem(ReservationInfo reservationInfo) {
        ReservationInfo res = new ReservationInfo();

        res.setOrderID(reservationInfo.getOrderID());
        res.setIdPerson(reservationInfo.getIdPerson());
        res.setNamePerson(reservationInfo.getNamePerson());
        res.setTimeReservation(reservationInfo.getTimeReservation());
        res.setRestaurantId(reservationInfo.getRestaurantId());
        res.setcLatitude(reservationInfo.getcLatitude());
        res.setcLongitude(reservationInfo.getcLongitude());
        res.setrLatitude(reservationInfo.getrLatitude());
        res.setrLongitude(reservationInfo.getrLongitude());


        return res;
    }

    @Override
    public void onRefresh() {
        //TODO: myUpdateOP.
        mySwipeRefreshLayout.setRefreshing(false);
    }

    //////////////////////////////////////////////////////////////
    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);

// Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

// Connecting to url
            urlConnection.connect();

// Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while( ( line = br.readLine()) != null){
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        }catch(Exception e){
            //Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return context;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String>
    {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

// For storing data from web service
            String data = "";

            try{
// Fetching the data from web service
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
// doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

// Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

// Starts parsing data
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            if(result.size()<1){
                //Toast.makeText(getBaseContext(), "No Points", Toast.LENGTH_SHORT).show();
                return;
            }

            // Traversing through all the routes
            for(int i=0;i<result.size();i++)
            {
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for(int j=0;j <path.size();j++)
                {
                    HashMap<String,String> point = path.get(j);
                    if(j==0){ // Get distance from the list
                        distance = (String)point.get("distance");
                        continue;
                    }
                }
            }

            ////////////////////////////////////////////////////////
            {
                final FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("delivery").child(preferences.getString("Uid", " ")).child("totaldistance");

                /*ValueEventListener UserFromID = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                         c="    ";
                        Float.parseFloat(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                };*/
                ValueEventListener UserFromID = new x();
                myRef.addValueEventListener(UserFromID);
            }
            ////////////////////////////////////////////////////////



            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    float_total_distance += float_distance;
                    database.child("delivery").child(preferences.getString("Uid", " ")).child("totaldistance").setValue(Float.toString(float_total_distance));
                }
            }, 1000);

        }

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    //Handler handler;

    float float_distance;
    float float_total_distance;
    boolean isFinished=false;
   public class x  implements   ValueEventListener{
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

            Object o = dataSnapshot.getValue();

            if (o != null)
            {
                float_total_distance =Float.parseFloat(o.toString());
                distance= distance.split(" ")[0]; //removes string "km" from the result.
                // distance="10.172";
                float_distance= Float.parseFloat(distance);
            }

           //
        }


        @Override
        public void onCancelled(DatabaseError databaseError) {
        }


    }
}
