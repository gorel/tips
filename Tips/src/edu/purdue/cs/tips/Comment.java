package edu.purdue.cs.tips;

import android.content.Context;

/**
 * Represents a Comment on the server
 * Simple class with just a constructor and getter methods
 * @author Logan Gore
 */
public class Comment
{
	private String poster;
	private String postDate;
	private String comment;

	/**
	 * Construct a Comment object with the given parameters
	 * @param poster the name of who posted the comment
	 * @param postDate the date and time that the comment was posted
	 * @param comment the actual comment that was left
	 */
	public Comment(String poster, String postDate, String comment)
	{
		this.poster = poster;
		this.postDate = postDate;
		this.comment = comment;
	}

	/**
	 * Retrieve the name of who posted the comment
	 * @return the comment poster's name
	 */
	public String getPoster()
	{
		return this.poster;
	}

	/**
	 * Retrieve the date and time of when the comment was posted
	 * @return a String representation of when the comment was posted
	 */
	public String getPostString()
	{
		return this.postDate;
	}

	/**
	 * Retrieve the comment that was posted
	 * @return the contents of the comment
	 */
	public String getComment()
	{
		return this.comment;
	}

	/**
	 * Create a String representation of this Comment
	 * Note: This should only be used for debugging
	 * @return a String representation of this Comment
	 */
	public String toString()
	{
		return 	"poster:\t" + poster + "\n" +
			"postDate\t" + postDate + "\n" +
			"comment:\t" + comment + "\n";
	}
	
	/**
	 * Create a CommentView representation of this tip
	 * @param Context the context to create this TipView in
	 * @return a TipView representation of this tip
	 */
	public CommentView toView(Context context)
	{
		return new CommentView(context, this);
	}
}
