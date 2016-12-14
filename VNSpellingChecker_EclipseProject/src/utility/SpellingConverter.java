package utility;

import java.util.ArrayList;

import fileio.FileIOSpelling;

public class SpellingConverter {
	public static String convertToHTML(ArrayList<String> lstSentences) {
		String htmlStr = "";
		htmlStr += FileIOSpelling.HTML_FIRST + "\r\n";
		for (String str : lstSentences) {
			htmlStr += str + "\r\n";
		}
		htmlStr += FileIOSpelling.HTML_LAST + "\r\n";
		return htmlStr;
	}

	public static String convertToHTML(String lstSentences) {
		return FileIOSpelling.HTML_FIRST + "\r\n" + lstSentences + FileIOSpelling.HTML_LAST + "\r\n";
	}
}
