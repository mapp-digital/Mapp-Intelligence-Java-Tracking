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
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@WebServlet("/search/*")
public class SearchServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public SearchServlet() {
        super();
    }

    private String decode(String str) {
        String decodedString = str;
        try {
            decodedString = URLDecoder.decode(str, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException | NullPointerException e) {
            // do nothing
        }

        return ((decodedString != null) ? decodedString : "");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUrl = (String) request.getAttribute("requestUrl");
        String[] urlSplit = requestUrl.split("\\?")[0].split("/");
        String search = this.decode(urlSplit[urlSplit.length - 1]);

        ProductModel productModel = new ProductModel();
        List<Product> searchList = productModel.findAllBySearch(search);

        TrackingService tracking = (TrackingService) request.getAttribute("tracking");
        tracking.search(search, searchList.size());

        request.setAttribute("search", search);
        request.setAttribute("searchList", null);
        request.setAttribute("searchListResult", searchList.size());
        if (searchList.size() > 0) {
            request.setAttribute("searchList", searchList);
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
