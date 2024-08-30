package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/ShowUsers")
public class ShowUsers extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        ServletContext servletContext = getServletContext();
        ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
        templateResolver.setPrefix("/WEB-INF/");
        templateResolver.setSuffix(".html");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        
        User user = (User) session.getAttribute("user"); 
        if (user == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }
        
        
        String title = null;
        Date date = getToday();
        Integer duration = null;
        Integer min = null;
        Integer max = null;
        
        try {
        	title = StringEscapeUtils.escapeJava(request.getParameter("title"));
        
        	duration = Integer.parseInt(request.getParameter("duration"));
        	min = Integer.parseInt(request.getParameter("min"));
        	max = Integer.parseInt(request.getParameter("max"));
        } catch (NumberFormatException | NullPointerException e) {
        	System.out.println(e.toString());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        
        
        if (min < 1 || max < 1 || duration < 1 || max<min) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Not accetable values");
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
        session.setAttribute("group", group);


        session.setAttribute("selectedUsers", new ArrayList<String>());
        session.setAttribute("attempts", 0);
                
        
        UserDAO userDAO = new UserDAO(connection);
        
        try {
        	
			List<User> users = userDAO.findAllUsersOrderedByLastname();
			users.removeIf(u -> u.getUsername().equals(user.getUsername()));
			session.setAttribute("usersList", users);
			String path = "/Anagrafica.html";
	        ServletContext servletContext = getServletContext();
	        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
	        ctx.setVariable("users", users);
	        templateEngine.process(path, ctx, response.getWriter());
		} catch (SQLException e) {
			e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot load user list");
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
