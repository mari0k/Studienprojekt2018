package unused;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Ddsip {
	
	public static void model_lp(Instance instance, int[][] szenarien, int[] a, int kapital) {
		try {
            BufferedWriter bw = new BufferedWriter (new FileWriter("./neos/model_" + instance.getInstanceName() + ".lp"));

			bw.write("maximize");
			bw.newLine();
			bw.write(" - ");
			bw.write(String.valueOf(instance.getC()));
			bw.write(" y_1");
			for (int i = 0; i < instance.get_c().length; i++) {
				bw.write(" + ");
				bw.write(String.valueOf(instance.get_p()[i] - instance.get_c()[i]));
				bw.write(" s(" + i + ")_1");
				bw.write(" + ");
				bw.write(String.valueOf(instance.get_c()[i]));
				bw.write(" _1_x(" + i + ")");
				//bw.write(" + ");
				//bw.write(String.valueOf(instance.get_c()[i] * a[i]));
				bw.write(" - ");
				bw.write(String.valueOf(instance.get_w()[i] + instance.get_c()[i]));
				bw.write(" t(" + i + ")_1");
				bw.write(" - ");
				bw.write(String.valueOf(instance.get_c()[i]));
				bw.write(" _1_x(" + i + ")");
			}
			bw.newLine();
			
			
			bw.write("subject to");
			bw.newLine();
			for (int i = 0; i < instance.get_c().length; i++) {
				bw.write(" eins" + i + ": s(" + i + ")_1 - _1_x(" + i + ") <= " + a[i]);
				bw.newLine();
				bw.write(" zwei" + i + ": t(" + i + ")_1 - _1_x(" + i + ") + s(" + i + ")_1 <= " + a[i]);
				bw.newLine();
				bw.write(" szenarioabhaengig" + i + ": s(" + i + ")_1 <= " + szenarien[0][i]);
				bw.newLine();
			}
			bw.write(" Material: " + instance.getV() + " y_1");
			for (int i = 0; i < instance.get_c().length; i++) {
				bw.write(" - ");
				bw.write(String.valueOf(instance.get_v()[i]));
				bw.write(" _1_x(" + i + ")");
				bw.write(" + ");
				bw.write(String.valueOf(instance.get_v()[i]));
				bw.write(" s(" + i + ")_1");
				bw.write(" + ");
				bw.write(String.valueOf(instance.get_v()[i]));
				bw.write(" t(" + i + ")_1");
			}
			bw.write(" >= ");
			int help = 0;
			for (int i = 0; i < a.length; i++) {
				help += a[i] * instance.get_v()[i];
			}
			bw.write(String.valueOf(help));
			bw.newLine();
			bw.write(" Kapital: " + instance.get_c()[0] + " _1_x(0)");
			for (int i = 1; i < instance.get_c().length; i++) {
				bw.write(" + ");
				bw.write(String.valueOf(instance.get_c()[i]));
				bw.write(" _1_x(" + i +")");
			}
			bw.write(" <= ");
			bw.write(String.valueOf(kapital));
			bw.newLine();
			
			
			bw.write("bounds");
			bw.newLine();
			for (int i = 0; i < instance.get_c().length; i++) {
				bw.write(" 0 <= _1_x(" + i + ") <= " + instance.get_b()[i]);
				bw.newLine();
				bw.write(" 0 <= s(" + i + ")_1");
				bw.newLine();
				bw.write(" 0 <= t(" + i + ")_1 <= ");
				bw.write(String.valueOf(instance.get_b()[i] + a[i]));
				bw.newLine();
			}
			bw.write(" 0 <= y_1");
			bw.newLine();
			
			
			bw.write("generals");
			bw.newLine();
			for (int i = 0; i < instance.get_c().length; i++) {
				bw.write(" _1_x(" + i + ") s(" + i + ")_1 t(" + i + ")_1");
			}
			bw.write("y_1");
			bw.newLine();
			bw.write("end");
			
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
	}
	

	
	public static void rhs_sc(int[][] szenarien, String instanceName) {
		try {
            BufferedWriter bw = new BufferedWriter (new FileWriter("./neos/rhs_" + instanceName + ".sc"));

			bw.write("Names");
			bw.newLine();
			for (int i = 0; i < szenarien[0].length; i++) {
				bw.write("szenarioabhaengig" + i);
				bw.newLine();
			}
			double p = 1.0 / szenarien.length;
			for (int j = 0; j < szenarien.length; j++) {
				bw.write("scen ");
				bw.write(String.valueOf(p));
				bw.newLine();
				for (int i = 0; i < szenarien[j].length; i++) {
					bw.write(String.valueOf(szenarien[j][i]));
					bw.newLine();
				}
			}
			
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
	}
	

	
	public static void ddsip_conf(int n, int d, String instanceName) {
		try {
            BufferedWriter bw = new BufferedWriter (new FileWriter("./neos/ddsip_" + instanceName + ".conf"));

			bw.write("BEGIN");
			bw.newLine();
			bw.write("PREFIX _1_");
			bw.newLine();
			bw.write("SCENAR ");
			bw.write(String.valueOf(d));
			bw.newLine();
			bw.write("STOCRHS ");
			bw.write(String.valueOf(n));
			bw.newLine();
			bw.write("STOCCOST 0");
			bw.newLine();
			bw.write("STOCMAT 0");
			bw.newLine();
			bw.write("OUTLEV 20");
			bw.newLine();
			
            bw.close();
        }
        catch (IOException e) {
            System.out.println("Fehler: "+e.toString());
        }
	}
	
	
	
	public static void main(String[] args) {
		Instance instance = new Instance("Instanz", false);
		
		int[][] szenarien = {{9, 10, 17}, {16, 15, 29}};
		
		int[] a = {0, 0, 0};
		
		model_lp(instance, szenarien, a, 90);
		
		rhs_sc(szenarien, instance.getInstanceName());
		
		ddsip_conf(szenarien[0].length, szenarien.length, instance.getInstanceName());
		
	}

}
