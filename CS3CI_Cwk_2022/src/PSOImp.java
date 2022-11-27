
public class PSOImp {
	
	DemandPrediction dp; //non static
	Particle[] particles;
	
	int dimensions = DemandPrediction.N_DEMAND_INDICATORS; //TODO
	int particlesNum = DemandPrediction.N_DEMAND_INDICATORS; //accessed statically
	int iterations = 1000;
	
	//constants - with placeholder values
	double cognitive = 1.1193; //how pbest affects particle movement
	double inertia = 0.725; //control impact of velocity
	double social = 1.1193; //how gbest affects particle movement
	
	double[][] gBest = new double[dimensions][dimensions];
	double[][] best; //best candidate solution
	
	//uniform random vectors - random every generation
	//help exploration
	double r1 = Math.random();
	double r2 = Math.random();
	
	public static void main(String[] args) {
		
	}
	
	public PSOImp() {
		particles = new Particle[particlesNum];
		PSOCal PSO = new PSOCal(dimensions, particlesNum,iterations, cognitive, social, inertia);
		
		//initialise them first
		PSO.population(particles);
		
		int iter = 0;
		while(iter < iterations) {
			
			//update velocity and position
			//evaluate new position
			
			for(int i = 0; i < particlesNum; i++) {
				PSO.getBest(particles[i]);
			}
			
			//update personal best position
			iter++;
		}
		
		//find best candidate solution
	
	}
	
}
