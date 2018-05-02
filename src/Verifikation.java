import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Verifikation {

	public static void main(String[] args) throws IOException {
		String dateiname = args[0];
		String pfad = "./";
		if (args.length == 2) {
			pfad =  args[1];
		}
		
		
		int S = -1;
		int F = -1;
		int C = -1;
		int V = -1;
		int n = -1;
		int[] c = {};
		int[] p = {};
		int[] v = {};
		int[] w = {};
		int[] b = {};				
        try {
            String delims = "[ ]+";
            BufferedReader bwr = new BufferedReader(new FileReader(pfad + dateiname + ".in"));
            String line = bwr.readLine();
            String[] lineValues = line.split(delims);
            S = Integer.parseInt(lineValues[0]);
            F = Integer.parseInt(lineValues[1]);
            C = Integer.parseInt(lineValues[2]);
            V = Integer.parseInt(lineValues[3]);
            n = Integer.parseInt(lineValues[4]);            
            c = new int[n];
            p = new int[n];
            v = new int[n];
            w = new int[n];
            b = new int[n];           
            for (int i = 0; i < n; i++) {
            	line = bwr.readLine();
            	lineValues = line.split(delims);
            	c[i] = Integer.parseInt(lineValues[0]);
            	p[i] = Integer.parseInt(lineValues[1]);
            	v[i] = Integer.parseInt(lineValues[2]);
            	w[i] = Integer.parseInt(lineValues[3]);
            	b[i] = Integer.parseInt(lineValues[4]);           	
            }
            bwr.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
		
		
		
        int m = -1;
        int[][] d = {{}};       
        try {
            String delims = "[ ]+";
            BufferedReader bwr = new BufferedReader(new FileReader(pfad + dateiname + ".dem"));
            String line = bwr.readLine();
            m = Integer.parseInt(line);          
            d = new int[m + 1][n];          
            for (int i = 1; i <= m; i++) {
            	line = bwr.readLine();
            	String[] lineValues = line.split(delims);
            	for (int j = 0; j < n; j++) {
            		d[i][j] = Integer.parseInt(lineValues[j]);
            	}
            }
            bwr.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
        
        
        
        long K = S;
        int[] bestand = new int[n];
        
        String delims = "[ ]+";
        BufferedReader br = new BufferedReader(new FileReader(pfad + dateiname + ".log"));
        String line;
        
        for (int periode = 1; periode <= m; periode++) {
        	K -= F;
        	if (K < 0) {
        		System.out.println("Nach Abziehen der Fixkosten in Periode " + periode + " ist das Kapital negativ.");
        		System.exit(1);
        	}
        	line = br.readLine();
        	String[] lineValues = line.split(delims);
        	for (int produkt = 0; produkt < lineValues.length; produkt++) {
        		int produktionsmenge = Integer.parseInt(lineValues[produkt]);
        		if (produktionsmenge < 0 || produktionsmenge > b[produkt]) {
        			System.out.println("Produktionsmenge von Produkt " + produkt + " in Periode " + periode + " unzulässig.");
        			System.exit(1);
        		}
        		bestand[produkt] += produktionsmenge;
        		K -= produktionsmenge * c[produkt];
        	}
        	if (K < 0) {
        		System.out.println("Kapital nach Produktionsphase in Periode " + periode + " negativ.");
        		System.exit(1);
        	}
        	for (int produkt = 0; produkt < n; produkt++) {
        		int verkaufsmenge = Math.min(bestand[produkt], d[periode][produkt]);
        		bestand[produkt] -= verkaufsmenge;
        		K += p[produkt] * verkaufsmenge;
        	}
        	line = br.readLine();
        	int anzahlLager = Integer.parseInt(line);
        	K -= C * anzahlLager;
        	if (K < 0) {
        		System.out.println("Kapital in Periode " + periode + " negativ, da zu viele Lager angemietet werden.");
        		System.exit(1);
        	}
        	int[] lagermenge = new int[n];
        	for (int lager = 0; lager < anzahlLager; lager++) {
        		int freieKapazitaet = V;
            	line = br.readLine();
            	lineValues = line.split(delims);
            	for (int produkt = 0; produkt < lineValues.length; produkt++) {
            		int menge = Integer.parseInt(lineValues[produkt]);
            		lagermenge[produkt] += menge;
            		freieKapazitaet -= v[produkt] * menge;
            	}
            	if (freieKapazitaet < 0) {
            		System.out.println("Das Lager " + lager + " in Periode " + periode + " ist zu voll.");
            		System.exit(1);
            	}
        	}
        	for (int produkt = 0; produkt < n; produkt++) {
        		int wegwerfmenge = bestand[produkt] - lagermenge[produkt];
        		if (wegwerfmenge < 0) {
        			System.out.println("In Periode " + periode + " wird von Produkt " + produkt + " mehr gelagert als verfügbar ist.");
        			System.exit(1);
        		}
        		bestand[produkt] -= wegwerfmenge;
        		K -= w[produkt] * wegwerfmenge;
        	}
        	if (K < 0) {
        		System.out.println("Nach dem Wegwerfen in Periode " + periode + " ist das Kapital negativ.");
        		System.exit(1);
        	}	
        }
        line = br.readLine();
        long kapitalAmEnde = Long.parseLong(line);
        if (kapitalAmEnde != K) {
        	System.out.println("Die Kapitalberechnung ist fehlerhaft. Es sollte " + K + " nach Periode " + m + " an Kapital vorhanden sein.");
        	System.exit(1);
        }
        System.out.println("Herzlichen Glückwunsch. Sie haben zulässige Entscheidungen getroffen.");
        
        br.close();
	}
}
