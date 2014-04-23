import java.util.Date;

public class Tip
{
	private int tipID;
	private String tip;
	private Date postDate;
	private int karma;
	private int userID;

	public Tip(int tipID, String tip, String dateString, int karma, int userID)
	{
		this.tipID = tipID;
		this.tip = tip;
		this.postDate = Date(dateString);
		this.karma = karma;
		this.userID = userID;
	}

	public int getTipID()
	{
		return this.tipID;
	}

	public String getTip()
	{
		return this.tip;
	}

	public Date getPostDate()
	{
		return this.postDate;
	}

	public int getKarma()
	{
		return this.karma;
	}

	public int getUserID()
	{
		return this.userID;
	}
}
