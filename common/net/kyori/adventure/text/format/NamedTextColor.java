/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.text.format;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;
import net.kyori.adventure.util.Index;
import net.kyori.examination.ExaminableProperty;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class NamedTextColor
implements TextColor {
    private static final int BLACK_VALUE = 0;
    private static final int DARK_BLUE_VALUE = 170;
    private static final int DARK_GREEN_VALUE = 43520;
    private static final int DARK_AQUA_VALUE = 43690;
    private static final int DARK_RED_VALUE = 0xAA0000;
    private static final int DARK_PURPLE_VALUE = 0xAA00AA;
    private static final int GOLD_VALUE = 0xFFAA00;
    private static final int GRAY_VALUE = 0xAAAAAA;
    private static final int DARK_GRAY_VALUE = 0x555555;
    private static final int BLUE_VALUE = 0x5555FF;
    private static final int GREEN_VALUE = 0x55FF55;
    private static final int AQUA_VALUE = 0x55FFFF;
    private static final int RED_VALUE = 0xFF5555;
    private static final int LIGHT_PURPLE_VALUE = 0xFF55FF;
    private static final int YELLOW_VALUE = 0xFFFF55;
    private static final int WHITE_VALUE = 0xFFFFFF;
    public static final NamedTextColor BLACK = new NamedTextColor("black", 0);
    public static final NamedTextColor DARK_BLUE = new NamedTextColor("dark_blue", 170);
    public static final NamedTextColor DARK_GREEN = new NamedTextColor("dark_green", 43520);
    public static final NamedTextColor DARK_AQUA = new NamedTextColor("dark_aqua", 43690);
    public static final NamedTextColor DARK_RED = new NamedTextColor("dark_red", 0xAA0000);
    public static final NamedTextColor DARK_PURPLE = new NamedTextColor("dark_purple", 0xAA00AA);
    public static final NamedTextColor GOLD = new NamedTextColor("gold", 0xFFAA00);
    public static final NamedTextColor GRAY = new NamedTextColor("gray", 0xAAAAAA);
    public static final NamedTextColor DARK_GRAY = new NamedTextColor("dark_gray", 0x555555);
    public static final NamedTextColor BLUE = new NamedTextColor("blue", 0x5555FF);
    public static final NamedTextColor GREEN = new NamedTextColor("green", 0x55FF55);
    public static final NamedTextColor AQUA = new NamedTextColor("aqua", 0x55FFFF);
    public static final NamedTextColor RED = new NamedTextColor("red", 0xFF5555);
    public static final NamedTextColor LIGHT_PURPLE = new NamedTextColor("light_purple", 0xFF55FF);
    public static final NamedTextColor YELLOW = new NamedTextColor("yellow", 0xFFFF55);
    public static final NamedTextColor WHITE = new NamedTextColor("white", 0xFFFFFF);
    private static final List<NamedTextColor> VALUES = Collections.unmodifiableList(Arrays.asList(BLACK, DARK_BLUE, DARK_GREEN, DARK_AQUA, DARK_RED, DARK_PURPLE, GOLD, GRAY, DARK_GRAY, BLUE, GREEN, AQUA, RED, LIGHT_PURPLE, YELLOW, WHITE));
    public static final Index<String, NamedTextColor> NAMES = Index.create(constant -> constant.name, VALUES);
    private final String name;
    private final int value;
    private final HSVLike hsv;

    @Nullable
    public static NamedTextColor namedColor(int value) {
        switch (value) {
            case 0: {
                return BLACK;
            }
            case 170: {
                return DARK_BLUE;
            }
            case 43520: {
                return DARK_GREEN;
            }
            case 43690: {
                return DARK_AQUA;
            }
            case 0xAA0000: {
                return DARK_RED;
            }
            case 0xAA00AA: {
                return DARK_PURPLE;
            }
            case 0xFFAA00: {
                return GOLD;
            }
            case 0xAAAAAA: {
                return GRAY;
            }
            case 0x555555: {
                return DARK_GRAY;
            }
            case 0x5555FF: {
                return BLUE;
            }
            case 0x55FF55: {
                return GREEN;
            }
            case 0x55FFFF: {
                return AQUA;
            }
            case 0xFF5555: {
                return RED;
            }
            case 0xFF55FF: {
                return LIGHT_PURPLE;
            }
            case 0xFFFF55: {
                return YELLOW;
            }
            case 0xFFFFFF: {
                return WHITE;
            }
        }
        return null;
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    @Nullable
    public static NamedTextColor ofExact(int value) {
        switch (value) {
            case 0: {
                return BLACK;
            }
            case 170: {
                return DARK_BLUE;
            }
            case 43520: {
                return DARK_GREEN;
            }
            case 43690: {
                return DARK_AQUA;
            }
            case 0xAA0000: {
                return DARK_RED;
            }
            case 0xAA00AA: {
                return DARK_PURPLE;
            }
            case 0xFFAA00: {
                return GOLD;
            }
            case 0xAAAAAA: {
                return GRAY;
            }
            case 0x555555: {
                return DARK_GRAY;
            }
            case 0x5555FF: {
                return BLUE;
            }
            case 0x55FF55: {
                return GREEN;
            }
            case 0x55FFFF: {
                return AQUA;
            }
            case 0xFF5555: {
                return RED;
            }
            case 0xFF55FF: {
                return LIGHT_PURPLE;
            }
            case 0xFFFF55: {
                return YELLOW;
            }
            case 0xFFFFFF: {
                return WHITE;
            }
        }
        return null;
    }

    @NotNull
    public static NamedTextColor nearestTo(@NotNull TextColor any) {
        if (any instanceof NamedTextColor) {
            return (NamedTextColor)any;
        }
        Objects.requireNonNull(any, "color");
        float matchedDistance = Float.MAX_VALUE;
        NamedTextColor match = VALUES.get(0);
        int length = VALUES.size();
        for (int i = 0; i < length; ++i) {
            NamedTextColor potential = VALUES.get(i);
            float distance = NamedTextColor.distance(any.asHSV(), potential.asHSV());
            if (distance < matchedDistance) {
                match = potential;
                matchedDistance = distance;
            }
            if (distance == 0.0f) break;
        }
        return match;
    }

    private static float distance(@NotNull HSVLike self, @NotNull HSVLike other) {
        float hueDistance = 3.0f * Math.min(Math.abs(self.h() - other.h()), 1.0f - Math.abs(self.h() - other.h()));
        float saturationDiff = self.s() - other.s();
        float valueDiff = self.v() - other.v();
        return hueDistance * hueDistance + saturationDiff * saturationDiff + valueDiff * valueDiff;
    }

    private NamedTextColor(String name, int value) {
        this.name = name;
        this.value = value;
        this.hsv = HSVLike.fromRGB(this.red(), this.green(), this.blue());
    }

    @Override
    public int value() {
        return this.value;
    }

    @Override
    @NotNull
    public HSVLike asHSV() {
        return this.hsv;
    }

    @NotNull
    public String toString() {
        return this.name;
    }

    @Override
    @NotNull
    public Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.concat(Stream.of(ExaminableProperty.of("name", this.name)), TextColor.super.examinableProperties());
    }
}

