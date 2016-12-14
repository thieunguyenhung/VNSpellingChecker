package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import edu.stanford.nlp.ling.WordTag;
import thirdparty.JazzySpell;
import utility.SpellingConverter;
import vn.hus.nlp.tagger.VietnameseMaxentTagger;

public class VietnameseSpellingTool {
	private int errorCounter;

	public VietnameseSpellingTool() {
		errorCounter = 0;
	}

	public void resetError() {
		errorCounter = 0;
	}

	public int getErrorCounter() {
		return errorCounter;
	}

	/*-
	public static void checkSentenceUsenGram(ArrayList<String> sentences, int nGram) {
		// Loop all sub-sentence in sentence
		for (String str : sentences) {
			str = str.trim();
			ArrayList<String> lstWords = new
			// Loop all word in the sub-sentence
			ArrayList<>(Arrays.asList(str.split(" ")));
			for (int i = 0; i < lstWords.size(); i++) {
				String wordToCheck = lstWords.get(i); // Loop for n-gram
				for (int j = 1; j < nGram; j++) {
					wordToCheck += " " + lstWords.get(i + j);
				}
				System.out.println(wordToCheck + " : " + new JazzySpell().getSuggestions(wordToCheck)); //
				System.out.println(wordToCheck);
				// If this is the last possible work then break
				if (lstWords.size() - nGram == i)
					break;
			}
		}
	}*/

	private static boolean isAcronym(String word) {
		return word.matches("[0-9]*[A-Z0-9]+[0-9]*");
	}

	private static String markWord(String w1, ArrayList<String> lstSuggestions, String comboId) {
		String result = "";
		if (lstSuggestions.size() > 0) {
			result += "<div class='dropdown'><span class='mark' id='" + comboId + "'>" + w1 + "</span><div class='dropdown-content'>";
			result += "<a onclick=\"changeWord('" + comboId + "','" + w1 + "')\">" + w1 + "</a>";
			for (String str : lstSuggestions) {
				result += "<a onclick=\"changeWord('" + comboId + "','" + str + "')\">" + str + "</a>";
			}
			result += "</div></div> ";
		} else
			result += "<span class='mark'>" + w1 + "</span> ";
		return result;
	}

	public String checkSentenceCombined(JazzySpell jazzyChecker, String sentence) {
		String regex = "[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽếềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ]+";
		sentence = sentence.trim(); // cut white space first and last if exist
		sentence = sentence.replaceAll("[" + "\\" + "x5B" + "\\" + "x5D(){}<>`\"\']", " ");/*-remove all |<> {} [] () ` " '| char*/
		ArrayList<String> lstWords = new ArrayList<>(Arrays.asList(sentence.split(" ")));
		sentence = ""; // now sentence will store result
		for (int i = 0; i < lstWords.size(); i++) {
			String word1 = lstWords.get(i);
			/*- CHECK if w1 can handle, if not skip to next loop*/
			if (!word1.matches(regex)) {
				sentence += word1 + " ";
				continue;
			}
			/*- CHECK if word1 start with uppercase*/
			if (Character.isUpperCase(word1.codePointAt(0))) {
				// check for acronym word
				if (isAcronym(word1)) {
					sentence += word1 + " ";
					continue;
				}
				String pronouns = "";
				int count = 0;
				// loop until next word not start with upper
				for (int k = i + 1; k < lstWords.size(); k++) {
					String w = lstWords.get(k);
					if (!Character.isUpperCase(w.codePointAt(0)))
						break;
					pronouns += w + " ";
					count += 1;
				}
				// if 2 or more word start with upper, skip to next loop
				if (count > 0) {
					sentence += word1 + " " + pronouns;
					i += count;
					continue;
				}
				// Check if word1 is proper noun
				else {
					VietnameseMaxentTagger tagger = new VietnameseMaxentTagger();
					List<WordTag> lstTagged = tagger.tagText2(word1);
					if (getWord(lstTagged.get(lstTagged.size() - 1).toString()).get(1).equals("Np")) {
						sentence += word1 + " ";
						continue;
					}
				}
			}
			/*- CHECK if last word in sentence*/
			if (i == lstWords.size() - 1) {
				// check if w1 exist in dic
				ArrayList<String> word1Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase());
				if (word1Suggestions.contains(word1.toLowerCase())) {
					sentence += word1;
				} else {// MAYBE w1 incorrect
					sentence += markWord(word1, word1Suggestions, "comboWord" + errorCounter);
					errorCounter += 1;
				}
				break; // go (S3)
			}
			i += 1;
			String word2 = lstWords.get(i);
			/*- CHECK if w2 can handle, if not skip to next loop*/
			if (!word2.matches(regex)) {
				ArrayList<String> word1Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase());
				if (word1Suggestions.contains(word1.toLowerCase()))
					sentence += word1 + " ";
				else // MAYBE w1 incorrect
				{
					sentence += markWord(word1, word1Suggestions, "comboWord" + errorCounter);
					errorCounter += 1;
				}
				sentence += word2 + " ";
				continue;
			}
			/*- CHECK if w1 + w2 exist in dic*/
			if (jazzyChecker.getSuggestions(word1.toLowerCase() + " " + word2.toLowerCase()).contains(word1.toLowerCase() + " " + word2.toLowerCase())) {
				sentence += word1 + " " + word2 + " ";/*-If w1 + w2 exist then it's correct, go (S3)*/
			} else { /*- If not, we need word3 to check w2 + w3*/
				i += 1;
				/*-CHECK if w3 is available*/
				if (i >= lstWords.size()) {
					// if index = size then w1+w2 MAYBE incorrect, go (S3)
					ArrayList<String> word12Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase() + " " + word2.toLowerCase());
					sentence += markWord(word1 + " " + word2, word12Suggestions, "comboWord" + errorCounter);
					errorCounter += 1;
					break;
				}
				String word3 = lstWords.get(i);
				/*- CHECK if w3 can handle, if not skip to next loop*/
				if (!word3.matches(regex)) {
					ArrayList<String> word12Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase() + " " + word2.toLowerCase());
					sentence += markWord(word1 + " " + word2, word12Suggestions, "comboWord" + errorCounter) + word3 + " ";
					errorCounter += 1;
					continue;
				}
				if (jazzyChecker.getSuggestions(word2.toLowerCase() + " " + word3.toLowerCase()).contains(word2.toLowerCase() + " " + word3.toLowerCase())) {
					/*- if w2 + w3 exist then we only need to check if w1 exist in dic*/
					ArrayList<String> word1Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase());
					if (word1Suggestions.contains(word1.toLowerCase())) {
						/*- if w1 exist then w1 + w2 + w3 is correct, go (S3)*/
						sentence += word1 + " " + word2 + " " + word3 + " ";
					} else { // MAYBE w1 incorrect, go (S3)
						sentence += markWord(word1, word1Suggestions, "comboWord" + errorCounter);
						errorCounter += 1;
						sentence += word2 + " " + word3 + " ";
					}
				} else { // CHECK if word 1+2+3 exist
					String w123 = word1 + " " + word2 + " " + word3;
					ArrayList<String> word123Suggestions = jazzyChecker.getSuggestions(w123.toLowerCase());
					if (word123Suggestions.contains(w123.toLowerCase()))
						sentence += word1 + " " + word2 + " " + word3 + " ";
					else { // CHECK if w1 exist in dic
						ArrayList<String> word1Suggestions = jazzyChecker.getSuggestions(word1.toLowerCase());
						if (word1Suggestions.contains(word1.toLowerCase())) {
							sentence += word1 + " ";
						} else { // MAYBE w1 incorrect
							sentence += markWord(word1, word1Suggestions, "comboWord" + errorCounter);
							errorCounter += 1;
						}
						i -= 2; // continue loop from w2, go (S3)
					}
				}
			}
		}
		// S3: return result
		return sentence.trim();
	}

	public static String checkSentenceUsingTagger(JazzySpell jazzyChecker, int subSenId, String sentence) {
		String unicodeRegex = "[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂưăạảấầẩẫậắằẳẵặẹẻẽếềềểỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ]+";
		sentence = sentence.trim(); // cut white space first and last if exist
		VietnameseMaxentTagger a = new VietnameseMaxentTagger();
		List<WordTag> lstTagged = a.tagText2(sentence);
		sentence = "";// now sentence will store result
		for (int i = 0; i < lstTagged.size(); i++) {
			String comboId = "comboWord" + subSenId + i;
			WordTag wordTag = lstTagged.get(i);
			ArrayList<String> lstWord = getWord(wordTag.toString());
			String word = lstWord.get(0);
			String tagName = lstWord.get(1);
			if (!isProcessable(tagName) || !word.matches(unicodeRegex)) {
				if (word.matches("[" + "\\" + "x5b(<{]"))
					sentence += word;
				else if (word.matches("[" + "\\" + "x5d)>}]"))
					sentence = sentence.substring(0, sentence.length() - 1) + word + " ";
				else
					sentence += word + " ";
				continue;
			}
			ArrayList<String> wordSuggestions = jazzyChecker.getSuggestions(word.toLowerCase());
			/*- word exist in dict*/
			if (wordSuggestions.contains(word.toLowerCase())) {
				sentence += word + " ";
				continue;
			}
			/*- word not exist, mark word*/
			sentence += markWord(word, wordSuggestions, comboId);
		}
		return sentence.trim();
	}

	private static boolean isProcessable(String tagName) {
		ArrayList<String> lstProcessableTag = new ArrayList<>(Arrays.asList(new String[] { "Nc", "Nu", "N", "V", "A", "P ", "R", "L", "E", "C", "CC", "I", "T", "Z" }));
		if (lstProcessableTag.contains(tagName))
			return true;
		return false;
	}

	private static ArrayList<String> getWord(String word) {
		ArrayList<String> result = new ArrayList<>();
		result.add(word.substring(0, word.lastIndexOf('/')));
		result.add(word.substring(word.lastIndexOf('/') + 1, word.length()));
		return result;
	}

	public String checkSpelling(JazzySpell jazzyChecker, String paragraph) {
		long start = System.nanoTime();
		ArrayList<String> lstParagraphs = new ArrayList<>(Arrays.asList(paragraph.split("\n")));
		paragraph = ""; // now paragraph will store the result
		for (String para : lstParagraphs) {
			// split to sentences
			ArrayList<String> lstSentences = new ArrayList<>(Arrays.asList(para.split("[.]")));
			for (String sentence : lstSentences) {
				// split to sub-sentences
				ArrayList<String> lstSubSentences = new ArrayList<>(Arrays.asList(sentence.split("[,]")));
				for (int i = 0; i < lstSubSentences.size(); i++) {
					String subSen = lstSubSentences.get(i).trim();
					// start checking sub-sentence
					if (subSen.length() > 0 && !subSen.isEmpty()) {
						if (i > 0) {
							paragraph += ", ";
						}
						paragraph += checkSentenceCombined(jazzyChecker, subSen);
					}
				}
				// paragraph = paragraph.replaceFirst("[,]", "");
				if (sentence.length() > 0 && !sentence.trim().isEmpty())
					paragraph += ". ";
			}
			paragraph += "\n<br>";
		}
		paragraph = paragraph.trim();
		long elapsedTime = System.nanoTime() - start;
		System.out.println("\n Elapsed time:" + TimeUnit.MILLISECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS));
		return SpellingConverter.convertToHTML(paragraph);
	}

	/*-	public static void main(String[] args) {
			System.out.println(isProcessable("X"));
		}*/
}
