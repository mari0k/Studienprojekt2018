import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import unused.FirstFitDecreasing;

public class Lagerung {

	
	public static int[][] lagere(Instanz inst, int maxLaufzeit) {
		
		
		/*
		 * Um das Lagerproblem zu loesen wird zunaechst eine obere Schranke an die
		 * Anzahl der benoetigten Lager mittels First-Fit-Decreasing bestimmt.
		 * 
		 * Man beachte, dass eine ermittelte Loesung spaeter (noch) nicht mit uebergeben
		 * wird, sondern nur der "Zielfunktionswert"
		 */

		int[][] temp = FirstFitDecreasing.pack(inst.lagervolumen, inst.aktuellerBestand, inst.volumen);
		int[][] lagerung = new int[temp[0].length][inst.produkte];
		for (int i = 0; i < inst.produkte; i++) {
			for (int j = 0; j < temp[0].length; j++) {
				lagerung[j][i] = temp[i][j];
			}
		}
		temp = null;

		int maxLager = lagerung.length;

		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			GRBVar[] behalten = new GRBVar[inst.produkte];
			for (int produkt = 0; produkt < inst.produkte; produkt++) {
				behalten[produkt] = model.addVar(0, inst.aktuellerBestand[produkt], 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die gelagert wird");
			}
			GRBVar[] wegwerfen = new GRBVar[inst.produkte];
			for (int produkt = 0; produkt < inst.produkte; produkt++) {
				wegwerfen[produkt] = model.addVar(0, inst.aktuellerBestand[produkt], 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die weggeworfen wird");
			}
			GRBVar[] lagerVerwenden = new GRBVar[maxLager];
			for (int lager = 0; lager < maxLager; lager++) {
				lagerVerwenden[lager] = model.addVar(0, 1, 0, GRB.BINARY, "Entscheidung, ob Lager " + String.valueOf(lager) + " verwendet werden soll");
			}
			GRBVar[][] lagermenge = new GRBVar[inst.produkte][maxLager];
			for (int produkt = 0; produkt < inst.produkte; produkt++) {
				for (int lager = 0; lager < maxLager; lager++) {
					lagermenge[produkt][lager] = model.addVar(0, inst.aktuellerBestand[produkt], 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die in Lager " + String.valueOf(lager) + " gelagert wird");
				}
			}

			for (int produkt = 0; produkt < inst.produkte; produkt++) {
				GRBLinExpr expr = new GRBLinExpr();
				expr.addTerm(1, behalten[produkt]);
				expr.addTerm(1, wegwerfen[produkt]);
				model.addConstr(expr, GRB.EQUAL, inst.aktuellerBestand[produkt], "das, was von Produkt " + String.valueOf(produkt) + " in Summe gelagert und weggeworfen wird, muss dem Bestand entsprechen");
			}

			for (int produkt = 0; produkt < inst.produkte; produkt++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				GRBLinExpr exprRechts = new GRBLinExpr();
				exprRechts.addTerm(1, behalten[produkt]);
				for (int k = 0; k < maxLager; k++) {
					exprLinks.addTerm(1, lagermenge[produkt][k]);
				}
				model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "das, was von Produkt " + String.valueOf(produkt + " behalten werden soll muss ueber alle Lager verteilt auftauchen"));
			}

			for (int lager = 0; lager < maxLager; lager++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				GRBLinExpr exprRechts = new GRBLinExpr();
				exprRechts.addTerm(inst.lagervolumen, lagerVerwenden[lager]);
				for (int produkt = 0; produkt < inst.produkte; produkt++) {
					exprLinks.addTerm(inst.volumen[produkt], lagermenge[produkt][lager]);
				}
				model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts, "Volumen von Lager " + String.valueOf(lager) + " muss eingehalten werden");
			}
			
						
			/*
			 * Symmetrie verhindern:
			 * erst soll Lager 0, dann Lager 1 usw.
			 * verwendet werden.
			 */
			

			for (int lager = 0; lager < maxLager - 1; lager++) {
				GRBLinExpr exprLinks = new GRBLinExpr();
				GRBLinExpr exprRechts = new GRBLinExpr();
				exprLinks.addTerm(1, lagerVerwenden[lager]);
				exprRechts.addTerm(1, lagerVerwenden[lager + 1]);
				model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "erst Lager " + String.valueOf(lager) + " und dann Lager " + String.valueOf(lager + 1) + " verwenden");
			}

			/*
			 * Zielfunktion: Produkte auf Lager werden mit ihren Produktionskosten (positiv)
			 * und weggeworfene Produkte mit ihren Wegwerfkosten (negativ) bewertet
			 */

			GRBLinExpr expr = new GRBLinExpr();

			for (int i = 0; i < inst.produkte; i++) {
				expr.addTerm(inst.delta[i], behalten[i]);
			}

			for (int i = 0; i < inst.produkte; i++) {
				expr.addTerm(-inst.wegwerfkosten[i], wegwerfen[i]);
			}

			for (int k = 0; k < maxLager; k++) {
				expr.addTerm(-inst.lagerkosten, lagerVerwenden[k]);
			}

			model.setObjective(expr, GRB.MAXIMIZE);
			
			model.set("MIPGap", "0.0");
			model.set("MIPGapAbs", "0.0");
			model.set("TimeLimit", String.valueOf(maxLaufzeit));
			model.set("LogToConsole", "0");

			model.optimize();

			
			// Bestimme Anzahl verwendeter Lager:
			int anzahlLager = 0;
			for (int lager = 0; lager < maxLager; lager++) {
				anzahlLager += Math.round(lagerVerwenden[lager].get(GRB.DoubleAttr.X));
			}

			/*
			 * Die Variablenbelegungen aus dem geloesten Modell
			 * werden gespeichert
			 */
			
			lagerung = new int[anzahlLager][inst.produkte];

			for (int lager = 0; lager < anzahlLager; lager++) {
				for (int produkt = 0; produkt < inst.produkte; produkt++) {
					lagerung[lager][produkt] = (int) Math.round(lagermenge[produkt][lager].get(GRB.DoubleAttr.X));
				}

			}


			// Lagerkosten zahlen:
			inst.aktuellesKapital -= inst.lagerkosten * anzahlLager;

			// Kosten fuers Wegwerfen zahlen
			for (int i = 0; i < inst.produkte; i++) {
				inst.aktuellesKapital -= inst.wegwerfkosten[i] * Math.round(wegwerfen[i].get(GRB.DoubleAttr.X));
			}

			// aktuellen Bestand anpassen

			for (int i = 0; i < inst.produkte; i++) {
				inst.aktuellerBestand[i] = (int) Math.round(behalten[i].get(GRB.DoubleAttr.X));
			}

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			e.printStackTrace();
		}
		
		return lagerung;

	}
}
