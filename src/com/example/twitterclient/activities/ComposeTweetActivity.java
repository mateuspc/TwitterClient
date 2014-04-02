package com.example.twitterclient.activities;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.twitterclient.R;
import com.example.twitterclient.activities.helpers.Constants;
import com.example.twitterclient.activities.helpers.TwitterClientApp;
import com.example.twitterclient.activities.models.Tweet;
import com.example.twitterclient.activities.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

public class ComposeTweetActivity extends Activity {
	ImageView ivUserProfile;
	TextView tvUserName;
	TextView tvScreenName;
	TextView tvCharacterCount;
	EditText etTweetText;
	private static final String PADDING = "  ";
	MenuItem miTweet; 
	Tweet refTweet;
	String screenname;
	int action;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		refTweet = (Tweet)getIntent().getSerializableExtra("tweet");
		action = getIntent().getIntExtra("action", 0);
		screenname = getIntent().getStringExtra("screenname");
		setContentView(R.layout.activity_compose_tweet);
		setViews();
		setUserInfo();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void setViews() {
		ivUserProfile = (ImageView) findViewById(R.id.ivUserProfile);
		tvUserName = (TextView) findViewById(R.id.tvUserName);
		tvScreenName = (TextView) findViewById(R.id.tvScreenName);
		etTweetText = (EditText) findViewById(R.id.etTweetText);
		if (refTweet != null || screenname != null) {
			String text = "";
			if (action == Constants.ACTION_REPLY) {
				if (screenname != null) {
					text = "@" + screenname;
				} else {
					final User user = User.getUser(refTweet.userId);
					text = "@" + user.screenName;
				}
			} else if (action == Constants.ACTION_RETWEET) {
				if (refTweet != null) {
					text = refTweet.body;
				}
			}
			etTweetText.setText(text);
			etTweetText.setSelection(text.length());
		}
		textDidChange();
	}
	
	private void setUserInfo() {
		User currentUser = TwitterClientApp.getCurrentUser();
		if(currentUser != null) {
			ImageLoader mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(ImageLoaderConfiguration.createDefault(ComposeTweetActivity.this));
			mImageLoader.displayImage(currentUser.profileImageUrl, ivUserProfile);
			
			tvUserName.setText(currentUser.name);
			tvScreenName.setText("@" + currentUser.screenName);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compose_tweet, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		final MenuItem miCharacterCount = menu.findItem(R.id.miCharacterCount); 
		miTweet = menu.findItem(R.id.miTweet);
		miCharacterCount.setEnabled(false);
		miTweet.setEnabled(false);
		tvCharacterCount = (TextView) miCharacterCount.getActionView();
		etTweetText.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {
					
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence text, int arg1, int arg2,
					int arg3) {
				textDidChange();
			}});
		textDidChange();
		return super.onPrepareOptionsMenu(menu);
	}
	
	public void textDidChange() {
		if (miTweet == null || tvCharacterCount == null || etTweetText == null) return;
		final String tweetText = etTweetText.getText().toString();
		if(tweetText != null && tweetText.length() > 0) {
			int remainingChars = Constants.MAX_CHARS - tweetText.length();
			tvCharacterCount.setText(Integer.toString(remainingChars) + " " + PADDING);
			if(remainingChars <= 0 || remainingChars == Constants.MAX_CHARS) {
				miTweet.setEnabled(false);
			}
			else {
				miTweet.setEnabled(true);
			}
		} else {
			tvCharacterCount.setText("140" + PADDING);
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    	case android.R.id.home:
            // app icon in action bar clicked; goto parent activity.
            this.finish();
            return true;
	        case R.id.miTweet:
	        	String tweetText = etTweetText.getText().toString();
	            if(tweetText != null && tweetText.length() > 0) {
	            	TwitterClientApp.getClient().updateStatus(tweetText, new JsonHttpResponseHandler() {
	            		
	            		@Override
	            		public void onSuccess(JSONObject jsonObject) {
	            			Intent data = new Intent();
	            			data.putExtra("tweet", Tweet.fromJson(jsonObject));
	            			setResult(RESULT_OK, data);
	            			finish();
	            		}
	            		
	            		@Override
	            		public void onFailure(Throwable e) {
	            			e.printStackTrace();
	            		}
	            		
	            	});
	            }
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

}
