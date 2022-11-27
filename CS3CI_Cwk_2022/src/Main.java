import java.io.IOException;
import java.util.Random;

/* A simple example of how the DemandPrediction class could be used as part
 * of a random search. It is not expected that you use this code as part of
 * your solution - it is just a demonstration of how the class's methods can be
 * called and how we can use two versions of the problem (here train and
 * test) to, respectively, obtain a promising set of parameters (using
 * train) and then to measure their performance (using test).
 */
public class Main {
    public static void main(String[] args) throws IOException {
        var training_problem = new DemandPrediction("train");
        var bounds = DemandPrediction.bounds();
        var r = new Random();

        /* Generate N_TRIES random parameters and measure their MSE on the test
         * problem, saving the best parameters.
         */
        int N_TRIES = 100;
        double[] best_parameters = random_parameters(bounds,r);
        double best_training_error = training_problem.evaluate(best_parameters);
        for (int i = 0; i < N_TRIES - 1; i++) {
            var parameters = random_parameters(bounds,r);
            var training_error = training_problem.evaluate(parameters);
            if(training_error < best_training_error){
                best_training_error = training_error;
                best_parameters = parameters;
            }
        }
        System.out.printf("Best training error after %d " +
                "iterations: %f%n", N_TRIES, best_training_error);

        // Check the MSE of the best parameters on the test problem.
        var test_problem  = new DemandPrediction("test");
        var test_error = test_problem.evaluate(best_parameters);
        System.out.printf("Test error of best solution " +
                "found while training: %f%n", test_error);
    }

    public static double[] random_parameters(double[][] bounds, Random r){
        var parameters = new double[bounds.length];
        for (int j = 0; j < bounds.length; j++) {
            parameters[j] = bounds[j][0] + r.nextDouble() * (bounds[j][1] - bounds[j][0]);
        }
        return parameters;
    }
}
