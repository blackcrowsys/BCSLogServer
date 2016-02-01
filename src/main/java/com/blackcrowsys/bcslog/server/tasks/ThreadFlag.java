/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "flags are thread safe due to volatile members")
public class ThreadFlag {

    private static final ThreadFlag instance = new ThreadFlag();

    /**
     * Flag to whether the fifo reader should continue to read fifo.
     */
    private volatile boolean carryOnReading;

    /**
     * Flag to indicate whether the task should continue to show data on screen.
     */
    private volatile boolean carryOnScreenDump;

    /**
     * Flag to indicate whether the task should continue to write data to file.
     */
    private volatile boolean carryOnWriting;

    /**
     * Flag to indicate whether the task should continue to update database.
     */
    private volatile boolean carryOnDbOperation;

    /**
     * Flag to indicate whether the database connection is opened or not.
     */
    private volatile boolean dbConnectionOpen;

    /**
     * Flag to indicate whether the queue polling task should wait a bit for
     * some updates or checks. Also, set to true by the polling task if there is
     * an issue with writing to output.
     */
    private volatile boolean waitForPoll;

    /**
     * Flag that indicates whether the queue has data or not.
     */
    private volatile boolean hasData;

    /**
     * Flag to indicate that we should exit due to unrecoverable error.
     */
    private volatile boolean exit;

    /**
     * Default settings for thread flag.
     */
    private ThreadFlag() {
        carryOnReading = true;
        carryOnScreenDump = false;
        carryOnWriting = false;
        carryOnDbOperation = false;
        dbConnectionOpen = false;
        waitForPoll = false;
        hasData = false;
        exit = false;
    }

    public static ThreadFlag getInstance() {
        return instance;
    }

    public boolean isCarryOnReading() {
        return carryOnReading;
    }

    public void setCarryOnReading(boolean carryOnReading) {
        this.carryOnReading = carryOnReading;
    }

    public boolean isCarryOnScreenDump() {
        return carryOnScreenDump;
    }

    public void setCarryOnScreenDump(boolean carryOnScreenDump) {
        this.carryOnScreenDump = carryOnScreenDump;
    }

    public boolean isCarryOnWriting() {
        return carryOnWriting;
    }

    public void setCarryOnWriting(boolean carryOnWriting) {
        this.carryOnWriting = carryOnWriting;
    }

    public boolean isCarryOnDbOperation() {
        return carryOnDbOperation;
    }

    public void setCarryOnDbOperation(boolean carryOnDbOperation) {
        this.carryOnDbOperation = carryOnDbOperation;
    }

    public boolean isDbConnectionOpen() {
        return dbConnectionOpen;
    }

    public void setDbConnectionOpen(boolean dbConnectionOpen) {
        this.dbConnectionOpen = dbConnectionOpen;
    }

    public boolean isWaitForPoll() {
        return waitForPoll;
    }

    public void setWaitForPoll(boolean waitForPoll) {
        this.waitForPoll = waitForPoll;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public boolean isExit() {
        return exit;
    }

    public void setExit(boolean exit) {
        this.exit = exit;
    }
}
