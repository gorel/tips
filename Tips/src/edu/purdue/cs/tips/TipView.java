package edu.purdue.cs.tips;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.*;

public class TipView {
	private Context context;
	private MainActivity activity;
	private Tip tip;

	public TipView(Context context, MainActivity activity, Tip tip) {
		this.context = context;
		this.activity = activity;
		this.tip = tip;
	}
	
	public View display(boolean displayComments) {
		final TipView thisview = this;
		
		//Create the layout for this tip
		final LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.HORIZONTAL);
		layout.setMinimumHeight(150);
		
		//Create the upvote button
		final ImageButton upvoteButton = new ImageButton(context);
		upvoteButton.setBackgroundColor(context.getResources().getColor(R.color.purdue_gold));
		upvoteButton.setImageResource(R.drawable.ic_launcher);
		
		//Create the downvote button
		final ImageButton downvoteButton = new ImageButton(context);
		downvoteButton.setBackgroundColor(context.getResources().getColor(R.color.purdue_gold));
		downvoteButton.setImageResource(R.drawable.ic_launcher);
		downvoteButton.setRotation(180);
		
		//Create the view for the actual tip
		LinearLayout tipLayout = new LinearLayout(context);
		tipLayout.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, .8f));
		tipLayout.setOrientation(LinearLayout.VERTICAL);
		tipLayout.setBackgroundColor(context.getResources().getColor(R.color.purdue_gold));
		tipLayout.setTag(this);
		
		//Add the text to the tip layout
		TextView text = new TextView(context);
		text.setText(tip.getTip());
		text.setTextColor(Color.BLACK);
		
		TextView date = new TextView(context);
		date.setText(tip.getPostDate() + " by " + activity.getUsername(tip.getUserID()));
		date.setTextColor(Color.BLACK);
		
		TextView karma = new TextView(context);
		karma.setText("Karma: " + tip.getKarma());
		karma.setTextColor(Color.BLACK);
		
		tipLayout.addView(text);
		tipLayout.addView(date);
		tipLayout.addView(karma);
		
		//Add the elements to the layout
		layout.addView(upvoteButton);
		layout.addView(tipLayout);
		layout.addView(downvoteButton);
		
		//Set an on click listener for the upvote button
		upvoteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				upvoteButton.setEnabled(false);
				downvoteButton.setEnabled(true);
				upvoteButton.setBackgroundColor(Color.YELLOW);
				downvoteButton.setBackgroundColor(context.getResources().getColor(R.color.purdue_gold));

				//If the user already downvoted this tip, we have to upvote it twice, effectively
				if (!downvoteButton.isEnabled())
					activity.voteTip(tip.getTipID(), true);
				activity.voteTip(tip.getTipID(), true);
			}
		});
		
		//Set an on click listener for the downvote button
		downvoteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downvoteButton.setEnabled(false);
				upvoteButton.setEnabled(true);
				downvoteButton.setBackgroundColor(Color.RED);
				upvoteButton.setBackgroundColor(context.getResources().getColor(R.color.purdue_gold));

				//If the user already upvoted this tip, we have to downvote it twice, effectively
				if (!upvoteButton.isEnabled())
					activity.voteTip(tip.getTipID(), false);
				activity.voteTip(tip.getTipID(), false);
			}
		});
		
		if (displayComments) {
			//Set an on click listener for the tip body that sends the user to the comments for that tip
			tipLayout.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
					transaction.replace(R.id.container, new MainActivity.CommentsViewFragment(tip.getTipID(), thisview));
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
		}
		
		return layout;
	}
	
	public static View noResultsView(Context context) {
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		EditText text = new EditText(context);
		text.setText("No tips to display");
		text.setEnabled(false);
		
		layout.addView(text);
		return layout;
	}
}