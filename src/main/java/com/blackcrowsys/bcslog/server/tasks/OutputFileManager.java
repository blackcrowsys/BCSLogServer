/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Callable;

import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * Manager for the file to output logs to.
 * It's main purpose is to make sure that the sharedObjectWrapper's PrintWriter object is initialized for the 
 * FileWriter to write to.
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "Thread safe by virtue of thread-safe shared objects")
public final class OutputFileManager implements Callable<Integer> {
    
    private static final int SLEEP = 5000;
    
    @SharedObject
    private ThreadFlag threadFlag;
    
    @SharedObject
    private SharedObjectWrapper sharedObjectWrapper;
    
    private String filename;

    @SuppressWarnings("unused")
    private OutputFileManager() {}
    
    public OutputFileManager(ThreadFlag threadFlag, final String filename, SharedObjectWrapper sharedObjectWrapper) {
        this.threadFlag = threadFlag;
        this.filename = filename;
        this.sharedObjectWrapper = sharedObjectWrapper;
    }
    
    /* (non-Javadoc)
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        while(threadFlag.isCarryOnWriting()) {
            if(threadFlag.isWaitForPoll()) {
                try {
                    sharedObjectWrapper.setFileWriter(new PrintWriter(filename));
                    threadFlag.setWaitForPoll(false);
                    System.out.println("OutputFileManager: writer to " + filename + " has been opened");
                }catch(IOException | UnsupportedOperationException | SecurityException e) {
                    e.printStackTrace();
                    threadFlag.setCarryOnReading(false);
                    threadFlag.setCarryOnWriting(false);
                    threadFlag.setExit(true);
                    System.out.println("OutputFileManager: could not open file for writing.");
                    return ReturnCode.UNRECOVERABLE_ERROR.getValue();
                }
            }
            Thread.sleep(SLEEP);
        }
        System.out.println("OutputFileManager: exiting.");
        return ReturnCode.OKAY.getValue();
    }

}
