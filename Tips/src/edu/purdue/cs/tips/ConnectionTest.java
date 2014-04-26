package edu.purdue.cs.tips;

import java.util.*;

/**
 * Tester class to ensure that the server and connector is working
 * This is for internal purposes only to test functionality
 * @author Logan Gore
 */
public class ConnectionTest
{
	public static void main(String[] args)
	{
		
		//Connect to the server
		System.out.println("Connecting...");
		ServerConnection conn = new ServerConnection("data.cs.purdue.edu", 9312, 9314);
		System.out.println("Connected to database.");

		ArrayList<Tip> tips;
		ArrayList<Comment> comments;

		
		//Retrieve the latest 5 tips
		System.out.println("New tips:");
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		//Retrieve tips for a specific tag
		System.out.println("#beauty tips:");
		String[] tags = {"beauty"};
		tips = conn.getTipsByTags(tags);
		for (Tip t : tips)
			System.out.println(t);

		//Retrieve tips for a specific username
		System.out.println("god's tips:");
		tips = conn.getTipsByUsername("god");
		for (Tip t : tips)
			System.out.println(t);
		
		//Attempt to upvote a tip
		System.out.println("after upvote:");
		conn.voteTip(3, true);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		//Attempt to downvote a tip
		System.out.println("after downvote:");
		conn.voteTip(2, false);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		//Retrieve the comments for a tip
		System.out.println("Comments:");
		comments = conn.getCommentsForTip(1);
		for (Comment c : comments)
			System.out.println(c);
		
		//Post a tip
		conn.postTip("Change your oil #car #maintenance", 1);

		//Post a comment
		conn.postComment(1, "Lol, that's cool", "god");
	}
}
