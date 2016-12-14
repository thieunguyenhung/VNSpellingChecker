package fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import utility.Encryptor;

public class FileIOSpelling {
	public static final String HTML_FIRST = "<!DOCTYPE html><html><head><style>.mark{background-color: #FFFF00;}.dropdown{display: inline-block;}.dropdown-content{overflow-y: auto; max-height: 250px; display: none; position: absolute; background-color: #f9f9f9; min-width: 160px; box-shadow: 0px 8px 16px 0px rgba(0,0,0,0.2);}.dropdown-content a{color: black; padding: 12px 16px; text-decoration: none; display: block;}.dropdown-content a:hover{background-color: #f1f1f1}.dropdown:hover .dropdown-content{display: block;}</style><script>function changeWord(comboId,newText){document.getElementById(comboId).innerHTML=newText;}</script></head><body>";
	public static final String HTML_LAST = "</body></html>";

	/**
	 * Add file extendtion
	 * 
	 * @param fileName
	 *            String file's name
	 * @param mode
	 *            int <br>
	 *            1: txt file<br>
	 *            2: html file<br>
	 *            3: vsc file
	 */
	private static String standardizedFileName(String fileName, int mode) {
		switch (mode) {
		case 1:
			if (!fileName.endsWith(".txt"))
				fileName += ".txt";
			break;
		case 2:
			if (!fileName.endsWith(".html"))
				fileName += ".html";
			break;
		case 3:
			if (!fileName.endsWith(".vsc"))
				fileName += ".vsc";
			break;
		}
		return fileName;
	}

	public static String readFile(String path) {
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
			StringBuilder builder = new StringBuilder();
			for (String temp = ""; temp != null; temp = bf.readLine()) {
				// if (temp.trim().isEmpty())
				// temp = "";
				builder.append(temp).append("\n");
			}
			bf.close();
			return builder.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String readFile(File file) {
		try {
			BufferedReader bf = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
			StringBuilder builder = new StringBuilder();
			for (String temp = ""; temp != null; temp = bf.readLine()) {
				builder.append(temp).append("\n");
			}
			bf.close();
			return builder.toString().trim();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static boolean writeFile(String outStr, String fileName) {
		try {
			File fileDir = new File(standardizedFileName(fileName, 1));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
			out.append(outStr);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeFile(ArrayList<String> lstSentences, String fileName) {
		try {
			File fileDir = new File(standardizedFileName(fileName, 1));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
			for (String str : lstSentences) {
				out.append(str).append("\r\n");
			}
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeFileHTML(String sentence, String fileName) {
		try {
			File fileDir = new File(standardizedFileName(fileName, 2));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
			out.append(sentence).append("\r\n");
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean writeFileHTML(ArrayList<String> lstSentences, String fileName) {
		try {
			File fileDir = new File(standardizedFileName(fileName, 2));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
			for (String str : lstSentences) {
				out.append(str).append("\r\n");
			}
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean saveProject(String input, String output, String fileName) {
		try {
			File fileDir = new File(standardizedFileName(fileName, 3));
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileDir), "UTF8"));
			out.append("<input>" + Encryptor.encrypt(Encryptor.DEFAULT_KEY, Encryptor.DEFAULT_INITVECTOR, input)
					+ "</input>");
			out.append("<output>" + Encryptor.encrypt(Encryptor.DEFAULT_KEY, Encryptor.DEFAULT_INITVECTOR, output)
					+ "</output>");
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static ArrayList<String> openProject(String fileName) {
		String savedPro = readFile(fileName);
		String inputStr = savedPro.substring(savedPro.indexOf("<input>") + 7, savedPro.lastIndexOf("</input>"));
		String outputStr = savedPro.substring(savedPro.indexOf("<output>") + 8, savedPro.lastIndexOf("</output>"));

		inputStr = Encryptor.decrypt(Encryptor.DEFAULT_KEY, Encryptor.DEFAULT_INITVECTOR, inputStr);
		outputStr = Encryptor.decrypt(Encryptor.DEFAULT_KEY, Encryptor.DEFAULT_INITVECTOR, outputStr);
		// encrypted string has been damaged
		if (inputStr.equals("false") || outputStr.equals("false"))
			return null;

		ArrayList<String> resultStr = new ArrayList<>();
		resultStr.add(inputStr);
		resultStr.add(outputStr);
		return resultStr;
	}
}
