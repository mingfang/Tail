package com.rebelsoft;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * A log file tailer is designed to monitor a log file and send notifications
 * when new lines are added to the log file. This class has a notification
 * strategy similar to a SAX parser: implement the LogFileTailerListener interface,
 * create a LogFileTailer to tail your log file, add yourself as a listener, and
 * start the LogFileTailer. It is your job to interpret the results, build meaningful
 * sets of data, etc. This tailer simply fires notifications containing new log file lines,
 * one at a time.
 */
public class FileTailer {
    /**
     * How frequently to check for file changes; defaults to 5 seconds
     */
    private long sampleInterval = 5000;

    /**
     * The log file to tail
     */
    private File logfile;

    /**
     * Defines whether the log file tailer should include the entire contents
     * of the exising log file or tail from the end of the file when the tailer starts
     */
    private boolean startAtBeginning = false;

    /**
     * Is the tailer currently tailing?
     */
    private volatile boolean tailing = false;
    private FileTailerListener listener;

    /**
     * Set of listeners
     */

    /**
     * Creates a new log file tailer that tails an existing file and checks the file for
     * updates every 5000ms
     */
    public FileTailer(File file) {
        this.logfile = file;
    }

    /**
     * Creates a new log file tailer
     *
     * @param file             The file to tail
     * @param sampleInterval   How often to check for updates to the log file (default = 5000ms)
     * @param startAtBeginning Should the tailer simply tail or should it process the entire
     *                         file and continue tailing (true) or simply start tailing from the
     *                         end of the file
     */
    public FileTailer(File file, long sampleInterval, boolean startAtBeginning) {
        this.logfile = file;
        this.sampleInterval = sampleInterval;
        this.startAtBeginning = startAtBeginning;
    }

    public void setListener(FileTailerListener listener) {
        this.listener = listener;
    }

    protected void fireNewLogFileLine(String line, long filePointer) {
        listener.newLogFileLine(line, filePointer);
    }

    public void stop() {
        this.tailing = false;
    }

    public void start() {
        Thread thread = new Thread(
                new Runnable() {
                    public void run() {
                        FileTailer.this.run();
                    }
                }
                , "Tail " + logfile.getName());
        thread.start();
    }

    private void run() {
        // The file pointer keeps track of where we are in the file
        long filePointer = 0;

        // Determine start point
        if (this.startAtBeginning) {
            filePointer = 0;
        } else {
            filePointer = this.logfile.length();
        }

        RandomAccessFile file = null;
        try {
            // Start tailing
            this.tailing = true;
            file = new RandomAccessFile(logfile, "r");
            while (this.tailing) {
                // Compare the length of the file to the file pointer
                long fileLength = this.logfile.length();
                if (fileLength < filePointer) {
                    // Log file must have been rotated or deleted;
                    // reopen the file and reset the file pointer
                    file = new RandomAccessFile(logfile, "r");
                    filePointer = 0;
                }

                if (fileLength > filePointer) {
                    // There is data to read
                    file.seek(filePointer);
                    String line = file.readLine();
                    filePointer = file.getFilePointer();
                    while (line != null) {
                        this.fireNewLogFileLine(line, filePointer);
                        line = file.readLine();
                    }
                }

                // Sleep for the specified interval
                if (sampleInterval > 0.0) {
                    Thread.sleep(this.sampleInterval);
                } else {
                    tailing = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                listener.stop();
            } catch (Exception all) {

            }

            // Close the file that we are tailing
            if (file == null) {
                try {
                    file.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
