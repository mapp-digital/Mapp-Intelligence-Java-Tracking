package demo.servlets;

import demo.entities.Product;
import demo.models.ProductModel;
import demo.service.TrackingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/product_detail/*")
public class DetailServlet extends HttpServlet {
    public DetailServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUrl = (String) request.getAttribute("requestUrl");
        String[] productSplit = requestUrl.split("\\?")[0].split("/");
        String productSku = productSplit[productSplit.length - 1];

        ProductModel productModel = new ProductModel();
        Product product = productModel.find(productSku);

        request.setAttribute("product", null);
        request.setAttribute("related", null);

        if (product != null) {
            request.setAttribute("product", product);
            request.setAttribute("related", productModel.findAllByCategory(product.getCategory()));

            TrackingService tracking = (TrackingService) request.getAttribute("tracking");
            tracking.viewProduct(product);
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
