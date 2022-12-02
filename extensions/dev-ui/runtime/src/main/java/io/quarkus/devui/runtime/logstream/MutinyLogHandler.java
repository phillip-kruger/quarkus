package io.quarkus.devui.runtime.logstream;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

import io.quarkus.arc.Arc;

/**
 * Log handler for Logger Manager
 */
public class MutinyLogHandler extends ExtHandler {

    private LogStreamBroadcaster logStreamBroadcaster;

    @Override
    public final void doPublish(final ExtLogRecord record) {
        // Don't log empty messages
        if (record.getMessage() == null || record.getMessage().isEmpty()) {
            return;
        }

        if (isLoggable(record)) {
            getBroadcaster().onNext(toStreamingLogEntry(record));
        }
    }

    private StreamingLogEntry toStreamingLogEntry(ExtLogRecord logRecord) {
        StreamingLogEntry streamingLogEntry = new StreamingLogEntry();

        streamingLogEntry.setType(LOG_LINE);
        if (logRecord.getLoggerName() != null) {
            streamingLogEntry.setLoggerNameShort(getShortFullClassName(logRecord.getLoggerName(), EMPTY));
            streamingLogEntry.setLoggerName(logRecord.getLoggerName());
        }
        if (logRecord.getLoggerClassName() != null) {
            streamingLogEntry.setLoggerClassName(logRecord.getLoggerClassName());
        }
        if (logRecord.getHostName() != null) {
            streamingLogEntry.setHostName(logRecord.getHostName());
        }
        if (logRecord.getLevel() != null) {
            streamingLogEntry.setLevel(logRecord.getLevel().getName());
        }
        if (logRecord.getFormattedMessage() != null) {
            streamingLogEntry.setFormattedMessage(logRecord.getFormattedMessage());
        }
        if (logRecord.getMessage() != null) {
            streamingLogEntry.setMessage(logRecord.getMessage());
        }
        streamingLogEntry.setSourceLineNumber(logRecord.getSourceLineNumber());
        if (logRecord.getSourceClassName() != null) {
            String justClassName = getJustClassName(logRecord.getSourceClassName());
            streamingLogEntry.setSourceClassNameFullShort(getShortFullClassName(logRecord.getSourceClassName(), justClassName));
            streamingLogEntry.setSourceClassNameFull(logRecord.getSourceClassName());
            streamingLogEntry.setSourceClassName(justClassName);
        }
        if (logRecord.getSourceFileName() != null) {
            streamingLogEntry.setSourceFileName(logRecord.getSourceFileName());
        }
        if (logRecord.getSourceMethodName() != null) {
            streamingLogEntry.setSourceMethodName(logRecord.getSourceMethodName());
        }
        if (logRecord.getThrown() != null) {
            streamingLogEntry.setStacktrace(getStacktraces(logRecord.getThrown()));
        }
        streamingLogEntry.setThreadId(logRecord.getThreadID());
        streamingLogEntry.setThreadName(logRecord.getThreadName());
        streamingLogEntry.setProcessId(logRecord.getProcessId());
        streamingLogEntry.setProcessName(logRecord.getProcessName());
        streamingLogEntry.setTimestamp(logRecord.getMillis());
        streamingLogEntry.setSequenceNumber(logRecord.getSequenceNumber());

        return streamingLogEntry;
    }

    private List<String> getStacktraces(Throwable t) {
        List<String> traces = new LinkedList<>();
        addStacktrace(traces, t);
        return traces;
    }

    private void addStacktrace(List<String> traces, Throwable t) {
        traces.add(getStacktrace(t));
        if (t.getCause() != null)
            addStacktrace(traces, t.getCause());
    }

    private String getStacktrace(Throwable t) {
        try (StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw)) {
            t.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            return null;
        }
    }

    private String getJustClassName(String fullName) {
        int lastDot = fullName.lastIndexOf(DOT) + 1;
        return fullName.substring(lastDot);
    }

    private String getShortFullClassName(String fullName, String justClassName) {
        String[] parts = fullName.split("\\" + DOT);
        try (StringWriter buffer = new StringWriter()) {
            for (int i = 0; i < parts.length - 1; i++) {
                String part = parts[i];
                if (part.equals(justClassName) || part.length() < 3) {
                    buffer.write(part);
                } else {
                    buffer.write(part.substring(0, 3));
                }
                buffer.write(DOT);
            }
            buffer.write(parts[parts.length - 1]);
            return buffer.toString();
        } catch (IOException ex) {
            return fullName;
        }
    }

    private LogStreamBroadcaster getBroadcaster() {
        synchronized (this) {
            if (this.logStreamBroadcaster == null) {
                this.logStreamBroadcaster = Arc.container().instance(LogStreamBroadcaster.class).get();
            }
        }
        return this.logStreamBroadcaster;
    }

    private static final String DOT = ".";
    private static final String LOG_LINE = "logLine";
    private static final String EMPTY = "";
}
