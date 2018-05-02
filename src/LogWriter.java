import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LogWriter {

	private BufferedWriter bw;
	
	public LogWriter(String dateipfad, String dateiname) {
		try {
			bw = new BufferedWriter(new FileWriter(dateipfad + dateiname));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void schreibeProduktion(int[] daten) {
		String str = String.valueOf(daten[0]);
		for (int i = 1; i < daten.length; i++) {
			str += " " + String.valueOf(daten[i]);
		}
		
		try {
			bw.write(str);
			bw.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void schreibeLager(int[][] lager) {
		try {
			bw.write(String.valueOf(lager.length));
			bw.newLine();
			
			for (int i = 0; i < lager.length; i++) {
				schreibeProduktion(lager[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void schreibeKapital(long k) {
		try {
			bw.write(String.valueOf(k));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
