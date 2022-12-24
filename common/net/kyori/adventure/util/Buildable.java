/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.util;

import java.util.function.Consumer;
import net.kyori.adventure.builder.AbstractBuilder;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Buildable<R, B extends Builder<R>> {
    @Deprecated
    @Contract(mutates="param1")
    @NotNull
    public static <R extends Buildable<R, B>, B extends Builder<R>> R configureAndBuild(@NotNull B builder, @Nullable Consumer<? super B> consumer) {
        return (R)((Buildable)AbstractBuilder.configureAndBuild(builder, consumer));
    }

    @Contract(value="-> new", pure=true)
    @NotNull
    public B toBuilder();

    @Deprecated
    public static interface Builder<R>
    extends AbstractBuilder<R> {
        @Override
        @Contract(value="-> new", pure=true)
        @NotNull
        public R build();
    }
}

