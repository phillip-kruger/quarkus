package io.quarkus.devui.runtime.logstream;

import io.quarkus.arc.Arc;
import io.smallrye.mutiny.Multi;

public class LogStreamJsonRPCService {

    public Multi<StreamingLogEntry> streamLog() {
        LogStreamBroadcaster logStreamBroadcaster = Arc.container().instance(LogStreamBroadcaster.class).get();
        return logStreamBroadcaster.getLogStream();
    }

}
