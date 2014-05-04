package edu.purdue.cs.tips;

import android.content.Context;

/**
 * Creates an immutable object representation of a tip
 * This class just contains a constructor and simple getter methods
 * @author Logan Gore
 */
public class Tip
{
	private int tipID;
	private String tip;
	private String postDate;
	private int karma;
	private int userID;

	/**
	 * Create a Tip object
	 * @param tipID the tip_id of this tip
	 * @param tip the actual String tip that was posted
	 * @param postDate the date and time the tip was posted
	 * @param karma the current karma score for the tip
	 * @param userID the user_id of the user that posted this tip
	 */
	public Tip(int tipID, String tip, String postDate, int karma, int userID)
	{
		this.tipID = tipID;
		this.tip = tip;
		this.postDate = postDate;
		this.karma = karma;
		this.userID = userID;
	}

	/**
	 * Retrieve the tip_id for this tip
	 * @return the tip_id for this tip
	 */
	public int getTipID()
	{
		return this.tipID;
	}

	/**
	 * Retrieve the actual tip that was posted for this tip
	 * @return the actual String tip for this tip
	 */
	public String getTip()
	{
		return this.tip;
	}

	/**
	 * Retrieve the post date for this tip
	 * @return the String representation of this tip's post date/time
	 */
	public String getPostDate()
	{
		return this.postDate;
	}

	/**
	 * Retrieve the karma count for this tip
	 * @return the karma count for this tip
	 */
	public int getKarma()
	{
		return this.karma;
	}

	/**
	 * Retrieve the user_id of the poster of this tip
	 * @return the user_id of the poster of this tip
	 */
	public int getUserID()
	{
		return this.userID;
	}

	/**
	 * Create a String representation of this tip
	 * Note: This should only be used for debugging purposes
	 * @return a String representation of this tip
	 */
	public String toString()
	{
		return 	"tipID:\t\t" + tipID + "\n" +
			"tip:\t\t" + tip + "\n" + 
			"postDate:\t" + postDate + "\n" + 
			"karma:\t\t" + karma + "\n" + 
			"userID:\t\t" + userID + "\n";
	}
	
	/**
	 * Create a TipView representation of this tip
	 * @param Context the context to create this TipView in
	 * @return a TipView representation of this tip
	 */
	public TipView toView(Context context, MainActivity activity)
	{
		return new TipView(context, activity, this);
	}
}
