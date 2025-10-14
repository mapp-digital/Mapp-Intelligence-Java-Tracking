package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Session parameters always refer to a complete session (visit). In case of transmitting a parameter multiple times
 * during a visit, the system only evaluates the first or last value of the parameter during the visit (based on
 * the configuration in your account).
 * <p>
 * An example for the utilisation of a session parameter would be the login status of the user. By default, each visit
 * would be indicated as "not logged-in" at the beginning. The successful login is passed to the same parameter and
 * overwrites the first value.
 * <p>
 * A session parameter cannot be used to add an information to a specific web page, but only to the complete visit.
 * Please use page parameters if you want to analyse information of a specific page of your website or evaluate
 * multiple values of a parameter in the same visit.
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceSession extends AbstractMappIntelligenceData {
    private static final String TEMPORARY_SESSION_ID_TYPE = "2.0.0";

    /**
     * Pass the current users login status here.
     */
    private String loginStatus = "";
    /**
     * In order to keep the session and the user during a single session, we offer the possibility to set a temporary
     * session ID that keeps the session together but is not permanently stored on a device.
     */
    private String temporarySessionId = "";
    /**
     * In order to keep the session and the user during a single session, we offer the possibility to set a temporary
     * session ID that keeps the session together but is not permanently stored on a device.
     */
    private String temporarySessionIdType = "";
    /**
     * Session parameters always refer to a complete session (visit). If the value for the parameter is transmitted
     * during a visit several times, only the first or last value is evaluated, based on the configuration of the
     * Webtrekk GUI.
     */
    private final Map<Integer, String> parameter = new HashMap<>();

    /**
     * Default constructor.
     */
    public MappIntelligenceSession() {
        // do nothing
    }

    /**
     * @return A list of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("loginStatus", MappIntelligenceParameter.LOGIN_STATUS);
        queryList.put("temporarySessionId", MappIntelligenceParameter.TEMPORARY_SESSION_ID);
        queryList.put("temporarySessionIdType", MappIntelligenceParameter.TEMPORARY_SESSION_ID_TYPE);
        queryList.put("parameter", MappIntelligenceParameter.CUSTOM_SESSION_PARAMETER);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("loginStatus", this.loginStatus);
        data.put("temporarySessionId", this.temporarySessionId);
        data.put("temporarySessionIdType", this.temporarySessionIdType);
        data.put("parameter", this.parameter);

        return data;
    }

    /**
     * @param lStatus Pass the current users login status here
     *
     * @return MappIntelligenceSession
     */
    public MappIntelligenceSession setLoginStatus(String lStatus) {
        this.loginStatus = lStatus;

        return this;
    }

    /**
     * Session parameters always refer to a complete session (visit). If the value for the parameter is transmitted
     * during a visit several times, only the first or last value is evaluated, based on the configuration of the
     * Webtrekk GUI
     *
     * @param i ID of the parameter
     * @param v Value of the parameter
     *
     * @return MappIntelligenceSession
     */
    public MappIntelligenceSession setParameter(int i, String v) {
        this.parameter.put(i, v);

        return this;
    }

    /**
     * In order to keep the session and the user during a single session, we offer the possibility to set a temporary
     * session ID that keeps the session together but is not permanently stored on a device.
     *
     * @param tSessionId Pass the temporary session ID here
     *
     * @return MappIntelligenceSession
     */
    public MappIntelligenceSession setTemporarySessionId(String tSessionId) {
        this.temporarySessionId = tSessionId;
        this.temporarySessionIdType = TEMPORARY_SESSION_ID_TYPE;

        return this;
    }
}
