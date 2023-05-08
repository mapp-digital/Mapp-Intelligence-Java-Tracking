package demo.models;

import demo.entities.Product;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class ProductModel {
    private static final String JSON_PRODUCTS = Objects.requireNonNull(ProductModel.class.getClassLoader().getResource("products.json")).getFile();
    private List<Product> products = new ArrayList<>();
    private List<Product> bestSeller = new ArrayList<>();
    private List<Product> featureProducts = new ArrayList<>();
    private List<Product> latestProducts = new ArrayList<>();

    /**
     *
     */
    public ProductModel() {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader(JSON_PRODUCTS)) {
            JSONArray jsonProducts = (JSONArray) jsonParser.parse(reader);

            for (Object jsonProduct : jsonProducts) {
                this.addProduct((JSONObject) jsonProduct);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param jsonProduct Products
     */
    private void addProduct(JSONObject jsonProduct) {
        Product product = new Product();

        product.setId((long) jsonProduct.get("Id"));
        product.setSku((String) jsonProduct.get("SKU"));
        product.setName((String) jsonProduct.get("Name"));
        product.setCategory((String) jsonProduct.get("Category"));
        product.setPrice((double) jsonProduct.get("Price"));
        product.setDescription((String) jsonProduct.get("Description"));
        product.setUrl((String) jsonProduct.get("ImageUrl"));
        product.setBestSeller((boolean) jsonProduct.get("BestSeller"));
        product.setFeature((boolean) jsonProduct.get("Feature"));
        product.setLatest((boolean) jsonProduct.get("Latest"));

        if (product.getBestSeller()) {
            this.bestSeller.add(product);
        }

        if (product.getFeature()) {
            this.featureProducts.add(product);
        }

        if (product.getLatest()) {
            this.latestProducts.add(product);
        }

        this.products.add(product);
    }

    /**
     * @return all products
     */
    public List<Product> findAll() {
        return this.products;
    }

    /**
     * @param cat category
     * @return all products of this category
     */
    public List<Product> findAllByCategory(String cat) {
        List<Product> p = new ArrayList<>();
        for (Product product : this.products) {
            if (cat.equalsIgnoreCase(product.getCategory())) {
                p.add(product);
            }
        }

        return p;
    }

    /**
     * @param searchTerm search term
     * @return all searching products
     */
    public List<Product> findAllBySearch(String searchTerm) {
        List<Product> p = new ArrayList<>();
        String category;
        String name;
        String description;
        String term = searchTerm.toLowerCase();

        for (Product product : this.products) {
            category = product.getCategory().toLowerCase();
            name = product.getName().toLowerCase();
            description = product.getDescription().toLowerCase();

            if (category.contains(term) || name.contains(term) || description.contains(term)) {
                p.add(product);
            }
        }

        return p;
    }

    /**
     * @return best seller products
     */
    public List<Product> findBestSeller() {
        return this.bestSeller;
    }

    /**
     * @return feature products
     */
    public List<Product> findFeature() {
        return this.featureProducts;
    }

    /**
     * @return latest products
     */
    public List<Product> findLatest() {
        return this.latestProducts;
    }

    /**
     * @param id product id
     * @return product
     */
    public Product find(int id) {
        for (Product product : this.products) {
            if (id == product.getId()) {
                return product;
            }
        }

        return null;
    }

    /**
     * @param sku product sku
     * @return product
     */
    public Product find(String sku) {
        for (Product product : this.products) {
            if (sku.equals(product.getSku())) {
                return product;
            }
        }

        return null;
    }
}
