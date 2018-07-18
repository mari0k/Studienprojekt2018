import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class Produktion {

	private static int[] varX = null;
	private static int[][] varS = null;
	private static int[][] varZ = null;
	private static int[] varY = null;
	private static int[][] varT = null;
	private static double[] varE = null;
	

	private static double gamma = 0.975;
	private static double kappa = 0.0;
	
	
public static void setGamma(Instanz inst) {
	try {
		GRBEnv env = new GRBEnv();
		GRBModel model = new GRBModel(env);

		/*
		 * Es folgen die Variablen für: - Produktionsmenge - Anzahl der benoetigten
		 * Lager - Verkaufsmenge - Wegwerfmenge - Lagermenge - Ertrag
		 */

		GRBVar[] anzahl = new GRBVar[inst.getAnzahlProdukte()];
		for (Produkt produkt : inst.getProdukte()) {
			anzahl[produkt.getId()] = model.addVar(0, inst.getLagervolumen() / produkt.getVolumen(), produkt.getVolumen(), GRB.INTEGER,
					"x_" + String.valueOf(produkt));
		}
		

		/*
		 * Es folgt die Nebenbedingung
		 */
		GRBLinExpr expr = new GRBLinExpr();
		for (Produkt produkt : inst.getProdukte()) {
			expr.addTerm(produkt.getVolumen(), anzahl[produkt.getId()]);
		}
		model.addConstr(expr, GRB.LESS_EQUAL, inst.getLagervolumen(), "Volumenbeschraenkung");

		

		/*
		 * Zielfunktion aufstellen: Die Produktionskosten gehen negativ ein. Die Erloese
		 * der Szenarien werden mit den Eintrittswahrscheinlichkeiten des Szenarios
		 * gewichtet. Hier besitzt jedes Szenario dieselbe Wahrscheinlichkeit, da
		 * zufaellige Szenarios entsprechend der Verteilung generiert wurden.
		 * 
		 */

		model.setObjective(expr, GRB.MAXIMIZE);

		model.set("MIPGap", "0.0");
		model.set("MIPGapAbs", "0.0");
		model.set("TimeLimit", String.valueOf(2));
		model.set("LogToConsole", "0"); //TODO
		model.optimize();

		/*
		 * Entsprechend der Loesung des Modells werden die Produktionsmengen festgelegt.
		 */

		gamma = Math.min(gamma, model.get(GRB.DoubleAttr.ObjVal));
		
		
		

		model.dispose();
		env.dispose();
	} catch (GRBException e) {
		System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		e.printStackTrace();
	}
}
	
public static int[] produziere(Instanz inst, int[][] szenarien, int maxLaufzeit) {
		
		int anzahlSzenarien = szenarien.length;
		
		int[] produktion = new int[inst.getAnzahlProdukte()];
		
		for (int i = 0; i < produktion.length; i++) produktion[i] = 0;
		
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			/*
			 * Es folgen die Variablen für: - Produktionsmenge - Anzahl der benoetigten
			 * Lager - Verkaufsmenge - Wegwerfmenge - Lagermenge - Ertrag
			 */

			GRBVar[] produktionsmenge = new GRBVar[inst.getAnzahlProdukte()];
			for (Produkt produkt : inst.getProdukte()) {
				produktionsmenge[produkt.getId()] = model.addVar(0, produkt.getProduktionsschranke(), 0, GRB.INTEGER,
						"x_" + String.valueOf(produkt));
				if (varX != null) {
					produktionsmenge[produkt.getId()].set(GRB.DoubleAttr.Start, varX[produkt.getId()] - produkt.getAktuellerBestand());
				}
			}
			GRBVar[] anzahlBenoetigterLager = new GRBVar[anzahlSzenarien];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				anzahlBenoetigterLager[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
						"y_" + String.valueOf(szenario));
				if (varY != null) {
					anzahlBenoetigterLager[szenario].set(GRB.DoubleAttr.Start, varY[szenario]);
				}
			}
			GRBVar[][] verkaufsmenge = new GRBVar[anzahlSzenarien][inst.getProdukte().length];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					verkaufsmenge[szenario][produkt.getId()] = model.addVar(0, szenarien[szenario][produkt.getId()], 0,
							GRB.CONTINUOUS, "s_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
					if (varS != null) {
						verkaufsmenge[szenario][produkt.getId()].set(GRB.DoubleAttr.Start, varS[szenario][produkt.getId()]);
					}
				}
			}
			GRBVar[][] wegwerfmenge = new GRBVar[anzahlSzenarien][inst.getAnzahlProdukte()];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					wegwerfmenge[szenario][produkt.getId()] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
							"t_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
					if (varT != null) {
						wegwerfmenge[szenario][produkt.getId()].set(GRB.DoubleAttr.Start, varT[szenario][produkt.getId()]);
					}
				}
			}
			GRBVar[][] lagermenge = new GRBVar[anzahlSzenarien][inst.getAnzahlProdukte()];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					lagermenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
							"z_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
					if (varZ != null) {
						lagermenge[szenario][produkt].set(GRB.DoubleAttr.Start, varZ[szenario][produkt]);
					}
				}
			}
			GRBVar[] ertrag = new GRBVar[anzahlSzenarien];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				ertrag[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "E_" + String.valueOf(szenario));
				//if (varE != null) {
				//	ertrag[szenario].set(GRB.DoubleAttr.Start, varE[szenario]);
				//}
			}

			/*
			 * Es folgen die Nebenbedingungen
			 */

			/*
			 * Nicht mehr produzieren als das aktuelle Kapital zulaesst
			 */

			GRBLinExpr expr = new GRBLinExpr();
			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(produkt.getHerstellungskosten(), produktionsmenge[produkt.getId()]);
			}
			model.addConstr(expr, GRB.LESS_EQUAL, inst.getAktuellesKapital(), "Kapitalbeschraenkung");

			/*
			 * Nebenbedingungen an die Verkaufsmenge
			 */

			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					exprLinks.addTerm(1, verkaufsmenge[szenario][produkt.getId()]);
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts,
							"Beschraenkung der Verkaufsmenge von Produkt " + String.valueOf(produkt)
									+ " in Szenario " + String.valueOf(szenario));
				}
			}

			/*
			 * Die Lagermenge ist die Differenz aus Bestand + Produktion und Verkauf +
			 * Wegwerfen
			 */

			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					exprLinks.addTerm(1, lagermenge[szenario][produkt.getId()]);
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt.getId()]);
					exprRechts.addTerm(-1, wegwerfmenge[szenario][produkt.getId()]);
					model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "Lagermenge von Produkt "
							+ String.valueOf(produkt) + " in Szenario " + String.valueOf(szenario));
				}
			}

			/*
			 * Die Lagermenge ist hoechstens so hoch wie : Bestand + Produktion - Verkauf
			 */
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					exprLinks.addTerm(1, lagermenge[szenario][produkt.getId()]);
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt.getId()]);
					model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts,
							"Beschraenkung der Lagermenge von Produkt " + String.valueOf(produkt) + " in Szenario "
									+ String.valueOf(szenario));
				}
			}

			/*
			 * Die Wegwerfmenge ist hoechstens so hoch wie : Bestand + Produktion - Verkauf
			 */
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					exprLinks.addTerm(1, wegwerfmenge[szenario][produkt.getId()]);
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt.getId()]);
					model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts,
							"Beschraenkung der Wegwerfmenge von Produkt " + String.valueOf(produkt)
									+ " in Szenario " + String.valueOf(szenario));
				}
			}

			/*
			 * Anzahl der benoetigten Lager bestimmen: Hierbei wird das Volumen EINES Lagers
			 * auf gamma * V gesetzt. Zusaetzlich wird das Gesamtvolumen der Produkte um
			 * epsilon * V erhoeht.
			 */

			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				exprLinks.addTerm(gamma * inst.getLagervolumen(), anzahlBenoetigterLager[szenario]);
				GRBLinExpr exprRechts = new GRBLinExpr();
				for (Produkt produkt : inst.getProdukte()) {
					exprRechts.addTerm(produkt.getVolumen(), lagermenge[szenario][produkt.getId()]);
				}
				exprRechts.addConstant(kappa * inst.getLagervolumen());
				model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts,
						"Anzahl der benoetigten Lager in Szenario " + String.valueOf(szenario));
			}

			/*
			 * Ertrag in jedem Szenario: Dieser berechnet sich aus + Verkaufserloes -
			 * Wegwerfkosten - Lagerkosten + Bewertung der gelagerten Produkte (hier werden
			 * die Produktionskosten genommen)
			 */

			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				exprLinks.addTerm(1, ertrag[szenario]);
				GRBLinExpr exprRechts = new GRBLinExpr();
				exprRechts.addTerm(-inst.getLagerkosten(), anzahlBenoetigterLager[szenario]);
				for (Produkt produkt : inst.getProdukte()) {
					produkt.setTempAnzahl(Math.max(0, Math.min(produkt.getProduktionsniveau(), produkt.getAktuellerBestand() + produkt.getProduktionsschranke()) - szenarien[szenario][produkt.getId()]));
					produkt.bewerte();
					exprRechts.addTerm(produkt.getVerkaufserloes(), verkaufsmenge[szenario][produkt.getId()]);
					exprRechts.addTerm(produkt.getTempBewertung(), lagermenge[szenario][produkt.getId()]);
					exprRechts.addTerm(-produkt.getWegwerfkosten(), wegwerfmenge[szenario][produkt.getId()]);
				}
				model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "Ertrag im Szenario " + String.valueOf(szenario));
			}

			/*
			 * Zielfunktion aufstellen: Die Produktionskosten gehen negativ ein. Die Erloese
			 * der Szenarien werden mit den Eintrittswahrscheinlichkeiten des Szenarios
			 * gewichtet. Hier besitzt jedes Szenario dieselbe Wahrscheinlichkeit, da
			 * zufaellige Szenarios entsprechend der Verteilung generiert wurden.
			 * 
			 */

			expr = new GRBLinExpr();
			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(-produkt.getHerstellungskosten(), produktionsmenge[produkt.getId()]);
			}
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				expr.addTerm(1.0 / anzahlSzenarien, ertrag[szenario]);
			}

			model.setObjective(expr, GRB.MAXIMIZE);

			model.set("MIPGap", "0.0");
			model.set("MIPGapAbs", "0.0");
			model.set("TimeLimit", String.valueOf(maxLaufzeit));
			//model.set("LogToConsole", "0"); //TODO
			model.optimize();

			/*
			 * Entsprechend der Loesung des Modells werden die Produktionsmengen festgelegt.
			 */

			for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
				produktion[produkt] = (int) Math.round(produktionsmenge[produkt].get(GRB.DoubleAttr.X));
			}

			inst.zahleProduktionskosten(produktion);
			
			for (Produkt produkt : inst.getProdukte()) {
				produkt.setAktuellerBestand(produkt.getAktuellerBestand() + produktion[produkt.getId()]);
			}
			
			
			if (varX == null) {
				varX = new int[inst.getAnzahlProdukte()];
				varS = new int[szenarien.length][inst.getAnzahlProdukte()];
				varZ = new int[szenarien.length][inst.getAnzahlProdukte()];
				varY = new int[szenarien.length];
				varT = new int[szenarien.length][inst.getAnzahlProdukte()];
				varE = new double[szenarien.length];
				for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
					varX[i] = produktion[i];
					for (int j = 0; j < szenarien.length; j++) {
						varS[j][i] = (int) Math.round(verkaufsmenge[j][i].get(GRB.DoubleAttr.X));
						varZ[j][i] = (int) Math.round(lagermenge[j][i].get(GRB.DoubleAttr.X));
						varT[j][i] = (int) Math.round(wegwerfmenge[j][i].get(GRB.DoubleAttr.X));
					}
				}
				for (int i = 0; i < szenarien.length; i++) {
					varY[i] =  (int) Math.round(anzahlBenoetigterLager[i].get(GRB.DoubleAttr.X));
					varE[i] =  ertrag[i].get(GRB.DoubleAttr.X);
				}
				
			}
			

			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		return produktion;
	}




	public static int[] produziereLetztePeriode(Instanz inst, int[][] szenarien, int maxLaufzeit) {
		
		int anzahlSzenarien = szenarien.length;
		
		int[] produktion = new int[inst.getAnzahlProdukte()];
		
		for (int i = 0; i < produktion.length; i++) produktion[i] = 0;
		
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);
	
			/*
			 * Es folgen die Variablen für: - Produktionsmenge - Anzahl der benoetigten
			 * Lager - Verkaufsmenge - Wegwerfmenge - Lagermenge - Ertrag
			 */
	
			GRBVar[] produktionsmenge = new GRBVar[inst.getAnzahlProdukte()];
			for (Produkt produkt : inst.getProdukte()) {
				produktionsmenge[produkt.getId()] = model.addVar(0, produkt.getProduktionsschranke(), 0, GRB.INTEGER,
						"x_" + String.valueOf(produkt));
			}
			GRBVar[][] verkaufsmenge = new GRBVar[anzahlSzenarien][inst.getProdukte().length];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					verkaufsmenge[szenario][produkt.getId()] = model.addVar(0, szenarien[szenario][produkt.getId()], 0,
							GRB.CONTINUOUS, "s_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
				}
			}
			GRBVar[][] wegwerfmenge = new GRBVar[anzahlSzenarien][inst.getAnzahlProdukte()];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					wegwerfmenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
							"t_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
				}
			}
			GRBVar[] ertrag = new GRBVar[anzahlSzenarien];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				ertrag[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "E_" + String.valueOf(szenario));
			}
	
			/*
			 * Es folgen die Nebenbedingungen
			 */
	
			/*
			 * Nicht mehr produzieren als das aktuelle Kapital zulaesst
			 */
	
			GRBLinExpr expr = new GRBLinExpr();
			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(produkt.getHerstellungskosten(), produktionsmenge[produkt.getId()]);
			}
			model.addConstr(expr, GRB.LESS_EQUAL, inst.getAktuellesKapital(), "Kapitalbeschraenkung");
	
			/*
			 * Nebenbedingungen an die Verkaufsmenge
			 */
	
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					exprLinks.addTerm(1, verkaufsmenge[szenario][produkt.getId()]);
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts,
							"Beschraenkung der Verkaufsmenge von Produkt " + String.valueOf(produkt)
									+ " in Szenario " + String.valueOf(szenario));
				}
			}
	
			/*
			 * Die Lagermenge ist die Differenz aus Bestand + Produktion und Verkauf +
			 * Wegwerfen
			 */
	
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (Produkt produkt : inst.getProdukte()) {
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, produktionsmenge[produkt.getId()]);
					exprRechts.addConstant(produkt.getAktuellerBestand());
					exprRechts.addTerm(-1, verkaufsmenge[szenario][produkt.getId()]);
					exprRechts.addTerm(-1, wegwerfmenge[szenario][produkt.getId()]);
					model.addConstr(0, GRB.EQUAL, exprRechts, "Lagermenge von Produkt "
							+ String.valueOf(produkt) + " in Szenario " + String.valueOf(szenario));
				}
			}
	
			/*
			 * Ertrag in jedem Szenario: Dieser berechnet sich aus + Verkaufserloes -
			 * Wegwerfkosten - Lagerkosten + Bewertung der gelagerten Produkte (hier werden
			 * die Produktionskosten genommen)
			 */
	
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				exprLinks.addTerm(1, ertrag[szenario]);
				GRBLinExpr exprRechts = new GRBLinExpr();
				for (Produkt produkt : inst.getProdukte()) {
					exprRechts.addTerm(produkt.getVerkaufserloes(), verkaufsmenge[szenario][produkt.getId()]);
					exprRechts.addTerm(-produkt.getWegwerfkosten(), wegwerfmenge[szenario][produkt.getId()]);
				}
				model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "Ertrag im Szenario " + String.valueOf(szenario));
			}
	
			/*
			 * Zielfunktion aufstellen: Die Produktionskosten gehen negativ ein. Die Erloese
			 * der Szenarien werden mit den Eintrittswahrscheinlichkeiten des Szenarios
			 * gewichtet. Hier besitzt jedes Szenario dieselbe Wahrscheinlichkeit, da
			 * zufaellige Szenarios entsprechend der Verteilung generiert wurden.
			 * 
			 */
	
			expr = new GRBLinExpr();
			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(-produkt.getHerstellungskosten(), produktionsmenge[produkt.getId()]);
			}
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				expr.addTerm(1.0 / anzahlSzenarien, ertrag[szenario]);
			}
	
			model.setObjective(expr, GRB.MAXIMIZE);
	
			model.set("MIPGap", "0.0");
			model.set("MIPGapAbs", "0.0");
			model.set("TimeLimit", String.valueOf(maxLaufzeit));
			model.set("LogToConsole", "0");
			model.optimize();
	
			/*
			 * Entsprechend der Loesung des Modells werden die Produktionsmengen festgelegt.
			 */
	
			for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
				produktion[produkt] = (int) Math.round(produktionsmenge[produkt].get(GRB.DoubleAttr.X));
			}
	
			inst.zahleProduktionskosten(produktion);
			
			for (Produkt produkt : inst.getProdukte()) {
				produkt.setAktuellerBestand(produkt.getAktuellerBestand() + produktion[produkt.getId()]);
			}
	
			model.dispose();
			env.dispose();
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		return produktion;
	}
}
