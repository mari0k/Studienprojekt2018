
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
		String unserName = "unser Teamname";
		int zeitProPeriode = 29;	// Zeit pro Periode in Sekunden
		String serverName = "server IP";
		int serverPort = 1234;
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

		try {
			tcp.closeConnection();
			System.out.println("Trennen ohne Exception.");
			System.exit(0);
		} catch (Exception e) {
			System.out.println("Beenden der Verbindung fehlgechlagen!");
			System.exit(100);
		}
		
		/*
		 * Zeit fuer die erste Periode laeuft
		 */
		long periodenstart = System.nanoTime();
		
		// TODO Szenarien generieren
		// in Abhaengigkeit der Instanzgroesse eine (kleine) Menge an Szenarien generieren
		int anzahlSzenarien = 10;
		int[][] szenarien = LHS.normal_lhs(anzahlSzenarien, inst.erwartungswert, inst.varianz);
		
		
		/*
		 * Produktionsentscheidung fuer erste Periode treffen
		 */
		int[] produktion = new int[inst.produkte];
		// TODO Produktionsentscheidung
		
		
		
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
			 * Lager- und Wegwerfentscheidung fuer Restbestand aus vorheriger Periode treffen
			 */
			int anzahlVerwendeterLager = 3;
			lagerung = new int[anzahlVerwendeterLager][inst.produkte];
			// TODO Lagerentscheidung
			

			/*
			 * Produktionsentscheidung fuer aktuelle Periode treffen
			 */
			produktion = new int[inst.produkte];
			// TODO Produktionsentscheidung
			
			
			

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
