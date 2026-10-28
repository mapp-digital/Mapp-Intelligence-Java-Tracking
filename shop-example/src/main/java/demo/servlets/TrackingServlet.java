package demo.servlets;

import com.mapp.intelligence.tracking.MappIntelligence;
import com.mapp.intelligence.tracking.MappIntelligenceCookie;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;
import com.mapp.intelligence.tracking.core.MappIntelligenceHybrid;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
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
            // with the query string: the hybrid forwards the query parameters of the pixel request
            .setRequestURL(
                request.getRequestURL() + (request.getQueryString() != null ? "?" + request.getQueryString() : "")
            )
            .setRemoteAddress(request.getRemoteAddr())
            .setUserAgent(request.getHeader("User-Agent"));

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                mappConfig.addCookie(cookie.getName(), cookie.getValue());
            }
        }

        // the instance carries the data of this request, so it is created and closed per request
        try (MappIntelligenceHybrid mappHybrid = new MappIntelligenceHybrid(mappConfig)) {
            mappHybrid.track();

            byte[] imgBytes = mappHybrid.getResponseAsBytes();

            response.setContentType("image/gif");
            response.setContentLength(imgBytes.length);

            MappIntelligenceCookie mappUserCookie = mappHybrid.getUserIdCookie(MappIntelligence.V4, MappIntelligence.CLIENT_SIDE_COOKIE);
            if (mappUserCookie != null) {
                Cookie mappUserCookieResponse = new Cookie(mappUserCookie.getName(), mappUserCookie.getValue());
                mappUserCookieResponse.setHttpOnly(mappUserCookie.isHttpOnly());
                mappUserCookieResponse.setSecure(mappUserCookie.isSecure());
                // the v4 and smart pixel cookies have no domain (empty string)
                if (!mappUserCookie.getDomain().isEmpty()) {
                    mappUserCookieResponse.setDomain(mappUserCookie.getDomain());
                }
                mappUserCookieResponse.setPath(mappUserCookie.getPath());
                mappUserCookieResponse.setMaxAge(mappUserCookie.getMaxAge());
                response.addCookie(mappUserCookieResponse);
            }

            // write the bytes as they are: re-encoding them would not match the content length above
            OutputStream out = response.getOutputStream();
            out.write(imgBytes);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }
}

