package edu.purdue.cs.tips;

public class ConnectionTest
{
	public static void main(String args)
	{
		ServerConnection conn = new ServerConnection("data.cs.purdue.edu", 9313);
		ArrayList<Tip> tips = getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		String[] tags = {"beauty"};
		tips = getTipsByTags(tags);
		for (Tip t : tips)
			System.out.println(t);

		tips = getTipsByUsername("god");
		for (tip t : tips)
			System.out.println(t);

		upvoteTip(1);
		tips = getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		downvoteTip(1);
		tips = getNewTips(5);
		for (Tip t : tips)
			System.out.println(t);

		ArrayList<Comment> comments = getCommentsForTip(1);
		for (Comment c : comments)
			System.out.println(c);

		postTip("Change your oil #car #maintenance", 1);

		postComment(1, "Lol, that's cool", "god");
	}
}
