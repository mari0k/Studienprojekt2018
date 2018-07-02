package unused;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Instance {
	
	private static String delims = "[ ]+";
	
	private String instanceName;
	private boolean knownDistributions;
	
	private int S = -1;
	private int F = -1;
	private int C = -1;
	private int V = -1;
	private int n = -1;
	private int[] c = {};
	private int[] p = {};
	private int[] v = {};
	private int[] w = {};
	private int[] b = {};
	
	private int[] mean = {};
	private double[] sd = {};

	public Instance(String instanceName, boolean knownDistributions) {
		this.instanceName = instanceName;
		this.knownDistributions = knownDistributions;
		
		readStaticData();
		if (knownDistributions) readStaticDemandData();
	}
	

	private void readStaticData() {
		try {
            BufferedReader br = new BufferedReader (new FileReader("./instances/" + instanceName + ".in"));
            String line = br.readLine();
            String[] lineValues = line.split(delims);
            S = Integer.parseInt(lineValues[0]);
            F = Integer.parseInt(lineValues[1]);
            C = Integer.parseInt(lineValues[2]);
            V = Integer.parseInt(lineValues[3]);
            n = Integer.parseInt(lineValues[4]);            
            c = new int[n];
            p = new int[n];
            v = new int[n];
            w = new int[n];
            b = new int[n];
            
            for (int i = 0; i < n; i++) {
            	line = br.readLine();
            	lineValues = line.split(delims);
            	c[i] = Integer.parseInt(lineValues[0]);
            	p[i] = Integer.parseInt(lineValues[1]);
            	v[i] = Integer.parseInt(lineValues[2]);
            	w[i] = Integer.parseInt(lineValues[3]);
            	b[i] = Integer.parseInt(lineValues[4]);           	
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
	}

	private void readStaticDemandData() {
		try {
            BufferedReader br = new BufferedReader (new FileReader("./instances/" + instanceName + ".deminfo"));
            String line = br.readLine();
            
            assert Integer.parseInt(line) == n: "Fehlerhafte Instanz! Anzahl Produkttypen stimmt nicht überein.";

            mean = new int[n];
            sd = new double[n];

            for (int i = 0; i < n; i++) {
            	line = br.readLine();
            	String[] lineValues = line.split(delims);
            	mean[i] = Integer.parseInt(lineValues[0]);
            	sd[i] = Math.sqrt(Integer.parseInt(lineValues[1]));
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
		
	}

	
	

	public String getInstanceName() {
		return instanceName;
	}

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	public boolean isKnownDistributions() {
		return knownDistributions;
	}

	public void setKnownDistributions(boolean knownDistributions) {
		this.knownDistributions = knownDistributions;
	}

	public int getS() {
		return S;
	}

	public void setS(int s) {
		S = s;
	}

	public int getF() {
		return F;
	}

	public void setF(int f) {
		F = f;
	}

	public int getC() {
		return C;
	}

	public void setC(int c) {
		C = c;
	}

	public int getV() {
		return V;
	}

	public void setV(int v) {
		V = v;
	}

	public int getN() {
		return n;
	}

	public void setN(int n) {
		this.n = n;
	}
	
	
	
	
	
	


	public int[] get_p() {
		return p;
	}
	
	public void set_p(int[] p) {
		this.p = p;
	}

	public int[] get_w() {
		return w;
	}

	public void set_w(int[] w) {
		this.w = w;
	}

	public int[] get_b() {
		return b;
	}

	public void set_b(int[] b) {
		this.b = b;
	}

	public int[] get_c() {
		return c;
	}
	
	public void set_c(int[] c) {
		this.c = c;
	}

	public int[] get_v() {
		return v;
	}

	public void set_v(int[] v) {
		this.v = v;
	}

	
	
	
	
	public int[] getMean() {
		assert knownDistributions: "Fehler! Erwartungswert abgefragt, aber nicht bekannt.";
		return mean;
	}
	
	public void setMean(int[] mean) {
		assert knownDistributions: "Fehler! Erwartungswert soll gesetzt werden, aber Instanz mit unbekannten Verteilungen.";
		this.mean = mean;
	}

	public double[] getSd() {
		assert knownDistributions: "Fehler! Standardabweichung abgefragt, aber nicht bekannt.";
		return sd;
	}
	
	public void setSd(double[] sd) {
		assert knownDistributions: "Fehler! Standardabweichung soll gesetzt werden, aber Instanz mit unbekannten Verteilungen.";
		this.sd = sd;
	}
	
	

}
