
public class Instanz {

	public int startkapital;
	public int perioden;
	public int produkte;
	public int lagervolumen;
	public int lagerkosten;
	public int fixkosten;
	
	public int[] produktionsschranke;
	public int[] herstellkosten;
	public int[] verkaufserloes;
	public int[] wegwerfkosten;
	public int[] volumen;
	
	public int[] erwartungswert;
	public int[] varianz;
	
	public int aktuellesKapital;
	public int[] aktuellerBestand;
	
	public Instanz(int S, int m, int n, int V, int C, int F, int[] b, int[] c, int[] p, int[] w, int[] v, int[] mean, int[] var) {
		startkapital = S;
		perioden = m;
		produkte = n;
		lagervolumen = V;
		lagerkosten = C;
		fixkosten = F;
		
		produktionsschranke = b.clone();
		herstellkosten = c.clone();
		verkaufserloes = p.clone();
		wegwerfkosten = w.clone();
		volumen = v.clone();
		
		erwartungswert = mean.clone();
		varianz = var.clone();
		
		
		aktuellesKapital = S;
		aktuellerBestand = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			aktuellerBestand[i] = 0;
		}
	}
	
	public Instanz() {
		
	}

	public int getStartkapital() {
		return startkapital;
	}

	public void setStartkapital(int startkapital) {
		this.startkapital = startkapital;
	}

	public int getPerioden() {
		return perioden;
	}

	public void setPerioden(int perioden) {
		this.perioden = perioden;
	}

	public int getProdukte() {
		return produkte;
	}

	public void setProdukte(int produkte) {
		this.produkte = produkte;
	}

	public int getLagervolumen() {
		return lagervolumen;
	}

	public void setLagervolumen(int lagervolumen) {
		this.lagervolumen = lagervolumen;
	}

	public int getLagerkosten() {
		return lagerkosten;
	}

	public void setLagerkosten(int lagerkosten) {
		this.lagerkosten = lagerkosten;
	}

	public int getFixkosten() {
		return fixkosten;
	}

	public void setFixkosten(int fixkosten) {
		this.fixkosten = fixkosten;
	}

	public int[] getProduktionsschranke() {
		return produktionsschranke;
	}

	public void setProduktionsschranke(int[] produktionsschranke) {
		this.produktionsschranke = produktionsschranke;
	}

	public int[] getHerstellkosten() {
		return herstellkosten;
	}

	public void setHerstellkosten(int[] herstellkosten) {
		this.herstellkosten = herstellkosten;
	}

	public int[] getVerkaufserloes() {
		return verkaufserloes;
	}

	public void setVerkaufserloes(int[] verkaufserloes) {
		this.verkaufserloes = verkaufserloes;
	}

	public int[] getWegwerfkosten() {
		return wegwerfkosten;
	}

	public void setWegwerfkosten(int[] wegwerfkosten) {
		this.wegwerfkosten = wegwerfkosten;
	}

	public int[] getVolumen() {
		return volumen;
	}

	public void setVolumen(int[] volumen) {
		this.volumen = volumen;
	}

	public int[] getErwartungswert() {
		return erwartungswert;
	}

	public void setErwartungswert(int[] erwartungswert) {
		this.erwartungswert = erwartungswert;
	}

	public int[] getVarianz() {
		return varianz;
	}

	public void setVarianz(int[] varianz) {
		this.varianz = varianz;
	}

	public int getAktuellesKapital() {
		return aktuellesKapital;
	}

	public void setAktuellesKapital(int aktuellesKapital) {
		this.aktuellesKapital = aktuellesKapital;
	}

	public int[] getAktuellerBestand() {
		return aktuellerBestand;
	}

	public void setAktuellerBestand(int[] aktuellerBestand) {
		this.aktuellerBestand = aktuellerBestand;
	}
	
	
	
}
