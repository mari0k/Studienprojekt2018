import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MainDeterministic {
	
	private static String delims = "[ ]+";

	public static void main(String[] args) {
		
		String speicherort = "./instances/";
		String dateinameInstanz = "Instanz1.in";
		String dateinameNachfrage = "Instanz1.dem";
		int maxLager = 50;
		
		
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
            BufferedReader br = new BufferedReader (new FileReader(speicherort + dateinameInstanz));
            String line = br.readLine();
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
            	line = br.readLine();
            	lineValues = line.split(delims);
            	c[i] = Integer.parseInt(lineValues[0]);
            	p[i] = Integer.parseInt(lineValues[1]);
            	v[i] = Integer.parseInt(lineValues[2]);
            	w[i] = Integer.parseInt(lineValues[3]);
            	b[i] = Integer.parseInt(lineValues[4]);           	
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
        //************* ENDE: Einlesen der Instanz (ohne Nachfrage) *************

        /*
        System.out.print(S + " " + F + " " + C + " " + V + " " + n );
        System.out.println();
        for (int i = 0; i < n; i++) {
        	 System.out.print(c[i] + " " + p[i] + " " + v[i] + " " + w[i] + " " + b[i] );
        	 System.out.println();
        }
        */
        
        //************* ANFANG: Einlesen der Nachfrage *************
        int m = -1;
        int[][] d = {{}};
        
        try {
            BufferedReader br = new BufferedReader (new FileReader(speicherort + dateinameNachfrage));
            String line = br.readLine();
            m = Integer.parseInt(line);
           
            d = new int[m + 1][n];
            
            for (int i = 1; i <= m; i++) {
            	line = br.readLine();
            	String[] lineValues = line.split(delims);
            	for (int j = 0; j < n; j++) {
            		d[i][j] = Integer.parseInt(lineValues[j]);
            	}
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
        //************* ENDE: Einlesen der Nachfrage *************
        
        /*
        System.out.println();
        System.out.println();
        for (int i = 0; i < d.length; i++) {
        	for (int j = 0; j < d[i].length; j++) {
        		System.out.print(d[i][j] + " ");
        	}
        	System.out.println();
        }
        */
        
        
        
        //************* ANFANG: Gurobi Model *************
        try {
            GRBEnv   env   = new GRBEnv();
            GRBModel model = new GRBModel(env);

            GRBVar[][] x = new GRBVar[m + 1][n];
            for (int i = 0; i < x.length; i++) {
            	for (int j = 0; j < x[i].length; j++) {
            		x[i][j] = model.addVar(0, b[j], 0, GRB.INTEGER, "was");
            	}
            }           
            GRBVar[][][] y = new GRBVar[m + 1][n][maxLager];
            for (int i = 0; i < y.length; i++) {
            	for (int j = 0; j < y[i].length; j++) {
            		for (int k = 0; k < y[i][j].length; k++) {
            			y[i][j][k] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "was");
            		}
            	}
            }
            GRBVar[][] s = new GRBVar[m + 1][n];
            for (int i = 0; i < s.length; i++) {
            	for(int j = 0; j < s[i].length; j++) {
            		s[i][j] = model.addVar(0, d[i][j], 0, GRB.INTEGER, "was");
            	}
            } 
            GRBVar[][] z = new GRBVar[m + 1][maxLager];
            for (int i = 0; i < z.length; i++) {
            	for (int j = 0; j < z[i].length; j++) {
            		z[i][j] = model.addVar(0, 1, 0, GRB.BINARY, "was");
            	}
            } 
            GRBVar[][] t = new GRBVar[m + 1][n];
            for (int i = 0; i < t.length; i++) {
            	for (int j = 0; j < t[i].length; j++) {
            		t[i][j] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "was");
            	}
            } 
            GRBVar[] K = new GRBVar[m + 2];
            for (int i = 0; i < K.length; i++) {
            	K[i] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "was");
            }
            
            
            
            

      		// Nebenbedingung (1):
      		for (int i = 0; i <= m; i++) {
      			for (int k = 0; k < maxLager; k++) {
      	      		GRBLinExpr exprLinks = new GRBLinExpr();
      	      		GRBLinExpr exprRechts = new GRBLinExpr();
      	      		exprRechts.addTerm(V, z[i][k]);
      	      		for (int j = 0; j < n; j++) {
      	      			exprLinks.addTerm(v[j], y[i][j][k]);      	      			
      	      		}
      	      		model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was");
      			}
      		}
      		
      		
      		/*
      		// Nebenbedingung (2):
      		for (int i = 0; i < m; i++) {
  	      		GRBLinExpr exprLinks = new GRBLinExpr();
  	      		GRBLinExpr exprRechts = new GRBLinExpr();
  	      		exprRechts.addTerm(1, K[i]);
  	      		for (int j = 0; j < n; j++) {
  	      			exprLinks.addTerm(c[j], x[i][j]);
  	      		}
  	      		for (int k = 0; k < maxLager; k++) {
  	      			exprLinks.addTerm(C, z[i][k]);
  	      		}
  	      		for (int j = 0; j < n; j++) {
  	      			exprLinks.addTerm(w[j], t[i][j]);
  	      		}
  	      		exprLinks.addConstant(F);
  	      		model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "was");
      		}
      		*/
      		
      		
      		// Nebenbedingung (3):
      		for (int i = 1; i <= m + 1; i++) {
  	      		GRBLinExpr exprLinks = new GRBLinExpr();
  	      		GRBLinExpr exprRechts = new GRBLinExpr();
  	      		exprRechts.addTerm(1, K[i]);
  	      		exprLinks.addTerm(1, K[i - 1]);
  	      		for (int j = 0; j < n; j++) {
  	      			exprLinks.addTerm(p[j], s[i - 1][j]);
  	      		}
  	      		for (int j = 0; j < n; j++) {
  	      			exprLinks.addTerm(-c[j], x[i - 1][j]);
  	      		}
  	      		for (int j = 0; j < n; j++) {
  	      			exprLinks.addTerm(-w[j], t[i - 1][j]);
  	      		}
  	      		for (int k = 0; k < maxLager; k++) {
  	      			exprLinks.addTerm(-C, z[i - 1][k]);
  	      		}
  	      		exprLinks.addConstant(-F);
  	      		model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "was");
      		}
      		
      		
      		// Nebenbedingung (4):
      		for (int i = 1; i <= m; i++) {
      			for (int j = 0; j < n; j++) {
      	      		GRBLinExpr exprLinks = new GRBLinExpr();
      	      		GRBLinExpr exprRechts = new GRBLinExpr();
      	      		for (int k = 0; k < maxLager; k++) {
      	      			exprLinks.addTerm(1, y[i][j][k]);
      	      		}
      	      		for (int k = 0; k < maxLager; k++) {
      	      			exprRechts.addTerm(1, y[i - 1][j][k]);
      	      		}
      	      		exprRechts.addTerm(1, x[i - 1][j]);
      	      		exprRechts.addTerm(-1, s[i][j]);
      	      		exprRechts.addTerm(-1, t[i][j]);
      	      		model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "was");   	      		
      			}
      		}
      		
      			
      		// Nebenbedingung (5)
  			GRBLinExpr expr = new GRBLinExpr();
      		expr.addTerm(1, K[0]);
			model.addConstr(expr, GRB.EQUAL, S, "was");
      				
			
			for (int j = 0; j < n; j++) {
				expr = new GRBLinExpr();
				expr.addTerm(1, s[0][j]);
				model.addConstr(expr, GRB.EQUAL, 0, "was");				
			}
			
			for (int j = 0; j < n; j++) {
				for (int k = 0; k < maxLager; k++) {
					expr = new GRBLinExpr();
					expr.addTerm(1, y[0][j][k]);
					model.addConstr(expr, GRB.EQUAL, 0, "was");		
				}
			}
			
			
			for (int k = 0; k < maxLager; k++) {
				expr = new GRBLinExpr();
				expr.addTerm(1, z[0][k]);
				model.addConstr(expr, GRB.EQUAL, 0, "was");				
			}
			
			
      		// WICHTIG: Zielfunktion neu setzen und MAXIMIEREN!!!!
      		
            expr = new GRBLinExpr();
            expr.addTerm(1, K[K.length - 1]);
            model.setObjective(expr, GRB.MAXIMIZE);
      		
            model.set("MIPGap", "1e-12");
            model.optimize();


            // Ausgabe:
            System.out.println();
            System.out.println();
            System.out.println();
            System.out.println("Kapital zu Beginn von Periode 0: " + Math.round(K[0].get(GRB.DoubleAttr.X)));
            System.out.println("Produktion in Periode 0:");
            for (int j = 0; j < n; j++) {
            	System.out.println("Produkt " + j + ": " + Math.round(x[0][j].get(GRB.DoubleAttr.X)) + " Einheiten");
            }
            
            for (int i = 1; i <= m; i++) {
            	System.out.println();
            	System.out.println("Kapital zu Beginn von Periode " + i + ": " + Math.round(K[i].get(GRB.DoubleAttr.X)));
            	System.out.println("Es wird verkauft:");
                for (int j = 0; j < n; j++) {
                	System.out.println("Produkt " + j + ": " + Math.round(s[i][j].get(GRB.DoubleAttr.X)) + " Einheiten");
                }
                System.out.println("Es wird weggeworfen:");
                for (int j = 0; j < n; j++) {
                	System.out.println("Produkt " + j + ": " + Math.round(t[i][j].get(GRB.DoubleAttr.X)) + " Einheiten");
                }
                for (int k = 0; k < maxLager; k++) {
                	if (Math.round(z[i][k].get(GRB.DoubleAttr.X)) == 1) {
                		System.out.println("In Lager " + k + " wird gelagert:");
                		for (int j = 0; j < n; j++) {
                			if ( Math.round(y[i][j][k].get(GRB.DoubleAttr.X)) != 0) {
                				System.out.println("Produkt " + j + ": " + Math.round(y[i][j][k].get(GRB.DoubleAttr.X)) + " Einheiten");
                			}
                		}
                	}
                }
                System.out.println("Produktion in Periode " + i + ":");
                for (int j = 0; j < n; j++) {
                	System.out.println("Produkt " + j + ": " + Math.round(x[i][j].get(GRB.DoubleAttr.X)) + " Einheiten");
                }
            }
            
            System.out.println("Kapital ganz am Ende (Beginn von Periode m+1): " + Math.round(K[m + 1].get(GRB.DoubleAttr.X)));
            
            

            model.dispose();
            env.dispose();

        } 
        catch (GRBException e) {
            System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
            e.printStackTrace();
        }
        //************* ENDE: Gurobi Model *************
        
        
        
        
	}

}
