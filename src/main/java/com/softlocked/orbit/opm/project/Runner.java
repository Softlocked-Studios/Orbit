package com.softlocked.orbit.opm.project;

/**
 * Generic interface for running a project. Used in both modules and REPL mode.
 */
public interface Runner {
    default void run() {}

    default void run(String input) {}
}
