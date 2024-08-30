package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckRemoval")
public class CheckRemoval extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		User user = (User) request.getSession().getAttribute("user");
		int groupId = Integer.parseInt(request.getParameter("groupId"));
		String participantUsername = StringEscapeUtils.escapeJava(request.getParameter("username"));
		System.out.println(participantUsername);
		UserDAO userDAO = new UserDAO(connection);


		try {
			if (!userDAO.checkAuthorization(user, groupId)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("User not authorized");
				return;

			} else if (!userDAO.isParticipating(participantUsername, groupId)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("User not participating in group");
				return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover request: " + e.getMessage());
			return;
		}

		GroupDAO groupDAO = new GroupDAO(connection);
		Group group = null;
		try {
			group = groupDAO.findGroupById(groupId);
			if (group == null) {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
				response.getWriter().println("Resource not found");
				return;
			}

		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover group");
			return;
		}
		
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

		int newParticipants;
		try {
			newParticipants = groupDAO.getParticipants(groupId).size() - 1;
			if (newParticipants >= group.getMin()) {
				try {
					groupDAO.removeParticipant(participantUsername, groupId);
				} catch (SQLException e) {
					response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					response.getWriter().println("Failed to remove participant. Please try again later.");
					return;
				}
				
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("Participant removed successfully.");
			} else {
				response.setStatus(HttpServletResponse.SC_OK);
				response.getWriter().println("Removal not allowed. Minimum participants condition not met.");
				

			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover partcipants");
		}

	}
}
