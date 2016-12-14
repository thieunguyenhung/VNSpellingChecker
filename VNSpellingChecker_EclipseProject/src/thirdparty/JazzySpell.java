package thirdparty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;

public class JazzySpell implements SpellCheckListener {
	private SpellChecker spellChecker;
	private List<String> misspelledWords;

	private static SpellDictionaryHashMap dictionaryHashMap;
	static {
		File dict = new File("src/dictionary/vndict396777.txt");
		try {
			dictionaryHashMap = new SpellDictionaryHashMap(dict);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JazzySpell() {
		spellChecker = new SpellChecker(dictionaryHashMap);
		spellChecker.addSpellCheckListener(this);
		misspelledWords = new ArrayList<String>();
	}

	public ArrayList<String> getSuggestions(String misspelledWord) {
		if (misspelledWord.length() > 0 && !misspelledWord.trim().isEmpty()) {
			@SuppressWarnings("unchecked")
			List<Word> lstSuggestWords = spellChecker.getSuggestions(misspelledWord, 0);
			ArrayList<String> lstResultSuggests = new ArrayList<String>();
			for (Word suggestWord : lstSuggestWords) {
				lstResultSuggests.add(suggestWord.getWord());
			}
			return lstResultSuggests;
		}
		return new ArrayList<>();
	}

	@Override
	public void spellingError(SpellCheckEvent arg0) {
		arg0.ignoreWord(true);
		misspelledWords.add(arg0.getInvalidWord());
	}

	public static void main(String[] args) {
		JazzySpell jazzySpell = new JazzySpell();
		String line = "Không biet";
		System.out.println(jazzySpell.getSuggestions(line));
	}
}
