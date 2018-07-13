package unused;
import java.util.LinkedList;

public class Bin {

	private int binNummer;
	private LinkedList<Element> inhalt;
	private int freieKapazitaet;
	
	public Bin(int binNummer, int kapazitaet) {
		this.binNummer = binNummer;
		inhalt = new LinkedList<Element>();
		freieKapazitaet = kapazitaet;
	}
	
	
	public void packeElementRein(Element element) {
		freieKapazitaet -= element.getGroesse();
		inhalt.add(element);
		element.setNummerBin(binNummer);
	}
	
	
	
	public boolean passtElementNochRein(Element element) {
		if (freieKapazitaet >= element.getGroesse()) {
			return true;
		} else {
			return false;
		}
	}
	
	
}
