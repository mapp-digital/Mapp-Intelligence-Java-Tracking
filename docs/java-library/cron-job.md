# Cron Job

> Based on the public documentation <https://docs.mapp.com/docs/cron-job> (retrieved 2026-09-29), updated to the current code of this repository.

We recommend using a cron job to send the logfile requests to the Mapp tracking server every few minutes and empty your file to keep it at a manageable size. The cron job reads the request log files written by the file consumer (see [Data Transfer: Consumer](data-transfer-consumer.md)) and by `MappIntelligenceHybrid` (see [Classes](classes.md)). You have two possibilities:

## Custom Cron Job File

This option requires you to write your own cron job file and use our *MappIntelligenceCronjob* class.

### Integration

#### Maven

```xml
<dependency>
    <groupId>com.mapp.intelligence.tracking</groupId>
    <artifactId>cronjob</artifactId>
    <version>1.1.0</version>
</dependency>
```

#### Gradle

```groovy
implementation group: 'com.mapp.intelligence.tracking', name: 'cronjob', version: '1.1.0'
```

### Methods

#### constructor

For a complete configuration of the Mapp Intelligence Cronjob please check [Configuration](integration-and-configuration.md).

The track ID and the track domain are required, otherwise a `MappIntelligenceException` is thrown. Without a file path, the directory of the system property `java.io.tmpdir` is used; without a file prefix, `MappIntelligenceRequests`. The cron job sets the consumer type to `HTTP_CLIENT` regardless of the configuration (a consumer passed in via `setConsumer` is still used instead, and is not closed by the cron job), sends batches of up to 1000 requests (`maxBatchSize` 1000) and uses an unlimited queue (`maxQueueSize` 0), so no request of a log file is discarded because of the queue size, and sends without `requestInterval` (the result of every file is decided by the flush at its end). Settings such as `requestQueues`, `maxAttempt`, `attemptTimeout`, `connectionTimeout`, `readTimeout`, `maxFileDuration`, `logLevel` and `logger` are taken from the configuration. `requestQueues`, `connectionTimeout`, `readTimeout` and `logger` have no config file key (see [Configuration](integration-and-configuration.md)): they can only be set with `new MappIntelligenceCronjob(config)`, not with the [predefined cron job script](#predefined-cron-job-script) and `-c` (there, `--debug` sets the logger).

```java
/**
 * @param config Mapp Intelligence config
 * @throws MappIntelligenceException Mapp Intelligence configuration exception
 */
MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(MappIntelligenceConfig config);
```

Alternatively, you can pass the command-line arguments of the [predefined cron job script](#predefined-cron-job-script):

```java
/**
 * @param args Command line arguments
 * @throws MappIntelligenceException Mapp Intelligence configuration exception
 */
MappIntelligenceCronjob cronjob = new MappIntelligenceCronjob(String[] args);
```

#### run

Send the logfile requests to the Mapp tracking server and delete your logfiles to keep it at a manageable size. Returns the following results:

- **0**: All data was sent successfully, there were no log files to send, another run is still sending the files of the same file path and prefix (this run sends nothing), or tracking is deactivated via `setDeactivate(true)` (nothing is sent).
- **1**: An error has occurred.

The cron job processes all files `<filePrefix>-<timestamp>….log` in the file path, in the order of their names. Only files named after the prefix, a `-` and then the digits of the timestamp belong to a prefix, so the prefix `Shop` takes neither the files of `ShopDE` nor those of `Shop-DE` (the same rule as for the file consumer, see [Data Transfer: Consumer](data-transfer-consumer.md)). Temporary files (`<filePrefix>-<timestamp>….tmp`) that are older than the configured `maxFileDuration` (default 30 minutes, according to the 13-digit timestamp in the file name) are renamed to `.log` first, so that they are sent as well; a `.tmp` file whose timestamp does not have 13 digits is always renamed. A successful rename is logged at level INFO (`Rename expired temporary file into log file`). Each log file is read completely and split into requests at the platform line separator (empty lines are skipped); the outcome per file is:

| Outcome | File | Run |
|---|---|---|
| Every request queue reported success | The file is deleted. If it cannot be deleted, it is renamed to `*.log.sent` (logged at level WARN) and not sent again. | Continues with the next file. If the file can neither be deleted nor renamed, an error is logged, the file stays as `.log` and is sent again by the next run; the run stops with exit status 1. |
| Requests could not be delivered, but may still succeed (for example network errors or server errors after all attempts) | The file is kept as `.log` with the requests that were not delivered, and is sent by the next run. | Stops, exit status 1. The remaining files are kept for the next run, in order. |
| At least one batch was discarded because it can never be sent (permanent client error, oversized batch) | Its requests - only these, not the delivered ones, in the order of the log file - are appended to `*.log.discarded` (created if it does not exist yet, logged at level ERROR): kept for inspection, but not picked up again. If nothing is left to retry, the log file is deleted (or renamed to `*.log.sent`, as in the first row). If requests are left to retry as well, the file is kept as `.log` with only those, as in the second row; a later run appends its discarded requests to the same `*.log.discarded`. If the discarded requests cannot be written, the file is kept unchanged as `.log` (logged at level ERROR) and sent again by the next run. | Nothing left to retry: continues with the next file, exit status 1. Requests left to retry, or the discarded requests cannot be written: stops, exit status 1. |
| The file cannot be read | The file is kept and the error is logged at level ERROR. | Stops, exit status 1. |

A file that was sent partially (some batches delivered, then a batch that could not be delivered) keeps only the requests that were not delivered, in the order of the file (logged at level INFO), so the next run does not send the delivered ones again. The remaining requests are written into a helper file `<file>.log.<random>.part` first, which then replaces the log file - atomically if the file system supports an atomic move (`ATOMIC_MOVE`), otherwise with a normal replace. If the log file cannot be replaced, the error is logged at level ERROR, the file keeps its old content and the next run sends the whole file again. A helper file is deleted afterwards; only if the cron job is killed between writing it and moving it, it is left behind. Such a `.part` file is not processed by the cron job (it is not a `.log` file) and can be deleted. If the cron job is killed in the middle of a file before it is replaced, the whole file is sent again with the next run.

Only one run at a time sends the files of a file path and prefix: a run holds an exclusive lock on `<filePath>/<filePrefix>.cronjob.lock`. A run that starts while another one is still sending (e.g. with a slow track server) ends right away with exit status **0** and an INFO message, without sending anything. The operating system releases the lock when the process ends, also if it is killed. The lock file is created by the first run and stays in the directory permanently; do not delete it while a run is active. If it cannot be created (for example because the file path does not exist or is not writable), the run logs `Request log files '<filePath>' not found` at level ERROR and returns **1**. A run with `setDeactivate(true)` takes no lock and does not create the lock file.

Requests that were not delivered are not sent once more when the run ends - the kept log file is the copy that counts. If the thread running the cron job is interrupted while it waits for a file to be sent, the file is kept for the next run and `run()` returns **1**.

> [!NOTE]
> If no log files are found, `run()` returns **0** (the message "Request log files '...' not found" is logged at level INFO): nothing to send is not an error, so monitoring does not raise an alarm on every quiet run. If the file path does not exist or is not writable (the lock file cannot be created, see above) or cannot be read, the same message is logged at level ERROR and `run()` returns **1**.
>
> Expired temporary files are renamed under the same file lock the writers use (`<filePrefix>.lock`), so no request is written into a file at the moment it is taken over, and a rename never replaces an existing file.

```java
/**
 * @return Status
 */
cronjob.run();
```

##### Example

```java
package demo.cronjob;

import com.mapp.intelligence.tracking.MappIntelligenceCronjob;
import com.mapp.intelligence.tracking.config.MappIntelligenceConfig;

public class MappIntelligenceCronjobCommandLine {
	public static void main(String[] args) {
        int status = 1;
        try {
			MappIntelligenceConfig mappIntelligenceConfig = new MappIntelligenceConfig();
			mappIntelligenceConfig.setTrackId("111111111111111");
			mappIntelligenceConfig.setTrackDomain("analytics01.wt-eu02.net");
			mappIntelligenceConfig.setFilePath("/path/to/your/log/file/");
			mappIntelligenceConfig.setFilePrefix("MappIntelligenceData");

            MappIntelligenceCronjob c = new MappIntelligenceCronjob(mappIntelligenceConfig);
            status = c.run();
        } catch (Exception e) {
            // do nothing
        }

        System.exit(status);
    }
}
```

## Predefined Cron Job Script

This option allows you to use our predefined `mapp-intelligence-java-cronjob.jar` file (an executable jar that contains all dependencies).

> [!IMPORTANT]
> The jar in the folder `dist/` of the repository is an older build and does not contain the behavior described on this page for version 1.1.0. Build the jar yourself from the repository root with `mvn -pl cronjob -am package`; the executable jar is written to `cronjob/target/mapp-intelligence-java-cronjob.jar`. `make build` builds it in a docker container and also copies it to `dist/`.

| Short notation | Long notation | Description |
|---|---|---|
| -i | --trackId | Enter your Mapp Intelligence track ID provided by Mapp. |
| -d | --trackDomain | Enter your Mapp Intelligence tracking domain. |
| -f | --filePath | Enter the path to your request logging files (default: the directory of `java.io.tmpdir`). |
| -p | --filePrefix | Enter the prefix for your request logging files (default: `MappIntelligenceRequests`). |
|  |  |  |
| -c | --config | Enter the path to your configuration file (*.properties or *.xml). The other options override the values of the file. |
|  |  |  |
|  | --debug | Activates the debug mode. The debug mode sends messages to the command line. |
| -ll | --logLevel | If you set this to a particular level, it will show all messages at that level and at higher levels of importance. You can set the following values: 'DEBUG', 'INFO', 'WARN', 'ERROR', 'FATAL' and 'NONE' (case-insensitive, default: 'ERROR'). An invalid value is ignored (the level stays 'ERROR' or the value of the configuration file). |
|  | --help | Display the help (this text) and exit with status 0, nothing is sent. |
|  | --version | Display version and exit with status 0, nothing is sent. |

The exit status of the script is the result of ***run*** (see above). Additionally, it exits with status **1** without a message if the configuration is invalid (for example missing track ID or track domain), and with the help text and status **1** if an unknown option is passed.

> [!NOTE]
> - `--debug` only defines where the messages go: they are written via `java.util.logging` (a `ConsoleHandler`) to **stderr**. Without `--debug` the script prints no messages at all, whatever the log level. Which messages are shown is controlled by `--logLevel` (or `logLevel` in the configuration file), which defaults to `ERROR` - use `--debug --logLevel DEBUG` to see all messages.
> - `--help` and `--version` print their output and end the program with exit status 0, also together with a complete configuration (nothing is sent). This applies to the script (`MappIntelligenceCronjobCommandLine`); `new MappIntelligenceCronjob(args)` still prints them and continues with the configuration.

##### Example

```bash
$ java -jar ./mapp-intelligence-java-cronjob.jar -i 111111111111111 -d analytics01.wt-eu02.net -f /tmp/ -p MappIntelligenceData

$ java -jar ./mapp-intelligence-java-cronjob.jar -i 111111111111111 -d analytics01.wt-eu02.net -f /tmp/ -p MappIntelligenceData --debug

$ java -jar ./mapp-intelligence-java-cronjob.jar -i 111111111111111 -d analytics01.wt-eu02.net -f /tmp/ -p MappIntelligenceData --debug --logLevel DEBUG

$ java -jar ./mapp-intelligence-java-cronjob.jar -c /path/to/your/configuration/file/config.properties

$ java -jar ./mapp-intelligence-java-cronjob.jar -c /path/to/your/configuration/file/config.properties --debug
```

## Define a Cron Job

To create your crontab file, type the following command at the shell prompt `crontab -e` and add the following lines:

```bash
# sends every five minutes the tracking data to the Mapp tracking server
*/5 * * * * java -jar ./mapp-intelligence-java-cronjob.jar -c /path/to/your/configuration/file/config.properties
```
