/*
 * Decompiled with CFR 0.150.
 */
package pl.hackshield.shaded.org.yaml.snakeyaml.events;

import pl.hackshield.shaded.org.yaml.snakeyaml.error.Mark;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.Event;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.NodeEvent;

public final class AliasEvent
extends NodeEvent {
    public AliasEvent(String anchor, Mark startMark, Mark endMark) {
        super(anchor, startMark, endMark);
        if (anchor == null) {
            throw new NullPointerException("anchor is not specified for alias");
        }
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.Alias;
    }
}

