import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;

/*
 * This Class generates random instances of the given problem.
 * Data is written in files
 *   instance_name.in			static problem parameters such as Startkapital, Fixkosten, Lagervolumen, Lagerkosten, ...  
 *   instance_name.deminfo		static information about the distribution of demand
 *   instance_name.dem			the actual demand, which is distributed consistent to information given in instance_name.deminfo
 */

public class RandomInstanceGenerator {

	public static void main(String[] args) {
		SecureRandom rand = new SecureRandom();
		
		String instanceName = null;
		int numberOfProducts = rand.nextInt(20) + 10;
		int numberOfPeriods = rand.nextInt(800) + 20;
		int startCapital = rand.nextInt(80000) + 20000;
		int fixcost = rand.nextInt(Math.min(10000, startCapital/2)) + 100;
		int storeVolume = rand.nextInt(600) + 100;
		int storeCost = rand.nextInt(startCapital/20) + 20;
		
		// Check if Arguments are given and overwrite the corresponding randomly generated numbers
		switch (args.length) {
			case 5: fixcost = Integer.parseInt(args[4]);
			case 4: startCapital = Integer.parseInt(args[3]);
			case 3: numberOfPeriods = Integer.parseInt(args[2]);
			case 2: numberOfProducts = Integer.parseInt(args[1]); 
			case 1: instanceName = args[0]; break;
			default: instanceName = "instance"+rand.nextInt(100); break;
		}
		
		int[] productionConstraint = new int[numberOfProducts];
		int[] productionCost = new int[numberOfProducts];
		int[] sellValue = new int[numberOfProducts];
		int[] volume = new int[numberOfProducts];
		int[] disposalCost = new int[numberOfProducts];
		int[] demandMean = new int[numberOfProducts];
		int[] demandStandardDeviation = new int[numberOfProducts];
		
		
		// generate static instance parameters
		for (int i = 0; i < numberOfProducts; i++) {
			productionConstraint[i] = rand.nextInt(100);
			productionCost[i] = rand.nextInt(100);
			sellValue[i] = rand.nextInt(200) + productionCost[i];
			volume[i] = rand.nextInt(storeVolume/3 + 1) + 1;
			disposalCost[i] = (int) (rand.nextInt((int) ((sellValue[i] - productionCost[i])*0.9) + 1) + productionCost[i]*0.9);
			demandMean[i] = (int) (rand.nextInt((int) (productionConstraint[i]*0.5) + 1) + productionConstraint[i]*0.9);
			demandStandardDeviation[i] = rand.nextInt(Math.min(productionConstraint[i] + 1, demandMean[i] + 1));
		}
		
		int[][] demand = new int[numberOfPeriods][numberOfProducts];
		
		// generate demands
		for (int j = 0; j < numberOfPeriods; j++) {
			for (int i = 0; i < numberOfProducts; i++) {
				int d = -1;
				int k = 0;
				while(k++ < 10 && d < 0) {	// 10 tries to generate random number that is not negative
					d = (int) Math.round(rand.nextGaussian()*demandStandardDeviation[i]+demandMean[i]);	// normal distributed random number
				}
				demand[j][i] = Math.max(0, d);
			}
		}
		
		
		
		// write static instance data to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/"+instanceName+".in"));
			
			bw.write(startCapital + " " + fixcost + " " + storeCost + " " + storeVolume + " " + numberOfProducts);
			bw.newLine();
			for (int i = 0; i < numberOfProducts; i++) {
				bw.write(productionCost[i] + " " + sellValue[i] + " " + volume[i] + " " + disposalCost[i] + " " + productionConstraint[i]);
				bw.newLine();
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// write static demand parameters to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/"+instanceName+".deminfo"));
			
			bw.write(numberOfProducts+"");
			bw.newLine();
			for (int i = 0; i < numberOfProducts; i ++) {
				bw.write(demandMean[i] + " " + demandStandardDeviation[i]);
				bw.newLine();
			}
			
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// write demand values to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/"+instanceName+".dem"));
			
			bw.write(numberOfPeriods+"");
			bw.newLine();
			for (int j = 0; j < numberOfPeriods; j++) {
				bw.write(demand[j][0]+"");
				for (int i = 1; i < numberOfProducts; i ++) {
					bw.write(" " + demand[j][i]);
				}
				bw.newLine();
			}
			
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
