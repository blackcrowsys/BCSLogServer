/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import java.util.Queue;
import java.util.concurrent.Callable;
import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * Writes data in the queue to database. The queue object must be thread-safe.
 * 
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "This is thread safe by virtue of thread-safe ThreadFlag and Queue objects")
public class DbWriter implements Callable<Integer> {

    private static final int SLEEP = 1000;

    @SharedObject
    private ThreadFlag threadFlag;

    @SharedObject
    private Queue<String> queue;

    @SharedObject
    private SharedObjectWrapper sharedObjectWrapper;

    @SuppressWarnings("unused")
    private DbWriter() {
    }

    public DbWriter(ThreadFlag threadFlag, Queue<String> queue,
            SharedObjectWrapper sharedObjectWrapper) {
        this.threadFlag = threadFlag;
        this.queue = queue;
        this.sharedObjectWrapper = sharedObjectWrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        String lineNotWritten = null;
        while (threadFlag.isCarryOnDbOperation()) {
            while (threadFlag.isDbConnectionOpen()) {
                if (lineNotWritten != null) {
                    try {
                        if (sharedObjectWrapper.getApplication() == null) {
                            sharedObjectWrapper.getLogsDao().save(
                                    lineNotWritten,
                                    sharedObjectWrapper.getTable());
                        } else {
                            sharedObjectWrapper.getLogsDao().save(
                                    lineNotWritten,
                                    sharedObjectWrapper.getTable(),
                                    sharedObjectWrapper.getApplication());
                        }
                    } catch (Exception e) {
                        threadFlag.setDbConnectionOpen(false);
                        e.printStackTrace();
                    }
                } else {
                    if (queue.isEmpty()) {
                        threadFlag.setHasData(false);
                        Thread.sleep(SLEEP);
                    } else {
                        String line = queue.poll();
                        if (line != null) {
                            try {
                                if (sharedObjectWrapper.getApplication() == null) {
                                    sharedObjectWrapper.getLogsDao().save(
                                            line,
                                            sharedObjectWrapper.getTable());
                                } else {
                                    sharedObjectWrapper.getLogsDao().save(
                                            line,
                                            sharedObjectWrapper.getTable(),
                                            sharedObjectWrapper
                                                    .getApplication());
                                }
                            } catch (Exception e) {
                                threadFlag.setDbConnectionOpen(false);
                                e.printStackTrace();
                            }
                        } else {
                            threadFlag.setHasData(false);
                            Thread.sleep(SLEEP);
                        }

                    }
                }
            }
            Thread.sleep(SLEEP);
        }

        System.out.println("DbWriter: exiting");
        if (threadFlag.isHasData()) {
            return ReturnCode.DATA_IN_QUEUE.getValue();
        } else {
            return ReturnCode.OKAY.getValue();
        }
    }

}
