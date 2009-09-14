package edu.stanford.bmir.protegex.icd;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.smi.protege.model.Frame;

public class HTMLUtil {
	
	 public static String makeHTMLLinks(String text) {
			Matcher matcher = Pattern.compile("(?i)(\\b(http://|https://|www.|ftp://|file:/|mailto:)\\S+)(\\s*)").matcher(text);
			
			if (matcher.find()) {
				String url = matcher.group(1);
				String prefix = matcher.group(2);
				String endingSpaces = matcher.group(3);
				
				Matcher dotEndMatcher = Pattern.compile("([\\W&&[^/]]+)$").matcher(url);

				//Ending non alpha characters like [.,?%] shouldn't be included in the url.
				String endingDots = "";
				if (dotEndMatcher.find()) {
					endingDots = dotEndMatcher.group(1);
					url = dotEndMatcher.replaceFirst("");
				}

				text = matcher.replaceFirst("<a href='" + url + "'>" + url
						+ "</a>" + endingDots + endingSpaces);			
			}
			return text;
		}
	 
	 public static String getDisplayText(Collection values) {
		 StringBuffer buffer = new StringBuffer();
		 if (values == null) { return buffer.toString();}
		 for (Iterator iterator = values.iterator(); iterator.hasNext();) {
			Object object = iterator.next();
			buffer.append(getDisplayText(object));
			buffer.append(", ");
		}
		 if (buffer.length() > 2) {
			 return buffer.substring(0, buffer.length()-2);
		 }
		 return buffer.toString();
	 }
	 
	 
	 public static String getDisplayText(Object value) {		 
		 if (value instanceof Frame) {
			 return ((Frame)value).getBrowserText();
		 } else {
			 return HTMLUtil.makeHTMLLinks(value.toString());
		 }
	 }
	 
	 public static String replaceEOF(String text) {
		 return text.replaceAll("\n", "<br>");
	 }
	
	 public static String replaceSpaces(String text) {
		 return text.replaceAll(" ", "%20");
	 }
}
