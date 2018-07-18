package unused;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

public class LHS {
	
	public static Random rand = new Random();

	public static double[] lhs(int n) {
		return lhs(n, 1)[0];
	}
	
	/*
	 * statische Funktion zur Erzeugung eines Latin Hypercube Samplings auf dem Intervall (0, 1)
	 */
	public static double[][] lhs(int n, int d) {
		
		double[][] array = new double[d][n];
		
		// array initialisieren
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < n; j++) {
				// rationale Zufallszahl zwischen i/d und (i+1)/d erzeugen
				do {
					//array[i][j] = (i * (1.0 / d)) + (rand.nextDouble() * (1.0 / d));	// Zufälliger Punkt aus Teilintervall
					array[i][j] = (i * (1.0 / d)) + (0.5 * (1.0 / d));	// Mittelpunkt des Teilintervalls
				} while (array[i][j] == (i * (1.0 / d)));
			}
		}
		
		// array-Spalten permutieren
		int helperIndex;
		double helperValue;
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < d; i++) {
				helperIndex = rand.nextInt(d);
				helperValue = array[i][j];
				array[i][j] = array[helperIndex][j];
				array[helperIndex][j] = helperValue;
			}
		}
		
		return array;
	}
	
	/*
	 * statische Funktion zur Erzeugung eines Latin Hypercube Samplings auf dem Intervall (a, b)
	 */
	public static double[][] lhs(int n, int d, double a, double b) {
		// Konsistenz der Intervallgrenzen sicherstellen
		if (a < b) {
			double temp = b;
			b = a;
			a = temp;
		}
		
		// Latin Hypercube Sampling auf (0, 1) erstellen
		double[][] array = lhs(n, d);
		
		// Skalieren
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < n; j++) {
				array[i][j] = a + ((b - a) * array[i][j]);
			}
		}
		
		return array;
	}
	
	
	/*
	 * ################################################################################################################################################
	 * ################################################################################################################################################
	 */
	

	
	/*
	 * statische Funktion zur Erzeugung eines NORMALVERTEILTEN Latin Hypercube Samplings mit Quantilen aus dem Intervall (0, 1)
	 */
	public static int[][] normal_lhs(int d, int[] mean, int[] variance) {
		return normal_lhs(d, mean, variance, 0, 1);
	}
	
	/*
	 * statische Funktion zur Erzeugung eines NORMALVERTEILTEN Latin Hypercube Sampling mit Quantilen aus dem Intervall (a, b) subset (0, 1)
	 */
	public static int[][] normal_lhs(int d, int[] mean, int[] variance, double a, double b) {
		// konsistenz der Intervallgrenzen sicherstellen
		if (a < b) {
			double temp = b;
			b = a;
			a = temp;
		}
		assert (a > 0 && b < 1) : "Intervall (a, b) muss Teilmenge von (0, 1) sein.";
		int n = mean.length;
		assert n == variance.length : "Arrays für Erwartungswerte und Varianzen nicht gleich lang.";
		
		// gleichverteiltes Latin Hypercube Sampling auf (a, b) erzeugen
		double[][] array;
		if (a == 0 && b == 1) array = lhs(n, d);
		else array = lhs(n, d, a, b);
		
		// returnArray initialisieren
		int[][] returnArray = new int[d][n];
		NormalDistribution distribution;
		
		// gleichverteiltes LHS per Inversenmethode normalverteilt machen
		for (int j = 0; j < n; j++) {
			for (int i = 0; i < d; i++) {
				assert array[i][j] > 0 : "LHS hat 0 als Wert erzeugt, aber 0-Quantil der Normalverteilung kann nicht bestimmt werden.";
				distribution = new NormalDistribution(mean[j], Math.sqrt(variance[j]));
				returnArray[i][j] = Math.max(0, (int) Math.round(distribution.inverseCumulativeProbability(array[i][j])));
			}
		}
		
		return returnArray;
	}
	
}
