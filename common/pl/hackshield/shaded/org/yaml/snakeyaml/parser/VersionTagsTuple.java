/*
 * Decompiled with CFR 0.150.
 */
package pl.hackshield.shaded.org.yaml.snakeyaml.parser;

import java.util.Map;
import pl.hackshield.shaded.org.yaml.snakeyaml.DumperOptions;

class VersionTagsTuple {
    private DumperOptions.Version version;
    private Map<String, String> tags;

    public VersionTagsTuple(DumperOptions.Version version, Map<String, String> tags) {
        this.version = version;
        this.tags = tags;
    }

    public DumperOptions.Version getVersion() {
        return this.version;
    }

    public Map<String, String> getTags() {
        return this.tags;
    }

    public String toString() {
        return String.format("VersionTagsTuple<%s, %s>", new Object[]{this.version, this.tags});
    }
}

