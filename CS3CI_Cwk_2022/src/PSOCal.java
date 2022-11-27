import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class PSOCal {
	// -----calculations for swarm

	// initialisation phase
	// repeat until termination condition is satisfied
	// for each particle in population:
	// update velocity and position
	// evaluate new position
	// update personal best
	// end

	Particle[] particles; // collection of particles -> swarm

	int dimensions = DemandPrediction.N_PARAMETERS; // includes bias and indicators
	int particlesNum;

	int maxIter;

	// constants - with placeholder values, find online source to find better values
	double cognitive; // how pbest affects particle movement
	double inertia; // control impact of velocity
	double social; // how gbest affects particle movement

	double[] gBest = new double[dimensions];

	double gBias = Math.random() * 101; // 0-100

	public PSOCal(int dime, int pnum, int iterations, double c, double s, double i) {
		dimensions = dime;
		particlesNum = pnum;
		maxIter = iterations;
		cognitive = c;
		social = s;
		inertia = i;
	}

	public void initialise() throws IOException {

		// generate particles
		initPopulation(particles);

		// read file
		CSVReader();

		// generate weights
		getWeights();

		// fitness value
		evaluateFitness();

	}

	// makes 13 weights
	public double[] getWeights() {
		double[] weights = new double[DemandPrediction.N_DEMAND_INDICATORS];

		for (int i = 0; i < DemandPrediction.N_DEMAND_INDICATORS; i++) {
			weights[i] = Math.random(); // randomly between 0 and 1
		}

		return weights;
	}

	public String[] CSVReader() throws IOException {
		String f = "data/test.csv";
		FileReader fr = new FileReader(f);
		BufferedReader br = new BufferedReader(fr);
		String l;
		String[] values = null;

		while ((l = br.readLine()) != null) {
			values = l.split(",");
//			demand[0] = Double.parseDouble(values[0]); //known demand of each day
		}

		return values;
	}

	// fitness function = demand prediction equation, evaluate on one linerow
	// demand prediction = gBias (+ bias*indicator...)
	public double evaluateFitness() throws IOException {
		double fitness = gBias;
		String[] values = CSVReader();
		double[] bias = getWeights();
//		double[] indicator;

		for (int i = 0; i < values.length; i++) {
			fitness += (bias[i] * Double.parseDouble(values[i + 1]));
		}
		return fitness;
	}

	// initialise phase + search space
	void initPopulation(Particle[] p) {
		double[] innerBounds = new double[21];

		for (int idx = 0; idx < innerBounds.length; idx++) {
			for (int x = -100; x <= 100; x += 10) {
				innerBounds[idx] = x;
			}
		}

		for (int i = 0; i < p.length; i++) {
			double[] pos;
			double[] vel;

			for (int dime = 0; dime < dimensions; dime++) {

				pos[dime] = ((Math.random() * ((100.0 - (-100.0)))) - 100.0); // random point in space
				vel[dime] = ((Math.random() * ((100.0 - (-100.0)))) - 100.0) - (pos[dime]) / 2; // random point -
																								// current position / 2

			}

			// Particle(double p, double v, double pb)
			p[i] = new Particle(pos[i], vel[i]);
		}
	}

	// call this class AFTER updateVelocity
	// because velocity must be updated for particle to use it and
	// change its position
	void updatePos(Particle p) {
		// update new position after calculating velocity
		// updatedpos = currentpos + updated velocity

		for (int i = 0; i < dimensions; i++) {
			p.pos[i] = p.pos[i] + p.vel[i];
		}
	}

	// update particles velocity
	// call this BEFORE updatePos
	// is best neighbour needed?
	public void updateVelocity(Particle p, double[] r1, double[] r2) {

		// p

		double[] velocities = new double[dimensions];
		double[] pBest = new double[dimensions];
		double[] positions = new double[dimensions];
		// best neighbour?

		double[] in = new double[dimensions];
		double[] cog = new double[dimensions];
		double[] soc = new double[dimensions];

		// inertia values
		for (int i = 0; i < dimensions; i++) {
			for (int j = 0; j < dimensions; j++) {
				in[i] = inertia * velocities[i];
			}
		}

		// ----cognitive values - break into 3 steps
		// a = difference between positions
		double[] a = new double[dimensions];
		double[] cxr = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			// pbest - current position

			a[i] = (pBest[i] - positions[i]);

		}

		for (int i = 0; i < dimensions; i++) {
			// cog * r1
			cxr[i] = cognitive * r1[i];
		}

		for (int i = 0; i < dimensions; i++) {
			// cog * r1
			cog[i] = cxr[i] * a[i];
		}

		// ----social values - 3 steps
		double[] b = new double[dimensions];
		double[] sxr = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {
			// gbest - pbest
			b[i] = gBest[i] - positions[i];
		}

		for (int i = 0; i < dimensions; i++) {
			sxr[i] = social * r2[i];
		}

		for (int i = 0; i < dimensions; i++) {
			soc[i] = sxr[i] * b[i];
		}

		// ----update using calculated values
		for (int i = 0; i < dimensions; i++) {
			p.vel = in[i] + cog[i] + soc[i]; // TODO
		}
	}

	// problem statement
	// best weight training - use on test file
	// initialisation - -100,100 search space
	// a0 = global bias (gb), initialise function, a0 any number between 0-100,
	// parameter
	// a1 = indicator 1.... initialise all indicators with random values at once(?)
	// fitness function = demand prediction equation, evaluate on one linerow
	// find best weight for current line which will be closest to actual demand
	// repeat for all rows
	// once pso is done, iterate and compare weights of all processed lines
	// overall weights - work out later
	// somehow get best weight
	// test will use best weights for overall result
	// 3 parameter - PSO parameters (cog, social, ine)
	// trial and error with parameter values

	/*
	 * TODO list: -particle object: 1.single array with customisable length- number
	 * of alphas 2.velocity 3.position - current 4.pbest -swarm - repeat swarm for
	 * every row - should be done partly in PSOImp 1.gbest 2.update velocity
	 * 3.update pos 4.fitness function 5.number of iterations - 1000 6.csv reader
	 * -overall PSO overall weights generate data from test file - predicted demand
	 * per row from test file using overall weights one initialisation function one
	 * fitness function
	 */

	/*
	 * ---progress list: did fitness function somewhat started initialisation
	 * function changed particle to single array edited this class due to single
	 * array change made gbias made weights method made csv reader working on fixing
	 * update velocity and position methods
	 */

}
