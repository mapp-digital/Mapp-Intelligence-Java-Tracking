package demo.entities;

public class Product {
    private long id;
    private String sku;
    private String name;
    private double price;
    private String category;
    private String description;
    private String url;
    private boolean bestSeller;
    private boolean feature;
    private boolean latest;

    public Product() {}

    public void setId(long id) {
        this.id = id;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBestSeller(boolean bestSeller) {
        this.bestSeller = bestSeller;
    }

    public void setFeature(boolean feature) {
        this.feature = feature;
    }

    public void setLatest(boolean latest) {
        this.latest = latest;
    }

    public long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public boolean getBestSeller() {
        return this.bestSeller;
    }

    public boolean getFeature() {
        return this.feature;
    }

    public boolean getLatest() {
        return this.latest;
    }
}
