package be.lsm.tfe.common;

import java.util.Map;

public final class oloReferential {

    private static final int DUREE_MAX_REFERENTIEL = 30;

    private static final Map<Integer, Double> OLO_MAP_REFERENTIAL = Map.ofEntries(
            Map.entry(1, 2.62*0.7),
            Map.entry(2, 2.72*0.7),
            Map.entry(3, 2.83*0.7),
            Map.entry(4, 2.95*0.7),
            Map.entry(5, 3.06*0.7),
            Map.entry(6, 3.17*0.7),
            Map.entry(7, 3.27*0.7),
            Map.entry(8, 3.39*0.7),
            Map.entry(9, 3.51*0.7),
            Map.entry(10, 3.63*0.7),
            Map.entry(11, 3.73*0.7),
            Map.entry(12, 3.81*0.7),
            Map.entry(13, 3.87*0.7),
            Map.entry(14, 3.93*0.7),
            Map.entry(15, 3.98*0.7),
            Map.entry(16, 4.03*0.7),
            Map.entry(17, 4.08*0.7),
            Map.entry(18, 4.13*0.7),
            Map.entry(19, 4.17*0.7),
            Map.entry(20, 4.21*0.7),
            Map.entry(21, 4.25*0.7),
            Map.entry(22, 4.29*0.7),
            Map.entry(23, 4.32*0.7),
            Map.entry(24, 4.35*0.7),
            Map.entry(25, 4.37*0.7),
            Map.entry(26, 4.39*0.7),
            Map.entry(27, 4.41*0.7),
            Map.entry(28, 4.42*0.7),
            Map.entry(29, 4.43*0.7),
            Map.entry(30, 4.43*0.7)
    );

    private oloReferential() {
    }

    public static double tauxPourDuree(int dureeAnnees) {
        if (dureeAnnees < 0) {
            throw new IllegalArgumentException("La durée ne peut être négative : " + dureeAnnees);
        }
        if (dureeAnnees == 0) {
            return 0.0;
        }

        int dureeReference = Math.min(dureeAnnees, DUREE_MAX_REFERENTIEL);
        return OLO_MAP_REFERENTIAL.get(dureeReference) / 100.0;
    }
}
