/*
 * Decompiled with CFR 0.150.
 */
package pl.hackshield.shaded.org.yaml.snakeyaml.emitter;

import java.io.IOException;
import pl.hackshield.shaded.org.yaml.snakeyaml.events.Event;

public interface Emitable {
    public void emit(Event var1) throws IOException;
}

