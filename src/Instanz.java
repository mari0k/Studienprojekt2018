
public class Instanz {

	private final int startkapital;
	private final int anzahlPerioden;
	private final int anzahlProdukte;
	private final int lagervolumen;
	private final int lagerkosten;
	private final int fixkosten;
	
	private Produkt[] produkte;
	
	/*
	private int[] produktionsschranke;
	private int[] herstellkosten;
	private int[] verkaufserloes;
	private int[] wegwerfkosten;
	private int[] volumen;
	 */
	
	// erwartungswert und varianz werden zum Erstellen der Szenarien mit LHS benoetigt
	private final int[] erwartungswert;
	private final int[] varianz;
	
	
	private int aktuellesKapital;
	/*
	private int[] aktuellerBestand;
	private int[] delta;
	 */
	
	public Instanz(int S, int m, int n, int V, int C, int F, int[] b, int[] c, int[] p, int[] w, int[] v, int[] mean, int[] var) {
		startkapital = S;
		anzahlPerioden = m;
		anzahlProdukte = n;
		lagervolumen = V;
		lagerkosten = C;
		fixkosten = F;
		
		produkte = new Produkt[n];
		for (int i = 0; i < n; i++) {
			produkte[i] = new Produkt(i, c[i], p[i], v[i], w[i], b[i], mean[i], var[i]);
		}
		
		/*
		produktionsschranke = b.clone();
		herstellkosten = c.clone();
		verkaufserloes = p.clone();
		wegwerfkosten = w.clone();
		volumen = v.clone();
		 */
		
		erwartungswert = mean.clone();
		varianz = var.clone();
		
		
		aktuellesKapital = S;
		
		/*
		aktuellerBestand = new int[anzahlProdukte];
		for (int i = 0; i < anzahlProdukte; i++) {
			aktuellerBestand[i] = 0;
		}
		delta = c.clone();
		 */
	}
	
	
	public void aktualisiereBestand(int[] bestand) {
		for (int i = 0; i < anzahlProdukte; i++) {
			produkte[i].setAktuellerBestand(bestand[i]);
		}
	}
	
	public void zahleFixkosten() {
		aktuellesKapital -= fixkosten;
	}
	
	public void zahleProduktionskosten(int[] produktion) {
		for (int i = 0; i < anzahlProdukte; i++) {
			aktuellesKapital -= produkte[i].getHerstellungskosten() * produktion[i];	
		}
	}
	
	public void zahleLagerkosten(int anzahl) {
		aktuellesKapital -= anzahl * lagerkosten;
	}
	
	public void zahleWegwerfkosten(int[] entsorgung) {
		for (Produkt produkt : produkte) {
			aktuellesKapital -= produkt.getWegwerfkosten() * entsorgung[produkt.getId()];
		}
	}
	
	public void entsorgeRestbestand() {
		for (Produkt produkt : produkte) {
			aktuellesKapital -= produkt.getAktuellerBestand() * produkt.getWegwerfkosten();
			produkt.setAktuellerBestand(0);
		}
	}
	
	
	
	
	
	

	public int getAktuellesKapital() {
		return aktuellesKapital;
	}

	public void setAktuellesKapital(int aktuellesKapital) {
		this.aktuellesKapital = aktuellesKapital;
	}

	public int getStartkapital() {
		return startkapital;
	}

	public int getAnzahlPerioden() {
		return anzahlPerioden;
	}

	public int getAnzahlProdukte() {
		return anzahlProdukte;
	}

	public int getLagervolumen() {
		return lagervolumen;
	}

	public int getLagerkosten() {
		return lagerkosten;
	}

	public int getFixkosten() {
		return fixkosten;
	}

	public Produkt[] getProdukte() {
		return produkte;
	}

	public int[] getErwartungswert() {
		return erwartungswert;
	}

	public int[] getVarianz() {
		return varianz;
	}
	
}
