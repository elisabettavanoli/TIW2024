package it.polimi.tiw.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;

public class GroupDAO {
	private Connection con;

	public GroupDAO(Connection connection) {
		this.con = connection;
	}

	public List<Group> findCreatedGroupsByUser(String username) throws SQLException {
		List<Group> groups = new ArrayList<Group>();

		String query = "SELECT g.* " + "FROM gruppo g " + "JOIN participations p ON g.id = p.gruppo "
				+ "JOIN `user` u ON p.`user` = u.username " + "WHERE u.username = ? " + "AND p.status = 'created' "
				+ "AND DATE_ADD(g.date, INTERVAL g.duration DAY) >= CURDATE() ";
		//+ "AND g.date >= CURDATE()";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Group group = new Group();
					group.setId(result.getInt("id"));
					group.setTitle(result.getString("title"));
					group.setDate(result.getDate("date"));
					group.setDuration(result.getInt("duration"));
					group.setMin(result.getInt("min"));
					group.setMin(result.getInt("min"));
					groups.add(group);
				}
			}
		}
		return groups;
	}

	public List<Group> findInvitedGroupsByUser(String username) throws SQLException {
		List<Group> groups = new ArrayList<Group>();
		String query = "SELECT g.* " + "FROM gruppo g " + "JOIN participations p ON g.id = p.gruppo "
				+ "JOIN `user` u ON p.`user` = u.username " + "WHERE u.username = ? " + "AND p.status = 'invited' "
				+ "AND DATE_ADD(g.date, INTERVAL g.duration DAY) >= CURDATE()";
		try (PreparedStatement pstatement = con.prepareStatement(query);) {
			pstatement.setString(1, username);
			try (ResultSet result = pstatement.executeQuery();) {
				while (result.next()) {
					Group group = new Group();
					group.setId(result.getInt("id"));
					group.setTitle(result.getString("title"));
					group.setDate(result.getDate("date"));
					group.setDuration(result.getInt("duration"));
					group.setMin(result.getInt("min"));
					group.setMin(result.getInt("min"));
					groups.add(group);
				}
				
			}
		}
		
		return groups;

	}

	public void createGroup(Group g, String[] participants, User creator) throws SQLException {
		
	    con.setAutoCommit(false);
	    String groupQuery = "INSERT INTO gruppo (id, title, date, duration, min, max) VALUES (?, ?, ?, ?, ?, ?)";

	    try (PreparedStatement pStatement = con.prepareStatement(groupQuery);) {
	    	pStatement.setInt(1, g.getId());
	        pStatement.setString(2, g.getTitle());
	        pStatement.setDate(3, new java.sql.Date(g.getDate().getTime())); // Converte java.util.Date a java.sql.Date
	        pStatement.setInt(4, g.getDuration());
	        pStatement.setInt(5, g.getMin());
	        pStatement.setInt(6, g.getMax());
	        pStatement.executeUpdate();
	        
	        for (String p : participants) {
		    	this.inviteUserToGroup(p, g.getId());
		    }
		    
		    this.setGroupCreator(creator.getUsername(), g.getId());
		    con.commit();
	    } catch(SQLException e) {
	    	con.rollback();
	    	throw e;
	    } finally {
	    	con.setAutoCommit(true);
	    }
	    
	}




	public Group findGroupById(Integer id) throws SQLException {
	    Group group = null;

	    // Query SQL per trovare un gruppo dal suo ID
	    String query = "SELECT * FROM gruppo WHERE id = ?";

	    try (PreparedStatement pstatement = con.prepareStatement(query)) {
	        // Imposta il parametro nella query
	        pstatement.setInt(1, id);

	        // Esegui la query
	        ResultSet result = pstatement.executeQuery();

	        // Se troviamo un risultato, creiamo un oggetto Group
	        if (result.next()) {
	            group = new Group();
	            group.setId(result.getInt("id"));
	            group.setTitle(result.getString("title"));
	            group.setDate(result.getDate("date"));
	            group.setDuration(result.getInt("duration"));
	            group.setMin(result.getInt("min"));
	            group.setMax(result.getInt("max"));
	            
	        }
	    } catch (SQLException e) {
	        // Stampa l'eccezione completa per il debug
	        e.printStackTrace();
	        // Aggiungi un messaggio più descrittivo per la diagnostica
	        System.err.println("Errore durante l'esecuzione della query findGroupById: " + query + " con id: " + id);
	        // Rilancia l'eccezione per la gestione a livello superiore
	        throw e;
	    }

	    return group;
	}

	

	public ArrayList<User> getParticipants(int id) throws SQLException {
		ArrayList<User> participants = new ArrayList<>();

        String query = "SELECT u.name, u.lastname " +
                       "FROM participations p " +
                       "JOIN user u ON p.user = u.username " +
                       "WHERE p.gruppo = ? AND p.status = 'invited'";

        try (PreparedStatement pstatement = con.prepareStatement(query)) {
            // Imposta il parametro nella query
            pstatement.setInt(1, id);

            // Esegui la query
            ResultSet result = pstatement.executeQuery();

            // Itera attraverso i risultati e crea oggetti User
            while (result.next()) {
                User user = new User();
                user.setName(result.getString("name"));
                user.setLastname(result.getString("lastname"));
                participants.add(user);
            }
        }
        return participants;
	}
	
	public int lastId() throws SQLException {
	    int lastId = -1; // Valore predefinito nel caso non ci siano gruppi o si verifichi un errore
	    String query = "SELECT MAX(id) AS max_id FROM gruppo";

	    try (PreparedStatement statement = con.prepareStatement(query);
	         ResultSet resultSet = statement.executeQuery()) {
	        if (resultSet.next()) {
	            lastId = resultSet.getInt("max_id");
	        }
	    }

	    return lastId;
	}

	
	public void inviteUserToGroup(String u, int groupId) throws SQLException {
	    String query = "INSERT INTO participations (user, gruppo, status) VALUES (?, ?, 'invited')";
	    try (PreparedStatement pstatement = con.prepareStatement(query);) {
	        pstatement.setString(1, u);
	        pstatement.setInt(2, groupId);
	        pstatement.executeUpdate();
	    }
	}
	
	public void setGroupCreator(String u, int groupId) throws SQLException {
	    String query = "INSERT INTO participations (user, gruppo, status) VALUES (?, ?, 'created')";
	    try (PreparedStatement pstatement = con.prepareStatement(query);) {
	        pstatement.setString(1, u);
	        pstatement.setInt(2, groupId);
	        pstatement.executeUpdate();
	    }
	}

}
