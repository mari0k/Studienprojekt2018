

public class LagerWegwerfHeuristik {

	
	private static Produkt[][] produktSortierungen;
	private static double faktor = 2.0;

	
	
	public static double wert() {
		assert (produktSortierungen != null);
		double best = -Double.MAX_VALUE;
		int maxLager = berechneObereSchrankeLager();
		for (Produkt[] produkte : produktSortierungen) {
			best = Math.max(best, firstFit(produkte, maxLager));
			best = Math.max(best, bestFit(produkte, maxLager));
			best = Math.max(best, worstFit(produkte, maxLager));
		}
		return best;
	}
	
	
	
	
	
	public static int berechneObereSchrankeLager() {
		assert (produktSortierungen != null);
		assert (Lager.getVolumen() != 0);
		int summeVolumenProdukte = 0;
		for (Produkt produkt : produktSortierungen[0]) {
			summeVolumenProdukte += (produkt.getTempAnzahl() * produkt.getVolumen());
		}
		return ((int) Math.ceil(faktor * summeVolumenProdukte / Lager.getVolumen()));
	}
		
	
	
	public static double firstFit(Produkt[] produkte, int maxLager) {
		/*
		 * liefert den entsprechenden Double-Wert zureck
		 * eine Produktsortierung wird schon vorgegeben
		 * Lager werden in Array gespeichert
		 * faktor und untereSchranke muessen bekannt/berechnet sein/werden
		 */
		Lager[] lager = new Lager[maxLager];
		for (Produkt produkt : produkte) {
			int nochUnterzubringen = produkt.getTempAnzahl();
			int aktuellesLager = -1;
			while (nochUnterzubringen > 0) {
				aktuellesLager += 1;
				if (lager[aktuellesLager] == null) {
					lager[aktuellesLager] = new Lager();
				}			
				int kannUntergebrachtWerden = lager[aktuellesLager].wieVielPasstNochRein(produkt);
				if (kannUntergebrachtWerden > 0) {
					int wirdUntergebracht = Math.min(nochUnterzubringen, kannUntergebrachtWerden);
					nochUnterzubringen -= wirdUntergebracht;
					assert (nochUnterzubringen >= 0);
					lager[aktuellesLager].packeProduktRein(produkt, wirdUntergebracht);
				}
			}
		}	
		// berechne Wert
		double wert = 0.0;
		for (int i = 0; i < lager.length; i++) {
			if (lager[i] == null) {
				break;
			}
			wert += lager[i].wieVielIstLagerWirklichWert();
		}
		return wert;
	}
	
	
	
	
	public static double bestFit(Produkt[] produkte, int maxLager) {
		/*
		 * liefert den entsprechenden Double-Wert zureck
		 * eine Produktsortierung wird schon vorgegeben
		 * Lager werden in Array gespeichert
		 * faktor und untereSchranke muessen bekannt/berechnet sein/werden
		 */
		Lager[] lager = new Lager[maxLager]; 
		for (Produkt produkt : produkte) {
			int nochUnterzubringen = produkt.getTempAnzahl();
			assert (nochUnterzubringen >= 0);
			int aktuellesLager = -1;
			while (nochUnterzubringen > 0) {
				aktuellesLager += 1;
				if (lager[aktuellesLager] == null) {
					lager[aktuellesLager] = new Lager();
				}
				int kannUntergebrachtWerden = lager[aktuellesLager].wieVielPasstNochRein(produkt);
				if (kannUntergebrachtWerden > 0) {
					int wirdUntergebracht = Math.min(nochUnterzubringen, kannUntergebrachtWerden);
					nochUnterzubringen -= wirdUntergebracht;
					assert (nochUnterzubringen >= 0);
					lager[aktuellesLager].packeProduktRein(produkt, wirdUntergebracht);
					for (int intLager = aktuellesLager; intLager > 0; intLager--) {
						if (lager[intLager].getFreiesVolumen() < lager[intLager - 1].getFreiesVolumen()) {
							Lager hilfeLager = new Lager(lager[intLager]);
							lager[intLager] = lager[intLager - 1];
							lager[intLager - 1] = hilfeLager;
							
						} else {
							// abbrechen, da Rest schon sortiert
							break;
						}
					}
				}
			}
		}	
		// berechne Wert
		double wert = 0.0;
		for (int i = 0; i < lager.length; i++) {
			if (lager[i] == null) {
				break;
			}
			wert += lager[i].wieVielIstLagerWirklichWert();
		}
		return wert;
	}
	
	
	
	
	public static double worstFit(Produkt[] produkte, int maxLager) {
		Lager[] lager = new Lager[maxLager];
		int indexMaxVerwendet = 0;
		lager[0] = new Lager();
		for (Produkt produkt : produkte) {
			int nochUnterzubringen = produkt.getTempAnzahl();
			while (nochUnterzubringen > 0) {
				if (lager[indexMaxVerwendet].getFreiesVolumen() < produkt.getVolumen()) {
					indexMaxVerwendet += 1;
					lager[indexMaxVerwendet] = new Lager();
				}
				int kannUntergebrachtWerden = lager[indexMaxVerwendet].wieVielPasstNochRein(produkt);
				if (kannUntergebrachtWerden > 0) {
					int wirdUntergebracht = Math.min(nochUnterzubringen, kannUntergebrachtWerden);
					
	
					if (indexMaxVerwendet > 0) {
						int differenzRestkap = lager[indexMaxVerwendet].getFreiesVolumen() - lager[indexMaxVerwendet - 1].getFreiesVolumen();
						assert (differenzRestkap >= 0);
						wirdUntergebracht = Math.min(wirdUntergebracht, differenzRestkap / produkt.getVolumen() + 1);
						assert (wirdUntergebracht > 0);
					}
					
					nochUnterzubringen -= wirdUntergebracht;
					assert (nochUnterzubringen >= 0);
					lager[indexMaxVerwendet].packeProduktRein(produkt, wirdUntergebracht);
					for (int intLager = indexMaxVerwendet; intLager > 0; intLager--) {
						if (lager[intLager].getFreiesVolumen() < lager[intLager - 1].getFreiesVolumen()) {
							Lager hilfeLager = new Lager(lager[intLager]);
							lager[intLager] = lager[intLager - 1];
							lager[intLager - 1] = hilfeLager;
							
						} else {
							// abbrechen, da Rest schon sortiert
							break;
						}
					}
				}
			}
		}
		// berechne Wert
		double wert = 0.0;
		for (int i = 0; i < lager.length; i++) {
			if (lager[i] == null) {
				break;
			}
			wert += lager[i].wieVielIstLagerWirklichWert();
		}
		return wert;		
	}
	
	
	
	
	
	
	
	
	public static void setProduktSortierungen(Produkt[][] dieProduktSortierungen) {
		produktSortierungen = dieProduktSortierungen;
	}
	
	
}
