package unused;

import main.Produkt;

public class Lager {

	private static int volumen = 0;
	private static int kosten = 0;
	
	
	private int freiesVolumen;
	private double summeBewertung;
	private int summeWegwerfkosten;
	
	
	
	public Lager() {
		assert (volumen > 0);
		freiesVolumen = volumen;
		summeBewertung = 0.0;
		summeWegwerfkosten = 0;
	}
	
	
	public Lager(Lager lager) {
		this.freiesVolumen = lager.getFreiesVolumen();
		this.summeBewertung = lager.getSummeBewertung();
		this.summeWegwerfkosten = lager.getSummeWegwerfkosten();
	}
	

	
	public double wieVielIstLagerWirklichWert() {
		if (summeBewertung >= kosten - summeWegwerfkosten) {
			return (summeBewertung - kosten);
		} else {
			return (-summeWegwerfkosten);
		}
	}
	
	
	
	public int wieVielPasstNochRein(Produkt produkt) {
		assert (produkt != null);
		return freiesVolumen / produkt.getVolumen();
	}
	
	
	public void packeProduktRein(Produkt produkt, int anzahl) {
		assert (anzahl > 0 && produkt != null);
		assert (anzahl * produkt.getVolumen() <= freiesVolumen);
		freiesVolumen -= anzahl * produkt.getVolumen();
		assert (freiesVolumen >= 0);
		summeBewertung += anzahl * produkt.getTempBewertung();
		summeWegwerfkosten += anzahl * produkt.getWegwerfkosten();
	}
	
	
	
	
	
	public int getFreiesVolumen() {
		return freiesVolumen;
	}

	
	public static int getKosten() {
		return kosten;
	}


	public static int getVolumen() {
		return volumen;
	}


	public double getSummeBewertung() {
		return summeBewertung;
	}


	public int getSummeWegwerfkosten() {
		return summeWegwerfkosten;
	}


	public static void setVolumen(int lagervolumen) {
		assert (lagervolumen > 0);
		volumen = lagervolumen;
	}
	
	
	public static void setKosten(int lagerkosten) {
		assert (lagerkosten >= 0);
		kosten = lagerkosten;
	}
	
}
