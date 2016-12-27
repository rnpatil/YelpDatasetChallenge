
/**
 * @author Rohit Patil (rnpatil)
 *
 */

import java.io.StringReader;

import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;


public class Utils {


	/***
	 *
	 * 	Data Pre-processing function to remove stop words and  stem the words to the base origin.
	 *
	 * @param text
	 * @return
	 * @throws Exception
	 */
	public static String removeStopWordsandStem(String text) throws Exception {

		StringBuilder sb = new StringBuilder();
		Tokenizer whitespaceTokenizer = new WhitespaceTokenizer();
		whitespaceTokenizer.setReader(new StringReader(text));
		TokenStream tokenStream = new StopFilter(whitespaceTokenizer, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
		tokenStream = new PorterStemFilter(tokenStream);
		try
		{
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = charTermAttribute.toString();
				sb.append(term + " ");
			}
			return sb.toString();
		}
		catch(Exception e)
		{
			return null;
		}
		finally {
			tokenStream.close();
		}
	}
}
