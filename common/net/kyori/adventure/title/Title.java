/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.title;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitleImpl;
import net.kyori.adventure.title.TitlePart;
import net.kyori.adventure.util.Ticks;
import net.kyori.examination.Examinable;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

@ApiStatus.NonExtendable
public interface Title
extends Examinable {
    public static final Times DEFAULT_TIMES = Times.times(Ticks.duration(10L), Ticks.duration(70L), Ticks.duration(20L));

    @NotNull
    public static Title title(@NotNull Component title, @NotNull Component subtitle) {
        return Title.title(title, subtitle, DEFAULT_TIMES);
    }

    @NotNull
    public static Title title(@NotNull Component title, @NotNull Component subtitle, @Nullable Times times) {
        return new TitleImpl(title, subtitle, times);
    }

    @NotNull
    public Component title();

    @NotNull
    public Component subtitle();

    @Nullable
    public Times times();

    public <T> @UnknownNullability T part(@NotNull TitlePart<T> var1);

    public static interface Times
    extends Examinable {
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
        @NotNull
        public static Times of(@NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut) {
            return Times.times(fadeIn, stay, fadeOut);
        }

        @NotNull
        public static Times times(@NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut) {
            return new TitleImpl.TimesImpl(fadeIn, stay, fadeOut);
        }

        @NotNull
        public Duration fadeIn();

        @NotNull
        public Duration stay();

        @NotNull
        public Duration fadeOut();
    }
}

