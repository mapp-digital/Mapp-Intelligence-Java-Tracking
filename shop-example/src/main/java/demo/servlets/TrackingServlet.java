package demo.servlets;

import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceCookie;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet("/img/*")
public class TrackingServlet  extends HttpServlet {
    public TrackingServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        MappIntelligenceConfig mappConfig = new MappIntelligenceConfig()
            .setTrackId("123451234512345")
            .setTrackDomain("analytics01.wt-eu02.net")
            .setReferrerURL(request.getHeader("Referer"))
            .setRequestURL(request.getRequestURL().toString())
            .setRemoteAddress(request.getRemoteAddr())
            .setUserAgent(request.getHeader("User-Agent"));

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                mappConfig.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        MappIntelligenceHybrid mappHybrid = new MappIntelligenceHybrid(mappConfig);

        mappHybrid.track();

        response.setContentType("image/gif");
        response.setContentLength(43);

        MappIntelligenceCookie mappUserCookie = mappHybrid.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);
        if (mappUserCookie != null) {
            Cookie mappUserCookieResponse = new Cookie(mappUserCookie.getName(), mappUserCookie.getValue());
            mappUserCookieResponse.setHttpOnly(mappUserCookie.isHttpOnly());
            mappUserCookieResponse.setSecure(mappUserCookie.isSecure());
            mappUserCookieResponse.setDomain(mappUserCookie.getDomain());
            mappUserCookieResponse.setPath(mappUserCookie.getPath());
            mappUserCookieResponse.setMaxAge(mappUserCookie.getMaxAge());
            response.addCookie(mappUserCookieResponse);
        }

        byte[] imgBytes = mappHybrid.getResponseAsBytes();
        OutputStream out = response.getOutputStream();
        BufferedImage bufImg = ImageIO.read(new ByteArrayInputStream(imgBytes));
        ImageIO.write(bufImg, "gif", out);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}

