package edu.purdue.cs.tips;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class CommentView {
	private Context context;
	private String poster;
	private String postDate;
	private String comment;
	
	public CommentView(Context context, Comment comment) {
		this.context = context;
		
		this.poster = comment.getPoster();
		this.postDate = comment.getPostString();
		this.comment = comment.getComment();
	}
	
	public View display() {
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		//TODO: Make it look good
		return layout;
	}
	
	public static View noResultsView(Context context) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		//TODO: Display "No results" or something
		return layout;
	}

}
