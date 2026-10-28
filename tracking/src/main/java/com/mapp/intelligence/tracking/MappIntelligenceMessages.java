package com.mapp.intelligence.tracking;

/**
 * @author Mapp Digital c/o Webtrekk GmbH
 * @version 0.0.1
 */
public class MappIntelligenceMessages {
    // Mapp Intelligence tracking
    private static final String REQUIRED_TRACK_ID_AND_DOMAIN = "The Mapp Intelligence \"trackDomain\" and \"trackId\" are required to";
    public static final String TO_LARGE_BATCH_SIZE = "Batch size is larger than %s req. (%s req.)";
    public static final String MAX_BATCH_PAYLOAD_LIMIT_REACHED = "Maximum batch payload limit of 20MB for queue -%s- reached (%sMB with %s req.)";
    public static final String TO_LARGE_PAYLOAD_SIZE = "Payload size is larger than 24MB (%sMB)";
    public static final String GENERIC_ERROR = "%s (%s)";
    public static final String CREATE_NEW_LOG_FILE = "Create new file %s (%s) => %s";
    public static final String USE_EXISTING_LOG_FILE = "Use existing file %s (%s)";
    public static final String CANNOT_RENAME_TEMPORARY_FILE = "Create new file, because cannot rename temporary file";
    public static final String WRITE_BATCH_DATA = "Write batch data in %s (%s req.)";
    public static final String SEND_BATCH_DATA = "Send batch data to %s (%s req.)";
    public static final String BATCH_REQUEST_STATUS = "Batch request responding the status code %s";
    public static final String BATCH_RESPONSE_TEXT = "[%s]: %s";
    public static final String FLUSH_WAS_NOT_SUCCESSFUL = "Flush for queue -%s- was not successful (%s/%s).";
    public static final String TRY_AGAIN = "Try again for queue -%s- in %s milliseconds.";
    public static final String CURRENT_THREAD_WAS_INTERRUPT = "Current thread for queue -%s- was interrupt.";
    public static final String REQUIRED_TRACK_ID_AND_DOMAIN_FOR_COOKIE = REQUIRED_TRACK_ID_AND_DOMAIN + " get user cookie";
    public static final String REQUIRED_TRACK_ID_AND_DOMAIN_FOR_TRACKING = REQUIRED_TRACK_ID_AND_DOMAIN + " track data";
    public static final String TRACKING_IS_DEACTIVATED = "Mapp Intelligence tracking is deactivated";
    public static final String TRACKING_IS_DEACTIVATED_BY_IN_AND_EXCLUDE = "Mapp Intelligence tracking is deactivated by include / exclude";
    public static final String SENT_BATCH_REQUESTS = "Send batch requests, current queue -%s- size is %s req.";
    public static final String BATCH_REQUEST_FAILED = "Batch request for queue -%s- failed!";
    public static final String CURRENT_QUEUE_STATUS = "Batch of %s req. sent, current queue -%s- size is %s req.";
    public static final String QUEUE_IS_EMPTY = "Queue -%s- is empty";
    public static final String ADD_THE_FOLLOWING_REQUEST_TO_QUEUE = "Add the following request to queue -%s- (%s req.): %s";
    public static final String MAPP_INTELLIGENCE = "Mapp Intelligence";
    public static final String UNDEFINED_CONSUMER_TYPE = "Cannot send because no consumer is available or an undefined consumer type was specified.";
    public static final String TASK_REJECTED = "Task rejected, queue -%s- must be processed.";
    public static final String DISCARD_UNPROCESSABLE_BATCH =
        "Discarding a batch of %s req. that can never be sent - retrying it would block the queue forever.";
    public static final String DISCARD_BATCH_ON_CLIENT_ERROR =
        "Discarding a batch of %s req. after HTTP status %s - a client error is permanent, "
        + "retrying would send the identical request and block the queue.";
    public static final String REQUEST_AFTER_CLOSE =
        "Request rejected, the tracking instance is already closed: %s";
    public static final String VALUE_LARGER_THAN_MAXIMUM = "Value %s for %s is larger than the maximum, %s is used";
    public static final String MAX_BATCH_SIZE_OVERWRITTEN =
        "maxBatchSize %s is set to 1, the consumer %s writes every request immediately";
    public static final String MAX_QUEUE_SIZE_REACHED = "Queue -%s- is full (%s req.), the %s oldest requests are discarded";
    public static final String INVALID_PIXEL_REQUEST =
        "Request ignored, it has no pixel parameter \"p\" or is larger than %s bytes: %s";
    public static final String FLUSH_AFTER_CLOSE =
        "Flush ignored, the tracking instance is already closed.";
    public static final String FLUSH_TIMEOUT_ON_CLOSE =
        "Timeout while waiting for queue -%s- to be flushed, remaining requests were not sent.";
    public static final String INSTANCE_IS_NOT_THREAD_SAFE =
        "This tracking instance is used by more than one thread. It carries request data (user agent, "
        + "IP, cookies, ever ID) and is not thread-safe - create one instance per request, otherwise "
        + "data of one user ends up in the requests of another.";

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
    public static final String OPTION_LOG_LEVEL = "If you set this to a particular level, it will show all messages at that level and at higher levels of importance. You can set the following values: 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL' and 'NONE' (case-insensitive, default: 'ERROR').";
    public static final String OPTION_VERSION = "Display version and exit.";
    public static final String REQUEST_LOG_FILES_NOT_FOUND = "Request log files '%s' not found";
    public static final String LOG_FILE_SENT_PARTIALLY =
        "Log file %s was sent partially: %s req. delivered, the %s unsent req. are kept for the next run";
    public static final String CRONJOB_IS_RUNNING =
        "Another cron job run sends the log files in %s (prefix %s), this run ends";
    public static final String CANNOT_READ_LOG_FILE = "Log file %s cannot be read, it is kept for the next run: %s";
    public static final String LOG_FILE_WITH_DISCARDED_REQUESTS =
        "Log file %s contained %s req. that can never be sent, they are kept in %s and not sent again";
    public static final String CANNOT_WRITE_DISCARDED_REQUESTS =
        "The %s req. of log file %s that can never be sent could not be written to %s, the file is sent again with the next run";
    public static final String RENAME_EXPIRED_TEMPORARY_FILE = "Rename expired temporary file into log file";
    public static final String CANNOT_DELETE_SENT_LOG_FILE =
        "Log file %s was sent but could not be deleted, kept as %s and not sent again";
    public static final String CANNOT_REMOVE_SENT_LOG_FILE =
        "Log file %s was sent but could neither be deleted nor renamed, it will be sent again on the next run";
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
