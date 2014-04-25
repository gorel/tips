package edu.purdue.cs.tips;

import java.util.*;

public class ConnectionTest
{
	public static void main(String[] args)
	{
		ServerConnection conn = new ServerConnection("data.cs.purdue.edu", 9313);
		ArrayList<Tip> tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		String[] tags = {"beauty"};
		tips = conn.getTipsByTags(tags);
		for (Tip t : tips)
			System.out.println(t);

		tips = conn.getTipsByUsername("god");
		for (Tip t : tips)
			System.out.println(t);

		conn.upvoteTip(1);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		conn.downvoteTip(1);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		ArrayList<Comment> comments = conn.getCommentsForTip(1);
		for (Comment c : comments)
			System.out.println(c);

		conn.postTip("Change your oil #car #maintenance", 1);

		conn.postComment(1, "Lol, that's cool", "god");
	}
}
