package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public interface MappIntelligenceCookie {
    /**
     * @param domain Specifies the domain within which this cookie should be presented.
     */
    void setDomain(String domain);

    /**
     * @return Gets the domain name of this cookie.
     */
    String getDomain();

    /**
     * Sets the maximum age in seconds for this cookie.
     *
     * @param expiry Sets the maximum age in seconds for this cookie.
     */
    void setMaxAge(int expiry);

    /**
     * Gets the maximum age in seconds of this cookie.
     *
     * @return Gets the maximum age in seconds of this cookie.
     */
    int getMaxAge();

    /**
     * @param uri Specifies a path for the cookie to which the client should return the cookie.
     */
    void setPath(String uri);

    /**
     * @return Returns the path on the server to which the browser returns this cookie.
     */
    String getPath();

    /**
     * @param flag Indicates to the browser whether the cookie should only be sent using a secure protocol, such as
     *             HTTPS or SSL.
     */
    void setSecure(boolean flag);

    /**
     * @return  Returns true if the browser is sending cookies only over a secure protocol, or false if the browser
     *          can send cookies using any protocol.
     */
    boolean isSecure();

    /**
     * @return Returns the name of the cookie.
     */
    String getName();

    /**
     * @return Gets the current value of this cookie.
     */
    String getValue();

    /**
     * @param isHttpOnly Marks or unmarks this cookie as HttpOnly.
     */
    void setHttpOnly(boolean isHttpOnly);

    /**
     * @return Checks whether this cookie has been marked as HttpOnly.
     */
    boolean isHttpOnly();
}
