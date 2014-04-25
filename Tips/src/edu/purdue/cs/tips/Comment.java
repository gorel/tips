public class Comment
{
	private String poster;
	private String postDate;
	private String comment;

	public Comment(String poster, String postDate, String comment)
	{
		this.poster = poster;
		this.postDate = postDate;
		this.comment = comment;
	}

	public String getPoster()
	{
		return this.poster;
	}

	public String getPostString()
	{
		return this.postDate;
	}

	public String getComment()
	{
		return this.comment;
	}

	public String toString()
	{
		return 	"poster:\t" + poster + "\n" +
			"postDate\t" + postDate + "\n" +
			"comment:\t" + comment + "\n";
	}
}
