import java.util.*;

public class ConnectionTest
{
	public static void main(String[] args)
	{
		System.out.println("Connecting...");
		ServerConnection conn = new ServerConnection("data.cs.purdue.edu", 9312, 9314);
		System.out.println("Connected to database.");
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

		conn.voteTip(1, true);
		tips = conn.getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		conn.voteTip(1, false);
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
