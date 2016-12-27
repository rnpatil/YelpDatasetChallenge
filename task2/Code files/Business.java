
/**
 * @author Rohit Patil (rnpatil)
 *
 */
public class Business {


	/**
	 *
	 * 	Model to store business object attributes.
	 *
	 **/

	private  int score;
	private double rating;
	private int reviewCount;
	private double predictedScore;
	private String businessID;
	private String businessName;

	public String getBusinessID() {
		return businessID;
	}
	public void setBusinessID(String businessID) {
		this.businessID = businessID;
	}
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}



	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public double getRating() {
		return rating;
	}
	public void setRating(double rating) {
		this.rating = rating;
	}
	public int getReviewCount() {
		return reviewCount;
	}
	public void setReviewCount(int reviewCount) {
		this.reviewCount = reviewCount;
	}
	public double getPredictedScore() {
		return predictedScore;
	}
	public void setPredictedScore(double predictedScore) {
		this.predictedScore = predictedScore;
	}

}
