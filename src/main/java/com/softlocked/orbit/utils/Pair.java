package com.softlocked.orbit.utils;

import java.io.Serializable;

/**
 * A simple class to hold a pair of objects
 * @param <T> First object
 * @param <U> Second object
 */
public class Pair<T, U> implements Serializable, Comparable<Pair<T, U>> {
    public T first;
    public U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Pair<?, ?> pair = (Pair<?, ?>) obj;
        return first.equals(pair.first) && second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Pair<T, U> o) {
        // Compare first to first and second to second
        if (first instanceof Comparable && second instanceof Comparable) {
            int firstComparison = ((Comparable<T>) first).compareTo(o.first);
            if (firstComparison != 0) {
                return firstComparison;
            }
            return ((Comparable<U>) second).compareTo(o.second);
        }

        return 0;
    }
}
