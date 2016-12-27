

/**
 * @author Rohit Patil (rnpatil)
 * @author Sameeksha Vaity (samvaity)
 *
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class TopBusinessPredictor {

	private static MongoClient mongoClient;
	private static DB db;
	private static Business business;
	private static DBObject businessdBObject;
	private static DBCursor reviewCursor;
	private static DBObject reviewQuery;
	private static DBObject reviewsdBObject;
	private static DBCursor tipCursor;
	private static DBObject tipQuery;
	private static DBObject tipsdBObject;
	private static float ratingReviewCountScore=0f;
	private static List circle = new ArrayList();
	private static DBCollection reviewsCollection;
	private static DBCollection tipsCollection;
	private static DBCollection businessLocationCollection;
	private static float reviewScore = 0f;
	private static float reviewtipScore=0f;
	private static float tipScore = 0f;


	@SuppressWarnings({ "deprecation", "unchecked", "rawtypes" })
	public static void main(String args[]) throws IOException {
		mongoClient = new MongoClient(Constants.localhost, 27017);
		db = mongoClient.getDB(Constants.db);

		/**
		 *
		 *  Load reviews and tips json collections
		 */
		reviewsCollection = db.getCollection(Constants.reviews);
		tipsCollection = db.getCollection(Constants.tips);


		/**
		 *
		 * Create an index on reviews and tips collection based on business id
		 */
		reviewsCollection.createIndex(new BasicDBObject(Constants.business_id,1));
		tipsCollection.createIndex(new BasicDBObject(Constants.business_id, 1));

		/**
		 *
		 *  Load geo spatial business_location json collection
		 */
		businessLocationCollection = db.getCollection(Constants.businessLocation);

		/**
		 *
		 *  Create geo spatial index on location for business_location collection
		 *
		 */
		businessLocationCollection.createIndex(new BasicDBObject(Constants.location, "2dsphere"));



		/**
		 *
		 * This can be a location entered by the user OR
		 * User location  picked up from GPS information
		 *
		 * The given location { -112.2606911, 33.5329299 } represents a location in Arizona.
		 *
		 */
		circle.add( new double[]{ -112.2606911, 33.5329299 });


		/**
		 *
		 *  Fix 2 miles radius for searching neighborhood businesses
		 *
		 *  The query converts the distance to radians by dividing by the approximate equatorial radius of the earth, 3963.2 miles
		 */
		circle.add(Constants.radius / 3963.2);


		BasicDBObject searchObject = new BasicDBObject();

		/**
		 *  Search query to find all businesses within the 2 mile radius for a given location.
		 *  An added filter of businesses with rating 3.5 and above is also implied.
		 *
		 */

		searchObject.put(Constants.location,new BasicDBObject("$within", new BasicDBObject("$centerSphere", circle)));
		searchObject.put(Constants.stars, new BasicDBObject("$gt",Constants.minimumRating));

		DBCursor businessLocCursor = businessLocationCollection.find(searchObject);
		System.out.println("Number of businesses nearby the given location : "+businessLocCursor.size());


		Calculate_Rating_Score scoreCalculator = new Calculate_Rating_Score();

		List<Business> businessScoreList = new LinkedList<Business>();

		/**
		 * 	Load sentiment.properties file.
		 */
		SentimentAnalyzer.init();


		int i=0;
		while (businessLocCursor.hasNext()) {
			businessdBObject = businessLocCursor.next();

			++i;
			System.out.println("Processing business: "+ i);

			business = new Business();
			business.setBusinessID((String) businessdBObject.get(Constants.business_id));
			business.setBusinessName((String) businessdBObject.get(Constants.name));
			business.setRating((Double)businessdBObject.get(Constants.stars));
			business.setReviewCount((Integer)businessdBObject.get(Constants.review_count));


			/**
			 * get reviews for a business
			 */

			reviewQuery = new BasicDBObject();
			reviewQuery.put(Constants.business_id, business.getBusinessID());

			/**
			 *
			 * Comment to remove useful vote filter
			 */
			reviewQuery.put(Constants.votes_useful, new BasicDBObject("$gt",5));

			reviewCursor = reviewsCollection.find(reviewQuery);




			/**
			 * get tips for a business
			 */

			tipQuery = new BasicDBObject(Constants.business_id, business.getBusinessID());
			tipCursor = tipsCollection.find(tipQuery);


			reviewScore = 0f;
			if(reviewCursor.count()>0)
			{
				while (reviewCursor.hasNext()) {
					reviewsdBObject = reviewCursor.next();
					try {

						/**
						 *
						 * 1.	removeStopWordsandStem  --- Data Preprocessing.
						 * 2.	remove any special characters and multi space characters
						 * 3.	Find a sentiment score for the processed review text
						 *
						 **/

						//System.out.println("review: "+ (String) reviewsdBObject.get(Constants.text));

						reviewScore+=  SentimentAnalyzer.findSentiment(Utils.removeStopWordsandStem((String) reviewsdBObject.get(Constants.text)));
					} catch (Exception e) {

						e.printStackTrace();
					}
				}
				reviewScore = (float)reviewScore / reviewCursor.count();
			}



			tipScore = 0f;
			if(tipCursor.count()>0)
			{
				while (tipCursor.hasNext()) {
					tipsdBObject = tipCursor.next();
					try {

						/**
						 *
						 * 1.	removeStopWordsandStem  --- Data Preprocessing.
						 * 2.	remove any special characters and multi space characters
						 * 3.	Find a sentiment score for the processed tip text
						 *
						 **/

						//System.out.println("tip: "+ (String) tipsdBObject.get(Constants.text));

						tipScore +=  SentimentAnalyzer.findSentiment(Utils.removeStopWordsandStem((String) tipsdBObject.get(Constants.text)));

					} catch (Exception e) {

						e.printStackTrace();
					}
				}
				tipScore = (float)tipScore / tipCursor.count();
			}

			reviewtipScore=0f;

			/**
			 *
			 * Higher weight assigned to tip score.
			 *
			 *
			 **/

			if(reviewScore >0 && tipScore>0){

				reviewtipScore = (float) 0.4*(reviewScore) + (float) 0.6* (tipScore);
			}
			else if(reviewScore>0)
			{
				reviewtipScore=	(float) (0.8*reviewScore);
			}
			else
			{
				reviewtipScore=	(float) (1.2*tipScore);
			}



			/**
			 *
			 * Score attributed by a multiplication factor generated by the Calculate_Rating_Score class and the review count of a business
			 *
			 *
			 **/
			ratingReviewCountScore=scoreCalculator.calcRatingScore(business.getRating(),business.getReviewCount());

			/**
			 *
			 *  Popularity Score = (70%)* (Rating score) +(30%)*(Weighted Review + Tip Score)
			 *
			 **/
			business.setPredictedScore((0.7*ratingReviewCountScore)+(0.3*reviewtipScore));
			businessScoreList.add(business);


		}



		/**
		 *
		 *  Sort the business set based on the popularity score.
		 *
		 *
		 */

		Collections.sort(businessScoreList,new Comparator<Business>() {
			public int compare(Business business1, Business business2) {
				return Double.compare(business2.getPredictedScore(), business1.getPredictedScore());
			}
		});


		/**
		 *
		 *  Output file generated.
		 *  Used for evaluation task.
		 *
		 **/

		BufferedWriter bw = new BufferedWriter(new FileWriter("TopNbusiness-2miles.txt"));


		/***
		 *
		 *  Predict top N thriving businesses in a location
		 *  Output written to a txt file.
		 *
		 ***/
		int counter =0;
		for(Business b:businessScoreList)
		{
			counter++;
			bw.write("{Rank : "+ counter +", BusinessName: "+b.getBusinessName()+ ", Business_id : " + b.getBusinessID() + ", Review Count: "+ b.getReviewCount() +", Popularity Score : " +  b.getPredictedScore() + ", Original starRating : "+ b.getRating()+" }\n");


			if(counter>30)
			{
				break;

			}

		}
		bw.close();
	}
}
