/**
 * Black Crow Systems Limited.
 * 2016.
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
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.blackcrowsys.bcslog.server.Exceptions.CannotSubmitTaskException;
import com.blackcrowsys.bcslog.server.annotations.GuardedBy;
import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.helper.BcsProperties;
import com.blackcrowsys.bcslog.server.helper.PropertiesExtractor;
import com.blackcrowsys.bcslog.server.tasks.FifoReader;
import com.blackcrowsys.bcslog.server.tasks.ReturnCode;
import com.blackcrowsys.bcslog.server.tasks.ScreenDumper;
import com.blackcrowsys.bcslog.server.tasks.ThreadFlag;

/**
 * @author ramindursingh
 *
 */
public class FifoToScreen {

    private static final int TASK_COUNT = 2;

    private static final int WAIT_FOR_QUEUE = 3000;
    
    private static final PropertiesExtractor extractor = new PropertiesExtractor();

    @SharedObject
    @GuardedBy( by = "the flags are declared as volatile")
    private static ThreadFlag threadFlag;

    private static BcsExecutor executor;

    private static Future<Integer> readerFuture;

    private static Future<Integer> screenFuture;

    @SharedObject
    @GuardedBy( by = "the queue is thread-safe instance")
    private static final Queue<String> queue = new ConcurrentLinkedQueue<>();

    @Parameter(names = {"--fifo", "-f"}, description = "Fully qualified path to fifo/named pipe to read")
    private static String namedPipe;
    
    @Parameter(names = {"--configFile", "-c"}, description = "Fully qualified path to configuration file")
    private static String configFile;

    /**
     * @param args
     */
    public static void main(String[] args) {
        FifoToScreen fifoToScreen = new FifoToScreen();
        JCommander argParser = new JCommander(fifoToScreen, args);
        if(namedPipe == null ^ configFile == null) {
            if(configFile != null) {
                Properties properties = extractor.getPropertiesForScreen(configFile);
                namedPipe = properties.getProperty(BcsProperties.FIFO);
            }
            fifoToScreen.run();
        }else {
            argParser.usage();
        }
    }
    
    private static void printMessage() {
        System.out.println("FifoToScreen Version 1.0, 25 January 2016");
        System.out.println("This is released under GNU General Public License Version 3.");
    }

    public void run() {
        printMessage();
        threadFlag = ThreadFlag.getInstance();
        threadFlag.setCarryOnScreenDump(true);
        executor = SimpleExecutorDecorator.getInstance(TASK_COUNT);

        try {
            Callable<Integer> fifoReader = new FifoReader(threadFlag, queue,
                    namedPipe);
            Callable<Integer> screenDumper = new ScreenDumper(threadFlag, queue);
            readerFuture = executor.submit(fifoReader);
            screenFuture = executor.submit(screenDumper);
        } catch (CannotSubmitTaskException e) {
            System.out.println(e.getMessage());
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    Integer readerRc, screenRc = null;
                    threadFlag.setCarryOnReading(false);
                    if (readerFuture != null) {
                        readerRc = readerFuture.get();
                        if (readerRc.equals(ReturnCode.DATA_IN_QUEUE.getValue())) {
                            while (threadFlag.isHasData()) {
                                Thread.sleep(WAIT_FOR_QUEUE);
                            }
                        }
                    }
                    while (threadFlag.isHasData()) {
                        Thread.sleep(WAIT_FOR_QUEUE);
                    }
                    threadFlag.setCarryOnScreenDump(false);
                    if (screenFuture != null) {
                        screenRc = screenFuture.get();
                    }
                    System.out.println("Exiting FifoToScreen: RC=["
                            + screenRc + "]");

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

    }

}
