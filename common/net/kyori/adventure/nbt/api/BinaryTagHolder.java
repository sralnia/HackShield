/*
 * Decompiled with CFR 0.150.
 */
package net.kyori.adventure.nbt.api;

import net.kyori.adventure.nbt.api.BinaryTagHolderImpl;
import net.kyori.adventure.util.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public interface BinaryTagHolder {
    @NotNull
    public static <T, EX extends Exception> BinaryTagHolder encode(@NotNull T nbt, @NotNull Codec<? super T, String, ?, EX> codec) throws EX {
        return new BinaryTagHolderImpl(codec.encode(nbt));
    }

    @NotNull
    public static BinaryTagHolder binaryTagHolder(@NotNull String string) {
        return new BinaryTagHolderImpl(string);
    }

    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion="5.0.0")
    @NotNull
    public static BinaryTagHolder of(@NotNull String string) {
        return new BinaryTagHolderImpl(string);
    }

    @NotNull
    public String string();

    @NotNull
    public <T, DX extends Exception> T get(@NotNull Codec<T, String, DX, ?> var1) throws DX;
}

