/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.bytes.ByteLongPair;
import java.io.Serializable;
import java.util.Objects;

public class ByteLongMutablePair
implements ByteLongPair,
Serializable {
    private static final long serialVersionUID = 0L;
    protected byte left;
    protected long right;

    public ByteLongMutablePair(byte left, long right) {
        this.left = left;
        this.right = right;
    }

    public static ByteLongMutablePair of(byte left, long right) {
        return new ByteLongMutablePair(left, right);
    }

    @Override
    public byte leftByte() {
        return this.left;
    }

    @Override
    public ByteLongMutablePair left(byte l) {
        this.left = l;
        return this;
    }

    @Override
    public long rightLong() {
        return this.right;
    }

    @Override
    public ByteLongMutablePair right(long r) {
        this.right = r;
        return this;
    }

    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof ByteLongPair) {
            return this.left == ((ByteLongPair)other).leftByte() && this.right == ((ByteLongPair)other).rightLong();
        }
        if (other instanceof Pair) {
            return Objects.equals(this.left, ((Pair)other).left()) && Objects.equals(this.right, ((Pair)other).right());
        }
        return false;
    }

    public int hashCode() {
        return this.left * 19 + HashCommon.long2int(this.right);
    }

    public String toString() {
        return "<" + this.leftByte() + "," + this.rightLong() + ">";
    }
}

