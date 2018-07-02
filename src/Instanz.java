
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
}
