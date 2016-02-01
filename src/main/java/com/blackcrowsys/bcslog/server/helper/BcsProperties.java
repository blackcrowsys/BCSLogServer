/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.helper;

/**
 * @author ramindursingh
 *
 */
public class BcsProperties {

    // parameter for fifo/named pipe
    public static final String FIFO = "fifo";
    // parameter for name of output file
    public static final String OUTPUT_FILE = "output";
    // parameter for id of the logging application
    public static final String APP_ID = "application.id";
    // parameter for table name
    public static final String DB_TABLE = "db.table";
    
    // parameters for database connection
    // We use HikariCP for connections to the database
    public static final String CONN_PROVIDER = "hibernate.connection.provider_class";
    public static final String DATA_SRC_CLASS = "hibernate.hikari.dataSourceClassName";
    public static final String URL = "hibernate.hikari.dataSource.url";
    public static final String USER = "hibernate.hikari.dataSource.user";
    public static final String PASSWORD = "hibernate.hikari.dataSource.password";
}
