package com.wallhack.currencyexchange.utils;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

@Getter
public class SingletonDataBaseConnection {
    private Connection connection;
    private static volatile SingletonDataBaseConnection instance;
    private static final Logger logger = Logger.getLogger(SingletonDataBaseConnection.class.getName());
    private final String url = "jdbc:sqlite:C:\\Users\\SILVER\\IdeaProjects\\currency-exchange\\database.sqlite";


    private SingletonDataBaseConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            logger.severe("Database not found : " + e.getMessage());
        }catch (SQLException e) {
            logger.severe("Database Connection Creation Failed : " + e.getMessage());
        }
    }

    public static SingletonDataBaseConnection getInstance() {
        if (instance == null) {
            instance = new SingletonDataBaseConnection();
        }

        return instance;
    }

}
