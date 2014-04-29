package edu.purdue.cs.tips;

import android.support.v7.app.*;
import android.support.v4.app.*;
import android.content.Context;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import java.security.*;
import java.util.ArrayList;
import java.util.concurrent.*;

public class MainActivity extends ActionBarActivity {
	private ServerConnection conn;
	private ExecutorService service;
	
	private MessageDigest hasher;
	private String username;
	private int userID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction().add(R.id.container, new LoginFragment()).commit();
		}
		
		try {
			hasher = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		conn = new ServerConnection("data.cs.purdue.edu", 9312, 9314);
		service = Executors.newFixedThreadPool(1);
	}
	
	/**
	 * Get the SHA-1 hash of s
	 * @param s the string to hash
	 * @return a hashed string
	 */
	public String hash(String s) {
		hasher.update(s.getBytes());
		byte[] bytes = hasher.digest();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		
		return sb.toString();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A login fragment containing a simple view.
	 * @author Evan Arnold
	 */
	public static class LoginFragment extends Fragment {
		public LoginFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

			Button registerButton = (Button)rootView.findViewById(R.id.register_button);
			Button loginButton = (Button)rootView.findViewById(R.id.login_button);
			
			//Create a listener for the register button
			registerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					TextView statusText = (TextView)rootView.findViewById(R.id.status); 
					statusText.setVisibility(View.GONE);
					
					String username = ((EditText)rootView.findViewById(R.id.username_input)).getText().toString();
					
					FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
					RegisterFragment fragment = new RegisterFragment();
					fragment.setUsername(username);
					transaction.replace(R.id.container, fragment);
					transaction.commit();
				}
			});
			
			//Create a listener for the login button
			loginButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					TextView statusText = (TextView)rootView.findViewById(R.id.status);
					statusText.setVisibility(View.GONE);
					
					String username = ((EditText)rootView.findViewById(R.id.username_input)).getText().toString();
					String password = ((EditText)rootView.findViewById(R.id.password_input)).getText().toString();
					int status = ((MainActivity) getActivity()).login(username, password);
					
					statusText.setVisibility(View.VISIBLE);
					if (status == -2)
						statusText.setText("Username already taken");
					else if (status == -1)
						statusText.setText("Error connecting to server");
					else {
						FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
						transaction.replace(R.id.container, new TipsViewFragment());
						transaction.commit();
					}
				}
			});
			
			return rootView;
		}
	}
	
	/**
	 * A register fragment containing a simple view
	 * @author Evan Arnold
	 *
	 */
	public static class RegisterFragment extends Fragment {
		private String username;
		
		public RegisterFragment() {
			username = null;
		}
		
		public void setUsername(String username) {
			this.username = username;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_register, container, false);
			
			if (username != null) {
				((EditText)rootView.findViewById(R.id.username_input)).setText(username);
			}

			Button registerButton = (Button)rootView.findViewById(R.id.register_button);
			
			//Create a listener for the register button
			registerButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					TextView statusText = (TextView)rootView.findViewById(R.id.status); 
					statusText.setVisibility(View.GONE);
					
					String username = ((EditText)rootView.findViewById(R.id.username_input)).getText().toString();
					String password = ((EditText)rootView.findViewById(R.id.password_input)).getText().toString();
					String confirm  = ((EditText)rootView.findViewById(R.id.confirm_password_input)).getText().toString();
					
					statusText.setVisibility(View.VISIBLE);
					
					if (!password.equals(confirm)) {
						statusText.setText("Passwords do not match");
						return;
					}
					
					int status = ((MainActivity) getActivity()).createAccount(username, password);
					
					if (status == -1)
						statusText.setText("No server response");
					else {
						FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
						transaction.replace(R.id.container, new TipsViewFragment());
						transaction.commit();
					}
				}
			});
			
			return rootView;
		}
	}
	
	/**
	 * A tips view fragment containing a simple view
	 * @author Evan Arnold
	 *
	 */
	public static class TipsViewFragment extends Fragment {
		private final int TIPLIMIT = 25;
		private LinearLayout tipsView;
		
		public TipsViewFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_tips_list, container, false);
			
			tipsView = (LinearLayout)rootView.findViewById(R.id.tips_view);
			
			Button searchUsernameButton = (Button)rootView.findViewById(R.id.search_by_username_button);
			Button searchTagsButton = (Button)rootView.findViewById(R.id.search_by_tags_button);
			Button postTipButton = (Button)rootView.findViewById(R.id.post_tip_button);
			
			//Create a listener for the register button
			searchUsernameButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Search by username clicked!");
					String username = ((EditText)rootView.findViewById(R.id.search_by_username)).getText().toString();
					ArrayList<Tip> tips = ((MainActivity)getActivity()).getTipsByUsername(username);
					loadTips(tips);
				}
			});
			
			//Create a listener for the search by tags button
			searchTagsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Search by tags clicked!");
					String[] tags = ((EditText)rootView.findViewById(R.id.search_by_tags)).getText().toString().replaceAll("#", "").split(" ");
					ArrayList<Tip> tips = ((MainActivity)getActivity()).getTipsByTags(tags);
					loadTips(tips);
				}
			});
			
			//Create a listener for the post tip button
			postTipButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
					transaction.replace(R.id.container, new PostTipFragment());
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			ArrayList<Tip> tips = ((MainActivity)getActivity()).getNewTips(TIPLIMIT);
			loadTips(tips);
			
			return rootView;
		}
		
		/**
		 * Load the ArrayList of tips into the view
		 * @param tips the list of tips to load
		 */
		private void loadTips(ArrayList<Tip> tips) {	
			//TODO: Remove old tips
			if (tips == null) {
				Log.d("help", "Tips returned null!");
				tipsView.addView(TipView.noResultsView(getActivity().getApplicationContext()));
				return;
			}
			
			for (Tip tip : tips) {
				tipsView.addView(tip.toView(getActivity().getApplicationContext(), (MainActivity)getActivity()).display());
			}
		}
	}

	/**
	 * A comments view fragment containing a simple view
	 * @author Evan Arnold
	 */
	public static class CommentsViewFragment extends Fragment {
		private LinearLayout commentsView;
		private int tipID;
		
		public CommentsViewFragment(int tipID) {
			this.tipID = tipID;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_comment_view, container, false);
			
			commentsView = (LinearLayout)rootView.findViewById(R.id.comments_view);
			
			Button postCommentButton = (Button)rootView.findViewById(R.id.post_comment_button);
			
			//Create a listener for the post comment button
			postCommentButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try{
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e){
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Post comment clicked!");
					FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
					PostCommentFragment fragment = new PostCommentFragment(tipID);
					transaction.replace(R.id.container, fragment);
					transaction.addToBackStack(null);
					transaction.commit();
				}
			});
			
			ArrayList<Comment> comments = ((MainActivity)getActivity()).getCommentsForTip(tipID);
			loadComments(comments);
			
			return rootView;
		}
	
		/**
		 * Load the ArrayList of comments into the view
		 * @param comments the list of comments to load
		 */
		private void loadComments(ArrayList<Comment> comments) {
			if (comments == null) {
				Log.d("help", "Comments returned null!");
				commentsView.addView(CommentView.noResultsView(getActivity().getApplicationContext()));
				return;
			}
			
			for (Comment comment : comments) {
				Log.d("help", "Loading comment: " + comment.toString());
				commentsView.addView(comment.toView(getActivity().getApplicationContext()).display());
			}
		}
	}

	/**
	 * A post tip fragment containing a simple view
	 * @author Evan Arnold
	 */
	public static class PostTipFragment extends Fragment {
		public PostTipFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_post_tip, container, false);
			
			final EditText tipInput = (EditText)rootView.findViewById(R.id.tip_input);
			final Button postTipButton = (Button)rootView.findViewById(R.id.post_tip_button);
			final ImageButton backButton = (ImageButton)rootView.findViewById(R.id.back_button);
			
			//Create a listener for the post tip button
			postTipButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Post tip clicked!");
					((MainActivity)getActivity()).postTip(tipInput.getText().toString());
					getFragmentManager().popBackStackImmediate();
				}
			});

			//Create a listener for the back button
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Back button clicked!");
					getFragmentManager().popBackStackImmediate();
				}
			});
			
			return rootView;
		}
	}

	/**
	 * A post comment fragment containing a simple view
	 * @author Evan Arnold
	 */
	public static class PostCommentFragment extends Fragment {
		private int tipID;
		public PostCommentFragment(int tipID) {
			this.tipID = tipID;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			final View rootView = inflater.inflate(R.layout.fragment_post_comment, container, false);
			
			final EditText commentInput = (EditText)rootView.findViewById(R.id.comment_input);
			final Button postCommentButton = (Button)rootView.findViewById(R.id.post_comment_button);
			final ImageButton backButton = (ImageButton)rootView.findViewById(R.id.back_button);
			
			//Create a listener for the post comment button
			postCommentButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Post comment clicked!");
					((MainActivity)getActivity()).postComment(tipID, commentInput.getText().toString());
					getFragmentManager().popBackStackImmediate();
				}
			});

			//Create a listener for the back button
			backButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						InputMethodManager inputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
						inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
					} catch (Exception e) {
						//TODO: Why is this happening?
						Log.e(e.getClass().getName(), e.getMessage(), e);
					}
					
					Log.d("help", "Back button clicked!");
					getFragmentManager().popBackStackImmediate();
				}
			});
			
			return rootView;
		}
	}
	
	/**
	 * Create an account
	 * @param username the username to create the account with
	 * @param password the password to create the account with
	 * @return the new account's user_id
	 */
	public int createAccount(final String username, final String password) {
		final String hashedPassword = hash(password);
		Future<Integer> task = service.submit(new Callable<Integer>(){
			public Integer call() {
				return conn.createAccount(username, hashedPassword);
			}
		});
		
		//Display loading symbol
		View loadingSymbol = findViewById(R.id.loading_symbol);
		loadingSymbol.setVisibility(View.VISIBLE);
		
		try {
			this.username = username;
			userID = task.get();
			loadingSymbol.setVisibility(View.GONE);
			return userID;
		} catch (Exception e) {
			userID = -1;
			return -1;
		}
	}
	
	/**
	 * Attempt to login
	 * @param username the username to login with
	 * @param password the password to login with
	 * @return the user's user_id
	 */
	public int login(final String username, final String password) {
		final String hashedPassword = hash(password);
		Future<Integer> task = service.submit(new Callable<Integer>(){
			public Integer call() {
				return conn.login(username, hashedPassword);
			}
		});
		
		//Display loading symbol
		View loadingSymbol = findViewById(R.id.loading_symbol);
		loadingSymbol.setVisibility(View.VISIBLE);
		
		try {
			this.username = username;
			userID = task.get();
			loadingSymbol.setVisibility(View.GONE);
			return userID;
		} catch (Exception e) {
			userID = -1;
			return -1;
		}
	}
	
	/**
	 * Get a list of <limit> new tips
	 * @param limit the maximum number of tips to return
	 * @return an arraylist of Tip objects
	 */
	public ArrayList<Tip> getNewTips(final int limit) {
		Log.d("help", "Requested " + limit + " new tips");
		
		Future<ArrayList<Tip>> task = service.submit(new Callable<ArrayList<Tip>>(){
			public ArrayList<Tip> call() {
				return conn.getNewTips(limit);
			}
		});
		
		try {
			return task.get();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get a list of <limit> new tips
	 * @param tags the tags to filter by
	 * @return an arraylist of Tip objects
	 */
	public ArrayList<Tip> getTipsByTags(final String[] tags) {
		for (String tag : tags)
			Log.d("help", "Requested tips with tag: " + tag);
		
		Future<ArrayList<Tip>> task = service.submit(new Callable<ArrayList<Tip>>(){
			public ArrayList<Tip> call() {
				return conn.getTipsByTags(tags);
			}
		});
		
		try {
			return task.get();
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get a list of <limit> new tips
	 * @param username the username to filter by
	 * @return an arraylist of Tip objects
	 */
	public ArrayList<Tip> getTipsByUsername(final String username) {
		Log.d("help", "Requested tips by " + username);
		
		Future<ArrayList<Tip>> task = service.submit(new Callable<ArrayList<Tip>>(){
			public ArrayList<Tip> call() {
				return conn.getTipsByUsername(username);
			}
		});
		
		try {
			return task.get();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get a list of <limit> new tips
	 * @param tipID the tipID to vote
	 * @param upvote whether this is an upvote or downvote
	 * @return true on successful vote
	 */
	public boolean voteTip(final int tipID, final boolean upvote) {
		Log.d("help", "Voting tip " + tipID + (upvote ? " up" : " down"));
		
		Future<Boolean> task = service.submit(new Callable<Boolean>(){
			public Boolean call() {
				return conn.voteTip(tipID, upvote);
			}
		});
		
		try {
			return task.get();
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * Get a list of <limit> new tips
	 * @param tipID the tipID to get comments for
	 * @return an arraylist of Comments
	 */
	public ArrayList<Comment> getCommentsForTip(final int tipID) {
		Log.d("help", "Getting comments for tip " + tipID);
		
		Future<ArrayList<Comment>> task = service.submit(new Callable<ArrayList<Comment>>(){
			public ArrayList<Comment> call() {
				return conn.getCommentsForTip(tipID);
			}
		});
		
		try {
			ArrayList<Comment> comments = task.get();
			return comments;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Post a new tip
	 * @param tip the tip to post
	 * @return whether or not the tip was successfully posted
	 */
	public boolean postTip(final String tip) {
		Log.d("help", "Creating new tip: " + tip);
		service.submit(new Callable<Boolean>(){
			public Boolean call() {
				return conn.postTip(tip, userID);
			}
		});
		
		//Assume the tip was posted
		return true;
	}
	
	/**
	 * Post a new comment to a tip
	 * @param tipID the tip to post a comment to
	 * @param comment the comment to post
	 * @return whether or not the comment was successfully posted
	 */
	public boolean postComment(final int tipID, final String comment) {
		Log.d("help", "Posting new comment: " + comment + " to tipID " + tipID);
		
		service.submit(new Callable<Boolean>(){
			public Boolean call() {
				return conn.postComment(tipID, comment, username);
			}
		});
		
		//Assume the comment was posted
		return true;
	}
}
