/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.text.format;

import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.ComponentBuilderApplicable;
import net.kyori.adventure.text.format.Style;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface StyleBuilderApplicable
extends ComponentBuilderApplicable {
    @Contract(mutates="param")
    public void styleApply(@NotNull Style.Builder var1);

    @Override
    default public void componentBuilderApply(@NotNull ComponentBuilder<?, ?> component) {
        component.style(this::styleApply);
    }
}

