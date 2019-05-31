package it.polito.mad.customer;


import java.util.List;

public class RestaurantInfo {
    private String name;
    private int numerReview;
    private int[] votes;
    private String description;
    private String id;
    private List<String> typeOfFood;
    private String photoURL;
    private boolean favorite;

    public RestaurantInfo(String name, int numerReview, int[] votes, String description, String id, List<String> typeOfFood, String photo, boolean favorite) {
        this.name = name;
        this.numerReview = numerReview;
        this.votes = votes;
        this.description = description;
        this.id = id;
        this.typeOfFood = typeOfFood;
        this.photoURL = photo;
        this.favorite = favorite;
    }

    public String getPhoto() {
        return photoURL;
    }

    public void setPhoto(String photo) {
        this.photoURL = photo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumerReview() {
        return numerReview;
    }

    public void setNumerReview(int numerReview) {
        this.numerReview = numerReview;
    }

    public int[] getVotes() {
        return votes;
    }

    public void setVotes(int[] votes) {
        this.votes = votes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getTypeOfFood() {
        return typeOfFood;
    }

    public void setTypeOfFood(List<String> typeOfFood) {
        this.typeOfFood = typeOfFood;
    }


    public String getVotesString()
    {
        StringBuffer buffer = new StringBuffer("");
        buffer.append(String.format("%.1f", getValueRatinBar())).append(" (").append(numerReview).append(")");
        return buffer.toString();
    }

    public float getValueRatinBar()
    {
        float ret = 0;

        for (int i = 0 ; i < 5 ; i++)
        {
            ret += (votes[i]*(i+1));
        }

        ret = ret/numerReview;
        return ret;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
