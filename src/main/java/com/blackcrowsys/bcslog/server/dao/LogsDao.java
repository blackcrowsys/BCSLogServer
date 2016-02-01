/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.dao;


/**
 * @author ramindursingh
 *
 */
public interface LogsDao {
    
    /**
     * Saves/inserts data into table.
     * @param data the data to save
     * @param table the table to which is should be saved to
     */
    void save(String data, String table);
    
    /**
     * Saves/inserts data into table.
     * @param data the data to save
     * @param table the table name
     * @param application the id of the application
     */
    void save(String data, String table, String application);
}
