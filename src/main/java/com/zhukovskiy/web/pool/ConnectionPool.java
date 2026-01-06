package com.zhukovskiy.web.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class ConnectionPool {
    private static ConnectionPool instance;
    private BlockingDeque<Connection> free = new LinkedBlockingDeque <>(8);
    private BlockingDeque<Connection> used = new LinkedBlockingDeque <>(8);

    static {
        try {
            DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        } catch (SQLException e) {
            e.printStackTrace();
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
                throw new RuntimeException(e);
            }
            free.add(connection);
        }
    }

    public static synchronized ConnectionPool getInstance() {
        if(instance == null){
            instance = new ConnectionPool();
        }
        return instance;
    }

    public Connection getConnection(){
        Connection connection = null;
        try{
            connection = free.take();
            used.put(connection);
        } catch (InterruptedException e){
            e.printStackTrace();
        }
        return connection;
    }

    public void releaseConnection(Connection connection){
        try {
            used.remove(connection);
            free.put(connection);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
