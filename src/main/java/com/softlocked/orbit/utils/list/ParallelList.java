package com.softlocked.orbit.utils.list;

import java.util.ArrayList;

/**
 * A wrapper for a list that allows for parallel iteration
 */
public class ParallelList extends ArrayList<Object> {
    private int allocatedThreads;

    public ParallelList(int allocatedThreads) {
        this.allocatedThreads = allocatedThreads;
    }

    public ParallelList() {
        this.allocatedThreads = 4;
    }

    public int getAllocatedThreads() {
        return allocatedThreads;
    }
}
