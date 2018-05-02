import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * This Class reads a File "instance_name.dem" containing the demands of each product in each period.
 * Other Objects may ask this Class for the demand values of the current period.
 * The demand values are returned for one period at a time. When demand values are returned 
 * there is a waiting time before the next set of demand values can be reported.
 */

public class DemandModule {
	
    private static String delims = "[ ]+";
    //private static long timeBetweenPeriods = 60000000000L; // 60 seconds
    private static long timeBetweenPeriods = 600000000L; // .6 seconds
    private static boolean demandFromFile = true;
    
    private HashMap<Integer, ArrayList<Integer>> pastDemands;
	
    private int numberOfProducts = 0;
	private int numberOfPeriods = 0;
	private int currentPeriod = 0;
	private BufferedReader br;
	
	private long lastDemandCall = 0;

	public DemandModule(String instanceName, int numberOfProducts) {
		try {
			br = new BufferedReader(new FileReader("./instances/" + instanceName + ".dem"));
			numberOfPeriods = Integer.parseInt(br.readLine());
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.numberOfProducts = numberOfProducts;
		pastDemands = new HashMap<Integer, ArrayList<Integer>>();
		for (int i = 0; i < numberOfProducts; i++) {
			pastDemands.put(i, new ArrayList<>());
		}
	}
	
	public int[] getNewDemand() {
		if (currentPeriod == -1) {
			currentPeriod++;
			int[] returnArray = new int[numberOfProducts];
			for (int i = 0; i < numberOfProducts; i++) {
				returnArray[i] = 0;
			}
			return returnArray;
		}
		if (demandFromFile) {
			return getNewDemandFromFile();
		}
		else {
			return getNewDemandFromWeb();
		}
	}
	
	private int[] getNewDemandFromFile() {
		if (currentPeriod < numberOfPeriods) {
			if (System.nanoTime() - lastDemandCall < timeBetweenPeriods) {	// call for new demand is too early. wait till period is over.
				try {
					Thread.sleep(Math.max(0, (timeBetweenPeriods + lastDemandCall - System.nanoTime()) / 1000000));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			String line = null;
			try {
				line = br.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
	        String[] lineValues = line.split(delims);	// split values by defined delimiters
	        
	        assert lineValues.length == numberOfProducts: "Fehlerhafte Instanz! Anzahl gegebener Nachfragen stimmt nicht mit Anzahl Produkttypen überein.";
	        
	        int[] demand = new int[numberOfProducts];
	        for(int i = 0; i < numberOfProducts; i++) {	// parse Integer values from the String values
	        	demand[i] = Integer.parseInt(lineValues[i]);
	        	
	        	pastDemands.get(i).add(demand[i]);
	        }
	        
	        currentPeriod++;
	        lastDemandCall = System.nanoTime();
	        
	        return demand;
		}
		else {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return new int[0];
		}
	}
	
	private int[] getNewDemandFromWeb() {
		return new int[0];
	}
	
	
	
	
	
	public HashMap<Integer, ArrayList<Integer>> getPastDemands() {
		return pastDemands;
	}
	
	
	
	
	public void closeBufferedReader() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
