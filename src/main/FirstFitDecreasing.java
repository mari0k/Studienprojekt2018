package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FirstFitDecreasing {
	
	public static int[][] pack(int binSize, int[] bestand, int[] v) {
		ArrayList<BinFFD> bins = new ArrayList<>();
		Item[] items = new Item[bestand.length];
		for (int i = 0; i < bestand.length; i++) {
			items[i] = new Item(i, bestand[i], v[i]);
		}
		Arrays.sort(items);
		// packe Items in Bins
		for (int i = 0; i < items.length; i++) {
			while (items[i].getAmount() > 0) {
				boolean newBinNeeded = true;
				for (BinFFD bin : bins) {
					items[i].setAmount(items[i].getAmount() - bin.addAsManyAsPossible(items[i]));
					if (items[i].getAmount() == 0) {
						newBinNeeded = false;
						break;
					}
				}
				while (newBinNeeded) {
					BinFFD bin = new BinFFD(binSize);
					items[i].setAmount(items[i].getAmount() - bin.addAsManyAsPossible(items[i]));
					bins.add(bin);
					if (items[i].getAmount() == 0) newBinNeeded = false;
				}
			}
		}
		// konstruiere Return-Array
		int[][] returnArray = new int[bestand.length][bins.size()];
		for (int i = 0; i < bestand.length; i++) {
			for (int j = 0; j < bins.size(); j++) {
				returnArray[i][j] = 0;
			}
		}
		int counter = 0;
		for (BinFFD bin : bins) {
			for (int i : bin.getContent().keySet()) {
				returnArray[i][counter] = bin.getContent().get(i);
			}
			counter++;
		}
		
		return returnArray;
	}

}

class BinFFD {
	
	private int free;
	private HashMap<Integer, Integer> content;
	
	public BinFFD(int size) {
		this.free = size;
		this.content = new HashMap<>();
	}
	
	public int addAsManyAsPossible(Item item) {
		int i = 0;
		for (; i < item.getAmount(); i++) {
			if (free > item.getVolume()) {
				free -= item.getVolume();
			} else {
				break;
			}
		}
		if (i > 0) content.put(item.getId(), i);
		return i;
	}
	
	public HashMap<Integer, Integer> getContent() {
		return content;
	}
}

class Item implements Comparable<Item> {
	
	private int id;
	private int amount;
	private int volume;
	
	public Item(int id, int amount, int volume) {
		this.id = id;
		this.amount = amount;
		this.volume = volume;
	}

	@Override
	public int compareTo(Item item) {
		return item.getVolume() - this.volume;	// sort descending
	}

	public int getId() {
		return id;
	}

	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int k) {
		this.amount = k;
	}

	public int getVolume() {
		return volume;
	}	
	
}
