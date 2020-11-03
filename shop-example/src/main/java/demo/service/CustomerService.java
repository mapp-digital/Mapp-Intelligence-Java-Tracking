package demo.service;

import demo.entities.Personal;
import demo.entities.User;
import demo.models.UserModel;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CustomerService {
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final UserModel userModel;

    public CustomerService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;

        this.userModel = new UserModel();

        this.request.setAttribute("user", null);
        this.request.setAttribute("personal", null);
        this.request.setAttribute("error", null);
        this.request.setAttribute("info", null);

        this.init();
    }

    private void info(String msg) {
        this.request.setAttribute("info", msg);
    }

    private void error(String msg) {
        this.request.setAttribute("error", msg);
    }

    private String decode(String str) {
        String decodedString = str;
        try {
            decodedString = URLDecoder.decode(str, StandardCharsets.UTF_8.toString());

            byte[] bytes = decodedString.getBytes(StandardCharsets.ISO_8859_1.toString());
            decodedString = new String(bytes, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException | NullPointerException e) {
            // do nothing
        }

        return ((decodedString != null) ? decodedString : "");
    }

    private void login(HttpSession session) {
        String email = this.decode(this.request.getParameter("email"));
        String password = this.decode(this.request.getParameter("password"));
        this.request.setAttribute("user", null);
        if (!email.isEmpty() && !password.isEmpty()) {
            User user = this.userModel.login(email, password);
            session.setAttribute("user", user);
            this.request.setAttribute("user", user);

            if (user == null) {
                this.error("Invalid login or password.");
            } else {
                this.info("The login was successful.");
            }
        }
    }

    private void logout(HttpSession session) {
        this.info("The logout was successful.");
        session.setAttribute("user", null);
    }

    private void register(HttpSession session) {
        User user = this.userModel.register(
            this.decode(this.request.getParameter("email")),
            this.decode(this.request.getParameter("password")),
            this.decode(this.request.getParameter("firstname")),
            this.decode(this.request.getParameter("lastname")),
            this.decode(this.request.getParameter("tel"))
        );

        if (user != null) {
            session.setAttribute("user", user);
            this.request.setAttribute("user", user);
            this.info("The registration was successful.");
        } else {
            this.error("There is already an account with this email address.");
        }
    }

    private void contact() {
        this.info("Your inquiry was submitted and will be responded to as soon as possible. Thank you for contacting us.");
    }

    private void personal(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            user = new User();

            user.setFirstName(this.decode(this.request.getParameter("firstname")));
            user.setLastName(this.decode(this.request.getParameter("lastname")));
            user.setEmail(this.decode(this.request.getParameter("email")));
            user.setTelephone(this.decode(this.request.getParameter("tel")));

            this.request.setAttribute("user", user);
        }

        Personal personal = new Personal(user);

        personal.setAddress1(this.decode(this.request.getParameter("address1")));
        personal.setAddress2(this.decode(this.request.getParameter("address2")));
        personal.setCountry(this.decode(this.request.getParameter("country")));
        personal.setCity(this.decode(this.request.getParameter("city")));
        personal.setPostCode(this.decode(this.request.getParameter("postcode")));

        this.request.setAttribute("personal", personal);
    }

    private void init() {
        HttpSession session = this.request.getSession();
        String action = this.request.getParameter("action");

        if (action != null) {
            switch (action) {
                case "login":
                    this.login(session);
                    break;
                case "logout":
                    this.logout(session);
                    break;
                case "register":
                    this.register(session);
                    break;
                case "personal":
                    this.personal(session);
                    break;
                case "contact":
                    this.contact();
                    break;
                default: break;
            }
        }

        User user = (User) session.getAttribute("user");
        if (user != null) {
            this.request.setAttribute("user", user);

            Personal personal = (Personal) this.request.getAttribute("personal");

            TrackingService tracking = (TrackingService) request.getAttribute("tracking");
            tracking.customer(user, personal);
        }
    }
}
