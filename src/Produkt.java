package src;


import java.util.*;

public class Produkt {

	 int c;
	 int p;
	 int v;
	 int w;
	 int b;
	 int index;
	 
	 public Produkt() {
		 
	 }
	 
	 public Produkt(Instanz inst, int index) {
		 this.c = inst.getHerstellkosten()[index];
		 this.p= inst.getVerkaufserloes()[index];
		 this.v=inst.getVolumen()[index];
		 this.w=inst.getWegwerfkosten()[index];
		 this.b=inst.getProduktionsschranke()[index];
		 this.index = index;
	 }
	 
	public LinkedList<Produkt> erzeugeProdukte(Instanz inst){
		LinkedList<Produkt> ausgabe = new LinkedList<Produkt>();
		
		for(int i=0;i<inst.getProduktionsschranke().length;i++) {
			Produkt tmp = new Produkt(inst, i);
			ausgabe.add(tmp);
		}
		return ausgabe;
	}

	public int getC() {
		return c;
	}

	public void setC(int c) {
		this.c = c;
	}

	public int getP() {
		return p;
	}

	public void setP(int p) {
		this.p = p;
	}

	public int getV() {
		return v;
	}

	public void setV(int v) {
		this.v = v;
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getB() {
		return b;
	}

	public void setB(int b) {
		this.b = b;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
	
	
}
