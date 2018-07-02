package unused;

import java.io.IOException;
import java.util.*;
import gurobi.*;

public class binPackingGurobiSolver {

	public static void solveBinPacking(double[] zahlen, double binGroesse) throws IOException, GRBException {

		int n = zahlen.length;

		for (int i = 0; i < n; i++) {
			if (zahlen[i] > binGroesse) {
				System.out.println("Falsche Eingabe");
				return;
			}

		}

		try {

			GRBEnv env = new GRBEnv();
			GRBModel model = new GRBModel(env);

			GRBVar[] x = new GRBVar[n];
			GRBVar[][] t = new GRBVar[n][n];
			// Create variables

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (j == 0) {
						x[i] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x_" + i);
					}
					t[i][j] = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "t_" + i + j);
				}
			}

			// Set objective: minimize sum (i=1:n) x_i

			GRBLinExpr expr = new GRBLinExpr();
			for (int i = 0; i < n; i++) {
				expr.addTerm(1.0, x[i]);
			}

			model.setObjective(expr, GRB.MINIMIZE);
			// t[Objekt][Box] ; x[Box]
			// Add constraint: sum (j=1:n) t_ij x_ij =1

			for (int i = 0; i < n; i++) {
				expr = new GRBLinExpr();

				for (int j = 0; j < n; j++) {
					expr.addTerm(1.0, t[i][j]);

				}
				model.addConstr(expr, GRB.EQUAL, 1.0, "1.1" + i );
			}

			// Add constraint: Bin Kapazit�t

			for (int j = 0; j < n; j++) {
				expr = new GRBLinExpr();

				for (int i = 0; i < n; i++) {
					expr.addTerm(zahlen[i], t[i][j]);

				}
				model.addConstr(expr, GRB.LESS_EQUAL, binGroesse, "Bin Grenze eingehalten Bin" + j);
			}

			// Add constraint: Bin genutzt

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					expr = new GRBLinExpr();

					expr.addTerm(1.0, t[i][j]);
					expr.addTerm(-1.0, x[j]);
					model.addConstr(expr, GRB.LESS_EQUAL, 0, "Bin " + j + " genutzt");
				}
				
			}

			// Optimize model

			model.optimize();

			/*
			 * Status code Value Description LOADED 1 Model is loaded, but no solution
			 * information is available. OPTIMAL 2 Model was solved to optimality (subject
			 * to tolerances), and an optimal solution is available. INFEASIBLE 3 Model was
			 * proven to be infeasible. INF_OR_UNBD 4 Model was proven to be either
			 * infeasible or unbounded. To obtain a more definitive conclusion, set the
			 * DualReductions parameter to 0 and reoptimize. UNBOUNDED 5 Model was proven to
			 * be unbounded. Important note: an unbounded status indicates the presence of
			 * an unbounded ray that allows the objective to improve without limit. It says
			 * nothing about whether the model has a feasible solution. If you require
			 * information on feasibility, you should set the objective to zero and
			 * reoptimize. CUTOFF 6 Optimal objective for model was proven to be worse than
			 * the value specified in the Cutoff parameter. No solution information is
			 * available. ITERATION_LIMIT 7 Optimization terminated because the total number
			 * of simplex iterations performed exceeded the value specified in the
			 * IterationLimit parameter, or because the total number of barrier iterations
			 * exceeded the value specified in the BarIterLimit parameter. NODE_LIMIT 8
			 * Optimization terminated because the total number of branch-and-cut nodes
			 * explored exceeded the value specified in the NodeLimit parameter. TIME_LIMIT
			 * 9 Optimization terminated because the time expended exceeded the value
			 * specified in the TimeLimit parameter. SOLUTION_LIMIT 10 Optimization
			 * terminated because the number of solutions found reached the value specified
			 * in the SolutionLimit parameter. INTERRUPTED 11 Optimization was terminated by
			 * the user. NUMERIC 12 Optimization was terminated due to unrecoverable
			 * numerical difficulties. SUBOPTIMAL 13 Unable to satisfy optimality
			 * tolerances; a sub-optimal solution is available. INPROGRESS 14 An
			 * asynchronous optimization call was made, but the associated optimization run
			 * is not yet complete. USER_OBJ_LIMIT 15 User specified an objective limit (a
			 * bound on either the best objective or the best bound), and that limit has
			 * been reached.
			 */

			System.out.println("Azahl genutzter Bins:" + model.get(GRB.DoubleAttr.ObjVal));
			System.out.println("Bin Belegung:");
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (t[i][j].get(GRB.DoubleAttr.X) == 1.0) {
						System.out.println("t_  " + i + " " + j + "=" + t[i][j].get(GRB.DoubleAttr.X));
					}
				}
			}
		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " + e.getMessage());
		}
	}
}
