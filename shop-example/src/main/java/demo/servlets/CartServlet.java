package demo.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import demo.entities.Item;
import demo.entities.Product;
import demo.models.ProductModel;
import demo.service.TrackingService;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
@WebServlet("/basket/*")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public CartServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            this.displayCart(request, response);
        } else {
            if (action.equalsIgnoreCase("add")) {
                this.add(request, response);
            } else if (action.equalsIgnoreCase("remove")) {
                this.remove(request, response);
            }
            else {
                this.displayCart(request, response);
            }
        }
    }

    protected void displayCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void remove(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Item> cart = (List<Item>) session.getAttribute("cart");
        int id = Integer.parseInt(request.getParameter("id"));
        int index = this.isExisting(id, cart);

        if (index != -1) {
            cart.remove(index);
        }

        session.setAttribute("cart", cart);
        response.sendRedirect("cart");
    }

    protected void add(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductModel productModel = new ProductModel();
        HttpSession session = request.getSession();

        int id = Integer.parseInt(request.getParameter("id"));
        int qty = Integer.parseInt(request.getParameter("qty"));
        Product product = productModel.find(id);

        if (product != null) {
            TrackingService tracking = (TrackingService) request.getAttribute("tracking");
            tracking.addProduct(product, qty);

            if (session.getAttribute("cart") == null) {
                List<Item> cart = new ArrayList<>();
                cart.add(new Item(product, qty));
                session.setAttribute("cart", cart);
            } else {
                List<Item> cart = (List<Item>) session.getAttribute("cart");
                int index = this.isExisting(id, cart);
                if (index == -1) {
                    cart.add(new Item(product, qty));
                } else {
                    int quantity = cart.get(index).getQuantity() + qty;
                    cart.get(index).setQuantity(quantity);
                }
                session.setAttribute("cart", cart);
            }
        }

        response.sendRedirect("cart");
    }

    private int isExisting(int id, List<Item> cart) {
        for (int i = 0; i < cart.size(); i++) {
            if (cart.get(i).getProduct().getId() == id) {
                return i;
            }
        }
        return -1;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
