package unused;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;



/*
 * This Class generates random instances of the given problem.
 * Data is written in files
 *   instance_name.in			static problem parameters such as Startkapital, Fixkosten, Lagervolumen, Lagerkosten, ...  
 *   instance_name.deminfo		static information about the distribution of demand
 *   instance_name.dem			the actual demand, that is distributed consistent to information given in instance_name.deminfo
 */

public class RandomInstanceGenerator {

	public static void main(String[] args) {
		SecureRandom rand = new SecureRandom();
		
		String instanceName = null;
		boolean schwer = true;
		int numberOfProducts = rand.nextInt(8) + 5;
		int numberOfPeriods = rand.nextInt(200) + 500;
		int startCapital = rand.nextInt(8000) + 2000;
		int fixcost = rand.nextInt(Math.min(2000, startCapital/2)) + 1000;
		int storeVolume = rand.nextInt(600) + 100;
		int storeCost = rand.nextInt(startCapital/10) + 500;
		
		// Check if Arguments are given and overwrite the corresponding randomly generated numbers
		switch (args.length) {
			case 8: storeVolume = Integer.parseInt(args[7]);
			case 7: storeCost = Integer.parseInt(args[6]);
			case 6: fixcost = Integer.parseInt(args[5]);
			case 5: startCapital = Integer.parseInt(args[4]);
			case 4: numberOfPeriods = Integer.parseInt(args[3]);
			case 3: numberOfProducts = Integer.parseInt(args[2]); 
			case 2: schwer = Boolean.parseBoolean(args[1]);
			case 1: instanceName = args[0]; break;
			default: instanceName = "instance"+rand.nextInt(100); break;
		}
		
		int[] productionConstraint = new int[numberOfProducts];
		int[] productionCost = new int[numberOfProducts];
		int[] sellValue = new int[numberOfProducts];
		int[] volume = new int[numberOfProducts];
		int[] disposalCost = new int[numberOfProducts];
		int[] demandMean = new int[numberOfProducts];
		int[] demandVariance = new int[numberOfProducts];
		
		
		// generate static instance parameters
		for (int i = 0; i < numberOfProducts; i++) {
			double helper = rand.nextDouble();
			while (helper < 0.5) {
				helper = rand.nextDouble();
			}
			int helper2 = 0;
			if (schwer && rand.nextDouble() < 0.5) {
				helper2 = 1;
			}
			
			
			// production cost
			productionCost[i] = (int) Math.round(rand.nextInt((int) Math.round(fixcost / numberOfProducts)) + 1 + helper2 * fixcost / numberOfProducts);
			
			// sell price
			sellValue[i] = (int) Math.round(helper * rand.nextInt((int) Math.round(helper * productionCost[i])) + 1 + helper2 * productionCost[i]);
			
			// volume
			volume[i] = (int) Math.round(Math.min(0.7 * storeVolume, rand.nextInt((int) Math.round(helper * storeVolume) + 1) + 1 + helper2 * Math.min(0.3 * (1 - helper) * storeVolume, 100)));
			
			// disposal cost
			disposalCost[i] = (int) Math.round(rand.nextInt((int) Math.round(helper * 0.5 * productionCost[i])) + 1 + helper2 * (0.8 * storeCost * volume[i]) / storeVolume);
			
			// production constraint
			productionConstraint[i] = (int) Math.round(rand.nextInt((int) Math.round((2.0 * startCapital) / productionCost[i])));
			
			// demand: mean and variance
			demandMean[i] = (int) (rand.nextInt((int) (helper * productionConstraint[i]) + 1) + helper * productionConstraint[i]);
			demandVariance[i] = (int) Math.round((rand.nextInt(Math.min(productionConstraint[i] + 1, demandMean[i] + 1)) + helper * rand.nextInt(6)) / 1.5);
		}
		
		int[][] demand = new int[numberOfPeriods][numberOfProducts];
		
		// generate demands
		for (int j = 0; j < numberOfPeriods; j++) {
			for (int i = 0; i < numberOfProducts; i++) {
				int d = -1;
				int k = 0;
				while(k++ < 10 && d < 0) {	// 10 tries to generate random number that is not negative
					d = (int) Math.round(rand.nextGaussian() * Math.sqrt(demandVariance[i]) + demandMean[i]);	// normal distributed random number
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
				bw.write(demandMean[i] + " " + demandVariance[i]);
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
