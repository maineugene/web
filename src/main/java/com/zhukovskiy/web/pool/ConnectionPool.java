package com.zhukovskiy.web.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectionPool {
    private static ConnectionPool instance;
    private final BlockingDeque<Connection> free = new LinkedBlockingDeque<>(8);
    private final BlockingDeque<Connection> used = new LinkedBlockingDeque<>(8);
    private static final Lock instanceLock = new ReentrantLock();

    static {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
            //Class.forName("om.mysql.cj.jdbc.Driver"); //2
        } catch (SQLException e) {
             throw new ExceptionInInitializerError();
        }
    }

    private ConnectionPool() {
        //todo хранение должно быть в проперти-файлах
        String url = "jdbc:mysql://localhost:3306/restaurant";
        Properties prop = new Properties();
        prop.put("user", "root");
        prop.put("password", "QWerty123456!");
        prop.put("autoReconnect", "true");
        prop.put("characterEncoding", "UTF-8");
        prop.put("useUnicode", "true");
        prop.put("useSSL", "true");
        prop.put("useJDBCCompliantTimezoneShift", "true");
        prop.put("useLegacyDatetimeCode", "false");
        prop.put("serverTimeZone", "UTC");
        prop.put("serverSslCert", "classpath:server.crt");
        for (int i = 0; i < 8; ++i) {
            Connection connection = null;
            try {
                connection = DriverManager.getConnection(url, prop);
            } catch (SQLException e) {
                throw new ExceptionInInitializerError();
            }
            free.add(connection);
        }
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
            // log
            Thread.currentThread().interrupt();
        }
        return connection;
    }

    public void releaseConnection(Connection connection) {
        try {
            used.remove(connection);
            free.put(connection);
        } catch (InterruptedException e) {
            // log
            Thread.currentThread().interrupt();
        }
    }
    //todo deregisterDriver
    public void destroyPool(){
        for(int i = 0; i < 8; ++i){
            try {
                free.take().close();
            } catch (SQLException | InterruptedException e) {
                // log
            }
        }
    }
}
