package com.rebelsoft;

/**
 * Provides listener notification methods when a tailed log file is updated
 */
public interface FileTailerListener {
    /**
     * A new line has been added to the tailed log file
     *
     * @param line        The new line that has been added to the tailed log file
     * @param filePointer
     */
    public void newLogFileLine(String line, long filePointer);

    public void stop();
}
