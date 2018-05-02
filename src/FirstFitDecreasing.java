import java.util.ArrayList;
import java.util.Arrays;

public class FirstFitDecreasing {
	
	public static int packe(int binSize, int[] items) {
		int numBins = 0;
		ArrayList<Bin> bins = new ArrayList<>();
		Arrays.sort(items);
		
		for (int i = items.length -1; i > -1; i--) {
			boolean newBinNeeded = true;
			for (Bin bin : bins) {
				if (bin.fits(items[i])) {
					bin.add(items[i]);
					newBinNeeded = false;
					break;
				}
			}
			if (newBinNeeded) {
				bins.add(new Bin(binSize - items[i]));
				numBins++;
			}
		}
		
		return numBins;
	}
	
	public static int[] prepare(int[] bestand, int[] volumes) {
		int sum = 0;
		for (int i = 0; i < bestand.length; i++) {
			sum += bestand[i];
		}
		int[] items = new int[sum];
		int k = 0;
		for (int i = 0; i < bestand.length; i++) {
			for (int j = 0; j < bestand[i]; j++) {
				items[k] = volumes[i];
				k++;
			}
		}
		
		return items;
	}

}

class Bin {
	
	private int free;
	
	public Bin(int size) {
		this.free = size;
		
	}
	
	public boolean fits(int item) {
		return (item < free);
	}
	
	public void add(int item) {
		free -= item;
		assert free >= 0: "Fehler! Bin zu voll.";
	}
}
