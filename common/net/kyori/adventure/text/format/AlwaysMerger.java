/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.text.format;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.Merger;
import net.kyori.adventure.text.format.StyleImpl;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AlwaysMerger
implements Merger {
    static final AlwaysMerger INSTANCE = new AlwaysMerger();

    private AlwaysMerger() {
    }

    @Override
    public void mergeColor(StyleImpl.BuilderImpl target, @Nullable TextColor color) {
        target.color(color);
    }

    @Override
    public void mergeDecoration(StyleImpl.BuilderImpl target, @NotNull TextDecoration decoration, @NotNull TextDecoration.State state) {
        target.decoration(decoration, state);
    }

    @Override
    public void mergeClickEvent(StyleImpl.BuilderImpl target, @Nullable ClickEvent event) {
        target.clickEvent(event);
    }

    @Override
    public void mergeHoverEvent(StyleImpl.BuilderImpl target, @Nullable HoverEvent<?> event) {
        target.hoverEvent(event);
    }

    @Override
    public void mergeInsertion(StyleImpl.BuilderImpl target, @Nullable String insertion) {
        target.insertion(insertion);
    }

    @Override
    public void mergeFont(StyleImpl.BuilderImpl target, @Nullable Key font) {
        target.font(font);
    }
}

