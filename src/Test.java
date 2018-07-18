import java.util.Arrays;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.math3.distribution.NormalDistribution;

import main.ImprovedLHS;


public class Test {
	
	/*
	 * Klasse zum Testen verschiedener Verfahren zum Erzeugen der Szenarien
	 */

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	
            	int n = 1;
            	
            	int[] mean = new int[n];
				int[] var = new int[n];
				for (int i = 0; i < n; i++) {
					mean[i] = 50;
					var[i] = 50;
				}
				
				
				int d = 301;
				
				int[][] lhs = ImprovedLHS.normal_lhs(d, mean, var);
				
				int[][] lhs_t = new int[n][d];
				
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < d; j++) {
						lhs_t[i][j] = lhs[j][i];
					}
				}
				
				for (int i = 0; i < n; i++ ) {
					Arrays.sort(lhs_t[i]);
				}
				

                new TestHistogram(lhs_t[0]);
                
				//int[] lhs = szenarienKonstrukteur(mean[0], var[0], 101);
				//int[] lhs = lhsSampling(mean[0], var[0], 30001);
				
				Random rand = new Random();
				
				for (int i = 0; i < n; i++) {
					lhs_t[0][i] = (int) Math.round(mean[0] + Math.sqrt(var[0]) * rand.nextGaussian());
				}
				
				
                new TestHistogram(lhs_t[0]);
            }
        });
		
		
		
	}
	
	
	
	public static int[] lhsSampling(int erwartungswert, int varianz, int anzahl) {
		double faktor = 1.0 / (anzahl + 1);
		NormalDistribution distribution = new NormalDistribution(erwartungswert, Math.sqrt(varianz));
		//NormalDistribution distribution = new NormalDistribution(erwartungswert, Math.sqrt(varianz), 1e-12);
		int[] rueck = new int[anzahl];
		for (int i = 0; i < rueck.length; i++) {
			rueck[i] = Math.max(0, (int) Math.round(distribution.inverseCumulativeProbability(faktor * (i + 1))));
		}
		return rueck;
	}

	
	
	public static int[] szenarienKonstrukteur(int erwartungswert, int varianz, int anzahlSzenarien) {
		NormalDistribution distribution = new NormalDistribution(erwartungswert, Math.sqrt(varianz), 1e-16);
		int maxWert = (int) Math.round(distribution.inverseCumulativeProbability(0.99999999));
		double[] wkeiten = new double[maxWert + 1];
		wkeiten[0] = distribution.cumulativeProbability(0.5);
		for (int i = 1; i < wkeiten.length; i++) {
			wkeiten[i] = distribution.cumulativeProbability(i + 0.5) - distribution.cumulativeProbability(i - 0.5);
		}
		int[] anzahlen = new int[maxWert + 1];
		anzahlen[erwartungswert] = 1;
		for (int i = 1; i < anzahlSzenarien; i++) {
			int index = -1;
			double maxAbweichung = 0.0;
			for (int j = 0; j < wkeiten.length; j++) {
				double aktWkeit = 1.0 * anzahlen[j] / (1.0 * i);
				double diff = wkeiten[j] - aktWkeit;
				if (diff > maxAbweichung) {
					maxAbweichung = diff;
					index = j;
				}
			}
			if (maxAbweichung == 0) {
				System.out.println("Komisch!");
				System.exit(1);
			}
			anzahlen[index] += 1;
		}
		int[] rueck = new int[anzahlSzenarien];
		int index = 0;
		for (int i = 0; i < anzahlen.length; i++) {
			for (int j = 1; j <= anzahlen[i]; j++) {
				rueck[index] = i;
				index += 1;
			}
		}
		return rueck;
	}





	
	
	public static int[] neuesLHS(int erwartungswert, int varianz, int anzahlSzenarien) {
		NormalDistribution distribution = new NormalDistribution(erwartungswert, Math.sqrt(varianz), 1e-16);
		int maxWert = (int) Math.round(distribution.inverseCumulativeProbability(0.999999)); // kann man in Abhaengigkeit von anzahlSzenarien setzen
		double[] wkeiten = new double[maxWert + 1];
		wkeiten[0] = distribution.cumulativeProbability(0.5);
		for (int i = 1; i < wkeiten.length; i++) {
			wkeiten[i] = distribution.cumulativeProbability(i + 0.5) - distribution.cumulativeProbability(i - 0.5);
		}
		int[] anzahlen = new int[maxWert + 1];
		int gesamtAnzahl = 0;
		for (int i = 0; i < anzahlen.length; i++) {
			anzahlen[i] = (int) Math.round(anzahlSzenarien * wkeiten[i]);
			gesamtAnzahl += anzahlen[i];
		}
		if (gesamtAnzahl < anzahlSzenarien) {
			for (int i = 1; i <= anzahlSzenarien - gesamtAnzahl; i++) {
				double maxDiff = -Double.MAX_VALUE;
				int index = -1;
				for (int j = 0; j < anzahlen.length; j++) {
					double diff = wkeiten[j] - 1.0 * anzahlen[j] / anzahlSzenarien;
					if (diff > maxDiff) {
						maxDiff = diff;
						index = j;
					}
				}
				assert (index != -1);
				anzahlen[index] += 1;
			}
		} else {
			if (gesamtAnzahl > anzahlSzenarien) {
				for (int i = 1; i <= gesamtAnzahl - anzahlSzenarien; i++) {
					double minDiff = Double.MAX_VALUE;
					int index = -1;
					for (int j = 0; j < anzahlen.length; j++) {
						double diff = wkeiten[j] - 1.0 * anzahlen[j] / anzahlSzenarien;
						if (diff < minDiff) {
							minDiff = diff;
							index = j;
						}
					}
					assert (index != -1);
					anzahlen[index] -= 1;
				}
			}
		}
		int[] rueck = new int[anzahlSzenarien];
		int index = 0;
		for (int i = 0; i < anzahlen.length; i++) {
			for (int j = 1; j <= anzahlen[i]; j++) {
				rueck[index] = i;
				index += 1;
			}
		}
		return rueck;
	}

	
}



class TestHistogram {
//http://stackoverflow.com/a/12520104/714968

    public TestHistogram(int[] data) {
        // For this example, I just randomised some data, you would
        // Need to load it yourself...
        
        Map<Integer, Integer> mapHistory = new TreeMap<Integer, Integer>();
        for (int r = 0; r < data.length; r++) {
            int value = data[r];
            int amount = 0;
            if (mapHistory.containsKey(value)) {
                amount = mapHistory.get(value);
                amount++;
            } else {
                amount = 1;
            }
            mapHistory.put(value, amount);
        }
        
        JFrame frame = new JFrame("Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(new JScrollPane(new Graph(mapHistory)));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    
    @SuppressWarnings("serial")
	protected class Graph extends JPanel {

        protected static final int MIN_BAR_WIDTH = 10;
        private Map<Integer, Integer> mapHistory;

        public Graph(Map<Integer, Integer> mapHistory) {
            this.mapHistory = mapHistory;
            int width = (mapHistory.size() * MIN_BAR_WIDTH) + 11;
            Dimension minSize = new Dimension(width, 128);
            Dimension prefSize = new Dimension(width, 256);
            setMinimumSize(minSize);
            setPreferredSize(prefSize);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (mapHistory != null) {
                int xOffset = 5;
                int yOffset = 5;
                int width = getWidth() - 1 - (xOffset * 2);
                int height = getHeight() - 1 - (yOffset * 2);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawRect(xOffset, yOffset, width, height);
                int barWidth = Math.max(MIN_BAR_WIDTH,
                        (int) Math.floor((float) width
                        / (float) mapHistory.size()));
                System.out.println("width = " + width + "; size = "
                        + mapHistory.size() + "; barWidth = " + barWidth);
                int maxValue = 0;
                for (Integer key : mapHistory.keySet()) {
                    int value = mapHistory.get(key);
                    maxValue = Math.max(maxValue, value);
                }
                int xPos = xOffset;
                for (Integer key : mapHistory.keySet()) {
                    int value = mapHistory.get(key);
                    int barHeight = Math.round(((float) value
                            / (float) maxValue) * height);
                    g2d.setColor(new Color(key, key, key));
                    int yPos = height + yOffset - barHeight;
//Rectangle bar = new Rectangle(xPos, yPos, barWidth, barHeight);
                    Rectangle2D bar = new Rectangle2D.Float(
                            xPos, yPos, barWidth, barHeight);
                    g2d.fill(bar);
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.draw(bar);
                    xPos += barWidth;
                }
                g2d.dispose();
            }
        }
    }
}