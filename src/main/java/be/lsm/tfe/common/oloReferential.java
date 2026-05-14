package be.lsm.tfe.common;

import java.util.Map;

public final class oloReferential {

    private static final int DUREE_MAX_REFERENTIEL = 30;

    private static final Map<Integer, Double> OLO_MAP_REFERENTIAL = Map.ofEntries(
            Map.entry(1, 0.0),
            Map.entry(2, 0.0),
            Map.entry(3, 0.0),
            Map.entry(4, 0.0),
            Map.entry(5, 0.0),
            Map.entry(6, 0.0),
            Map.entry(7, 0.0),
            Map.entry(8, 0.0),
            Map.entry(9, 0.0),
            Map.entry(10, 0.0),
            Map.entry(11, 0.0),
            Map.entry(12, 0.0),
            Map.entry(13, 0.0),
            Map.entry(14, 0.0),
            Map.entry(15, 0.0),
            Map.entry(16, 0.0),
            Map.entry(17, 0.0),
            Map.entry(18, 0.0),
            Map.entry(19, 0.0),
            Map.entry(20, 0.0),
            Map.entry(21, 0.0),
            Map.entry(22, 0.0),
            Map.entry(23, 0.0),
            Map.entry(24, 0.0),
            Map.entry(25, 0.0),
            Map.entry(26, 0.0),
            Map.entry(27, 0.0),
            Map.entry(28, 0.0),
            Map.entry(29, 0.0),
            Map.entry(30, 0.0)
    );

    private oloReferential() {
    }

    public static double tauxPourDuree(int dureeAnnees) {
        if (dureeAnnees < 0) {
            return 0.0;
        }
        if (dureeAnnees == 0) {
            return 0.0;
        }

        int dureeReference = Math.min(dureeAnnees, DUREE_MAX_REFERENTIEL);
        return OLO_MAP_REFERENTIAL.get(dureeReference) / 100.0;
    }
}
