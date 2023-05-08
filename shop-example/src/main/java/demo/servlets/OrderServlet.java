package demo.servlets;

import demo.entities.Item;
import demo.service.TrackingService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
@WebServlet("/order/*")
public class OrderServlet  extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int MIN_RANDOM = 10000;
    private static final int MAX_RANDOM = 99999;

    public OrderServlet() {
        super();
    }

    private long getTimestamp() {
        return (new Date()).getTime();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        List<Item> cart = (List<Item>) session.getAttribute("cart");

        if (cart != null && cart.size() > 0) {
            String orderId = this.getTimestamp() + "" + ThreadLocalRandom.current().nextInt(MIN_RANDOM, MAX_RANDOM);

            TrackingService tracking = (TrackingService) request.getAttribute("tracking");
            tracking.order(orderId, cart);

            request.setAttribute("order_id", orderId);
            session.setAttribute("cart", new ArrayList<Item>());
        }

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}
