package ch.ethz.ivt.matsim.playgrounds.sebhoerl.pushsim;

import org.matsim.api.core.v01.Id;

import java.util.Arrays;

public class RunExample {
    static public void main(String[] args) {
        PushLink linkA = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);

        PushLink linkB = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);
        PushLink linkC = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);
        PushLink linkD = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);

        PushLink linkE = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);
        PushLink linkF = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);
        PushLink linkG = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);

        PushLink linkH = new PushLink(1000.0, 1000.0 / 3600.0, 20.0);

        linkA.addOutgoingLink(Id.createLinkId("B"), linkB);
        linkA.addOutgoingLink(Id.createLinkId("C"), linkC);
        linkA.addOutgoingLink(Id.createLinkId("D"), linkD);

        linkB.addOutgoingLink(Id.createLinkId("E"), linkE);
        linkC.addOutgoingLink(Id.createLinkId("F"), linkF);
        linkD.addOutgoingLink(Id.createLinkId("G"), linkG);

        linkE.addOutgoingLink(Id.createLinkId("H"), linkH);
        linkF.addOutgoingLink(Id.createLinkId("H"), linkH);
        linkG.addOutgoingLink(Id.createLinkId("H"), linkH);

        PushVehicle vehicle = new PushVehicle(Arrays.asList(
                Id.createLinkId("C"),
                Id.createLinkId("F"),
                Id.createLinkId("H")
        ));

        double maximumTime = 3600.0;
        double currentTime = 0.0;
        double timeInterval = 1.0;

        while (currentTime < maximumTime) {
            currentTime += timeInterval;

            for (PushLink link : Arrays.asList(linkA, linkB, linkC, linkD, linkE, linkF, linkG, linkH)) {
                link.update(currentTime);
            }
        }
    }
}
