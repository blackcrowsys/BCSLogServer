# BCSLogServer
## Syslog/log capture software

### Description
**BCSLogServer** is part of **BCSLogCentral** application suite. BCSLogServer is an application that is used on servers where you want some way of managing application/server logs and controlling access to your servers.
For example, instead of giving SSH access to Linux/UNIX servers, users/support staff simply use a GUI tool, **BCSLogGUI**, to examine logs sent to a central database.

The advantages of using BCSLogCentral are:

1. Increased security
2. Better management of log files on local servers
3. Centralized storage of logs


### How to use this software
You will need a FIFO or named pipe. To create a FIFO/named pipe, use the following command:

*mkfifo namedPipe*
 
 
[See this link for more information on FIFO/named pipe](http://www.linuxjournal.com/article/2156)

Then, send all output from your application to the named pipe. For example:

*foo > namedPipe 2>&1*

You can then run one of the three Java programmes to:

1. **_FifoToScreen_** - displays the output, written to the named pipe, to screen
2. **_FifoToFile_** - writes the output, written to the named pipe, to specified file
3. **_FifoToDatabase_** - writes the output, written to named pipe, to specified database

To run these applications directly (as opposed to using scripts):

*java -cp ./target/BCSLogServer-jar-with-dependencies.jar com.blackcrowsys.bcslog.server.FifoToScreen -c config.properties*

### FifoToScreen
There are two options, either one, but only one, can be used:
* **--configFile** *configurationFile* or **-c** **configurationFile**
* **--fifo** *namedPipe* or **-f** *namedPipe*

*configurationFile* is the fully qualified path and name to the configuration file. See below for more information on options.

*namedPipe* is the fully qualified path and name to the FIFO/named pipe, i.e. /var/foo/namedPipe.

**Configuration File Options**
There is only one option used: **_fifo_** that specifies the full path to the named pipe:

*fifo=/var/foo/namedPipe*

### FifoToFile
You can use either:

* **--configFile** *configurationFile* or **-c** **configurationFile**

or:

* **--fifo** *namedPipe* or **-f** *namedPipe*
* **--output** *filename* or **-o** *filename*

* *namedPipe* is the full path to the FIFO/named pipe (specified by fifo=... in the configuration file)
* *output* is the full path to the output file (specified by output=... in the configuration file)

### FifoToDatabase
This has only one option:
* **--configFile** *configurationFile* or **-c** **configurationFile**

The following is an example of a MySQL configuration file that can be used for sending all logs to the database:

fifo=/var/foo/namedpipe
application.id=foo
db.table=FOO
hibernate.dialect=org.hibernate.dialect.MySQLDialect
hibernate.connection.provider_class=com.zaxxer.hikari.hibernate.HikariConnectionProvider
hibernate.hikari.minimumIdle=5
hibernate.hikari.maximumPoolSize=10
hibernate.hikari.idleTimeout=10000
hibernate.hikari.dataSourceClassName=com.mysql.jdbc.jdbc2.optional.MysqlDataSource
hibernate.hikari.dataSource.url=jdbc:mysql://server/BCSLOGS
hibernate.hikari.dataSource.user=USER
hibernate.hikari.dataSource.password=PASSWORD

The only options that you should modify for your application are:
* fifo - this should point to the full path to the FIFO/named pipe
* application.id - (optional) if you want to identify the application that generates the logs
* db.table - the name of the table to which the logs will be written to
* hibernate.hikari.dataSource.url - the database server to which the logs are sent. It should be in the format of jdbc:mysql://**_fully qualified server name or IP Address_**/**_database_**

The other options can be tweeked for your environment and the included JAR files. The above options use [HikariCP](https://brettwooldridge.github.io/HikariCP/) for managing database connection pool.

Although the above example of configuration uses MySQL and HikariCP, any database and connection pool manager can be used. You will need to set the options for your database and connection pool manager so that the application can open up a session.

## Support Contracts
You can get support contracts from [Black Crow Systems Limited](http://www.blackcrowsys.com) if you would like a support contract for BCSLogCentral.

