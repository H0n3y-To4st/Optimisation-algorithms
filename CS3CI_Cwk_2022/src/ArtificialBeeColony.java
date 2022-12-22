import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.Spliterator.OfPrimitive;

public class ArtificialBeeColony {

	/*
	 * Problem - Minmise = Actual - Estimate Where Estimate = a0 + a1 * I1 + a2 * I2
	 * ... a13 * I13
	 * 
	 * fx = estimate fitness = actual - fx;
	 */
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		double[][] limits = { { rangeLowForAlphaZero, rangeHighForAlphaZero }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha },
				{ rangeLowForAlpha, rangeHighForAlpha }, { rangeLowForAlpha, rangeHighForAlpha } };

		int iterations = 1000;
		
		ArtificialBeeColony hive = new ArtificialBeeColony(100, 13, limits); //original colony size is 100

		String[] lines = getLinesFromTrainCSV();
		String[] lines2 = getLinesFromTestCSV();
		
		double avgTrainingError = 0;
		double avgTestingError = 0;

		// for training
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			double[] indicators = getIndicators(l);
			double knownDemand = getKnownDemand(l);
			double[] weightsForCurrentDataset = hive.findSolution(iterations, knownDemand, indicators);
			double estimate = generateEstimate(weightsForCurrentDataset, indicators);
			avgTrainingError += Math.abs(estimate - knownDemand);
			
			System.out.println("Training: " + knownDemand + ", " + estimate);
		}
		
		avgTrainingError /= lines.length;
		
		//for testing
		for (int i = 0; i < lines.length; i++) {
			String l = lines[i];
			double[] indicators = getIndicators(l);
			double knownDemand = getKnownDemand(l);
			double[] weightsForCurrentDataset = hive.findSolution(iterations, knownDemand, indicators);
			double estimate = generateEstimate(weightsForCurrentDataset, indicators);
			avgTestingError += Math.abs(estimate - knownDemand);
			
			System.out.println("Testing: " + knownDemand + ", " + estimate);
		}
		
		avgTestingError /= lines2.length;
		
		System.out.println("The average training error calculated: " + avgTrainingError);
		System.out.println("The average testing error calculated: " + avgTestingError);
		
	}

	private static double rangeLowForAlphaZero = 0;
	private static double rangeHighForAlphaZero = 100;
	private static double rangeLowForAlpha = 0;
	private static double rangeHighForAlpha = 5.12;

	private static int No_Better_Solution_Found_Max_Round = 10;

	private static int dimensions = 13;
	int colonySize;
	int numberOfCountersBeforeScouting;
	int startingFoodSources = 200;
	Random random = new Random();
	double[][] limits;

	private Bee[] employed;
	private Bee[] onlookers;

	public ArtificialBeeColony(int sizeOfColony, int noOfDimensions, double[][] limits) {
		colonySize = sizeOfColony;
		dimensions = noOfDimensions;
		this.limits = limits;
	}

	public double[][] initlisationPhase() {
		// TODO : Init
		/*
		 * Normally we would initlaise the problem space However, since this is
		 * unconstrained problem - we cannot initlise a problem space
		 * 
		 * http://www.scholarpedia.org/article/Artificial_bee_colony_algorithm#Eq-1 This
		 * problem is also known as a constrained optimization problem. If it is an
		 * unconstrained optimization problem, then both p=0 and q=0 .
		 */

		numberOfCountersBeforeScouting = (colonySize * dimensions) / 2;
		startingFoodSources = colonySize / 2;

		employed = new Bee[colonySize / 2];
		onlookers = new Bee[colonySize - employed.length];

		// Ignoring the above and using contrained problem space
		double[][] foodSources = initFoodSources();

		for (int i = 0; i < employed.length; i++) {
			int[] pos = new int[2];
			pos[0] = 1;
			pos[1] = 1;
			Bee bee = new Bee(foodSources[i], pos);
			employed[i] = bee;
		}

		for (int i = 0; i < onlookers.length; i++) {
			Bee bee = new Bee(null, null);
			onlookers[i] = bee;
		}

		return foodSources;
	}

	private double[][] initFoodSources() {

		// Generate problemSpaceSize data points between
		double[][] problemSpace = new double[startingFoodSources][dimensions];
		for (int i = 0; i < startingFoodSources; i++) {
			double[] foodSource = generateFoodSource(); // randomly generate food sources in search space
			problemSpace[i] = foodSource;
		}

		return problemSpace;
	}

	private double[] generateFoodSource() {
		double[] foodSource = new double[dimensions];

		foodSource[0] = random.nextDouble(rangeLowForAlphaZero, rangeHighForAlphaZero);

		for (int d = 1; d < dimensions; d++) {
			// generates a random position low >= n, n < high;
			double val = random.nextDouble(rangeLowForAlpha, rangeHighForAlpha);
			foodSource[d] = val;
		}

		return foodSource;
	}

	// find solution until iterate function
	public double[] findSolution(int iterations, double knownActual, double[] indicators) {

		int count = 0;

		double bestFitness = Double.MIN_VALUE;
		double bestEstimate = Double.MAX_VALUE;
		double[] bestSoFar = null;

		double[][] foodSources = initlisationPhase();

		do {
			employedBeePhase(knownActual, indicators, foodSources);
			onlookerBeePhase(knownActual, indicators, foodSources);
			scoutBeePhase(knownActual, indicators, foodSources);

			// remember the current best
			for (int i = 0; i < foodSources.length; i++) {
				double estimate = generateEstimate(foodSources[i], indicators);
				double fitness = calculateFitness(estimate, knownActual);
				if (fitness > bestFitness) {
					bestFitness = fitness;
					bestEstimate = estimate;
					bestSoFar = foodSources[i].clone();
				}
			}

			count++;

		} while (count < iterations);

		//System.out.println("Best Fitness: " + bestFitness + " | " + "Best Estimate: " + bestEstimate + " | " + "Best so far: " + Arrays.toString(bestSoFar));

//		System.out.println("Results: " + knownActual + "," + bestEstimate);
		return bestSoFar;
	}

	// bee phase 1
	private void employedBeePhase(double knownActual, double[] indicators, double[][] problemSpace) {
		for (Bee bee : employed) {
			int i = bee.pos[0];
			int k = getValueInNeighbourhood(i);
			int j = getValueInDimension();

			// xmi is value at current index
			double xmi = problemSpace[i][j];
			// xki is vlaue at proposed index
			double xki = problemSpace[k][j];

			// Calculate the amount of nectar at the new position
			double newNectar = calculateNewNectarSource(xmi, xki);
			double[] weights = bee.weights;
			weights[j] = newNectar;

			// Generate estimate based on new nectar source
			double estimate = generateEstimate(weights, indicators);
			// Generate a fitness criteria to assess the new nectar source against the
			// current
			double fitness = calculateFitness(estimate, knownActual);

			if (fitness > bee.fitness) {
				// Found better nectar source
				bee.fitness = fitness;
				bee.estimate = estimate;
				bee.weights = weights;
				bee.counter = 0;
			} else {
				bee.counter++;
			}
		}
	}

	private double calculateFitness(double estimate, double knownActual) {
		double fx = knownActual - estimate;
		fx = Math.abs(fx);
		double fitness;
		// For ABC
		// To calculate fitness we use
		// fitm(xm→)=⎧⎩⎨⎪⎪11+fm(xm→)1+abs(fm(xm→))if fm(xm→)≥0if fm(xm→)<0⎫⎭⎬⎪⎪(7)
		//
		// if fx >= 0 : Fitm = 1 / (1 + fx)
		// if fx < 0 : Fitm = 1 + abs(fx);
		// See -
		// http://www.scholarpedia.org/article/Artificial_bee_colony_algorithm#Eq-7
		if (fx >= 0) {
			fitness = 1 / (1 + fx);
		} else {
			fitness = 1 + Math.abs(fx);
		}

		return fitness;
	}

	// estimated demand
	private static double generateEstimate(double[] weights, double[] indicators) {
		double estimate = weights[0];
		for (int i = 1; i < weights.length; i++) {
			estimate += weights[i] * indicators[i - 1];
		}
		return estimate;
	}

	private static double getKnownDemand(String l) {
		String[] a = l.split(",");
		double x = Double.parseDouble(a[0]);
		return x;
	}

	private static double[] getIndicators(String l) {
		String[] a = l.split(",");
		double[] indicators = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			indicators[i] = Double.parseDouble(a[0]);
		}

		return indicators;
	}

	private static String[] getLinesFromTestCSV() {
		String f = "CS3CI_Cwk_2022\\data\\test.csv";
		String[] values = null;

		try {
			var lines = Files.readAllLines(Paths.get(f));
			values = lines.toArray(new String[0]);

		} catch (IOException i) {
			i.printStackTrace();
		}

		return values;
	}

	private static String[] getLinesFromTrainCSV() {
		String f = "CS3CI_Cwk_2022\\data\\train.csv";
		String[] values = null;

		try {
			var lines = Files.readAllLines(Paths.get(f));
			values = lines.toArray(new String[0]);

		} catch (IOException i) {
			i.printStackTrace();
		}

		return values;
	}

	private double calculateNewNectarSource(double xmi, double xki) {

		// Phi is a value between -a to a
		// NextDouble does not generate 1 - TODO : Fix?
		double ϕmi = random.nextDouble(-1, 1);

		// // xmi is value at current index
		// double xmi = problemSpace[i][j];
		// // xki is value at proposed index
		// double xki = problemSpace[i][k];

		// υmi=xmi+ϕmi(xmi−xki)
		double vmi = xmi + (ϕmi * (xmi - xki));
		return vmi;
	}

	// bee phase 2
	private void onlookerBeePhase(double knownActual, double[] indicators, double[][] foodsources) {
		double[] probabilities = new double[startingFoodSources];
		double total = 0;
		for (Bee bee : employed) {
			total += bee.fitness;
		}

		for (int i = 0; i < startingFoodSources; i++) {
			probabilities[i] = employed[i].fitness / total;
		}

		for (Bee bee : onlookers) {
			double probability = random.nextDouble();
			int i = -1;
			for (double d : probabilities) {
				i++;
				if (d <= probability)
					break;
			}

			int k = getValueInNeighbourhood(i);
			int j = getValueInDimension();

			// xmi is value at current index
			double xmi = foodsources[i][j];
			// xki is vlaue at proposed index
			double xki = foodsources[k][j];

			// Calculate the amount of nectar at the new position
			double newNectar = calculateNewNectarSource(xmi, xki);
			double[] weights = foodsources[i];
			weights[j] = newNectar;

			// Generate estimate based on new nectar source
			double estimate = generateEstimate(weights, indicators);
			// Generate a fitness criteria to assess the new nectar source against the
			// current
			double fitness = calculateFitness(estimate, knownActual);

			if (fitness > bee.fitness) {
				// Found better nectar source
				foodsources[i][j] = newNectar;
				bee.counter = 0;
			} else {
				bee.counter++;
			}
		}
	}

	// bee phase 3
	private void scoutBeePhase(double knownActual, double[] indicators, double[][] foodSources) {
		for (int i = 0; i < employed.length; i++) {
			Bee bee = employed[i];
			if (bee.counter > numberOfCountersBeforeScouting) {
				// Counter reached, abandon current location and scout for a new
				foodSources[i] = generateFoodSource();
			}
		}
	}

	private int getValueInNeighbourhood(int i) {
		// How far from origin is considered a neighbour
		int neighbourhoodDistance = 5;

		// Generate a value in the range of -neighbourhoodDistance <= k, k <=
		// neighbourhoodDistance
		// within the neighbourhood of the current dimension and offset from j
		int k = random.nextInt(neighbourhoodDistance * -1, neighbourhoodDistance + 1);
		int val = i + k;

		// new index must remain within the problem space
		// if not, try again until we can find one
		if (val >= startingFoodSources || val < 0)
			val = getValueInNeighbourhood(i);

		return val;
	}

	private int getValueInDimension() {
		// return up to dimensions, because dimensions is zero indexed array
		return random.nextInt(dimensions);
	}

	private class Bee {
		public double[] weights;
		public int[] pos;
		public double fitness;
		public double estimate;
		public int counter;

		public Bee(double[] weights, int[] pos) {
			this.weights = weights;
			this.pos = pos;
		}
	}
}