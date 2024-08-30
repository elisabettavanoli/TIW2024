package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckSignup")
public class CheckSignup extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;

	public CheckSignup() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
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
				throw new Exception("Missing or empty credential value");
			}
					

		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
			return;
		}

		// query db to authenticate for user
		UserDAO userDao = new UserDAO(connection);
		Boolean newUser = false;
		
		try {
			newUser = userDao.checkUsername(usrn);
			
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create username");
			return;
		}
		


		String path = "/index.html"; // Always return to the login page
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("signupMsg", "");
		
		if (!checkEmail(email)) {
			ctx.setVariable("signupMsg", "Email not valid!");
		}else if(!pwd1.equals(pwd2)) {
			ctx.setVariable("signupMsg", "Passwords do not match!");
		} else if (newUser) {
			try {
				userDao.createUser(usrn, name, lastname, email, pwd1);
			} catch (SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to create user");
				return;
			}
			ctx.setVariable("signupMsg", "User signed up, you can now login");
		    
		} else {
			ctx.setVariable("signupMsg", "Username already taken, please choose a new one");
		}
		    
		// Processa il template e reindirizza alla pagina di login
		templateEngine.process(path, ctx, response.getWriter());

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
