/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.floats;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.floats.FloatBytePair;
import java.io.Serializable;
import java.util.Objects;

public class FloatByteMutablePair
implements FloatBytePair,
Serializable {
    private static final long serialVersionUID = 0L;
    protected float left;
    protected byte right;

    public FloatByteMutablePair(float left, byte right) {
        this.left = left;
        this.right = right;
    }

    public static FloatByteMutablePair of(float left, byte right) {
        return new FloatByteMutablePair(left, right);
    }

    @Override
    public float leftFloat() {
        return this.left;
    }

    @Override
    public FloatByteMutablePair left(float l) {
        this.left = l;
        return this;
    }

    @Override
    public byte rightByte() {
        return this.right;
    }

    @Override
    public FloatByteMutablePair right(byte r) {
        this.right = r;
        return this;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof FloatBytePair) {
            return this.left == ((FloatBytePair)other).leftFloat() && this.right == ((FloatBytePair)other).rightByte();
        }
        if (other instanceof Pair) {
            return Objects.equals(Float.valueOf(this.left), ((Pair)other).left()) && Objects.equals(this.right, ((Pair)other).right());
        }
        return false;
    }

    public int hashCode() {
        return HashCommon.float2int(this.left) * 19 + this.right;
    }

    public String toString() {
        return "<" + this.leftFloat() + "," + this.rightByte() + ">";
    }
}

