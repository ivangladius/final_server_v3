package org.example;

import java.sql.SQLException;

public class DatabaseFactory {

	public static Database getDatabase() {

		HSQLDatabase db = null;
		try {
			db = HSQLDatabase.getInstance();
			if (db.connection.isClosed()) {
				throw new Exception("Error: Failed to create database connection");
			}
		} catch (ClassNotFoundException e) {
			System.out.println("Error: Failed to load JDBC driver");
		} catch (SQLException e) {
			System.out.println("Error: Failed to create database connection");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return db;
	}
}
