package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckSignup")
@MultipartConfig
public class CheckSignup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public CheckSignup() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String usrn = null;
		String email = null;
		String pwd1 = null;
		String pwd2 = null;
		String name = null;
		String lastname = null;
		
		
		try {
			usrn = StringEscapeUtils.escapeJava(request.getParameter("username"));
			email = StringEscapeUtils.escapeJava(request.getParameter("email"));
			name = StringEscapeUtils.escapeJava(request.getParameter("name"));
			lastname = StringEscapeUtils.escapeJava(request.getParameter("lastname"));
			pwd1 = StringEscapeUtils.escapeJava(request.getParameter("pwd1"));
			pwd2 = StringEscapeUtils.escapeJava(request.getParameter("pwd2"));
			
			if (usrn==null || email==null || pwd1==null || pwd2==null || name==null || lastname==null ||usrn.isEmpty() || email.isEmpty() || pwd1.isEmpty() || pwd2.isEmpty() || name.isEmpty() || lastname.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Missing or empty credential value");
				return;

			}
			

		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		// query db to authenticate for user
		UserDAO userDao = new UserDAO(connection);
		Boolean newUser = false;

		
		response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

		if (!checkEmail(email)) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Email not valid!");
        } else if (!pwd1.equals(pwd2)) {
        	response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Passwords do not match!");
            
        } else {
        	
            try {
            	newUser = userDao.checkUsername(usrn);
                if (newUser) {
                	 userDao.createUser(usrn, name, lastname, email, pwd1);
                	 response.setStatus(HttpServletResponse.SC_OK);
                     response.getWriter().println("User signed up, you can now login");
                } else {
                	response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.getWriter().println("Username already taken, please choose a new one");
                }
            } catch (SQLException e) {
            	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Not Possible to create user");
            }
        }
    }
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private Boolean checkEmail(String s) {
		String emailRegex = "^[a-zA-Z0-9_+&-]+(?:\\.[a-zA-Z0-9_+&-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        // Compile the pattern
        Pattern pattern = Pattern.compile(emailRegex);

        // Create matcher object
        Matcher matcher = pattern.matcher(s);

        // Check if the email matches the pattern
        return matcher.matches();
	}
}
