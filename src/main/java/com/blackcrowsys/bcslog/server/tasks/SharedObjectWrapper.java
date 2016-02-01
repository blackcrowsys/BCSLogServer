/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

import java.io.PrintWriter;
import java.util.Properties;

import org.hibernate.SessionFactory;

import com.blackcrowsys.bcslog.server.annotations.ThreadSafe;
import com.blackcrowsys.bcslog.server.dao.LogsDao;

/**
 * @author ramindursingh
 *
 */
@ThreadSafe(comments = "thread safe as all methods are synchronized")
public class SharedObjectWrapper {

    private PrintWriter fileWriter;
    
    private Properties dbProperties;
    
    private LogsDao logsDao;
    
    private String table;
    
    private String application;

    public synchronized PrintWriter getFileWriter() {
        return fileWriter;
    }

    public synchronized void setFileWriter(PrintWriter fileWriter) {
        this.fileWriter = fileWriter;
    }

    public synchronized Properties getDbProperties() {
        return dbProperties;
    }

    public synchronized void setDbProperties(Properties dbProperties) {
        this.dbProperties = dbProperties;
    }

    public synchronized LogsDao getLogsDao() {
        return logsDao;
    }

    public synchronized void setLogsDao(LogsDao logsDao) {
        this.logsDao = logsDao;
    }

    public synchronized String getTable() {
        return table;
    }

    public synchronized void setTable(String table) {
        this.table = table;
    }

    public synchronized String getApplication() {
        return application;
    }

    public synchronized void setApplication(String application) {
        this.application = application;
    }
}
