/**
 * Black Crow Systems Limited.
 * 2014.
 * This code is released under GNU General Public License Version 3.
 * See LICENSE for full details of the license conditions.
 */
package com.blackcrowsys.bcslog.server.helper;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import com.blackcrowsys.bcslog.server.Exceptions.ConfigurationNotValidException;

/**
 * Properties extractor/generator helper. Always returns a Properties required
 * for BCSLogServer, otherwise throws ConfigurationNotValidException.
 * 
 * @author ramindursingh
 *
 */
public class PropertiesExtractor {
    
    private static final int NON_DB_MIN_PARAMETER = 2;
    private Properties dbProperties;

    /**
     * Returns properties for FifoToScreen. Only requires one configuration,
     * path to fifo/named pipe.
     * 
     * @param configFile
     *            the configuration file
     * @return properties
     * @throws ConfigurationNotValidException
     *             if fifo is not defined
     */
    public Properties getPropertiesForScreen(String configFile)
            throws ConfigurationNotValidException {
        Properties properties = getPropertiesFromFile(configFile);
        if (properties.get(BcsProperties.FIFO) == null) {
            throw new ConfigurationNotValidException(
                    "FIFO/Named Pipe not defined or missing configuration file");
        }
        return properties;
    }

    /**
     * Returns properties for FifoToFile.
     * 
     * @param configFile
     *            the configuration file
     * @return properties
     * @throws ConfigurationNotValidException
     *             if both the fifo or output file are not defined
     */
    public Properties getPropertiesForFileOutput(String configFile)
            throws ConfigurationNotValidException {
        Properties properties = getPropertiesFromFile(configFile);
        if (properties.get(BcsProperties.FIFO) == null
                || properties.get(BcsProperties.OUTPUT_FILE) == null) {
            throw new ConfigurationNotValidException(
                    "FIFO/Named Pipe or output file not defined");
        }
        return properties;
    }
    
    /**
     * Return non-database properties to be used when writing to database.
     * It also (may not be a SOLID design) extracts all other properties assumed to be for database.
     * @param configFile the configuration file
     * @return non-database properties
     * @throws ConfigurationNotValidException if the four properties (fifo, output file, application id, table name) are not define
     */
    public Properties getPropertiesForDbOutput(String configFile) 
            throws ConfigurationNotValidException {
        Properties allProperties = getPropertiesFromFile(configFile);
        Set<Object> keys = allProperties.keySet();
        Properties justNonDbProperties = new Properties();
        dbProperties = new Properties();
        int numberOfProperties = 0;
        for(Object keyObject : keys) {
            String key = (String)keyObject;
            String value = allProperties.getProperty(key);
            if(BcsProperties.FIFO.equals(key)
                    || BcsProperties.DB_TABLE.equals(key)) {
                justNonDbProperties.put(key, value);
                numberOfProperties++;
            }else if(BcsProperties.APP_ID.equals(key)) {
                justNonDbProperties.put(key, value);
            }else {
                dbProperties.put(key, value);
            }
        }
        if(numberOfProperties < NON_DB_MIN_PARAMETER) {
            throw new ConfigurationNotValidException(
                    "Fifo or table name not defined");
        }
        return justNonDbProperties;
    }
    
    /**
     * Returns properties to open a session to a database.
     * It assumes that all predefined non-database properties are database properties.
     * @param configFile the properties file
     * @return database specific properties (see comments on how this is achieved)
     * @throws ConfigurationNotValidException if some of the critical db properties are not defined
     */
    public Properties getDbProperties(String configFile) throws ConfigurationNotValidException {
        if(dbProperties == null) {
            getPropertiesForDbOutput(configFile);
        }
        // These are the minimum that we check for
        if(dbProperties.getProperty(BcsProperties.CONN_PROVIDER) == null
                || dbProperties.getProperty(BcsProperties.DATA_SRC_CLASS) == null
                || dbProperties.getProperty(BcsProperties.URL) == null
                || dbProperties.getProperty(BcsProperties.USER) == null
                || dbProperties.getProperty(BcsProperties.PASSWORD) == null) {
            throw new ConfigurationNotValidException(
                    "Required database properties not defined");
        }
        return dbProperties;
    }

    /**
     * Extracts properties from configuration file.
     * 
     * @param configFile
     *            the configuration file
     * @return properties extracted
     * @throws ConfigurationNotValidException
     *             if cannot open or close the file
     */
    private Properties getPropertiesFromFile(String configFile)
            throws ConfigurationNotValidException {
        InputStream input = null;
        Properties properties = new Properties();
        try {
            input = new FileInputStream(configFile);
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigurationNotValidException(
                    "Could not open configuration file");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    throw new ConfigurationNotValidException(
                            "Could not close configuration file");
                }
            }
        }
        return properties;
    }
}
