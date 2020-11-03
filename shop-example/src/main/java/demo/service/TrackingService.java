package demo.service;

import com.mapp.intelligence.tracking.MappIntelligenceLogger;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;
import com.mapp.intelligence.tracking.core.MappIntelligenceTracking;
import com.mapp.intelligence.tracking.data.*;
import demo.entities.Item;
import demo.entities.Personal;
import demo.entities.Product;
import demo.entities.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

class CustomLogger implements MappIntelligenceLogger {
    /**
     * @param msg Debug message
     */
    @Override
    public void log(String msg) {
        System.out.println(msg);
    }

    /**
     * @param format String format
     * @param args   Arguments
     */
    @Override
    public void log(String format, Object... args) {
        System.out.println(String.format(format, args));
    }
}

public class TrackingService {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final MappIntelligenceTracking mappIntelligenceTracking;

    private MappIntelligenceAction actionData;
    private MappIntelligenceCampaign campaignData;
    private MappIntelligenceCustomer customerData;
    private MappIntelligenceOrder orderData;
    private MappIntelligencePage pageData;
    private MappIntelligenceProductCollection productDataList = new MappIntelligenceProductCollection();
    private MappIntelligenceSession sessionData;

    public TrackingService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;

        MappIntelligenceConfig mic = new MappIntelligenceConfig()
            .setTrackId("123451234512345")
            .setTrackDomain("analytics01.wt-eu02.net")
            .setLogger(new CustomLogger())
            .setConsumerType(MappIntelligenceConsumerType.FILE)
            .setMaxFileLines(10)
            .setReferrerURL(this.request.getHeader("Referer"))
            .setRequestURL(this.request.getRequestURL().toString())
            .setRemoteAddress(this.request.getRemoteAddr())
            .setUserAgent(this.request.getHeader("User-Agent"));

        Cookie[] cookies = this.request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                mic.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        this.mappIntelligenceTracking = new MappIntelligenceTracking(mic);
    }

    private void trackProduct(Product product, String status, int qty) {
        MappIntelligenceProduct tProduct = new MappIntelligenceProduct()
            .setStatus(status)
            .setQuantity(qty)
            .setId(product.getSku())
            .setCost(product.getPrice() * qty)
            .setParameter(1, product.getBestSeller() ? "best seller" : "")
            .setParameter(2, product.getFeature() ? "feature product" : "")
            .setParameter(3, product.getLatest() ? "latest product" : "")
            .setCategory(1, product.getDescription())
            .setCategory(2, product.getCategory())
            .setCategory(3, product.getId() + "")
            .setCategory(4, product.getName());

        this.productDataList.add(tProduct);
    }

    public void search(String search, int numberSearchResults) {
        this.pageData = new MappIntelligencePage()
            .setSearch(search)
            .setNumberSearchResults(numberSearchResults);
    }

    public void customer(User user, Personal personal) {
        this.sessionData = new MappIntelligenceSession()
            .setLoginStatus("login");

        this.customerData = new MappIntelligenceCustomer()
            .setFirstName(user.getFirstName())
            .setLastName(user.getLastName())
            .setEmail(user.getEmail())
            .setTelephone(user.getTelephone())
            .setValidation(true)
            .setCategory(1, user.getFullName());

        if (personal != null) {
            this.customerData
                .setCity(personal.getCity())
                .setStreet(personal.getAddress1())
                .setCountry(personal.getCountry())
                .setPostalCode(personal.getPostCode());
        }
    }

    public void order(String orderId, List<Item> cart) {
        double orderValue = 0;
        for (Item item : cart) {
            this.trackProduct(item.getProduct(), MappIntelligenceProduct.CONFIRMATION, item.getQuantity());

            orderValue += item.getProduct().getPrice() * item.getQuantity();
        }

        this.orderData = new MappIntelligenceOrder()
            .setCurrency("EUR")
            .setId(orderId)
            .setValue(orderValue);
    }

    public void viewProduct(Product product) {
        this.trackProduct(product, MappIntelligenceProduct.VIEW, 1);
    }

    public void addProduct(Product product, int qty) {
        this.trackProduct(product, MappIntelligenceProduct.BASKET, qty);
    }

    public void track() {
        MappIntelligenceDataMap dataMap = new MappIntelligenceDataMap()
            .action(this.actionData)
            .campaign(this.campaignData)
            .customer(this.customerData)
            .order(this.orderData)
            .page(this.pageData)
            .product(this.productDataList)
            .session(this.sessionData);

        this.mappIntelligenceTracking.track(dataMap);

        this.actionData = null;
        this.campaignData = null;
        this.customerData = null;
        this.orderData = null;
        this.pageData = null;
        this.productDataList = new MappIntelligenceProductCollection();
        this.sessionData = null;
    }
}
