package unused;

import java.util.*;


public class Sortiere_Absteigend implements Comparator<Produkt> {

	public int compare(Produkt a, Produkt b) {
		
		double gewinnA = a.getP()-a.getC();
		double gewinnB = b.getP()-b.getC();

		if (gewinnA < gewinnB) {
			return 1;
		} else {
			return -1;
		}

	}

}