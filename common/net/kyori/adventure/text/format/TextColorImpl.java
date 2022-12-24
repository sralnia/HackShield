/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.text.format;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.Debug;
import org.jetbrains.annotations.Nullable;

@Debug.Renderer(text="asHexString()")
final class TextColorImpl
implements TextColor {
    private final int value;

    TextColorImpl(int value) {
        this.value = value;
    }

    @Override
    public int value() {
        return this.value;
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof TextColorImpl)) {
            return false;
        }
        TextColorImpl that = (TextColorImpl)other;
        return this.value == that.value;
    }

    public int hashCode() {
        return this.value;
    }

    public String toString() {
        return this.asHexString();
    }
}

