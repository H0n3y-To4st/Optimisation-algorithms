
public class Particle {
	
	public double[] pos; //position
	public double[] vel; //velocity
	public double[] pbest; //personal best
	
	public Particle(double[] p, double[] v) {
		pos = p;
		vel = v;
		pbest = pos;
	}
	
//	public double[] getParticle() {
//		double[] particle = new double[4];
//		
//		particle[0] = pos;
//		particle[1]	= vel;
//		particle[2] = pbest;
//		particle[3] = alphas;
//				
//		return particle;
//	}
}
