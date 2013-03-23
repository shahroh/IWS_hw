package edu.upenn.cis455.hw1;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class LoginServlet extends HttpServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException 
	{       
		String id = req.getParameter("realname");
		String password = req.getParameter("mypassword");
	}

	public void doPost(HttpServletRequest req, HttpServletResponse res)throws ServletException, IOException 
	{       
		String id = req.getParameter("realname");
		String password = req.getParameter("mypassword");
	}
}