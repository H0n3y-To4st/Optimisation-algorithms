import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PSOImp {

	DemandPrediction dp; // non static
	Particle[] particles;

	int dimensions = DemandPrediction.N_DEMAND_INDICATORS; // TODO
	int particlesNum = DemandPrediction.N_DEMAND_INDICATORS * 2; // double the num of dimensions, more particle then
																	// more accurate
	int iterations = 1000;

	// constants - with placeholder values
	double cognitive = 1.1193; // how pbest affects particle movement
	double inertia = 0.725; // control impact of velocity
	double social = 1.1193; // how gbest affects particle movement

	public static void main(String[] args) {
		PSOImp imp = new PSOImp();
		imp.run();

	}

	public PSOImp() {
		particles = new Particle[particlesNum];
	}

	public void run() {
		String[] lines = getLinesFromTrainCSV();
		String[] lines2 = getLinesFromTestCSV();

		double[][] totalGBest = new double[lines.length][dimensions];
		double[] averageTotalGBest = new double[dimensions];

		for (int i = 0; i < lines.length; i++) {
			var l = lines[i];
			double knownDemand = getKnownDemand(l);
			double[] indicators = getIndicators(l);

			PSOCal PC = new PSOCal(dimensions, particlesNum, iterations, knownDemand, indicators, social, cognitive,
					inertia);
			PC.initialise();
			double[] gBestForCurrentLine = PC.releaseTheSwarm();
			totalGBest[i] = gBestForCurrentLine;

		}

		for (int i = 0; i < dimensions; i++) {
			double total = 0;
			for (int j = 0; j < lines.length; j++) {
				total += totalGBest[j][i];
			}
			double average = total / lines.length;
			averageTotalGBest[i] = average;
		}

		for (int i = 0; i < lines2.length; i++) {
			var l = lines2[i];
			double knownDemand = getKnownDemand(l);
			double[] indicators = getIndicators(l);
			double estimate = getEstimate(averageTotalGBest, indicators);
			System.out.println(knownDemand + "\t" + estimate);
		}
	}

	public double getEstimate(double[] average, double[] indicators) {
		double estimate = average[0];

		for (int i = 1; i < average.length; i++) {
			estimate += (average[i] * indicators[i - 1]);
		}
		return estimate;
	}

	public double getKnownDemand(String l) {
		String[] a = l.split(",");
		double x = Double.parseDouble(a[0]);
		return x;
	}

	public double[] getIndicators(String l) {
		String[] a = l.split(",");
		double[] indicators = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			indicators[i] = Double.parseDouble(a[0]);
		}

		return indicators;
	}

	private String[] getLinesFromTestCSV() {
		String f = "C:\\Users\\ismah\\OneDrive\\Documents\\Ismah's world\\Github\\CS3CI_Cwk_2022\\CS3CI_Cwk_2022\\data\\test.csv";
		String[] values = null;

		try {
			var lines = Files.readAllLines(Paths.get(f));
			values = lines.toArray(new String[0]);

		} catch (IOException i) {
			i.printStackTrace();
		}

		return values;
	}

	private String[] getLinesFromTrainCSV() {

		String f = "C:\\Users\\ismah\\OneDrive\\Documents\\Ismah's world\\Github\\CS3CI_Cwk_2022\\CS3CI_Cwk_2022\\data\\train.csv";
		String[] values = null;

		try {
			var lines = Files.readAllLines(Paths.get(f));
			values = lines.toArray(new String[0]);

		} catch (IOException i) {
			i.printStackTrace();
		}

		return values;

	}

}
