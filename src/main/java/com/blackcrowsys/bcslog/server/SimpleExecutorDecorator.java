/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import com.blackcrowsys.bcslog.server.Exceptions.CannotSubmitTaskException;

/**
 * Implementation of the BcsExecutor that wraps ExecutorService object. This is
 * a simple executor service using a fixed thread pool for each task.
 * 
 * @author ramindursingh
 *
 */
public class SimpleExecutorDecorator implements BcsExecutor {

    private static final String ERROR_MSG = "The task could not be submitted to the executor service";

    private static final SimpleExecutorDecorator instance = new SimpleExecutorDecorator();

    private static ExecutorService executor = null;

    /**
     * Prevent anyone for creating a new instance of this object
     */
    private SimpleExecutorDecorator() {
    }

    public static BcsExecutor getInstance(int taskCount) {
        if (executor == null) {
            executor = Executors.newFixedThreadPool(taskCount);
        }
        return instance;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.blackcrowsys.bcslog.server.BcsExecutor#isShutDown()
     */
    public boolean isShutDown() {
        return executor.isShutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.blackcrowsys.bcslog.server.BcsExecutor#isTerminated()
     */
    public boolean isTerminated() {
        return executor.isTerminated();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.blackcrowsys.bcslog.server.BcsExecutor#shutDown()
     */
    public void shutDown() {
        executor.shutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.blackcrowsys.bcslog.server.BcsExecutor#submit(java.util.concurrent
     * .Callable)
     */
    public <T> Future<T> submit(Callable<T> task)
            throws CannotSubmitTaskException {
        try {
            return executor.submit(task);
        } catch (NullPointerException | RejectedExecutionException e) {
            throw new CannotSubmitTaskException(ERROR_MSG);
        }
    }

    /* (non-Javadoc)
     * @see com.blackcrowsys.bcslog.server.BcsExecutor#hardShutDown()
     */
    @Override
    public void hardShutDown() {
        executor.shutdownNow();
    }

}
