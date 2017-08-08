package ch.ethz.ivt.matsim.playgrounds.sebhoerl.locations.matsim.microcensus;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

public class MicrocensusReader {
    static final private Logger logger = Logger.getLogger(MicrocensusReader.class);

    final private Microcensus microcensus;
    final private Map<String, Microcensus.Mode> MODE_MAP = new HashMap<>();
    final private Map<String, Microcensus.Purpose> PURPOSE__MAP = new HashMap<>();

    private void prepareModeMap() {
        MODE_MAP.put("2", Microcensus.Mode.pt);
        MODE_MAP.put("3", Microcensus.Mode.pt);
        MODE_MAP.put("4", Microcensus.Mode.pt);
        MODE_MAP.put("5", Microcensus.Mode.pt);
        MODE_MAP.put("6", Microcensus.Mode.pt);
        MODE_MAP.put("7", Microcensus.Mode.pt);
        MODE_MAP.put("8", Microcensus.Mode.pt);
        MODE_MAP.put("9", Microcensus.Mode.car);
        MODE_MAP.put("14", Microcensus.Mode.bike);
        MODE_MAP.put("15", Microcensus.Mode.walk);
    }

    private void preparePurposeMap() {
        PURPOSE__MAP.put("4", Microcensus.Purpose.shop);
        PURPOSE__MAP.put("5", Microcensus.Purpose.shop);
        PURPOSE__MAP.put("6", Microcensus.Purpose.remote_work);
        PURPOSE__MAP.put("7", Microcensus.Purpose.remote_work);
        PURPOSE__MAP.put("8", Microcensus.Purpose.leisure);
        PURPOSE__MAP.put("9", Microcensus.Purpose.escort_kids);
        PURPOSE__MAP.put("10", Microcensus.Purpose.escort_other);
    }

    public MicrocensusReader(Microcensus microcensus) {
        prepareModeMap();
        preparePurposeMap();
        this.microcensus = microcensus;
    }

    private List<String> processLine(String line) {
        String[] items = line.split(";");
        List<String> result = new ArrayList<>(items.length);

        for (String item : items) {
            result.add(item.replace("\"", ""));
        }

        return result;
    }

    public void read(File inputPath) throws IOException {
        logger.info("Reading microcensus from " + inputPath + " ...");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputPath)));

        String rawLine = null;
        List<String> header = null;

        while ((rawLine = reader.readLine()) != null) {
            List<String> line = processLine(rawLine);

            if (header == null) {
                header = line;
                logger.info("CSV Header: " + header);
            } else {
                double distance = Double.parseDouble(line.get(header.indexOf("w_dist_obj2")));
                double travelTime = Double.parseDouble(line.get(header.indexOf("dauer2")));

                Microcensus.Mode mode = MODE_MAP.get(line.get(header.indexOf("wmittel")));
                Microcensus.Purpose purpose = PURPOSE__MAP.get(line.get(header.indexOf("wzweck1")));

                distance *= 1000.0;
                travelTime *= 60.0;

                if (mode != null && purpose != null && distance > 0.0 && travelTime > 0.0) {
                    microcensus.addObservation(distance, travelTime, mode);
                }
            }
        }

        logger.info("Done loading " + microcensus.getNumberOfObservations() + " observations ...");
    }

    static public void main(String[] args) throws IOException {
        Microcensus microcensus = new Microcensus(); //new Microcensus(300, 30.0 * 3600.0, 100);
        MicrocensusReader loader = new MicrocensusReader(microcensus);
        loader.read(new File("/home/sebastian/uq/project/Sebastian.csv"));

        microcensus.createDistanceDistribution(new Random(0L), Microcensus.Mode.walk, 1.0 * 3600.0);
        microcensus.createDistanceDistribution(new Random(0L), Microcensus.Mode.walk, 1.0 * 3600.0 + 5.0);
    }
}
