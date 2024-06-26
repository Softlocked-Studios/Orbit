package com.softlocked.orbit.utils.list;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A list implementation that is specifically designed for iterating over numbers from n to m, with a step size.
 * This is not an actual list, but rather a virtual list that generates the numbers on the fly.
 * Very useful for iterating over numbers in a for-each loop, but elements cannot be added, removed or modified.
 */
public class CountingList extends AbstractList<Integer> {
    int start;
    int end;
    int step;

    public CountingList(int start, int end, int step) {
        this.start = start;
        this.end = end;
        this.step = step;
    }

    public CountingList(int start, int end) {
        this.start = start;
        this.end = end;
        this.step = 1;
    }

    public CountingList(int end) {
        this.start = 0;
        this.end = end;
        this.step = 1;
    }

    @Override
    public int size() {
        return (end - start) / step;
    }

    @Override
    public boolean isEmpty() {
        return start == end;
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Number) {
            int i = ((Number) o).intValue();
            return i >= start && i < end && (i - start) % step == 0;
        }
        return false;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            int current = start;

            @Override
            public boolean hasNext() {
                return current < end;
            }

            @Override
            public Integer next() {
                current += step;
                return current - step;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] array = new Object[size()];
        for (int i = 0; i < size(); i++) {
            array[i] = start + i * step;
        }
        return array;
    }

    @Override
    public boolean add(Integer integer) {
        throw new UnsupportedOperationException("Cannot add to a CountingList");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Cannot remove from a CountingList");
    }

    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(java.util.Collection<? extends Integer> c) {
        throw new UnsupportedOperationException("Cannot add to a CountingList");
    }

    @Override
    public boolean addAll(int index, java.util.Collection<? extends Integer> c) {
        throw new UnsupportedOperationException("Cannot add to a CountingList");
    }

    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove from a CountingList");
    }

    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        throw new UnsupportedOperationException("Cannot remove from a CountingList");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Cannot clear a CountingList");
    }

    @Override
    public Integer get(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size());
        }
        return start + index * step;
    }

    @Override
    public Integer set(int index, Integer element) {
        throw new UnsupportedOperationException("Cannot set in a CountingList");
    }

    @Override
    public void add(int index, Integer element) {
        throw new UnsupportedOperationException("Cannot add to a CountingList");
    }

    @Override
    public Integer remove(int index) {
        throw new UnsupportedOperationException("Cannot remove from a CountingList");
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Number) {
            int i = ((Number) o).intValue();
            if (i >= start && i < end && (i - start) % step == 0) {
                return (i - start) / step;
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return indexOf(o);
    }

    @Override
    public ListIterator<Integer> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<Integer> listIterator(int index) {
        return new ListIterator<>() {
            int current = start + index * step;

            @Override
            public boolean hasNext() {
                return current < end;
            }

            @Override
            public Integer next() {
                current += step;
                return current - step;
            }

            @Override
            public boolean hasPrevious() {
                return current > start;
            }

            @Override
            public Integer previous() {
                current -= step;
                return current;
            }

            @Override
            public int nextIndex() {
                return (current - start) / step;
            }

            @Override
            public int previousIndex() {
                return (current - start) / step - 1;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Cannot remove from a CountingList");
            }

            @Override
            public void set(Integer integer) {
                throw new UnsupportedOperationException("Cannot set in a CountingList");
            }

            @Override
            public void add(Integer integer) {
                throw new UnsupportedOperationException("Cannot add to a CountingList");
            }
        };
    }

    @Override
    public List<Integer> subList(int fromIndex, int toIndex) {
        if (fromIndex < 0 || toIndex > size() || fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex: " + fromIndex + ", toIndex: " + toIndex + ", Size: " + size());
        }
        return new CountingList(start + fromIndex * step, start + toIndex * step, step);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size(); i++) {
            sb.append(start + i * step);
            if (i < size() - 1) {
                sb.append(", ");
            }
        }

        return sb.append("]").toString();
    }
}
