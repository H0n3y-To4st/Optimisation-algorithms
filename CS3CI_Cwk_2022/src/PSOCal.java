import java.io.BufferedReader;
import java.io.FileNotFoundException;
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

	public static final double MAX_VELOCITY = 4.0;
	Particle[] particles; // collection of particles -> swarm

	int dimensions = DemandPrediction.N_PARAMETERS; // includes bias and indicators
	int particlesNum;
	int maxIter;

	// constants - with placeholder values, find online source to find better values
	double cognitive; // how pbest affects particle movement
	double inertia; // control impact of velocity
	double social; // how gbest affects particle movement

	double[] gBest = new double[dimensions];
	double gBestFitness = Double.MAX_VALUE;

	double gBias = 100; // 0-100

	double knownDemand;
	double[] indicators;

	public PSOCal(int dime, int pnum, int iterations, double knownDemand, double[] indicators, double c, double s, double i) {
		dimensions = dime;
		particlesNum = pnum;
		maxIter = iterations;
		cognitive = c;
		social = s;
		inertia = i;
		this.knownDemand = knownDemand;
		this.indicators = indicators;
	}

	public void initialise(){

		// generate particles
		initPopulation();

	}

	public double[] releaseTheSwarm() {
		int iterations = maxIter;
		while (iterations > 0) {

			// fitness value
			calculateFitnessForCurrentIteration();
			
			double r1 = Math.random();
			double r2 = Math.random();
			
			for(int i = 0; i < particles.length; i++) {
				
				updateVelocity(particles[i], r1, r2);
				updatePos(particles[i]);
			}
			iterations--;
		}
		System.out.println("Training: " + knownDemand + ", " + gBestFitness);
		return gBest;
	}

	// fitness function = demand prediction equation, evaluate on one linerow
	// demand prediction = gBias (+ bias*indicator...)
	private double evaluateFitness(double[] pos, double[] indicators) {
		double fitness = pos[0];

		for (int i = 1; i < pos.length; i++) {
			fitness += (pos[i] * indicators[i - 1]);
		}
		return fitness;
	}

	private void calculateFitnessForCurrentIteration() {
		for (int i = 0; i < particles.length; i++) {
			double fitness = evaluateFitness(particles[i].pos, indicators);
			double pBest = Math.abs(particles[i].fitness - knownDemand);
			double currentFitness = Math.abs(fitness - knownDemand);
			double gBestFitnessAbsolute = Math.abs(gBestFitness - knownDemand);
					
			if (pBest < currentFitness) {
				particles[i].pbest = particles[i].pos;
				particles[i].fitness = fitness;
			}
			if (currentFitness < gBestFitnessAbsolute) {
				gBestFitness = fitness;
				gBest = particles[i].pos.clone();
			}
		}
	}

	// initialise phase + search space
	void initPopulation() {
		particles = new Particle[particlesNum];
		
		double[] innerBounds = new double[21];

		for (int idx = 0; idx < innerBounds.length; idx++) {
			for (int x = -100; x <= 100; x += 10) {
				innerBounds[idx] = x;
			}
		}

		for (int i = 0; i < particlesNum; i++) {
			
			double[] pos = new double[dimensions];
			double[] vel = new double[dimensions];
			
			// Reworked to allow gbias to have a range of -n to n
			// With other values having a range of -n <= x < n
			double initialBounds = 5.12; // 5.12 Value derived from Rastign function -- TODO : Review suitability
			pos[0] = Math.random() * (gBias+1) - gBias; // gBias+1 to allow for gBias value itself (as random does not allow 1.0
			for(int dime = 1; dime < dimensions; dime++) {
				
				// TODO : Check the maths - (a*(b*2))-b should give a range of -b to b
				double x = (Math.random() * (initialBounds*2)) - initialBounds;
				pos[dime] = x;
				
			}

			// Particle(double p, double v, double pb)
			particles[i] = new Particle(pos, vel);
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
	public void updateVelocity(Particle p, double r1, double r2) {

		double[] in = new double[dimensions];
		double[] cog = new double[dimensions];
		double[] soc = new double[dimensions];

		for (int i = 0; i < dimensions; i++) {

			// ----cognitive value
			cog[i] = cognitive * r1 * (p.pbest[i] - p.pos[i]);

			// ----social value
			soc[i] = social * r2 * (gBest[i] - p.pos[i]);

			// ----Inertia value 
			in[i] = inertia * p.vel[i];

			// ----update using calculated values
			double velocity = in[i] + cog[i] + soc[i]; // new velocity in current dimension
			
			p.vel[i] = velocity;
		}
	}
}
