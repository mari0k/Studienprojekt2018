import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class Produktion {
	
	public static int[] produziere(Instanz inst, int[][] szenarien, int maxLaufzeit) {
		
		int anzahlSzenarien = szenarien.length;
		double gamma = 1.0;
		double kappa = 0.0;
		
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
			GRBVar[] anzahlBenoetigterLager = new GRBVar[anzahlSzenarien];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				anzahlBenoetigterLager[szenario] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
						"z_" + String.valueOf(szenario));
			}
			GRBVar[][] verkaufsmenge = new GRBVar[anzahlSzenarien][inst.getProdukte().length];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					verkaufsmenge[szenario][produkt] = model.addVar(0, szenarien[szenario][produkt], 0,
							GRB.INTEGER, "s_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
				}
			}
			GRBVar[][] wegwerfmenge = new GRBVar[anzahlSzenarien][inst.getAnzahlProdukte()];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					wegwerfmenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
							"t_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
				}
			}
			GRBVar[][] lagermenge = new GRBVar[anzahlSzenarien][inst.getAnzahlProdukte()];
			for (int szenario = 0; szenario < anzahlSzenarien; szenario++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					lagermenge[szenario][produkt] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER,
							"y_" + String.valueOf(szenario) + "_" + String.valueOf(produkt));
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
