import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Random;

public class ArtificialBeeColony {

    // http://www.scholarpedia.org/article/Artificial_bee_colony_algorithm#Eq-1
    // https://en.wikipedia.org/wiki/Artificial_bee_colony_algorithm
    // https://towardsdatascience.com/implementing-artificial-bee-colony-algorithm-to-solve-business-problems-cb754f3b9255
    // https://www.sciencedirect.com/topics/computer-science/artificial-bee-colony-algorithm
    // https://analyticsindiamag.com/artificial-bee-colony-and-its-applications-to-optimization-problems/
    // https://abc.erciyes.edu.tr
    // https://abc.erciyes.edu.tr/pub/Step%20by%20Step%20Procedure%20of%20ABC.pdf

    /*
     * Problem - Minmise = Actual - Estimate
     * Where Estimate = a0 + a1 * I1 + a2 * I2 ... a13 * I13
     * 
     * fx = estimate
     * fitness = actual - fx;
     */
    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("asd");
    }

    private static int No_Better_Solution_Found_Max_Round = 10;

    private int dimensions = 13;
    int problemSpaceSize = 200;
    double problemSpaceLow = -5.12;
    double problemSpaceHigh = 5.12;
    Random random = new Random();

    private ArrayList<Bee> employed = new ArrayList<Bee>();
    private ArrayList<Bee> onlookers = new ArrayList<Bee>();
    private ArrayList<Bee> scouts = new ArrayList<>();

    public ArtificialBeeColony() {

    }

    public double[][] initlisationPhase() {
        // TODO : Init
        /*
         * Normally we would initialise the problem space
         * However, since this is unconstrained problem - we cannot initialise a problem
         * space
         * 
         * http://www.scholarpedia.org/article/Artificial_bee_colony_algorithm#Eq-1
         * This problem is also known as a constrained optimization problem. If it is an
         * unconstrained optimization problem, then both p=0 and q=0 .
         */

        // Ignoring the above and using contrained problem space
        return initConstrained();
    }

    private double[][] initConstrained() {

        double[][] limits = {
                { -100, 100 },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh },
                { problemSpaceLow, problemSpaceHigh }
        };

        // Generate 200 data points between
        double[][] problemSpace = new double[dimensions][problemSpaceSize];
        for (int d = 0; d < dimensions; d++) {
            double low = limits[d][0];
            double high = limits[d][1];
            for (int p = 0; p < problemSpaceSize; p++) {
                // generates a random position low < n < high;
                double rndvalue = random.nextDouble(low, high);

                problemSpace[p][d] = rndvalue;
            }
        }

        return problemSpace;
    }

    public void findSolution(int iterations, double knownActual, double[] indicators, double[][] problemSpace) {
        int count = 0;
        int numOfPhasesWhereNoBetterNecaterSourceWasFound = 0;
        do {
            employedBeePhase(knownActual, indicators, problemSpace);
            onlookerBeePhase();
            scoutBeePhase();

            boolean betterNectarSourceFound = false;

            if (!betterNectarSourceFound)
                numOfPhasesWhereNoBetterNecaterSourceWasFound++;
            else
                numOfPhasesWhereNoBetterNecaterSourceWasFound = 0;

        } while (count < iterations);
    }

    private void employedBeePhase(double knownActual, double[] indicators, double[][] problemSpace) {
        for (Bee bee : employed) {
            int i = bee.pos[0];
            int k = getValueInNeighbourhood(i);
            int j = getValueInDimension();

            // xmi is value at current index
            double xmi = problemSpace[i][j];
            // xki is vlaue at proposed index
            double xki = problemSpace[i][k];

            // Calculate the amount of nectar at the new position
            double newNectar = calculateNewNectarSource(xmi, xki);
            double[] weights = bee.weights;
            weights[k] = newNectar;

            // Generate estimate based on new nectar source
            double estimate = generateEstimate(weights, indicators);
            // Generate a fitness criteria to assess the new nectar source against the current
            double fitness = calculateFitness(estimate, knownActual);

            if (fitness > bee.fitness) {
                // Found better nectar source
                bee.fitness = fitness;
                bee.estimate = estimate;
                bee.weights = weights;
            }
        }
    }

    private double calculateFitness(double estimate, double knownActual) {
        double fx = knownActual - estimate;
        double fitness;
        // For ABC
        // To cacluate fitness we use
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

    private double generateEstimate(double[] weights, double[] indicators) {
        double estimate = weights[0];
        for (int i = 1; i < weights.length; i++) {
            estimate += weights[i] * indicators[i - 1];
        }
        return estimate;
    }

    private double calculateNewNectarSource(double xmi, double xki) {

        // Phi is a value between -a to a
        // NextDouble does not generate 1 - TODO : Fix?
        double ϕmi = random.nextDouble(-1, 1);

        // // xmi is value at current index
        // double xmi = problemSpace[i][j];
        // // xki is vlaue at proposed index
        // double xki = problemSpace[i][k];

        // υmi=xmi+ϕmi(xmi−xki)
        double vmi = xmi + (ϕmi * (xmi - xki));
        return vmi;
    }

    private void onlookerBeePhase() {
        for (Bee bee : onlookers) {

        }
    }

    private void scoutBeePhase() {
        for (Bee bee : scouts) {

        }
    }

    private int getValueInNeighbourhood(int i) {
        // How far from origin is considered a neighbour
        int neighbourhoodDistance = 5;

        // Generate a value in the range of -neighbourhoodDistance <= k, k <= neighbourhoodDistance
        // within the neighbourhood of the current dimension and offset from j
        int k = random.nextInt(neighbourhoodDistance * -1, neighbourhoodDistance + 1);
        int val = i + k;

        // new index must remain within the problem space
        // if not, try again until we can find one
        if (val > problemSpaceSize || val < 0)
            val = getValueInNeighbourhood(i);

        return val;
    }

    private int getValueInDimension() {
        // return up to dimensions, because dimensions is zero indexed array
        return random.nextInt(dimensions);
    }

    private double fitness(double[] x, double[] indicators, double knownActual) {
        double fx = x[0];

        for (int i = 0; i < dimensions; i++) {
            fx += x[i + 1] * indicators[i];
        }

        double fitness;
        if (fx >= 0)
            fitness = 1 / (1 + fx);
        else
            fitness = 1 + Math.abs(fx);

        return fitness;
    }
}