package demo.servlets;

import demo.entities.Product;
import demo.models.ProductModel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/category/*")
public class CategoryServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CategoryServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUrl = (String) request.getAttribute("requestUrl");
        String[] productSplit = requestUrl.split("\\?")[0].split("/");
        String productCategory = productSplit[productSplit.length - 1];

        ProductModel productModel = new ProductModel();
        List<Product> categoryList = productModel.findAllByCategory(productCategory);

        request.setAttribute("category", productCategory);
        request.setAttribute("categoryList", null);
        if (categoryList.size() > 0) {
            request.setAttribute("categoryList", categoryList);
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
