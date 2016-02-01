/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server;

import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.blackcrowsys.bcslog.server.Exceptions.CannotSubmitTaskException;
import com.blackcrowsys.bcslog.server.annotations.GuardedBy;
import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.helper.BcsProperties;
import com.blackcrowsys.bcslog.server.helper.PropertiesExtractor;
import com.blackcrowsys.bcslog.server.tasks.DbConnectionManager;
import com.blackcrowsys.bcslog.server.tasks.DbWriter;
import com.blackcrowsys.bcslog.server.tasks.FifoReader;
import com.blackcrowsys.bcslog.server.tasks.ReturnCode;
import com.blackcrowsys.bcslog.server.tasks.SharedObjectWrapper;
import com.blackcrowsys.bcslog.server.tasks.ThreadFlag;

/**
 * @author ramindursingh
 *
 */
public class FifoToDatabase {

private static final int TASK_COUNT = 3;
    
    private static final int WAIT_FOR_QUEUE = 3000;
    
    private static final int TIME_OUT = 10000;
    
    private static final PropertiesExtractor extractor = new PropertiesExtractor();
    
    @SharedObject
    @GuardedBy( by = "the flags are declared as volatile")
    private static ThreadFlag threadFlag;
    
    private static String namedPipe;

    private static BcsExecutor executor;

    private static Future<Integer> readerFuture;
    
    private static Future<Integer> dbManagerFuture;
    
    private static Future<Integer> dbWriterFuture;
    
    @SharedObject
    @GuardedBy( by = "the queue is thread-safe instance")
    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();
    
    @SharedObject
    private static SharedObjectWrapper sharedObjectWrapper = new SharedObjectWrapper();
    
    @Parameter(names = {"--configFile", "-c"}, description = "Fully qualified path to configuration file")
    private static String configFile;
    
    public static void main(String[] args) {
        FifoToDatabase fifoToDatabase = new FifoToDatabase();
        JCommander argParser = new JCommander(fifoToDatabase, args);
        if(configFile == null) {
            argParser.usage();
        }else {
            Properties fileProperties = extractor.getPropertiesForDbOutput(configFile);
            namedPipe = fileProperties.getProperty(BcsProperties.FIFO);
            sharedObjectWrapper.setTable(fileProperties.getProperty(BcsProperties.DB_TABLE));
            sharedObjectWrapper.setApplication(fileProperties.getProperty(BcsProperties.APP_ID));
            sharedObjectWrapper.setDbProperties(extractor.getDbProperties(configFile));
            fifoToDatabase.run();
        }
    }
    
    private void run() {
        printMessage();
        threadFlag = ThreadFlag.getInstance();
        threadFlag.setDbConnectionOpen(false);
        threadFlag.setCarryOnDbOperation(true);
        executor = SimpleExecutorDecorator.getInstance(TASK_COUNT);
        
        try {
            Callable<Integer> fifoReader = new FifoReader(threadFlag, queue, namedPipe);
            Callable<Integer> dbManager = new DbConnectionManager(threadFlag, sharedObjectWrapper);
            Callable<Integer> dbWriter = new DbWriter(threadFlag, queue, sharedObjectWrapper);
            readerFuture = executor.submit(fifoReader);
            dbManagerFuture = executor.submit(dbManager);
            dbWriterFuture = executor.submit(dbWriter);
        }catch (CannotSubmitTaskException e) {
            e.printStackTrace();
        }
        
        Thread hook = new Thread() {
            public void run() {
                System.out.println("FifoToDatabase: shutting down all tasks, please wait.");
                try {
                    Integer readerRc, dbOperationRc = null, dbManagerRc = null;
                    threadFlag.setCarryOnReading(false);
                    if(readerFuture != null) {
                        readerRc = readerFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
                        if(readerRc.equals(ReturnCode.DATA_IN_QUEUE.getValue())) {
                            while(threadFlag.isHasData()) {
                                Thread.sleep(WAIT_FOR_QUEUE);
                            }
                        }
                    }
                    while(threadFlag.isHasData()) {
                        Thread.sleep(WAIT_FOR_QUEUE);
                    }
                    
                    threadFlag.setCarryOnDbOperation(false);
                    
                    if(dbManagerFuture != null) {
                        dbManagerRc = dbManagerFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
                    }
                    
                    threadFlag.setDbConnectionOpen(false);
                    if(dbWriterFuture != null) {
                        dbOperationRc = dbWriterFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
                    }
                    
                    System.out.println("Size of queue:" + queue.size());
                    System.out.println("Exiting FifoToDatabase: RC=[" + dbManagerRc + ", " + dbOperationRc + "]");
                    
                }catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Runtime.getRuntime().addShutdownHook(hook);
        
        while(!threadFlag.isExit()) {
            try {
                Thread.sleep(WAIT_FOR_QUEUE);
            }catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
        
        if(threadFlag.isExit()) {
            System.out.println("There was an unrecoverable error. Exiting FifoToDatabase");
            Runtime.getRuntime().removeShutdownHook(hook);
            threadFlag.setCarryOnReading(false);
            threadFlag.setCarryOnDbOperation(false);
            executor.hardShutDown();
            Runtime.getRuntime().exit(0);
        }
        
    }
    
    private static void printMessage() {
        System.out.println("FifoToScreen Version 1.0, January 2016");
        System.out.println("This is released under GNU General Public License Version 3.");
    }

}
