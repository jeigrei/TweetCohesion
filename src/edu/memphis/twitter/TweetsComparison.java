package edu.memphis.twitter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TweetsComparison {

	static Map<String, Integer> wordFrequency = new HashMap<String, Integer>();

	// You may want to change the following file paths
//	static String TWEETS_CORPUS_EXCEL_PATH = "C:\\Users\\Jin\\Dropbox\\LAC\\tweets\\TweetsData\\English Libya Tweets.xlsx";
//	static String TWEETS_CORPUS_TXT_PATH = "C:\\Users\\Jin\\Desktop\\New folder\\tweets.txt";
//	static String OUTPUT_EXCEL = "D:\\twitter\\output-syria.xlsx";
//	static String OUTPUT_TXT = "D:\\twitter\\output-syria.txt";
//	static String OUTPUT_ENGLISH_TWEETS_TXT = "D:\\twitter\\syria_tweets_en.txt";
	
	String tweet_corpus_txt_path;
	String output_path;
	String output_path_english_tweets;
	int frameSize;
	boolean slidingFrame;
	
	public TweetsComparison(String tweetPath, String outputPath, String outputPathEnglishTweets, int frameSize, boolean slidingFrame) {
		this.tweet_corpus_txt_path = tweetPath;
		this.output_path = outputPath;
		this.output_path_english_tweets = outputPathEnglishTweets;
		this.frameSize = frameSize;
		this.slidingFrame = slidingFrame;
	}
	
	public void main() {
		//Load tweet corupus from excel file
		//List<Tweet> tweets = TweetUtils.getTweets(TWEETS_CORPUS_EXCEL_PATH);
		//Load tweet corpus from text file
		System.out.println("Starting to load tweets. This could take a while......");
		List<Tweet> tweets = TweetUtils.getTweetsFromTxt(tweet_corpus_txt_path);
		System.out.println("Finished loading tweets.");
		List<Tweet> enTweet = new ArrayList<Tweet>();
		for (Tweet tweet : tweets) {
			if(tweet.getLangCode().equalsIgnoreCase("en")){
				enTweet.add(tweet);
			}			
		}
		//Save all english tweets
		TweetUtils.writeTweetsTxt(enTweet, output_path_english_tweets); 
		
		//count all english tweets' word frequency
		String w[];
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		for (Tweet tweet : tweets) {
			try {
				tweet.setTime(formatter.parse(tweet.getTimeStr()));
			} catch (ParseException e) {
				System.err.println("A tweet time format is wrong: " + tweet.getTimeStr());
			}
			w = tweet.getText().split("\\s+");
			for (String word : w) {
				if (wordFrequency.containsKey(word)) {
					wordFrequency.put(word, wordFrequency.get(word) + 1);
				} else {
					wordFrequency.put(word, 1);
				}
			}
		}
		
		//cohesion computation
		if(slidingFrame){
			List<String[]> results = calculateTweetsSlidingFrame(enTweet, frameSize);
			TweetUtils.writeTxt(results, output_path); 
		} else {
			List<String[]> results = calculateTweets(enTweet, frameSize);
			TweetUtils.writeTxt(results, output_path); 
		}
		//save result to excel file
		//TweetUtils.writeResults(results, OUTPUT_EXCEL);
		// save result to txt file
	}
	
	
	//calculate tweet cohesion group by group, n tweets in a group
	public static List<String[]> calculateTweetsSlidingFrame(List<Tweet> tweets, int n) {		
		List<String[]> results = new ArrayList<String[]>();
		String[][] vs = new String[n][];
		Date startTime;
		Date endTime;
		Tweet startTweet = tweets.get(0);
		Tweet endTweet = tweets.get(n-1);
		long duration;
		String[] temp;
		
		while (true) {
			temp = new String[4];
			startTime = startTweet.getTime();
			//get n tweets
			for(int i = 0; i < n; i++){
				vs[i] = tweets.get(tweets.indexOf(startTweet) + i).getText().split("\\s+");
			}
			endTime = endTweet.getTime();
			duration = (endTime.getTime() - startTime.getTime()) / 1000;
			temp[0] = startTime.toString();
			temp[1] = endTime.toString();
			//time span
			temp[2] = Long.toString(duration);
			//calculate n tweet cohesion value
			temp[3] = Double.toString(cal(vs));
			results.add(temp);
			
			//break if we have reached the end of the list
			if(tweets.indexOf(endTweet) == (tweets.size() - 1)){
				break;
			}
			System.out.println("Calc'ing tweets: " + startTweet.getTimeStr() + " to " + endTweet.getTimeStr());
			startTweet = tweets.get(tweets.indexOf(startTweet) + 1);
			endTweet = tweets.get(tweets.indexOf(endTweet) + 1);
		}
		
		return results;
	}
	
	//calculate tweet cohesion group by group, n tweets in a group
	public static List<String[]> calculateTweets(List<Tweet> tweets, int n) {
		int size = tweets.size();		
		int remainder = size % n;
		System.out.println("Remainder: " + remainder);
		List<String[]> results = new ArrayList<String[]>();
		String[][] vs = new String[n][];
		Date startTime;
		Date endTime;
		long duration;
		String[] temp;
		//for each group of n tweets
		for (int i = 0; i < (tweets.size() - remainder) / n; i++) {
			temp = new String[4];
			startTime = tweets.get(n * i).getTime();
			//get n tweets
			for(int j = 0; j < n; j++){
				vs[j] = tweets.get(n * i + j).getText().split("\\s+");
			}

			endTime = tweets.get(n * i + (n-1)).getTime();
			duration = (endTime.getTime() - startTime.getTime()) / 1000;
			temp[0] = startTime.toString();
			temp[1] = endTime.toString();
			//time span
			temp[2] = Long.toString(duration);
			//calculate n tweet cohesion value
			temp[3] = Double.toString(cal(vs));
			results.add(temp);
		}
		//calculate the remaining tweets' cohesion value
		String[][] last = new String[remainder][];
		for (int i = 0; i < remainder; i++) {
			last[i] = tweets.get(size - remainder + i).getText().split("\\s+");
		}
		if (remainder > 0) {
			temp = new String[4];
			startTime = tweets.get(size - remainder).getTime();
			endTime = tweets.get(size - 1).getTime();
			duration = (endTime.getTime() - startTime.getTime()) / 1000;
			temp[0] = startTime.toString();
			temp[1] = endTime.toString();
			temp[2] = duration + "";
			temp[3] = cal(last) + "";
			results.add(temp);
		}
		return results;
	}

	//calculate a group of text's cohesion value
	public static double cal(String[]... vs) {
		double r = 0.0;
		int count = 0;
		for (int i = 0; i < vs.length; i++) {
			for (int j = i + 1; j < vs.length; j++) {
				r = r + getSimilarityValue(vs[i], vs[j], true);
				count++;
			}
		}
		return r / count;
	}

	// Algorithm which used to compare similarity of two word arrays
	public static double getSimilarityValue(String[] w1, String[] w2,
			boolean applyWeight) {
		Map<String, Integer> matrix = new HashMap<String, Integer>();
		Set<String> w1Set = new HashSet<String>();
		Set<String> sameWord = new HashSet<String>();
		for (String w : w1) {
			w1Set.add(w);
			if (matrix.containsKey(w)) {
				matrix.put(w, matrix.get(w) + 1);
			} else {
				matrix.put(w, 1);
			}
		}
		for (String w : w2) {
			if (w1Set.contains(w)) {
				sameWord.add(w);
			}
			if (matrix.containsKey(w)) {
				matrix.put(w, matrix.get(w) + 1);
			} else {
				matrix.put(w, 1);
			}
		}
		double lower = 0.0;
		for (String term : matrix.keySet()) {
			lower = lower + Math.log(matrix.get(term) + 1)
					/ Math.log(wordFrequency.get(term) + 1);
		}
		double upper = 0.0;
		for (String term : sameWord) {
			upper = upper + Math.log(matrix.get(term) + 1)
					/ Math.log(wordFrequency.get(term) + 1);
		}
		return (upper / lower);
	}
}