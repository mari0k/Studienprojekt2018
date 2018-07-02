package src;

import java.util.*;

public class Greedy_Heuristik {

	static boolean[] produktBetrachtet;
	static boolean alleProdukteBetrachtet;
	static int[] globaleProduktion;

	public Greedy_Heuristik(Instanz inst) {
		this.produktBetrachtet = new boolean[inst.produkte];
		this.alleProdukteBetrachtet = false;
	}

	public static void main(String[] args) {
		int n = 3;

		int[] b = { 40, 50, 30 };
		int[] c = { 2, 4, 3 };
		int[] p = { 3, 6, 6 };
		int[] w = { 1, 2, 4 };
		int[] v = { 5, 7, 8 };
		int[] mean = { 12, 12, 22 };
		int[] var = { 1, 1, 1 };

		Instanz inst = new Instanz(100, 1, 3, 50, 20, 10, b, c, p, w, v, mean, var);

		int[] produktion = new int[n];
		int[][] szenarien = new int[2][n];
		szenarien[0][0] = 9;
		szenarien[0][1] = 10;
		szenarien[0][2] = 17;
		szenarien[1][0] = 16;
		szenarien[1][1] = 15;
		szenarien[1][2] = 29;

		int[] lagerBestand = new int[n];
		produktBetrachtet = new boolean[n];
		double[] wahrscheinlichkeiten = new double[2];
		wahrscheinlichkeiten[0] = 0.5;
		wahrscheinlichkeiten[1] = 0.5;

		for (int i = 0; i < n; i++) {
			lagerBestand[i] = 0;
			produktBetrachtet[i] = false;
			produktion[i] = 0;
		}

		alleProdukteBetrachtet = false;
		Produkt prod = new Produkt();
		LinkedList<Produkt> produkte = prod.erzeugeProdukte(inst);

		Collections.sort(produkte, new Sortiere_Absteigend());
		double startGewinn = berechneGewinnGlobal(inst, produktion, szenarien, wahrscheinlichkeiten, lagerBestand);

		greedyHeuristik(inst, produktion, startGewinn, szenarien, lagerBestand, 0, wahrscheinlichkeiten,
				produkte, 100);

		for(int i=0;i<n;i++) {
			System.out.println("Produkt_"+i+": "+globaleProduktion[i]);
		}
		System.out.println(berechneGewinnGlobal(inst, globaleProduktion, szenarien,wahrscheinlichkeiten, lagerBestand));
		System.out.println("Fertig");
	}

	public static void greedyHeuristik(Instanz inst, int[] produktion, double aktuellerGewinn, int[][] szenarien,
			int[] lagerBestand, int aktuellesProdukt, double[] wahrscheinlichkeiten, LinkedList<Produkt> produkte,
			double verfügbaresKapital) {

		int produktIndex = produkte.get(aktuellesProdukt).getIndex();
		int n = inst.produkte;
		int[] ausgabe = new int[n];
		int[] nächsteProduktion = produktion.clone();
		nächsteProduktion[produktIndex] += 1;

		double nächsterGewinn = berechneGewinnGlobal(inst, nächsteProduktion, szenarien, wahrscheinlichkeiten,
				lagerBestand);

		if (nächsterGewinn > aktuellerGewinn & produktionMoeglich(inst, nächsteProduktion, verfügbaresKapital)) {
			greedyHeuristik(inst, nächsteProduktion, nächsterGewinn, szenarien, lagerBestand, aktuellesProdukt,
					wahrscheinlichkeiten, produkte, verfügbaresKapital);
		} else {
			nächsteProduktion = produktion.clone();
			produktBetrachtet[produktIndex] = true;
			alleProdukteBetrachtet = true;
			for (int i = 0; i < n; i++) {
				System.out.println(produktBetrachtet[i]);
				if (!produktBetrachtet[i]) {
					alleProdukteBetrachtet = false;
					break;
				}
			}
			if (!alleProdukteBetrachtet) {
				System.out.println(produkte.get(aktuellesProdukt + 1).getIndex());
				aktuellesProdukt++;
				greedyHeuristik(inst, produktion, aktuellerGewinn, szenarien, lagerBestand, aktuellesProdukt,
						wahrscheinlichkeiten, produkte, verfügbaresKapital);
			} else {
				globaleProduktion=produktion.clone();
			}
		}
	}

	public static boolean produktionMoeglich(Instanz inst, int[] produktion, double verfügbaresKapital) {
		int n = inst.produkte;
		double produktionsKosten = 0;

		for (int i = 0; i < n; i++) {
			produktionsKosten += produktion[i] * inst.getHerstellkosten()[i];
			if (produktion[i] > inst.getProduktionsschranke()[i]) {
				return false;
			}
		}
		if (produktionsKosten < verfügbaresKapital) {
			return true;
		} else {
			return false;
		}

	}

	public static double berechneGewinnGlobal(Instanz inst, int[] produktion, int[][] szenarien,
			double[] wahrscheinlichkeiten, int[] lagerBestand) {
		double ausgabe = 0;
		double[] gewinn = new double[szenarien.length];

		for (int i = 0; i < gewinn.length; i++) {
			gewinn[i] = berechneGewinnSzenario(inst, produktion, lagerBestand, szenarien[i]);

		}

		for (int i = 0; i < inst.produkte; i++) {
			ausgabe -= produktion[i] * inst.herstellkosten[i];
		}
		for (int i = 0; i < gewinn.length; i++) {

			ausgabe += gewinn[i] * wahrscheinlichkeiten[i];
		}

		return ausgabe;
	}

	public static double berechneGewinnSzenario(Instanz inst, int[] produktion, int[] lagerBestand, int[] nachfrage) {

		double gewinn = 0.0;
		int n = inst.produkte;
		int[] verkaufsMenge = new int[n];
		int[] c = inst.getHerstellkosten();
		int[] p = inst.getVerkaufserloes();
		int[] w = inst.getWegwerfkosten();
		int C = inst.getLagerkosten();

		double anzahlLager = 0;
		int[] produkteAufLager = new int[n];
		int[] produkteWegGeworfen = new int[n];
		double[] delta = new double[n];

		for (int i = 0; i < n; i++) {
			verkaufsMenge[i] = Math.min(produktion[i] + lagerBestand[i], nachfrage[i]);
			gewinn = gewinn + (verkaufsMenge[i] * p[i]) + (produkteAufLager[i] * delta[i])
					+ (produkteWegGeworfen[i] * w[i]);
		}
		gewinn = gewinn - C * anzahlLager;

		return gewinn;
	}

	public static boolean pleiteInSzenario(Instanz inst, int[] produktion, int[] nachfrage, double kapital,
			int[] lagermenge) {
		boolean pleite = false;
		if ((kapital - berechneGewinnSzenario(inst, produktion, lagermenge, nachfrage)) < 0) {
			pleite = true;
		}
		return pleite;

	}

}
