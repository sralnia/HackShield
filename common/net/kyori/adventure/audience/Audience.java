/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.audience;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import net.kyori.adventure.audience.Audiences;
import net.kyori.adventure.audience.EmptyAudience;
import net.kyori.adventure.audience.ForwardingAudience;
import net.kyori.adventure.audience.ForwardingAudienceOverrideNotRequired;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointered;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.NotNull;

public interface Audience
extends Pointered {
    @NotNull
    public static Audience empty() {
        return EmptyAudience.INSTANCE;
    }

    @NotNull
    public static Audience audience(Audience ... audiences) {
        int length = audiences.length;
        if (length == 0) {
            return Audience.empty();
        }
        if (length == 1) {
            return audiences[0];
        }
        return Audience.audience(Arrays.asList(audiences));
    }

    @NotNull
    public static ForwardingAudience audience(@NotNull Iterable<? extends Audience> audiences) {
        return () -> audiences;
    }

    @NotNull
    public static Collector<? super Audience, ?, ForwardingAudience> toAudience() {
        return Audiences.COLLECTOR;
    }

    @NotNull
    default public Audience filterAudience(@NotNull Predicate<? super Audience> filter) {
        return filter.test(this) ? this : Audience.empty();
    }

    default public void forEachAudience(@NotNull Consumer<? super Audience> action) {
        action.accept(this);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull ComponentLike message) {
        this.sendMessage(Identity.nil(), message);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message) {
        this.sendMessage(source, message.asComponent());
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message) {
        this.sendMessage(source, message.asComponent());
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Component message) {
        this.sendMessage(Identity.nil(), message);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identified source, @NotNull Component message) {
        this.sendMessage(source, message, MessageType.SYSTEM);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        this.sendMessage(source, message, MessageType.SYSTEM);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull ComponentLike message, @NotNull MessageType type) {
        this.sendMessage(Identity.nil(), message, type);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identified source, @NotNull ComponentLike message, @NotNull MessageType type) {
        this.sendMessage(source, message.asComponent(), type);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Identity source, @NotNull ComponentLike message, @NotNull MessageType type) {
        this.sendMessage(source, message.asComponent(), type);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendMessage(@NotNull Component message, @NotNull MessageType type) {
        this.sendMessage(Identity.nil(), message, type);
    }

    default public void sendMessage(@NotNull Identified source, @NotNull Component message, @NotNull MessageType type) {
        this.sendMessage(source.identity(), message, type);
    }

    default public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendActionBar(@NotNull ComponentLike message) {
        this.sendActionBar(message.asComponent());
    }

    default public void sendActionBar(@NotNull Component message) {
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendPlayerListHeader(@NotNull ComponentLike header) {
        this.sendPlayerListHeader(header.asComponent());
    }

    default public void sendPlayerListHeader(@NotNull Component header) {
        this.sendPlayerListHeaderAndFooter(header, Component.empty());
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendPlayerListFooter(@NotNull ComponentLike footer) {
        this.sendPlayerListFooter(footer.asComponent());
    }

    default public void sendPlayerListFooter(@NotNull Component footer) {
        this.sendPlayerListHeaderAndFooter(Component.empty(), footer);
    }

    @ForwardingAudienceOverrideNotRequired
    default public void sendPlayerListHeaderAndFooter(@NotNull ComponentLike header, @NotNull ComponentLike footer) {
        this.sendPlayerListHeaderAndFooter(header.asComponent(), footer.asComponent());
    }

    default public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
    }

    @ForwardingAudienceOverrideNotRequired
    default public void showTitle(@NotNull Title title) {
        Title.Times times = title.times();
        if (times != null) {
            this.sendTitlePart(TitlePart.TIMES, times);
        }
        this.sendTitlePart(TitlePart.SUBTITLE, title.subtitle());
        this.sendTitlePart(TitlePart.TITLE, title.title());
    }

    default public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
    }

    default public void clearTitle() {
    }

    default public void resetTitle() {
    }

    default public void showBossBar(@NotNull BossBar bar) {
    }

    default public void hideBossBar(@NotNull BossBar bar) {
    }

    default public void playSound(@NotNull Sound sound) {
    }

    default public void playSound(@NotNull Sound sound, double x, double y, double z) {
    }

    default public void playSound(@NotNull Sound sound, @NotNull Sound.Emitter emitter) {
    }

    @ForwardingAudienceOverrideNotRequired
    default public void stopSound(@NotNull Sound sound) {
        this.stopSound(Objects.requireNonNull(sound, "sound").asStop());
    }

    default public void stopSound(@NotNull SoundStop stop) {
    }

    @ForwardingAudienceOverrideNotRequired
    default public void openBook(@NotNull Book.Builder book) {
        this.openBook(book.build());
    }

    default public void openBook(@NotNull Book book) {
    }
}

