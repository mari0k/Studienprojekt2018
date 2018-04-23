import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/*
 * This Class reads a File "instance_name.dem" containing the demands of each product in each period.
 * Other Objects may ask this Class for the demand values of the current period.
 * The demand values are returned for one period at a time. When demand values are returned 
 * there is a waiting time before the next set of demand values can be reported.
 */

public class DemandModule {
	
    private static String delims = "[ ]+";
    private static long timeBetweenPeriods = 60000000000L; // 60 seconds
	
	private int numberOfPeriods = 0;
	private int currentPeriod = 0;
	private BufferedReader br;
	
	private long lastDemandCall = 0;

	public DemandModule(String filename) {
		try {
			br = new BufferedReader(new FileReader(filename));
			numberOfPeriods = Integer.parseInt(br.readLine());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int[] getNewDemand() {
		if (currentPeriod < numberOfPeriods) {
			if (System.nanoTime() - lastDemandCall < timeBetweenPeriods) {	// call for new demand is too early. wait till period is over.
				try {
					Thread.sleep(Math.max(0, (timeBetweenPeriods + lastDemandCall - System.nanoTime()) / 1000));
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
	        int[] demand = new int[lineValues.length];
	        for(int i = 0; i < lineValues.length; i++) {	// parse Integer values from the String values
	        	demand[i] = Integer.parseInt(lineValues[i]);
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
	
	

}
