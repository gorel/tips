package edu.purdue.cs.tips;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class TipView {
	private Context context;
	private int tipID;
	private String tip;
	private String postDate;
	private int karma;
	private int userID;

	public TipView(Context context, Tip tip) {
		this.context = context;
		
		this.tipID = tip.getTipID();
		this.tip = tip.getTip();
		this.postDate = tip.getPostDate();
		this.karma = tip.getKarma();
		this.userID = tip.getUserID();
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
