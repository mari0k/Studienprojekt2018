/*
 * ACHTUNG: diese Klasse, sowie die Klassen
 * "Bin" und "Element" muessen dringend bearbeitet
 * werden. Das Berechnen einer oberen Schranke fuer
 * die Anzahl der benoetigten Lager mittels
 * First Fit Decreasing funktioniert jedoch
 * eindwandfrei.
 */


import java.util.Arrays;
import java.util.LinkedList;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class BinPacking {

	private int maxSekunden; // maximale Laufzeit in Sekunden
	private int binKapazitaet; // die Kapazitaet EINES Bins
	private int anzahlVerschiedeneGroessen; // Anzahl der verschiedenen Groessen / Gewichte / Elemente
	private int[] groessen; // die verschiedenen Groessen / Gewichte / Elemente
	private int[] anzahlen; // die Anzahlen, d.h. Multiplizitäten eines Typs von Gewicht
	private Element[] alleElemente;
	private LinkedList<Bin> besteLoesung;
	
	
	public BinPacking(int maxSekunden, int binKapazitaet, int[] groessen, int[] anzahlen) {
		this.maxSekunden = maxSekunden;
		this.binKapazitaet = binKapazitaet;
		this.groessen = groessen;
		this.anzahlen = anzahlen;
		anzahlVerschiedeneGroessen = groessen.length;
		int gesamtAnzahlElemente = 0;
		for (int i = 0; i < anzahlVerschiedeneGroessen; i++) {
			gesamtAnzahlElemente += anzahlen[i];
		}
		alleElemente = new Element[gesamtAnzahlElemente];
		int aktuellesElement = -1;
		for (int i = 0; i < anzahlVerschiedeneGroessen; i++) {
			for (int j = 0; j < anzahlen[i]; j++) {
				aktuellesElement += 1;
				alleElemente[aktuellesElement] = new Element(groessen[i], i, j);
			}
		}
		Arrays.sort(alleElemente);
	}
	
	
	public void loeseMitGurobiGanzzahligesModell() {
		if (besteLoesung == null) {
			System.out.println("Es sollte erst eine zulässige Lösung berechnet und abgespeichert werden!");
			System.exit(0);
		}
		try {
			GRBEnv   env   = new GRBEnv();
            GRBModel model = new GRBModel(env);
            GRBVar[][] x = new GRBVar[alleElemente.length][besteLoesung.size()];
            for (int element = 0; element < x.length; element++) {
            	for (int bin = 0; bin < besteLoesung.size(); bin++) {
            		x[element][bin] = model.addVar(0, 1, 0, GRB.BINARY, "was");
            	}
            }
            GRBVar[] y = new GRBVar[besteLoesung.size()];
            for (int bin = 0; bin < y.length; bin++) {                	
        		y[bin] = model.addVar(0, 1, 1, GRB.BINARY, "was");                	
            }
            
            for (int bin = 0; bin < y.length; bin++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	for (int element = 0; element < alleElemente.length; element++) {
            		exprLinks.addTerm(alleElemente[element].getGroesse(), x[element][bin]);
            	}
            	GRBLinExpr exprRechts = new GRBLinExpr();
            	exprRechts.addTerm(binKapazitaet, y[bin]);
            	model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was"); 
            }
            
            for (int element = 0; element < alleElemente.length; element++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	for (int bin = 0; bin < y.length; bin++) {
            		exprLinks.addTerm(1, x[element][bin]);
            	}
            	model.addConstr(exprLinks, GRB.EQUAL, 1, "was"); 
            }

         	
            for (int bin = 0; bin < y.length - 1; bin++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	exprLinks.addTerm(1, y[bin]);
            	GRBLinExpr exprRechts = new GRBLinExpr();
            	exprRechts.addTerm(1, y[bin + 1]);
            	model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "was"); 
            }
            
            
  			model.set("MIPGap", "0");
  			model.set("MIPGapAbs", "0.8");
  			model.set("TimeLimit", "10000000");
  			model.set("FeasibilityTol", "1e-9");
  			model.set("IntFeasTol", "1e-9");
  			model.set("OptimalityTol", "1e-9"); 
  			//model.set("LogToConsole", "0");
  			
  			for (int bin = 0; bin < y.length; bin++) {
  				y[bin].set(GRB.DoubleAttr.Start, 1);
  			}
  			for (int element = 0; element < alleElemente.length; element++) {
  				for (int bin = 0; bin < y.length; bin++) {
  					if (alleElemente[element].getNummerBin() == bin) {
  						x[element][bin].set(GRB.DoubleAttr.Start, 1);
  					} else {
  						x[element][bin].set(GRB.DoubleAttr.Start, 0);
  					}
  				}
  			}
  			model.optimize();
  			model.dispose();
  			env.dispose();
          } catch (GRBException e) {
        	  System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        	  e.printStackTrace();
          }
	}
	
	
	
	
	
	
	public void loeseMitGurobiBinaeresModell() {
		if (besteLoesung == null) {
			System.out.println("Es sollte erst eine zulässige Lösung berechnet und abgespeichert werden!");
			System.exit(0);
		}
		try {
			GRBEnv   env   = new GRBEnv();
            GRBModel model = new GRBModel(env);
            GRBVar[][] x = new GRBVar[alleElemente.length][besteLoesung.size()];
            for (int element = 0; element < x.length; element++) {
            	for (int bin = 0; bin < besteLoesung.size(); bin++) {
            		x[element][bin] = model.addVar(0, 1, 0, GRB.BINARY, "was");
            	}
            }
            GRBVar[] y = new GRBVar[besteLoesung.size()];
            for (int bin = 0; bin < y.length; bin++) {                	
        		y[bin] = model.addVar(0, 1, 1, GRB.BINARY, "was");                	
            }
            
            for (int bin = 0; bin < y.length; bin++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	for (int element = 0; element < alleElemente.length; element++) {
            		exprLinks.addTerm(alleElemente[element].getGroesse(), x[element][bin]);
            	}
            	GRBLinExpr exprRechts = new GRBLinExpr();
            	exprRechts.addTerm(binKapazitaet, y[bin]);
            	model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was"); 
            }
            
            for (int element = 0; element < alleElemente.length; element++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	for (int bin = 0; bin < y.length; bin++) {
            		exprLinks.addTerm(1, x[element][bin]);
            	}
            	model.addConstr(exprLinks, GRB.EQUAL, 1, "was"); 
            }

         	
            for (int bin = 0; bin < y.length - 1; bin++) {
            	GRBLinExpr exprLinks = new GRBLinExpr();
            	exprLinks.addTerm(1, y[bin]);
            	GRBLinExpr exprRechts = new GRBLinExpr();
            	exprRechts.addTerm(1, y[bin + 1]);
            	model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "was"); 
            }
            
            
  			model.set("MIPGap", "0");
  			model.set("MIPGapAbs", "0.8");
  			model.set("TimeLimit", "10000000");
  			model.set("FeasibilityTol", "1e-9");
  			model.set("IntFeasTol", "1e-9");
  			model.set("OptimalityTol", "1e-9"); 
  			//model.set("LogToConsole", "0");
  			
  			for (int bin = 0; bin < y.length; bin++) {
  				y[bin].set(GRB.DoubleAttr.Start, 1);
  			}
  			for (int element = 0; element < alleElemente.length; element++) {
  				for (int bin = 0; bin < y.length; bin++) {
  					if (alleElemente[element].getNummerBin() == bin) {
  						x[element][bin].set(GRB.DoubleAttr.Start, 1);
  					} else {
  						x[element][bin].set(GRB.DoubleAttr.Start, 0);
  					}
  				}
  			}
  			model.optimize();
  			model.dispose();
  			env.dispose();
          } catch (GRBException e) {
        	  System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        	  e.printStackTrace();
          }
	}
	
	
	public void firstFitDecreasing() { // zusaetzlich koennte man immer noch die Bins nach ihrer Restkapazitaet sortieren
		LinkedList<Bin> bins = new LinkedList<Bin>();
		bins.add(new Bin(0, binKapazitaet));
		for (Element element : alleElemente) {
			boolean gepackt = false;
			for (Bin bin : bins) {
				if (bin.passtElementNochRein(element)) {
					bin.packeElementRein(element);
					gepackt = true;
					break;
				}
			}
			if (gepackt) {
				continue;
			}
			Bin neuesBin = new Bin(bins.size(), binKapazitaet);
			neuesBin.packeElementRein(element);
			bins.add(neuesBin);
		}
		if (besteLoesung == null || bins.size() < besteLoesung.size()) {
			besteLoesung = bins;
		}
	}
	
	
	public void gibDieGroessenDerElementeAus() {
		for (int i = 0; i < alleElemente.length; i++) {
			System.out.print(alleElemente[i].getGroesse() + " ");
		}
	}
	
	public int obereSchrankeAnBenoetigteBins() {
		if (besteLoesung == null) {
			System.out.println("Es wurde noch keine Lösung bestimmt!");
			System.exit(0);
		}
		return besteLoesung.size();
	}


	public LinkedList<Bin> getBesteLoesung() {
		return besteLoesung;
	}
	
}
