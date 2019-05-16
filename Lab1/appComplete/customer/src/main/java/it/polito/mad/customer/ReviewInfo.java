package it.polito.mad.customer;

class ReviewInfo {
    private String rate;
    private String title;
    private String description;
    private String date;

    public ReviewInfo(String rate, String title, String description, String date) {
        this.rate = rate;
        this.title = title;
        this.description = description;
        this.date = date;
    }

    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
