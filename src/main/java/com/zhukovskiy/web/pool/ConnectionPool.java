package com.zhukovskiy.web.pool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private static final Logger logger = LogManager.getLogger();
    private static ConnectionPool instance;
    private final BlockingDeque<Connection> free = new LinkedBlockingDeque<>();
    private final BlockingDeque<Connection> used = new LinkedBlockingDeque<>();
    private static final Lock instanceLock = new ReentrantLock();
    private static final Lock destroyLock = new ReentrantLock();
    private final String url;
    private final Properties connectionProperties;
    private final int poolSize;
    private boolean isDestroyed = false;

    static {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            logger.info("MySQL JDBC driver registered successfully");
            //Class.forName("om.mysql.cj.jdbc.Driver"); //2
        } catch (SQLException e) {
            logger.fatal("Failed to register MySQL JDBC driver", e);
            throw new ExceptionInInitializerError();
        }
    }

    private ConnectionPool() {
        Properties dbProperties = loadProperties();
        url = dbProperties.getProperty("db.url");
        String poolSizeStr = dbProperties.getProperty("db.pool.size", "8");
        poolSize = Integer.parseInt(poolSizeStr);

        connectionProperties = createConnectionProperties(dbProperties);
        initializeConnections();
        logger.info("Connection pool initialized successfully with {} connections", poolSize);

    }

    private void initializeConnections() {
        int successfulConnections = 0;

        for (int i = 0; i < poolSize; ++i) {
            try {
                Connection connection = DriverManager.getConnection(url, connectionProperties);

                if (connection.isValid(2)) {
                    free.add(connection);
                    successfulConnections++;
                    logger.debug("Connection {} created successfully", i + 1);
                } else {
                    logger.warn("Connection {} is invalid, closing", i + 1);
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Failed to create database connection {}", i + 1, e);
            }
        }

        if (successfulConnections == 0) {
            logger.fatal("Failed to create any database connections");
            throw new ExceptionInInitializerError("No database connections could be established");
        }

        if (successfulConnections < poolSize) {
            logger.warn("Created only {} out of {} requested connections",
                    successfulConnections, poolSize);
        }
    }

    private Properties createConnectionProperties(Properties dbProperties) {
        Properties props = new Properties();
        String user = dbProperties.getProperty("db.user");
        String password = dbProperties.getProperty("db.password");

        if (user == null || user.isEmpty()) {
            logger.error("Database user is not specified");
            throw new ExceptionInInitializerError("Database user is required");
        }

        if (password == null) {
            logger.warn("Database password is not specified (may be empty)");
            password = "";
        }

        props.put("user", user);
        props.put("password", password);

        props.put("autoReconnect", dbProperties.getProperty("db.autoReconnect", "true"));
        props.put("characterEncoding", dbProperties.getProperty("db.characterEncoding", "UTF-8"));
        props.put("useUnicode", dbProperties.getProperty("db.useUnicode", "true"));
        props.put("useSSL", dbProperties.getProperty("db.useSSL", "true"));
        props.put("useJDBCCompliantTimezoneShift",
                dbProperties.getProperty("db.useJDBCCompliantTimezoneShift", "true"));
        props.put("useLegacyDatetimeCode",
                dbProperties.getProperty("db.useLegacyDatetimeCode", "false"));
        props.put("serverTimeZone", dbProperties.getProperty("db.serverTimeZone", "UTC"));

        logger.debug("Connection properties configured for user: {}", user);
        return props;

    }

    private Properties loadProperties() {
        Properties properties = new Properties();
        String configFile = "database.properties";

        try (InputStream input = ConnectionPool.class.getClassLoader()
                .getResourceAsStream(configFile)) {
            if (input == null) {
                logger.fatal("Database configuration file '{}' not found", configFile);
                throw new ExceptionInInitializerError(
                        String.format("Database configuration file '%s' not found", configFile)
                );
            }
            properties.load(input);
            logger.info("Database configuration loaded from '{}'", configFile);
        } catch (IOException e) {
            logger.fatal("Error reading database configuration");
            throw new ExceptionInInitializerError(String.format("Failed to load database configuration: %s", e.getMessage()));
        }
        return properties;
    }

    public static ConnectionPool getInstance() {
        instanceLock.lock();
        try {
            if (instance == null) {
                instance = new ConnectionPool();
            }
            return instance;
        } finally {
            instanceLock.unlock();
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            connection = free.take();
            used.put(connection);
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while waiting for connection", e);
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public boolean releaseConnection(Connection connection) {
        if (connection == null) {
            logger.warn("Attempt to release null connection");
            return false;
        }

        try {
            if (!used.remove(connection)) {
                logger.warn("Connection not found in used pool");
                return false;
            }

            if (connection.isClosed()) {
                logger.warn("Connection is closed, creating new one");
                try {
                    connection = DriverManager.getConnection(url, connectionProperties);
                } catch (SQLException e) {
                    logger.error("Failed to create replacement connection", e);
                    return false;
                }
            }

            free.put(connection);
            logger.debug("Connection released, free: {}, used: {}",
                    free.size(), used.size());
            return true;
        } catch (InterruptedException e) {
            logger.error("Thread interrupted while releasing connection", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (SQLException e) {
            logger.error("Error checking connection state", e);
            return false;
        }
    }

    public void destroyPool() {
        destroyLock.lock();
        try {
            if (isDestroyed) {
                logger.info("Pool already destroyed");
                return;
            }

            logger.info("Starting pool destruction...");
            isDestroyed = true;

            int closedCount = 0;

            while (!free.isEmpty()) {
                try {
                    Connection conn = free.poll();
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        closedCount++;
                        logger.debug("Closed free connection");
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to close connection", e);
                }
            }

            while (!used.isEmpty()) {
                try {
                    Connection conn = used.poll();
                    if (conn != null && !conn.isClosed()) {
                        conn.close();
                        closedCount++;
                        logger.debug("Closed used connection");
                    }
                } catch (SQLException e) {
                    logger.warn("Failed to close connection", e);
                }
            }

            deregisterDriver();

            instance = null;

            logger.info("Pool destroyed successfully. Closed {} connections", closedCount);

        } finally {
            destroyLock.unlock();
        }
    }

    private void deregisterDriver() {
        try {
            Driver driver = DriverManager.getDriver(url);
            DriverManager.deregisterDriver(driver);
            logger.info("JDBC driver deregistered");
        } catch (SQLException e) {
            logger.warn("Failed to deregister JDBC driver", e);
        }
    }
}
