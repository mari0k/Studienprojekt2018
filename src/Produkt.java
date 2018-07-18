import org.apache.commons.math3.distribution.NormalDistribution;

public class Produkt {

	
	// man müsste jedem Produkt nach seine Anzahl mitgeben!!!
	// also als Attribut, sonst könnte die LagerWegwerfHeuristik nicht wissen,
	// wie viel von welchem Produkt da ist
	// die Übergabe der Anzahl könnte auch das bewerten des Produkts vereinfachen
	// Aber die Anzahl müsste jedes mal neu gesetzt werden?!
	
	
	private static Produkt[][] produktSortierungen;	
	
	
	private int tempAnzahl;
	private int tempBewertung;
	
	
	private int aktuellerBestand;
	private int produktionsniveau;
	
	
	private int id;
	private int herstellungskosten;
	private int verkaufserloes;
	private int volumen;
	private int wegwerfkosten;
	private int produktionsschranke;
	private int erwartungswert;
	private int varianz;
	private NormalDistribution distribution;
	
	
	public Produkt(int id, int herstellungskosten, int verkaufserloes, int volumen, int wegwerfkosten, int produktionsschranke, int erwartungswert, int varianz) {
		this.id = id;
		this.herstellungskosten = herstellungskosten;
		this.verkaufserloes = verkaufserloes;
		this.volumen = volumen;
		this.wegwerfkosten = wegwerfkosten;
		this.produktionsschranke = produktionsschranke;
		this.erwartungswert = erwartungswert;
		this.varianz = varianz;
		this.aktuellerBestand = 0;
		this.produktionsniveau = 0;
		this.tempAnzahl = -1;
		this.tempBewertung = herstellungskosten;
		distribution = new NormalDistribution(erwartungswert, Math.sqrt(varianz), 1e-15);
		assert (eingabeGueltig());
	}
	
	
	
	private static boolean sindAlleProduktSortierungenRichtig() {
		assert (produktSortierungen.length > 0);
		for (int i = 0; i < produktSortierungen.length; i++) {
			boolean[] istDabei = new boolean[produktSortierungen[i].length];
			for (int j = 0; j < istDabei.length; j++) {
				istDabei[produktSortierungen[i][j].getId()] = true;
			}
			for (int j = 0; j < istDabei.length; j++) {
				if (!istDabei[j]) {
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void bewerte() {
		assert (tempAnzahl >= 0);
		if (tempAnzahl == 0) {
			tempBewertung = herstellungskosten;
			return;
		}
		/*
		 * TODO bisher keine Eingabeparameter verwendet!!!
		 */
		/*
		 * Idee: gegeben Anzahl, unterstelle, dass in nächster Produktionsphase das Maximum produziert wird.
		 * Berechne für 1,...,Anzahl die Bewertung einzeln (z.B eine Wkeit, dass das Produkt wegkommt) und
		 * nehme dann den Durchschnitt.
		 */
		
		
		/*
		 * Anzahl muss aus tempAnzaahl gelesen werden!!!
		 */
		
		double summe = 0.0;
		for (int i = 1; i <= tempAnzahl; i++) {
			double p = distribution.cumulativeProbability(produktionsschranke + i - 0.5);
			summe += herstellungskosten * (1 + p) + verkaufserloes * (1.0 - p);
		}
		tempBewertung = (int) Math.round(summe / (2 * tempAnzahl));
		
		//System.out.println(herstellungskosten + " " + tempBewertung + " " + verkaufserloes);
		//System.out.println(tempAnzahl);
	}
	
	
	private static Produkt[] gibPermutationDerProdukteZurueck(Produkt[] produkte, int[] permutation) {
		assert (produkte.length == permutation.length);
		Produkt[] rueck = new Produkt[produkte.length];
		for (int i = 0; i < rueck.length; i++) {
			rueck[i] = produkte[permutation[i]];
		}
		return rueck;
	}
	
	
	
	public static void erstelleProduktSortierungen(Produkt[] produkte) {
		produktSortierungen = new Produkt[5][produkte.length];
		int[] permutation = erzeugePermutationVolumenAbsteigend(produkte);
		produktSortierungen[0] = gibPermutationDerProdukteZurueck(produkte, permutation);
		assert (istNachVolumenAbsteigendSortiert(produktSortierungen[0]));
		permutation = erzeugePermutationVerkaufserloesAbsteigend(produkte);
		produktSortierungen[1] = gibPermutationDerProdukteZurueck(produkte, permutation);
		assert (istNachVerkaufserloesAbsteigendSortiert(produktSortierungen[1]));
		permutation = erzeugePermutationHerstellungskostenAbsteigend(produkte);
		produktSortierungen[2] = gibPermutationDerProdukteZurueck(produkte, permutation);
		assert (istNachHerstellungskostenAbsteigendSortiert(produktSortierungen[2]));
		permutation = erzeugePermutationHerstellungskostenProVolumenAbsteigend(produkte);
		produktSortierungen[3] = gibPermutationDerProdukteZurueck(produkte, permutation);
		assert (istNachHerstellungskostenProVolumenAbsteigendSortiert(produktSortierungen[3]));
		permutation = erzeugePermutationVerkaufserloesProVolumenAbsteigend(produkte);
		produktSortierungen[4] = gibPermutationDerProdukteZurueck(produkte, permutation);
		assert (istNachVerkaufserloesProVolumenAbsteigendSortiert(produktSortierungen[4]));
		assert (sindAlleProduktSortierungenRichtig());
		LagerWegwerfHeuristik.setProduktSortierungen(produktSortierungen);
	}
	
	
	private static boolean istNachVerkaufserloesAbsteigendSortiert(Produkt[] produkte) {
		for (int i = 0; i < produkte.length - 1; i++) {
			if (produkte[i].getVerkaufserloes() < produkte[i + 1].getVerkaufserloes()) {
				return false;
			}
		}
		return true;
	}
	
	
	private static boolean istNachVolumenAbsteigendSortiert(Produkt[] produkte) {
		for (int i = 0; i < produkte.length - 1; i++) {
			if (produkte[i].getVolumen() < produkte[i + 1].getVolumen()) {
				return false;
			}
		}
		return true;
	}
	
	
	private static boolean istNachHerstellungskostenProVolumenAbsteigendSortiert(Produkt[] produkte) {
		for (int i = 0; i < produkte.length - 1; i++) {
			if (1.0 * produkte[i].getHerstellungskosten() / produkte[i].getVolumen() < 1.0 * produkte[i + 1].getHerstellungskosten() / produkte[i + 1].getVolumen()) {
				return false;
			}
		}
		return true;
	}
	
	
	
	
	private static boolean istNachVerkaufserloesProVolumenAbsteigendSortiert(Produkt[] produkte) {
		for (int i = 0; i < produkte.length - 1; i++) {
			if (1.0 * produkte[i].getVerkaufserloes() / produkte[i].getVolumen() < 1.0 * produkte[i + 1].getVerkaufserloes() / produkte[i + 1].getVolumen()) {
				return false;
			}
		}
		return true;
	}
	
	
	private static boolean istNachHerstellungskostenAbsteigendSortiert(Produkt[] produkte) {
		for (int i = 0; i < produkte.length - 1; i++) {
			if (produkte[i].getHerstellungskosten() < produkte[i + 1].getHerstellungskosten()) {
				return false;
			}
		}
		return true;
	}
	
	
	
	private static int[] erzeugePermutationHerstellungskostenAbsteigend(Produkt[] produkte) {
		int[] perm = new int[produkte.length];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < produkte.length; i++) {
			for (int j = i + 1; j < produkte.length; j++) {
				if (produkte[perm[i]].getHerstellungskosten() < produkte[perm[j]].getHerstellungskosten()) {
					int hilfe = perm[i];
					perm[i] = perm[j];
					perm[j] = hilfe;
				}
			}
		}
		return perm;
	}
	
	
	
	
	private static int[] erzeugePermutationVerkaufserloesAbsteigend(Produkt[] produkte) {
		int[] perm = new int[produkte.length];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < produkte.length; i++) {
			for (int j = i + 1; j < produkte.length; j++) {
				if (produkte[perm[i]].getVerkaufserloes() < produkte[perm[j]].getVerkaufserloes()) {
					int hilfe = perm[i];
					perm[i] = perm[j];
					perm[j] = hilfe;
				}
			}
		}
		return perm;
	}
	
	
	private static int[] erzeugePermutationHerstellungskostenProVolumenAbsteigend(Produkt[] produkte) {
		int[] perm = new int[produkte.length];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < produkte.length; i++) {
			for (int j = i + 1; j < produkte.length; j++) {
				if (1.0 * produkte[perm[i]].getHerstellungskosten() / produkte[perm[i]].getVolumen() < 1.0 * produkte[perm[j]].getHerstellungskosten() / produkte[perm[j]].getVolumen()) {
					int hilfe = perm[i];
					perm[i] = perm[j];
					perm[j] = hilfe;
				}
			}
		}
		return perm;
	}
	
	
	
	
	private static int[] erzeugePermutationVerkaufserloesProVolumenAbsteigend(Produkt[] produkte) {
		int[] perm = new int[produkte.length];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < produkte.length; i++) {
			for (int j = i + 1; j < produkte.length; j++) {
				if (1.0 * produkte[perm[i]].verkaufserloes / produkte[perm[i]].getVolumen() < 1.0 * produkte[perm[j]].getVerkaufserloes() / produkte[perm[j]].getVolumen()) {
					int hilfe = perm[i];
					perm[i] = perm[j];
					perm[j] = hilfe;
				}
			}
		}
		return perm;
	}
	
	
	
	private static int[] erzeugePermutationVolumenAbsteigend(Produkt[] produkte) {
		int[] perm = new int[produkte.length];
		for (int i = 0; i < perm.length; i++) {
			perm[i] = i;
		}
		for (int i = 0; i < produkte.length; i++) {
			for (int j = i + 1; j < produkte.length; j++) {
				if (produkte[perm[i]].getVolumen() < produkte[perm[j]].getVolumen()) {
					int hilfe = perm[i];
					perm[i] = perm[j];
					perm[j] = hilfe;
				}
			}
		}
		return perm;
	}
	
	private boolean eingabeGueltig() {
		return (id >= 0 && herstellungskosten >= 0 && verkaufserloes >= 0 && volumen >= 1 && wegwerfkosten >= 0 && produktionsschranke >= 0 && erwartungswert >= 0 && varianz >= 0);
	}




	public int getId() {
		return id;
	}




	public int getHerstellungskosten() {
		return herstellungskosten;
	}




	public int getVerkaufserloes() {
		return verkaufserloes;
	}




	public int getVolumen() {
		return volumen;
	}




	public int getWegwerfkosten() {
		return wegwerfkosten;
	}




	public int getProduktionsschranke() {
		return produktionsschranke;
	}
	
	
	
	public int getTempAnzahl() {
		return tempAnzahl;
	}
	
	public int getTempBewertung() {
		return tempBewertung;
	}
	
	
	
	public static Produkt[][] getProduktSortierungen() {
		return produktSortierungen;
	}



	public void setTempAnzahl(int tempAnzahl) {
		assert (tempAnzahl >= 0);
		this.tempAnzahl = tempAnzahl;
	}



	public int getAktuellerBestand() {
		return aktuellerBestand;
	}



	public void setAktuellerBestand(int aktuellerBestand) {
		this.aktuellerBestand = aktuellerBestand;
	}



	public int getProduktionsniveau() {
		return produktionsniveau;
	}



	public void setProduktionsniveau(int produktionsniveau) {
		this.produktionsniveau = produktionsniveau;
	}
	
	
}
