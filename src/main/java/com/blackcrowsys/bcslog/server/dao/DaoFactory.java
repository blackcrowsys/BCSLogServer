/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.dao;

import org.hibernate.SessionFactory;

import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;

/**
 * Factory for generating DAO objects.
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "class is thread safe by virtue of getting locks on dao objects")
public class DaoFactory {

    private volatile static LogsDao logsDao;
    
    public static LogsDao getLogsDao(SessionFactory sessionFactory) {
        if(logsDao == null) {
            synchronized(LogsDao.class) {
                if(logsDao == null) {
                    logsDao = new JdbcLogsDao(sessionFactory);
                }
            }
        }
        return logsDao;
    }
}
