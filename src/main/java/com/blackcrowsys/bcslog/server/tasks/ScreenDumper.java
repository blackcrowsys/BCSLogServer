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
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "This is thread safe by virtue of thread-safe ThreadFlag and Queue objects")
public final class ScreenDumper implements Callable<Integer> {

    private static final int SLEEP = 1000;
    
    @SharedObject
    private ThreadFlag threadFlag;
    
    @SharedObject
    private Queue<String> queue;
    
    @SuppressWarnings("unused")
    private ScreenDumper() {}
    
    public ScreenDumper(ThreadFlag threadFlag, Queue<String> queue) {
        this.threadFlag = threadFlag;
        this.queue = queue;
    }
    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        while(threadFlag.isCarryOnScreenDump()) {
            if(queue.isEmpty()) {
                threadFlag.setHasData(false);
                Thread.sleep(SLEEP);
            }else {
                String line = queue.poll();
                if(line != null) {
                    System.out.println(line);
                }else {
                    threadFlag.setHasData(false);
                    Thread.sleep(SLEEP);
                }
            }
        }
        if(threadFlag.isHasData()) {
            return ReturnCode.DATA_IN_QUEUE.getValue();
        }else {
            return ReturnCode.OKAY.getValue();
        }
    }

}
