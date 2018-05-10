package src;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import gurobi.*;

public class MainBasisModell {

	private static String delims = "[ ]+";

	public static void main(String[] args) {

		String speicherort = "./instances/";
		String dateinameInstanz = "Instanz1.in";
		String dateinameNachfrageInfo = "Instanz1.deminfo";
		int maxLager = 50;

		// ************* ANFANG: Einlesen der Instanz (ohne Nachfrage) *************
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
			BufferedReader br = new BufferedReader(new FileReader(speicherort + dateinameInstanz));
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
		} catch (IOException e) {
			System.out.println("Fehler: " + e.toString());
		}
		
		int j = 1;
		int[][] a = new int[n][1];
		int[] d = new int[n];
		
		// ************* ANFANG: Gurobi Model *************
		try {
			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			GRBVar[][] x = new GRBVar[n][1];
			for (int i = 0; i < n; i++) {
				x[i][0] = model.addVar(0, b[1], 0, GRB.INTEGER, "x_" + i + "_" + j);
			}
			GRBVar[][] y = new GRBVar[1][maxLager];
			for (int k = 0; k < maxLager; k++) {
				y[0][k] = model.addVar(0, 1, 0, GRB.BINARY, "y_" + j + "_" + k);
			}
			GRBVar[][][] z = new GRBVar[n][1][maxLager];
			for (int i = 0; i < n; i++) {
				for (int k = 0; k < maxLager; k++) {
					z[i][0][k] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "z_" + i + "_" + j + "_" + k);
				}
			}
			GRBVar[][] s = new GRBVar[n][1];
			for (int i = 0; i < n; i++) {

				s[i][0] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "s_" + i + "_" + j);

			}
			GRBVar[][] t = new GRBVar[n][1];
			for (int i = 0; i < n; i++) {
				t[i][0] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "t_" + i + "_" + j);
			}
			GRBVar[] K = new GRBVar[2];

			K[0] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "K_" + j);
			K[1] = model.addVar(0, GRB.INFINITY, 0, GRB.INTEGER, "K_" + (j + 1));
			

			// Zielfunktion
			
			GRBLinExpr expr = new GRBLinExpr();
			
			expr.addTerm(1, K[1]);
			
			model.setObjective(expr);
			
			
			
			// Nebenbedingung (2):
			for (int i = 0; i < n; i++) {
				expr = new GRBLinExpr();
				expr.addTerm(1, t[i][0]);
				for (int k = 0; k < maxLager; k++) {
					expr.addTerm(1, z[i][0][k]);
				}
				model.addConstr(expr, GRB.GREATER_EQUAL, a[i][0], "(2)" + i);
			}


			// Nebenbedingung (3):
			for (int k = 0; k < maxLager; k++) {
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

			for (int k = 0; k < maxLager; k++) {
				expr.addTerm(C, y[0][k]);
			}
			for (int i = 0; i < n; i++) {
				expr.addTerm(c[i], x[i][0]);
				expr.addTerm(w[i], t[i][0]);
			}
			expr.addTerm(-1, K[0]);
			
			model.addConstr(expr, GRB.LESS_EQUAL, -F, "(7)");
			
			//Nebenbedingung (8)
			expr = new GRBLinExpr();
			expr.addTerm(1, K[0]);
			expr.addTerm(-1, K[1]);
			
			for(int i=0;i<n;i++) {
				expr.addTerm(p[i], s[i][0]);
				expr.addTerm(c[i], x[i][j]);
				expr.addTerm(w[i], t[i][0]);
			}
			for(int k=0;k<maxLager;k++) {
				expr.addTerm(-C, y[0][k]);
			}
			
			model.addConstr(expr, GRB.GREATER_EQUAL, F, "(8)");
			
			// WICHTIG: Zielfunktion neu setzen und MAXIMIEREN!!!!

			

			
			model.optimize();

			

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
			e.printStackTrace();
		}
		// ************* ENDE: Gurobi Model *************

	}

}
