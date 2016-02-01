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
 * Writes data in the queue to a file.
 * The queue must be thread-safe.
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "This is thread safe by virtue of thread-safe ThreadFlag and Queue objects")
public final class FileWriter implements Callable<Integer> {

    private static final int SLEEP = 1000;
    
    @SharedObject
    private ThreadFlag threadFlag;
    
    @SharedObject
    private Queue<String> queue;
    
    @SharedObject
    private SharedObjectWrapper sharedObjectWrapper;
    
    @SuppressWarnings("unused")
    private FileWriter() {}
    
    public FileWriter(ThreadFlag threadFlag, Queue<String> queue, SharedObjectWrapper sharedObjectWrapper) {
        this.threadFlag = threadFlag;
        this.queue = queue;
        this.sharedObjectWrapper = sharedObjectWrapper;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        String lineNotWritten = null;
        while(threadFlag.isCarryOnWriting()) {
            while(!threadFlag.isWaitForPoll()) {
                if(lineNotWritten != null) {
                    try {
                        sharedObjectWrapper.getFileWriter().println(lineNotWritten);
                        lineNotWritten = null;
                    }catch(Exception e) {
                        e.printStackTrace();
                        threadFlag.setWaitForPoll(true);
                    }
                }else {
                    if(queue.isEmpty()) {
                        threadFlag.setHasData(false);
                        Thread.sleep(SLEEP);
                    }else {
                        String line = queue.poll();
                        if(line != null) {
                            try {
                                sharedObjectWrapper.getFileWriter().println(line);
                            }catch(Exception e) {
                                e.printStackTrace();
                                threadFlag.setWaitForPoll(true);
                                lineNotWritten = line;
                            }
                        }else {
                            threadFlag.setHasData(false);
                            Thread.sleep(SLEEP);
                        }
                    }
                    sharedObjectWrapper.getFileWriter().flush();
                }
            }
            Thread.sleep(SLEEP);
        }
        System.out.println("FileWriter: exiting");
        if(threadFlag.isHasData()) {
            return ReturnCode.DATA_IN_QUEUE.getValue();
        }else {
            return ReturnCode.OKAY.getValue();
        }
    }

}
