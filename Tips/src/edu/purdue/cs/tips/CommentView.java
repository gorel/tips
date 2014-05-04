package edu.purdue.cs.tips;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class CommentView {
	private Context context;
	private Comment comment;
	
	public CommentView(Context context, Comment comment) {
		this.context = context;
		this.comment = comment;
	}
	
	public View display() {
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		EditText text = new EditText(context);
		text.setText(comment.toString());
		text.setEnabled(false);
		
		layout.addView(text);
		//TODO: Make it look good
		return layout;
	}
	
	public static View noResultsView(Context context) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		EditText text = new EditText(context);
		text.setText("No comments to display");
		text.setEnabled(false);
		layout.addView(text);
		return layout;
	}

}
