package io.quarkus.devui.runtime.logstream;

import javax.enterprise.context.ApplicationScoped;

import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@ApplicationScoped
public class LogStreamBroadcaster {

    private final BroadcastProcessor<StreamingLogEntry> logStream = BroadcastProcessor.create();

    public BroadcastProcessor<StreamingLogEntry> getLogStream() {
        return this.logStream;
    }

    public void onNext(StreamingLogEntry toStreamingLogEntry) {
        this.logStream.onNext(toStreamingLogEntry);
    }

}
