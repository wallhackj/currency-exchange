package com.wallhack.currencyexchange.utils;

import lombok.Getter;
import lombok.Synchronized;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public class SingletonDataBaseConnection {
    private static volatile SingletonDataBaseConnection instance;
    private final Connection connection;

    private SingletonDataBaseConnection() throws SQLException {
        String url = "jdbc:sqlite:database.sqlite";
        connection = DriverManager.getConnection(url);
    }

    @Synchronized
    public static SingletonDataBaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (SingletonDataBaseConnection.class) {
                if (instance == null) {
                    instance = new SingletonDataBaseConnection();
                }
            }
        }

        return instance;
    }
}
