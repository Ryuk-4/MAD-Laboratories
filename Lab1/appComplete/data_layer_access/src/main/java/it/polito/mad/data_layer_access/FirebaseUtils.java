package it.polito.mad.data_layer_access;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FirebaseUtils {

    //    Common among all the three modules
    public static FirebaseAuth auth;
    public static DatabaseReference database;
    public static String Uid;
    public static StorageReference storage;


    //    Restaurant's DatabaseReferences

    public static DatabaseReference timeBranch;
    public static DatabaseReference popularFoodBranch;
    public static DatabaseReference branchRestaurantOrders;
    public static DatabaseReference branchOrdersFlag;
    public static DatabaseReference branchOrdersIncoming;
    public static DatabaseReference branchOrdersInPreparation;
    public static DatabaseReference branchOrdersReady;
    public static DatabaseReference branchDailyFood;
    public static DatabaseReference branchStoricFood;
    public static DatabaseReference branchFavouriteFood;
    public static DatabaseReference branchCustomer;
    public static DatabaseReference branchRestaurantProfile;
    public static DatabaseReference branchDeliveryMan;
    public static DatabaseReference branchRestaurantTypeFood;
    public static DatabaseReference branchSoldOrders;
    public static DatabaseReference branchComment;
    public static DatabaseReference branchOverallRating;


    public static GeoFire geofireRider;
    public static GeoFire geofireRestaurant;



    //  -----------------------------------------------------------------------------------------
    //        Customer's DatabaseReferences

    public static DatabaseReference branchCustomerPreviousOrder;
    public static DatabaseReference branchCustomerProfile;
    public static DatabaseReference branchCustomerPosition;
    public static DatabaseReference branchCustomerFavoriteRestaurant;
    public static DatabaseReference branchRestaurantPosition;
    public static DatabaseReference branchRestaurant;
    public static DatabaseReference branchRiderPosition;


    public static StorageReference storageCustomerProfileImage;




    //  ----------------------------------------------------------------------------------------
    //        DeliverMan's DatabaseReferences


    /**
     *  common setup to all the methods
     */
    public static void setCommonParam() {
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        Uid = auth.getCurrentUser().getUid();
        storage = FirebaseStorage.getInstance().getReference();
    }

    /**
     *  restaurant firebase setup
     */
    public static void setupFirebase() {

        setCommonParam();

        branchRestaurantProfile = database.child("restaurants/" + Uid + "/Profile");
        branchRestaurantTypeFood = database.child("restaurants/" + Uid + "/type_food");
        branchCustomer = database.child("customers");
        branchDeliveryMan = database.child("delivery");
        timeBranch = database.child("restaurants/" + Uid + "/Time");
        popularFoodBranch = database.child("restaurants/" + Uid + "/Food_Analytics");
        branchDailyFood = database.child("restaurants/" + Uid + "/Daily_Food");
        branchStoricFood = database.child("restaurants/" + Uid + "/Storic_Food");
        branchFavouriteFood = database.child("restaurants/" + Uid + "/Favourites_Food/");
        branchRestaurantOrders = database.child("restaurants/" + Uid + "/Orders/");
        branchOrdersFlag = database.child("restaurants/" + Uid + "/Orders/IncomingReservationFlag");
        branchOrdersIncoming = database.child("restaurants/" + Uid + "/Orders/Incoming");
        branchOrdersInPreparation = database.child("restaurants/" + Uid + "/Orders/In_Preparation");
        branchOrdersReady = database.child("restaurants/" + Uid + "/Orders/Ready_To_Go");
        branchSoldOrders = database.child("restaurants/" + Uid + "/sold_orders");
        branchComment = database.child("restaurants/" + Uid + "/review_description");
        branchOverallRating = database.child("restaurants/" + Uid + "/review");

        geofireRider = new GeoFire(database.child("riders_position"));
        geofireRestaurant = new GeoFire(database.child("restaurants_position"));

    }


    /**
     *  customer firebase setup
     */
    public static void setupFirebaseCustomer()
    {
        setCommonParam();

        branchCustomerPreviousOrder = database.child("customers").child(Uid).child("previous_order");
        branchCustomerProfile = database.child("customers").child(Uid).child("Profile");
        branchCustomerPosition = database.child("customers_position");
        branchCustomerFavoriteRestaurant = database.child("customers").child(Uid).child("favorite_restaurant");
        branchRestaurant = database.child("restaurants");
        branchRiderPosition = database.child("riders_position");
        branchRestaurantPosition = database.child("restaurants_position");

        storageCustomerProfileImage = storage.child("profile_images/profile"+Uid);

        geofireRestaurant = new GeoFire(branchRestaurantPosition);
    }
}
