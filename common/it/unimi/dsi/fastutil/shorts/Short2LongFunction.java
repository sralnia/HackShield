/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.shorts;

import it.unimi.dsi.fastutil.Function;
import it.unimi.dsi.fastutil.SafeMath;
import it.unimi.dsi.fastutil.bytes.Byte2LongFunction;
import it.unimi.dsi.fastutil.bytes.Byte2ShortFunction;
import it.unimi.dsi.fastutil.chars.Char2LongFunction;
import it.unimi.dsi.fastutil.chars.Char2ShortFunction;
import it.unimi.dsi.fastutil.doubles.Double2LongFunction;
import it.unimi.dsi.fastutil.doubles.Double2ShortFunction;
import it.unimi.dsi.fastutil.floats.Float2LongFunction;
import it.unimi.dsi.fastutil.floats.Float2ShortFunction;
import it.unimi.dsi.fastutil.ints.Int2LongFunction;
import it.unimi.dsi.fastutil.ints.Int2ShortFunction;
import it.unimi.dsi.fastutil.longs.Long2ByteFunction;
import it.unimi.dsi.fastutil.longs.Long2CharFunction;
import it.unimi.dsi.fastutil.longs.Long2DoubleFunction;
import it.unimi.dsi.fastutil.longs.Long2FloatFunction;
import it.unimi.dsi.fastutil.longs.Long2IntFunction;
import it.unimi.dsi.fastutil.longs.Long2LongFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import it.unimi.dsi.fastutil.longs.Long2ShortFunction;
import it.unimi.dsi.fastutil.objects.Object2LongFunction;
import it.unimi.dsi.fastutil.objects.Object2ShortFunction;
import it.unimi.dsi.fastutil.objects.Reference2LongFunction;
import it.unimi.dsi.fastutil.objects.Reference2ShortFunction;
import it.unimi.dsi.fastutil.shorts.Short2ByteFunction;
import it.unimi.dsi.fastutil.shorts.Short2CharFunction;
import it.unimi.dsi.fastutil.shorts.Short2DoubleFunction;
import it.unimi.dsi.fastutil.shorts.Short2FloatFunction;
import it.unimi.dsi.fastutil.shorts.Short2IntFunction;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
import it.unimi.dsi.fastutil.shorts.Short2ReferenceFunction;
import it.unimi.dsi.fastutil.shorts.Short2ShortFunction;
import java.util.function.IntToLongFunction;

@FunctionalInterface
public interface Short2LongFunction
extends Function<Short, Long>,
IntToLongFunction {
    @Override
    @Deprecated
    default public long applyAsLong(int operand) {
        return this.get(SafeMath.safeIntToShort(operand));
    }

    @Override
    default public long put(short key, long value) {
        throw new UnsupportedOperationException();
    }

    public long get(short var1);

    default public long getOrDefault(short key, long defaultValue) {
        long v = this.get(key);
        return v != this.defaultReturnValue() || this.containsKey(key) ? v : defaultValue;
    }

    default public long remove(short key) {
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    default public Long put(Short key, Long value) {
        short k = key;
        boolean containsKey = this.containsKey(k);
        long v = this.put(k, (long)value);
        return containsKey ? Long.valueOf(v) : null;
    }

    @Override
    @Deprecated
    default public Long get(Object key) {
        if (key == null) {
            return null;
        }
        short k = (Short)key;
        long v = this.get(k);
        return v != this.defaultReturnValue() || this.containsKey(k) ? Long.valueOf(v) : null;
    }

    @Override
    @Deprecated
    default public Long getOrDefault(Object key, Long defaultValue) {
        if (key == null) {
            return defaultValue;
        }
        short k = (Short)key;
        long v = this.get(k);
        return v != this.defaultReturnValue() || this.containsKey(k) ? Long.valueOf(v) : defaultValue;
    }

    @Override
    @Deprecated
    default public Long remove(Object key) {
        if (key == null) {
            return null;
        }
        short k = (Short)key;
        return this.containsKey(k) ? Long.valueOf(this.remove(k)) : null;
    }

    default public boolean containsKey(short key) {
        return true;
    }

    @Override
    @Deprecated
    default public boolean containsKey(Object key) {
        return key == null ? false : this.containsKey((Short)key);
    }

    default public void defaultReturnValue(long rv) {
        throw new UnsupportedOperationException();
    }

    default public long defaultReturnValue() {
        return 0L;
    }

    @Override
    @Deprecated
    default public <T> java.util.function.Function<T, Long> compose(java.util.function.Function<? super T, ? extends Short> before) {
        return Function.super.compose(before);
    }

    @Override
    @Deprecated
    default public <T> java.util.function.Function<Short, T> andThen(java.util.function.Function<? super Long, ? extends T> after) {
        return Function.super.andThen(after);
    }

    default public Short2ByteFunction andThenByte(Long2ByteFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Byte2LongFunction composeByte(Byte2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2ShortFunction andThenShort(Long2ShortFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Short2LongFunction composeShort(Short2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2IntFunction andThenInt(Long2IntFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Int2LongFunction composeInt(Int2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2LongFunction andThenLong(Long2LongFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Long2LongFunction composeLong(Long2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2CharFunction andThenChar(Long2CharFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Char2LongFunction composeChar(Char2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2FloatFunction andThenFloat(Long2FloatFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Float2LongFunction composeFloat(Float2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public Short2DoubleFunction andThenDouble(Long2DoubleFunction after) {
        return k -> after.get(this.get(k));
    }

    default public Double2LongFunction composeDouble(Double2ShortFunction before) {
        return k -> this.get(before.get(k));
    }

    default public <T> Short2ObjectFunction<T> andThenObject(Long2ObjectFunction<? extends T> after) {
        return k -> after.get(this.get(k));
    }

    default public <T> Object2LongFunction<T> composeObject(Object2ShortFunction<? super T> before) {
        return k -> this.get(before.getShort(k));
    }

    default public <T> Short2ReferenceFunction<T> andThenReference(Long2ReferenceFunction<? extends T> after) {
        return k -> after.get(this.get(k));
    }

    default public <T> Reference2LongFunction<T> composeReference(Reference2ShortFunction<? super T> before) {
        return k -> this.get(before.getShort(k));
    }
}

