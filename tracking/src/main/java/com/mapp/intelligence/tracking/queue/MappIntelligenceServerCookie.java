package com.mapp.intelligence.tracking.queue;

import com.mapp.intelligence.tracking.MappIntelligenceCookie;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceServerCookie implements MappIntelligenceCookie {
    /**
     * Name of the cookie.
     */
    private final String name;
    /**
     * Value of the cookie.
     */
    private final String value;
    /**
     * The domain within which this cookie should be presented.
     */
    private String domain = "";
    /**
     * The maximum age in seconds for this cookie.
     */
    private int expiry;
    /**
     * The path on the server to which the browser returns this cookie.
     */
    private String path = "";
    /**
     * true if the browser is sending cookies only over a secure protocol, or false if the browser can send
     * cookies using any protocol.
     */
    private boolean secure;
    /**
     * Marks or unmarks this cookie as HttpOnly.
     */
    private boolean httpOnly;

    /**
     * @param n Name of the cookie.
     * @param v Value of this cookie.
     */
    MappIntelligenceServerCookie(String n, String v) {
        this.name = n;
        this.value = v;
    }

    /**
     * @param d Specifies the domain within which this cookie should be presented.
     */
    @Override
    public void setDomain(String d) {
        this.domain = d;
    }

    /**
     * @return Gets the domain name of this cookie.
     */
    @Override
    public String getDomain() {
        return this.domain;
    }

    /**
     * Sets the maximum age in seconds for this cookie.
     *
     * @param e Sets the maximum age in seconds for this cookie.
     */
    @Override
    public void setMaxAge(int e) {
        this.expiry = e;
    }

    /**
     * Gets the maximum age in seconds of this cookie.
     *
     * @return Gets the maximum age in seconds of this cookie.
     */
    @Override
    public int getMaxAge() {
        return this.expiry;
    }

    /**
     * @param p Specifies a path for the cookie to which the client should return the cookie.
     */
    @Override
    public void setPath(String p) {
        this.path = p;
    }

    /**
     * @return Returns the path on the server to which the browser returns this cookie.
     */
    @Override
    public String getPath() {
        return this.path;
    }

    /**
     * @param s Indicates to the browser whether the cookie should only be sent using a secure protocol,
     *          such as HTTPS or SSL.
     */
    @Override
    public void setSecure(boolean s) {
        this.secure = s;
    }

    /**
     * @return  Returns true if the browser is sending cookies only over a secure protocol, or false if the browser
     *          can send cookies using any protocol.
     */
    @Override
    public boolean isSecure() {
        return this.secure;
    }

    /**
     * @return Returns the name of the cookie.
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return Gets the current value of this cookie.
     */
    @Override
    public String getValue() {
        return this.value;
    }

    /**
     * @param isHttpOnly Marks or unmarks this cookie as HttpOnly.
     */
    @Override
    public void setHttpOnly(boolean isHttpOnly) {
        this.httpOnly = isHttpOnly;
    }

    /**
     * @return Checks whether this cookie has been marked as HttpOnly.
     */
    @Override
    public boolean isHttpOnly() {
        return this.httpOnly;
    }
}
