package be.lsm.tfe.common;

import java.util.List;

/**
 * Résultat d'une simulation pour un versement annuel donné.
 *
 * @param versementAnnuel                  Versement annuel simulé (€)
 * @param capitalFinal                     Capital brut en fin de période (€)
 * @param capitalFinalNet                  Capital net après fiscalité de sortie (taxe PV, etc.) (€)
 * @param economiesFiscalesCapitalisees    Somme des économies fiscales capitalisées à l'échéance,
 *                                         au taux OLO net de la durée totale (€) — Branche 23 uniquement, 0 pour CT
 * @param feesCapitalises                  Somme des frais (TOB + courtage) capitalisés à l'échéance,
 *                                         au taux OLO net de la durée totale (€) — CT uniquement, 0 pour B23
 * @param valeurTerminale                  Valeur terminale nette utilisée pour la comparaison (€)
 *                                         B23 : capitalFinalNet + économiesFiscalesCapitalisées
 *                                         CT  : capitalFinalNet − feesCapitalisés
 * @param taxeCompteTitresTotale           Taxe sur grands patrimoines cumulée sur la durée (€)
 * @param resultatParAnnee                 Détail année par année
 */
public record ResultatSimulation(
        double               versementAnnuel,
        double               capitalFinal,
        double               capitalFinalNet,
        double               economiesFiscalesCapitalisees,
        double               feesCapitalises,
        double               valeurTerminale,
        double               taxeCompteTitresTotale,
        List<ResultatAnnuel> resultatParAnnee
) {
    public ResultatSimulation {
        resultatParAnnee = List.copyOf(resultatParAnnee);
    }
}
