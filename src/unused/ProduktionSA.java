package unused;
import java.util.Random;
import main.Instanz;
import org.apache.commons.math3.distribution.NormalDistribution;

public class ProduktionSA {
	
	/*
	 * Einfache Testklasse!
	 * 
	 * - Paramter nicht optimiert
	 * - Wird nicht verwendet
	 * 
	 */

    private static double maxTemp = - 1 / Math.log(0.999);
    private static double minTemp = - 1 / Math.log(0.9);
    private static int runtime = 10; // seconds
    private static int runs = 1000;
    
    
	
    public static int[] los(Instanz inst, int[] lagerbewertung) {
        long starttime = System.nanoTime();
        double alpha = Math.pow(minTemp / maxTemp, 1.0 / runtime);
        double temp = maxTemp;
        
        
        double zfw = 0;
        int[] produktion = new int[inst.getAnzahlProdukte()];
        double bestZFW = 0;
        int[] bestProduktion = new int[inst.getAnzahlProdukte()];
        for (int i = 0; i < inst.getAnzahlProdukte(); i++) {
        	produktion[i] = 0;
        	bestProduktion[i] = 0;
        }
        
        int restkapital = inst.getAktuellesKapital();
        
        while (temp >= minTemp) {
            for (int run = runs; run <= runs; run++) {
                int[] mutation = bestimmeZulaessigeMutation(inst, produktion, restkapital);
                double mutationExpectedDeltaZfw = expectedDeltaZfw(inst, lagerbewertung[mutation[0]], produktion[mutation[0]], mutation[0], mutation[1]);
                if (mutationExpectedDeltaZfw >= 0) {
                    produktion[mutation[0]] += mutation[1];
                    restkapital -= inst.getProdukte()[mutation[0]].getHerstellungskosten() * mutation[1];
                    zfw += mutationExpectedDeltaZfw;                }
                else {
                    if (Math.random() < Math.exp(mutationExpectedDeltaZfw / temp)) {
                    	if (zfw >= bestZFW) {
                    		bestZFW = zfw;
                    		bestProduktion = produktion.clone();
                    	}
                        produktion[mutation[0]] += mutation[1];
                        restkapital -= inst.getProdukte()[mutation[0]].getHerstellungskosten() * mutation[1];
                        zfw += mutationExpectedDeltaZfw;
                    }
                }
            }
            temp = maxTemp * Math.pow(alpha,(System.nanoTime() - starttime) / 1000000000.0);
        }
        if(zfw >= bestZFW) {
        	bestZFW = zfw;
        	bestProduktion = produktion.clone();
        }
        
        return bestProduktion;
    }
    
    private static int[] bestimmeZulaessigeMutation(Instanz inst, int[] produktion, int restkapital) {
    	Random rand = new Random();
    	int produkt = 0;
    	int anzahl = 0;
    	int maxIter = 0;
    	while (maxIter < 1000) {
	    	produkt = rand.nextInt(inst.getAnzahlProdukte());
	    	if (rand.nextBoolean() || produktion[produkt] == 0) {
	    		anzahl = rand.nextInt(Math.min(3, (int) restkapital/ inst.getProdukte()[produkt].getHerstellungskosten())) + 1;
	    	}
	    	else {
	    		anzahl = - rand.nextInt(Math.min(3, produktion[produkt])) - 1;
	    	}
			if (restkapital >= anzahl * inst.getProdukte()[produkt].getHerstellungskosten() && produktion[produkt] + anzahl >= 0 &&
					produktion[produkt] + anzahl <= inst.getProdukte()[produkt].getHerstellungskosten()) {
				break;
			}
			maxIter++;
		}
    	if (maxIter == 1000) {
    		produkt = 0; anzahl = 0;
    	}
    	int[] returnArray = {produkt, anzahl};
    	return returnArray;
	}
	
    private static double expectedDeltaZfw(Instanz inst, int lagerbewertung, int aktuell, int produkt, int zusaetzlich) {
    	
    	NormalDistribution distribution = new NormalDistribution(inst.getErwartungswert()[produkt], Math.sqrt(inst.getVarianz()[produkt]));
    	double p; // Wahrscheinlichkeit, dass eine weitere Einheit von produkt verkauft werden kann
    	
    	double expectedDeltaZfw = 0;
    	
    	for (int i = 0; i < zusaetzlich; i++) {
	    	p = 1 - distribution.cumulativeProbability(inst.getProdukte()[i].getAktuellerBestand() + aktuell + i + 1);
	    	
	    	expectedDeltaZfw -= inst.getProdukte()[produkt].getHerstellungskosten();
	    	expectedDeltaZfw += p * inst.getProdukte()[produkt].getVerkaufserloes();
	    	expectedDeltaZfw -= (1 - p) * (inst.getProdukte()[produkt].getVolumen() / inst.getLagervolumen()) * inst.getLagerkosten();
	    	expectedDeltaZfw += (1 - p) * lagerbewertung;
    	}
    	
    	return expectedDeltaZfw;
    }
    
}
