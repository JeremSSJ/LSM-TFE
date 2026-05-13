package be.lsm.tfe.common;

import java.util.Map;

public final class oloReferential {

    private static final int DUREE_MAX_REFERENTIEL = 30;

    private static final Map<Integer, Double> OLO_MAP_REFERENTIAL = Map.ofEntries(
            Map.entry(1, 1.834),
            Map.entry(2, 1.904),
            Map.entry(3, 1.981),
            Map.entry(4, 2.065),
            Map.entry(5, 2.142),
            Map.entry(6, 2.219),
            Map.entry(7, 2.289),
            Map.entry(8, 2.373),
            Map.entry(9, 2.457),
            Map.entry(10, 2.541),
            Map.entry(11, 2.611),
            Map.entry(12, 2.667),
            Map.entry(13, 2.709),
            Map.entry(14, 2.751),
            Map.entry(15, 2.786),
            Map.entry(16, 2.821),
            Map.entry(17, 2.856),
            Map.entry(18, 2.891),
            Map.entry(19, 2.919),
            Map.entry(20, 2.947),
            Map.entry(21, 2.975),
            Map.entry(22, 3.003),
            Map.entry(23, 3.024),
            Map.entry(24, 3.045),
            Map.entry(25, 3.059),
            Map.entry(26, 3.073),
            Map.entry(27, 3.087),
            Map.entry(28, 3.094),
            Map.entry(29, 3.101),
            Map.entry(30, 3.101)
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
