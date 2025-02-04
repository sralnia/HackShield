/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.audience;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

final class EmptyAudience
implements Audience {
    static final EmptyAudience INSTANCE = new EmptyAudience();

    EmptyAudience() {
    }

    @Override
    @NotNull
    public <T> Optional<T> get(@NotNull Pointer<T> pointer) {
        return Optional.empty();
    }

    @Override
    @Contract(value="_, null -> null; _, !null -> !null")
    @Nullable
    public <T> T getOrDefault(@NotNull Pointer<T> pointer, @Nullable T defaultValue) {
        return defaultValue;
    }

    @Override
    public <T> @UnknownNullability T getOrDefaultFrom(@NotNull Pointer<T> pointer, @NotNull Supplier<? extends T> defaultValue) {
        return defaultValue.get();
    }

    @Override
    @NotNull
    public Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return this;
    }

    @Override
    public void forEachAudience(@NotNull Consumer<? super Audience> action) {
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message) {
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
    }

    @Override
    public void sendMessage(@NotNull ComponentLike message, @NotNull MessageType type) {
    }

    @Override
    public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message, @NotNull MessageType type) {
    }

    @Override
    public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message, @NotNull MessageType type) {
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
    }

    @Override
    public void sendPlayerListHeader(@NotNull ComponentLike header) {
    }

    @Override
    public void sendPlayerListFooter(@NotNull ComponentLike footer) {
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
    }

    @Override
    public void openBook( @NotNull Book.Builder book) {
    }

    public boolean equals(Object that) {
        return this == that;
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "EmptyAudience";
    }
}

