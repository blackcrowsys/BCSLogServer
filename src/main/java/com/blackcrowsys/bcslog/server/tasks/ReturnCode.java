/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.tasks;

/**
 * @author ramindursingh
 *
 */
public enum ReturnCode {

    OKAY(Integer.valueOf(0)),
    CANNOT_OPEN_FIFO(Integer.valueOf(1)),
    DATA_IN_QUEUE(Integer.valueOf(2)),
    UNRECOVERABLE_ERROR(Integer.valueOf(3));
    
    private final Integer value;
    
    ReturnCode(final Integer value){
        this.value = value;
    }
    
    public Integer getValue() {
        return value;
    }
}
