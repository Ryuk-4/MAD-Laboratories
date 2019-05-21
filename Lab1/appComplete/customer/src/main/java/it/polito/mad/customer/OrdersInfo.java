package it.polito.mad.customer;

import java.util.Map;

enum OrderState
{
    PENDING,
    ACCEPTED,
    DELIVERING,
    CANCELLED,
    DELIVERED
}

class OrdersInfo {
    private String restaurantName;
    private String restaurantId;
    private String orderId;
    private String time;
    private String address;
    private Map<String, Integer> foodAmount;
    private Map<String, Float> foodPrice;
    private Map<String, String> foodId;
    private OrderState state;
    private String deliverymanId;
    private boolean review;


    public OrdersInfo(String restaurantName, String restaurantId, String time, String address, Map<String, Integer> foodAmount, Map<String, Float> foodPrice, Map<String, String> foodId, OrderState state, String orderId, String deliverymanId, boolean review) {
        this.restaurantName = restaurantName;
        this.restaurantId = restaurantId;
        this.time = time;
        this.address = address;
        this.foodAmount = foodAmount;
        this.foodPrice = foodPrice;
        this.state = state;
        this.orderId = orderId;
        this.foodId = foodId;
        this.deliverymanId = deliverymanId;
        this.review = review;
    }

    public Map<String, String> getFoodId() {
        return foodId;
    }

    public void setFoodId(Map<String, String> foodId) {
        this.foodId = foodId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Map<String, Integer> getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(Map<String, Integer> foodAmount) {
        this.foodAmount = foodAmount;
    }

    public Map<String, Float> getFoodPrice() {
        return foodPrice;
    }

    public void setFoodPrice(Map<String, Float> foodPrice) {
        this.foodPrice = foodPrice;
    }

    public OrderState getState() {
        return state;
    }

    public void setState(OrderState state) {
        this.state = state;
    }

    public String getDeliverymanId() {
        return deliverymanId;
    }

    public void setDeliverymanId(String deliverymanId) {
        this.deliverymanId = deliverymanId;
    }

    public boolean isReview() {
        return review;
    }

    public void setReview(boolean review) {
        this.review = review;
    }


}
