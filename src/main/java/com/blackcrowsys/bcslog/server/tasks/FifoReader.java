/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * FIFO/Named pipe reader. It reads from a fifo and puts the data into a queue.
 * The queue must be thread-safe.
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "This is thread safe by virtue of thread-safe ThreadFlag and Queue objects")
public final class FifoReader implements Callable<Integer> {

    private static final int SLEEP = 1000;
    
    @SharedObject
    private ThreadFlag threadFlag;
    
    @SharedObject
    private Queue<String> queue;
    
    private String fifo;
    
    @SuppressWarnings("unused")
    private FifoReader() {}
    
    public FifoReader(ThreadFlag threadFlag, Queue<String> queue, String fifo) {
        this.threadFlag = threadFlag;
        this.queue = queue;
        this.fifo = fifo;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        File namedPipe = new File(fifo);
        RandomAccessFile pipe = new RandomAccessFile(namedPipe, "r");
        while(threadFlag.isCarryOnReading()) {
            String readLine = pipe.readLine();
            if(readLine == null) {
                Thread.sleep(SLEEP);
            }else {
                queue.add(readLine);
                threadFlag.setHasData(true);
            }
        }
        pipe.close();
        return ReturnCode.OKAY.getValue();
    }

}
