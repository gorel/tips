import java.util.*;

public class ConnectionTest
{
	public static void main(String[] args)
	{
		System.out.println("Connecting...");
		ServerConnection conn = new ServerConnection("data.cs.purdue.edu", 9312, 9314);
		System.out.println("Connected to database.");
		ArrayList<Tip> tips;
		ArrayList<Comment> comments;

		
		System.out.println("New tips:");
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		System.out.println("#beauty tips:");
		String[] tags = {"beauty"};
		tips = conn.getTipsByTags(tags);
		for (Tip t : tips)
			System.out.println(t);

		System.out.println("god's tips:");
		tips = conn.getTipsByUsername("god");
		for (Tip t : tips)
			System.out.println(t);
		

		System.out.println("after upvote:");
		conn.voteTip(3, true);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		
		System.out.println("after downvote:");
		conn.voteTip(2, false);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		
		System.out.println("Comments:");
		comments = conn.getCommentsForTip(1);
		for (Comment c : comments)
			System.out.println(c);

		conn.postTip("Change your oil #car #maintenance", 1);

		conn.postComment(1, "Lol, that's cool", "god");
		
	}
}
