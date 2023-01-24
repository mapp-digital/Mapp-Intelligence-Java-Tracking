package com.mapp.intelligence.tracking.config;

import com.mapp.intelligence.tracking.consumer.MappIntelligenceConsumerType;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Private class to read properties from config file (*.properties or *.xml).
 *
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
class MappIntelligenceConfigProperties {
    /**
     * Properties object.
     */
    private final Properties prop;

    /**
     * @param propertyFile Property file path
     */
    MappIntelligenceConfigProperties(String propertyFile) {
        this.prop = new Properties();
        try {
            InputStream inputStream = new FileInputStream(propertyFile);
            if (propertyFile.endsWith(".xml")) {
                this.prop.loadFromXML(inputStream);
            } else {
                this.prop.load(inputStream);
            }
        } catch (IOException e) {
            // do nothing
        }
    }

    /**
     * @param propertyName Name of the property
     *
     * @return String
     */
    private String getProperty(String propertyName) {
        String propertyValue = prop.getProperty(propertyName);

        return ((propertyValue != null) ? propertyValue : "");
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     *
     * @return String
     */
    public String getStringProperty(String propertyName, String defaultValue) {
        String propertyValue = this.getProperty(propertyName);

        return ((!propertyValue.isEmpty()) ? propertyValue : defaultValue);
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     *
     * @return boolean
     */
    public boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        String propertyValue = this.getProperty(propertyName);

        return ((!propertyValue.isEmpty()) ? "true".equals(propertyValue) : defaultValue);
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     *
     * @return boolean
     */
    public int getIntegerProperty(String propertyName, int defaultValue) {
        String propertyValue = this.getProperty(propertyName);

        return ((propertyValue.matches("^\\d+$")) ? Integer.parseInt(propertyValue) : defaultValue);
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     *
     * @return MappIntelligenceConsumer
     */
    public String getConsumerTypeProperty(String propertyName, String defaultValue) {
        String consumerValue = defaultValue;
        String propertyValue = this.getProperty(propertyName);

        switch (propertyValue) {
            case MappIntelligenceConsumerType.FILE:
                consumerValue = MappIntelligenceConsumerType.FILE;
                break;
            case MappIntelligenceConsumerType.HTTP_CLIENT:
                consumerValue = MappIntelligenceConsumerType.HTTP_CLIENT;
                break;
            default:
                break;
        }

        return consumerValue;
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     *
     * @return MappIntelligenceConsumer
     */
    public List<String> getListProperty(String propertyName, List<String> defaultValue) {
        return getListProperty(propertyName, defaultValue, ",");
    }

    /**
     * @param propertyName Name of the property
     * @param defaultValue Default value for the property
     * @param splitTerm Value for string splitting
     *
     * @return MappIntelligenceConsumer
     */
    public List<String> getListProperty(String propertyName, List<String> defaultValue, String splitTerm) {
        List<String> listValue = defaultValue;
        String propertyValue = this.getProperty(propertyName);

        if (!propertyValue.isEmpty()) {
            listValue = Arrays.asList(propertyValue.split(splitTerm));
        }

        return listValue;
    }
}
