
public class Particle {
	
	public double[] pos; //position
	public double[] vel; //velocity
	public double[] pbest; //personal best
	public double fitness = Double.MAX_VALUE; //value used to determine accuracy
	
	public Particle(double[] p, double[] v) {
		pos = p;
		vel = v;
		pbest = pos;
	}
}
