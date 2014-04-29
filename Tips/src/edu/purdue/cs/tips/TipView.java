package edu.purdue.cs.tips;

import android.content.Context;
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
	
	public View display() {
		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView text = new TextView(context);
		text.setText(tip.toString());
		text.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
				transaction.replace(R.id.container, new MainActivity.CommentsViewFragment(tip.getTipID()));
				transaction.addToBackStack(null);
				transaction.commit();
			}
		});
		
		
		layout.addView(text);
		//TODO: Make it look good
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
