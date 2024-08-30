package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/CheckInvitations")
public class CheckInvitations extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection = null;
    private TemplateEngine templateEngine;
    private static final int MAX_ATTEMPTS = 3;

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

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(getServletContext().getContextPath() + "/index.html");
            return;
        }

        
        Group group = (Group) session.getAttribute("group");
        
   
        String[] users = request.getParameterValues("users");//recupera gli utenti selezionati
       
        
        int numParticipants;
        if (users == null) {
            numParticipants = 0;
            session.setAttribute("selectedUsers", new ArrayList<String>());
        } else {
            numParticipants = users.length;
            session.setAttribute("selectedUsers", Arrays.asList(users));
           
        }

      
      
        // Get the number of failed attempts
        Integer attempts = (Integer) session.getAttribute("attempts");
        if (attempts == null) {
            attempts = 0;
            session.setAttribute("attempts", attempts);
        }
        
        ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        
    	if (numParticipants < group.getMin()) {
            attempts++;
            session.setAttribute("attempts", attempts);
            if (attempts >= MAX_ATTEMPTS) {
                showCancellationPage(request, response);
            } else {
            	ctx.setVariable("users", (List<User>) session.getAttribute("usersList"));
            	ctx.setVariable("errorMsg", "Troppo pochi utenti selezionati, aggiungerne almeno " + (group.getMin() - numParticipants));
            	templateEngine.process("/Anagrafica", ctx, response.getWriter());
            }
            return;
        }
    	
    	if (numParticipants > group.getMax()) {
            attempts++;
            session.setAttribute("attempts", attempts);
            if (attempts >= MAX_ATTEMPTS) {
                showCancellationPage(request, response);
            } else {
            	ctx.setVariable("users", (List<User>) session.getAttribute("usersList"));
            	ctx.setVariable("errorMsg", "Troppi utenti selezionati, eliminarne almeno " + (numParticipants - group.getMax()));
            	templateEngine.process("/Anagrafica", ctx, response.getWriter());
                
            }
            return;
        }
    	
    	
    	GroupDAO groupDAO = new GroupDAO(connection);
        try {
            groupDAO.createGroup(group, users, user);
            
            // Reset attempts on success
            session.setAttribute("attempts", 0);
            response.sendRedirect(request.getContextPath() + "/Home");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot create group");
        }
       
        
    }

    private void showCancellationPage(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String path = "/Cancellazione.html";
        ServletContext servletContext = getServletContext();
        final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
        templateEngine.process(path, ctx, response.getWriter());
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
