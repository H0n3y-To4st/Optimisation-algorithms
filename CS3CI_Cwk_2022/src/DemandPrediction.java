import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/** Car price prediction problem */
public class DemandPrediction {
    public static final int N_DEMAND_INDICATORS = 13;
    // Parameters consist of a bias (intercept) for the sum and one weight for
    // each demand indicator.
    public static final int N_PARAMETERS = N_DEMAND_INDICATORS + 1;

    /**
     * Construct a car price prediction problem instance.
     * @param dataset Specifies which dataset to use. Valid values are "train",
     *                "validation" or "test".
     */
    public DemandPrediction(String dataset) throws IOException {
        if(Objects.equals(dataset, "train")) load_dataset("C:\\Users\\ismah\\OneDrive\\Documents\\Ismah's world\\Github\\CS3CI_Cwk_2022\\CS3CI_Cwk_2022\\data\\train.csv");
        else if(Objects.equals(dataset, "test")) load_dataset("C:\\Users\\ismah\\OneDrive\\Documents\\Ismah's world\\Github\\CS3CI_Cwk_2022\\CS3CI_Cwk_2022\\data\\test.csv");
        else throw new IllegalArgumentException("Only permitted arguments for " +
                    "CarPricePrediction::CarPricePrediction are train and test.");
    }
    /**
     * Rectangular bounds on the search space.
     * @return Vector b such that b[i][0] is the minimum permissible value of the
     * ith solution component and b[i][1] is the maximum.
     */
    public static double[][] bounds() {
        double[][] bnds = new double[N_PARAMETERS][2];
        double[] dim_bnd = {-100.0,100.0}; //bounds on search space
        for(int i = 0;i<N_PARAMETERS;++i)
            bnds[i] = dim_bnd;
        return bnds;
    }

    /**
     * Check whether the function parameters (weights) lie within the
     * problem's feasible region.
     * There should be the correct number of weights for the predictor function.
     * Each weight should lie within the range specified by the bounds.
     */
    public boolean is_valid(double[] parameters) {
        if(parameters.length != N_PARAMETERS) return false;
        //All weights lie within the bounds.
        double[][] b = bounds();
        for(int i = 0; i<N_PARAMETERS; i++)
            if(parameters[i] < b[i][0] || parameters[i] > b[i][1] )
                return false;
        return true;
    }

    /**
     * Evaluate a set of ANN parameters on the dataset used by the class
     * instance (train/validation/test).
     * @param parameters An array of size the bias/intercept and the weights
     *                   to be used to predict demand.
     * @return The mean absolute error of the predictions on the selected
     * dataset.
     */
    public double evaluate(double[] parameters) {
        double abs_error = 0.0;
        for(int i = 0; i < X.size(); i++){
            double y_pred = predict(X.get(i),parameters);
            abs_error += Math.abs(y.get(i)-y_pred);
        }
        abs_error /= X.size();
        return abs_error;
    }

    private List<double[]> X;
    private List<Double> y;

    private void load_dataset(String file) throws IOException {
        X = new ArrayList<>();
        y = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] line_data = line.split(",");
            if(line_data.length != N_DEMAND_INDICATORS + 1) {
                throw new RuntimeException("in CarpPricePrediction::load_dataset, " +
                        "a line in the dataset contained the wrong number of" +
                        "entries.");
            }
            double[] x = new double[N_DEMAND_INDICATORS];
            for(int i = 1; i <= N_DEMAND_INDICATORS; i++){ 
            	x[i-1] = Double.parseDouble(line_data[i]); 
            	}
            X.add(x);
            y.add(Double.parseDouble(line_data[0]));
        }
    }

    /*
     * Predicts demand based on a weighted sum of demand indicators. You may
     * replace this with something more complex, but will likely have to change
     * the form of the parameters array as well.
     */
    private static double predict(double[] demand_indicators, double[] parameters){
        double prediction = parameters[0];
        
        for (int i = 1; i < N_PARAMETERS ; i++) {
            prediction += demand_indicators[i - 1] * parameters[i];
        }

//        System.out.println(Arrays.toString(demand_indicators));
//        System.out.println(Arrays.toString(parameters));
//        System.out.println(prediction);
        return prediction;
    }
}
