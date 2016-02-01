/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import java.util.concurrent.Callable;

import org.hibernate.HibernateException;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.blackcrowsys.bcslog.server.annotations.SharedObject;
import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;
import com.blackcrowsys.bcslog.server.dao.DaoFactory;

/**
 * Database connection manager. It's main purpose is to make sure that there is a valid
 * SessionFactory in the sharedObjectWrapper.
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "thread-safe by virtue that the shared objects are thread-safe")
public final class DbConnectionManager implements Callable<Integer> {

    private static final int SLEEP = 5000;

    private static ServiceRegistry serviceRegistry;

    @SharedObject
    private ThreadFlag threadFlag;

    @SharedObject
    private SharedObjectWrapper sharedObjectWrapper;

    @SuppressWarnings("unused")
    private DbConnectionManager() {
    }

    public DbConnectionManager(ThreadFlag threadFlag,
            SharedObjectWrapper sharedObjectWrapper) {
        this.threadFlag = threadFlag;
        this.sharedObjectWrapper = sharedObjectWrapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.concurrent.Callable#call()
     */
    @Override
    public Integer call() throws Exception {
        while (threadFlag.isCarryOnDbOperation()) {
            if (!threadFlag.isDbConnectionOpen()) {
                try {
                    Configuration cfg = new Configuration()
                            .addProperties(sharedObjectWrapper.getDbProperties());
                    serviceRegistry = new StandardServiceRegistryBuilder()
                            .applySettings(cfg.getProperties()).build();
                    sharedObjectWrapper.setLogsDao(DaoFactory.getLogsDao(cfg.buildSessionFactory(serviceRegistry)));
                    threadFlag.setDbConnectionOpen(true);
                } catch (HibernateException e) {
                    threadFlag.setDbConnectionOpen(false);
                    e.printStackTrace();
                }
            }
            Thread.sleep(SLEEP);
        }
        System.out.println("SQL Connection Manager: exiting");
        return ReturnCode.OKAY.getValue();
    }

}
