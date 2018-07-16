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

		int[] aktuellerBestand = new int[inst.getAnzahlProdukte()];
		int[] volumen = new int[inst.getAnzahlProdukte()];
		for (Produkt produkt : inst.getProdukte()) {
			aktuellerBestand[produkt.getId()] = produkt.getAktuellerBestand();
			volumen[produkt.getId()] = produkt.getVolumen();
		}

		int[][] temp = FirstFitDecreasing.pack(inst.getLagervolumen(), aktuellerBestand, volumen);
		int[][] lagerung = new int[temp[0].length][inst.getAnzahlProdukte()];
		for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
			for (int j = 0; j < temp[0].length; j++) {
				lagerung[j][i] = temp[i][j];
			}
		}
		temp = null;
		aktuellerBestand = null;
		volumen = null;

		int maxLager = lagerung.length;

		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			GRBVar[] behalten = new GRBVar[inst.getAnzahlProdukte()];
			for (Produkt produkt : inst.getProdukte()) {
				behalten[produkt.getId()] = model.addVar(0, produkt.getAktuellerBestand(), 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die gelagert wird");
			}
			GRBVar[] wegwerfen = new GRBVar[inst.getAnzahlProdukte()];
			for (Produkt produkt : inst.getProdukte()) {
				wegwerfen[produkt.getId()] = model.addVar(0, produkt.getAktuellerBestand(), 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die weggeworfen wird");
			}
			GRBVar[] lagerVerwenden = new GRBVar[maxLager];
			for (int lager = 0; lager < maxLager; lager++) {
				lagerVerwenden[lager] = model.addVar(0, 1, 0, GRB.BINARY, "Entscheidung, ob Lager " + String.valueOf(lager) + " verwendet werden soll");
			}
			GRBVar[][] lagermenge = new GRBVar[inst.getAnzahlProdukte()][maxLager];
			for (Produkt produkt : inst.getProdukte()) {
				for (int lager = 0; lager < maxLager; lager++) {
					lagermenge[produkt.getId()][lager] = model.addVar(0, produkt.getAktuellerBestand(), 0, GRB.INTEGER, "Menge von Produkt " + String.valueOf(produkt) + ", die in Lager " + String.valueOf(lager) + " gelagert wird");
				}
			}

			for (Produkt produkt : inst.getProdukte()) {
				GRBLinExpr expr = new GRBLinExpr();
				expr.addTerm(1, behalten[produkt.getId()]);
				expr.addTerm(1, wegwerfen[produkt.getId()]);
				model.addConstr(expr, GRB.EQUAL, produkt.getAktuellerBestand(), "das, was von Produkt " + String.valueOf(produkt) + " in Summe gelagert und weggeworfen wird, muss dem Bestand entsprechen");
			}

			for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
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
				exprRechts.addTerm(inst.getLagervolumen(), lagerVerwenden[lager]);
				for (Produkt produkt : inst.getProdukte()) {
					exprLinks.addTerm(produkt.getVolumen(), lagermenge[produkt.getId()][lager]);
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

			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(produkt.getTempBewertung(), behalten[produkt.getId()]);
			}

			for (Produkt produkt : inst.getProdukte()) {
				expr.addTerm(-produkt.getWegwerfkosten(), wegwerfen[produkt.getId()]);
			}

			for (int k = 0; k < maxLager; k++) {
				expr.addTerm(-inst.getLagerkosten(), lagerVerwenden[k]);
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
			
			lagerung = new int[anzahlLager][inst.getAnzahlProdukte()];

			for (int lager = 0; lager < anzahlLager; lager++) {
				for (int produkt = 0; produkt < inst.getAnzahlProdukte(); produkt++) {
					lagerung[lager][produkt] = (int) Math.round(lagermenge[produkt][lager].get(GRB.DoubleAttr.X));
				}

			}


			// Lagerkosten zahlen
			inst.zahleLagerkosten(anzahlLager);

			// Kosten fuers Wegwerfen zahlen
			int[] wegwerfmenge = new int[inst.getAnzahlProdukte()];
			for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
				wegwerfmenge[i] = (int) Math.round(wegwerfen[i].get(GRB.DoubleAttr.X));
			}
			inst.zahleWegwerfkosten(wegwerfmenge);

			// aktuellen Bestand anpassen
			for (Produkt produkt : inst.getProdukte()) {
				produkt.setAktuellerBestand((int) Math.round(behalten[produkt.getId()].get(GRB.DoubleAttr.X)));
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
