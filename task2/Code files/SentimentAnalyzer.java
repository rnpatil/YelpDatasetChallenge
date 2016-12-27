
/**
 * @author Sameeksha Vaity (samvaity)
 *
 */

import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations.SentimentAnnotatedTree;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

public class SentimentAnalyzer {
	static StanfordCoreNLP pipeline;

	public static void init() {
		pipeline = new StanfordCoreNLP("sentiment.properties");
	}

	public static float findSentiment(String review) {

		int finalReviewScore = 0;
		Annotation annotation = null;
		List<CoreMap> sentences = null;
		if (review != null && review.length() > 0) {
			annotation = pipeline.process(review);
			sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
			for (CoreMap sentence : sentences) {
				Tree tree = sentence.get(SentimentAnnotatedTree.class);
				int sc = RNNCoreAnnotations.getPredictedClass(tree);
				finalReviewScore += sc;
			}
		}
		return (float) finalReviewScore / sentences.size();
	}
}
