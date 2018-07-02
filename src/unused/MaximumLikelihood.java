package unused;
import java.util.ArrayList;

/*
 * This Class computes Maximum Likelihood estimators for mean m and standard deviation sd
 * for a normally distributed random variable (input: some observed realizations of the random variable)
 */

public class MaximumLikelihood {

	public MaximumLikelihood() {

	}
	
	public static Double[] estimate(ArrayList<Integer> realizations) {
		Double[] estimators = new Double[3];
		
		if (realizations.size() <= 1) {
			if (realizations.size() < 1) estimators[0] = 0.0;
			else estimators[0] = realizations.get(0) * 1.0;
			estimators[1] = 0.0;
			estimators[2] = 0.0;
		}
		else {
			int meanSum = 0;
			for (Integer i : realizations) {
				meanSum += i;
			}
			estimators[0] = (double) (meanSum / realizations.size());		// ML mean estimator
			

			double sdSum = 0;
			for (Integer i : realizations) {
				sdSum += (i - estimators[0])*(i - estimators[0]);
			}
			estimators[1] = Math.sqrt(sdSum / realizations.size());			// ML standard deviation estimator
			estimators[2] = Math.sqrt(sdSum / (realizations.size() - 1));	// unbiased standard deviation estimator
		}
		
		return estimators;		// mean, sd, unbiased sd
	}
	
	

}
