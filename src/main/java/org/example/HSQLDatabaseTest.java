package org.example;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.jupiter.api.TestMethodOrder;
import org.junit.Assert;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import java.sql.Timestamp;
import java.util.List;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(OrderAnnotation.class)
class HSQLDatabaseTest {

	@Test
	@Order(1)
	void testFail() {

		fail("Unfortuantely, the test failed");

	}

	@Test
	@Order(2)
	void databaseSingletonCheck() {

		Database connection1 = DatabaseFactory.getDatabase();
		Database connection2 = DatabaseFactory.getDatabase();
		assertTrue(connection1 == connection2);
	}

	@Test
	@Order(3)
	void testCreateTableUsersContent() {

		try {

			Database db = DatabaseFactory.getDatabase();

			db.executeQuery(
					"INSERT INTO users (user_name, email, password) VALUES ('Darren', 'darren@gmail.com', 'AR8933');");
			ResultSet rs = db.executeQuery(
					"SELECT COUNT(*) FROM users WHERE user_name = 'Darren' AND email = 'darren@gmail.com' AND password = 'AR8933'");

			int count = 0;
			int id = 0;

			while (rs.next()) {

				count = rs.getInt(1);

			}

			Assert.assertEquals(1, count);

			rs = db.executeQuery(
					"SELECT ID FROM users WHERE user_name = 'Darren' AND email = 'darren@gmail.com' AND password = 'AR8933'");

			while (rs.next()) {
				id = rs.getInt(1);
			}

			db.executeQuery("DELETE FROM users WHERE ID = " + id);

		} catch (SQLException e) {

			e.printStackTrace();
			fail();

		}
	}

	@Test
	@Order(4)
	void testCreateTableChatsContent() {

		try {

			Database db = DatabaseFactory.getDatabase();

			// Inserts two new users to the database

			db.executeQuery(
					"INSERT INTO users (user_name, email, password) VALUES ('Swana', 'swanan@gmail.com', 'SWANA8933');");

			db.executeQuery(
					"INSERT INTO users (user_name, email, password) VALUES ('Saskia', 'saskia@gmail.com', 'SASKIA8933');");

			int senderID = Integer.parseInt(db.queryFindIDByUsername("Swana"));
			int receiverID = Integer.parseInt(db.queryFindIDByUsername("Saskia"));
			String message = "How is JAVA going?";
			Timestamp time = new Timestamp(System.currentTimeMillis());

			db.executeQuery("INSERT INTO chats (sender_ID, message, receiver_ID, time) " + "VALUES (" + senderID + ", '"
					+ message + "', " + receiverID + ", '" + time + "')");

			ResultSet rs = db
					.executeQuery("SELECT COUNT(*) FROM chats WHERE Sender_ID = " + senderID + " AND Message = '"
							+ message + "' AND Receiver_ID = " + receiverID + " AND Time = '" + time + "'");

			int count = 0;

			while (rs.next()) {

				count = rs.getInt(1);
				Assert.assertEquals(1, count);

			}

			// Deletes the two users created for testing from users and chats table

			db.executeQuery("DELETE FROM chats WHERE Sender_ID = " + senderID + " AND Message = '" + message
					+ "' AND Receiver_ID = " + receiverID + " AND Time = '" + time + "'");
			db.executeQuery("DELETE FROM users WHERE (ID = " + senderID + " OR ID = '" + receiverID + "')");

		} catch (SQLException e) {

			e.printStackTrace();

		}
	}

	@Test
	@Order(5)
	void testQueryValidateID() {
		Database db = DatabaseFactory.getDatabase();

		// Inserts a new test user
		int userID = Integer.parseInt(db.queryInsertUser("Tom", "tom@gmx.de", "Tom700"));

		// Validates the test user's ID
		Assert.assertTrue(db.queryValidateID(userID));

		// Cleans up by deleting the test user
		db.queryDeleteUser(userID);

		// Validates that the deleted ID is no longer valid
		Assert.assertFalse(db.queryValidateID(userID));
	}

	@Test
	@Order(6)
	void testQueryFindUsernameByID() {

		Database db = DatabaseFactory.getDatabase();

		String username = "Max";
		String email = "max@gmail.com";
		String password = "Games";
		int id = Integer.parseInt(db.queryInsertUser(username, email, password));

		String expectedUsername = "Max";
		String actualUsername = db.queryFindUsernameByID(id);

		Assert.assertEquals(expectedUsername, actualUsername);

		// Deletes the user
		db.queryDeleteUser(id);

	}

	@Test
	@Order(7)
	void testQueryFindIDByEmail() {
		Database db = DatabaseFactory.getDatabase();
		String username = "Michael";
		String email = "Michael@yahoo.com";
		String password = "NoGoArea";
		db.queryInsertUser(username, email, password);
		int expectedID = Integer.parseInt(db.queryFindIDByUsername(username));
		db.queryInsertUser(username, email, password);
		String actualID = db.queryFindIDByEmail(email);
		Assert.assertEquals(expectedID, Integer.parseInt(actualID));

		// Deletes the user

		db.queryDeleteUser(expectedID);
	}

	@Test
	@Order(8)
	void testQueryGetUsername() {
		Database db = DatabaseFactory.getDatabase();
		String username = "Harry";
		String email = "harry@mymail.com";
		String password = "HP";
		int id = Integer.parseInt(db.queryInsertUser(username, email, password));
		String expectedUsername = "Harry";
		String actualUsername = db.queryGetUsername(email);
		Assert.assertEquals(expectedUsername, actualUsername);

		// Deletes the user

		db.queryDeleteUser(id);
	}

	@Test
	@Order(9)
	void testQueryAddMessage() {

		try {
			Database db = DatabaseFactory.getDatabase();

			db.executeQuery(
					"INSERT INTO users (user_name, email, password) VALUES ('Marius', 'marius@gmx.de', 'Marius8933');");

			db.executeQuery(
					"INSERT INTO users (user_name, email, password) VALUES ('Niklas', 'niklas@hotmail.de', 'Niklas8933');");

			int senderID = Integer.parseInt(db.queryFindIDByUsername("Marius"));
			String message = "Hello, how are you?";
			int receiverID = Integer.parseInt(db.queryFindIDByUsername("Niklas"));
			Timestamp time = new Timestamp(System.currentTimeMillis());

			int result = db.queryAddMessage(senderID, message, receiverID, time);
			assertEquals(1, result);
			ResultSet rs = db.executeQuery("SELECT * FROM CHATS WHERE Sender_ID = " + senderID + " AND Message = '"
					+ message + "' AND Receiver_ID = " + receiverID);
			assertTrue(rs.next());
			int id = rs.getInt("MESSAGE_ID");

			// Deletes the two users created for testing from users and chats table

			db.executeQuery("DELETE FROM chats WHERE Message_ID = " + id);
			db.executeQuery("DELETE FROM users WHERE (ID = " + senderID + " OR ID = '" + receiverID + "')");

		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	@Order(10)
	public void testQueryListFriends() {
		Database db = DatabaseFactory.getDatabase();

		// Inserts some test users
		int senderID = Integer.parseInt(db.queryInsertUser("Junaid", "junaid@gmx.de", "Junaid156"));
		int receiverID1 = Integer.parseInt(db.queryInsertUser("Mehroz", "mehoroz@gmail.com", "Mehroz777"));
		int receiverID2 = Integer.parseInt(db.queryInsertUser("Hamza", "hamza@msn.com", "Hamza97"));

		// Inserts some test messages
		db.queryAddMessage(senderID, "Hi, Bro!", receiverID1, new Timestamp(System.currentTimeMillis()));
		db.queryAddMessage(senderID, "Hi, is everything good?", receiverID2,
				new Timestamp(System.currentTimeMillis()));

		// Gets all friends of the user being tested
		List<Integer> friendsIDs = db.queryListFriendsIDs(senderID);

		// Asserts that Receiver1 and Receiver2 are in the list of friends
		Assert.assertTrue(friendsIDs.contains(receiverID1)); // Receiver 1
		Assert.assertTrue(friendsIDs.contains(receiverID2)); // Receiver 2

		// Cleans up by deleting the test users
		db.queryDeleteUser(senderID);
		db.queryDeleteUser(receiverID1);
		db.queryDeleteUser(receiverID2);
	}

	@Test
	@Order(11)
	void testQueryChangeUsername() {

		Database db = DatabaseFactory.getDatabase();

		// Inserting a new user
		String username = "Ali";
		String email = "ali@hotmail.com";
		String password = "Area109";
		int id = Integer.parseInt(db.queryInsertUser(username, email, password));

		// Changing the username
		String newUsername = "Jane";
		db.queryChangeUsername(id, newUsername);

		String actualUsername = db.queryFindUsernameByID(id);

		// Checks if the username was changed
		Assert.assertEquals(newUsername, actualUsername);

		// Deletes the user
		db.queryDeleteUser(id);
	}

	@Test
	@Order(12)
	void testQueryChangeEmail() {

		try {

			Database db = DatabaseFactory.getDatabase();

			String username = "Bob";
			String email = "Bob@hotmail.com";
			String password = "PK121";
			int id = Integer.parseInt(db.queryInsertUser(username, email, password));

			String newEmail = "Bob@gmx.de";
			db.queryChangeEmail(id, newEmail);

			ResultSet rs = db.executeQuery("SELECT email FROM users WHERE id = " + id);
			rs.next();
			String actualEmail = rs.getString("email");
			Assert.assertEquals(newEmail, actualEmail);

			db.queryDeleteUser(id);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	@Order(13)
	void testQueryChangePassword() {

		try {
			Database db = DatabaseFactory.getDatabase();

			String username = "Alice";
			String email = "alice@hotmail.com";
			String password = "Area101";
			int id = Integer.parseInt(db.queryInsertUser(username, email, password));
			String newPassword = "NewPassword";
			db.queryChangePassword(id, newPassword); // Changes password

			// Checks if the password was changed by querying the database

			ResultSet rs = db.executeQuery("SELECT * FROM USERS WHERE id = " + id);
			while (rs.next()) {
				String actualPassword = rs.getString("password");
				Assert.assertEquals(newPassword, actualPassword);
			}

			// Deletes the user
			db.queryDeleteUser(id);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}

	}

	@Test
	@Order(14)
	void testQueryDeleteUser() {
		try {
			Database db = DatabaseFactory.getDatabase();
			String username = "Julia";
			String email = "julia@yahoo.com";
			String password = "Area999";
			int id = Integer.parseInt(db.queryInsertUser(username, email, password));
			db.queryDeleteUser(id);
			ResultSet rs = db.executeQuery("SELECT COUNT(*) FROM users WHERE id = " + id);
			int count = 0;
			while (rs.next()) {
				count = rs.getInt(1);
			}
			Assert.assertEquals(0, count);
		} catch (SQLException e) {
			e.printStackTrace();
			fail();
		}

	}

}