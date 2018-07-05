import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Instanzgenerator2 {
	
	
	public static void main(String[] args) {
		
		Random rand = new Random();
		
		String instanzname = "Instanz";
		int produkte = 5;
		
		switch (args.length) {
			case 2: produkte = Integer.parseInt(args[1]); break;
			case 1: instanzname = args[0]; break;
			default: instanzname += rand.nextInt(1000); break;
		}
		
		
		
		int perioden = rand.nextInt(11) + 15;
		
		int startkapital = (int) Math.round(2000 * rand.nextDouble() * perioden);
		int fixkosten = Math.max(startkapital / (perioden - 1), Math.min(2 * startkapital / perioden, (int) Math.round((startkapital / 4) * rand.nextDouble())));
		
		int lagervolumen = rand.nextInt(100) + 150;
		int lagerkosten = (int) Math.round((fixkosten / 2) * rand.nextDouble()) + rand.nextInt(fixkosten / 2);
		

		int[] herstellkosten = new int[produkte];
		int[] verkaufserloes = new int[produkte];
		int[] volumen = new int[produkte];
		int[] wegwerfkosten = new int[produkte];
		int[] produktionsschranke = new int[produkte];
		
		int[] erwartungswert = new int[produkte];
		int[] varianz = new int[produkte];
		
		
		double standardabweichung;
		
		for (int i = 0; i < produkte; i++) {

			herstellkosten[i] = rand.nextInt(fixkosten);
			verkaufserloes[i] = herstellkosten[i] + rand.nextInt(fixkosten / 2);
			volumen[i] = rand.nextInt(lagervolumen / produkte) + (int) Math.round(rand.nextDouble() * (lagervolumen / 5));
			wegwerfkosten[i] = (int) Math.round((0.5 + (0.5 * rand.nextDouble())) * (volumen[i] * lagerkosten) / lagervolumen) ;
			produktionsschranke[i] = (int) Math.round((0.8 + (rand.nextDouble() / 3)) * startkapital / herstellkosten[i]);
			
			erwartungswert[i] = (int) Math.round(0.6 * produktionsschranke[i]) + rand.nextInt((int) Math.round(0.4 * produktionsschranke[i]));
			standardabweichung = (0.6 + (rand.nextDouble() / 4)) * Math.min(erwartungswert[i], produktionsschranke[i]);
			varianz[i] = (int) Math.round(standardabweichung * standardabweichung);
				
		}
		

		int[][] nachfrage = new int[perioden][produkte];
		
		// generate demands
		for (int j = 0; j < perioden; j++) {
			for (int i = 0; i < produkte; i++) {
				int d = -1;
				int k = 0;
				while(k++ < 10 && d < 0) {	// 10 tries to generate random number that is not negative
					d = (int) Math.round(rand.nextGaussian() * Math.sqrt(varianz[i]) + erwartungswert[i]);	// normal distributed random number
				}
				nachfrage[j][i] = Math.max(0, d);
			}
		}
		
		
		
		
		// write static instance data to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/" + instanzname + ".in"));
			
			bw.write(startkapital + " " + fixkosten + " " + lagerkosten + " " + lagervolumen + " " + produkte);
			bw.newLine();
			for (int i = 0; i < produkte; i++) {
				bw.write(herstellkosten[i] + " " + verkaufserloes[i] + " " + volumen[i] + " " + wegwerfkosten[i] + " " + produktionsschranke[i]);
				bw.newLine();
			}
			
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// write static demand parameters to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/" + instanzname + ".deminfo"));
			
			bw.write(produkte+"");
			bw.newLine();
			for (int i = 0; i < produkte; i ++) {
				bw.write(erwartungswert[i] + " " + varianz[i]);
				bw.newLine();
			}
			
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// write demand values to file
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter("./instances/" + instanzname + ".dem"));
			
			bw.write(perioden+"");
			bw.newLine();
			for (int j = 0; j < perioden; j++) {
				bw.write(nachfrage[j][0]+"");
				for (int i = 1; i < produkte; i ++) {
					bw.write(" " + nachfrage[j][i]);
				}
				bw.newLine();
			}
			
			bw.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}

}
