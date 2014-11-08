package edu.memphis.twitter;

import java.util.Date;

public class Tweet {
	
	String text;
	boolean isArabic;
	String langCode;
	Date time;
	String timeStr;
	double[] vector;
	
	public double[] getVector() {
		return vector;
	}
	public void setVector(double[] vector) {
		this.vector = vector;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public boolean isArabic() {
		return isArabic;
	}
	public void setArabic(boolean isArabic) {
		this.isArabic = isArabic;
	}
	public String getLangCode() {
		return langCode;
	}
	public void setLangCode(String langCode) {
		this.langCode = langCode;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public String getTimeStr() {
		return timeStr;
	}
	public void setTimeStr(String timeStr) {
		this.timeStr = timeStr;
	}
	
}
