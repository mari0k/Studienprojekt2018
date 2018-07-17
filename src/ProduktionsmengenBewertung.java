
public class ProduktionsmengenBewertung {

	private static Produkt[] produkte = null;
	
	
	
	public static int verbessereProduktionsmengen(int[][] szenarien, int[] produktionsmengen, int kapital) {
		// Kapital ist bereits um Produktionskosten verringert
		double aktuellerWert = bewerte(szenarien, produktionsmengen, 2); // Anzahl Restperioden
		assert (kapital >= 0);
		do {
			for (Produkt produkt : produkte) {
				if (produktionsmengen[produkt.getId()] > 0) {
					produktionsmengen[produkt.getId()] -= 1;
					double wert = bewerte(szenarien, produktionsmengen, 2);
					if (wert > aktuellerWert) {
						aktuellerWert = wert;
						kapital += produkt.getHerstellungskosten();
					}
					else {
						produktionsmengen[produkt.getId()] += 1;
					}
				}
				if (produktionsmengen[produkt.getId()] < produkt.getProduktionsschranke() && kapital >= produkt.getHerstellungskosten()) {
					produktionsmengen[produkt.getId()] += 1;
					double wert = bewerte(szenarien, produktionsmengen, 2);
					if (wert > aktuellerWert) {
						aktuellerWert = wert;
						kapital -= produkt.getHerstellungskosten();
					}
					else {
						produktionsmengen[produkt.getId()] -= 1;
					}
				}
			}
		} while (false);
		return kapital;
	}
	
	
	
	public static double bewerte(int[][] szenarien, int[] produktionsmengen, int anzahlRestPerioden) {
		// grosse Szenarienmenge!!!
		assert (produkte != null);
		// alle Szenarien gleichwahrscheinlich
		// aktueller Bestand ist natuerlich im Produkt gespeichert
		double wert = 0.0;
		for (int[] szenario : szenarien) {
			for (Produkt produkt : produkte) {
				wert -= produktionsmengen[produkt.getId()] * produkt.getHerstellungskosten();
				int verfuegbareMenge = produkt.getAktuellerBestand() + produktionsmengen[produkt.getId()];
				int verkaufsmenge = Math.min(verfuegbareMenge, szenario[produkt.getId()]);
				wert += verkaufsmenge * produkt.getVerkaufserloes();
				assert (verfuegbareMenge - verfuegbareMenge >= 0);
				produkt.setTempAnzahl(verfuegbareMenge - verkaufsmenge);
				produkt.bewerte();		
			}
			wert += LagerWegwerfHeuristik.wert();
		}		
		return (wert / szenarien.length);
	}
	
	
	public static void setProdukte(Produkt[] dieProdukte) {
		produkte = dieProdukte;
	}
	
}
