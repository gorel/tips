package edu.purdue.cs.tips;

import java.net.*;
import java.util.*;
import java.sql.*;

public class ServerConnection
{
	java.sql.Connection connection;
	String hostname;
	int port;

	/**
	 * Create a conection to the tips database
	 */
	public ServerConnection(String hostname, int port)
	{
		String url = "jdbc:mysql://localhost:9312/TIPS";
		String username = "tips_user";
		String password = "tips";

		//Load the SQL driver
		Class.forName("com.mysql.jdbc.Driver");
		this.connection = DriverManager.getConnection(url, username, password);

		this.hostname = hostname;
		this.port = port;
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
			int user_id = -1;

			Thread t = new Thread()
			{
				String query = "SELECT user_id FROM users WHERE username LIKE ? AND password LIKE ?";
				PreparedStatement stat = connection.prepareStatement(query);
				stat.setString(1, username);
				stat.setString(2, password);

				ResultSet results = stat.executeQuery();
				if (results.next())
					user_id = results.getInt("user_id");

				stat.close();
				results.close();
			};

			t.start();
			t.join();

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
	 * @return an ArrayList of Tips matchin the given criteria
	 */
	public ArrayList<Tip> getNewTips(int limit)
	{
		try
		{
			ArrayList<Tip> tips = new ArrayList<Tip>();

			Thread t = new Thread()
			{
				String query = "SELECT * FROM tips ORDER BY post_date LIMIT ?";
				PreparedStatement stat = connection.prepareStatement(query);
				stat.setInt(1, limit);

				ResultSet results = stat.executeQuery();
				while (results.next())
				{
					int tipID = results.getInt("tip_id");
					String tip = results.getString("tip")
					String postDate = results.getString("post_date");
					int karma = results.getInt("karma");
					int userID = results.getInt("user_id");
					Tip t = new Tip(tipID, tip, postDate, karma, userID);
					tips.add(t);
				}

				stat.close();
				results.close();
			};
			
			t.start();
			t.join();

			return tips;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Get tips with the given tag.
	 * @param tags an array of tags to filter by
	 * @return an ArrayList of Tips matching the given criteria
	 */
	public ArrayList<Tip> getTipsByTags(String[] tags)
	{
		try
		{
			TreeSet<Integer> tipIDs = new TreeSet<Integer>();
			ArrayList<Tip> tips = new ArrayList<Tip>();

			Thread t = new Thread()
			{
				String query = "SELECT tip_id FROM tags WHERE tag LIKE ?";
				PreparedStatement stat = connection.prepareStatement(query);
				stat.setString(1, tags[0]);

				ResultSet results = stat.executeQuery();
				while (results.next())
					tipIDs.add(results.getInt("tip_id");

				stat.close();
				results.close();

				for (int i = 1; i < tags.length; i++)
				{
					ArrayList<Integer> intersection = new ArrayList<Integer>();
					stat = connection.prepareStatement(query);
					stat.setString(1, tags[i]);
					
					ResultSet results = stat.executeQuery();
					while (results.next())
						intersection.add(results.getInt("tip_id");

					stat.close();
					results.close();

					tipIDs.retainAll(intersection);
				}

				for (Integer id : tipIDs)
				{
					query = "SELECT * FROM tips WHERE tip_id = ?";
					stat = connection.prepareStatement(query);
					stat.setInt(1, id.intValue());

					ResultSet results = stat.executeQuery();
					while (results.next())
					{
						int tipID = results.getInt("tip_id");
						String tip = results.getString("tip")
						String postDate = results.getString("post_date");
						int karma = results.getInt("karma");
						int userID = results.getInt("user_id");
						Tip t = new Tip(tipID, tip, postDate, karma, userID);
						tips.add(t);
					}

					stat.close();
					results.close();
				}
			};
			
			t.start();
			t.join();

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
	 * @return an ArrayList of Tips matching the given criteria
	 */
	public ArrayList<Tip> getTipsByUsername(String username)
	{
		try
		{
			ArrayList<Tip> tips = new ArrayList<Tip>();

			Thread t = new Thread()
			{
				String query = "SELECT * FROM tips WHERE USERNAME LIKE ?";
				PreparedStatement stat = connection.prepareStatement(query);
				
				ResultSet results = stat.executeQuery();
				while (results.next())
				{
					int tipID = results.getInt("tip_id");
					String tip = results.getString("tip")
					String postDate = results.getString("post_date");
					int karma = results.getInt("karma");
					int userID = results.getInt("user_id");
					Tip t = new Tip(tipID, tip, postDate, karma, userID);
					tips.add(t);
				}

				stat.close();
				results.close();
			};
			
			t.start();
			t.join();

			return tips;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Give the given tip an upvote!
	 * @param tipID the tip to upvote
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean upvoteTip(int tipID)
	{
		try
		{
			new Thread()
			{
				String update = "UPDATE tips SET karma = karma + 1 WHERE tipID = ?";
				PreparedStatement stat = connection.prepareStatement(update);
				stat.setInt(1, tipID);

				stat.executeUpdate();
				stat.close();
			};
			
			t.start();
			t.join();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Give the given tip a downvote :(
	 * @param tipID the tip to downvote
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean downvoteTip(int tipID)
	{
		try
		{
			Thread t =new Thread()
			{
				String update = "UPDATE tips SET karma = karma - 1 WHERE tipID = ?";
				PreparedStatement stat = connection.prepareStatement(update);
				stat.setInt(1, tipID);

				stat.executeUpdate();
				stat.close();
			};
			
			t.start();
			t.join();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Retrieve the comments of the given tip
	 * @param tipID the tipID to retrieve comments for
	 * @return an ArrayList of comments from the tip
	 */
	public ArrayList<Comment> getCommentsForTip(int tipID)
	{
		try
		{
			ArrayList<Comment> comments = new ArrayList<Comment>();

			Thread t = new Thread()
			{
				String message = String.format("G %d", tipID);

				Socket socket = new Socket(hostname, port);
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				while (in.ready())
				{
					int start, pipe;
					String line = in.readLine();

					pipe = line.indexOf("|");
					String name = line.substring(0, pipe);

					start = pipe;
					pipe = line.indexOf("|", pipe);

					String dateString = line.substring(start, pipe);
					String comment = line.substring(pipe + 1);

					Comment comment = new Comment(name, dateString, comment);
					comments.add(comment);
				}
			};
			
			t.start();
			t.join();

			return comments;
		}
		catch (Exception e)
		{
			return null;
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
				while (end < word.length && Character.isLetter(word.charAt(end)))
					end++;

				if (end > 1)
					tags.add(word.substring(1, end));
			}
		}

		return tags;
	}

	/**
	 * Post a new tip to the server
	 * @param tip the tip to post to the server
	 * @param userID the userID of the posting user
	 * @return a boolean flag if the tip was successfully posted
	 */
	public boolean postTip(String tip, int userID)
	{
		try
		{
			ArrayList<String> tags;

			Thread t = new Thread()
			{
				tags = getTags(tip);

				String update = "INSERT INTO tips (tip, user_id) VALUES (?, ?)";
				PreparedStatement stat = connection.prepareStatement(update);
				stat.setString(1, tip);
				stat.setInt(2, userID);
				stat.executeUpdate();
				stat.close();
			};
			
			t.start();
			t.join();
			
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
			int tip_id;
			Thread t = new Thread()
			{
				String query = "SELECT tip_id FROM tips WHERE tip LIKE ?";
				String update = "INSERT INTO tags (tag) VALUES(?)";

				PreparedStatement stat = connection.prepareStatement(query);
				stat.setString(1, tip);
				
				ResultSet id_results = stat.executeQuery();
				id_results.next();
				tip_id = id_results.getInt("tip_id");

				stat.close();
				id_results.close();

				for (String tag : tags)
				{
					PreparedStatement stat2 = connection.prepareStatement(update);
					stat2.setString(1, tag);
					stat.executeUpdate();
					stat.close();
				}
			};

			t.start();
			t.join();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	/**
	 * Post a comment to the given tipID
	 * @param tipID the tipID to post a comment to
	 * @param comment the comment to post
	 * @param username the username of the poster
	 * @return a boolean flag if the comment was successfully posted
	 */
	public boolean postComment(int tipID, String comment, String username)
	{
		try
		{
			Thread t = new Thread()
			{
				String message = String.format("P %s %s", username, comment);

				Socket socket = new Socket(hostname, port);
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				out.print(message);

				out.close();
				socket.close();
			};
			
			t.start();	
			t.join();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
