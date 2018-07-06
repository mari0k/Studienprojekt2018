import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TcpConnector{
	
	
	public static final byte[] intToByteArray(int value) {
	    return new byte[] { (byte) (value >>> 24), (byte) (value >>> 16), (byte) (value >>> 8), (byte) value };
	}
	
	public static final int intFromByteArray(byte[] bytes) {
	     return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}
	
	
	/*
	 * Diese Klasse uebernimmt die gesamte Kommunikation mit dem Server
	 */

	private String serverName;
	private int port;
	private Socket socket;
	private DataOutputStream output;
	private DataInputStream input;
	
	public TcpConnector(String serverName, int port){
		this.serverName = serverName;
		this.port = port;
	}
	
	
	private int leseInteger() throws Exception {
		// return input.readInt();
		// return (int) Math.round(input.readUnsignedByte() + Math.pow(2, 8) * input.readUnsignedByte() + Math.pow(2, 16) * input.readUnsignedByte() + Math.pow(2, 24) * input.readUnsignedByte());
		
		byte[] bytes = new byte[4];
		for (int i = 3; i >= 0; i--) {
			bytes[i] = (byte) input.readByte();
		}
		
		return intFromByteArray(bytes);
	}
	
	private void schreibeInteger(int zahl) throws Exception {
		byte[] bytes = intToByteArray(zahl);
		for (int i = 3; i >=0; i--) {
			output.write(bytes[i]);
		}
	}
	
	/*
	 * Verbindung zum Server herstellen
	 */
	public void establishConnection(String unserName) throws Exception{
		// Verbindung herstellen
	    this.socket = new Socket(serverName, port);
	    // output und input-Streams holen
	    Thread.sleep(10);
	    this.output = new DataOutputStream(socket.getOutputStream());
	    this.input = new DataInputStream(socket.getInputStream());
	    
	    // unseren Teamnamen an den Server senden
	    byte[] bytes = unserName.getBytes();
	    // TODO: eventuell muss das byte Array hier noch gespiegelt werden
		output.write(bytes);
	    //output.writeBytes(unserName);
	    output.flush();
	}
	
	/*
	 * Verbindung zum Server trennen
	 */
	public void closeConnection() throws Exception{
	    socket.close();
	}
	
	/*
	 * Grunddaten des Problems abfragen und als Instanz-Objekt an Loeser weitergeben
	 */
	public Instanz frageGrunddatenAb() throws Exception{
		// warten bis Server Daten gesendet hat
		int attempts = 0;
        while(input.available() == 0 && attempts < 3000)
        {
            attempts++;
            Thread.sleep(10); // 0.01 Sekunde
        }
        
        
        // vom Server gesendete Daten lesen
		int startkapital = leseInteger();
		int perioden = leseInteger();
		int produkte = leseInteger();
		int lagervolumen = leseInteger();
		int lagerkosten = leseInteger();
		int fixkosten = leseInteger();
		int[] produktionsschranke = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			produktionsschranke[i] = leseInteger();
		}
		int[] herstellkosten = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			herstellkosten[i] =  leseInteger();
		}
		int[] verkaufserloes = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			verkaufserloes[i] = leseInteger();
		}
		int[] wegwerfkosten = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			wegwerfkosten[i] = leseInteger();
		}
		int[] volumen = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			volumen[i] = leseInteger();
		}
		int[] erwarteteNachfrage = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			erwarteteNachfrage[i] = leseInteger();
		}
		int[] varianzNachfrage = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			varianzNachfrage[i] = leseInteger();
		}
		
		/*
		 * Ausgabe nur fuer DEBUGGING
		 * TODO: im finalen Programm auskommentieren !!!
		 */
		
		System.out.println("Startkapital: " + startkapital);
		System.out.println("Perioden: " + perioden);
		System.out.println("Produkte: " + produkte);
		System.out.println("Lagervolumen: " + lagervolumen);
		System.out.println("Lagerkosten: " + lagerkosten);
		System.out.println("Fixkosten: " + fixkosten);
		System.out.println("Produktionsschranken: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + produktionsschranke[i]);
		}
		System.out.println("");
		System.out.println("Herstellkosten: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + herstellkosten[i]);
		}
		System.out.println("");
		System.out.println("Verkaufserloes: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + verkaufserloes[i]);
		}
		System.out.println("");
		System.out.println("Wegwerfkosten: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + wegwerfkosten[i]);
		}
		System.out.println("");
		System.out.println("Volumen: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + volumen[i]);
		}
		System.out.println("");
		System.out.println("Erwartungswert der Nachfrage: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + erwarteteNachfrage[i]);
		}
		System.out.println("");
		System.out.println("Varianz der Nachfrage: ");
		for (int i = 0; i < produkte; i++) {
			System.out.print(" " + varianzNachfrage[i]);
		}
		System.out.println("");
		
		
		// Eingelesene Instanz an Programm zurueckgeben
		return new Instanz(startkapital, perioden, produkte, lagervolumen, lagerkosten, fixkosten, produktionsschranke, herstellkosten, verkaufserloes, wegwerfkosten, volumen, erwarteteNachfrage, varianzNachfrage);
	}
	
	/*
	 * Produktionsentscheidung der ersten Periode an Server schicken und aktuellen Bestand in inst aktualisieren
	 */
	public void sendeProduktionsentscheidungDerErstenPeriode(Instanz inst, int[] produktion) throws Exception{
		// Fuer jedes Produkt Produktionsmenge senden
	    for (int i = 0; i < produktion.length; i++) {
	    	schreibeInteger(produktion[i]);
	    }
	    output.flush();
	    
	    // warten bis Server Daten gesendet hat
 		int attempts = 0;
        while(input.available() == 0 && attempts < 3000)
        {
            attempts++;
            Thread.sleep(10); // 0.01 Sekunde
        }
         
        // vom Server gesendete Daten lesen
	    int kapital = leseInteger();
	    int[] ueberschuss = new int[inst.produkte];
	    for (int i = 0; i < inst.produkte; i++) {
	    	ueberschuss[i] = leseInteger();
	    }
	    
	    inst.aktuellesKapital = kapital;
	    inst.aktuellerBestand = ueberschuss.clone();
	}
	
	/*
	 * Lager- und Produktionsentscheidung an Server schicken und aktuellen Bestand in inst aktualisieren
	 */
	public void sendeEntscheidungen(Instanz inst, int[][] lagerung, int[] produktion) throws Exception{
		// Anzahl verwendeter Lager senden
		schreibeInteger(lagerung.length);
		// Fuer jedes Lager Array mit Lagermenge von jedem Produkt senden
		for (int k = 0; k < lagerung.length; k++) {
			for (int i = 0; i < lagerung[k].length; i++) {
		    	schreibeInteger(lagerung[k][i]);
		    }
	    }
		// Fuer jedes Produkt Produktionsmenge senden
	    for (int i = 0; i < produktion.length; i++) {
	    	schreibeInteger(produktion[i]);
	    }
	    output.flush();
	    
	    // warten bis Server Daten gesendet hat
 		int attempts = 0;
        while(input.available() == 0 && attempts < 3000)
        {             
        	attempts++;
            Thread.sleep(10); // 0.01 Sekunde
        }
        
        // vom Server gesendete Daten lesen
	    int kapital = leseInteger();
	    int[] ueberschuss = new int[inst.produkte];
	    for (int i = 0; i < inst.produkte; i++) {
	    	ueberschuss[i] = leseInteger();
	    }
	    
	    inst.aktuellesKapital = kapital;
	    inst.aktuellerBestand = ueberschuss.clone();
	}
}
