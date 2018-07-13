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
		String unserName = "nbjm";
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

		
		
		/*
		 * Zeit fuer die erste Periode laeuft
		 */
		long periodenstart = System.nanoTime();
		
		// TODO Szenarien generieren
		// in Abhaengigkeit der Instanzgroesse eine (kleine) Menge an Szenarien generieren
		int anzahlSzenarien = 101;
		int[][] szenarien = ImprovedLHS.normal_lhs(anzahlSzenarien, inst.erwartungswert, inst.varianz);
		
		
		/*
		 * Produktionsentscheidung fuer erste Periode treffen
		 */
		System.out.println("Kapital zu Beginn von Periode 1 von " + inst.perioden + ": " + inst.aktuellesKapital);
		// Fixkosten bezahlen
		inst.aktuellesKapital -= inst.fixkosten;
		if (inst.aktuellesKapital < 0) {
			System.out.println("Pleite! vor Produktion in Periode 1 von " + inst.perioden);
			System.exit(0);
		}
		// TODO Produktionsentscheidung
		int[] produktion = Produktion.produziere(inst, szenarien, zeitProPeriode - 1);
		System.out.println("Produktion in Periode 1 von " + inst.perioden);
		for (int i = 0; i < inst.produkte; i++) {
			System.out.print(" " + produktion[i]);
		}
		System.out.println("");
		System.out.println("Benötigte Zeit für Berechnungen: " + (int) Math.ceil((System.nanoTime() - periodenstart) / 1000000000.0) + " Sekunden");
		

		if (inst.aktuellesKapital < 0) {
			System.out.println("Pleite! nach Produktion in Periode 1 von " + inst.perioden);
			System.exit(0);
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
				System.exit(3);
			}
			System.exit(3);
		}
		// der aktuelle Bestand in inst wurde aktualisiert
		
		
		int[][] lagerung;
		
		for (int aktuellePeriode = 2; aktuellePeriode <= inst.perioden; aktuellePeriode++) {

			/*
			 * Zeit fuer die naechste Periode laeuft
			 */
			periodenstart = System.nanoTime();

			/*
			 * Delta anpassen
			 */
			// TODO delta anpassen
			
			
			
			
			if (inst.aktuellesKapital < 0) {
				System.out.println("Pleite! vor Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.perioden);
				System.exit(0);
			}
			
			/*
			 * Lager- und Wegwerfentscheidung fuer Restbestand aus vorheriger Periode treffen
			 */
			// TODO Lagerentscheidung
			lagerung = Lagerung.lagere(inst, zeitProPeriode / 2 - 1);
			System.out.println("Restbestand nach erster Periode:");
			for (int i = 0; i < inst.produkte; i++) {
				System.out.print(" " + inst.aktuellerBestand[i]);
			}
			System.out.println("");
			System.out.println("Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.perioden);
			for (int j = 0; j < lagerung.length; j++) {
				System.out.println(j);
				for (int i = 0; i < inst.produkte; i++) {
					System.out.print(" " + lagerung[j][i]);
				}
				System.out.println("");
			}
			System.out.println("");

			if (inst.aktuellesKapital < 0) {
				System.out.println("Pleite! nach Lagerung in Periode " + (aktuellePeriode - 1) + " von " + inst.perioden);
				System.exit(0);
			}

			/*
			 * Produktionsentscheidung fuer aktuelle Periode treffen
			 */
			System.out.println("Kapital zu Beginn von Periode " + aktuellePeriode + " von " + inst.perioden + ": " + inst.aktuellesKapital);
			// Fixkosten bezahlen
			inst.aktuellesKapital -= inst.fixkosten;
			// produktion = new int[inst.produkte];
			// TODO Produktionsentscheidung
			produktion = Produktion.produziere(inst, szenarien, zeitProPeriode / 2 - 1);
			System.out.println("Produktion in Periode " + aktuellePeriode + " von " + inst.perioden);
			for (int i = 0; i < inst.produkte; i++) {
				System.out.print(" " + produktion[i]);
			}
			System.out.println("");
			System.out.println("Benötigte Zeit für Berechnungen: " + (int) Math.ceil((System.nanoTime() - periodenstart) / 1000000000.0) + " Sekunden");
			

			if (inst.aktuellesKapital < 0) {
				System.out.println("Pleite! nach Produktion in Periode " + aktuellePeriode + " von " + inst.perioden);
				System.exit(0);
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
					System.exit(3);
				}
				System.exit(3);
			}
			// der aktuelle Bestand in inst wurde aktualisiert
		}
		
		/*
		 * Restbestand entsorgen!
		 */
		for (int i = 0; i < inst.produkte; i++) {
			inst.aktuellesKapital -= inst.aktuellerBestand[i] * inst.wegwerfkosten[i];
			inst.aktuellerBestand[i] = 0;
		}
		if (inst.aktuellesKapital < 0) {
			System.out.println("Pleite! durch Entsorgung in letzter Periode");
			System.exit(0);
		}
		System.out.println("ENDE! Kapital nach Entsorgung: " + inst.aktuellesKapital);
		
		/*
		 * Verbindung zum Server beenden
		 */
		try {
			tcp.closeConnection();
		} catch (Exception e) {
			System.out.println("Verbindung zum Server konnte nicht beendet werden!");
			e.printStackTrace();
			System.exit(10);
		}
	}
}
