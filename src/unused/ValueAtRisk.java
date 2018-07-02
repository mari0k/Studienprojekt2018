package unused;
import org.apache.commons.math3.distribution.NormalDistribution;

/*
 * This Class computes the Value-at-Risk to a given confidence level alpha
 * for a normally distributed random variable (parameters mean m and standard-deviation sd)
 */

public class ValueAtRisk {
	
	private double alpha = 0.99;	// confidence level

	public ValueAtRisk(double alpha) {
		this.alpha = alpha;
	}
	
	
	public double computeVaR(double mean, double sd) {		
		if (sd > 0) {
			NormalDistribution distribution = new NormalDistribution(mean, sd);
			
			return distribution.inverseCumulativeProbability(1-alpha);
		}
		else {
			return mean;
		}
	}


	public double getAlpha() {
		return alpha;
	}


	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
	
	
}
