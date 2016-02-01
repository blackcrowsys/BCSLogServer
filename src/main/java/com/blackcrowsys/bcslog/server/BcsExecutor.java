/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server;


import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import com.blackcrowsys.bcslog.server.Exceptions.CannotSubmitTaskException;
import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * Interface decorator to create our Executor Services.
 * All implementations must only allow a single instance of this object
 * @author ramindursingh
 *
 */
@ThreadSafe( comments = "singleton wrapper for executor service")
public interface BcsExecutor {

    /**
     * Tests if this has shut down.
     * @return true or false
     */
    public boolean isShutDown();
    
    /**
     * Tests if this has terminated.
     * @return true/false
     */
    public boolean isTerminated();
    
    /**
     * Tells the service to start shutting down tasks.
     */
    public void shutDown();
    
    /**
     * Ungraceful shutdown for the executor.
     */
    public void hardShutDown();
    
    /**
     * Submits a task to the executor service.
     * @param task the Callable task
     * @return a future object that returns the return code
     * @throws CannotSubmitTaskException if the task is null or cannot be submittede
     */
    public <T> Future<T> submit(Callable<T> task) throws CannotSubmitTaskException;
}
