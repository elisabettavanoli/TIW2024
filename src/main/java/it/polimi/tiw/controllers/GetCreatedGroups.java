package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetCreatedGroups")
public class GetCreatedGroups extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetCreatedGroups() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    
	    HttpSession session = request.getSession();
	    User user = (User) session.getAttribute("user");	    
	    GroupDAO groupDAO = new GroupDAO(connection);
	    List<Group> createdGroups = new ArrayList<>();

	    try {
	        createdGroups = groupDAO.findCreatedGroupsByUser(user.getUsername());

	    } catch (SQLException e) {
	    	e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover groups");
			return;
	    }

	    // Redirect to the Home page and add missions to the parameters
		
 		Gson gson = new GsonBuilder()
 				   .setDateFormat("yyyy MMM dd").create();
 		String group = gson.toJson(createdGroups);
 		response.setContentType("application/json");
 		response.setCharacterEncoding("UTF-8");
 		response.getWriter().write(group);

	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
