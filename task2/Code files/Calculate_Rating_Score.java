
/**
 * @author Rohit Patil (rnpatil)
 *
 */

class Calculate_Rating_Score
{
	double []weight_3_5 = new double[4];
	double []weight_4_0 = new double[4];
	double []weight_4_5 = new double[4];
	double []weight_5_0 = new double[4];

	Calculate_Rating_Score()
	{
		weight_3_5[0] = 0.10;
		weight_3_5[1] = 0.15;
		weight_3_5[2] = 0.20;
		weight_3_5[3] = 0.25;

		weight_4_0[0] = 0.20;
		weight_4_0[1] = 0.30;
		weight_4_0[2] = 0.50;
		weight_4_0[3] = 0.60;

		weight_4_5[0] = 0.50;
		weight_4_5[1] = 0.55;
		weight_4_5[2] = 0.65;
		weight_4_5[3] = 0.75;

		weight_5_0[0] = 0.65;
		weight_5_0[1] = 0.85;
		weight_5_0[2] = 1.05;
		weight_5_0[3] = 1.35;
	}

	float calcRatingScore(double ratings, int review_count)
	{
		float ratingScore = 0;

		if (3.5 == ratings)
		{
			if ((0 <= review_count) && (5 >= review_count))
			{
				ratingScore += (weight_3_5[0] * review_count);
			}
			if ((5 <= review_count) && (10 >= review_count))
			{
				ratingScore += (weight_3_5[1] * review_count);
			}
			if ((10 <= review_count) && (15 >= review_count))
			{
				ratingScore += (weight_3_5[2] * review_count);
			}
			else
			{
				ratingScore += (weight_3_5[3] * review_count);
			}
		}

		if (4.0 == ratings)
		{
			if ((0 <= review_count) && (5 >= review_count))
			{
				ratingScore += (weight_4_0[0] * review_count);
			}
			if ((5 <= review_count) && (10 >= review_count))
			{
				ratingScore += (weight_4_0[1] * review_count);
			}
			if ((10 <= review_count) && (15 >= review_count))
			{
				ratingScore += (weight_4_0[2] * review_count);
			}
			else
			{
				ratingScore += (weight_4_0[3] * review_count);
			}
		}

		if (4.5 == ratings)
		{
			if ((0 <= review_count) && (5 >= review_count))
			{
				ratingScore += (weight_4_5[0] * review_count);
			}
			if ((5 <= review_count) && (10 >= review_count))
			{
				ratingScore += (weight_4_5[1] * review_count);
			}
			if ((10 <= review_count) && (15 >= review_count))
			{
				ratingScore += (weight_4_5[2] * review_count);
			}
			else
			{
				ratingScore += (weight_4_5[3] * review_count);
			}
		}

		if (5.0 == ratings)
		{
			if ((0 <= review_count) && (5 >= review_count))
			{
				ratingScore += (weight_5_0[0] * review_count);
			}
			if ((5 <= review_count) && (10 >= review_count))
			{
				ratingScore += (weight_5_0[1] * review_count);
			}
			if ((10 <= review_count) && (15 >= review_count))
			{
				ratingScore += (weight_5_0[2] * review_count);
			}
			else
			{
				ratingScore += (weight_5_0[3] * review_count);
			}
		}

		return ratingScore;
	}
}
