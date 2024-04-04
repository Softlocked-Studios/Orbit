package com.softlocked.orbit.utils;

import com.softlocked.orbit.interpreter.function.coroutine.Coroutine;

import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * A list implementation that is designed for iterating over values yielded by a coroutine.
 */
public class CoroutineList extends AbstractList<Object> {
    private Coroutine coroutine;

    public CoroutineList(Coroutine coroutine) {
        this.coroutine = coroutine;
    }

    @Override
    public int size() {
        int size = 0;

        while (!coroutine.isFinished()) {
            coroutine.resume();
            size++;
        }

        coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());

        return size;
    }

    @Override
    public Object get(int index) {
        while (!coroutine.isFinished() && index > 0) {
            coroutine.resume();
            index--;
        }

        Object value = coroutine.resume();

        coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());

        return value;
    }

    @Override
    public boolean isEmpty() {
        return coroutine.isFinished();
    }

    @Override
    public boolean contains(Object o) {
        Object value = coroutine.resume();

        while (!coroutine.isFinished() && !value.equals(o)) {
            value = coroutine.resume();
        }

        coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());

        return value.equals(o);
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                boolean finished = coroutine.isFinished();

                if (finished) {
                    coroutine = (Coroutine) coroutine.getFunction().call(coroutine.getContext(), coroutine.getArgs());
                }

                return !finished;
            }

            @Override
            public Object next() {
                return coroutine.resume();
            }
        };
    }
}
