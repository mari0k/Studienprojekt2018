import gurobi.*;

public class MainVaR{

	public static void main(String[] args) {
		String instanceName = null;
		double rho = 0.8;
		boolean deminfo = true;
		/*
		 * Check if Arguments are given and overwrite the corresponding variables
		 */
		switch (args.length) {
			case 3: deminfo = Boolean.parseBoolean(args[2]);
			case 2: rho = Double.parseDouble(args[1]); 
			case 1: instanceName = args[0]; break;
			default: instanceName = "kleineInstanz"; break;
		}
		
		
		
		/*
		 * Read instance data and initialize DemandModule, ValueAtRisk, MaximumLikelihood
		 */
		Instance inst = new Instance(instanceName, deminfo);
		DemandModule demandModule = new DemandModule(instanceName, inst.getN());
		ValueAtRisk VaR = new ValueAtRisk(rho);
		MaximumLikelihood ML = new MaximumLikelihood();
		
		
		
		/*
		 * Initialize variables needed for solving
		 */
		int period = -1;
		int capital = inst.getS();
		int n = inst.getN();
		int C = inst.getC();
		int V = inst.getV();
		int[] c = inst.get_c();
		int[] b = inst.get_b();
		int[] p = inst.get_p();
		int[] v = inst.get_v();
		int[] w = inst.get_w();
		int L = 50;	// upper bound for storage rooms needed 
		int[] bestand = new int[n];
		for (int i = 0; i < n; i++) {
			bestand[i] = 0;
		}
		int[] mean = new int[n];
		int[] sd = new int[n];
		int[] var = new int[n];
		if (inst.isKnownDistributions()) {
			mean = inst.getMean();
			sd = inst.getSd();
			for (int i = 0; i < n; i++) {
				var[i] = (int) Math.max(0, Math.ceil(VaR.computeVaR(mean[i], sd[i])));
			}
		}
		
		
		/*
		 * Start to solve
		 */
		while (capital >= 0) {
			period++;
			/*
			 * Sell products
			 */
			int[] demand = demandModule.getNewDemand();
			if (demand.length != n) break;
			int[] sell = new int[n];
			int sellProfit = 0;
			for (int i = 0; i < n; i++) {
				sell[i] = Math.max(0, Math.min(bestand[i], demand[i]));
				sellProfit += sell[i] * inst.get_p()[i];			
			}
			// update capital
			capital += sellProfit;
			
			
			
			/*
			 * Store products that were not sold
			 */
			int storageCost = 0;
			int disposalCost = 0;
			try {
				GRBEnv env = new GRBEnv();
				GRBModel model = new GRBModel(env);
				
				// create variables
				GRBVar[] x = new GRBVar[n];
                for (int i = 0; i < n; i++) {                	
            		x[i] = model.addVar(0, bestand[i], 0, GRB.INTEGER, "x_"+i);	// wegwerfen
                }           
				GRBVar[] y = new GRBVar[L];
                for (int j = 0; j < L; j++) {                	
            		y[j] = model.addVar(0, 1, 0, GRB.BINARY, "y_"+j);	// Lager verwenden
                }           
				GRBVar[][] z = new GRBVar[n][L];
                for (int i = 0; i < n; i++) {                	
                	for (int j = 0; j < L; j++) {                	
                		z[i][j] = model.addVar(0, bestand[i], 0, GRB.INTEGER, "z_"+i+","+j);	// einlagern
                    }
                }
                
                // create constraints 
                GRBLinExpr expr = new GRBLinExpr();
                for (int j = 0; j < L; j++) expr.addTerm(C, y[j]);
           		for (int i = 0; i < n; i++) expr.addTerm(w[i], x[i]);
   	      		model.addConstr(expr, GRB.LESS_EQUAL, capital - inst.getF(), "Kapitalschranke"); 
   	      		
   	      		for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
       	      		expr.addTerm(1.0, x[i]);    
	                for (int j = 0; j < L; j++) expr.addTerm(1.0, z[i][j]);
	   	      		model.addConstr(expr, GRB.GREATER_EQUAL, bestand[i], "alles, was nicht weggeworfen wird, einlagern"); 
   	      		}
   	      		
   	      		for (int j = 0; j < L; j++) {
	                expr = new GRBLinExpr();
       	      		expr.addTerm(-1.0*V, y[j]);    
	                for (int i = 0; i < n; i++) expr.addTerm(v[i], z[i][j]);
	   	      		model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "Lagervolumen und nur angemietete Lager"); 
   	      		}
                
	   	      	for (int j = 0; j < L - 1; j++) {
	       			GRBLinExpr exprLinks = new GRBLinExpr();
	       			GRBLinExpr exprRechts = new GRBLinExpr();
	       			exprLinks.addTerm(1, y[j]);
	       			exprRechts.addTerm(1, y[j + 1]);
	       			model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "Symmetrie verhindern");  
	       		}
	   	      	
	   	      	// create objective
	   	      	expr = new GRBLinExpr();
       			for (int j = 0; j < L; j++) expr.addTerm(C, y[j]);
       			for (int i = 0; i < n; i++) expr.addTerm(c[i] + w[i], x[i]);
       			model.setObjective(expr, GRB.MINIMIZE);
           		
       			// optimize model
       			model.set("MIPGap", "0");
       			model.set("MIPGapAbs", "0.8");
       			model.set("LogToConsole", "0");
       			model.optimize();
       			
       			
       			// get data from optimal solution
       			for (int j = 0; j < L; j++) {
       				storageCost += C * Math.round(y[j].get(GRB.DoubleAttr.X));
       			}
       			for (int i = 0; i < n; i++) {
       				disposalCost += w[i] * Math.round(x[i].get(GRB.DoubleAttr.X));
       			}
       			for (int i = 0; i < n; i++) {
       				bestand[i] -= (int) Math.round(x[i].get(GRB.DoubleAttr.X));
       			}

       			// dispose model
       			model.dispose();
       			env.dispose();
			} catch (GRBException e) {
				if (e.getErrorCode() == 10005) {
					System.out.println("Lagerungsproblem unzulässig. Bankrott unvermeidbar.");
					break;
				}
				else {
	                System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
	                e.printStackTrace();
				}
			}
					
			//update capital
			capital -= storageCost;
			capital -= disposalCost;
			if (capital < 0) {
				System.out.println("Bankrott durch Lagern");
				break;
			}
			
			
			
			/*
			 * Pay fix costs
			 */
			capital -= inst.getF();
			if (capital < 0) {
				System.out.println("Bankrott durch Fixkosten.");
				break;
			}
			
			
			
			
			/*
			 * Produce products
			 */
			if (inst.isKnownDistributions() != true) {
				for (int i = 0; i < n; i++) {
					Double[] estimates = ML.estimate(demandModule.getPastDemands().get(i));
					mean[i] = (int) Math.round(estimates[0]);
					sd[i] = (int) Math.round(estimates[2]);	// 2 = erwartungstreuer Schätzer (1 = ML Schätzer)
					var[i] = (int) Math.max(0, Math.ceil(VaR.computeVaR(mean[i], sd[i])));
				}
			}
			int productionCost = 0;
			try {
				GRBEnv env = new GRBEnv();
				GRBModel model = new GRBModel(env);
				
				// create variables
				GRBVar[] x = new GRBVar[n];
                for (int i = 0; i < n; i++) {                	
            		x[i] = model.addVar(0, b[i], 0, GRB.INTEGER, "x_"+i);	// produzieren
                } 
				GRBVar[] z = new GRBVar[n];
                for (int i = 0; i < n; i++) {                	
            		z[i] = model.addVar(0, b[i] + bestand[i], 0, GRB.INTEGER, "z_"+i);	// verkaufen (geplant)
                }           
				GRBVar[] y = new GRBVar[L];
                for (int j = 0; j < L; j++) {                	
            		y[j] = model.addVar(0, 1, 0, GRB.BINARY, "y_"+j);	// Lager verwenden
                }
				GRBVar[] t = new GRBVar[n];
                for (int i = 0; i < n; i++) {                	
            		t[i] = model.addVar(0, b[i] + bestand[i], 0, GRB.INTEGER, "t_"+i);	// wegwerfen (geplant)
                }        
				GRBVar[][] u = new GRBVar[n][L];
                for (int i = 0; i < n; i++) {                	
                	for (int j = 0; j < L; j++) {                	
                		u[i][j] = model.addVar(0, b[i] + bestand[i], 0, GRB.INTEGER, "z_"+i+","+j);	// einlagern
                    }
                }
                
                // create constraints 
                GRBLinExpr expr = new GRBLinExpr();
           		for (int i = 0; i < n; i++) expr.addTerm(c[i], x[i]);
   	      		model.addConstr(expr, GRB.LESS_EQUAL, capital, "Kapitalschranke (Produktion)"); 

	   	      	for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
	   	      		expr.addTerm(1.0, x[i]);
	   	      		model.addConstr(expr, GRB.LESS_EQUAL, b[i], "Produktionsschranke"); 
		      	}
	   	      	
	   	      	for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
	   	      		expr.addTerm(-1.0, x[i]);
	   	      		expr.addTerm(1.0, z[i]);
	   	      		model.addConstr(expr, GRB.LESS_EQUAL, bestand[i], "nur vekaufen, was da ist"); 
		      	}
	   	      	
	   	      	for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
	   	      		expr.addTerm(1.0, z[i]);
	   	      		model.addConstr(expr, GRB.LESS_EQUAL, mean[i], "nur vekaufen, was in Erwartung nachgefragt wird"); 
		      	}
   	      		
   	      		for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
       	      		expr.addTerm(-1.0, x[i]);
       	      		expr.addTerm(1.0, z[i]);
       	      		expr.addTerm(1.0, t[i]);
	                for (int j = 0; j < L; j++) expr.addTerm(1.0, u[i][j]);
	   	      		model.addConstr(expr, GRB.GREATER_EQUAL, bestand[i], "kompletten Restbestand einlagern"); 
   	      		}
   	      		
   	      		for (int i = 0; i < n; i++) {
	                expr = new GRBLinExpr();
       	      		expr.addTerm(-1.0, x[i]);
       	      		expr.addTerm(1.0, t[i]);
	                for (int j = 0; j < L; j++) expr.addTerm(1.0, u[i][j]);
	   	      		model.addConstr(expr, GRB.GREATER_EQUAL, bestand[i] - var[i], "einlagern, was mit W'keit rho übrig bleibt"); 
   	      		}
   	      		
   	      		for (int j = 0; j < L; j++) {
	                expr = new GRBLinExpr();
       	      		expr.addTerm(-1.0*V, y[j]);    
	                for (int i = 0; i < n; i++) expr.addTerm(v[i], u[i][j]);
	   	      		model.addConstr(expr, GRB.LESS_EQUAL, 0.0, "Lagervolumen und nur angemietete Lager"); 
   	      		}
   	      		
	            expr = new GRBLinExpr();
   	      		for (int i = 0; i < n; i++) {
       	      		expr.addTerm(c[i], x[i]);
	                expr.addTerm(w[i], t[i]);
	                expr.addTerm(-p[i], z[i]);
   	      		}
   	      		for (int j = 0; j < L; j++) expr.addTerm(C, y[j]);
	   	      	model.addConstr(expr, GRB.LESS_EQUAL, capital - inst.getF(), "Kapitalschranke (Lagerung)");
                
	   	      	for (int j = 0; j < L - 1; j++) {
	       			GRBLinExpr exprLinks = new GRBLinExpr();
	       			GRBLinExpr exprRechts = new GRBLinExpr();
	       			exprLinks.addTerm(1, y[j]);
	       			exprRechts.addTerm(1, y[j + 1]);
	       			model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "Symmetrie verhindern");  
	       		}
	   	      	
	   	      	// create objective
	   	      	expr = new GRBLinExpr();
       			for (int j = 0; j < L; j++) expr.addTerm(-C, y[j]);
       			for (int i = 0; i < n; i++) {
       				expr.addTerm(p[i], z[i]);
       				expr.addTerm(-c[i], x[i]);
       				expr.addTerm(-(w[i] + c[i]), t[i]);
       			}
       			model.setObjective(expr, GRB.MAXIMIZE);
           		
       			// optimize model
       			model.set("MIPGap", "0");
       			model.set("MIPGapAbs", "0.8");
       			model.set("LogToConsole", "0");
       			model.optimize();
       			
       			
       			// get data from optimal solution
       			for (int i = 0; i < n; i++) {
       				productionCost += c[i] * Math.round(x[i].get(GRB.DoubleAttr.X));
       			}
       			for (int i = 0; i < n; i++) {
       				bestand[i] += (int) Math.round(x[i].get(GRB.DoubleAttr.X));
       			}

       			// dispose model
       			model.dispose();
       			env.dispose();
			} catch (GRBException e) {
				if (e.getErrorCode() == 10005) {
					System.out.println("Produktionsproblem unzulässig. Bankrott in nächster Periode sehr wahrscheinlich.");
				}
				else {
	                System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
	                e.printStackTrace();
				}
			}
			//update capital
			capital -= productionCost;
			
			
			System.out.println("Kapital nach Periode " + period + ": " + capital);
			System.out.print("Bestand: ");
			for (int i = 0; i < n; i++) {
				System.out.print(bestand[i] + " ");
			}
			System.out.println();
		}
		
	}
	
	

}
