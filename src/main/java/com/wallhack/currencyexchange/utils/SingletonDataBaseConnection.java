package com.wallhack.currencyexchange.utils;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Getter
public class SingletonDataBaseConnection {
    private static volatile SingletonDataBaseConnection instance;
    private final String url = "jdbc:sqlite:C:\\Users\\SILVER\\IdeaProjects\\currency-exchange\\database.sqlite";
    private Connection connection;

    private SingletonDataBaseConnection(){
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            System.out.println("Database Connection Creation Failed : " + e.getMessage());
        }catch (SQLException ex){
            ex.printStackTrace();
        }

    }

    public static SingletonDataBaseConnection getInstance() {
        if (instance == null) {
            instance = new SingletonDataBaseConnection();
        }

        return instance;
    }

}
