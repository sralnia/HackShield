/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.util;

import java.util.stream.Stream;
import net.kyori.adventure.util.HSVLikeImpl;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface HSVLike
extends Examinable {
    @NotNull
    public static HSVLike hsvLike(float h, float s, float v) {
        return new HSVLikeImpl(h, s, v);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    @NotNull
    public static HSVLike of(float h, float s, float v) {
        return new HSVLikeImpl(h, s, v);
    }

    @NotNull
    public static HSVLike fromRGB(@Range(from=0L, to=255L) int red, @Range(from=0L, to=255L) int green, @Range(from=0L, to=255L) int blue) {
        float r = (float)red / 255.0f;
        float g = (float)green / 255.0f;
        float b = (float)blue / 255.0f;
        float min = Math.min(r, Math.min(g, b));
        float max = Math.max(r, Math.max(g, b));
        float delta = max - min;
        float s = max != 0.0f ? delta / max : 0.0f;
        if (s == 0.0f) {
            return new HSVLikeImpl(0.0f, s, max);
        }
        float h = r == max ? (g - b) / delta : (g == max ? 2.0f + (b - r) / delta : 4.0f + (r - g) / delta);
        if ((h *= 60.0f) < 0.0f) {
            h += 360.0f;
        }
        return new HSVLikeImpl(h / 360.0f, s, max);
    }

    public float h();

    public float s();

    public float v();

    @Override
    @NotNull
    default public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(ExaminableProperty.of("h", this.h()), ExaminableProperty.of("s", this.s()), ExaminableProperty.of("v", this.v()));
    }
}

