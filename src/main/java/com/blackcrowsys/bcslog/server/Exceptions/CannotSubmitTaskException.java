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
public class CannotSubmitTaskException extends Exception {

    private static final long serialVersionUID = 2276040994987803023L;

    public CannotSubmitTaskException (String message) {
        super(message);
    }
}
