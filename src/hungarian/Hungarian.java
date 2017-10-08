/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hungarian;

import java.util.Arrays;
/**
 *
 * @author Eranga
 */
public class Hungarian {
    
    int dimension;
    int numberOfRows ;
    int numberOfColumns ;
    double[][] matrix;
    double[] minimumValAsARow;
    int[] jobRelativeToWoker;
    int[] workerRelativeToJob;
    int[] labelByWorker;
    boolean[] committedWorkers;
    int[] parentWorkerByCommittedJob;
    double[] minSlackValueByJob;
    int[] minSlackWorkerByJob;
    
    public int[] getJobRelativeToWorker(double[][] matrix){
        
        dimension = matrix.length;
        numberOfRows = matrix.length;
        numberOfColumns = matrix[0].length;
        this.matrix = matrix;
        minimumValAsARow = new double[dimension];
        jobRelativeToWoker = new int[dimension];
        workerRelativeToJob = new int[dimension];
        labelByWorker = new int[dimension];
        minSlackWorkerByJob = new int[dimension];
        minSlackValueByJob = new double[dimension];
        parentWorkerByCommittedJob = new int[dimension];
        committedWorkers = new boolean[dimension];;
        Arrays.fill(jobRelativeToWoker, -1);
        Arrays.fill(workerRelativeToJob, -1);
        reduce();
        computeInitialFeasibleSolution();
        greedyMatch();
        int w = fetchUnmatchedWorker();
        while (w < dimension)
        {
            initializePhase(w);
            executePhase();
            w = fetchUnmatchedWorker();
        }
        int[] result = Arrays.copyOf(jobRelativeToWoker, numberOfColumns);
        return result;
        
    }
    
     protected void computeInitialFeasibleSolution()
    {
        for (int j = 0; j < dimension; j++)
        {
            minimumValAsARow[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dimension; w++)
        {
            for (int j = 0; j < dimension; j++)
            {
                if (matrix[w][j] < minimumValAsARow[j])
                {
                    minimumValAsARow[j] = matrix[w][j];
                }
            }
        }
    }
    
    protected void greedyMatch()
    {
        for (int w = 0; w < dimension; w++)
        {
            for (int j = 0; j < dimension; j++)
            {
                //matching 0 positions
                if (jobRelativeToWoker[w] == -1
                        && workerRelativeToJob[j] == -1
                        && matrix[w][j] - minimumValAsARow[j] == 0)
                {
                    match(w, j);
                }
            }
        }
    }
    
    protected void match(int w, int j)
    {
        jobRelativeToWoker[w] = j;
        workerRelativeToJob[j] = w;
    }
    
    protected void initializePhase(int w)
    {
        Arrays.fill(committedWorkers, false);
        Arrays.fill(parentWorkerByCommittedJob, -1);
        committedWorkers[w] = true;
        for (int j = 0; j < dimension; j++)
        {

            minSlackValueByJob[j] = matrix[w][j] - labelByWorker[w]
                    - minimumValAsARow[j];
            minSlackWorkerByJob[j] = w;
        }
    }
    
    protected void updateLabeling(double slack)
    {
        for (int w = 0; w < dimension; w++)
        {
            if (committedWorkers[w])
            {
                labelByWorker[w] += slack;
            }
        }
        for (int j = 0; j < dimension; j++)
        {
            if (parentWorkerByCommittedJob[j] != -1)
            {
                minimumValAsARow[j] -= slack;
            }
            else
            {
                minSlackValueByJob[j] -= slack;
            }
        }
    }
    protected void executePhase()
    {
        while (true)
        {
            int minSlackWorker = -1, minSlackJob = -1;
            double minSlackValue = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dimension; j++)
            {
                if (parentWorkerByCommittedJob[j] == -1)
                {
                    if (minSlackValueByJob[j] < minSlackValue)
                    {
                        minSlackValue = minSlackValueByJob[j];
                        minSlackWorker = minSlackWorkerByJob[j];
                        minSlackJob = j;
                    }
                }
            }
            if (minSlackValue > 0)
            {
                updateLabeling(minSlackValue);
            }
            parentWorkerByCommittedJob[minSlackJob] = minSlackWorker;
            if (workerRelativeToJob[minSlackJob] == -1)
            {
                /*
                 * An augmenting path has been found.
                 */
                int committedJob = minSlackJob;
                int parentWorker = parentWorkerByCommittedJob[committedJob];
                while (true)
                {
                    int temp = jobRelativeToWoker[parentWorker];
                    match(parentWorker, committedJob);
                    committedJob = temp;
                    if (committedJob == -1)
                    {
                        break;
                    }
                    parentWorker = parentWorkerByCommittedJob[committedJob];
                }
                return;
            }
            else
            {
                /*
                 * Update slack values since we increased the size of the
                 * committed
                 * workers set. worker is present
                 */
                int worker = workerRelativeToJob[minSlackJob];
                committedWorkers[worker] = true;
                for (int j = 0; j < dimension; j++)
                {
                    if (parentWorkerByCommittedJob[j] == -1)
                    {
                        double slack = matrix[worker][j]
                                - labelByWorker[worker] - minimumValAsARow[j];
                        if (minSlackValueByJob[j] > slack)
                        {
                            minSlackValueByJob[j] = slack;
                            minSlackWorkerByJob[j] = worker;
                        }
                    }
                }
            }
        }
    }
    
    protected int fetchUnmatchedWorker()
    {
        int w;
        for (w = 0; w < dimension; w++)
        {
            if (jobRelativeToWoker[w] == -1)
            {
                //index the only not matching
                break;
            }
        }
        return w;
    }
    
   
    protected void reduce()
    {
        for (int w = 0; w < dimension; w++)
        {
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < dimension; j++)
            {
                if (matrix[w][j] < min)
                {
                    min = matrix[w][j];
                }
            }
            for (int j = 0; j < dimension; j++)
            {
                matrix[w][j] -= min;
            }
        }
        double[] min = new double[dimension];
        for (int j = 0; j < dimension; j++)
        {
            min[j] = Double.POSITIVE_INFINITY;
        }
        for (int w = 0; w < dimension; w++)
        {
            for (int j = 0; j < dimension; j++)
            { 
                if (matrix[w][j] < min[j])
                {
                    min[j] = matrix[w][j];
                }
            }
        }
        for (int w = 0; w < dimension; w++)
        {
            for (int j = 0; j < dimension; j++)
            {
                matrix[w][j] -= min[j];
            }
        }
    }
}
