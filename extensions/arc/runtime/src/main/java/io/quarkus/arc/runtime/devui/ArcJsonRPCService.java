package io.quarkus.arc.runtime.devui;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import io.quarkus.arc.Arc;
import io.quarkus.arc.runtime.devconsole.Invocation;
import io.quarkus.arc.runtime.devconsole.InvocationsMonitor;
import io.quarkus.arc.runtime.devmode.EventInfo;
import io.quarkus.arc.runtime.devmode.EventsMonitor;
import io.quarkus.arc.runtime.devmode.InvocationInfo;
import io.smallrye.mutiny.Multi;

public class ArcJsonRPCService {

    public Multi<EventInfo> streamEvents() {
        EventsMonitor eventsMonitor = Arc.container().instance(EventsMonitor.class).get();
        return eventsMonitor.streamEvents();
    }

    public List<EventInfo> getLastEvents() {
        EventsMonitor eventsMonitor = Arc.container().instance(EventsMonitor.class).get();
        return eventsMonitor.getLastEvents();
    }

    public List<EventInfo> clearLastEvents() {
        EventsMonitor eventsMonitor = Arc.container().instance(EventsMonitor.class).get();
        eventsMonitor.clear();
        return eventsMonitor.getLastEvents();
    }

    public List<InvocationInfo> getLastInvocations() {
        InvocationsMonitor invocationsMonitor = Arc.container().instance(InvocationsMonitor.class).get();
        List<Invocation> lastInvocations = invocationsMonitor.getLastInvocations();
        return toInvocationInfos(lastInvocations);
    }

    public List<InvocationInfo> clearLastInvocations() {
        InvocationsMonitor invocationsMonitor = Arc.container().instance(InvocationsMonitor.class).get();
        invocationsMonitor.clear();
        return getLastInvocations();
    }

    private List<InvocationInfo> toInvocationInfos(List<Invocation> invocations) {
        List<InvocationInfo> infos = new ArrayList<>();
        for (Invocation invocation : invocations) {
            infos.add(toInvocationInfo(invocation));
        }
        return infos;
    }

    private InvocationInfo toInvocationInfo(Invocation invocation) {
        InvocationInfo info = new InvocationInfo();
        LocalDateTime starttime = LocalDateTime.ofInstant(Instant.ofEpochMilli(invocation.getStart()), ZoneId.systemDefault());
        info.setStartTime(timeString(starttime));
        return info;
    }

    private String timeString(LocalDateTime time) {
        String timestamp = time.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).replace("T", " ");
        return timestamp.substring(0, timestamp.lastIndexOf("."));
    }
}
