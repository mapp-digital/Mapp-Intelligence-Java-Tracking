package demo.servlets;

import demo.entities.Product;
import demo.models.ProductModel;
import demo.service.CustomerService;
import demo.service.TrackingService;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
@WebFilter("/*")
public class FilterServlet implements Filter {
    private List<Product> productList;
    private List<Product> randomizeList;
    private List<Product> bestSellerList;
    private List<Product> latestProductList;
    private List<Product> featureProductList;

    private int randInt(int max) {
        Random rand = new Random();
        return rand.nextInt((max) + 1);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ProductModel productModel = new ProductModel();
        this.productList = productModel.findAll();

        this.randomizeList = Arrays.asList(
            this.productList.get(this.randInt(this.productList.size() - 1)),
            this.productList.get(this.randInt(this.productList.size() - 1))
        );

        this.bestSellerList = productModel.findBestSeller();
        this.latestProductList = productModel.findLatest();
        this.featureProductList = productModel.findFeature();
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String requestUrl = request.getRequestURL().toString();

        if (requestUrl.contains("/assets/") || requestUrl.contains("favicon")) {
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            TrackingService ts = new TrackingService(request, response);
            request.setAttribute("tracking", ts);

            new CustomerService(request, response);

            request.setAttribute("products", this.productList);
            request.setAttribute("randomize", this.randomizeList);
            request.setAttribute("bestseller", this.bestSellerList);
            request.setAttribute("latest", this.latestProductList);
            request.setAttribute("feature", this.featureProductList);
            request.setAttribute("requestUrl", requestUrl);

            request.getRequestDispatcher("index.jsp").forward(servletRequest, servletResponse);

            ts.track();
        }
    }

    @Override
    public void destroy() {}
}
