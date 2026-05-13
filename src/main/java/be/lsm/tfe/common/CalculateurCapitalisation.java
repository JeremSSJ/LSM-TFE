package be.lsm.tfe.common;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Calcule les valeurs capitalisées à l'échéance des flux annexes
 * (économies fiscales pour la Branche 23, frais/TOB pour le compte-titres).
 *
 * <p>Le taux OLO net utilisé pour la capitalisation est celui correspondant
 * à la durée totale de l'investissement, extrait de {@link oloReferential}.</p>
 */
public final class CalculateurCapitalisation {

    private CalculateurCapitalisation() {
    }

    /**
     * Capitalise les économies fiscales annuelles de la Branche 23 vers l'échéance.
     *
     * <p>L'économie de l'index {@code t} (reçue en fin d'année {@code t+1}) est
     * capitalisée sur {@code dureeTotal - (t+1)} années au taux OLO net correspondant
     * à la durée totale.</p>
     *
     * @param annees     Résultats annuels contenant les économies fiscales
     * @param dureeTotal Durée totale de l'investissement (en années)
     * @return Somme des économies fiscales capitalisées à l'échéance (€)
     */
    public static double capitaliserEconomiesFiscales(List<ResultatAnnuel> annees, int dureeTotal) {
        return IntStream.range(0, annees.size())
                .mapToDouble(t -> {
                    double economie = annees.get(t).economiesFiscales();
                    int anneesRestantes = dureeTotal - (t + 1);
                    // anneesRestantes ∈ [0, dureeTotal-1] → toujours ≥ 0
                    return economie * Math.pow(1.0 + oloReferential.tauxPourDuree(anneesRestantes), anneesRestantes);
                })
                .sum();
    }

    /**
     * Capitalise un coût annuel fixe (courtage, TOB…) vers l'échéance.
     *
     * <p>Le coût payé à chaque période {@code t} (de 0 à {@code dureeAnnees} inclus)
     * est capitalisé sur {@code dureeAnnees - t} années au taux OLO net correspondant
     * à la durée totale.</p>
     *
     * @param coutAnnuel  Montant du coût annuel (€)
     * @param dureeAnnees Durée totale de l'investissement (en années)
     * @return Somme des coûts capitalisés à l'échéance (€)
     */
    public static double capitaliserCoutsAnnuels(double coutAnnuel, int dureeAnnees) {
        return IntStream.rangeClosed(0, dureeAnnees)
                .mapToDouble(t -> coutAnnuel * Math.pow(1.0 + oloReferential.tauxPourDuree(dureeAnnees - t), dureeAnnees - t))
                .sum();
    }
}

