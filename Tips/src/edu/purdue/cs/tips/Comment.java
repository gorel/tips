package edu.purdue.cs.tips;

import java.util.String;

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
		return this.postString;
	}

	public String getComment()
	{
		return this.comment;
	}
}
