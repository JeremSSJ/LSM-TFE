package be.lsm.tfe.common;

import java.util.List;
import java.util.stream.IntStream;

public final class CalculateurVAN {

    public static double vanCapital(double capitalFinalNet, double tauxOLO, int dureeAnnees) {
        return actualiser(capitalFinalNet, tauxOLO, dureeAnnees);
    }

    public static double vanEconomiesFiscales(List<ResultatAnnuel> resultatsAnnuels, double tauxOLO) {
        return IntStream.range(0, resultatsAnnuels.size())
                .mapToDouble(t -> {
                    double economie = resultatsAnnuels.get(t).economiesFiscales();
                    // encaissée en t+1 → facteur d'actualisation (t+1)
                    return actualiser(economie, tauxOLO, t + 1);
                })
                .sum();
    }

    public static double actualiser(double montant, double tauxOLO, int annee) {
        if (annee < 0) throw new IllegalArgumentException("L'année ne peut être négative : " + annee);
        return montant / Math.pow(1.0 + tauxOLO, annee);
    }

    //todo retirer les suivantes

    public static double vanTotale(double capitalFinalNet,
                                    List<ResultatAnnuel> resultatsAnnuels,
                                    double tauxOLO,
                                    int dureeAnnees) {
        double vanCap    = vanCapital(capitalFinalNet, tauxOLO, dureeAnnees);
        double vanEco    = vanEconomiesFiscales(resultatsAnnuels, tauxOLO);
        return vanCap + vanEco;
    }

    public static double facteurActualisation(double tauxOLO, int annees) {
        return 1.0 / Math.pow(1.0 + tauxOLO, annees);
    }
}
