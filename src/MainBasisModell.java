import gurobi.*;

public class MainBasisModell {

	public static void main(String[] args) {
		String instanceName = null;
		boolean deminfo = true;
		/*
		 * Check if Arguments are given and overwrite the corresponding variables
		 */
		switch (args.length) {
			case 2: deminfo = Boolean.parseBoolean(args[1]);
			case 1: instanceName = args[0]; break;
			default: instanceName = "kleineInstanz"; break;
		}
		
		
		
		/*
		 * Read instance data and initialize DemandModule and LogWriter
		 */
		Instance inst = new Instance(instanceName, deminfo);
		DemandModule demandModule = new DemandModule(instanceName, inst.getN());
		LogWriter logWriter = new LogWriter("./instances/", instanceName + ".log");
		long Kapital = inst.getS();
		
		
		
		/*
		 * Initialize variables needed for solving
		 */
		int zeitpunkt = -1;
		long capital = inst.getS();
		int n = inst.getN();
		int C = inst.getC();
		int V = inst.getV();
		int[] c = inst.get_c();
		int[] b = inst.get_b();
		int[] p = inst.get_p();
		int[] v = inst.get_v();
		int[] w = inst.get_w();
		int[][] temp = FirstFitDecreasing.pack(V, b, v);
		int L = temp[0].length;	// upper bound for storage rooms needed (computed with FirstFitDecreasing)
		temp = null;
		int[] bestand = new int[n];
		for (int i = 0; i < n; i++) {
			bestand[i] = 0;
		}
		int[] mean = new int[n];
		double[] sd = new double[n];
		if (inst.isKnownDistributions()) {
			mean = inst.getMean();
			sd = inst.getSd();
		}
		
		
		
		/*
		 * Start to solve
		 */
		boolean isFirstPeriod = true;
		while (capital >= 0) {
			
			int[] production = null;
			int[][] storage = null;
			
			zeitpunkt++;
			
			int[][] a = new int[n][1];
			for (int i = 0; i < n; i++) {
				a[i][0] = bestand[i];
			}
			
			if (inst.isKnownDistributions() != true) {
				for (int i = 0; i < n; i++) {
					Double[] estimates = MaximumLikelihood.estimate(demandModule.getPastDemands().get(i));
					mean[i] = (int) Math.round(estimates[0]);
					sd[i] = (int) Math.round(estimates[2]);	// 2 = erwartungstreuer Schätzer (1 = ML Schätzer)
					//var[i] = (int) Math.max(0, Math.ceil(VaR.computeVaR(mean[i], sd[i])));
				}
			}
			
			int[] d = new int[n];	// Schätzer für Nachfrage am Ende der Periode
			for (int i = 0; i < n; i++) {
				d[i] = mean[i];		// Schätzer erstmal auf Erwartungswert setzen
			}

			

			/*
			 * Sell products
			 */
			if (!isFirstPeriod) {
				int[] demand = demandModule.getNewDemand();
				if (demand.length != n) break;
				int[] sell = new int[n];
				int sellProfit = 0;
				for (int i = 0; i < n; i++) {
					sell[i] = Math.max(0, Math.min(a[i][0], demand[i]));
					sellProfit += sell[i] * p[i];			
					a[i][0] -= sell[i];
				}
				// update capital
				capital += sellProfit;
			}

			
			
			/*
			 * Lagern/ Wegwerfen und Produzieren
			 */
			int storageCost = 0;
			int disposalCost = 0;
			int productionCost = 0;
			if (demandModule.wasLastPeriod()) {
				// es folgt KEINE weitere Periode. Entsorge kompletten Restbestand.
				storage = new int[0][n];
				for (int i = 0; i < n; i++) {
					disposalCost += a[i][0] * w[i];
					a[i][0] = 0;
				}
				capital -= disposalCost;
				if (capital < 0) {
					System.out.println("Zeitpunkt " + zeitpunkt + ": Bankrott durch Entsorgung in letzter Periode.");
					break;
				}
			} 
			else {
				// es folgt weitere Periode. Lagere oder entsorge Restbestand und Produziere.
				int[][] initial_z = FirstFitDecreasing.pack(V, bestand, v);
				int L_store = initial_z[0].length;
				L_store = L;
				// ************* ANFANG: Gurobi Model *************
				try {
					GRBEnv env = new GRBEnv();
					GRBModel model = new GRBModel(env);
	
					// Variablen
					GRBVar[][] x = new GRBVar[n][1];
					for (int i = 0; i < n; i++) {
						x[i][0] = model.addVar(0, b[1], 0, GRB.INTEGER, "x_" + i + "_" + zeitpunkt);
						//x[i][0].set(GRB.DoubleAttr.Start, 0.0);
					}
					GRBVar[][] y = new GRBVar[1][L_store];
					for (int k = 0; k < L_store; k++) {
						y[0][k] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + zeitpunkt + "_" + k);
						//y[0][k].set(GRB.DoubleAttr.Start, 1.0);
					}
					GRBVar[][][] z = new GRBVar[n][1][L_store];
					for (int i = 0; i < n; i++) {
						for (int k = 0; k < L_store; k++) {
							z[i][0][k] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "z_" + i + "_" + zeitpunkt + "_" + k);
	                		//z[i][0][k].set(GRB.DoubleAttr.Start, initial_z[i][k] * 1.0);
						}
					}
					GRBVar[][] s = new GRBVar[n][1];
					for (int i = 0; i < n; i++) {
						s[i][0] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "s_" + i + "_" + zeitpunkt);
						//s[i][0].set(GRB.DoubleAttr.Start, 0.0);
					}
					GRBVar[][] t = new GRBVar[n][1];
					for (int i = 0; i < n; i++) {
						t[i][0] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "t_" + i + "_" + zeitpunkt);
						//t[i][0].set(GRB.DoubleAttr.Start, 0.0);
					}
					GRBVar K = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "K_" + (zeitpunkt + 1));
					//K.set(GRB.DoubleAttr.Start, 0.0);
					
	
					// Zielfunktion
					GRBLinExpr expr = new GRBLinExpr();
					expr.addTerm(1, K);
					model.setObjective(expr, GRB.MAXIMIZE);
					
					
					// Nebenbedingung (2):
					for (int i = 0; i < n; i++) {
						expr = new GRBLinExpr();
						expr.addTerm(1, t[i][0]);
						for (int k = 0; k < L_store; k++) {
							expr.addTerm(1, z[i][0][k]);
						}
						model.addConstr(expr, GRB.GREATER_EQUAL, a[i][0], "(2)" + i);
					}
	
					// Nebenbedingung (3):
					for (int k = 0; k < L_store; k++) {
						expr = new GRBLinExpr();
						expr.addTerm(-V, y[0][k]);
						for (int i = 0; i < n; i++) {
							expr.addTerm(v[i], z[i][0][k]);
						}
						model.addConstr(expr, GRB.LESS_EQUAL, 0, "(3)" + k);
					}
	
					// Nebenbedingung (4):
					for (int i = 0; i < n; i++) {
						expr = new GRBLinExpr();
						expr.addTerm(1, x[i][0]);
						model.addConstr(expr, GRB.LESS_EQUAL, b[i], "(4)" + i);
					}
	
					// Nebenbedingung (5)
					for (int i = 0; i < n; i++) {
						expr = new GRBLinExpr();
						expr.addTerm(1, s[i][0]);
						expr.addTerm(-1, x[i][0]);
						expr.addTerm(1, t[i][0]);
						model.addConstr(expr, GRB.LESS_EQUAL, a[i][0], "(5)" + i);
					}
	
					//Nebenbedingung (6)
					for (int i = 0; i < n; i++) {
						expr = new GRBLinExpr();
						expr.addTerm(1, s[i][0]);
						model.addConstr(expr, GRB.LESS_EQUAL, d[i], "(6)" + i);
					}
				
					// Nebenbedingung (7)
					expr = new GRBLinExpr();
					for (int k = 0; k < L_store; k++) {
						expr.addTerm(C, y[0][k]);
					}
					for (int i = 0; i < n; i++) {
						expr.addTerm(c[i], x[i][0]);
						expr.addTerm(w[i], t[i][0]);
					}
					model.addConstr(expr, GRB.LESS_EQUAL, capital - inst.getF(), "(7)");
					
					//Nebenbedingung (8)
					expr = new GRBLinExpr();
					expr.addTerm(1, K);
					for(int i = 0; i < n; i++) {
						expr.addTerm(- p[i], s[i][0]);
						expr.addTerm(c[i], x[i][0]);
						expr.addTerm(w[i], t[i][0]);
					}
					for(int k = 0; k < L_store; k++) {
						expr.addTerm(C, y[0][k]);
					}
					model.addConstr(expr, GRB.LESS_EQUAL, capital - inst.getF(), "(8)");
					
					
					
					// Lösen
	       			model.set("MIPGap", "0");
	       			model.set("MIPGapAbs", "0.8");
	       			model.set("LogToConsole", "0");
	       			model.set("TimeLimit", "55");
					model.optimize();
	
					
					// check status codes
	       			if (model.get(GRB.IntAttr.Status) == GRB.Status.INFEASIBLE) {
						System.out.println("Zeitpunkt " + zeitpunkt + ": Problem unzulässig. Bankrott unumgänglich. Programmabbruch!");
	       				break;
	       			}
	       			if (model.get(GRB.IntAttr.Status) == GRB.Status.TIME_LIMIT) {
						System.out.println("Zeitpunkt " + zeitpunkt + ": Zeitlimit erreicht. Verwende suboptimale Lösung. Bankrott in nächster Periode möglich.");
	       			}
	       			if (model.get(GRB.IntAttr.Status) == GRB.Status.OPTIMAL) {
						System.out.println("Zeitpunkt " + zeitpunkt + ": Optimale Entscheidung gefunden.");
	       			}
	       			
	       			
	       			// get data from optimal solution
	       			if (model.get(GRB.IntAttr.SolCount) < 1) {
	       				System.out.println("Zeitpunkt " + zeitpunkt + ": Keine zulässige Lösung für Problem innerhalb des Zeitlimits gefunden. Lagere nichts. Produziere nichts. Bankrott wahrscheinlich.");
	       				for (int i = 0; i < n; i++) {
		       				disposalCost += w[i] * a[i][0];
		       				a[i][0] = 0;
	       				}
	       			}
	       			else {
	       				int storageCount = 0;
	       				for (int k = 0; k < L_store; k++) {
	       					storageCount += Math.round(y[0][k].get(GRB.DoubleAttr.X));
		       				storageCost += C * Math.round(y[0][k].get(GRB.DoubleAttr.X));
		       			}
	       				storage = new int[storageCount][n];
	       				for (int k = 0; k < storageCount; k++) {
	       					for (int i = 0; i < n; i++) {
	       						storage[k][i] = (int) Math.round(z[i][0][k].get(GRB.DoubleAttr.X));
	       					}
	       				}
		       			for (int i = 0; i < n; i++) {
		       				disposalCost += w[i] * Math.round(t[i][0].get(GRB.DoubleAttr.X));
		       				a[i][0] -= (int) Math.round(t[i][0].get(GRB.DoubleAttr.X));
		       			}
		       			production = new int[n];
		       			for (int i = 0; i < n; i++) {
		       				production[i] = (int) Math.round(x[i][0].get(GRB.DoubleAttr.X));
		       				productionCost += c[i] * production[i];
		       				a[i][0] += production[i];
		       			}
	       			}
	       			
					
	       			// dispose model
					model.dispose();
					env.dispose();
	
				} catch (GRBException e) {
					System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
					e.printStackTrace();
				}
				// ************* ENDE: Gurobi Model *************
			}
			
			
			/*
			 * Kapitalberechnung
			 */
			// Fixkosten
			capital -= inst.getF();
			if (capital < 0) {
				System.out.println("Zeitpunkt " + zeitpunkt + ": Bankrott durch Fixkosten.");
				break;
			}
			// Lager- und Entsorgungskosten
			capital -= storageCost;
			capital -= disposalCost;
			if (capital < 0) {
				System.out.println("Zeitpunkt " + zeitpunkt + ": Bankrott durch Lagerung und Entsorgung.");
				break;
			}
			// Produktionskosten
			capital -= productionCost;
			if (capital < 0) {
				System.out.println("Zeitpunkt " + zeitpunkt + ": Bankrott durch Produktionskosten.");
				break;
			}
					
			
			
			/*
			 * Ausgabe und loggen der Entscheidungen
			 */
			if (!isFirstPeriod) {
				logWriter.schreibeLager(storage);
			} else {
				isFirstPeriod = false;
			}
			if (!demandModule.wasLastPeriod()) logWriter.schreibeProduktion(production);
			Kapital = capital;
			for (int i = 0; i < n; i++) {
				bestand[i] = a[i][0];
			}
			System.out.println("Zeitpunkt " + zeitpunkt + ": Kapital nach Produktion: " + capital);
			System.out.print("Zeitpunkt " + zeitpunkt + ": Bestand nach Produktion: ");
			for (int i = 0; i < n; i++) {
				System.out.print(bestand[i] + " ");
			}
			System.out.println();
			
			if (demandModule.wasLastPeriod()) break;
		}
		logWriter.schreibeKapital(Kapital);


	}

}
