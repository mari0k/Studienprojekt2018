package unused;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

/*
 * ANNAHMEN:
 * 		- unendlich Kapital
 * 		- erwartete Nachfrage <= Produktionsbeschränkung
 * 		- keine Fixkosten
 */

public class MainErwartungswert {

	public static void main(String[] args) {
		
		String speicherort = "C:\\Users\\Jan\\Desktop\\Instanzen\\";
		String dateinameInstanz = "instance90.in";
		String dateinameNachfrage = "instance90.dem";
		String dateinameNachfrageVerteilung = "instance90.deminfo";
		int maxLager = 10;
		
		
		//************* ANFANG: Einlesen der Instanz (ohne Nachfrage) *************
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
            BufferedReader bwr = new BufferedReader (new FileReader(speicherort + dateinameInstanz));
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
      //************* ENDE: Einlesen der Instanz (ohne Nachfrage) *************

      //************* ANFANG: Einlesen der Nachfrage *************
        int m = -1;
        int[][] d = {{}};
        
        try {
            String delims = "[ ]+";
            BufferedReader bwr = new BufferedReader (new FileReader(speicherort + dateinameNachfrage));
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
      //************* ENDE: Einlesen der Nachfrage *************
        
        int[] erwartungswerte = new int[n];
        
        try {
            String delims = "[ ]+";
            BufferedReader bwr = new BufferedReader (new FileReader(speicherort + dateinameNachfrageVerteilung));
            String line = bwr.readLine();

            for (int i = 0; i < n; i++) {
            	line = bwr.readLine();
            	String[] lineValues = line.split(delims);
            	erwartungswerte[i] = Integer.parseInt(lineValues[0]);
            }
            bwr.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
      

        //****************************************************************
        // LOS GEHT's!!!
        
        int[] aktuellerBestand = new int[n];
        long K = S;
        for (int periode = 1; periode <= m; periode++) {
        	K -= F; // Fixkosten bezahlen;
        	/*
        	 * Produzieren bis zum Erwartungswert der Nachfrage
        	 */
        	for (int i = 0; i < n; i++) {
        		int produktionsmenge = Math.min(b[i], erwartungswerte[i] - aktuellerBestand[i]);
        		K -= produktionsmenge * c[i];
        		aktuellerBestand[i] += produktionsmenge;
        	}
        	
        	/*
        	 * So viel verkaufen wie möglich
        	 */
        	for (int i = 0; i < n; i++) {      		
        		int verkaufsmenge = Math.min(aktuellerBestand[i], d[periode][i]);
        		K += p[i] * verkaufsmenge;
        		aktuellerBestand[i] -= verkaufsmenge;
        	}
        	
        	/*
        	 * Lagern und wegwerfen
        	 */

        	 try {
                 GRBEnv   env   = new GRBEnv();
                 GRBModel model = new GRBModel(env);

                 GRBVar[] behalten = new GRBVar[n];
                 for (int i = 0; i < behalten.length; i++) {                	
             		behalten[i] = model.addVar(0, aktuellerBestand[i], 0, GRB.INTEGER, "was");                	
                 }           
                 GRBVar[] wegwerfen = new GRBVar[n];
                 for (int i = 0; i < wegwerfen.length; i++) {                	
                	 wegwerfen[i] = model.addVar(0, aktuellerBestand[i], 0, GRB.INTEGER, "was");                	
                 }
                 GRBVar[] lagerVerwenden = new GRBVar[maxLager];
                 for (int i = 0; i < lagerVerwenden.length; i++) {                	
                	 lagerVerwenden[i] = model.addVar(0, 1, 0, GRB.BINARY, "was");                	
                 }
                 GRBVar[][] lagermenge = new GRBVar[n][maxLager];
                 for (int i = 0; i < n; i++) {
                	 for (int k = 0; k < maxLager; k++) {
                		 lagermenge[i][k] = model.addVar(0, aktuellerBestand[i], 0, GRB.INTEGER, "was");
                	 }
                 }

           		
           		for (int i = 0; i < n; i++) {       			
       	      		GRBLinExpr expr = new GRBLinExpr();
       	      		expr.addTerm(1, behalten[i]);
       	      		expr.addTerm(1, wegwerfen[i]);
       	      		model.addConstr(expr, GRB.EQUAL, aktuellerBestand[i], "was");           			
           		}

           		for (int i = 0; i < n; i++) {
           			GRBLinExpr exprLinks = new GRBLinExpr();
           			GRBLinExpr exprRechts = new GRBLinExpr();
           			exprRechts.addTerm(1, behalten[i]);
           			for (int k = 0; k < maxLager; k++) {
           				exprLinks.addTerm(1, lagermenge[i][k]);
           			}
           			model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "was");    
           		}
     			
           		for (int k = 0; k < maxLager; k++) {
           			GRBLinExpr exprLinks = new GRBLinExpr();
           			GRBLinExpr exprRechts = new GRBLinExpr();
           			exprRechts.addTerm(V, lagerVerwenden[k]);
           			for (int i = 0; i < n; i++) {
           				exprLinks.addTerm(v[i], lagermenge[i][k]);
           			}
           			model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was");    
           		}
           		
     			
           		// Symmetrie verhindern
           		
           		for (int k = 0; k < maxLager - 1; k++) {
           			GRBLinExpr exprLinks = new GRBLinExpr();
           			GRBLinExpr exprRechts = new GRBLinExpr();
           			exprLinks.addTerm(1, lagerVerwenden[k]);
           			exprRechts.addTerm(1, lagerVerwenden[k + 1]);
           			model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "was");  
           		}
           		
           		

           		/*
           		 * Zielfunktion:
           		 * Produkte auf Lager werden mit ihren Produktionskosten
           		 * (positiv) und weggeworfene Produkte mit ihren Wegwerfkosten
           		 * (negativ) bewertet
           		 */
           		
       			GRBLinExpr expr = new GRBLinExpr();
       			
       			for (int i = 0; i < n; i++) {
       				expr.addTerm(c[i], behalten[i]);
       			}
       			
       			for (int i = 0; i < n; i++) {
       				expr.addTerm(-w[i], wegwerfen[i]);
       			}
       			
       			for (int k = 0; k < maxLager; k++) {
       				expr.addTerm(-C, lagerVerwenden[k]);
       			}
       			
       			model.setObjective(expr, GRB.MAXIMIZE);
           		
       			model.set("MIPGap", "0");
       			model.set("MIPGapAbs", "0.8");
       			//model.set("LogToConsole", "0");
                 
       			model.optimize();
                 
                
       			
       			
       			// Bestimme Anzahl verwendeter Lager:
       			int anzahlLager = 0;
       			for (int k = 0; k < maxLager; k++) {
       				anzahlLager += Math.round(lagerVerwenden[k].get(GRB.DoubleAttr.X));
       			}
       			// Lagerkosten zahlen:
       			K -= C * anzahlLager;
       			
       			
       			// Kosten fürs Wegwerfen zahlen
       			for (int i = 0; i < n; i++) {
       				K -= w[i] * Math.round(wegwerfen[i].get(GRB.DoubleAttr.X));
       			}
       			
       			
       			// aktuellen Bestand anpassen
       			
       			for (int i = 0; i < n; i++) {
       				aktuellerBestand[i] = (int) Math.round(behalten[i].get(GRB.DoubleAttr.X));
       			}

       			model.dispose();
       			env.dispose();

               } catch (GRBException e) {
                 System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
                 e.printStackTrace();
               }
        	
        
        	 System.out.println("Kapital am Ende von Periode " + periode + ": " + K);
        }
        
             
	}

}
