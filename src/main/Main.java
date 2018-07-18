package main;

public class Main {
	
	/* ============================================================================
	 * 
	 *            Prototyp für den Ablauf unseres finalen Programms
	 * 
	 * ============================================================================
	 */

	public static void main(String[] args) {
		/*
		 * wichtige Parameter setzen
		 */
		String unserName = "Infinity";
		int zeitProPeriode = 28;	// Zeit pro Periode in Sekunden
		String serverName = "localhost"; 
		int serverPort = 22133;
		/*
		 * Check if Arguments are given and overwrite the corresponding variables
		 */
		switch (args.length) {
			case 4: serverPort = Integer.parseInt(args[3]);
			case 3: serverName = args[2];
			case 2: zeitProPeriode = Integer.parseInt(args[1]); 
			case 1: unserName = args[0]; break;
		}
		
		
		/* =========================================
		 *           JETZT GEHT ES LOS
		 * =========================================
		 */
		
		/*
		 * TcpConnector initialisieren
		 */
		TcpConnector tcp = new TcpConnector(serverName, serverPort);
		
		/*
		 * Verbindung zum Server aufbauen
		 */
		try {
			tcp.establishConnection(unserName);
		} catch (Exception e) {
			System.out.println("Verbindungsaufbau zum Server ist fehlgeschlagen!");
			e.printStackTrace();
			System.exit(1);
		}
		
		
		/*
		 * Grunddaten abfragen
		 */
		Instanz inst = null;
		try {
			inst = tcp.frageGrunddatenAb();
		} catch (Exception e) {
			System.out.println("Grunddaten konnten nicht abgefragt werden!");
			e.printStackTrace();
			try {
				tcp.closeConnection();
			} catch (Exception e1) {
				e1.printStackTrace();
				System.exit(2);
			}
			System.exit(2);
		}

		
		//ProduktionsmengenBewertung.setProdukte(inst.getProdukte());
		//Produkt.erstelleProduktSortierungen(inst.getProdukte());
		//LagerWegwerfHeuristik.setProduktSortierungen(Produkt.getProduktSortierungen());
		//Lager.setKosten(inst.getLagerkosten());
		//Lager.setVolumen(inst.getLagervolumen());
		
		
		/*
		 * Zeit fuer die erste Periode laeuft
		 */
		long periodenstart = System.nanoTime();
		
		// TODO Szenarien generieren
		int anzahlSzenarien = 301;
		
		if (inst.getAnzahlProdukte() >= 0) {
			anzahlSzenarien = 1501;
		}
		if (inst.getAnzahlProdukte() >= 7) {
			anzahlSzenarien = 1101;
		}
		if (inst.getAnzahlProdukte() >= 14) {
			anzahlSzenarien = 951;
		}
		if (inst.getAnzahlProdukte() >= 21) {
			anzahlSzenarien = 801;
		}
		if (inst.getAnzahlProdukte() >= 28) {
			anzahlSzenarien = 651;
		}
		if (inst.getAnzahlProdukte() >= 35) {
			anzahlSzenarien = 501;
		}
		if (inst.getAnzahlProdukte() >= 42) {
			anzahlSzenarien = 451;
		}
		if (inst.getAnzahlProdukte() >= 49) {
			anzahlSzenarien = 351;
		}
		if (inst.getAnzahlProdukte() >= 75) {
			anzahlSzenarien = 101;
		}
			
		int[][] szenarien = ImprovedLHS.normal_lhs(anzahlSzenarien, inst.getErwartungswert(), inst.getVarianz());
		
		
		/*
		 * Produktionsentscheidung fuer erste Periode treffen
		 */
		System.out.println("Kapital zu Beginn von Periode 1 von " + inst.getAnzahlPerioden() + ": " + inst.getAktuellesKapital());
		// Fixkosten bezahlen
		inst.zahleFixkosten();
		if (inst.getAktuellesKapital() < 0) {
			System.out.println("Pleite! vor Produktion in Periode 1 von " + inst.getAnzahlPerioden());
			//System.exit(0);
		}
		Produktion.setGamma(inst);
		// TODO Produktionsentscheidung
		int[] produktion = Produktion.produziere(inst, szenarien, (int) Math.floor(zeitProPeriode - (System.nanoTime() - periodenstart) / 1000000000.0 - 1));
		// Produktionsniveau der Produkte aktualisieren
		for (Produkt produkt : inst.getProdukte()) {
			produkt.setProduktionsniveau(produkt.getAktuellerBestand());
		}
		// Ausgabe
		System.out.println("Produktion in Periode 1 von " + inst.getAnzahlPerioden());
		for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
			System.out.print(" " + produktion[i]);
		}
		System.out.println("");
		System.out.println("Benötigte Zeit für Berechnungen: " + (int) Math.ceil((System.nanoTime() - periodenstart) / 1000000000.0) + " Sekunden");
		

		if (inst.getAktuellesKapital() < 0) {
			System.out.println("Pleite! nach Produktion in Periode 1 von " + inst.getAnzahlPerioden());
			//System.exit(0);
		}
		
		
		/*
		 * Produktionsentscheidung fuer erste Periode an Server senden
		 */
		try {
			tcp.sendeProduktionsentscheidungDerErstenPeriode(inst, produktion);
		} catch (Exception e) {
			System.out.println("Produktionsentscheidung der ersten Periode konnte nicht an Server übermittelt werden!");
			e.printStackTrace();
			try {
				tcp.closeConnection();
			} catch (Exception e1) {
				e1.printStackTrace();
				//System.exit(3);
			}
			//System.exit(3);
		}
		// der aktuelle Bestand in inst wurde aktualisiert
		
		
		int[][] lagerung;
		
		for (int aktuellePeriode = 2; aktuellePeriode <= inst.getAnzahlPerioden(); aktuellePeriode++) {

			/*
			 * Zeit fuer die naechste Periode laeuft
			 */
			periodenstart = System.nanoTime();
						
			
			
			if (inst.getAktuellesKapital() < 0) {
				System.out.println("Pleite! vor Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.getAnzahlPerioden());
				//System.exit(0);
			}
			
			/*
			 * Lager- und Wegwerfentscheidung fuer Restbestand aus vorheriger Periode treffen
			 */
			// TODO Lagerentscheidung
			lagerung = Lagerung.lagere(inst, zeitProPeriode / 4 - 1);

			System.out.println("Restbestand nach Periode " + aktuellePeriode + " von " + inst.getAnzahlPerioden());
			for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
				System.out.print(" " + inst.getProdukte()[i].getAktuellerBestand());
			}
			System.out.println("");
			System.out.println("Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.getAnzahlPerioden());
			for (int j = 0; j < lagerung.length; j++) {
				System.out.println(j);
				for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
					System.out.print(" " + lagerung[j][i]);
				}
				System.out.println("");
			}
			System.out.println("");

			if (inst.getAktuellesKapital() < 0) {
				System.out.println("Pleite! nach Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.getAnzahlPerioden());
				//System.exit(0);
			}

			/*
			 * Produktionsentscheidung fuer aktuelle Periode treffen
			 */
			System.out.println("Kapital zu Beginn von Periode " + aktuellePeriode + " von " + inst.getAnzahlPerioden() + ": " + inst.getAktuellesKapital());
			// Fixkosten bezahlen
			inst.zahleFixkosten();
			if (inst.getAktuellesKapital() > 0) {
				// TODO Produktionsentscheidung
				if (aktuellePeriode < inst.getAnzahlPerioden()) {
					produktion = Produktion.produziere(inst, szenarien, (int) Math.floor(zeitProPeriode - (System.nanoTime() - periodenstart) / 1000000000.0 - 1));
				}
				else {
					produktion = Produktion.produziereLetztePeriode(inst, szenarien, (int) Math.floor(zeitProPeriode - (System.nanoTime() - periodenstart) / 1000000000.0 - 1));
				}
				// Produktionsniveau der Produkte aktualisieren
				for (Produkt produkt : inst.getProdukte()) {
					produkt.setProduktionsniveau(Math.max(produkt.getProduktionsniveau(), produkt.getAktuellerBestand()));
				}
			}
			else {
				produktion = new int[inst.getAnzahlProdukte()];
			}
			//inst.setAktuellesKapital(ProduktionsmengenBewertung.verbessereProduktionsmengen(szenarien, produktion, inst.getAktuellesKapital()));
			System.out.println("Produktion in Periode " + aktuellePeriode + " von " + inst.getAnzahlPerioden());
			for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
				System.out.print(" " + produktion[i]);
			}
			System.out.println("");
			System.out.println("Benötigte Zeit für Berechnungen: " + (int) Math.ceil((System.nanoTime() - periodenstart) / 1000000000.0) + " Sekunden");
			

			if (inst.getAktuellesKapital() < 0) {
				System.out.println("Pleite! nach Produktion in Periode " + aktuellePeriode + " von " + inst.getAnzahlPerioden());
				//System.exit(0);
			}
			
			

			/*
			 * Lager- und Produktionsentscheidung an Server senden
			 */
			try {
				tcp.sendeEntscheidungen(inst, lagerung, produktion);
			} catch (Exception e) {
				System.out.println("Produktionsentscheidung der ersten Periode konnte nicht an Server übermittelt werden!");
				e.printStackTrace();
				try {
					tcp.closeConnection();
				} catch (Exception e1) {
					e1.printStackTrace();
					//System.exit(3);
				}
				//System.exit(3);
			}
			// der aktuelle Bestand in inst wurde aktualisiert
		}
		
		/*
		 * Restbestand entsorgen!
		 */
		inst.entsorgeRestbestand();
		if (inst.getAktuellesKapital() < 0) {
			System.out.println("Pleite! durch Entsorgung in letzter Periode");
			//System.exit(0);
		}
		System.out.println("ENDE! Kapital nach Entsorgung: " + inst.getAktuellesKapital());
		
		/*
		 * Verbindung zum Server beenden
		 */
		try {
			tcp.closeConnection();
		} catch (Exception e) {
			System.out.println("Verbindung zum Server konnte nicht beendet werden!");
			e.printStackTrace();
			//System.exit(10);
		}
	}
}
