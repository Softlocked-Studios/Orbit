package com.softlocked.orbit.utils;

import java.util.*;

/**
 * A sorted map implementation that uses simple binary search to find elements.
 */
public class BinaryMap<K extends Comparable<K>, V> implements Map<K, V> {
    private final List<K> keys;
    private final List<V> values;

    public BinaryMap() {
        keys = new ArrayList<>();
        values = new ArrayList<>();
    }

    public BinaryMap(int initialCapacity) {
        keys = new ArrayList<>(initialCapacity);
        values = new ArrayList<>(initialCapacity);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        int index = Collections.binarySearch(keys, (K) key);

        if (index < 0) {
            return null;
        }

        return values.get(index);
    }

    public V getByIndex(int index) {
        if (index < 0 || index >= keys.size()) {
            return null;
        }

        return values.get(index);
    }

    @Override
    public V put(K key, V value) {
        if (keys.contains(key)) {
            int index = keys.indexOf(key);
            V oldValue = values.get(index);
            values.set(index, value);
            return oldValue;
        }

        int index = Collections.binarySearch(keys, key);

        if (index < 0) {
            index = -index - 1;
        }

        keys.add(index, key);
        values.add(index, value);

        return null;
    }

    public void putAtIndex(int index, K key, V value) {
        if (index >= keys.size()) {
            keys.add(key);
            values.add(value);
            return;
        }

        if(key != null) {
            keys.set(index, key);
        }

        values.set(index, value);
    }

    public int getIndexOf(K key) {
        return keys.indexOf(key);
    }

    @Override
    public V remove(Object key) {
        int index = Collections.binarySearch(keys, (K) key);

        if (index < 0) {
            return null;
        }

        keys.remove(index);
        return values.remove(index);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (Entry<? extends K, ? extends V> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        keys.clear();
        values.clear();
    }

    @Override
    public Set<K> keySet() {
        return new HashSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return new ArrayList<>(values);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = new HashSet<>();

        for (int i = 0; i < keys.size(); i++) {
            entrySet.add(new AbstractMap.SimpleEntry<>(keys.get(i), values.get(i)));
        }

        return entrySet;
    }
}