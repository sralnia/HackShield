/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.bytes;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.SafeMath;
import it.unimi.dsi.fastutil.bytes.AbstractByte2ByteMap;
import it.unimi.dsi.fastutil.bytes.AbstractByteCollection;
import it.unimi.dsi.fastutil.bytes.AbstractByteSet;
import it.unimi.dsi.fastutil.bytes.Byte2ByteFunction;
import it.unimi.dsi.fastutil.bytes.Byte2ByteMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteBytePair;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.bytes.ByteHash;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.bytes.ByteSet;
import it.unimi.dsi.fastutil.bytes.ByteSpliterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

public class Byte2ByteOpenCustomHashMap
extends AbstractByte2ByteMap
implements Serializable,
Cloneable,
Hash {
    private static final long serialVersionUID = 0L;
    private static final boolean ASSERTS = false;
    protected transient byte[] key;
    protected transient byte[] value;
    protected transient int mask;
    protected transient boolean containsNullKey;
    protected ByteHash.Strategy strategy;
    protected transient int n;
    protected transient int maxFill;
    protected final transient int minN;
    protected int size;
    protected final float f;
    protected transient Byte2ByteMap.FastEntrySet entries;
    protected transient ByteSet keys;
    protected transient ByteCollection values;

    public Byte2ByteOpenCustomHashMap(int expected, float f, ByteHash.Strategy strategy) {
        this.strategy = strategy;
        if (f <= 0.0f || f >= 1.0f) {
            throw new IllegalArgumentException("Load factor must be greater than 0 and smaller than 1");
        }
        if (expected < 0) {
            throw new IllegalArgumentException("The expected number of elements must be nonnegative");
        }
        this.f = f;
        this.minN = this.n = HashCommon.arraySize(expected, f);
        this.mask = this.n - 1;
        this.maxFill = HashCommon.maxFill(this.n, f);
        this.key = new byte[this.n + 1];
        this.value = new byte[this.n + 1];
    }

    public Byte2ByteOpenCustomHashMap(int expected, ByteHash.Strategy strategy) {
        this(expected, 0.75f, strategy);
    }

    public Byte2ByteOpenCustomHashMap(ByteHash.Strategy strategy) {
        this(16, 0.75f, strategy);
    }

    public Byte2ByteOpenCustomHashMap(Map<? extends Byte, ? extends Byte> m, float f, ByteHash.Strategy strategy) {
        this(m.size(), f, strategy);
        this.putAll(m);
    }

    public Byte2ByteOpenCustomHashMap(Map<? extends Byte, ? extends Byte> m, ByteHash.Strategy strategy) {
        this(m, 0.75f, strategy);
    }

    public Byte2ByteOpenCustomHashMap(Byte2ByteMap m, float f, ByteHash.Strategy strategy) {
        this(m.size(), f, strategy);
        this.putAll(m);
    }

    public Byte2ByteOpenCustomHashMap(Byte2ByteMap m, ByteHash.Strategy strategy) {
        this(m, 0.75f, strategy);
    }

    public Byte2ByteOpenCustomHashMap(byte[] k, byte[] v, float f, ByteHash.Strategy strategy) {
        this(k.length, f, strategy);
        if (k.length != v.length) {
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
        }
        for (int i = 0; i < k.length; ++i) {
            this.put(k[i], v[i]);
        }
    }

    public Byte2ByteOpenCustomHashMap(byte[] k, byte[] v, ByteHash.Strategy strategy) {
        this(k, v, 0.75f, strategy);
    }

    public ByteHash.Strategy strategy() {
        return this.strategy;
    }

    private int realSize() {
        return this.containsNullKey ? this.size - 1 : this.size;
    }

    private void ensureCapacity(int capacity) {
        int needed = HashCommon.arraySize(capacity, this.f);
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    private void tryCapacity(long capacity) {
        int needed = (int)Math.min(0x40000000L, Math.max(2L, HashCommon.nextPowerOfTwo((long)Math.ceil((float)capacity / this.f))));
        if (needed > this.n) {
            this.rehash(needed);
        }
    }

    private byte removeEntry(int pos) {
        byte oldValue = this.value[pos];
        --this.size;
        this.shiftKeys(pos);
        if (this.n > this.minN && this.size < this.maxFill / 4 && this.n > 16) {
            this.rehash(this.n / 2);
        }
        return oldValue;
    }

    private byte removeNullEntry() {
        this.containsNullKey = false;
        byte oldValue = this.value[this.n];
        --this.size;
        if (this.n > this.minN && this.size < this.maxFill / 4 && this.n > 16) {
            this.rehash(this.n / 2);
        }
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends Byte, ? extends Byte> m) {
        if ((double)this.f <= 0.5) {
            this.ensureCapacity(m.size());
        } else {
            this.tryCapacity(this.size() + m.size());
        }
        super.putAll(m);
    }

    private int find(byte k) {
        if (this.strategy.equals(k, (byte)0)) {
            return this.containsNullKey ? this.n : -(this.n + 1);
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return -(pos + 1);
        }
        if (this.strategy.equals(k, curr)) {
            return pos;
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return -(pos + 1);
        } while (!this.strategy.equals(k, curr));
        return pos;
    }

    private void insert(int pos, byte k, byte v) {
        if (pos == this.n) {
            this.containsNullKey = true;
        }
        this.key[pos] = k;
        this.value[pos] = v;
        if (this.size++ >= this.maxFill) {
            this.rehash(HashCommon.arraySize(this.size + 1, this.f));
        }
    }

    @Override
    public byte put(byte k, byte v) {
        int pos = this.find(k);
        if (pos < 0) {
            this.insert(-pos - 1, k, v);
            return this.defRetValue;
        }
        byte oldValue = this.value[pos];
        this.value[pos] = v;
        return oldValue;
    }

    private byte addToValue(int pos, byte incr) {
        byte oldValue = this.value[pos];
        this.value[pos] = (byte)(oldValue + incr);
        return oldValue;
    }

    public byte addTo(byte k, byte incr) {
        int pos;
        if (this.strategy.equals(k, (byte)0)) {
            if (this.containsNullKey) {
                return this.addToValue(this.n, incr);
            }
            pos = this.n;
            this.containsNullKey = true;
        } else {
            byte[] key = this.key;
            pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
            byte curr = key[pos];
            if (curr != 0) {
                if (this.strategy.equals(curr, k)) {
                    return this.addToValue(pos, incr);
                }
                while ((curr = key[pos = pos + 1 & this.mask]) != 0) {
                    if (!this.strategy.equals(curr, k)) continue;
                    return this.addToValue(pos, incr);
                }
            }
        }
        this.key[pos] = k;
        this.value[pos] = (byte)(this.defRetValue + incr);
        if (this.size++ >= this.maxFill) {
            this.rehash(HashCommon.arraySize(this.size + 1, this.f));
        }
        return this.defRetValue;
    }

    protected final void shiftKeys(int pos) {
        byte[] key = this.key;
        while (true) {
            byte curr;
            int last = pos;
            pos = last + 1 & this.mask;
            while (true) {
                if ((curr = key[pos]) == 0) {
                    key[last] = 0;
                    return;
                }
                int slot = HashCommon.mix(this.strategy.hashCode(curr)) & this.mask;
                if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                pos = pos + 1 & this.mask;
            }
            key[last] = curr;
            this.value[last] = this.value[pos];
        }
    }

    @Override
    public byte remove(byte k) {
        if (this.strategy.equals(k, (byte)0)) {
            if (this.containsNullKey) {
                return this.removeNullEntry();
            }
            return this.defRetValue;
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return this.defRetValue;
        }
        if (this.strategy.equals(k, curr)) {
            return this.removeEntry(pos);
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return this.defRetValue;
        } while (!this.strategy.equals(k, curr));
        return this.removeEntry(pos);
    }

    @Override
    public byte get(byte k) {
        if (this.strategy.equals(k, (byte)0)) {
            return this.containsNullKey ? this.value[this.n] : this.defRetValue;
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return this.defRetValue;
        }
        if (this.strategy.equals(k, curr)) {
            return this.value[pos];
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return this.defRetValue;
        } while (!this.strategy.equals(k, curr));
        return this.value[pos];
    }

    @Override
    public boolean containsKey(byte k) {
        if (this.strategy.equals(k, (byte)0)) {
            return this.containsNullKey;
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return false;
        }
        if (this.strategy.equals(k, curr)) {
            return true;
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return false;
        } while (!this.strategy.equals(k, curr));
        return true;
    }

    @Override
    public boolean containsValue(byte v) {
        byte[] value = this.value;
        byte[] key = this.key;
        if (this.containsNullKey && value[this.n] == v) {
            return true;
        }
        int i = this.n;
        while (i-- != 0) {
            if (key[i] == 0 || value[i] != v) continue;
            return true;
        }
        return false;
    }

    @Override
    public byte getOrDefault(byte k, byte defaultValue) {
        if (this.strategy.equals(k, (byte)0)) {
            return this.containsNullKey ? this.value[this.n] : defaultValue;
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return defaultValue;
        }
        if (this.strategy.equals(k, curr)) {
            return this.value[pos];
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return defaultValue;
        } while (!this.strategy.equals(k, curr));
        return this.value[pos];
    }

    @Override
    public byte putIfAbsent(byte k, byte v) {
        int pos = this.find(k);
        if (pos >= 0) {
            return this.value[pos];
        }
        this.insert(-pos - 1, k, v);
        return this.defRetValue;
    }

    @Override
    public boolean remove(byte k, byte v) {
        if (this.strategy.equals(k, (byte)0)) {
            if (this.containsNullKey && v == this.value[this.n]) {
                this.removeNullEntry();
                return true;
            }
            return false;
        }
        byte[] key = this.key;
        int pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
        byte curr = key[pos];
        if (curr == 0) {
            return false;
        }
        if (this.strategy.equals(k, curr) && v == this.value[pos]) {
            this.removeEntry(pos);
            return true;
        }
        do {
            if ((curr = key[pos = pos + 1 & this.mask]) != 0) continue;
            return false;
        } while (!this.strategy.equals(k, curr) || v != this.value[pos]);
        this.removeEntry(pos);
        return true;
    }

    @Override
    public boolean replace(byte k, byte oldValue, byte v) {
        int pos = this.find(k);
        if (pos < 0 || oldValue != this.value[pos]) {
            return false;
        }
        this.value[pos] = v;
        return true;
    }

    @Override
    public byte replace(byte k, byte v) {
        int pos = this.find(k);
        if (pos < 0) {
            return this.defRetValue;
        }
        byte oldValue = this.value[pos];
        this.value[pos] = v;
        return oldValue;
    }

    @Override
    public byte computeIfAbsent(byte k, IntUnaryOperator mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        int pos = this.find(k);
        if (pos >= 0) {
            return this.value[pos];
        }
        byte newValue = SafeMath.safeIntToByte(mappingFunction.applyAsInt(k));
        this.insert(-pos - 1, k, newValue);
        return newValue;
    }

    @Override
    public byte computeIfAbsent(byte key, Byte2ByteFunction mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        int pos = this.find(key);
        if (pos >= 0) {
            return this.value[pos];
        }
        if (!mappingFunction.containsKey(key)) {
            return this.defRetValue;
        }
        byte newValue = mappingFunction.get(key);
        this.insert(-pos - 1, key, newValue);
        return newValue;
    }

    @Override
    public byte computeIfAbsentNullable(byte k, IntFunction<? extends Byte> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        int pos = this.find(k);
        if (pos >= 0) {
            return this.value[pos];
        }
        Byte newValue = mappingFunction.apply(k);
        if (newValue == null) {
            return this.defRetValue;
        }
        byte v = newValue;
        this.insert(-pos - 1, k, v);
        return v;
    }

    @Override
    public byte computeIfPresent(byte k, BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int pos = this.find(k);
        if (pos < 0) {
            return this.defRetValue;
        }
        Byte newValue = remappingFunction.apply((Byte)k, (Byte)this.value[pos]);
        if (newValue == null) {
            if (this.strategy.equals(k, (byte)0)) {
                this.removeNullEntry();
            } else {
                this.removeEntry(pos);
            }
            return this.defRetValue;
        }
        this.value[pos] = newValue;
        return this.value[pos];
    }

    @Override
    public byte compute(byte k, BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int pos = this.find(k);
        Byte newValue = remappingFunction.apply((Byte)k, pos >= 0 ? Byte.valueOf(this.value[pos]) : null);
        if (newValue == null) {
            if (pos >= 0) {
                if (this.strategy.equals(k, (byte)0)) {
                    this.removeNullEntry();
                } else {
                    this.removeEntry(pos);
                }
            }
            return this.defRetValue;
        }
        byte newVal = newValue;
        if (pos < 0) {
            this.insert(-pos - 1, k, newVal);
            return newVal;
        }
        this.value[pos] = newVal;
        return this.value[pos];
    }

    @Override
    public byte merge(byte k, byte v, BiFunction<? super Byte, ? super Byte, ? extends Byte> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        int pos = this.find(k);
        if (pos < 0) {
            if (pos < 0) {
                this.insert(-pos - 1, k, v);
            } else {
                this.value[pos] = v;
            }
            return v;
        }
        Byte newValue = remappingFunction.apply((Byte)this.value[pos], (Byte)v);
        if (newValue == null) {
            if (this.strategy.equals(k, (byte)0)) {
                this.removeNullEntry();
            } else {
                this.removeEntry(pos);
            }
            return this.defRetValue;
        }
        this.value[pos] = newValue;
        return this.value[pos];
    }

    @Override
    public void clear() {
        if (this.size == 0) {
            return;
        }
        this.size = 0;
        this.containsNullKey = false;
        Arrays.fill(this.key, (byte)0);
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    public Byte2ByteMap.FastEntrySet byte2ByteEntrySet() {
        if (this.entries == null) {
            this.entries = new MapEntrySet();
        }
        return this.entries;
    }

    @Override
    public ByteSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public ByteCollection values() {
        if (this.values == null) {
            this.values = new AbstractByteCollection(){

                @Override
                public ByteIterator iterator() {
                    return new ValueIterator();
                }

                @Override
                public ByteSpliterator spliterator() {
                    return new ValueSpliterator();
                }

                @Override
                public void forEach(ByteConsumer consumer) {
                    if (Byte2ByteOpenCustomHashMap.this.containsNullKey) {
                        consumer.accept(Byte2ByteOpenCustomHashMap.this.value[Byte2ByteOpenCustomHashMap.this.n]);
                    }
                    int pos = Byte2ByteOpenCustomHashMap.this.n;
                    while (pos-- != 0) {
                        if (Byte2ByteOpenCustomHashMap.this.key[pos] == 0) continue;
                        consumer.accept(Byte2ByteOpenCustomHashMap.this.value[pos]);
                    }
                }

                @Override
                public int size() {
                    return Byte2ByteOpenCustomHashMap.this.size;
                }

                @Override
                public boolean contains(byte v) {
                    return Byte2ByteOpenCustomHashMap.this.containsValue(v);
                }

                @Override
                public void clear() {
                    Byte2ByteOpenCustomHashMap.this.clear();
                }
            };
        }
        return this.values;
    }

    public boolean trim() {
        return this.trim(this.size);
    }

    public boolean trim(int n) {
        int l = HashCommon.nextPowerOfTwo((int)Math.ceil((float)n / this.f));
        if (l >= this.n || this.size > HashCommon.maxFill(l, this.f)) {
            return true;
        }
        try {
            this.rehash(l);
        }
        catch (OutOfMemoryError cantDoIt) {
            return false;
        }
        return true;
    }

    protected void rehash(int newN) {
        byte[] key = this.key;
        byte[] value = this.value;
        int mask = newN - 1;
        byte[] newKey = new byte[newN + 1];
        byte[] newValue = new byte[newN + 1];
        int i = this.n;
        int j = this.realSize();
        while (j-- != 0) {
            while (key[--i] == 0) {
            }
            int pos = HashCommon.mix(this.strategy.hashCode(key[i])) & mask;
            if (newKey[pos] != 0) {
                while (newKey[pos = pos + 1 & mask] != 0) {
                }
            }
            newKey[pos] = key[i];
            newValue[pos] = value[i];
        }
        newValue[newN] = value[this.n];
        this.n = newN;
        this.mask = mask;
        this.maxFill = HashCommon.maxFill(this.n, this.f);
        this.key = newKey;
        this.value = newValue;
    }

    public Byte2ByteOpenCustomHashMap clone() {
        Byte2ByteOpenCustomHashMap c;
        try {
            c = (Byte2ByteOpenCustomHashMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.keys = null;
        c.values = null;
        c.entries = null;
        c.containsNullKey = this.containsNullKey;
        c.key = (byte[])this.key.clone();
        c.value = (byte[])this.value.clone();
        c.strategy = this.strategy;
        return c;
    }

    @Override
    public int hashCode() {
        int h = 0;
        int j = this.realSize();
        int i = 0;
        int t = 0;
        while (j-- != 0) {
            while (this.key[i] == 0) {
                ++i;
            }
            t = this.strategy.hashCode(this.key[i]);
            h += (t ^= this.value[i]);
            ++i;
        }
        if (this.containsNullKey) {
            h += this.value[this.n];
        }
        return h;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        byte[] key = this.key;
        byte[] value = this.value;
        EntryIterator i = new EntryIterator();
        s.defaultWriteObject();
        int j = this.size;
        while (j-- != 0) {
            int e = i.nextEntry();
            s.writeByte(key[e]);
            s.writeByte(value[e]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.n = HashCommon.arraySize(this.size, this.f);
        this.maxFill = HashCommon.maxFill(this.n, this.f);
        this.mask = this.n - 1;
        this.key = new byte[this.n + 1];
        byte[] key = this.key;
        this.value = new byte[this.n + 1];
        byte[] value = this.value;
        int i = this.size;
        while (i-- != 0) {
            int pos;
            byte k = s.readByte();
            byte v = s.readByte();
            if (this.strategy.equals(k, (byte)0)) {
                pos = this.n;
                this.containsNullKey = true;
            } else {
                pos = HashCommon.mix(this.strategy.hashCode(k)) & this.mask;
                while (key[pos] != 0) {
                    pos = pos + 1 & this.mask;
                }
            }
            key[pos] = k;
            value[pos] = v;
        }
    }

    private void checkTable() {
    }

    private final class MapEntrySet
    extends AbstractObjectSet<Byte2ByteMap.Entry>
    implements Byte2ByteMap.FastEntrySet {
        private MapEntrySet() {
        }

        @Override
        public ObjectIterator<Byte2ByteMap.Entry> iterator() {
            return new EntryIterator();
        }

        @Override
        public ObjectIterator<Byte2ByteMap.Entry> fastIterator() {
            return new FastEntryIterator();
        }

        @Override
        public ObjectSpliterator<Byte2ByteMap.Entry> spliterator() {
            return new EntrySpliterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            byte v = (Byte)e.getValue();
            if (Byte2ByteOpenCustomHashMap.this.strategy.equals(k, (byte)0)) {
                return Byte2ByteOpenCustomHashMap.this.containsNullKey && Byte2ByteOpenCustomHashMap.this.value[Byte2ByteOpenCustomHashMap.this.n] == v;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            int pos = HashCommon.mix(Byte2ByteOpenCustomHashMap.this.strategy.hashCode(k)) & Byte2ByteOpenCustomHashMap.this.mask;
            byte curr = key[pos];
            if (curr == 0) {
                return false;
            }
            if (Byte2ByteOpenCustomHashMap.this.strategy.equals(k, curr)) {
                return Byte2ByteOpenCustomHashMap.this.value[pos] == v;
            }
            do {
                if ((curr = key[pos = pos + 1 & Byte2ByteOpenCustomHashMap.this.mask]) != 0) continue;
                return false;
            } while (!Byte2ByteOpenCustomHashMap.this.strategy.equals(k, curr));
            return Byte2ByteOpenCustomHashMap.this.value[pos] == v;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Byte)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Byte)) {
                return false;
            }
            byte k = (Byte)e.getKey();
            byte v = (Byte)e.getValue();
            if (Byte2ByteOpenCustomHashMap.this.strategy.equals(k, (byte)0)) {
                if (Byte2ByteOpenCustomHashMap.this.containsNullKey && Byte2ByteOpenCustomHashMap.this.value[Byte2ByteOpenCustomHashMap.this.n] == v) {
                    Byte2ByteOpenCustomHashMap.this.removeNullEntry();
                    return true;
                }
                return false;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            int pos = HashCommon.mix(Byte2ByteOpenCustomHashMap.this.strategy.hashCode(k)) & Byte2ByteOpenCustomHashMap.this.mask;
            byte curr = key[pos];
            if (curr == 0) {
                return false;
            }
            if (Byte2ByteOpenCustomHashMap.this.strategy.equals(curr, k)) {
                if (Byte2ByteOpenCustomHashMap.this.value[pos] == v) {
                    Byte2ByteOpenCustomHashMap.this.removeEntry(pos);
                    return true;
                }
                return false;
            }
            do {
                if ((curr = key[pos = pos + 1 & Byte2ByteOpenCustomHashMap.this.mask]) != 0) continue;
                return false;
            } while (!Byte2ByteOpenCustomHashMap.this.strategy.equals(curr, k) || Byte2ByteOpenCustomHashMap.this.value[pos] != v);
            Byte2ByteOpenCustomHashMap.this.removeEntry(pos);
            return true;
        }

        @Override
        public int size() {
            return Byte2ByteOpenCustomHashMap.this.size;
        }

        @Override
        public void clear() {
            Byte2ByteOpenCustomHashMap.this.clear();
        }

        @Override
        public void forEach(Consumer<? super Byte2ByteMap.Entry> consumer) {
            if (Byte2ByteOpenCustomHashMap.this.containsNullKey) {
                consumer.accept(new AbstractByte2ByteMap.BasicEntry(Byte2ByteOpenCustomHashMap.this.key[Byte2ByteOpenCustomHashMap.this.n], Byte2ByteOpenCustomHashMap.this.value[Byte2ByteOpenCustomHashMap.this.n]));
            }
            int pos = Byte2ByteOpenCustomHashMap.this.n;
            while (pos-- != 0) {
                if (Byte2ByteOpenCustomHashMap.this.key[pos] == 0) continue;
                consumer.accept(new AbstractByte2ByteMap.BasicEntry(Byte2ByteOpenCustomHashMap.this.key[pos], Byte2ByteOpenCustomHashMap.this.value[pos]));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Byte2ByteMap.Entry> consumer) {
            AbstractByte2ByteMap.BasicEntry entry = new AbstractByte2ByteMap.BasicEntry();
            if (Byte2ByteOpenCustomHashMap.this.containsNullKey) {
                entry.key = Byte2ByteOpenCustomHashMap.this.key[Byte2ByteOpenCustomHashMap.this.n];
                entry.value = Byte2ByteOpenCustomHashMap.this.value[Byte2ByteOpenCustomHashMap.this.n];
                consumer.accept(entry);
            }
            int pos = Byte2ByteOpenCustomHashMap.this.n;
            while (pos-- != 0) {
                if (Byte2ByteOpenCustomHashMap.this.key[pos] == 0) continue;
                entry.key = Byte2ByteOpenCustomHashMap.this.key[pos];
                entry.value = Byte2ByteOpenCustomHashMap.this.value[pos];
                consumer.accept(entry);
            }
        }
    }

    private final class KeySet
    extends AbstractByteSet {
        private KeySet() {
        }

        @Override
        public ByteIterator iterator() {
            return new KeyIterator();
        }

        @Override
        public ByteSpliterator spliterator() {
            return new KeySpliterator();
        }

        @Override
        public void forEach(ByteConsumer consumer) {
            if (Byte2ByteOpenCustomHashMap.this.containsNullKey) {
                consumer.accept(Byte2ByteOpenCustomHashMap.this.key[Byte2ByteOpenCustomHashMap.this.n]);
            }
            int pos = Byte2ByteOpenCustomHashMap.this.n;
            while (pos-- != 0) {
                byte k = Byte2ByteOpenCustomHashMap.this.key[pos];
                if (k == 0) continue;
                consumer.accept(k);
            }
        }

        @Override
        public int size() {
            return Byte2ByteOpenCustomHashMap.this.size;
        }

        @Override
        public boolean contains(byte k) {
            return Byte2ByteOpenCustomHashMap.this.containsKey(k);
        }

        @Override
        public boolean remove(byte k) {
            int oldSize = Byte2ByteOpenCustomHashMap.this.size;
            Byte2ByteOpenCustomHashMap.this.remove(k);
            return Byte2ByteOpenCustomHashMap.this.size != oldSize;
        }

        @Override
        public void clear() {
            Byte2ByteOpenCustomHashMap.this.clear();
        }
    }

    private final class EntryIterator
    extends MapIterator<Consumer<? super Byte2ByteMap.Entry>>
    implements ObjectIterator<Byte2ByteMap.Entry> {
        private MapEntry entry;

        private EntryIterator() {
        }

        @Override
        public MapEntry next() {
            this.entry = new MapEntry(this.nextEntry());
            return this.entry;
        }

        @Override
        final void acceptOnIndex(Consumer<? super Byte2ByteMap.Entry> action, int index) {
            this.entry = new MapEntry(index);
            action.accept(this.entry);
        }

        @Override
        public void remove() {
            super.remove();
            this.entry.index = -1;
        }
    }

    private final class ValueSpliterator
    extends MapSpliterator<ByteConsumer, ValueSpliterator>
    implements ByteSpliterator {
        private static final int POST_SPLIT_CHARACTERISTICS = 256;

        ValueSpliterator() {
        }

        ValueSpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
            super(pos, max, mustReturnNull, hasSplit);
        }

        @Override
        public int characteristics() {
            return this.hasSplit ? 256 : 320;
        }

        @Override
        final void acceptOnIndex(ByteConsumer action, int index) {
            action.accept(Byte2ByteOpenCustomHashMap.this.value[index]);
        }

        @Override
        final ValueSpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
            return new ValueSpliterator(pos, max, mustReturnNull, true);
        }
    }

    private final class ValueIterator
    extends MapIterator<ByteConsumer>
    implements ByteIterator {
        @Override
        final void acceptOnIndex(ByteConsumer action, int index) {
            action.accept(Byte2ByteOpenCustomHashMap.this.value[index]);
        }

        @Override
        public byte nextByte() {
            return Byte2ByteOpenCustomHashMap.this.value[this.nextEntry()];
        }
    }

    private final class KeySpliterator
    extends MapSpliterator<ByteConsumer, KeySpliterator>
    implements ByteSpliterator {
        private static final int POST_SPLIT_CHARACTERISTICS = 257;

        KeySpliterator() {
        }

        KeySpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
            super(pos, max, mustReturnNull, hasSplit);
        }

        @Override
        public int characteristics() {
            return this.hasSplit ? 257 : 321;
        }

        @Override
        final void acceptOnIndex(ByteConsumer action, int index) {
            action.accept(Byte2ByteOpenCustomHashMap.this.key[index]);
        }

        @Override
        final KeySpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
            return new KeySpliterator(pos, max, mustReturnNull, true);
        }
    }

    private final class KeyIterator
    extends MapIterator<ByteConsumer>
    implements ByteIterator {
        @Override
        final void acceptOnIndex(ByteConsumer action, int index) {
            action.accept(Byte2ByteOpenCustomHashMap.this.key[index]);
        }

        @Override
        public byte nextByte() {
            return Byte2ByteOpenCustomHashMap.this.key[this.nextEntry()];
        }
    }

    private final class EntrySpliterator
    extends MapSpliterator<Consumer<? super Byte2ByteMap.Entry>, EntrySpliterator>
    implements ObjectSpliterator<Byte2ByteMap.Entry> {
        private static final int POST_SPLIT_CHARACTERISTICS = 1;

        EntrySpliterator() {
        }

        EntrySpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
            super(pos, max, mustReturnNull, hasSplit);
        }

        @Override
        public int characteristics() {
            return this.hasSplit ? 1 : 65;
        }

        @Override
        final void acceptOnIndex(Consumer<? super Byte2ByteMap.Entry> action, int index) {
            action.accept(new MapEntry(index));
        }

        @Override
        final EntrySpliterator makeForSplit(int pos, int max, boolean mustReturnNull) {
            return new EntrySpliterator(pos, max, mustReturnNull, true);
        }
    }

    private abstract class MapSpliterator<ConsumerType, SplitType extends MapSpliterator<ConsumerType, SplitType>> {
        int pos = 0;
        int max;
        int c;
        boolean mustReturnNull;
        boolean hasSplit;

        MapSpliterator() {
            this.max = Byte2ByteOpenCustomHashMap.this.n;
            this.c = 0;
            this.mustReturnNull = Byte2ByteOpenCustomHashMap.this.containsNullKey;
            this.hasSplit = false;
        }

        MapSpliterator(int pos, int max, boolean mustReturnNull, boolean hasSplit) {
            this.max = Byte2ByteOpenCustomHashMap.this.n;
            this.c = 0;
            this.mustReturnNull = Byte2ByteOpenCustomHashMap.this.containsNullKey;
            this.hasSplit = false;
            this.pos = pos;
            this.max = max;
            this.mustReturnNull = mustReturnNull;
            this.hasSplit = hasSplit;
        }

        abstract void acceptOnIndex(ConsumerType var1, int var2);

        abstract SplitType makeForSplit(int var1, int var2, boolean var3);

        public boolean tryAdvance(ConsumerType action) {
            if (this.mustReturnNull) {
                this.mustReturnNull = false;
                ++this.c;
                this.acceptOnIndex(action, Byte2ByteOpenCustomHashMap.this.n);
                return true;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            while (this.pos < this.max) {
                if (key[this.pos] != 0) {
                    ++this.c;
                    this.acceptOnIndex(action, this.pos++);
                    return true;
                }
                ++this.pos;
            }
            return false;
        }

        public void forEachRemaining(ConsumerType action) {
            if (this.mustReturnNull) {
                this.mustReturnNull = false;
                ++this.c;
                this.acceptOnIndex(action, Byte2ByteOpenCustomHashMap.this.n);
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            while (this.pos < this.max) {
                if (key[this.pos] != 0) {
                    this.acceptOnIndex(action, this.pos);
                    ++this.c;
                }
                ++this.pos;
            }
        }

        public long estimateSize() {
            if (!this.hasSplit) {
                return Byte2ByteOpenCustomHashMap.this.size - this.c;
            }
            return Math.min((long)(Byte2ByteOpenCustomHashMap.this.size - this.c), (long)((double)Byte2ByteOpenCustomHashMap.this.realSize() / (double)Byte2ByteOpenCustomHashMap.this.n * (double)(this.max - this.pos)) + (long)(this.mustReturnNull ? 1 : 0));
        }

        public SplitType trySplit() {
            if (this.pos >= this.max - 1) {
                return null;
            }
            int retLen = this.max - this.pos >> 1;
            if (retLen <= 1) {
                return null;
            }
            int myNewPos = this.pos + retLen;
            int retPos = this.pos;
            int retMax = myNewPos;
            SplitType split = this.makeForSplit(retPos, retMax, this.mustReturnNull);
            this.pos = myNewPos;
            this.mustReturnNull = false;
            this.hasSplit = true;
            return split;
        }

        public long skip(long n) {
            if (n < 0L) {
                throw new IllegalArgumentException("Argument must be nonnegative: " + n);
            }
            if (n == 0L) {
                return 0L;
            }
            long skipped = 0L;
            if (this.mustReturnNull) {
                this.mustReturnNull = false;
                ++skipped;
                --n;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            while (this.pos < this.max && n > 0L) {
                if (key[this.pos++] == 0) continue;
                ++skipped;
                --n;
            }
            return skipped;
        }
    }

    private final class FastEntryIterator
    extends MapIterator<Consumer<? super Byte2ByteMap.Entry>>
    implements ObjectIterator<Byte2ByteMap.Entry> {
        private final MapEntry entry;

        private FastEntryIterator() {
            this.entry = new MapEntry();
        }

        @Override
        public MapEntry next() {
            this.entry.index = this.nextEntry();
            return this.entry;
        }

        @Override
        final void acceptOnIndex(Consumer<? super Byte2ByteMap.Entry> action, int index) {
            this.entry.index = index;
            action.accept(this.entry);
        }
    }

    private abstract class MapIterator<ConsumerType> {
        int pos;
        int last;
        int c;
        boolean mustReturnNullKey;
        ByteArrayList wrapped;

        private MapIterator() {
            this.pos = Byte2ByteOpenCustomHashMap.this.n;
            this.last = -1;
            this.c = Byte2ByteOpenCustomHashMap.this.size;
            this.mustReturnNullKey = Byte2ByteOpenCustomHashMap.this.containsNullKey;
        }

        abstract void acceptOnIndex(ConsumerType var1, int var2);

        public boolean hasNext() {
            return this.c != 0;
        }

        public int nextEntry() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            --this.c;
            if (this.mustReturnNullKey) {
                this.mustReturnNullKey = false;
                this.last = Byte2ByteOpenCustomHashMap.this.n;
                return this.last;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            do {
                if (--this.pos >= 0) continue;
                this.last = Integer.MIN_VALUE;
                byte k = this.wrapped.getByte(-this.pos - 1);
                int p = HashCommon.mix(Byte2ByteOpenCustomHashMap.this.strategy.hashCode(k)) & Byte2ByteOpenCustomHashMap.this.mask;
                while (!Byte2ByteOpenCustomHashMap.this.strategy.equals(k, key[p])) {
                    p = p + 1 & Byte2ByteOpenCustomHashMap.this.mask;
                }
                return p;
            } while (key[this.pos] == 0);
            this.last = this.pos;
            return this.last;
        }

        public void forEachRemaining(ConsumerType action) {
            if (this.mustReturnNullKey) {
                this.mustReturnNullKey = false;
                this.last = Byte2ByteOpenCustomHashMap.this.n;
                this.acceptOnIndex(action, this.last);
                --this.c;
            }
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            while (this.c != 0) {
                if (--this.pos < 0) {
                    this.last = Integer.MIN_VALUE;
                    byte k = this.wrapped.getByte(-this.pos - 1);
                    int p = HashCommon.mix(Byte2ByteOpenCustomHashMap.this.strategy.hashCode(k)) & Byte2ByteOpenCustomHashMap.this.mask;
                    while (!Byte2ByteOpenCustomHashMap.this.strategy.equals(k, key[p])) {
                        p = p + 1 & Byte2ByteOpenCustomHashMap.this.mask;
                    }
                    this.acceptOnIndex(action, p);
                    --this.c;
                    continue;
                }
                if (key[this.pos] == 0) continue;
                this.last = this.pos;
                this.acceptOnIndex(action, this.last);
                --this.c;
            }
        }

        private void shiftKeys(int pos) {
            byte[] key = Byte2ByteOpenCustomHashMap.this.key;
            while (true) {
                byte curr;
                int last = pos;
                pos = last + 1 & Byte2ByteOpenCustomHashMap.this.mask;
                while (true) {
                    if ((curr = key[pos]) == 0) {
                        key[last] = 0;
                        return;
                    }
                    int slot = HashCommon.mix(Byte2ByteOpenCustomHashMap.this.strategy.hashCode(curr)) & Byte2ByteOpenCustomHashMap.this.mask;
                    if (last <= pos ? last >= slot || slot > pos : last >= slot && slot > pos) break;
                    pos = pos + 1 & Byte2ByteOpenCustomHashMap.this.mask;
                }
                if (pos < last) {
                    if (this.wrapped == null) {
                        this.wrapped = new ByteArrayList(2);
                    }
                    this.wrapped.add(key[pos]);
                }
                key[last] = curr;
                Byte2ByteOpenCustomHashMap.this.value[last] = Byte2ByteOpenCustomHashMap.this.value[pos];
            }
        }

        public void remove() {
            if (this.last == -1) {
                throw new IllegalStateException();
            }
            if (this.last == Byte2ByteOpenCustomHashMap.this.n) {
                Byte2ByteOpenCustomHashMap.this.containsNullKey = false;
            } else if (this.pos >= 0) {
                this.shiftKeys(this.last);
            } else {
                Byte2ByteOpenCustomHashMap.this.remove(this.wrapped.getByte(-this.pos - 1));
                this.last = -1;
                return;
            }
            --Byte2ByteOpenCustomHashMap.this.size;
            this.last = -1;
        }

        public int skip(int n) {
            int i = n;
            while (i-- != 0 && this.hasNext()) {
                this.nextEntry();
            }
            return n - i - 1;
        }
    }

    final class MapEntry
    implements Byte2ByteMap.Entry,
    Map.Entry<Byte, Byte>,
    ByteBytePair {
        int index;

        MapEntry(int index) {
            this.index = index;
        }

        MapEntry() {
        }

        @Override
        public byte getByteKey() {
            return Byte2ByteOpenCustomHashMap.this.key[this.index];
        }

        @Override
        public byte leftByte() {
            return Byte2ByteOpenCustomHashMap.this.key[this.index];
        }

        @Override
        public byte getByteValue() {
            return Byte2ByteOpenCustomHashMap.this.value[this.index];
        }

        @Override
        public byte rightByte() {
            return Byte2ByteOpenCustomHashMap.this.value[this.index];
        }

        @Override
        public byte setValue(byte v) {
            byte oldValue = Byte2ByteOpenCustomHashMap.this.value[this.index];
            Byte2ByteOpenCustomHashMap.this.value[this.index] = v;
            return oldValue;
        }

        @Override
        public ByteBytePair right(byte v) {
            Byte2ByteOpenCustomHashMap.this.value[this.index] = v;
            return this;
        }

        @Override
        @Deprecated
        public Byte getKey() {
            return Byte2ByteOpenCustomHashMap.this.key[this.index];
        }

        @Override
        @Deprecated
        public Byte getValue() {
            return Byte2ByteOpenCustomHashMap.this.value[this.index];
        }

        @Override
        @Deprecated
        public Byte setValue(Byte v) {
            return this.setValue((byte)v);
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return Byte2ByteOpenCustomHashMap.this.strategy.equals(Byte2ByteOpenCustomHashMap.this.key[this.index], (Byte)e.getKey()) && Byte2ByteOpenCustomHashMap.this.value[this.index] == (Byte)e.getValue();
        }

        @Override
        public int hashCode() {
            return Byte2ByteOpenCustomHashMap.this.strategy.hashCode(Byte2ByteOpenCustomHashMap.this.key[this.index]) ^ Byte2ByteOpenCustomHashMap.this.value[this.index];
        }

        public String toString() {
            return Byte2ByteOpenCustomHashMap.this.key[this.index] + "=>" + Byte2ByteOpenCustomHashMap.this.value[this.index];
        }
    }
}

