package com.rebelsoft;

import java.io.File;

/**
 * Implements console-based log file tailing, or more specifically, tail following:
 * it is somewhat equivalent to the unix command "tail -f"
 */
public class Tail implements FileTailerListener {
    /**
     * The log file tailer
     */
    private FileTailer tailer;

    /**
     * Creates a new Tail instance to follow the specified file
     */
    public Tail(String filename) {
        tailer = new FileTailer(new File(filename), 1000, false);
        tailer.setListener(this);
        tailer.start();
    }

    /**
     * A new line has been added to the tailed log file
     *
     * @param line        The new line that has been added to the tailed log file
     * @param filePointer
     */
    public void newLogFileLine(String line, long filePointer) {
        System.out.println(line);
    }

    public void stop() {
    }

    /**
     * Command-line launcher
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: Tail <filename>");
            System.exit(0);
        }
        Tail tail = new Tail(args[0]);
    }
}
