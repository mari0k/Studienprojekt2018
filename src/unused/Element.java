package unused;

public class Element implements Comparable<Element> {

	private int groesse; // Groesse / Gewicht des Elements
	private int typId; // wenn es n verschiedene Typen gibt: ganze Zahl zwischen 0 und n-1
	private int nummerBin; // Nummer des zugeordneten Bins: -1 fuer nicht zugeordnet, ansonten 0,1,2,3,...
	private int nummerInnerhalbDesTyps; // wenn die Multiplizitaet bei diesem Typ m betraegt: ganze Zahl zwischen 1 und m
	
	
	public Element(int groesse, int typId, int nummerInnerhalbDesTyps) {
		nummerBin = -1;
		this.groesse = groesse;
		this.typId = typId;
		this.nummerInnerhalbDesTyps = nummerInnerhalbDesTyps;
	}
	
	
	@Override
	public int compareTo(Element element) {
		return element.groesse - this.groesse;
	}


	public int getGroesse() {
		return groesse;
	}


	public int getNummerBin() {
		return nummerBin;
	}


	public void setNummerBin(int nummerBin) {
		this.nummerBin = nummerBin;
	}
	
	
	
}
