package be.lsm.tfe.common;

import java.util.List;
import java.util.stream.IntStream;

public final class CalculateurVAN {

    private CalculateurVAN() {
    }

    public static double vanCapital(double capitalFinalNet, int dureeAnnees) {
        return actualiser(capitalFinalNet, dureeAnnees);
    }

    public static double vanCapital(double capitalFinalNet, int dureeAnnees, double tauxActualisation) {
        return actualiser(capitalFinalNet, dureeAnnees, tauxActualisation);
    }

    public static double vanEconomiesFiscales(List<ResultatAnnuel> resultatsAnnuels) {
        return IntStream.range(0, resultatsAnnuels.size())
                .mapToDouble(t -> {
                    double economie = resultatsAnnuels.get(t).economiesFiscales();
                    // encaissée en t+1 → facteur d'actualisation (t+1)
                    return actualiser(economie, t + 1);
                })
                .sum();
    }

    public static double vanCoutsAnnuels(double coutAnnuel, int dureeAnnees) {
        return IntStream.rangeClosed(0, dureeAnnees)
                .mapToDouble(t -> actualiser(coutAnnuel, t))
                .sum();
    }

    public static double actualiser(double montant, int annee) {
        if (annee < 0) throw new IllegalArgumentException("L'année ne peut être négative : " + annee);
        if (annee == 0) {
            return montant;
        }

        double tauxOLO = oloReferential.tauxPourDuree(annee);
        return actualiser(montant, annee, tauxOLO);
    }

    private static double actualiser(double montant, int annee, double tauxActualisation) {
        if (annee < 0) throw new IllegalArgumentException("L'année ne peut être négative : " + annee);
        if (annee == 0) {
            return montant;
        }

        return montant / Math.pow(1.0 + tauxActualisation, annee);
    }
}
