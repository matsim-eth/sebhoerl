package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim;

import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.discrete.solver.DiscreteChainSolver;
import ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.sampling.ChainSamplerResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.vividsolutions.jts.util.Debug;

import java.io.IOException;

public class DebugInformation {
    public long indexInChain;
    public long lengthOfChain;
    public boolean isDistanceChainSampledConverged;
    public long numberOfSamples;
    public long numberOfSolverIterations;
    public boolean isDiscreteChainSolverConverged;
    public long discreteIterations;
    public double discretizationError;

    public double referenceDistance;
    public double discretizedDistance;

    private DebugInformation() {} // for Jackson

    public DebugInformation(int indexInChain, long lengthOfChain, ChainSamplerResult samplerResult, DiscreteChainSolver.Result discreteResult) {
        this.indexInChain = indexInChain;
        this.lengthOfChain = lengthOfChain;

        referenceDistance = samplerResult.getContinuousSolverResult().getDistances().get(indexInChain);
        discretizedDistance = discreteResult.getDiscreteDistances().get(indexInChain);

        isDistanceChainSampledConverged = samplerResult.isConverged();
        numberOfSamples = samplerResult.getNumberOfSamples();
        numberOfSolverIterations = samplerResult.getNumberOfSolverIterations();

        isDiscreteChainSolverConverged = discreteResult.isConverged();
        discreteIterations = discreteResult.getNumberOfIterations();
        discretizationError = discretizedDistance - referenceDistance;
    }

    public static String write(DebugInformation debugInformation) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return objectMapper.writeValueAsString(debugInformation);
        } catch (JsonProcessingException e) {
            return e.toString();
        }
    }

    public static DebugInformation read(String debugInformation) {
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            return objectMapper.readValue(debugInformation, DebugInformation.class);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
}
