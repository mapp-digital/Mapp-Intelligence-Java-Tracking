package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceRegistration extends AbstractMappIntelligenceData {
    /**
     * The Email address used to identify the user in Mapp Engage.
     */
    private String email = "";
    /**
     * Provide the group ID in case of a new registration for the user in Mapp Engage.
     */
    private String groupId = "";
    /**
     * Provide the registration method used to register for marketing activities.
     * <ul>
     *     <li>"c" (CONFIRMED OPT IN): New contacts receive a welcome message via email when they are added to the
     *     group. The contact does not need to confirm the subscription.</li>
     *     <li>"d" (DOUBLE OPT IN): New contacts receive an invitation to join the group via email. The contact
     *     must accept the invitation before they are added to the group.</li>
     *     <li>"o" (OPT IN): (default) New contacts are added to the group without
     *     notification.</li>
     * </ul>
     */
    private String mode = "";
    /**
     * First name of the user to be used in Mapp Engage.
     */
    private String firstName = "";
    /**
     * Last name of the user to be used in Mapp Engage.
     */
    private String lastName = "";
    /**
     * Gender of the user.
     * <ul>
     *     <li>"u": undisclosed</li>
     *     <li>"f": female</li>
     *     <li>"m": male</li>
     * </ul>
     */
    private String gender = "";
    /**
     * The title of the user to be used in Mapp Engage.
     */
    private String title = "";
    /**
     * Provide information that the user consented to use their data.
     * <ul>
     *     <li>true: User consent</li>
     *     <li>false: No consent</li>
     * </ul>
     */
    private boolean optIn = false;

    /**
     * Default constructor.
     */
    public MappIntelligenceRegistration() {
        // do nothing
    }

    /**
     * @return A list of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();

        if (this.optIn) {
            queryList.put("email", MappIntelligenceParameter.REGISTRATION_EMAIL);
            queryList.put("groupId", MappIntelligenceParameter.REGISTRATION_GROUP_ID);
            queryList.put("mode", MappIntelligenceParameter.REGISTRATION_MODE);
            queryList.put("firstName", MappIntelligenceParameter.REGISTRATION_FIRST_NAME);
            queryList.put("lastName", MappIntelligenceParameter.REGISTRATION_LAST_NAME);
            queryList.put("gender", MappIntelligenceParameter.REGISTRATION_GENDER);
            queryList.put("title", MappIntelligenceParameter.REGISTRATION_TITLE);
        }

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("email", this.email);
        data.put("groupId", this.groupId);
        data.put("mode", this.mode);
        data.put("firstName", this.firstName);
        data.put("lastName", this.lastName);
        data.put("gender", this.gender);
        data.put("title", this.title);
        data.put("optIn", this.optIn);

        return data;
    }

    /**
     * @param e The Email address used to identify the user in Mapp Engage.
     *
     * @return this
     */
    public MappIntelligenceRegistration setEmail(String e) {
        this.email = e;

        return this;
    }

    /**
     * @param g Provide the group ID in case of a new registration for the user in Mapp Engage.
     *
     * @return this
     */
    public MappIntelligenceRegistration setGroupId(String g) {
        this.groupId = g;

        return this;
    }

    /**
     * @param m Provide the registration method used to register for marketing activities.
     *
     * @return this
     */
    public MappIntelligenceRegistration setMode(String m) {
        this.mode = m;

        return this;
    }

    /**
     * @param f First name of the user to be used in Mapp Engage.
     *
     * @return this
     */
    public MappIntelligenceRegistration setFirstName(String f) {
        this.firstName = f;

        return this;
    }

    /**
     * @param l Last name of the user to be used in Mapp Engage.
     *
     * @return this
     */
    public MappIntelligenceRegistration setLastName(String l) {
        this.lastName = l;

        return this;
    }

    /**
     * @param g Gender of the user.
     *
     * @return this
     */
    public MappIntelligenceRegistration setGender(String g) {
        this.gender = g;

        return this;
    }

    /**
     * @param t The title of the user to be used in Mapp Engage.
     *
     * @return this
     */
    public MappIntelligenceRegistration setTitle(String t) {
        this.title = t;

        return this;
    }

    /**
     * @param o Provide information that the user consented to use their data.
     *
     * @return this
     */
    public MappIntelligenceRegistration setOptIn(boolean o) {
        this.optIn = o;

        return this;
    }
}
