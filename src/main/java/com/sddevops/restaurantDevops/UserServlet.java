package com.sddevops.restaurantDevops;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class UserServlet
 */
@WebServlet("/UserServlet")
public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	//Step 1: Prepare list of variables used for database connections
	private String jdbcURL = "jdbc:mysql://localhost:3306/userinfo";
	private String jdbcUsername = "root";
	private String jdbcPassword = "password";
	
	//Step 2: Prepare list of SQL prepared statements to perform CRUD to our database
	private static final String INSERT_USERS_SQL = "INSERT INTO UserInfo" 
											+ " (name, password, email) VALUES " 
													+ "(?, ?, ?);";
	private static final String SELECT_USER_BY_ID = "select name,password,email from userinfo where name =?;";
	private static final String SELECT_ALL_USERS = "select * from UserInfo ";
	private static final String DELETE_USERS_SQL = "delete from UserInfo where name = ?;";
	private static final String UPDATE_USERS_SQL = "update UserInfo set name = ?,password= ?, email =? where name = ?;";

//	public Boolean loggedIn = false;
	
	//Step 3: Implement the getConnection method which facilitates connection to the database via JDBC
	protected Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return connection;
	}
	
	
	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public UserServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
			// TODO Auto-generated method stub
			response.getWriter().append("Served at: ").append(request.getContextPath());
				
			//Step 4: Depending on the request servlet path, determine the function to invoke using the follow switch statement.
			String action = request.getServletPath();
			try {
				switch (action) {
				/*
				 * case "/insert": break;
				 */
				case "/UserServlet/showLogin":
					showLogin(request, response);
					break;
				case "/UserServlet/login":
					login(request, response);
					break;
				case "/UserServlet/delete":
					deleteUser(request, response);
					break;
				case "/UserServlet/edit":
					showEditForm(request, response);
					break;
				case "/UserServlet/update":
					updateUser(request, response);
					break;
				default:
					listUsers(request, response);
					break;
				}
			} catch (SQLException ex) {
				throw new ServletException(ex);
		}
		
	}
	
	private void showLogin(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		request.setAttribute("loggedIn", false);
	}

	private void login(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException {
		String name = request.getParameter("yourName");
		String password = request.getParameter("yourPassword");
//		Boolean loggedIn = false;
		try (
				Connection connection = getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);
			) 
		{
			preparedStatement.setString(1, name);
			
			ResultSet rs = preparedStatement.executeQuery();
			
			while (rs.next()) {
				if(password == rs.getString("password")) {
					request.setAttribute("loggedIn", true);
					request.setAttribute("name", name);
				}
				
			}
		}catch (SQLException e) {
				System.out.println(e.getMessage());
		}

		request.getRequestDispatcher("/login.jsp").forward(request, response);
		
	}
	
	private void showEditForm(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, ServletException, IOException 
	{
		//get parameter passed in the URL
		String name = request.getParameter("name");
		User existingUser = new User("", "", "");
		
		// Step 1: Establishing a Connection
		try (
				Connection connection = getConnection();
				// Step 2:Create a statement using connection object
				PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_ID);
			) 
		{
			preparedStatement.setString(1, name);
				
			// Step 3: Execute the query or update query
			ResultSet rs = preparedStatement.executeQuery();
			
			// Step 4: Process the ResultSet object
			while (rs.next()) {
				name = rs.getString("name");
				String password = rs.getString("password");
				String email = rs.getString("email");
				existingUser = new User(name, password, email);
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		
		//Step 5: Set existingUser to request and serve up the userEdit form
		request.setAttribute("user", existingUser);
		request.getRequestDispatcher("/userEdit.jsp").forward(request, response);
	}
	
	private void updateUser(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException {
		
		//Step 1: Retrieve value from the request
		String oriName = request.getParameter("oriName");
		String name = request.getParameter("name");
		String password = request.getParameter("password");
		String email = request.getParameter("email");
		
		//Step 2: Attempt connection with database and execute update user SQL query
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_USERS_SQL);) {
			statement.setString(1, name);
			statement.setString(2, password);
			statement.setString(3, email);
			statement.setString(4, oriName);
			int i = statement.executeUpdate();
		} 
		//Step 3: redirect back to UserServlet (note: remember to change the url to your project name)
		response.sendRedirect("http://localhost:8080/huanRestaurant/UserServlet");
	}
	
	private void deleteUser(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException 
	{ 
		//Step 1: Retrieve value from the request
		String name = request.getParameter("name"); //Step 2: Attempt connection with database and execute delete user SQL query
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_USERS_SQL);) 
		{
			statement.setString(1, name);
			int i = statement.executeUpdate();
		} 
		//Step 3: redirect back to UserServlet dashboard (note: remember to change the url to your project name)
		response.sendRedirect("http://localhost:8080/huanRestaurant/UserServlet");
	}
	
	
	
	//Step 5: listUsers function to connect to the database and retrieve all users records
	private void listUsers(HttpServletRequest request, HttpServletResponse response)
			throws SQLException, IOException, ServletException
	{
		List <User> users = new ArrayList <>();
		try (
				Connection connection = getConnection();
				// Step 5.1: Create a statement using connection object
				PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS);
			) 
		{
			// Step 5.2: Execute the query or update query
			ResultSet rs = preparedStatement.executeQuery();
			// Step 5.3: Process the ResultSet object.
			while (rs.next()) {
				String name = rs.getString("name");
				String password = rs.getString("password");
				String email = rs.getString("email");
				users.add(new User(name, password, email));
				
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
		// Step 5.4: Set the users list into the listUsers attribute to be pass to the userManagement.jsp
		request.setAttribute("listUsers", users);
		request.getRequestDispatcher("/User.jsp").forward(request, response);
	}
	
	private void oneUser(HttpServletRequest request, HttpServletResponse response)
	throws SQLException, IOException, ServletException
	{
		
	}
	
	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		String formName = request.getParameter("yourName");
		String formPassword = request.getParameter("yourPassword");
		String formEmail = request.getParameter("yourEmail");

		// Step 3: attempt connection to database using JDBC, you can change the
		// username and password accordingly using the phpMyAdmin > User Account
		// dashboard
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/userinfo", "root", "password");

			// Step 4: implement the sql query using prepared statement
			// (https://docs.oracle.com/javase/tutorial/jdbc/basics/prepared.html)
			PreparedStatement ps = con.prepareStatement("insert into USERINFO values(?,?,?)");

			// Step 5: parse in the data retrieved from the web form request into the
			// prepared statement accordingly
			ps.setString(1, formName);
			ps.setString(2, formPassword);
			ps.setString(3, formEmail);

			// Step 6: perform the query on the database using the prepared statement
			int i = ps.executeUpdate();
			// Step 7: check if the query had been successfully execute, return “You are
			// successfully registered” via the response,
			if (i > 0) {
				
				PrintWriter writer = response.getWriter();
				writer.println("Successfully registered!"); 
				writer.close();
				request.setAttribute("name", formName);
			}
		}
		// Step 8: catch and print out any exception
		catch (Exception exception) {
			System.out.println(exception);
			out.close();
		}

		/*
		 * PrintWriter writer = response.getWriter(); writer.println("<h1>Name = " +
		 * formName + "<br>" + "Password = " + formPassword);
		 */

//		response.sendRedirect("http://localhost:8080/huanRestaurant/UserServlet/");
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
