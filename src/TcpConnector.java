import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TcpConnector{
	
	/*
	 * Diese Klasse uebernimmt die gesamte Kommunikation mit dem Server
	 */

	private String serverName;
	private int port;
	private Socket socket;
	
	public TcpConnector(String serverName, int port){
		this.serverName = serverName;
		this.port = port;
	}
	
	/*
	 * Verbindung zum Server herstellen
	 */
	public void establishConnection(String unserName) throws Exception{
	    this.socket = new Socket(serverName, port);
	    DataOutputStream output = new DataOutputStream(socket.getOutputStream());
	    output.writeBytes(unserName);
	    output.flush();
	    output.close();
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
		DataInputStream input = new DataInputStream(socket.getInputStream());
		
		int startkapital = input.readInt();
		int perioden = input.readInt();
		int produkte = input.readInt();
		int lagervolumen = input.readInt();
		int lagerkosten = input.readInt();
		int fixkosten = input.readInt();
		int[] produktionsschranke = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			produktionsschranke[i] = input.readInt();
		}
		int[] herstellkosten = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			herstellkosten[i] = input.readInt();
		}
		int[] verkaufserloes = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			verkaufserloes[i] = input.readInt();
		}
		int[] wegwerfkosten = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			wegwerfkosten[i] = input.readInt();
		}
		int[] volumen = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			volumen[i] = input.readInt();
		}
		int[] erwarteteNachfrage = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			erwarteteNachfrage[i] = input.readInt();
		}
		int[] varianzNachfrage = new int[produkte];
		for (int i = 0; i < produkte; i++) {
			varianzNachfrage[i] = input.readInt();
		}
		
		return new Instanz(startkapital, perioden, produkte, lagervolumen, lagerkosten, fixkosten, produktionsschranke, herstellkosten, verkaufserloes, wegwerfkosten, volumen, erwarteteNachfrage, varianzNachfrage);
	}
	
	/*
	 * Produktionsentscheidung der ersten Periode an Server schicken und aktuellen Bestand in inst aktualisieren
	 */
	public void sendeProduktionsentscheidungDerErstenPeriode(Instanz inst, int[] produktion) throws Exception{
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		// Fuer jedes Produkt Produktionsmenge
	    for (int i = 0; i < produktion.length; i++) {
	    	output.writeInt(produktion[i]);
	    }
	    output.flush();
	    output.close();
	    
	    DataInputStream input = new DataInputStream(socket.getInputStream());
	    int kapital = input.readInt();
	    int[] ueberschuss = new int[inst.produkte];
	    for (int i = 0; i < inst.produkte; i++) {
	    	ueberschuss[i] = input.readInt();
	    }
	    input.close();
	    
	    if(kapital != inst.aktuellesKapital) {
	    	System.out.println("Fehler in der Kapitalberechnung");
	    	inst.aktuellesKapital = kapital;
	    }
	    inst.aktuellerBestand = ueberschuss.clone();
	}
	
	/*
	 * Lager- und Produktionsentscheidung an Server schicken und aktuellen Bestand in inst aktualisieren
	 */
	public void sendeEntscheidungen(Instanz inst, int[][] lagerung, int[] produktion) throws Exception{
		DataOutputStream output = new DataOutputStream(socket.getOutputStream());
		// Anzahl verwendeter Lager
		output.writeInt(lagerung.length);
		// Fuer jedes Lager Array mit Lagermenge von jedem Produkt
		for (int k = 0; k < lagerung.length; k++) {
			for (int i = 0; i < lagerung[k].length; i++) {
		    	output.writeInt(lagerung[k][i]);
		    }
	    }
		// Fuer jedes Produkt Produktionsmenge
	    for (int i = 0; i < produktion.length; i++) {
	    	output.writeInt(produktion[i]);
	    }
	    output.flush();
	    output.close();
	    
	    DataInputStream input = new DataInputStream(socket.getInputStream());
	    int kapital = input.readInt();
	    int[] ueberschuss = new int[inst.produkte];
	    for (int i = 0; i < inst.produkte; i++) {
	    	ueberschuss[i] = input.readInt();
	    }
	    input.close();
	    
	    if(kapital != inst.aktuellesKapital) {
	    	System.out.println("Fehler in der Kapitalberechnung");
	    	inst.aktuellesKapital = kapital;
	    }
	    inst.aktuellerBestand = ueberschuss.clone();
	}
}
