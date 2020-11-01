package com.firenio.collection;

import com.carrotsearch.hppc.IntObjectScatterMap;
import com.carrotsearch.hppc.predicates.IntObjectPredicate;
import com.carrotsearch.hppc.procedures.IntObjectProcedure;

/**
 * @author: wangkai
 **/
public class IntObjectMap<V> extends IntObjectScatterMap<V> {

    private int scanIndex;
    private int scanSize;
    private int key;
    private V   value;

    public IntObjectMap() {
        this(8);
    }

    public IntObjectMap(int size) {
        super(size);
    }

    @Override
    public <T extends IntObjectPredicate<? super V>> T forEach(T predicate) {
        final int[] keys     = this.keys;
        final V[]   values   = (V[]) this.values;
        final int   size     = size();
        int         consumed = 0;
        if (hasEmptyKey) {
            if (!predicate.apply(0, values[mask + 1])) {
                return predicate;
            }
            consumed++;
        }

        for (int slot = 0, max = this.mask; consumed < size && slot <= max; slot++) {
            if (!((keys[slot]) == 0)) {
                if (!predicate.apply(keys[slot], values[slot])) {
                    break;
                }
                consumed++;
            }
        }

        return predicate;
    }

    @Override
    public <T extends IntObjectProcedure<? super V>> T forEach(T procedure) {
        final int[] keys     = this.keys;
        final V[]   values   = (V[]) this.values;
        final int   size     = size();
        int         consumed = 0;
        if (hasEmptyKey) {
            consumed++;
            procedure.apply(0, values[mask + 1]);
        }

        for (int slot = 0, max = this.mask; consumed < size && slot <= max; slot++) {
            if (!((keys[slot]) == 0)) {
                procedure.apply(keys[slot], values[slot]);
                consumed++;
            }
        }

        return procedure;
    }

    public void scan() {
        this.scanIndex = 0;
        this.scanSize = 0;
    }

    public boolean hasNext() {
        if (scanSize < assigned) {
            for (int slot = scanIndex, max = this.mask; slot <= max; slot++) {
                if (!((keys[slot]) == 0)) {
                    this.scanIndex = slot + 1;
                    this.scanSize++;
                    this.key = keys[slot];
                    this.value = (V) values[slot];
                    return true;
                }
            }
        }
        if (scanSize == assigned && hasEmptyKey) {
            this.scanSize++;
            this.key = 0;
            this.value = (V) values[mask + 1];
            return true;
        }
        return false;
    }

    public int getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
