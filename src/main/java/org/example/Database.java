package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.sql.Timestamp;

public interface Database {


	public ResultSet executeQuery(String query) throws SQLException;

	public void queryCreateTables() throws SQLException;

	public String queryFindIDByEmail(String email);

	public String queryFindIDByUsername(String username);

	public String queryFindUsernameByID(Integer id);

	public boolean queryValidateID(Integer id);

	public String queryInsertUser(String username, String email, String password);

	public int verifyLoginCredentials(String email, String password);

	public int checkIfUserExists(String username, String email) throws SQLException;

	public String queryGetUsername(String email);

	public List<Integer> queryListFriendsIDs(Integer id);
	
	public void databaseConnectionPoolStatistics();

	public void removeMessagesWithDots();

	public List<String[]> querySearchUsers(String name);

	public int queryChangeUsername(Integer id, String username);

	public int queryChangeEmail(Integer id, String email);

	public int queryChangePassword(Integer id, String password);

	public int queryDeleteUser(Integer id);

	public int queryAddMessage(Integer senderID, String message, Integer receiverID, Timestamp Time);

	public List<String> queryGetMessages(Integer id, String partner);

	public int queryDeleteSelectedMessages(Integer senderID, Integer receiverID, List<Integer> messageID);
	

}