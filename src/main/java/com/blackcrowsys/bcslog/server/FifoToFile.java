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
import com.blackcrowsys.bcslog.server.helper.PropertiesExtractor;
import com.blackcrowsys.bcslog.server.tasks.FifoReader;
import com.blackcrowsys.bcslog.server.tasks.FileWriter;
import com.blackcrowsys.bcslog.server.tasks.OutputFileManager;
import com.blackcrowsys.bcslog.server.tasks.ReturnCode;
import com.blackcrowsys.bcslog.server.tasks.SharedObjectWrapper;
import com.blackcrowsys.bcslog.server.tasks.ThreadFlag;

/**
 * @author ramindursingh
 *
 */
public class FifoToFile {
    
    private static final int TASK_COUNT = 3;
    
    private static final int WAIT_FOR_QUEUE = 3000;
    
    private static final int TIME_OUT = 10000;
    
    private static final PropertiesExtractor extractor = new PropertiesExtractor();
    
    @SharedObject
    @GuardedBy( by = "the flags are declared as volatile")
    private static ThreadFlag threadFlag;

    private static BcsExecutor executor;

    private static Future<Integer> readerFuture;
    
    private static Future<Integer> fileManagerFuture;
    
    private static Future<Integer> fileWriterFuture;
    
    @SharedObject
    @GuardedBy( by = "the queue is thread-safe instance")
    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();
    
    @SharedObject
    private static SharedObjectWrapper sharedObjectWrapper = new SharedObjectWrapper();
    
    @Parameter(names = {"--fifo", "-f"}, description = "Fully qualified path to fifo/named pipe to read")
    private static String namedPipe;
    
    @Parameter(names = {"--output", "-o"}, description = "Fully qualified name of the file to send the output to")
    private static String output;
    
    @Parameter(names = {"--configFile", "-c"}, description = "Fully qualified path to configuration"
            + " file")
    private static String configFile;
    
    // The setting in sharedObjectWrapper file
    private static final String CONFIG_FIFO = "fifo";
    private static final String OUTPUT_FILE = "output";

    public static void main(String[] args) {
        FifoToFile fifoToFile = new FifoToFile();
        JCommander argParser = new JCommander(fifoToFile, args);
        if( configFile == null ) {
            if(namedPipe != null && output != null) {
                fifoToFile.run();
            }else {
                argParser.usage();
            }
        }else {
            if(namedPipe != null || output != null) {
                argParser.usage();
            }else {
                Properties properties = extractor.getPropertiesForFileOutput(configFile);
                namedPipe = properties.getProperty(CONFIG_FIFO);
                output = properties.getProperty(OUTPUT_FILE);
                fifoToFile.run();
            }
        }
    }
    
    public void run() {
        printMessage();
        threadFlag = ThreadFlag.getInstance();
        threadFlag.setCarryOnWriting(true);
        threadFlag.setWaitForPoll(true);
        executor = SimpleExecutorDecorator.getInstance(TASK_COUNT);
        
        try {
            Callable<Integer> fifoReader = new FifoReader(threadFlag, queue, namedPipe);
            Callable<Integer> outputFileManager = new OutputFileManager(threadFlag, output, sharedObjectWrapper);
            Callable<Integer> fileWriter = new FileWriter(threadFlag, queue, sharedObjectWrapper);
            readerFuture = executor.submit(fifoReader);
            fileWriterFuture = executor.submit(fileWriter);
            fileManagerFuture = executor.submit(outputFileManager);
        } catch(CannotSubmitTaskException e) {
            e.printStackTrace();
        }
        
        Thread hook = new Thread() {
            public void run() {
                System.out.println("FifoToFile: shutting down all tasks, please wait");
                try {
                    Integer readerRc, fileManagerRc = null, fileWriterRc = null;
                    threadFlag.setCarryOnReading(false);
                    System.out.println("FifoToFile: shutting down reader");
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
                    
                    threadFlag.setCarryOnWriting(false);
                    threadFlag.setWaitForPoll(true);
                    
                    if(fileManagerFuture != null) {
                        fileManagerRc = fileManagerFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
                    }
                    
                    if(fileWriterFuture != null) {
                        fileWriterRc = fileWriterFuture.get(TIME_OUT, TimeUnit.MILLISECONDS);
                    }
                    
                    if(sharedObjectWrapper.getFileWriter() != null) {
                        sharedObjectWrapper.getFileWriter().close();
                    }
                    
                    System.out.println("Size of queue:" + queue.size());
                    System.out.println("Exiting FifoToFile: RC=[" + fileManagerRc + ", " + fileWriterRc + "]");
                    
                } catch (InterruptedException | ExecutionException | TimeoutException e) {
                    e.printStackTrace();
                }
            }
        };
        
        Runtime.getRuntime().addShutdownHook(hook);

        while(!threadFlag.isExit()) {
            try {
                Thread.sleep(WAIT_FOR_QUEUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if(threadFlag.isExit()) {
            System.out.println("There was an unrecoverable error. Exiting FifoToFile.");
            Runtime.getRuntime().removeShutdownHook(hook);
            threadFlag.setCarryOnReading(false);
            threadFlag.setCarryOnWriting(false);
            executor.hardShutDown();
            Runtime.getRuntime().exit(0);
        }
        
    }
    
    private static void printMessage() {
        System.out.println("FifoToScreen Version 1.0, 26 January 2016");
        System.out.println("This is released under GNU General Public License Version 3.");
    }
}
