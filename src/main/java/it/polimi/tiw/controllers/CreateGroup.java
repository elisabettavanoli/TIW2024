package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CreateGroup")
public class CreateGroup extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;

    public void init() throws ServletException {
    	connection = ConnectionHandler.getConnection(getServletContext());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        
        String title = null;
        Date date = getToday();
        Integer duration = null;
        Integer min = null;
        Integer max = null;
        String participants = null;
        
        try {
        	title = StringEscapeUtils.escapeJava(request.getParameter("title"));
        	duration = Integer.parseInt(request.getParameter("duration"));
        	min = Integer.parseInt(request.getParameter("min"));
        	max = Integer.parseInt(request.getParameter("max"));
        	participants = StringEscapeUtils.escapeJava(request.getParameter("users"));
        } catch (NumberFormatException | NullPointerException e) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
            return;
        }
        
        List<String> users = Arrays.asList(participants.split(","));
        
        if (min < 1 || max < 1 || duration < 1 || max<min) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Not acceptable values");
            return;
        }        
        
		GroupDAO groupDAO = new GroupDAO(connection);
        Group group = new Group();
        try {
			group.setId(groupDAO.lastId()+1);
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        group.setTitle(title);
        group.setDate(date); // Correct conversion
        group.setDuration(duration);
        group.setMin(min);
        group.setMax(max);
    	
    	
        try {
            groupDAO.createGroup(group, users, user);
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Group created successfully");
            
        } catch (SQLException e) {
        	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("Cannot create group");
        }
       
        
    }

    
    private Date getToday() {
		return new Date(System.currentTimeMillis());
	}


    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
