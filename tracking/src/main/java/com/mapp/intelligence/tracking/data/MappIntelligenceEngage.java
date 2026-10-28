package com.mapp.intelligence.tracking.data;

import com.mapp.intelligence.tracking.MappIntelligenceParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * You can transmit attributes and trigger events in real-time to enrich customer profiles and activate automation flows.
 * <p>
 * Use cases include:
 * <ul>
 *     <li>Send dynamic user attributes to Mapp Engage, such as preferences or behavior (e.g., "Size Finder used")
 *     for improved segmentation and targeting.</li>
 *     <li>Trigger a Custom Web Event when a user interacts with a specific feature (e.g., virtual fitting room),
 *     which can initiate an automation workflow in Engage.</li>
 * </ul>
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceEngage extends AbstractMappIntelligenceData {
    /**
     * Sends user-specific data to Mapp Engage, which can be stored as Custom Profile Attributes. These attributes
     * can be created and managed in Engage.
     */
    private final Map<String, String> attributes = new HashMap<>();
    /**
     * In order to keep the session and the user during a single session, we offer the possibility to set a temporary
     * session ID that keeps the session together but is not permanently stored on a device.
     */
    private String eventName = "";
    /**
     * In order to keep the session and the user during a single session, we offer the possibility to set a temporary
     * session ID that keeps the session together but is not permanently stored on a device.
     */
    private int eventId = 0;


    /**
     * Default constructor.
     */
    public MappIntelligenceEngage() {
        // do nothing
    }

    /**
     * Escapes a string for use inside a JSON string literal (RFC 8259). Without this, an attribute
     * containing a quote or a backslash produces broken JSON in the "eaj" parameter, and allows
     * injecting further fields.
     *
     * @param value Value to escape
     *
     * @return String
     */
    private String escapeJSON(String value) {
        StringBuilder escaped = new StringBuilder(value.length());

        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);

            switch (c) {
                case '"':
                    escaped.append("\\\"");
                    break;
                case '\\':
                    escaped.append("\\\\");
                    break;
                case '\b':
                    escaped.append("\\b");
                    break;
                case '\f':
                    escaped.append("\\f");
                    break;
                case '\n':
                    escaped.append("\\n");
                    break;
                case '\r':
                    escaped.append("\\r");
                    break;
                case '\t':
                    escaped.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
            }
        }

        return escaped.toString();
    }

    /**
     * @param attr Attributes to serialize
     *
     * @return String
     */
    private String toJSON(Map<String, String> attr) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        for (Map.Entry<String, String> entry : attr.entrySet()) {
            jsonBuilder.append("\"")
                .append(this.escapeJSON(entry.getKey()))
                .append("\":\"")
                .append(this.escapeJSON(entry.getValue()))
                .append("\",");
        }

        if (jsonBuilder.length() > 1) {
            jsonBuilder.setLength(jsonBuilder.length() - 1);
        }

        jsonBuilder.append("}");

        return jsonBuilder.toString();
    }

    /**
     * @return A list of query strings
     */
    protected Map<String, String> getQueryList() {
        Map<String, String> queryList = new HashMap<>();
        queryList.put("attributes", MappIntelligenceParameter.ENGAGE_ATTRIBUTES);
        queryList.put("eventName", MappIntelligenceParameter.ENGAGE_EVENT_NAME);
        queryList.put("eventId", MappIntelligenceParameter.ENGAGE_EVENT_ID);

        return queryList;
    }

    /**
     * @return Data as object
     */
    protected Map<String, Object> toMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("attributes", this.toJSON(this.attributes));
        data.put("eventName", this.eventName);
        data.put("eventId", this.eventId);

        return data;
    }

    /**
     * Takes over any value that can be represented as an attribute. Every value is serialized as a
     * JSON string, which is the format the "eaj" parameter has always used.
     *
     * @param a Key for the Engage Custom Profile Attribute
     * @param v Value for the Engage Custom Profile Attribute
     */
    private void addAttribute(String a, Object v) {
        if (a == null || v == null) {
            return;
        }

        if (v instanceof String && ((String) v).isEmpty()) {
            return;
        }

        // NaN and infinity have no JSON representation and are meaningless as an attribute value
        if (v instanceof Double && !Double.isFinite((Double) v)) {
            return;
        }

        if (v instanceof Float && !Float.isFinite((Float) v)) {
            return;
        }

        this.attributes.put(a, String.valueOf(v));
    }

    /**
     * @param a Sends user-specific data to Mapp Engage, which can be stored as Custom Profile Attributes.
     *          These attributes can be created and managed in Engage.
     *
     * @return MappIntelligenceEngage
     */
    public MappIntelligenceEngage setAttributes(Map<String, Object> a) {
        this.attributes.clear();

        return this.addAttributes(a);
    }

    /**
     * @param a Sends user-specific data to Mapp Engage, which can be stored as Custom Profile Attributes.
     *          These attributes can be created and managed in Engage.
     *
     * @return MappIntelligenceEngage
     */
    public MappIntelligenceEngage addAttributes(Map<String, Object> a) {
        if (a == null) {
            return this;
        }

        for (Map.Entry<String, Object> entry : a.entrySet()) {
            this.addAttribute(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * @param e Defines the name of the Engage Custom Web Event (e.g. "Size Finder Used"). This can trigger
     *          automations in the Whiteboard.
     *
     * @return MappIntelligenceEngage
     */
    public MappIntelligenceEngage setEventName(String e) {
        this.eventName = e;

        return this;
    }

    /**
     * @param e Alternative to eventName. Refers to the Engage Custom Web Event ID as defined in Engage. Mostly
     *          used for advanced setups.
     *
     * @return MappIntelligenceEngage
     */
    public MappIntelligenceEngage setEventId(int e) {
        this.eventId = e;

        return this;
    }
}
