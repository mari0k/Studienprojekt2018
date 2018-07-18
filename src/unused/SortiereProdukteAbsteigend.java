package unused;

import java.util.*;
import main.Produkt;


public class SortiereProdukteAbsteigend implements Comparator<Produkt> {

	public int compare(Produkt a, Produkt b) {
		
		double gewinnA = a.getVerkaufserloes()-a.getHerstellungskosten();
		double gewinnB = b.getVerkaufserloes()-b.getHerstellungskosten();

		if (gewinnA < gewinnB) {
			return 1;
		} else {
			return -1;
		}

	}

}