import java.util.Date;

public class Comment
{
	private String poster;
	private Date postDate;
	private String comment;

	public Comment(String poster, String dateString, String comment)
	{
		this.poster = poster;
		this.postDate = Date(dateString);
		this.comment = comment;
	}

	public String getPoster()
	{
		return this.poster;
	}

	public Date getPostDate()
	{
		return this.postDate;
	}

	public String getComment()
	{
		return this.comment;
	}
}
