/*
 * Decompiled with CFR 0.150.
 */
package it.unimi.dsi.fastutil.longs;

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLong2BooleanMap;
import it.unimi.dsi.fastutil.longs.AbstractLong2BooleanSortedMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2BooleanSortedMap;
import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongComparators;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSortedSet;
import it.unimi.dsi.fastutil.objects.ObjectBidirectionalIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ObjectSortedSet;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;

public class Long2BooleanRBTreeMap
extends AbstractLong2BooleanSortedMap
implements Serializable,
Cloneable {
    protected transient Entry tree;
    protected int count;
    protected transient Entry firstEntry;
    protected transient Entry lastEntry;
    protected transient ObjectSortedSet<Long2BooleanMap.Entry> entries;
    protected transient LongSortedSet keys;
    protected transient BooleanCollection values;
    protected transient boolean modified;
    protected Comparator<? super Long> storedComparator;
    protected transient LongComparator actualComparator;
    private static final long serialVersionUID = -7046029254386353129L;
    private transient boolean[] dirPath;
    private transient Entry[] nodePath;

    public Long2BooleanRBTreeMap() {
        this.allocatePaths();
        this.tree = null;
        this.count = 0;
    }

    private void setActualComparator() {
        this.actualComparator = LongComparators.asLongComparator(this.storedComparator);
    }

    public Long2BooleanRBTreeMap(Comparator<? super Long> c) {
        this();
        this.storedComparator = c;
        this.setActualComparator();
    }

    public Long2BooleanRBTreeMap(Map<? extends Long, ? extends Boolean> m) {
        this();
        this.putAll(m);
    }

    public Long2BooleanRBTreeMap(SortedMap<Long, Boolean> m) {
        this(m.comparator());
        this.putAll((Map<? extends Long, ? extends Boolean>)m);
    }

    public Long2BooleanRBTreeMap(Long2BooleanMap m) {
        this();
        this.putAll(m);
    }

    public Long2BooleanRBTreeMap(Long2BooleanSortedMap m) {
        this(m.comparator());
        this.putAll(m);
    }

    public Long2BooleanRBTreeMap(long[] k, boolean[] v, Comparator<? super Long> c) {
        this(c);
        if (k.length != v.length) {
            throw new IllegalArgumentException("The key array and the value array have different lengths (" + k.length + " and " + v.length + ")");
        }
        for (int i = 0; i < k.length; ++i) {
            this.put(k[i], v[i]);
        }
    }

    public Long2BooleanRBTreeMap(long[] k, boolean[] v) {
        this(k, v, null);
    }

    final int compare(long k1, long k2) {
        return this.actualComparator == null ? Long.compare(k1, k2) : this.actualComparator.compare(k1, k2);
    }

    final Entry findKey(long k) {
        int cmp;
        Entry e = this.tree;
        while (e != null && (cmp = this.compare(k, e.key)) != 0) {
            e = cmp < 0 ? e.left() : e.right();
        }
        return e;
    }

    final Entry locateKey(long k) {
        Entry e = this.tree;
        Entry last = this.tree;
        int cmp = 0;
        while (e != null && (cmp = this.compare(k, e.key)) != 0) {
            last = e;
            e = cmp < 0 ? e.left() : e.right();
        }
        return cmp == 0 ? e : last;
    }

    private void allocatePaths() {
        this.dirPath = new boolean[64];
        this.nodePath = new Entry[64];
    }

    @Override
    public boolean put(long k, boolean v) {
        Entry e = this.add(k);
        boolean oldValue = e.value;
        e.value = v;
        return oldValue;
    }

    private Entry add(long k) {
        Entry e;
        this.modified = false;
        int maxDepth = 0;
        if (this.tree == null) {
            ++this.count;
            this.lastEntry = this.firstEntry = new Entry(k, this.defRetValue);
            this.tree = this.firstEntry;
            e = this.firstEntry;
        } else {
            Entry p = this.tree;
            int i = 0;
            while (true) {
                int cmp;
                if ((cmp = this.compare(k, p.key)) == 0) {
                    while (i-- != 0) {
                        this.nodePath[i] = null;
                    }
                    return p;
                }
                this.nodePath[i] = p;
                this.dirPath[i++] = cmp > 0;
                if (this.dirPath[i++]) {
                    if (p.succ()) {
                        ++this.count;
                        e = new Entry(k, this.defRetValue);
                        if (p.right == null) {
                            this.lastEntry = e;
                        }
                        e.left = p;
                        e.right = p.right;
                        p.right(e);
                        break;
                    }
                    p = p.right;
                    continue;
                }
                if (p.pred()) {
                    ++this.count;
                    e = new Entry(k, this.defRetValue);
                    if (p.left == null) {
                        this.firstEntry = e;
                    }
                    e.right = p;
                    e.left = p.left;
                    p.left(e);
                    break;
                }
                p = p.left;
            }
            this.modified = true;
            maxDepth = i--;
            while (i > 0 && !this.nodePath[i].black()) {
                Entry x;
                Entry y;
                if (!this.dirPath[i - 1]) {
                    y = this.nodePath[i - 1].right;
                    if (!this.nodePath[i - 1].succ() && !y.black()) {
                        this.nodePath[i].black(true);
                        y.black(true);
                        this.nodePath[i - 1].black(false);
                        i -= 2;
                        continue;
                    }
                    if (!this.dirPath[i]) {
                        y = this.nodePath[i];
                    } else {
                        x = this.nodePath[i];
                        y = x.right;
                        x.right = y.left;
                        y.left = x;
                        this.nodePath[i - 1].left = y;
                        if (y.pred()) {
                            y.pred(false);
                            x.succ(y);
                        }
                    }
                    x = this.nodePath[i - 1];
                    x.black(false);
                    y.black(true);
                    x.left = y.right;
                    y.right = x;
                    if (i < 2) {
                        this.tree = y;
                    } else if (this.dirPath[i - 2]) {
                        this.nodePath[i - 2].right = y;
                    } else {
                        this.nodePath[i - 2].left = y;
                    }
                    if (!y.succ()) break;
                    y.succ(false);
                    x.pred(y);
                    break;
                }
                y = this.nodePath[i - 1].left;
                if (!this.nodePath[i - 1].pred() && !y.black()) {
                    this.nodePath[i].black(true);
                    y.black(true);
                    this.nodePath[i - 1].black(false);
                    i -= 2;
                    continue;
                }
                if (this.dirPath[i]) {
                    y = this.nodePath[i];
                } else {
                    x = this.nodePath[i];
                    y = x.left;
                    x.left = y.right;
                    y.right = x;
                    this.nodePath[i - 1].right = y;
                    if (y.succ()) {
                        y.succ(false);
                        x.pred(y);
                    }
                }
                x = this.nodePath[i - 1];
                x.black(false);
                y.black(true);
                x.right = y.left;
                y.left = x;
                if (i < 2) {
                    this.tree = y;
                } else if (this.dirPath[i - 2]) {
                    this.nodePath[i - 2].right = y;
                } else {
                    this.nodePath[i - 2].left = y;
                }
                if (!y.pred()) break;
                y.pred(false);
                x.succ(y);
                break;
            }
        }
        this.tree.black(true);
        while (maxDepth-- != 0) {
            this.nodePath[maxDepth] = null;
        }
        return e;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    @Override
    public boolean remove(long k) {
        block69: {
            block66: {
                block68: {
                    block64: {
                        block67: {
                            block65: {
                                block62: {
                                    block63: {
                                        this.modified = false;
                                        if (this.tree == null) {
                                            return this.defRetValue;
                                        }
                                        p = this.tree;
                                        i = 0;
                                        kk = k;
                                        do lbl-1000:
                                        // 3 sources

                                        {
                                            if ((cmp = this.compare(kk, p.key)) == 0) {
                                                if (p.left != null) break block62;
                                                break block63;
                                            }
                                            this.dirPath[i] = cmp > 0;
                                            this.nodePath[i] = p;
                                            if (!this.dirPath[i++]) continue;
                                            if ((p = p.right()) != null) ** GOTO lbl-1000
                                            while (i-- != 0) {
                                                this.nodePath[i] = null;
                                            }
                                            return this.defRetValue;
                                        } while ((p = p.left()) != null);
                                        while (i-- != 0) {
                                            this.nodePath[i] = null;
                                        }
                                        return this.defRetValue;
                                    }
                                    this.firstEntry = p.next();
                                }
                                if (p.right == null) {
                                    this.lastEntry = p.prev();
                                }
                                if (!p.succ()) break block65;
                                if (p.pred()) {
                                    if (i == 0) {
                                        this.tree = p.left;
                                    } else if (this.dirPath[i - 1]) {
                                        this.nodePath[i - 1].succ(p.right);
                                    } else {
                                        this.nodePath[i - 1].pred(p.left);
                                    }
                                } else {
                                    p.prev().right = p.right;
                                    if (i == 0) {
                                        this.tree = p.left;
                                    } else if (this.dirPath[i - 1]) {
                                        this.nodePath[i - 1].right = p.left;
                                    } else {
                                        this.nodePath[i - 1].left = p.left;
                                    }
                                }
                                break block66;
                            }
                            r = p.right;
                            if (!r.pred()) break block67;
                            r.left = p.left;
                            r.pred(p.pred());
                            if (!r.pred()) {
                                r.prev().right = r;
                            }
                            if (i == 0) {
                                this.tree = r;
                            } else if (this.dirPath[i - 1]) {
                                this.nodePath[i - 1].right = r;
                            } else {
                                this.nodePath[i - 1].left = r;
                            }
                            color = r.black();
                            r.black(p.black());
                            p.black(color);
                            this.dirPath[i] = true;
                            this.nodePath[i++] = r;
                            break block66;
                        }
                        j = i++;
                        while (true) {
                            this.dirPath[i] = false;
                            this.nodePath[i++] = r;
                            s = r.left;
                            if (s.pred()) {
                                this.dirPath[j] = true;
                                this.nodePath[j] = s;
                                if (s.succ()) {
                                    break;
                                }
                                break block64;
                            }
                            r = s;
                        }
                        r.pred(s);
                        break block68;
                    }
                    r.left = s.right;
                }
                s.left = p.left;
                if (!p.pred()) {
                    p.prev().right = s;
                    s.pred(false);
                }
                s.right(p.right);
                color = s.black();
                s.black(p.black());
                p.black(color);
                if (j == 0) {
                    this.tree = s;
                } else if (this.dirPath[j - 1]) {
                    this.nodePath[j - 1].right = s;
                } else {
                    this.nodePath[j - 1].left = s;
                }
            }
            maxDepth = i;
            if (!p.black()) break block69;
            while (i > 0) {
                if (this.dirPath[i - 1] && !this.nodePath[i - 1].succ() || !this.dirPath[i - 1] && !this.nodePath[i - 1].pred()) {
                    v0 = x = this.dirPath[i - 1] != false ? this.nodePath[i - 1].right : this.nodePath[i - 1].left;
                    if (!x.black()) {
                        x.black(true);
                        break;
                    }
                }
                if (this.dirPath[i - 1]) ** GOTO lbl160
                w = this.nodePath[i - 1].right;
                if (!w.black()) {
                    w.black(true);
                    this.nodePath[i - 1].black(false);
                    this.nodePath[i - 1].right = w.left;
                    w.left = this.nodePath[i - 1];
                    if (i < 2) {
                        this.tree = w;
                    } else if (this.dirPath[i - 2]) {
                        this.nodePath[i - 2].right = w;
                    } else {
                        this.nodePath[i - 2].left = w;
                    }
                    this.nodePath[i] = this.nodePath[i - 1];
                    this.dirPath[i] = false;
                    this.nodePath[i - 1] = w;
                    if (maxDepth == i++) {
                        ++maxDepth;
                    }
                    w = this.nodePath[i - 1].right;
                }
                if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
                    w.black(false);
                } else {
                    if (w.succ() || w.right.black()) {
                        y = w.left;
                        y.black(true);
                        w.black(false);
                        w.left = y.right;
                        y.right = w;
                        w = this.nodePath[i - 1].right = y;
                        if (w.succ()) {
                            w.succ(false);
                            w.right.pred(w);
                        }
                    }
                    w.black(this.nodePath[i - 1].black());
                    this.nodePath[i - 1].black(true);
                    w.right.black(true);
                    this.nodePath[i - 1].right = w.left;
                    w.left = this.nodePath[i - 1];
                    if (i < 2) {
                        this.tree = w;
                    } else if (this.dirPath[i - 2]) {
                        this.nodePath[i - 2].right = w;
                    } else {
                        this.nodePath[i - 2].left = w;
                    }
                    if (!w.pred()) break;
                    w.pred(false);
                    this.nodePath[i - 1].succ(w);
                    break;
lbl160:
                    // 1 sources

                    w = this.nodePath[i - 1].left;
                    if (!w.black()) {
                        w.black(true);
                        this.nodePath[i - 1].black(false);
                        this.nodePath[i - 1].left = w.right;
                        w.right = this.nodePath[i - 1];
                        if (i < 2) {
                            this.tree = w;
                        } else if (this.dirPath[i - 2]) {
                            this.nodePath[i - 2].right = w;
                        } else {
                            this.nodePath[i - 2].left = w;
                        }
                        this.nodePath[i] = this.nodePath[i - 1];
                        this.dirPath[i] = true;
                        this.nodePath[i - 1] = w;
                        if (maxDepth == i++) {
                            ++maxDepth;
                        }
                        w = this.nodePath[i - 1].left;
                    }
                    if ((w.pred() || w.left.black()) && (w.succ() || w.right.black())) {
                        w.black(false);
                    } else {
                        if (w.pred() || w.left.black()) {
                            y = w.right;
                            y.black(true);
                            w.black(false);
                            w.right = y.left;
                            y.left = w;
                            w = this.nodePath[i - 1].left = y;
                            if (w.pred()) {
                                w.pred(false);
                                w.left.succ(w);
                            }
                        }
                        w.black(this.nodePath[i - 1].black());
                        this.nodePath[i - 1].black(true);
                        w.left.black(true);
                        this.nodePath[i - 1].left = w.right;
                        w.right = this.nodePath[i - 1];
                        if (i < 2) {
                            this.tree = w;
                        } else if (this.dirPath[i - 2]) {
                            this.nodePath[i - 2].right = w;
                        } else {
                            this.nodePath[i - 2].left = w;
                        }
                        if (!w.succ()) break;
                        w.succ(false);
                        this.nodePath[i - 1].pred(w);
                        break;
                    }
                }
                --i;
            }
            if (this.tree != null) {
                this.tree.black(true);
            }
        }
        this.modified = true;
        --this.count;
        while (maxDepth-- != 0) {
            this.nodePath[maxDepth] = null;
        }
        return p.value;
    }

    @Override
    public boolean containsValue(boolean v) {
        ValueIterator i = new ValueIterator();
        int j = this.count;
        while (j-- != 0) {
            boolean ev = i.nextBoolean();
            if (ev != v) continue;
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        this.count = 0;
        this.tree = null;
        this.entries = null;
        this.values = null;
        this.keys = null;
        this.lastEntry = null;
        this.firstEntry = null;
    }

    @Override
    public boolean containsKey(long k) {
        return this.findKey(k) != null;
    }

    @Override
    public int size() {
        return this.count;
    }

    @Override
    public boolean isEmpty() {
        return this.count == 0;
    }

    @Override
    public boolean get(long k) {
        Entry e = this.findKey(k);
        return e == null ? this.defRetValue : e.value;
    }

    @Override
    public long firstLongKey() {
        if (this.tree == null) {
            throw new NoSuchElementException();
        }
        return this.firstEntry.key;
    }

    @Override
    public long lastLongKey() {
        if (this.tree == null) {
            throw new NoSuchElementException();
        }
        return this.lastEntry.key;
    }

    @Override
    public ObjectSortedSet<Long2BooleanMap.Entry> long2BooleanEntrySet() {
        if (this.entries == null) {
            this.entries = new AbstractObjectSortedSet<Long2BooleanMap.Entry>(){
                final Comparator<? super Long2BooleanMap.Entry> comparator;
                {
                    this.comparator = Long2BooleanRBTreeMap.this.actualComparator == null ? (x, y) -> Long.compare(x.getLongKey(), y.getLongKey()) : (x, y) -> Long2BooleanRBTreeMap.this.actualComparator.compare(x.getLongKey(), y.getLongKey());
                }

                @Override
                public Comparator<? super Long2BooleanMap.Entry> comparator() {
                    return this.comparator;
                }

                @Override
                public ObjectBidirectionalIterator<Long2BooleanMap.Entry> iterator() {
                    return new EntryIterator();
                }

                @Override
                public ObjectBidirectionalIterator<Long2BooleanMap.Entry> iterator(Long2BooleanMap.Entry from) {
                    return new EntryIterator(from.getLongKey());
                }

                @Override
                public boolean contains(Object o) {
                    if (o == null || !(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry e = (Map.Entry)o;
                    if (e.getKey() == null) {
                        return false;
                    }
                    if (!(e.getKey() instanceof Long)) {
                        return false;
                    }
                    if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                        return false;
                    }
                    Entry f = Long2BooleanRBTreeMap.this.findKey((Long)e.getKey());
                    return e.equals(f);
                }

                @Override
                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry)) {
                        return false;
                    }
                    Map.Entry e = (Map.Entry)o;
                    if (e.getKey() == null) {
                        return false;
                    }
                    if (!(e.getKey() instanceof Long)) {
                        return false;
                    }
                    if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                        return false;
                    }
                    Entry f = Long2BooleanRBTreeMap.this.findKey((Long)e.getKey());
                    if (f == null || f.getBooleanValue() != ((Boolean)e.getValue()).booleanValue()) {
                        return false;
                    }
                    Long2BooleanRBTreeMap.this.remove(f.key);
                    return true;
                }

                @Override
                public int size() {
                    return Long2BooleanRBTreeMap.this.count;
                }

                @Override
                public void clear() {
                    Long2BooleanRBTreeMap.this.clear();
                }

                @Override
                public Long2BooleanMap.Entry first() {
                    return Long2BooleanRBTreeMap.this.firstEntry;
                }

                @Override
                public Long2BooleanMap.Entry last() {
                    return Long2BooleanRBTreeMap.this.lastEntry;
                }

                @Override
                public ObjectSortedSet<Long2BooleanMap.Entry> subSet(Long2BooleanMap.Entry from, Long2BooleanMap.Entry to) {
                    return Long2BooleanRBTreeMap.this.subMap(from.getLongKey(), to.getLongKey()).long2BooleanEntrySet();
                }

                @Override
                public ObjectSortedSet<Long2BooleanMap.Entry> headSet(Long2BooleanMap.Entry to) {
                    return Long2BooleanRBTreeMap.this.headMap(to.getLongKey()).long2BooleanEntrySet();
                }

                @Override
                public ObjectSortedSet<Long2BooleanMap.Entry> tailSet(Long2BooleanMap.Entry from) {
                    return Long2BooleanRBTreeMap.this.tailMap(from.getLongKey()).long2BooleanEntrySet();
                }
            };
        }
        return this.entries;
    }

    @Override
    public LongSortedSet keySet() {
        if (this.keys == null) {
            this.keys = new KeySet();
        }
        return this.keys;
    }

    @Override
    public BooleanCollection values() {
        if (this.values == null) {
            this.values = new AbstractBooleanCollection(){

                @Override
                public BooleanIterator iterator() {
                    return new ValueIterator();
                }

                @Override
                public boolean contains(boolean k) {
                    return Long2BooleanRBTreeMap.this.containsValue(k);
                }

                @Override
                public int size() {
                    return Long2BooleanRBTreeMap.this.count;
                }

                @Override
                public void clear() {
                    Long2BooleanRBTreeMap.this.clear();
                }
            };
        }
        return this.values;
    }

    @Override
    public LongComparator comparator() {
        return this.actualComparator;
    }

    @Override
    public Long2BooleanSortedMap headMap(long to) {
        return new Submap(0L, true, to, false);
    }

    @Override
    public Long2BooleanSortedMap tailMap(long from) {
        return new Submap(from, false, 0L, true);
    }

    @Override
    public Long2BooleanSortedMap subMap(long from, long to) {
        return new Submap(from, false, to, false);
    }

    public Long2BooleanRBTreeMap clone() {
        Long2BooleanRBTreeMap c;
        try {
            c = (Long2BooleanRBTreeMap)super.clone();
        }
        catch (CloneNotSupportedException cantHappen) {
            throw new InternalError();
        }
        c.keys = null;
        c.values = null;
        c.entries = null;
        c.allocatePaths();
        if (this.count != 0) {
            Entry rp = new Entry();
            Entry rq = new Entry();
            Entry p = rp;
            rp.left(this.tree);
            Entry q = rq;
            rq.pred(null);
            while (true) {
                Entry e;
                if (!p.pred()) {
                    e = p.left.clone();
                    e.pred(q.left);
                    e.succ(q);
                    q.left(e);
                    p = p.left;
                    q = q.left;
                } else {
                    while (p.succ()) {
                        p = p.right;
                        if (p == null) {
                            q.right = null;
                            c.firstEntry = c.tree = rq.left;
                            while (c.firstEntry.left != null) {
                                c.firstEntry = c.firstEntry.left;
                            }
                            c.lastEntry = c.tree;
                            while (c.lastEntry.right != null) {
                                c.lastEntry = c.lastEntry.right;
                            }
                            return c;
                        }
                        q = q.right;
                    }
                    p = p.right;
                    q = q.right;
                }
                if (p.succ()) continue;
                e = p.right.clone();
                e.succ(q.right);
                e.pred(q);
                q.right(e);
            }
        }
        return c;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        int n = this.count;
        EntryIterator i = new EntryIterator();
        s.defaultWriteObject();
        while (n-- != 0) {
            Entry e = i.nextEntry();
            s.writeLong(e.key);
            s.writeBoolean(e.value);
        }
    }

    private Entry readTree(ObjectInputStream s, int n, Entry pred, Entry succ) throws IOException, ClassNotFoundException {
        if (n == 1) {
            Entry top = new Entry(s.readLong(), s.readBoolean());
            top.pred(pred);
            top.succ(succ);
            top.black(true);
            return top;
        }
        if (n == 2) {
            Entry top = new Entry(s.readLong(), s.readBoolean());
            top.black(true);
            top.right(new Entry(s.readLong(), s.readBoolean()));
            top.right.pred(top);
            top.pred(pred);
            top.right.succ(succ);
            return top;
        }
        int rightN = n / 2;
        int leftN = n - rightN - 1;
        Entry top = new Entry();
        top.left(this.readTree(s, leftN, pred, top));
        top.key = s.readLong();
        top.value = s.readBoolean();
        top.black(true);
        top.right(this.readTree(s, rightN, top, succ));
        if (n + 2 == (n + 2 & -(n + 2))) {
            top.right.black(false);
        }
        return top;
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        this.setActualComparator();
        this.allocatePaths();
        if (this.count != 0) {
            Entry e = this.tree = this.readTree(s, this.count, null, null);
            while (e.left() != null) {
                e = e.left();
            }
            this.firstEntry = e;
            e = this.tree;
            while (e.right() != null) {
                e = e.right();
            }
            this.lastEntry = e;
        }
    }

    private static final class Entry
    extends AbstractLong2BooleanMap.BasicEntry
    implements Cloneable {
        private static final int BLACK_MASK = 1;
        private static final int SUCC_MASK = Integer.MIN_VALUE;
        private static final int PRED_MASK = 0x40000000;
        Entry left;
        Entry right;
        int info;

        Entry() {
            super(0L, false);
        }

        Entry(long k, boolean v) {
            super(k, v);
            this.info = -1073741824;
        }

        Entry left() {
            return (this.info & 0x40000000) != 0 ? null : this.left;
        }

        Entry right() {
            return (this.info & Integer.MIN_VALUE) != 0 ? null : this.right;
        }

        boolean pred() {
            return (this.info & 0x40000000) != 0;
        }

        boolean succ() {
            return (this.info & Integer.MIN_VALUE) != 0;
        }

        void pred(boolean pred) {
            this.info = pred ? (this.info |= 0x40000000) : (this.info &= 0xBFFFFFFF);
        }

        void succ(boolean succ) {
            this.info = succ ? (this.info |= Integer.MIN_VALUE) : (this.info &= Integer.MAX_VALUE);
        }

        void pred(Entry pred) {
            this.info |= 0x40000000;
            this.left = pred;
        }

        void succ(Entry succ) {
            this.info |= Integer.MIN_VALUE;
            this.right = succ;
        }

        void left(Entry left) {
            this.info &= 0xBFFFFFFF;
            this.left = left;
        }

        void right(Entry right) {
            this.info &= Integer.MAX_VALUE;
            this.right = right;
        }

        boolean black() {
            return (this.info & 1) != 0;
        }

        void black(boolean black) {
            this.info = black ? (this.info |= 1) : (this.info &= 0xFFFFFFFE);
        }

        Entry next() {
            Entry next = this.right;
            if ((this.info & Integer.MIN_VALUE) == 0) {
                while ((next.info & 0x40000000) == 0) {
                    next = next.left;
                }
            }
            return next;
        }

        Entry prev() {
            Entry prev = this.left;
            if ((this.info & 0x40000000) == 0) {
                while ((prev.info & Integer.MIN_VALUE) == 0) {
                    prev = prev.right;
                }
            }
            return prev;
        }

        @Override
        public boolean setValue(boolean value) {
            boolean oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public Entry clone() {
            Entry c;
            try {
                c = (Entry)super.clone();
            }
            catch (CloneNotSupportedException cantHappen) {
                throw new InternalError();
            }
            c.key = this.key;
            c.value = this.value;
            c.info = this.info;
            return c;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry e = (Map.Entry)o;
            return this.key == (Long)e.getKey() && this.value == (Boolean)e.getValue();
        }

        @Override
        public int hashCode() {
            return HashCommon.long2int(this.key) ^ (this.value ? 1231 : 1237);
        }

        @Override
        public String toString() {
            return this.key + "=>" + this.value;
        }
    }

    private final class ValueIterator
    extends TreeIterator
    implements BooleanListIterator {
        private ValueIterator() {
        }

        @Override
        public boolean nextBoolean() {
            return this.nextEntry().value;
        }

        @Override
        public boolean previousBoolean() {
            return this.previousEntry().value;
        }
    }

    private class KeySet
    extends AbstractLong2BooleanSortedMap.KeySet {
        private KeySet() {
        }

        @Override
        public LongBidirectionalIterator iterator() {
            return new KeyIterator();
        }

        @Override
        public LongBidirectionalIterator iterator(long from) {
            return new KeyIterator(from);
        }
    }

    private final class Submap
    extends AbstractLong2BooleanSortedMap
    implements Serializable {
        private static final long serialVersionUID = -7046029254386353129L;
        long from;
        long to;
        boolean bottom;
        boolean top;
        protected transient ObjectSortedSet<Long2BooleanMap.Entry> entries;
        protected transient LongSortedSet keys;
        protected transient BooleanCollection values;

        public Submap(long from, boolean bottom, long to, boolean top) {
            if (!bottom && !top && Long2BooleanRBTreeMap.this.compare(from, to) > 0) {
                throw new IllegalArgumentException("Start key (" + from + ") is larger than end key (" + to + ")");
            }
            this.from = from;
            this.bottom = bottom;
            this.to = to;
            this.top = top;
            this.defRetValue = Long2BooleanRBTreeMap.this.defRetValue;
        }

        @Override
        public void clear() {
            SubmapIterator i = new SubmapIterator();
            while (i.hasNext()) {
                i.nextEntry();
                i.remove();
            }
        }

        final boolean in(long k) {
            return !(!this.bottom && Long2BooleanRBTreeMap.this.compare(k, this.from) < 0 || !this.top && Long2BooleanRBTreeMap.this.compare(k, this.to) >= 0);
        }

        @Override
        public ObjectSortedSet<Long2BooleanMap.Entry> long2BooleanEntrySet() {
            if (this.entries == null) {
                this.entries = new AbstractObjectSortedSet<Long2BooleanMap.Entry>(){

                    @Override
                    public ObjectBidirectionalIterator<Long2BooleanMap.Entry> iterator() {
                        return new SubmapEntryIterator();
                    }

                    @Override
                    public ObjectBidirectionalIterator<Long2BooleanMap.Entry> iterator(Long2BooleanMap.Entry from) {
                        return new SubmapEntryIterator(from.getLongKey());
                    }

                    @Override
                    public Comparator<? super Long2BooleanMap.Entry> comparator() {
                        return Long2BooleanRBTreeMap.this.long2BooleanEntrySet().comparator();
                    }

                    @Override
                    public boolean contains(Object o) {
                        if (!(o instanceof Map.Entry)) {
                            return false;
                        }
                        Map.Entry e = (Map.Entry)o;
                        if (e.getKey() == null || !(e.getKey() instanceof Long)) {
                            return false;
                        }
                        if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                            return false;
                        }
                        Entry f = Long2BooleanRBTreeMap.this.findKey((Long)e.getKey());
                        return f != null && Submap.this.in(f.key) && e.equals(f);
                    }

                    @Override
                    public boolean remove(Object o) {
                        if (!(o instanceof Map.Entry)) {
                            return false;
                        }
                        Map.Entry e = (Map.Entry)o;
                        if (e.getKey() == null || !(e.getKey() instanceof Long)) {
                            return false;
                        }
                        if (e.getValue() == null || !(e.getValue() instanceof Boolean)) {
                            return false;
                        }
                        Entry f = Long2BooleanRBTreeMap.this.findKey((Long)e.getKey());
                        if (f != null && Submap.this.in(f.key)) {
                            Submap.this.remove(f.key);
                        }
                        return f != null;
                    }

                    @Override
                    public int size() {
                        int c = 0;
                        ObjectIterator i = this.iterator();
                        while (i.hasNext()) {
                            ++c;
                            i.next();
                        }
                        return c;
                    }

                    @Override
                    public boolean isEmpty() {
                        return !new SubmapIterator().hasNext();
                    }

                    @Override
                    public void clear() {
                        Submap.this.clear();
                    }

                    @Override
                    public Long2BooleanMap.Entry first() {
                        return Submap.this.firstEntry();
                    }

                    @Override
                    public Long2BooleanMap.Entry last() {
                        return Submap.this.lastEntry();
                    }

                    @Override
                    public ObjectSortedSet<Long2BooleanMap.Entry> subSet(Long2BooleanMap.Entry from, Long2BooleanMap.Entry to) {
                        return Submap.this.subMap(from.getLongKey(), to.getLongKey()).long2BooleanEntrySet();
                    }

                    @Override
                    public ObjectSortedSet<Long2BooleanMap.Entry> headSet(Long2BooleanMap.Entry to) {
                        return Submap.this.headMap(to.getLongKey()).long2BooleanEntrySet();
                    }

                    @Override
                    public ObjectSortedSet<Long2BooleanMap.Entry> tailSet(Long2BooleanMap.Entry from) {
                        return Submap.this.tailMap(from.getLongKey()).long2BooleanEntrySet();
                    }
                };
            }
            return this.entries;
        }

        @Override
        public LongSortedSet keySet() {
            if (this.keys == null) {
                this.keys = new KeySet();
            }
            return this.keys;
        }

        @Override
        public BooleanCollection values() {
            if (this.values == null) {
                this.values = new AbstractBooleanCollection(){

                    @Override
                    public BooleanIterator iterator() {
                        return new SubmapValueIterator();
                    }

                    @Override
                    public boolean contains(boolean k) {
                        return Submap.this.containsValue(k);
                    }

                    @Override
                    public int size() {
                        return Submap.this.size();
                    }

                    @Override
                    public void clear() {
                        Submap.this.clear();
                    }
                };
            }
            return this.values;
        }

        @Override
        public boolean containsKey(long k) {
            return this.in(k) && Long2BooleanRBTreeMap.this.containsKey(k);
        }

        @Override
        public boolean containsValue(boolean v) {
            SubmapIterator i = new SubmapIterator();
            while (i.hasNext()) {
                boolean ev = i.nextEntry().value;
                if (ev != v) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean get(long k) {
            Entry e;
            long kk = k;
            return this.in(kk) && (e = Long2BooleanRBTreeMap.this.findKey(kk)) != null ? e.value : this.defRetValue;
        }

        @Override
        public boolean put(long k, boolean v) {
            Long2BooleanRBTreeMap.this.modified = false;
            if (!this.in(k)) {
                throw new IllegalArgumentException("Key (" + k + ") out of range [" + (this.bottom ? "-" : String.valueOf(this.from)) + ", " + (this.top ? "-" : String.valueOf(this.to)) + ")");
            }
            boolean oldValue = Long2BooleanRBTreeMap.this.put(k, v);
            return Long2BooleanRBTreeMap.this.modified ? this.defRetValue : oldValue;
        }

        @Override
        public boolean remove(long k) {
            Long2BooleanRBTreeMap.this.modified = false;
            if (!this.in(k)) {
                return this.defRetValue;
            }
            boolean oldValue = Long2BooleanRBTreeMap.this.remove(k);
            return Long2BooleanRBTreeMap.this.modified ? oldValue : this.defRetValue;
        }

        @Override
        public int size() {
            SubmapIterator i = new SubmapIterator();
            int n = 0;
            while (i.hasNext()) {
                ++n;
                i.nextEntry();
            }
            return n;
        }

        @Override
        public boolean isEmpty() {
            return !new SubmapIterator().hasNext();
        }

        @Override
        public LongComparator comparator() {
            return Long2BooleanRBTreeMap.this.actualComparator;
        }

        @Override
        public Long2BooleanSortedMap headMap(long to) {
            if (this.top) {
                return new Submap(this.from, this.bottom, to, false);
            }
            return Long2BooleanRBTreeMap.this.compare(to, this.to) < 0 ? new Submap(this.from, this.bottom, to, false) : this;
        }

        @Override
        public Long2BooleanSortedMap tailMap(long from) {
            if (this.bottom) {
                return new Submap(from, false, this.to, this.top);
            }
            return Long2BooleanRBTreeMap.this.compare(from, this.from) > 0 ? new Submap(from, false, this.to, this.top) : this;
        }

        @Override
        public Long2BooleanSortedMap subMap(long from, long to) {
            if (this.top && this.bottom) {
                return new Submap(from, false, to, false);
            }
            if (!this.top) {
                long l = to = Long2BooleanRBTreeMap.this.compare(to, this.to) < 0 ? to : this.to;
            }
            if (!this.bottom) {
                long l = from = Long2BooleanRBTreeMap.this.compare(from, this.from) > 0 ? from : this.from;
            }
            if (!this.top && !this.bottom && from == this.from && to == this.to) {
                return this;
            }
            return new Submap(from, false, to, false);
        }

        public Entry firstEntry() {
            Entry e;
            if (Long2BooleanRBTreeMap.this.tree == null) {
                return null;
            }
            if (this.bottom) {
                e = Long2BooleanRBTreeMap.this.firstEntry;
            } else {
                e = Long2BooleanRBTreeMap.this.locateKey(this.from);
                if (Long2BooleanRBTreeMap.this.compare(e.key, this.from) < 0) {
                    e = e.next();
                }
            }
            if (e == null || !this.top && Long2BooleanRBTreeMap.this.compare(e.key, this.to) >= 0) {
                return null;
            }
            return e;
        }

        public Entry lastEntry() {
            Entry e;
            if (Long2BooleanRBTreeMap.this.tree == null) {
                return null;
            }
            if (this.top) {
                e = Long2BooleanRBTreeMap.this.lastEntry;
            } else {
                e = Long2BooleanRBTreeMap.this.locateKey(this.to);
                if (Long2BooleanRBTreeMap.this.compare(e.key, this.to) >= 0) {
                    e = e.prev();
                }
            }
            if (e == null || !this.bottom && Long2BooleanRBTreeMap.this.compare(e.key, this.from) < 0) {
                return null;
            }
            return e;
        }

        @Override
        public long firstLongKey() {
            Entry e = this.firstEntry();
            if (e == null) {
                throw new NoSuchElementException();
            }
            return e.key;
        }

        @Override
        public long lastLongKey() {
            Entry e = this.lastEntry();
            if (e == null) {
                throw new NoSuchElementException();
            }
            return e.key;
        }

        private class SubmapIterator
        extends TreeIterator {
            SubmapIterator() {
                this.next = Submap.this.firstEntry();
            }

            /*
             * Enabled aggressive block sorting
             */
            SubmapIterator(long k) {
                this();
                if (this.next == null) return;
                if (!submap.bottom && submap.Long2BooleanRBTreeMap.this.compare(k, this.next.key) < 0) {
                    this.prev = null;
                    return;
                }
                if (!submap.top) {
                    this.prev = submap.lastEntry();
                    if (submap.Long2BooleanRBTreeMap.this.compare(k, this.prev.key) >= 0) {
                        this.next = null;
                        return;
                    }
                }
                this.next = submap.Long2BooleanRBTreeMap.this.locateKey(k);
                if (submap.Long2BooleanRBTreeMap.this.compare(this.next.key, k) <= 0) {
                    this.prev = this.next;
                    this.next = this.next.next();
                    return;
                }
                this.prev = this.next.prev();
            }

            @Override
            void updatePrevious() {
                this.prev = this.prev.prev();
                if (!Submap.this.bottom && this.prev != null && Long2BooleanRBTreeMap.this.compare(this.prev.key, Submap.this.from) < 0) {
                    this.prev = null;
                }
            }

            @Override
            void updateNext() {
                this.next = this.next.next();
                if (!Submap.this.top && this.next != null && Long2BooleanRBTreeMap.this.compare(this.next.key, Submap.this.to) >= 0) {
                    this.next = null;
                }
            }
        }

        private class KeySet
        extends AbstractLong2BooleanSortedMap.KeySet {
            private KeySet() {
            }

            @Override
            public LongBidirectionalIterator iterator() {
                return new SubmapKeyIterator();
            }

            @Override
            public LongBidirectionalIterator iterator(long from) {
                return new SubmapKeyIterator(from);
            }
        }

        private final class SubmapValueIterator
        extends SubmapIterator
        implements BooleanListIterator {
            private SubmapValueIterator() {
            }

            @Override
            public boolean nextBoolean() {
                return this.nextEntry().value;
            }

            @Override
            public boolean previousBoolean() {
                return this.previousEntry().value;
            }
        }

        private final class SubmapKeyIterator
        extends SubmapIterator
        implements LongListIterator {
            public SubmapKeyIterator() {
            }

            public SubmapKeyIterator(long from) {
                super(from);
            }

            @Override
            public long nextLong() {
                return this.nextEntry().key;
            }

            @Override
            public long previousLong() {
                return this.previousEntry().key;
            }
        }

        private class SubmapEntryIterator
        extends SubmapIterator
        implements ObjectListIterator<Long2BooleanMap.Entry> {
            SubmapEntryIterator() {
            }

            SubmapEntryIterator(long k) {
                super(k);
            }

            @Override
            public Long2BooleanMap.Entry next() {
                return this.nextEntry();
            }

            @Override
            public Long2BooleanMap.Entry previous() {
                return this.previousEntry();
            }
        }
    }

    private class EntryIterator
    extends TreeIterator
    implements ObjectListIterator<Long2BooleanMap.Entry> {
        EntryIterator() {
        }

        EntryIterator(long k) {
            super(k);
        }

        @Override
        public Long2BooleanMap.Entry next() {
            return this.nextEntry();
        }

        @Override
        public Long2BooleanMap.Entry previous() {
            return this.previousEntry();
        }
    }

    private final class KeyIterator
    extends TreeIterator
    implements LongListIterator {
        public KeyIterator() {
        }

        public KeyIterator(long k) {
            super(k);
        }

        @Override
        public long nextLong() {
            return this.nextEntry().key;
        }

        @Override
        public long previousLong() {
            return this.previousEntry().key;
        }
    }

    private class TreeIterator {
        Entry prev;
        Entry next;
        Entry curr;
        int index = 0;

        TreeIterator() {
            this.next = Long2BooleanRBTreeMap.this.firstEntry;
        }

        TreeIterator(long k) {
            this.next = Long2BooleanRBTreeMap.this.locateKey(k);
            if (this.next != null) {
                if (Long2BooleanRBTreeMap.this.compare(this.next.key, k) <= 0) {
                    this.prev = this.next;
                    this.next = this.next.next();
                } else {
                    this.prev = this.next.prev();
                }
            }
        }

        public boolean hasNext() {
            return this.next != null;
        }

        public boolean hasPrevious() {
            return this.prev != null;
        }

        void updateNext() {
            this.next = this.next.next();
        }

        Entry nextEntry() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.curr = this.prev = this.next;
            ++this.index;
            this.updateNext();
            return this.curr;
        }

        void updatePrevious() {
            this.prev = this.prev.prev();
        }

        Entry previousEntry() {
            if (!this.hasPrevious()) {
                throw new NoSuchElementException();
            }
            this.curr = this.next = this.prev;
            --this.index;
            this.updatePrevious();
            return this.curr;
        }

        public int nextIndex() {
            return this.index;
        }

        public int previousIndex() {
            return this.index - 1;
        }

        public void remove() {
            if (this.curr == null) {
                throw new IllegalStateException();
            }
            if (this.curr == this.prev) {
                --this.index;
            }
            this.next = this.prev = this.curr;
            this.updatePrevious();
            this.updateNext();
            Long2BooleanRBTreeMap.this.remove(this.curr.key);
            this.curr = null;
        }

        public int skip(int n) {
            int i = n;
            while (i-- != 0 && this.hasNext()) {
                this.nextEntry();
            }
            return n - i - 1;
        }

        public int back(int n) {
            int i = n;
            while (i-- != 0 && this.hasPrevious()) {
                this.previousEntry();
            }
            return n - i - 1;
        }
    }
}

