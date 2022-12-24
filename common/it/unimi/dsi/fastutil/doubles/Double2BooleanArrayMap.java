/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.doubles;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterator;
import it.unimi.dsi.fastutil.booleans.BooleanSpliterators;
import it.unimi.dsi.fastutil.doubles.AbstractDouble2BooleanMap;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleSet;
import it.unimi.dsi.fastutil.doubles.Double2BooleanMap;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.doubles.DoubleSet;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterator;
import it.unimi.dsi.fastutil.doubles.DoubleSpliterators;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

public class Double2BooleanArrayMap
extends AbstractDouble2BooleanMap
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    private transient double[] key;
    private transient boolean[] value;
    private int size;
    private transient Double2BooleanMap.FastEntrySet entries;
    private transient DoubleSet keys;
    private transient BooleanCollection values;

    public Double2BooleanArrayMap(double[] key, boolean[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Double2BooleanArrayMap() {
        this.key = DoubleArrays.EMPTY_ARRAY;
        this.value = BooleanArrays.EMPTY_ARRAY;
    }

    public Double2BooleanArrayMap(int capacity) {
        this.key = new double[capacity];
        this.value = new boolean[capacity];
    }

    public Double2BooleanArrayMap(Double2BooleanMap m) {
        this(m.size());
        int i = 0;
        for (Double2BooleanMap.Entry e : m.double2BooleanEntrySet()) {
            this.key[i] = e.getDoubleKey();
            this.value[i] = e.getBooleanValue();
            ++i;
        }
        this.size = i;
    }

    public Double2BooleanArrayMap(Map<? extends Double, ? extends Boolean> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<? extends Double, ? extends Boolean> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Double2BooleanArrayMap(double[] key, boolean[] value, int size) {
        this.key = key;
        this.value = value;
        this.size = size;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
        if (size > key.length) {
            throw new IllegalArgumentException("The provided size (" + size + ") is larger than or equal to the backing-arrays size (" + key.length + ")");
        }
    }

    public Double2BooleanMap.FastEntrySet double2BooleanEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(double k) {
        double[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToLongBits(key[i]) != Double.doubleToLongBits(k)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public boolean get(double k) {
        double[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (Double.doubleToLongBits(key[i]) != Double.doubleToLongBits(k)) continue;
            return this.value[i];
        }
        return this.defRetValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        this.size = 0;
    }

    @Override
    public boolean containsKey(double k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(boolean v) {
        int i = this.size;
        while (i-- != 0) {
            if (this.value[i] != v) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public boolean put(double k, boolean v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            boolean oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return oldValue;
        }
        if (this.size == this.key.length) {
            double[] newKey = new double[this.size == 0 ? 2 : this.size * 2];
            boolean[] newValue = new boolean[this.size == 0 ? 2 : this.size * 2];
            int i = this.size;
            while (i-- != 0) {
                newKey[i] = this.key[i];
                newValue[i] = this.value[i];
            }
            this.key = newKey;
            this.value = newValue;
        }
        this.key[this.size] = k;
        this.value[this.size] = v;
        ++this.size;
        return this.defRetValue;
    }

    @Override
    public boolean remove(double k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return this.defRetValue;
        }
        boolean oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        return oldValue;
    }

    @Override
    public DoubleSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public BooleanCollection values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Double2BooleanArrayMap clone() {
        Double2BooleanArrayMap c;
        try {
            c = (Double2BooleanArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (double[])this.key.clone();
        c.value = (boolean[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeDouble(this.key[i]);
            s.writeBoolean(this.value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new double[this.size];
        this.value = new boolean[this.size];
        for (int i = 0; i < this.size; ++i) {
            this.key[i] = s.readDouble();
            this.value[i] = s.readBoolean();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Double2BooleanMap.Entry>
    implements Double2BooleanMap.FastEntrySet {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Double2BooleanMap.Entry> iterator() {
            return new ObjectIterator<Double2BooleanMap.Entry>(){
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Double2BooleanArrayMap.this.size;
                }

                @Override
                public Double2BooleanMap.Entry next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next;
                    return new AbstractDouble2BooleanMap.BasicEntry(Double2BooleanArrayMap.this.key[this.curr], Double2BooleanArrayMap.this.value[this.next++]);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Double2BooleanArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2BooleanArrayMap.this.key, this.next + 1, Double2BooleanArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2BooleanArrayMap.this.value, this.next + 1, Double2BooleanArrayMap.this.value, this.next, tail);
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2BooleanMap.Entry> action) {
                    int max = Double2BooleanArrayMap.this.size;
                    while (this.next < max) {
                        this.curr = this.next;
                        action.accept(new AbstractDouble2BooleanMap.BasicEntry(Double2BooleanArrayMap.this.key[this.curr], Double2BooleanArrayMap.this.value[this.next++]));
                    }
                }
            };
        }

        @Override
        public ObjectIterator<Double2BooleanMap.Entry> fastIterator() {
            return new ObjectIterator<Double2BooleanMap.Entry>(){
                int next = 0;
                int curr = -1;
                final AbstractDouble2BooleanMap.BasicEntry entry = new AbstractDouble2BooleanMap.BasicEntry();

                @Override
                public boolean hasNext() {
                    return this.next < Double2BooleanArrayMap.this.size;
                }

                @Override
                public Double2BooleanMap.Entry next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next;
                    this.entry.key = Double2BooleanArrayMap.this.key[this.curr];
                    this.entry.value = Double2BooleanArrayMap.this.value[this.next++];
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Double2BooleanArrayMap.this.size-- - this.next--;
                    System.arraycopy(Double2BooleanArrayMap.this.key, this.next + 1, Double2BooleanArrayMap.this.key, this.next, tail);
                    System.arraycopy(Double2BooleanArrayMap.this.value, this.next + 1, Double2BooleanArrayMap.this.value, this.next, tail);
                }

                @Override
                public void forEachRemaining(Consumer<? super Double2BooleanMap.Entry> action) {
                    int max = Double2BooleanArrayMap.this.size;
                    while (this.next < max) {
                        this.curr = this.next;
                        this.entry.key = Double2BooleanArrayMap.this.key[this.curr];
                        this.entry.value = Double2BooleanArrayMap.this.value[this.next++];
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Double2BooleanMap.Entry> spliterator() {
            return new EntrySetSpliterator(0, Double2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Double2BooleanMap.Entry> action) {
            int max = Double2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new AbstractDouble2BooleanMap.BasicEntry(Double2BooleanArrayMap.this.key[i], Double2BooleanArrayMap.this.value[i]));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Double2BooleanMap.Entry> action) {
            AbstractDouble2BooleanMap.BasicEntry entry = new AbstractDouble2BooleanMap.BasicEntry();
            int max = Double2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                entry.key = Double2BooleanArrayMap.this.key[i];
                entry.value = Double2BooleanArrayMap.this.value[i];
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Double2BooleanArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Double)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                return false;
            }
            double k = (Double)e.getKey();
            return Double2BooleanArrayMap.this.containsKey(k) && Double2BooleanArrayMap.this.get(k) == ((Boolean)e.getValue()).booleanValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            if (e.getKey() == null || !(e.getKey() instanceof Double)) {
                return false;
            }
            if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                return false;
            }
            double k = (Double)e.getKey();
            boolean v = (Boolean)e.getValue();
            int oldPos = Double2BooleanArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Double2BooleanArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Double2BooleanArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2BooleanArrayMap.this.key, oldPos + 1, Double2BooleanArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2BooleanArrayMap.this.value, oldPos + 1, Double2BooleanArrayMap.this.value, oldPos, tail);
            Double2BooleanArrayMap.this.size--;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Double2BooleanMap.Entry>
        implements ObjectSpliterator<Double2BooleanMap.Entry> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Double2BooleanMap.Entry get(int location) {
                return new AbstractDouble2BooleanMap.BasicEntry(Double2BooleanArrayMap.this.key[location], Double2BooleanArrayMap.this.value[location]);
            }

            protected final EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractDoubleSet {
        private KeySet() {
        }

        @Override
        public boolean contains(double k) {
            return Double2BooleanArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(double k) {
            int oldPos = Double2BooleanArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Double2BooleanArrayMap.this.size - oldPos - 1;
            System.arraycopy(Double2BooleanArrayMap.this.key, oldPos + 1, Double2BooleanArrayMap.this.key, oldPos, tail);
            System.arraycopy(Double2BooleanArrayMap.this.value, oldPos + 1, Double2BooleanArrayMap.this.value, oldPos, tail);
            Double2BooleanArrayMap.this.size--;
            return true;
        }

        @Override
        public DoubleIterator iterator() {
            return new DoubleIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2BooleanArrayMap.this.size;
                }

                @Override
                public double nextDouble() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2BooleanArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2BooleanArrayMap.this.size - this.pos;
                    System.arraycopy(Double2BooleanArrayMap.this.key, this.pos, Double2BooleanArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2BooleanArrayMap.this.value, this.pos, Double2BooleanArrayMap.this.value, this.pos - 1, tail);
                    Double2BooleanArrayMap.this.size--;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(DoubleConsumer action) {
                    int max = Double2BooleanArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(Double2BooleanArrayMap.this.key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public DoubleSpliterator spliterator() {
            return new KeySetSpliterator(0, Double2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(DoubleConsumer action) {
            int max = Double2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(Double2BooleanArrayMap.this.key[i]);
            }
        }

        @Override
        public int size() {
            return Double2BooleanArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2BooleanArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends DoubleSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements DoubleSpliterator {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16721;
            }

            @Override
            protected final double get(int location) {
                return Double2BooleanArrayMap.this.key[location];
            }

            @Override
            protected final KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(DoubleConsumer action) {
                int max = Double2BooleanArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(Double2BooleanArrayMap.this.key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractBooleanCollection {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(boolean v) {
            return Double2BooleanArrayMap.this.containsValue(v);
        }

        @Override
        public BooleanIterator iterator() {
            return new BooleanIterator(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Double2BooleanArrayMap.this.size;
                }

                @Override
                public boolean nextBoolean() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Double2BooleanArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Double2BooleanArrayMap.this.size - this.pos;
                    System.arraycopy(Double2BooleanArrayMap.this.key, this.pos, Double2BooleanArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Double2BooleanArrayMap.this.value, this.pos, Double2BooleanArrayMap.this.value, this.pos - 1, tail);
                    Double2BooleanArrayMap.this.size--;
                    --this.pos;
                }

                @Override
                public void forEachRemaining(BooleanConsumer action) {
                    int max = Double2BooleanArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(Double2BooleanArrayMap.this.value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public BooleanSpliterator spliterator() {
            return new ValuesSpliterator(0, Double2BooleanArrayMap.this.size);
        }

        @Override
        public void forEach(BooleanConsumer action) {
            int max = Double2BooleanArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(Double2BooleanArrayMap.this.value[i]);
            }
        }

        @Override
        public int size() {
            return Double2BooleanArrayMap.this.size;
        }

        @Override
        public void clear() {
            Double2BooleanArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends BooleanSpliterators.EarlyBindingSizeIndexBasedSpliterator
        implements BooleanSpliterator {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16720;
            }

            @Override
            protected final boolean get(int location) {
                return Double2BooleanArrayMap.this.value[location];
            }

            @Override
            protected final ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(BooleanConsumer action) {
                int max = Double2BooleanArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(Double2BooleanArrayMap.this.value[this.pos++]);
                }
            }
        }
    }
}

