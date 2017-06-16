package ch.ethz.ivt.matsim.playgrounds.sebhoerl.network_compression;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.network.io.NetworkWriter;
import org.matsim.core.utils.misc.Counter;

import java.util.*;
import java.util.stream.Collectors;

public class NetworkSimplifier {
    static public void main(String[] args) {
        Network original = NetworkUtils.createNetwork();
        new MatsimNetworkReader(original).readFile(args[0]);

        Network updated = new NetworkSimplifier().simplify(original);
        new NetworkWriter(updated).write(args[1]);
    }

    final static private Logger logger = Logger.getLogger(NetworkSimplifier.class);

    public Network simplify(Network original) {
        Set<Node> candidateNodes = original.getNodes().values().stream().filter(n -> n.getOutLinks().size() == 1 && n.getInLinks().size() == 1).collect(Collectors.toSet());
        logger.info(String.format("Found %s candidate nodes", candidateNodes.size()));

        Set<List<Link>> collapsableRoutes = new HashSet<>();
        Counter counter = new Counter("", " links processed");

        for (Node sourceNode : original.getNodes().values()) {
            for (Link sourceLink : sourceNode.getOutLinks().values()) {
                List<Link> route = new LinkedList<>();

                Link currentLink = sourceLink;

                while (candidateNodes.contains(currentLink.getToNode())) {
                    route.add(currentLink);
                    currentLink = currentLink.getToNode().getOutLinks().values().iterator().next();
                }

                route.add(currentLink);

                if (route.size() > 1) {
                    collapsableRoutes.add(route);
                }
            }

            counter.incCounter();
        }

        logger.info(String.format("Found %d collapsable routes", collapsableRoutes.size()));

        final Set<Link> redundantLinks = new HashSet<>();
        collapsableRoutes.forEach(l -> redundantLinks.addAll(l.subList(1, l.size())));
        final Set<Node> redundantNodes = redundantLinks.stream().map(l -> l.getFromNode()).collect(Collectors.toSet());

        logger.info(String.format("Found %d redundant links", redundantLinks.size()));
        logger.info(String.format("Found %d redundant nodes", redundantNodes.size()));

        Network network = NetworkUtils.createNetwork();
        NetworkFactory factory = network.getFactory();

        final Map<Link, Link> collapsedLinks = new HashMap<>();
        final Map<Link, List<Link>> collapsedRoutes = new HashMap<>();

        for (List<Link> route : collapsableRoutes) {
            Link link = factory.createLink(route.get(0).getId(), route.get(0).getFromNode(), route.get(route.size() - 1).getToNode());

            List<Double> lengths = route.stream().map(l -> l.getLength()).collect(Collectors.toList());
            List<Double> speeds = route.stream().map(l -> l.getFreespeed()).collect(Collectors.toList());
            List<Double> capacities = route.stream().map(l -> l.getCapacity()).collect(Collectors.toList());
            List<Double> laness = route.stream().map(l -> l.getNumberOfLanes()).collect(Collectors.toList());

            Set<String> allowedModes = new HashSet<>();
            route.forEach(l -> allowedModes.addAll(l.getAllowedModes()));

            double length = lengths.stream().mapToDouble(d -> d).sum();

            double speed = 0.0;
            for (int i = 0; i < lengths.size(); i++) speed += speeds.get(i) * lengths.get(i) / length;

            double capacity = capacities.stream().mapToDouble(d -> d).min().getAsDouble();
            double lanes = laness.stream().mapToDouble(d -> d).min().getAsDouble();

            link.setAllowedModes(allowedModes);
            link.setFreespeed(speed);
            link.setCapacity(capacity);
            link.setLength(length);
            link.setNumberOfLanes(lanes);

            collapsedLinks.put(route.get(0), link);
            collapsedRoutes.put(route.get(0), route);
        }

        for (Node node : original.getNodes().values()) {
            if (!redundantNodes.contains(node)) {
                network.addNode(factory.createNode(node.getId(), node.getCoord()));
            } else if(node.getInLinks().size() != 1 || node.getOutLinks().size() != 1) {
                logger.error("Node " + node.getId() + " should not be removed!");
                throw new RuntimeException("Only nodes with inlinks = 1 and outlinks = 1 should be removed!");
            }
        }

        for (Link link : original.getLinks().values()) {
            if (!redundantLinks.contains(link)) {
                Link updated = factory.createLink(link.getId(), link.getFromNode(), link.getToNode());

                if (collapsedLinks.containsKey(link)) {
                    updated = collapsedLinks.get(link);
                    List<Link> route = collapsedRoutes.get(link);
                    String replacement = String.join(",", route.subList(1, route.size()).stream().map(l -> l.getId().toString()).collect(Collectors.toList()));
                    updated.getAttributes().putAttribute("replaces", replacement);
                } else {
                    updated.setLength(link.getLength());
                    updated.setCapacity(link.getCapacity());
                    updated.setNumberOfLanes(link.getNumberOfLanes());
                    updated.setFreespeed(link.getFreespeed());
                    updated.setAllowedModes(link.getAllowedModes());
                }

                updated.setFromNode(network.getNodes().get(updated.getFromNode().getId()));
                updated.setToNode(network.getNodes().get(updated.getToNode().getId()));

                network.addLink(updated);
            }
        }

        logger.info(String.format("Number of links (original): %d", original.getLinks().size()));
        logger.info(String.format("Number of nodes (original): %d", original.getNodes().size()));

        logger.info(String.format("Number of links (update): %d (%.2f%%)", network.getLinks().size(), 100.0 * (double) network.getLinks().size() / (double) original.getLinks().size()));
        logger.info(String.format("Number of nodes (update): %d (%.2f%%)", network.getNodes().size(), 100.0 * (double) network.getNodes().size() / (double) original.getNodes().size()));

        return network;
    }
}
