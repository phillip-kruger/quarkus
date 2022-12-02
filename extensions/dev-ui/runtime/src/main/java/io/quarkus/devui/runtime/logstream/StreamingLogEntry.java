package io.quarkus.devui.runtime.logstream;

import java.util.List;

/**
 * Simple POJO that represents a LogRecord. Can be streamed to a client
 */
public class StreamingLogEntry {
    private String type;

    private String level;
    private long sequenceNumber;
    private String hostName;
    private long timestamp;

    private String loggerNameShort;
    private String loggerName;

    private String loggerClassName;
    private String sourceClassNameFull;
    private String sourceClassNameFullShort;
    private String sourceClassName;

    private String sourceMethodName;
    private String sourceFileName;
    private int sourceLineNumber;

    private long processId;
    private String processName;

    private long threadId;
    private String threadName;

    private String message;
    private String formattedMessage;

    private List<String> stacktrace;

    public StreamingLogEntry() {
    }

    public StreamingLogEntry(String type, String level, long sequenceNumber, String hostName, long timestamp,
            String loggerNameShort,
            String loggerName, String loggerClassName, String sourceClassNameFull, String sourceClassNameFullShort,
            String sourceClassName, String sourceMethodName, String sourceFileName, int sourceLineNumber, long processId,
            String processName, long threadId, String threadName, String message, String formattedMessage,
            List<String> stacktrace) {
        this.type = type;
        this.level = level;
        this.sequenceNumber = sequenceNumber;
        this.hostName = hostName;
        this.timestamp = timestamp;
        this.loggerNameShort = loggerNameShort;
        this.loggerName = loggerName;
        this.loggerClassName = loggerClassName;
        this.sourceClassNameFull = sourceClassNameFull;
        this.sourceClassNameFullShort = sourceClassNameFullShort;
        this.sourceClassName = sourceClassName;
        this.sourceMethodName = sourceMethodName;
        this.sourceFileName = sourceFileName;
        this.sourceLineNumber = sourceLineNumber;
        this.processId = processId;
        this.processName = processName;
        this.threadId = threadId;
        this.threadName = threadName;
        this.message = message;
        this.formattedMessage = formattedMessage;
        this.stacktrace = stacktrace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLoggerNameShort() {
        return loggerNameShort;
    }

    public void setLoggerNameShort(String loggerNameShort) {
        this.loggerNameShort = loggerNameShort;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }

    public String getLoggerClassName() {
        return loggerClassName;
    }

    public void setLoggerClassName(String loggerClassName) {
        this.loggerClassName = loggerClassName;
    }

    public String getSourceClassNameFull() {
        return sourceClassNameFull;
    }

    public void setSourceClassNameFull(String sourceClassNameFull) {
        this.sourceClassNameFull = sourceClassNameFull;
    }

    public String getSourceClassNameFullShort() {
        return sourceClassNameFullShort;
    }

    public void setSourceClassNameFullShort(String sourceClassNameFullShort) {
        this.sourceClassNameFullShort = sourceClassNameFullShort;
    }

    public String getSourceClassName() {
        return sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public String getSourceMethodName() {
        return sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public int getSourceLineNumber() {
        return sourceLineNumber;
    }

    public void setSourceLineNumber(int sourceLineNumber) {
        this.sourceLineNumber = sourceLineNumber;
    }

    public long getProcessId() {
        return processId;
    }

    public void setProcessId(long processId) {
        this.processId = processId;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public long getThreadId() {
        return threadId;
    }

    public void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFormattedMessage() {
        return formattedMessage;
    }

    public void setFormattedMessage(String formattedMessage) {
        this.formattedMessage = formattedMessage;
    }

    public List<String> getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(List<String> stacktrace) {
        this.stacktrace = stacktrace;
    }
}
