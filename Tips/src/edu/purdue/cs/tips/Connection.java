import java.net.*;
import java.util.*;
import java.sql.*;

public class DatabaseConnection
{
	java.sql.Connection connection;

	/**
	 * Create a conection to the tips database
	 */
	public DatabaseConnection()
	{
		String url = "jdbc:mysql://localhost:9312/TIPS";
		String username = "tips_user";
		String password = "tips";

		//Load the SQL driver
		Class.forName("com.mysql.jdbc.Driver");
		this.connection = DriverManager.getConnection(url, username, password);
	}

	/**
	 * Load up to <limit> new tips from the Tips database.
	 * @param limit the maximum number of tips to load
	 * @return an array of Tips matchin the given criteria
	 */
	public Tip[] getNewTips(int limit)
	{
	
	}

	/**
	 * Get tips with the given tag.
	 * @param tag an array of tags to filter by
	 * @return an array of Tips matching the given criteria
	 */
	public Tip[] getTipsByTags(String[] tag)
	{
	
	}

	/**
	 * Get tips from the given username
	 * @param username the user to load tips from
	 * @return an array of Tips matching the given criteria
	 */
	public Tip[] getTipsByUsername(String username)
	{
	
	}

	/**
	 * Give the given tip an upvote!
	 * @param tipID the tip to upvote
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean upvoteTip(int tipID)
	{
	
	}

	/**
	 * Give the given tip a downvote :(
	 * @param tipID the tip to downvote
	 * @return a boolean flag if the vote was successfully posted
	 */
	public boolean downvoteTip(int tipID)
	{
	
	}

	/**
	 * Retrieve the comments of the given tip
	 * @param tipID the tipID to retrieve comments for
	 * @return an array of comments from the tip
	 */
	public Comment[] getCommentsForTip(int tipID)
	{
	
	}

	/**
	 * Post a comment to the given tipID
	 * @param tipID the tipID to post a comment to
	 * @return a boolean flag if the comment was successfully posted
	 */
	public boolean postComment(int tipID)
	{
	
	}
}
