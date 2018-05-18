import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class Stichprobenmodell {

	public static void main(String[] args) {
		
		
		//*********************************************************
		//******************** Welche Instanz? ********************
		
		String dateipfad = "C:\\Users\\Jan\\Desktop\\Instanzen\\";
		String dateiname = "50Produkte";
		
		//******************** Welche Instanz? ********************
		//*********************************************************
		
		
		//***********************************************************
		//******************** Globale Parameter ********************
		/*
		 * Es stehen erst wenige Einstellmöglichkeiten zur Verfügung.
		 * Viele weitere müssen noch implementiert werden.
		 */
		
		int anzahlSzenarien = 300; // Anzahl der Szenarien, die in jeder Periode zufaellig neu generiert und gleich gewichtet werden
		double gamma = 0.95; // Faktor, um den das Volumen eines Lagers "geschrumpft" wird: V -> gamma * V
		double epsilon = 0.0; // Erhoehung des Volumens der zu lagernden Produkte um epsilon * V
		int maxLaufzeitProduktion = 300; // maximale Laufzeit fuer die Produktionsentscheidung (Gurobi)
		int maxLaufzeitLagern = 10; // maximale Laufzeit fuer die Lager- und Wegwerfentscheidung(Gurobi)
		
		//******************** Globale Parameter ********************
		//***********************************************************
		
		
		//**************************************************************
		//******************** Einlesen der Instanz ********************
		
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
            BufferedReader bufferedReader = new BufferedReader (new FileReader(dateipfad + dateiname + ".in"));
            String line = bufferedReader.readLine();
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
            for (int produkt = 0; produkt < n; produkt++) {
            	line = bufferedReader.readLine();
            	lineValues = line.split(delims);
            	c[produkt] = Integer.parseInt(lineValues[0]);
            	p[produkt] = Integer.parseInt(lineValues[1]);
            	v[produkt] = Integer.parseInt(lineValues[2]);
            	w[produkt] = Integer.parseInt(lineValues[3]);
            	b[produkt] = Integer.parseInt(lineValues[4]);           	
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
        int m = -1;
        int[][] d = {{}};       
        try {
            String delims = "[ ]+";
            BufferedReader bufferedReader = new BufferedReader (new FileReader(dateipfad + dateiname + ".dem"));
            String line = bufferedReader.readLine();
            m = Integer.parseInt(line);           
            d = new int[m][n];            
            for (int periode = 0; periode < m; periode++) {
            	line = bufferedReader.readLine();
            	String[] lineValues = line.split(delims);
            	for (int produkt = 0; produkt < n; produkt++) {
            		d[periode][produkt] = Integer.parseInt(lineValues[produkt]);
            	}
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }        
        int[] erwartungswerte = new int[n];
        int[] varianzen = new int[n];
        try {
            String delims = "[ ]+";
            BufferedReader bufferedReader = new BufferedReader (new FileReader(dateipfad + dateiname + ".deminfo"));
            String line = bufferedReader.readLine(); // in der ersten Zeile steht nur noch mal die Anzahl der Perioden, d.h. m
            for (int produkt = 0; produkt < n; produkt++) {
            	line = bufferedReader.readLine();
            	String[] lineValues = line.split(delims);
            	erwartungswerte[produkt] = Integer.parseInt(lineValues[0]);
            	varianzen[produkt] = Integer.parseInt(lineValues[1]);
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
        
        //******************** Einlesen der Instanz ********************
        //**************************************************************
        
        
        //***********************************************************
        //******************** Initialisierungen ********************        
        
        int[] aktuellerBestand = new int[n]; // fuer jedes Produkt wird zu jedem Zeitpunkt der aktuelle Bestand nachgehalten; er beträgt 0 zu Beginn
        long aktuellesKapital = S; // Variable, in der zu jedem Zeitpunkt das aktuelle Kapital gespeichert wird
        LogWriter logWriter = new LogWriter(dateipfad, dateiname + ".log"); // zum Anlegen und Schreiben der .log-Datei
        
        //******************** Initialisierungen ******************** 
        //***********************************************************
        
        
        //**********************************************************************
        //******************** Optimierung ueber m Perioden ********************   
        
        for (int periode = 0; periode < m; periode++) {
        	System.out.println("Kapital zu Beginn von Periode " + (periode + 1) + " von " + m + ": " + aktuellesKapital);
        	aktuellesKapital -= F; // Fixkosten werden bezahlt
        	if (aktuellesKapital < 0) {
        		System.out.println("Pleite!");
        		System.exit(0);
        	}
        	
        	/*
        	 * Zu Beginn jeder Periode werden so viele Szenarien, d.h. Realisierungen der Nachfrage, erzeugt, wie in "anzahlSzenarien" angegeben ist
        	 */
        	
        	int[][] nachfrageSzenario = new int[anzahlSzenarien][n];
    		for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
    			for (int produkt = 0; produkt < n; produkt++) {
    				nachfrageSzenario[szenario][produkt] = erzeugePositiveGanzzahligeNormalverteilteNachfrage(erwartungswerte[produkt], varianzen[produkt]);
    			}
    		}
    		
    		/*
    		 * Anschliessend wird das Modell aufgestellt und geloest.
    		 */
    		
    		try {
    			GRBEnv env = new GRBEnv();
                GRBModel model = new GRBModel(env);
                
                /*
                 * Es folgen die Variablen für:
                 * - Produktionsmenge
                 * - Anzahl der benoetigten Lager
                 * - Verkaufsmenge
                 * - Wegwerfmenge
                 * - Lagermenge
                 * - Ertrag
                 */
                
                GRBVar[] produktionsmenge = new GRBVar[n];
                for (int produkt = 0; produkt < n; produkt++) {                	
            		produktionsmenge[produkt] = model.addVar(0, b[produkt], 0, GRB.INTEGER, "x_" + String.valueOf(produkt));                	
                }
                GRBVar[] anzahlBenoetigterLager = new GRBVar[anzahlSzenarien];
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {                	
            		anzahlBenoetigterLager[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "z_" + String.valueOf(szenario));                	
                }
                GRBVar[][] verkaufsmenge = new GRBVar[anzahlSzenarien][n];
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		verkaufsmenge[szenario][produkt] = model.addVar(0, nachfrageSzenario[szenario][produkt], 0, GRB.INTEGER, "s_" + String.valueOf(szenario) + "_" + String.valueOf(produkt)); 
                	}
                }
                GRBVar[][] wegwerfmenge = new GRBVar[anzahlSzenarien][n];
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		wegwerfmenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "t_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
                	}
                }
                GRBVar[][] lagermenge = new GRBVar[anzahlSzenarien][n];
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		lagermenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "y_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
                	}
                }
                GRBVar[] ertrag = new GRBVar[anzahlSzenarien];
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
            		ertrag[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "E_" + String.valueOf(szenario)); 
                }
                               
                /*
                 * Es folgen die Nebenbedingungen
                 */
                               
                /*
                 * Nicht mehr produzieren als das aktuelle Kapital zulaesst
                 */
                
                GRBLinExpr expr2 = new GRBLinExpr();
                for (int produkt = 0; produkt < n; produkt++) {
                	expr2.addTerm(c[produkt], produktionsmenge[produkt]);
                }
                model.addConstr(expr2, GRB.LESS_EQUAL, aktuellesKapital, "Kapitalbeschraenkung"); 
                
                
                /*
                 * Nebenbedingungen an die Verkaufsmenge
                 */
                
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		GRBLinExpr exprLinks = new GRBLinExpr();
                		exprLinks.addTerm(1, verkaufsmenge[szenario][produkt]);
                		GRBLinExpr exprRechts = new GRBLinExpr();
                		exprRechts.addTerm(1, produktionsmenge[produkt]);
                		exprRechts.addConstant(aktuellerBestand[produkt]);
                		model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "Beschraekung der Verkaufsmenge von Produkt " + String.valueOf(produkt) + " in Szenario " + String.valueOf(szenario)); 
                	}
                }
                
                
                /*
                 * Die Lagermenge ist die Differenz aus Bestand + Produktion und Verkauf + Wegwerfen
                 */
                
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		GRBLinExpr exprLinks = new GRBLinExpr();
                		exprLinks.addTerm(1, lagermenge[szenario][produkt]);
                		GRBLinExpr exprRechts = new GRBLinExpr();
                		exprRechts.addTerm(1, produktionsmenge[produkt]);
                		exprRechts.addConstant(aktuellerBestand[produkt]);
                		exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt]);
                		exprRechts.addTerm(-1, wegwerfmenge[szenario][produkt]);
                		model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "was"); 
                	}
                }
                
                
                /*
                 * Die Lagermenge ist höchstens so hoch wie : bestand + produktion - verkauf
                 */
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		GRBLinExpr exprLinks = new GRBLinExpr();
                		exprLinks.addTerm(1, lagermenge[szenario][produkt]);
                		GRBLinExpr exprRechts = new GRBLinExpr();
                		exprRechts.addTerm(1, produktionsmenge[produkt]);
                		exprRechts.addConstant(aktuellerBestand[produkt]);
                		exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt]);
                		model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was"); 
                	}
                }
                
                /*
                 * Die Wegwerfmenge ist höchstens so hoch wie : bestand + produktion - verkauf
                 */
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	for (int produkt = 0; produkt < n; produkt++) {
                		GRBLinExpr exprLinks = new GRBLinExpr();
                		exprLinks.addTerm(1, wegwerfmenge[szenario][produkt]);
                		GRBLinExpr exprRechts = new GRBLinExpr();
                		exprRechts.addTerm(1, produktionsmenge[produkt]);
                		exprRechts.addConstant(aktuellerBestand[produkt]);
                		exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt]);
                		model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was"); 
                	}
                }
                
                
                /*
                 * Anzahl der benoetigten Lager bestimmen
                 */
                
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	GRBLinExpr exprLinks = new GRBLinExpr();
                	exprLinks.addTerm(gamma * V, anzahlBenoetigterLager[szenario]);
                	GRBLinExpr exprRechts = new GRBLinExpr();
                	for (int produkt = 0; produkt < n; produkt++) {
                		exprRechts.addTerm(v[produkt], lagermenge[szenario][produkt]);
                	}
                	exprRechts.addConstant(epsilon * V);
                	model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "was"); 
                }
                
                
                /*
                 * Ertrag in jedem Szenario:
                 */
                
                for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
                	GRBLinExpr exprLinks = new GRBLinExpr();
                	exprLinks.addTerm(1, ertrag[szenario]);
                	GRBLinExpr exprRechts = new GRBLinExpr();
                	exprRechts.addTerm(-C, anzahlBenoetigterLager[szenario]);
                	for (int produkt = 0; produkt < n; produkt++) {
                		exprRechts.addTerm(p[produkt], verkaufsmenge[szenario][produkt]);
                		exprRechts.addTerm(c[produkt], lagermenge[szenario][produkt]);
                		exprRechts.addTerm(-w[produkt], wegwerfmenge[szenario][produkt]);
                	}
                	model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "was"); 
                }
                

                /*
                 * Zielfunktion aufstellen (da jedes Szenario dieselbe Wahrscheinlichkeit hat, wird der Teil mit der Wahrscheinlichkeit weggelassen)
                 */
         
      			GRBLinExpr expr = new GRBLinExpr();
      			for (int produkt = 0; produkt < n; produkt++) {
      				expr.addTerm(-c[produkt], produktionsmenge[produkt]);
      			}
      			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
      				expr.addTerm(1.0 / anzahlSzenarien, ertrag[szenario]);
      			}    			
      			model.setObjective(expr, GRB.MAXIMIZE);
      			//model.write(dateipfad + "modell.mps");
      			model.set("MIPGap", "0.0");
      			model.set("MIPGapAbs", "0.0");
      			model.set("TimeLimit", String.valueOf(maxLaufzeitProduktion));
      			//model.set("CliqueCuts", "0");
      			//model.set("CoverCuts", "0");
      			//model.set("FlowCoverCuts", "0");
      			//model.set("FlowPathCuts", "0");
      			//model.set("GUBCoverCuts", "0");
      			//model.set("ImpliedCuts", "0");
      			//model.set("MIPSepCuts", "0");
      			//model.set("MIRCuts", "2");
      			//model.set("StrongCGCuts", "0");
      			//model.set("ModKCuts", "0");
      			//model.set("NetworkCuts", "0");
      			//model.set("ProjImpliedCuts", "0");
      			//model.set("SubMIPCuts", "0");
      			//model.set("ZeroHalfCuts", "0");
      			//model.set("InfProofCuts", "0");
      			//model.set("CutPasses", "0");
      			//model.set("GomoryPasses", "1000000");
      			//model.set("FeasibilityTol", "1e-9");
      			//model.set("IntFeasTol", "1e-9");
      			//model.set("OptimalityTol", "1e-9"); 
      			//model.set("LogToConsole", "0");                
      			model.optimize();
      			
      			
      			int[] wirdNunProduziert = new int[n];
      			for (int produkt = 0; produkt < n; produkt++) {
      				wirdNunProduziert[produkt] = (int) Math.round(produktionsmenge[produkt].get(GRB.DoubleAttr.X));
      			}
      			
      			for (int produkt = 0; produkt < n; produkt++) {
      				aktuellesKapital -= c[produkt] * wirdNunProduziert[produkt];
      				aktuellerBestand[produkt] += wirdNunProduziert[produkt];
      			}
      			
      			logWriter.schreibeProduktion(wirdNunProduziert);
      			
      			if (aktuellesKapital < 0) {
      				System.out.println("Pleite!");
      				System.exit(0);
      			}
 			
      			model.dispose();
      			env.dispose();
              } catch (GRBException e) {
            	  System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
            	  e.printStackTrace();
              }
    		
    		
    		/*
    		 * Jetzt wird verkauft!
    		 */
    		
        	for (int produkt = 0; produkt < n; produkt++) {      		
        		int verkaufsmenge = Math.min(aktuellerBestand[produkt], d[periode][produkt]);
        		aktuellesKapital += p[produkt] * verkaufsmenge;
        		aktuellerBestand[produkt] -= verkaufsmenge;
        	}
    		
    		
        	
        	BinPacking binPacking = new BinPacking(10000000, V, v, aktuellerBestand);
        	binPacking.firstFitDecreasing();
	
        	int maxLager = binPacking.obereSchrankeAnBenoetigteBins();
        	
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
       		
   			model.set("MIPGap", "0.0");
   			model.set("MIPGapAbs", "0.0");
   			model.set("TimeLimit", String.valueOf(maxLaufzeitLagern));
   			model.set("LogToConsole", "0");
             
   			model.optimize();
             
            
   			
   			
   			// Bestimme Anzahl verwendeter Lager:
   			int anzahlLager = 0;
   			for (int k = 0; k < maxLager; k++) {
   				anzahlLager += Math.round(lagerVerwenden[k].get(GRB.DoubleAttr.X));
   			}
   			
   			int[][] logLager = new int[anzahlLager][n];
   			
   			for (int k = 0; k < anzahlLager; k++) {
					for (int produkt = 0; produkt < n; produkt++) {
						logLager[k][produkt] = (int) Math.round(lagermenge[produkt][k].get(GRB.DoubleAttr.X));
					}

   			}
   			
   			
   			// Lagerung in .log-Datei schreiben
   			
   			logWriter.schreibeLager(logLager);
   			
   			// Lagerkosten zahlen:
   			aktuellesKapital -= C * anzahlLager;
   			
   			
   			// Kosten f�rs Wegwerfen zahlen
   			for (int i = 0; i < n; i++) {
   				aktuellesKapital -= w[i] * Math.round(wegwerfen[i].get(GRB.DoubleAttr.X));
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
        	
        	
        	
        	
    		
        }
        logWriter.schreibeKapital(aktuellesKapital);
        
        System.out.println("Kapital: " + aktuellesKapital);
	}
	
	public static int erzeugePositiveGanzzahligeNormalverteilteNachfrage(int erwartungswert, int varianz) {
		assert (erwartungswert >= 0 || varianz >= 0) : "Erwartungswert oder Varianz negativ!";
		SecureRandom random = new SecureRandom();
		//Random random = new Random();
		int nachfrage = -1;
		while (nachfrage < 0) {
			nachfrage = (int) Math.round(varianz * random.nextGaussian() + erwartungswert);
		}
		return nachfrage;
	}

}
