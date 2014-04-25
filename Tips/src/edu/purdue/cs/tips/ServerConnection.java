import java.io.*;
import java.net.*;
import java.util.*;
import java.sql.*;

public class ServerConnection
{
	java.sql.Connection connection;
	String hostname;
	int tipsPort;
	int commentsPort;

	/**
	 * Create a connection to the tips database
	 */
	public ServerConnection(String hostname, int tipsPort, int commentsPort)
	{
		this.hostname = hostname;
		this.tipsPort = tipsPort;
		this.commentsPort = commentsPort;
	}

	/**
	 * Try to create an account with the given username and password
	 * @param username the username to assoicate with the account
	 * @param password the password to associate with the account
	 * @return the new account's user_id or -1 on failure
	 */
	public int createAccount(String username, String password)
	{
		try
		{
			int userID = -1;
			
			String message = String.format("create|%s|%s", username, password);

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				String line = in.readLine();
				userID = Integer.parseInt(line);
			}

			out.close();
			socket.close();
			
			return userID;
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
			int userID = -1;
			
			String message = String.format("login|%s|%s", username, password);

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				String line = in.readLine();
				userID = Integer.parseInt(line);
			}

			out.close();
			socket.close();
			
			return userID;
		}
		catch (Exception e)
		{
			return -1;
		}
	}

	/**
	 * Load up to <limit> new tips from the Tips database.
	 * @param limit the maximum number of tips to load
	 * @return an ArrayList of Tips matching the given criteria
	 */
	public ArrayList<Tip> getNewTips(final int limit)
	{
		try
		{
			ArrayList<Tip> tips = new ArrayList<Tip>();
			
			String message = String.format("getNew|%d", limit);

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				int start, pipe;
				String line = in.readLine();
				String[] values = line.split("|");
				
				int tipID = Integer.parseInt(values[0]);
				String tipString = values[1];
				String postDate = values[2];
				int karma = Integer.parseInt(values[3]);
				int userID = Integer.parseInt(values[4]);

				Tip tip = new Tip(tipID, tipString, postDate, karma, userID);
				tips.add(tip);
			}

			out.close();
			socket.close();
			
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
	public ArrayList<Tip> getTipsByTags(final String[] tags)
	{
		try
		{
			ArrayList<Tip> tips = new ArrayList<Tip>();
			
			String message = String.format("getByTags|%s", tags[0]);
			for (int i = 1; i < tags.length; i++)
				message += "," + tags[i];

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				int start, pipe;
				String line = in.readLine();
				String[] values = line.split("|");
				
				int tipID = Integer.parseInt(values[0]);
				String tipString = values[1];
				String postDate = values[2];
				int karma = Integer.parseInt(values[3]);
				int userID = Integer.parseInt(values[4]);

				Tip tip = new Tip(tipID, tipString, postDate, karma, userID);
				tips.add(tip);
			}

			out.close();
			socket.close();
			
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
	public ArrayList<Tip> getTipsByUsername(final String username)
	{		
		try
		{
			ArrayList<Tip> tips = new ArrayList<Tip>();
			
			String message = String.format("getByName|%s", username);

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				int start, pipe;
				String line = in.readLine();
				String[] values = line.split("|");
				
				int tipID = Integer.parseInt(values[0]);
				String tipString = values[1];
				String postDate = values[2];
				int karma = Integer.parseInt(values[3]);
				int userID = Integer.parseInt(values[4]);

				Tip tip = new Tip(tipID, tipString, postDate, karma, userID);
				tips.add(tip);
			}

			out.close();
			socket.close();
			
			return tips;
		}
		catch (Exception e)
		{
			return null;
		}
	}

	/**
	 * Give the given tip an upvote or downvote
	 * @param tipID the tip to upvote/downvote
	 * @param upvote whether or not this vote is an upvote (vs. a downvote)
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean voteTip(final int tipID, boolean upvote)
	{	
		try
		{
			String message = String.format("voteTip|%d|%c", tipID, upvote ? '+' : '-');

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.println(message);

			out.close();
			socket.close();

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
	public ArrayList<Comment> getCommentsForTip(final int tipID)
	{
		try
		{
			ArrayList<Comment> comments = new ArrayList<Comment>();
			String message = String.format("G %d", tipID);

			Socket socket = new Socket(hostname, commentsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(message);

			while (in.ready())
			{
				int start, pipe;
				String line = in.readLine();

				pipe = line.indexOf("|");
				String name = line.substring(0, pipe);

				start = pipe;
				pipe = line.indexOf("|", pipe);

				String dateString = line.substring(start, pipe);
				String commentString = line.substring(pipe + 1);

				Comment comment = new Comment(name, dateString, commentString);
				comments.add(comment);
			}
			
			socket.close();
			return comments;
		}
		catch (Exception e)
		{
			return null;
		}

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
			String message = String.format("postTip|%d|%s", userID, tip);

			Socket socket = new Socket(hostname, tipsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.println(message);

			out.close();
			socket.close();

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
			String message = String.format("P %d %s %s", tipID, username, comment);

			Socket socket = new Socket(hostname, commentsPort);
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			out.println(message);

			out.close();
			socket.close();

			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
