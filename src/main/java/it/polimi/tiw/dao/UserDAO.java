package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.User;

public class UserDAO {
	private Connection con;

	public UserDAO(Connection connection) {
		this.con = connection;
	}

	public User checkCredentials(String usrn, String pwd) throws SQLException {
		String query = "SELECT  * FROM user  WHERE username = ? AND password =?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			pstatement.setString(2, pwd);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst()) // no results, credential check failed
					return null;
				else {
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setLastname(result.getString("lastname"));

					return user;
				}
			}
		}
	}

	public boolean checkUsername(String usrn) throws SQLException {
		String query = "SELECT username FROM user WHERE username = ?";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, usrn);
			try (ResultSet result = pstatement.executeQuery();) {
				if (result.next()) {
					int count = result.getInt("count");
					return count < 1;
				}
				return true;
			}
		}

	}

	public void createUser(String username, String name, String lastname, String email, String password)
			throws SQLException {

		String query = "INSERT into user (username, name, lastname, email, password) VALUES(?, ?, ?, ?, ?)";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, name);
			pstatement.setString(3, lastname);
			pstatement.setString(4, email);
			pstatement.setString(5, password);

			pstatement.executeUpdate(); // what does it returns??
		}
	}

	public List<User> findAllUsersOrderedByLastname() throws SQLException {
		List<User> users = new ArrayList<>();
		String query = "SELECT * FROM user ORDER BY lastname ASC";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {
			ResultSet result = pstatement.executeQuery();
			while (result.next()) {
				User user = new User();
				user.setUsername(result.getString("username"));
				user.setName(result.getString("name"));
				user.setLastname(result.getString("lastname"));
				users.add(user);
			}
		}
		return users;
	}

	public boolean checkAuthorization(User user, int groupId) throws SQLException {
		String query = "SELECT * FROM participations WHERE user = ? AND gruppo = ? AND status = 'created'";

		try (PreparedStatement pstatement = con.prepareStatement(query)) {

			pstatement.setString(1, user.getUsername()); // Imposta lo username dell'utente
			pstatement.setInt(2, groupId); // Imposta l'ID del gruppo
			ResultSet result = pstatement.executeQuery();

			return result.next();

		}
	}

	public boolean isParticipating(String participantUsername, int groupId) throws SQLException {
		String query = "SELECT * FROM participations WHERE user = ? AND gruppo = ? AND status = 'invited'";
		try (PreparedStatement pstatement = con.prepareStatement(query)) {

			pstatement.setString(1, participantUsername); // Imposta lo username dell'utente
			pstatement.setInt(2, groupId); // Imposta l'ID del gruppo
			ResultSet result = pstatement.executeQuery();
			return result.next();
		}
	}

	

}