import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.io.*;
import java.util.*;


///////////////////////////// Multithreaded Server /////////////////////////////

public class SQLServer
{
	final static int port = 9312;
	public static void main(String[] args)
	{  
		try
		{  
			ServerSocket s = new ServerSocket(port);
			System.out.println("Server started on port " + port);

			while (true)
			{  
				Socket incoming = s.accept();
				new Thread(new ThreadedHandler(incoming)).start();
			}
		}
		catch (IOException e)
		{  
			e.printStackTrace();
		}
	}
}

/**
	This class handles the client input for one server socket connection. 
*/
class ThreadedHandler implements Runnable
{ 
	final static String ServerUser = "tips_user";
	final static String ServerPassword = "tips";
	private Socket incoming;

	public ThreadedHandler(Socket i)
	{ 
		incoming = i;
	}

	public static Connection getConnection() throws SQLException, IOException
		{
		Properties props = new Properties();
		FileInputStream in = new FileInputStream("database.properties");
		props.load(in);
		in.close();
		String drivers = props.getProperty("jdbc.drivers");
		if (drivers != null)
			System.setProperty("jdbc.drivers", drivers);
		String url = props.getProperty("jdbc.url");
		String username = props.getProperty("jdbc.username");
		String password = props.getProperty("jdbc.password");

		try
		{
			Class.forName("com.mysql.jdbc.Driver");
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		return DriverManager.getConnection( url, username, password);
	}

	/**
	 * Create a new Tips account
	 * @param username the username to associate with the account
	 * @param password the password to associate with the account
	 * @return the new account's user_id or -1 on failure
	 */
	public int createAccount(String username, String password)
	{
		try
		{
			Connection connection = getConnection();
			
			int user_id = -1;
			
			String update = "INSERT INTO users (username, password) VALUES(?, ?)";
			PreparedStatement stat = connection.prepareStatement(update);
			stat.setString(1, username);
			stat.setString(2, password);

			stat.executeUpdate();
			stat.close();

			String query = "SELECT user_id FROM users WHERE username LIKE ?";
			stat = connection.prepareStatement(query);
			stat.setString(1, username);

			ResultSet results = stat.executeQuery();
			if (results.next())
				user_id = results.getInt("user_id");

			stat.close();
			results.close();
			connection.close();
			
			return user_id;
		}
		catch (Exception e)
		{
			return -1;
		}

	}
	
	/**
	 * Attempt to login with the given username and password
	 * @param username the username to login with
	 * @param password the password to login with
	 * @return the user's user_id, or -1 on failure
	 */
	public int login(String username, String password)
	{
		try
		{
			Connection connection = getConnection();
			
			int user_id = -1;
			
			String query = "SELECT user_id FROM users WHERE username LIKE ? AND password LIKE ?";
			PreparedStatement stat = connection.prepareStatement(query);
			stat.setString(1, username);
			stat.setString(2, password);

			ResultSet results = stat.executeQuery();
			if (results.next())
				user_id = results.getInt("user_id");

			stat.close();
			results.close();
			connection.close();
			
			return user_id;
		}
		catch (Exception e)
		{
			return -1;
		}
	}
	
	/**
	 * Load up to <limit> new tips from the Tips database.
	 * @param limit the maximum number of tips to load
	 * @return an ArrayList of Strings representing tips matching the given criteria
	 */
	public ArrayList<String> getNewTips(final int limit)
	{
		try
		{
			Connection connection = getConnection();
			
			ArrayList<String> tips = new ArrayList<String>();
			
			String query = "SELECT * FROM tips ORDER BY post_date LIMIT ?";
			PreparedStatement stat = connection.prepareStatement(query);
			stat.setInt(1, limit);

			ResultSet results = stat.executeQuery();
			while (results.next())
			{
				int tipID = results.getInt("tip_id");
				String tip = results.getString("tip");
				String postDate = results.getString("post_date");
				int karma = results.getInt("karma");
				int userID = results.getInt("user_id");
				String t = String.format("%d|%s|%s|%d|%d", tipID, tip, postDate, karma, userID);
				System.out.println("Adding " + t);
				tips.add(t);
			}

			stat.close();
			results.close();
			connection.close();

			return tips;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Get tips with the given tag.
	 * @param tags an array of tags to filter by
	 * @return an ArrayList of Strings representing tips matching the given criteria
	 */
	public ArrayList<String> getTipsByTags(final String[] tags)
	{
		try
		{
			Connection connection = getConnection();
			
			TreeSet<Integer> tipIDs = new TreeSet<Integer>();
			ArrayList<String> tips = new ArrayList<String>();

			String query = "SELECT tip_id FROM tags WHERE tag LIKE ?";
			PreparedStatement stat = connection.prepareStatement(query);
			stat.setString(1, tags[0]);

			ResultSet results = stat.executeQuery();
			while (results.next())
				tipIDs.add(results.getInt("tip_id"));

			stat.close();
			results.close();

			for (int i = 1; i < tags.length; i++)
			{
				ArrayList<Integer> intersection = new ArrayList<Integer>();
				stat = connection.prepareStatement(query);
				stat.setString(1, tags[i]);
				
				results = stat.executeQuery();
				while (results.next())
					intersection.add(results.getInt("tip_id"));

				stat.close();
				results.close();

				tipIDs.retainAll(intersection);
			}

			for (Integer id : tipIDs)
			{
				query = "SELECT * FROM tips WHERE tip_id = ?";
				stat = connection.prepareStatement(query);
				stat.setInt(1, id.intValue());

				results = stat.executeQuery();
				while (results.next())
				{
					int tipID = results.getInt("tip_id");
					String tip = results.getString("tip");
					String postDate = results.getString("post_date");
					int karma = results.getInt("karma");
					int userID = results.getInt("user_id");
					String t = String.format("%d|%s|%s|%d|%d", tipID, tip, postDate, karma, userID);
					tips.add(t);
				}

				stat.close();
				results.close();
				connection.close();
			}

			return tips;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Get tips from the given username
	 * @param username the user to load tips from
	 * @return an ArrayList of Strings representing tips matching the given criteria
	 */
	public ArrayList<String> getTipsByUsername(final String username)
	{
		try
		{
			Connection connection = getConnection();
			
			ArrayList<String> tips = new ArrayList<String>();
			
			String query = "SELECT * FROM tips WHERE USERNAME LIKE ?";
			PreparedStatement stat = connection.prepareStatement(query);
			
			ResultSet results = stat.executeQuery();
			while (results.next())
			{
				int tipID = results.getInt("tip_id");
				String tip = results.getString("tip");
				String postDate = results.getString("post_date");
				int karma = results.getInt("karma");
				int userID = results.getInt("user_id");
				String t = String.format("%d|%s|%s|%d|%d", tipID, tip, postDate, karma, userID);
				tips.add(t);
			}

			stat.close();
			results.close();
			connection.close();

			return tips;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Give the given tip an upvote or downvote!
	 * @param tipID the tip to vote up or down
	 * @param upvote true if giving an upvote, false for a downvote
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean voteTip(final int tipID, boolean upvote)
	{
		try
		{
			Connection connection = getConnection();
			
			String update;
			if (upvote)
				update = "UPDATE tips SET karma = karma + 1 WHERE tipID = ?";
			else
				update = "UPDATE tips SET karma = karma - 1 WHERE tipID = ?";
			PreparedStatement stat = connection.prepareStatement(update);
			stat.setInt(1, tipID);

			stat.executeUpdate();
			stat.close();
			connection.close();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Retrieve the tags from the given tip
	 * @param tip the tip to parse
	 * @return an array of tags for the given tip
	 */
	private ArrayList<String> getTags(String tip)
	{
		ArrayList<String> tags = new ArrayList<String>();

		String[] words = tip.split(" ");
		for (String word : words)
		{
			if (word.charAt(0) == '#')
			{
				int end = 1;
				while (end < word.length() && Character.isLetter(word.charAt(end)))
					end++;

				if (end > 1)
					tags.add(word.substring(1, end));
			}
		}

		return tags;
	}
	
	/**
	 * Post a new tip to the server
	 * @param userID the userID of the posting user
	 * @param tip the tip to post to the server
	 * @return a boolean flag if the tip was successfully posted
	 */
	public boolean postTip(int userID, String tip)
	{
		try
		{
			Connection connection = getConnection();
		
			ArrayList<String> tags = getTags(tip);

			String update = "INSERT INTO tips (tip, user_id) VALUES (?, ?)";
			PreparedStatement stat = connection.prepareStatement(update);
			stat.setString(1, tip);
			stat.setInt(2, userID);
			stat.executeUpdate();
			
			stat.close();
			connection.close();
			
			return updateTags(tip, tags);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Associate the given tags with the tip
	 * @param tip the tip to associate the tags to
	 * @param tags the tags to assign to the tip
	 * @return a boolean flag if the tags were successfully posted
	 */
	private boolean updateTags(String tip, ArrayList<String> tags)
	{
		try
		{
			Connection connection = getConnection();
			
			int tip_id;
			
			String query = "SELECT tip_id FROM tips WHERE tip LIKE ?";
			String update = "INSERT INTO tags (tip_id, tag) VALUES(?, ?)";

			PreparedStatement stat = connection.prepareStatement(query);
			stat.setString(1, tip);
			
			ResultSet results = stat.executeQuery();
			results.next();
			tip_id = results.getInt("tip_id");

			stat.close();
			results.close();

			for (String tag : tags)
			{
				stat = connection.prepareStatement(update);
				stat.setInt(1, tip_id);
				stat.setString(2, tag);
				stat.executeUpdate();
				stat.close();
			}

			connection.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
	
	/**
	 * Handle a request from a client
	 * @param inStream the input stream of the socket
	 * @param outStream the output stream of the socket
	 */
	void handleRequest(InputStream inStream, OutputStream outStream)
	{
		Scanner in = new Scanner(inStream);			
		PrintWriter out = new PrintWriter(outStream, true);

		// Get parameters of the call
		String request = in.nextLine();
		System.out.println("Request: " + request);

		try {
			// Get arguments.
			int index = request.indexOf("|");
			String command = request.substring(0, index);

			// Do the operation
			if (command.equals("create"))
			{
				int start = index + 1;
				index = request.indexOf("|", start);
				String username = request.substring(start, index);
				start = index + 1;
				String password = request.substring(start);
				
				int user_id = createAccount(username, password);
				out.println(user_id);
			}
			if (command.equals("login")) //create|<username>|<password> or login|<username>|<password>
			{
				int start = index + 1;
				index = request.indexOf("|", start);
				String username = request.substring(start, index);
				start = index + 1;
				String password = request.substring(start);
				
				int user_id = login(username, password);
				out.println(user_id);
			}
			else if (command.equals("getNew")) //getNew|<limit>
			{
				int start = index + 1;
				int limit = Integer.parseInt(request.substring(start));
				
				ArrayList<String> tips = getNewTips(limit);
				if (tips != null)
				{
					for (String tip : tips)
						out.println(tip);
				}
				else
				{
					out.println("null");
				}
			}
			else if (command.equals("GetByTags")) //getByTags|<tag1>,<tag2>,...,<tagN>
			{
				int start = index + 1;
				String[] tags = request.substring(request.indexOf("|", start)).split(",");
				
				ArrayList<String> tips = getTipsByTags(tags);

				if (tips != null)
				{
					for (String tip : tips)
						out.println(tip);
				}
				else
				{
					out.println("null");
				}
			}
			else if (command.equals("GetByName")) //getByName|<username>
			{
				int start = index + 1;
				String username = request.substring(request.indexOf("|", start));
				
				ArrayList<String> tips = getTipsByUsername(username);
				if (tips != null)
				{
					for (String tip : tips)
						out.println(tip);
				}
				else
				{
					out.println("null");
				}
			}
			else if (command.equals("voteTip")) //voteTip|<tipID>|<+/->
			{
				int start = index + 1;
				index = request.indexOf("|", start);
				int tipID = Integer.parseInt(request.substring(start, index));
				start = index + 1;
				
				if (request.charAt(start) == '+')
					voteTip(tipID, true);
				else
					voteTip(tipID, false);
			}
			else if (command.equals("postTip")) //postTip|<userID>|<tip>
			{
				int start = index + 1;
				index = request.indexOf("|", start);
				int userID = Integer.parseInt(request.substring(start, index));
				start = index + 1;
				String tip = request.substring(start);
				
				postTip(userID, tip);
			}	
		}
		catch (Exception e) {
			System.out.println(e.toString());
			out.println(e.toString());
		}
	}

	public void run()
	{  
		try
		{  
			try
			{
				InputStream inStream = incoming.getInputStream();
				OutputStream outStream = incoming.getOutputStream();
				handleRequest(inStream, outStream);
			}
			catch (IOException e)
			{  
				e.printStackTrace();
			 }
			 finally
			 {
				incoming.close();
			 }
		  }
		catch (IOException e)
		{  
			e.printStackTrace();
		}
	}
}
