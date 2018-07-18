
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

public class Instanzgenerator {

	public static void main(String[] args) {

		String dateipfad = "";
		String dateiname = "";
		SecureRandom random = new SecureRandom();
		
		int minProdukte = 10;
		int maxProdukte = 35;
		int anzahlPerioden = 30;
		int minErwarteteNachfrage = 21;
		int maxErwarteteNachfrage = 254;
		int startkapital = 100000000;
		int fixkosten = 3000;

		double minProdE = 0.8;
		double maxProdE = 0.95;
		double maxProdV = 0.4;
		int minVarianz = 16;
		double maxVarianz = 2.75;		
		int minv = 30;
		int maxv = 150;
		int minV = 359;
		int minc = 10;
		int maxc = 134;
		int maxw = 45;
		double minMarge = 0.04;
		double maxMarge = 0.27;
		double minC = 0.05;
		double maxC = 0.1;

		
		int n = erzeugeGanzeZahlInDiesemBereich(minProdukte, maxProdukte, random);
		int[] c = new int[n];
		int[] p = new int[n];
		int[] v = new int[n];
		int[] w = new int[n];
		int[] b = new int[n];
		int[] erwartungswerte = new int[n];
		int[] varianzen = new int[n];

		
		
		for (int produkt = 0; produkt < n; produkt++) {
			erwartungswerte[produkt] = erzeugeGanzeZahlInDiesemBereich(minErwarteteNachfrage, maxErwarteteNachfrage, random);
		}
		
		for (int produkt = 0; produkt < n; produkt++) {
			varianzen[produkt] = erzeugeGanzeZahlInDiesemBereich(minVarianz, (int) Math.round(maxVarianz * erwartungswerte[produkt]), random);
		}
		
		for (int produkt = 0; produkt < n; produkt++) {
			v[produkt] = erzeugeGanzeZahlInDiesemBereich(minv, maxv, random);
		}

		double[] faktoren = new double[n];
		for (int produkt = 0; produkt < n; produkt++) {
			double doublev = v[produkt];
			double doubleminv = minv;
			double doublemaxv = maxv;
			faktoren[produkt] = Math.sqrt(1.0 + (doublev - doubleminv) / (doublemaxv - doubleminv));
		}
		
		for (int produkt = 0; produkt < n; produkt++) {
			c[produkt] = erzeugeGanzeZahlInDiesemBereich((int) Math.round(faktoren[produkt] * minc), (int) Math.round(faktoren[produkt] * maxc), random);
		}

		for (int produkt = 0; produkt < n; produkt++) {
			w[produkt] = erzeugeGanzeZahlInDiesemBereich(0, (int) Math.round(faktoren[produkt] * maxw), random);
		}

		for (int produkt = 0; produkt < n; produkt++) {
			b[produkt] = erzeugeGanzeZahlInDiesemBereich((int) Math.round(minProdE * erwartungswerte[produkt]), (int) Math.round(maxProdE * erwartungswerte[produkt]) + (int) Math.round(maxProdV * varianzen[produkt]), random);
		}
		
		for (int produkt = 0; produkt < n; produkt++) {
			p[produkt] = erzeugeGanzeZahlInDiesemBereich((int) Math.round((1.0 + minMarge) * c[produkt]), (int) Math.round(faktoren[produkt] * (1.0 + maxMarge) * c[produkt]), random);
		}
		
		double summe = 0.0;
		for (int produkt = 0; produkt < n; produkt++) {
			summe += 0.5 * v[produkt] * Math.sqrt(varianzen[produkt]);
		}		
		summe =  0.5 * summe / n;
		int V = (int) Math.round(summe);
		V = Math.max(V, minV);
		
		summe = 0.0;
		for (int produkt = 0; produkt < n; produkt++) {
			summe += 0.5 * c[produkt] * Math.sqrt(varianzen[produkt]) / Math.sqrt(n);
		}
		int C = erzeugeGanzeZahlInDiesemBereich((int) Math.round(minC * summe), (int) Math.round(maxC * summe), random);
		
		

		/*
		 * ***********************************************************************
		 */

		
		int[][] demand = new int[anzahlPerioden][n];

		// generate demands
		for (int j = 0; j < anzahlPerioden; j++) {
			for (int i = 0; i < n; i++) {				
				demand[j][i] = erzeugePositiveGanzzahligeNormalverteilteNachfrage(erwartungswerte[i], varianzen[i], random);
			}
		}

		// write static instance data to file
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(dateipfad + dateiname + ".in"));

			bw.write(startkapital + " " + fixkosten + " " + C + " " + V + " " + n);
			bw.newLine();
			for (int i = 0; i < n; i++) {
				bw.write(c[i] + " " + p[i] + " " + v[i] + " " + w[i] + " "
						+ b[i]);
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write static demand parameters to file
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(dateipfad + dateiname + ".deminfo"));

			bw.write(n + "");
			bw.newLine();
			for (int i = 0; i < n; i++) {
				bw.write(erwartungswerte[i] + " " + varianzen[i]);
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// write demand values to file
		try {
			BufferedWriter bw = new BufferedWriter(
					new FileWriter(dateipfad + dateiname + ".dem"));

			bw.write(anzahlPerioden + "");
			bw.newLine();
			for (int j = 0; j < anzahlPerioden; j++) {
				bw.write(demand[j][0] + "");
				for (int i = 1; i < n; i++) {
					bw.write(" " + demand[j][i]);
				}
				bw.newLine();
			}

			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public static int erzeugeGanzeZahlInDiesemBereich(int min, int max, SecureRandom random) {
		if (min > max) {
			System.out.println("Min sollte kleiner gleich max sein!");
			System.exit(0);
		}
		return min + random.nextInt(max - min + 1);
	}
	
	public static int erzeugePositiveGanzzahligeNormalverteilteNachfrage(int erwartungswert, int varianz, SecureRandom random) {
		assert (erwartungswert >= 0 || varianz >= 0) : "Erwartungswert oder Varianz negativ!";
		int nachfrage = -1;
		while (nachfrage < 0) {
			nachfrage = (int) Math.round(Math.sqrt(varianz) * random.nextGaussian() + erwartungswert);
		}
		return nachfrage;
	}
	
}
