/*
 * Decompiled with CFR 0.150.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$EntrySet.EntrySetSpliterator
 *  it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$KeySet.KeySetSpliterator
 *  it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$ValuesCollection.ValuesSpliterator
 */
package it.unimi.dsi.fastutil.objects;

import it.unimi.dsi.fastutil.objects.AbstractObject2ReferenceMap;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.AbstractReferenceCollection;
import it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSpliterator;
import it.unimi.dsi.fastutil.objects.ObjectSpliterators;
import it.unimi.dsi.fastutil.objects.ReferenceCollection;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;

public class Object2ReferenceArrayMap<K, V>
extends AbstractObject2ReferenceMap<K, V>
implements Serializable,
Cloneable {
    private static final long serialVersionUID = 1L;
    private transient Object[] key;
    private transient Object[] value;
    private int size;
    private transient Object2ReferenceMap.FastEntrySet<K, V> entries;
    private transient ObjectSet<K> keys;
    private transient ReferenceCollection<V> values;

    public Object2ReferenceArrayMap(Object[] key, Object[] value) {
        this.key = key;
        this.value = value;
        this.size = key.length;
        if (key.length != value.length) {
            throw new IllegalArgumentException("Keys and values have different lengths (" + key.length + ", " + value.length + ")");
        }
    }

    public Object2ReferenceArrayMap() {
        this.key = ObjectArrays.EMPTY_ARRAY;
        this.value = ObjectArrays.EMPTY_ARRAY;
    }

    public Object2ReferenceArrayMap(int capacity) {
        this.key = new Object[capacity];
        this.value = new Object[capacity];
    }

    public Object2ReferenceArrayMap(Object2ReferenceMap<K, V> m) {
        this(m.size());
        int i = 0;
        for (Object2ReferenceMap.Entry entry : m.object2ReferenceEntrySet()) {
            this.key[i] = entry.getKey();
            this.value[i] = entry.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2ReferenceArrayMap(Map<? extends K, ? extends V> m) {
        this(m.size());
        int i = 0;
        for (Map.Entry<K, V> e : m.entrySet()) {
            this.key[i] = e.getKey();
            this.value[i] = e.getValue();
            ++i;
        }
        this.size = i;
    }

    public Object2ReferenceArrayMap(Object[] key, Object[] value, int size) {
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

    public Object2ReferenceMap.FastEntrySet<K, V> object2ReferenceEntrySet() {
        if (this.entries == null) {
            this.entries = new EntrySet();
        }
        return this.entries;
    }

    private int findKey(Object k) {
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (!Objects.equals(key[i], k)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public V get(Object k) {
        Object[] key = this.key;
        int i = this.size;
        while (i-- != 0) {
            if (!Objects.equals(key[i], k)) continue;
            return (V)this.value[i];
        }
        return (V)this.defRetValue;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public void clear() {
        int i = this.size;
        while (i-- != 0) {
            this.key[i] = null;
            this.value[i] = null;
        }
        this.size = 0;
    }

    @Override
    public boolean containsKey(Object k) {
        return this.findKey(k) != -1;
    }

    @Override
    public boolean containsValue(Object v) {
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
    public V put(K k, V v) {
        int oldKey = this.findKey(k);
        if (oldKey != -1) {
            Object oldValue = this.value[oldKey];
            this.value[oldKey] = v;
            return (V)oldValue;
        }
        if (this.size == this.key.length) {
            Object[] newKey = new Object[this.size == 0 ? 2 : this.size * 2];
            Object[] newValue = new Object[this.size == 0 ? 2 : this.size * 2];
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
        return (V)this.defRetValue;
    }

    @Override
    public V remove(Object k) {
        int oldPos = this.findKey(k);
        if (oldPos == -1) {
            return (V)this.defRetValue;
        }
        Object oldValue = this.value[oldPos];
        int tail = this.size - oldPos - 1;
        System.arraycopy(this.key, oldPos + 1, this.key, oldPos, tail);
        System.arraycopy(this.value, oldPos + 1, this.value, oldPos, tail);
        --this.size;
        this.key[this.size] = null;
        this.value[this.size] = null;
        return (V)oldValue;
    }

    @Override
    public ObjectSet<K> keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public ReferenceCollection<V> values() {
        if (this.values == null) {
            this.values = new ValuesCollection();
        }
        return this.values;
    }

    public Object2ReferenceArrayMap<K, V> clone() {
        Object2ReferenceArrayMap c;
        try {
            c = (Object2ReferenceArrayMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.key = (Object[])this.key.clone();
        c.value = (Object[])this.value.clone();
        c.entries = null;
        c.keys = null;
        c.values = null;
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        int max = this.size;
        for (int i = 0; i < max; ++i) {
            s.writeObject(this.key[i]);
            s.writeObject(this.value[i]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.key = new Object[this.size];
        this.value = new Object[this.size];
        for (int i = 0; i < this.size; ++i) {
            this.key[i] = s.readObject();
            this.value[i] = s.readObject();
        }
    }

    private final class EntrySet
    extends AbstractObjectSet<Object2ReferenceMap.Entry<K, V>>
    implements Object2ReferenceMap.FastEntrySet<K, V> {
        private EntrySet() {
        }

        @Override
        public ObjectIterator<Object2ReferenceMap.Entry<K, V>> iterator() {
            return new ObjectIterator<Object2ReferenceMap.Entry<K, V>>(){
                int curr = -1;
                int next = 0;

                @Override
                public boolean hasNext() {
                    return this.next < Object2ReferenceArrayMap.this.size;
                }

                @Override
                public Object2ReferenceMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next;
                    return new AbstractObject2ReferenceMap.BasicEntry<Object, Object>(Object2ReferenceArrayMap.this.key[this.curr], Object2ReferenceArrayMap.this.value[this.next++]);
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Object2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2ReferenceArrayMap.this.key, this.next + 1, Object2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2ReferenceArrayMap.this.value, this.next + 1, Object2ReferenceArrayMap.this.value, this.next, tail);
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2ReferenceMap.Entry<K, V>> action) {
                    int max = Object2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        this.curr = this.next;
                        action.accept(new AbstractObject2ReferenceMap.BasicEntry<Object, Object>(Object2ReferenceArrayMap.this.key[this.curr], Object2ReferenceArrayMap.this.value[this.next++]));
                    }
                }
            };
        }

        @Override
        public ObjectIterator<Object2ReferenceMap.Entry<K, V>> fastIterator() {
            return new ObjectIterator<Object2ReferenceMap.Entry<K, V>>(){
                int next = 0;
                int curr = -1;
                final AbstractObject2ReferenceMap.BasicEntry<K, V> entry = new AbstractObject2ReferenceMap.BasicEntry();

                @Override
                public boolean hasNext() {
                    return this.next < Object2ReferenceArrayMap.this.size;
                }

                @Override
                public Object2ReferenceMap.Entry<K, V> next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    this.curr = this.next;
                    this.entry.key = Object2ReferenceArrayMap.this.key[this.curr];
                    this.entry.value = Object2ReferenceArrayMap.this.value[this.next++];
                    return this.entry;
                }

                @Override
                public void remove() {
                    if (this.curr == -1) {
                        throw new IllegalStateException();
                    }
                    this.curr = -1;
                    int tail = Object2ReferenceArrayMap.this.size-- - this.next--;
                    System.arraycopy(Object2ReferenceArrayMap.this.key, this.next + 1, Object2ReferenceArrayMap.this.key, this.next, tail);
                    System.arraycopy(Object2ReferenceArrayMap.this.value, this.next + 1, Object2ReferenceArrayMap.this.value, this.next, tail);
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super Object2ReferenceMap.Entry<K, V>> action) {
                    int max = Object2ReferenceArrayMap.this.size;
                    while (this.next < max) {
                        this.curr = this.next;
                        this.entry.key = Object2ReferenceArrayMap.this.key[this.curr];
                        this.entry.value = Object2ReferenceArrayMap.this.value[this.next++];
                        action.accept(this.entry);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<Object2ReferenceMap.Entry<K, V>> spliterator() {
            return new EntrySetSpliterator(0, Object2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super Object2ReferenceMap.Entry<K, V>> action) {
            int max = Object2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(new AbstractObject2ReferenceMap.BasicEntry<Object, Object>(Object2ReferenceArrayMap.this.key[i], Object2ReferenceArrayMap.this.value[i]));
            }
        }

        @Override
        public void fastForEach(Consumer<? super Object2ReferenceMap.Entry<K, V>> action) {
            AbstractObject2ReferenceMap.BasicEntry entry = new AbstractObject2ReferenceMap.BasicEntry();
            int max = Object2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                entry.key = Object2ReferenceArrayMap.this.key[i];
                entry.value = Object2ReferenceArrayMap.this.value[i];
                action.accept(entry);
            }
        }

        @Override
        public int size() {
            return Object2ReferenceArrayMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object k = e.getKey();
            return Object2ReferenceArrayMap.this.containsKey(k) && Object2ReferenceArrayMap.this.get(k) == e.getValue();
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            Object k = e.getKey();
            Object v = e.getValue();
            int oldPos = Object2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1 || v != Object2ReferenceArrayMap.this.value[oldPos]) {
                return false;
            }
            int tail = Object2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2ReferenceArrayMap.this.key, oldPos + 1, Object2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2ReferenceArrayMap.this.value, oldPos + 1, Object2ReferenceArrayMap.this.value, oldPos, tail);
            Object2ReferenceArrayMap.this.size--;
            ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
            ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
            return true;
        }

        final class EntrySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<Object2ReferenceMap.Entry<K, V>>
        implements ObjectSpliterator<Object2ReferenceMap.Entry<K, V>> {
            EntrySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final Object2ReferenceMap.Entry<K, V> get(int location) {
                return new AbstractObject2ReferenceMap.BasicEntry<Object, Object>(Object2ReferenceArrayMap.this.key[location], Object2ReferenceArrayMap.this.value[location]);
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$EntrySet.EntrySetSpliterator makeForSplit(int pos, int maxPos) {
                return new EntrySetSpliterator(pos, maxPos);
            }
        }
    }

    private final class KeySet
    extends AbstractObjectSet<K> {
        private KeySet() {
        }

        @Override
        public boolean contains(Object k) {
            return Object2ReferenceArrayMap.this.findKey(k) != -1;
        }

        @Override
        public boolean remove(Object k) {
            int oldPos = Object2ReferenceArrayMap.this.findKey(k);
            if (oldPos == -1) {
                return false;
            }
            int tail = Object2ReferenceArrayMap.this.size - oldPos - 1;
            System.arraycopy(Object2ReferenceArrayMap.this.key, oldPos + 1, Object2ReferenceArrayMap.this.key, oldPos, tail);
            System.arraycopy(Object2ReferenceArrayMap.this.value, oldPos + 1, Object2ReferenceArrayMap.this.value, oldPos, tail);
            Object2ReferenceArrayMap.this.size--;
            ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
            ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
            return true;
        }

        @Override
        public ObjectIterator<K> iterator() {
            return new ObjectIterator<K>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2ReferenceArrayMap.this.size;
                }

                @Override
                public K next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2ReferenceArrayMap.this.key[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Object2ReferenceArrayMap.this.key, this.pos, Object2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2ReferenceArrayMap.this.value, this.pos, Object2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    Object2ReferenceArrayMap.this.size--;
                    --this.pos;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super K> action) {
                    int max = Object2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(Object2ReferenceArrayMap.this.key[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<K> spliterator() {
            return new KeySetSpliterator(0, Object2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super K> action) {
            int max = Object2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(Object2ReferenceArrayMap.this.key[i]);
            }
        }

        @Override
        public int size() {
            return Object2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2ReferenceArrayMap.this.clear();
        }

        final class KeySetSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<K>
        implements ObjectSpliterator<K> {
            KeySetSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16465;
            }

            @Override
            protected final K get(int location) {
                return Object2ReferenceArrayMap.this.key[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$KeySet.KeySetSpliterator makeForSplit(int pos, int maxPos) {
                return new KeySetSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super K> action) {
                int max = Object2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(Object2ReferenceArrayMap.this.key[this.pos++]);
                }
            }
        }
    }

    private final class ValuesCollection
    extends AbstractReferenceCollection<V> {
        private ValuesCollection() {
        }

        @Override
        public boolean contains(Object v) {
            return Object2ReferenceArrayMap.this.containsValue(v);
        }

        @Override
        public ObjectIterator<V> iterator() {
            return new ObjectIterator<V>(){
                int pos = 0;

                @Override
                public boolean hasNext() {
                    return this.pos < Object2ReferenceArrayMap.this.size;
                }

                @Override
                public V next() {
                    if (!this.hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return Object2ReferenceArrayMap.this.value[this.pos++];
                }

                @Override
                public void remove() {
                    if (this.pos == 0) {
                        throw new IllegalStateException();
                    }
                    int tail = Object2ReferenceArrayMap.this.size - this.pos;
                    System.arraycopy(Object2ReferenceArrayMap.this.key, this.pos, Object2ReferenceArrayMap.this.key, this.pos - 1, tail);
                    System.arraycopy(Object2ReferenceArrayMap.this.value, this.pos, Object2ReferenceArrayMap.this.value, this.pos - 1, tail);
                    Object2ReferenceArrayMap.this.size--;
                    --this.pos;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).key[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                    ((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).value[((Object2ReferenceArrayMap)Object2ReferenceArrayMap.this).size] = null;
                }

                @Override
                public void forEachRemaining(Consumer<? super V> action) {
                    int max = Object2ReferenceArrayMap.this.size;
                    while (this.pos < max) {
                        action.accept(Object2ReferenceArrayMap.this.value[this.pos++]);
                    }
                }
            };
        }

        @Override
        public ObjectSpliterator<V> spliterator() {
            return new ValuesSpliterator(0, Object2ReferenceArrayMap.this.size);
        }

        @Override
        public void forEach(Consumer<? super V> action) {
            int max = Object2ReferenceArrayMap.this.size;
            for (int i = 0; i < max; ++i) {
                action.accept(Object2ReferenceArrayMap.this.value[i]);
            }
        }

        @Override
        public int size() {
            return Object2ReferenceArrayMap.this.size;
        }

        @Override
        public void clear() {
            Object2ReferenceArrayMap.this.clear();
        }

        final class ValuesSpliterator
        extends ObjectSpliterators.EarlyBindingSizeIndexBasedSpliterator<V>
        implements ObjectSpliterator<V> {
            ValuesSpliterator(int pos, int maxPos) {
                super(pos, maxPos);
            }

            @Override
            public int characteristics() {
                return 16464;
            }

            @Override
            protected final V get(int location) {
                return Object2ReferenceArrayMap.this.value[location];
            }

            protected final it.unimi.dsi.fastutil.objects.Object2ReferenceArrayMap$ValuesCollection.ValuesSpliterator makeForSplit(int pos, int maxPos) {
                return new ValuesSpliterator(pos, maxPos);
            }

            @Override
            public void forEachRemaining(Consumer<? super V> action) {
                int max = Object2ReferenceArrayMap.this.size;
                while (this.pos < max) {
                    action.accept(Object2ReferenceArrayMap.this.value[this.pos++]);
                }
            }
        }
    }
}

