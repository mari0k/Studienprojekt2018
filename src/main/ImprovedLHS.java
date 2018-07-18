package main;
import java.util.Random;

import org.apache.commons.math3.distribution.NormalDistribution;

/*
 * This is our attempt to a Java-Version of:
 */



/*
 * 
 * improvedLHS_R.cpp: A C routine for creating Improved Latin Hypercube Samples
 *                  used in the LHS package
 * Copyright (C) 2012  Robert Carnell
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */
public class ImprovedLHS {
	
	public static Random random = new Random();

	/*
	 * Arrays are passed into this routine to allow R to allocate and deallocate
	 * memory within the wrapper function.
	 * The R internal random numer generator is used so that R can set.seed for
	 * testing the functions.
	 * This code uses ISO C90 comment styles and layout
	 * Dimensions:  result  K x N
	 *              avail   K x N
	 *              point1  K x DUP(N-1)
	 *              list1   DUP(N-1)
	 *              vec     K
	 * Parameters:
	 *              N: The number of points to be sampled
	 *              K: The number of dimensions (or variables) needed
	 *              dup: The duplication factor which affects the number of points
	 *                   that the optimization algorithm has to choose from
	 * References:  Please see the package documentation
	 *
	 */

	public static int[][] improvedLHS_C(int n, int d, int dup) {
		
		int[][] m_result = new int[d][n];
		/* the length of the point1 columns and the list1 vector */
		int len = dup * (n - 1);
		/* create memory space for computations */
		int[][] avail = new int[d][n];
		int[][] point1 = new int[d][len];
		int[] list1 = new int[len];
		int[] vec = new int[d];
		/* optimum spacing between points */
		double opt = n / ( Math.pow(n, (1.0 / d)));
		/* the square of the optimum spacing between points */
		double opt2 = opt * opt;

		/* index of the current candidate point */
		int point_index;
		/* index of the optimum point */
		int best;
		/* the squared distance between points */
		int distSquared;
		/*
		* the minimum difference between the squared distance and the squared
		* optimum distance
		*/
		double min_all;
		/*  The minumum candidate squared distance between points */
		int min_candidate;

		/* initialize the avail matrix */
		for (int row = 0; row < d; row++)
		{
			for (int col = 0; col < n; col++)
			{
				avail[row][col] = (col + 1);
			}
		}

		/*
		* come up with an array of K integers from 1 to N randomly
		* and put them in the last column of result
		*/
		for (int row = 0; row < d; row++)
		{
			m_result[row][n-1] = (int) Math.floor(random.nextDouble() * n + 1.0);
		}

		/*
		* use the random integers from the last column of result to place an N value
		* randomly through the avail matrix
		*/
		for (int row = 0; row < d; row++)
		{
			avail[row][(m_result[row][n - 1] - 1)] = n;
		}

		/* move backwards through the result matrix columns */
		for (int count = n - 1; count > 0; count--)
		{
			for (int row = 0; row < d; row++)
			{
				for (int col = 0; col < dup; col++)
				{
					/* create the list1 vector */
					for (int j = 0; j < count; j++)
					{
						list1[j + count*col] = avail[row][j];
					}
				}
				/* create a set of points to choose from */
				for (int col = count * dup; col > 0; col--)
				/* Note: can't do col = count*duplication - 1; col >= 0 because it throws a warning at W4 */
				{
					point_index = (int) Math.floor(random.nextDouble() * col);
					point1[row][col-1] = list1[point_index];
					list1[point_index] = list1[col-1];
				}
			}
			min_all = Double.MAX_VALUE;
			best = 0;
			for (int col = 0; col < dup * count - 1; col++)
			{
				min_candidate = Integer.MAX_VALUE;
				for (int j = count; j < n; j++)
				{
					distSquared = 0;
					/*
					* find the distance between candidate points and the points already
					* in the sample
					*/
					for (int k = 0; k < d; k++)
					{
						vec[k] = point1[k][col] - m_result[k][j];
						distSquared += vec[k] * vec[k];
					}
					/* original code compared dist1 to opt, but using the squareroot
					* function and recasting distSquared to a double was unncessary.
					* dist1 = sqrt((double) distSquared);
					* if (min_candidate > dist1) min_candidate = dist1;
					*/

					/*
					* if the distSquard value is the smallest so far place it in
					* min candidate
					*/
					if (min_candidate > distSquared) min_candidate = distSquared;
				}
				/*
				* if the difference between min candidate and opt2 is the smallest so
				* far, then keep that point as the best.
				*/
				if (Math.abs(min_candidate - opt2) < min_all)
				{
					min_all = Math.abs(min_candidate - opt2);
					best = col;
				}
			}

			/* take the best point out of point1 and place it in the result */
			for (int row = 0; row < d; row++)
			{
				m_result[row][count - 1] = point1[row][best];
			}
			/* update the numbers that are available for the future points */
			for (int row = 0; row < d; row++)
			{
				for (int col = 0; col < n; col++)
				{
					if (avail[row][col] == m_result[row][count - 1])
					{
						avail[row][col] = avail[row][count-1];
					}
				}
			}
		}

		/*
		* once all but the last points of result are filled in, there is only
		* one choice left
		*/
		for (int row = 0; row < d; row++)
		{
			m_result[row][0] = avail[row][0];
		}

		return m_result;
	}
	
	
	
	
	
/*
 * ======================================================================================================================================================
 * 
 * 		Down here comes some other stuff we might need
 * 
 * ======================================================================================================================================================
 */
	

	/*
	 * statische Funktion zur Erzeugung eines Latin Hypercube Samplings auf dem Intervall (0, 1)
	 */
	public static double[][] improvedLHS(int n, int d) {
		
		double[][] array = new double[d][n];
		int[][] lhs = improvedLHS_C(d, n, 1);
		
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < n; j++) {
				do {
					//array[i][j] = ( lhs[j][i] - 1 + random.nextDouble() ) / d;	// Zufälliger Punkt aus Teilintervall
					array[i][j] = ( lhs[j][i] - 1 + 0.5 ) / d;	// Mittelpunkt des Teilintervalls
				} while (array[i][j] == 0);
			}
		}
		
		return array;
	}
	
	/*
	 * statische Funktion zur Erzeugung eines Latin Hypercube Samplings auf dem Intervall (a, b)
	 */
	public static double[][] improvedLHS(int n, int d, double a, double b) {
		// Konsistenz der Intervallgrenzen sicherstellen
		if (a < b) {
			double temp = b;
			b = a;
			a = temp;
		}
		
		// Latin Hypercube Sampling auf (0, 1) erstellen
		double[][] array = improvedLHS(n, d);
		
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
		if (a == 0 && b == 1) array = improvedLHS(n, d);
		else array = improvedLHS(n, d, a, b);
		
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
