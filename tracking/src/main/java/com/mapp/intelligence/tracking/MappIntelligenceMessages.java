package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceMessages {
    // Mapp Intelligence tracking
    private static final String REQUIRED_TRACK_ID_AND_DOMAIN = "The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to";
    public static final String TO_LARGE_BATCH_SIZE = "Batch size is larger than %s req. (%s req.)";
    public static final String TO_LARGE_PAYLOAD_SIZE = "Payload size is larger than 24MB (%sMB)";
    public static final String GENERIC_ERROR = "%s (%s)";
    public static final String CREATE_NEW_LOG_FILE = "Create new file %s (%s) => %s";
    public static final String USE_EXISTING_LOG_FILE = "Use existing file %s (%s)";
    public static final String CANNOT_RENAME_TEMPORARY_FILE = "Create new file, because cannot rename temporary file";
    public static final String WRITE_BATCH_DATA = "Write batch data in %s (%s req.)";
    public static final String SEND_BATCH_DATA = "Send batch data to %s (%s req.)";
    public static final String BATCH_REQUEST_STATUS = "Batch request responding the status code %s";
    public static final String BATCH_RESPONSE_TEXT = "[%s]: %s";
    public static final String REQUIRED_TRACK_ID_AND_DOMAIN_FOR_COOKIE = REQUIRED_TRACK_ID_AND_DOMAIN + " get user cookie";
    public static final String REQUIRED_TRACK_ID_AND_DOMAIN_FOR_TRACKING = REQUIRED_TRACK_ID_AND_DOMAIN + " track data";
    public static final String TRACKING_IS_DEACTIVATED = "Mapp Intelligence tracking is deactivated";
    public static final String TRACKING_IS_DEACTIVATED_BY_IN_AND_EXCLUDE = "Mapp Intelligence tracking is deactivated by include / exclude";
    public static final String SENT_BATCH_REQUESTS = "Send batch requests, current queue size is %s req.";
    public static final String BATCH_REQUEST_FAILED = "Batch request failed!";
    public static final String CURRENT_QUEUE_STATUS = "Batch of %s req. sent, current queue size is %s req.";
    public static final String QUEUE_IS_EMPTY = "MappIntelligenceQueue is empty";
    public static final String ADD_THE_FOLLOWING_REQUEST_TO_QUEUE = "Add the following request to queue (%s req.): %s";
    public static final String MAPP_INTELLIGENCE = "Mapp Intelligence";

    // Mapp Intelligence cronjob
    public static final String REQUIRED_TRACK_ID = "Argument \"-i\" or alternative \"--trackId\" are required";
    public static final String REQUIRED_TRACK_DOMAIN = "Argument \"-d\" or alternative \"--trackDomain\" are required";
    public static final String OPTION_TRACK_ID = "Enter your Mapp Intelligence track ID provided by Mapp.";
    public static final String OPTION_TRACK_DOMAIN = "Enter your Mapp Intelligence tracking domain.";
    public static final String OPTION_CONFIG = "Enter the path to your configuration file (*.properties or *.xml).";
    public static final String OPTION_FILE_PATH = "Enter the path to your request logging files.";
    public static final String OPTION_FILE_PREFIX = "Enter the prefix for your request logging files.";
    public static final String OPTION_HELP = "Display the help (this text) and exit.";
    public static final String OPTION_DEBUG = "Activates the debug mode. The debug mode sends messages to the command line.";
    public static final String OPTION_LOG_LEVEL = "If you set this to a particular level, it will show all messages at that level and at higher levels of importance. You can set the following values: 'DEBUG', 'INFO', 'WARN', 'ERROR' and 'FATAL'.";
    public static final String OPTION_VERSION = "Display version and exit.";
    public static final String REQUEST_LOG_FILES_NOT_FOUND = "Request log files '%s' not found";
    public static final String RENAME_EXPIRED_TEMPORARY_FILE = "Rename expired temporary file into log file";
    public static final String HELP_SYNTAX = "java -jar mapp-intelligence-java-cronjob.jar";
    public static final String HELP_HEADER = "Send the logfile requests to the Mapp tracking server and delete your logfiles to keep it at a manageable size.\n\n";
    public static final String HELP_FOOTER = "";

    /**
     * Default constructor.
     */
    private MappIntelligenceMessages() {
        // do nothing
    }
}
