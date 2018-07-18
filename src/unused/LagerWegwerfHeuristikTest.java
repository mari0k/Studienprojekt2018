package unused;

import java.util.Random;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import main.Produkt;

public class LagerWegwerfHeuristikTest {

	public static void main(String[] args) {

		while (true) {
			Random random = new Random();
			int n = 20;
			int[] id = new int[n];
			int[] herstellungskosten = new int[n];
			int[] verkaufserloes = new int[n];
			int[] volumen = new int[n];
			int[] wegwerfkosten = new int[n];
			int[] produktionsschranke = new int[n];
			int[] erwartungswert = new int[n];
			int[] varianz = new int[n];
			int[] anzahlen = new int[n];
			for (int i = 0; i < n; i++) {
				id[i] = i;
				herstellungskosten[i] = erzeugeGanzeZahlInDiesemBereich(0, 100, random);
				verkaufserloes[i] = erzeugeGanzeZahlInDiesemBereich(100, 300, random);
				volumen[i] = erzeugeGanzeZahlInDiesemBereich(1, 20, random);
				wegwerfkosten[i] = erzeugeGanzeZahlInDiesemBereich(0, 20, random);
				produktionsschranke[i] = erzeugeGanzeZahlInDiesemBereich(0, 4, random);
				erwartungswert[i] = erzeugeGanzeZahlInDiesemBereich(105, 300, random);
				varianz[i] = erzeugeGanzeZahlInDiesemBereich(40, 400, random);
				anzahlen[i] = erzeugeGanzeZahlInDiesemBereich(0, 14, random);
			}
			// *****************************************************************************************

			
			Lager.setKosten(800);
			Lager.setVolumen(100);
			

			Produkt[] produkte = new Produkt[n];
			for (int i = 0; i < n; i++) {
				produkte[i] = new Produkt(id[i], herstellungskosten[i], verkaufserloes[i], volumen[i], wegwerfkosten[i], produktionsschranke[i], erwartungswert[i], varianz[i]);
			}
			
			Produkt.erstelleProduktSortierungen(produkte);
			
			for (Produkt produkt : produkte) {
				produkt.setTempAnzahl(anzahlen[produkt.getId()]);
				produkt.bewerte(); 
			}
			
			
			/*
			 * Verwendung der Heurikstik:
			 */
			double wertHeuristik = LagerWegwerfHeuristik.wert();

			// *****************************************************************************************

			int maxLager = LagerWegwerfHeuristik.berechneObereSchrankeLager();
			double wertGurobi = 0.0;

			try {
				GRBEnv env = new GRBEnv();
				GRBModel model = new GRBModel(env);

				GRBVar[] behalten = new GRBVar[n];
				for (int produkt = 0; produkt < n; produkt++) {
					behalten[produkt] = model.addVar(0, anzahlen[produkt], 0, GRB.INTEGER,
							"Menge von Produkt " + String.valueOf(produkt) + ", die gelagert wird");
				}
				GRBVar[] wegwerfen = new GRBVar[n];
				for (int produkt = 0; produkt < n; produkt++) {
					wegwerfen[produkt] = model.addVar(0, anzahlen[produkt], 0, GRB.INTEGER,
							"Menge von Produkt " + String.valueOf(produkt) + ", die weggeworfen wird");
				}
				GRBVar[] lagerVerwenden = new GRBVar[maxLager];
				for (int lager = 0; lager < maxLager; lager++) {
					lagerVerwenden[lager] = model.addVar(0, 1, 0, GRB.BINARY,
							"Entscheidung, ob Lager " + String.valueOf(lager) + " verwendet werden soll");
				}
				GRBVar[][] lagermenge = new GRBVar[n][maxLager];
				for (int produkt = 0; produkt < n; produkt++) {
					for (int lager = 0; lager < maxLager; lager++) {
						lagermenge[produkt][lager] = model.addVar(0, anzahlen[produkt], 0, GRB.INTEGER,
								"Menge von Produkt " + String.valueOf(produkt) + ", die in Lager "
										+ String.valueOf(lager) + " gelagert wird");
					}
				}

				for (int produkt = 0; produkt < n; produkt++) {
					GRBLinExpr expr = new GRBLinExpr();
					expr.addTerm(1, behalten[produkt]);
					expr.addTerm(1, wegwerfen[produkt]);
					model.addConstr(expr, GRB.EQUAL, anzahlen[produkt],
							"das, was von Produkt " + String.valueOf(produkt)
									+ " in Summe gelagert und weggeworfen wird, muss dem Bestand entsprechen");
				}

				for (int produkt = 0; produkt < n; produkt++) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(1, behalten[produkt]);
					for (int k = 0; k < maxLager; k++) {
						exprLinks.addTerm(1, lagermenge[produkt][k]);
					}
					model.addConstr(exprLinks, GRB.EQUAL, exprRechts, "das, was von Produkt " + String
							.valueOf(produkt + " behalten werden soll muss ueber alle Lager verteilt auftauchen"));
				}

				for (int lager = 0; lager < maxLager; lager++) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprRechts.addTerm(Lager.getVolumen(), lagerVerwenden[lager]);
					for (int produkt = 0; produkt < n; produkt++) {
						exprLinks.addTerm(volumen[produkt], lagermenge[produkt][lager]);
					}
					model.addConstr(exprLinks, GRB.LESS_EQUAL, exprRechts,
							"Volumen von Lager " + String.valueOf(lager) + " muss eingehalten werden");
				}

				/*
				 * Symmetrie verhindern: erst soll Lager 0, dann Lager 1 usw. verwendet werden.
				 */

				for (int lager = 0; lager < maxLager - 1; lager++) {
					GRBLinExpr exprLinks = new GRBLinExpr();
					GRBLinExpr exprRechts = new GRBLinExpr();
					exprLinks.addTerm(1, lagerVerwenden[lager]);
					exprRechts.addTerm(1, lagerVerwenden[lager + 1]);
					model.addConstr(exprLinks, GRB.GREATER_EQUAL, exprRechts, "erst Lager " + String.valueOf(lager)
							+ " und dann Lager " + String.valueOf(lager + 1) + " verwenden");
				}

				/*
				 * Zielfunktion: Produkte auf Lager werden mit ihren Produktionskosten (positiv)
				 * und weggeworfene Produkte mit ihren Wegwerfkosten (negativ) bewertet
				 */

				GRBLinExpr expr = new GRBLinExpr();

				for (int i = 0; i < n; i++) {
					expr.addTerm(produkte[i].getTempBewertung(), behalten[i]);
				}

				for (int i = 0; i < n; i++) {
					expr.addTerm(-wegwerfkosten[i], wegwerfen[i]);
				}

				for (int k = 0; k < maxLager; k++) {
					expr.addTerm(-Lager.getKosten(), lagerVerwenden[k]);
				}

				model.setObjective(expr, GRB.MAXIMIZE);

				model.set("MIPGap", "0.0");
				model.set("MIPGapAbs", "0.0");
				model.set("TimeLimit", "100000000");
				model.set("LogToConsole", "0");

				model.optimize();

				wertGurobi = model.get(GRB.DoubleAttr.ObjVal);

				model.dispose();
				env.dispose();

			} catch (GRBException e) {
				System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
				e.printStackTrace();
			}

			System.out.println("Heuristisch: " + wertHeuristik + ", Gurobi: " + wertGurobi);
			if (wertGurobi + 1e-6 < wertHeuristik) {
				System.out.println("Fehler!");
				System.exit(0);
			}
		}
	}

	public static int erzeugeGanzeZahlInDiesemBereich(int min, int max, Random random) {
		if (min > max) {
			System.out.println("Min sollte kleiner gleich max sein!");
			System.exit(0);
		}
		return min + random.nextInt(max - min + 1);
	}
}
