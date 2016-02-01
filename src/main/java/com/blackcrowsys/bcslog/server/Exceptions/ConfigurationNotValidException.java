/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.Exceptions;

/**
 * @author ramindursingh
 *
 */
public class ConfigurationNotValidException extends RuntimeException {
    
    private static final long serialVersionUID = 6816095690736702142L;

    public ConfigurationNotValidException (String message) {
        super(message);
    }
}
