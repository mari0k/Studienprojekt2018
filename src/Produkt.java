
public class Produkt {

	
	// man müsste jedem Produkt nach seine Anzahl mitgeben!!!
	// also als Attribut, sonst könnte die LagerWegwerfHeuristik nicht wissen,
	// wie viel von welchem Produkt da ist
	// die Übergabe der Anzahl könnte auch das bewerten des Produkts vereinfachen
	// Aber die Anzahl müsste jedes mal neu gesetzt werden?!
	
	
	private static Produkt[][] produktSortierungen;	
	
	
	private int tempAnzahl;
	private double tempBewertung;
	
	
	private int aktuellerBestand;
	private int produktionsmenge;
	
	
	private final int id;
	private final int herstellungskosten;
	private final int verkaufserloes;
	private final int volumen;
	private final int wegwerfkosten;
	private final int produktionsschranke;
	private final int erwartungswert;
	private final int varianz;
	
	
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
		this.produktionsmenge = -1;
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
	
	
	public double bewerte(int anzahl, int verbleibendePerioden, int lagervolumen, int lagerkosten) {
		/*
		 * Idee: gegeben Anzahl, unterstelle, dass in nächster Produktionsphase das Maximum produziert wird.
		 * Berechne für 1,...,Anzahl die Bewertung einzeln (z.B eine Wkeit, dass das Produkt wegkommt) und
		 * nehme dann den Durchschnitt.
		 */
		return 0.0;
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
	
	public double getTempBewertung() {
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



	public int getProduktionsmenge() {
		return produktionsmenge;
	}



	public void setProduktionsmenge(int produktionsmenge) {
		this.produktionsmenge = produktionsmenge;
	}
	
	
	
}

