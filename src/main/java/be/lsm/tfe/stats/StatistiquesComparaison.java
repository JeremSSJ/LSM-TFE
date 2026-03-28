package be.lsm.tfe.stats;

import java.util.List;
import be.lsm.tfe.common.PointCroisement;

/**
 * Résultat statistique d'une comparaison entre deux véhicules d'investissement
 * sur une plage de versements annuels.
 *
 * <p>Chaque champ est calculé sur l'ensemble des versements de la plage
 * [versementMin, versementMax] avec un pas de 1€.</p>
 *
 * @param nomVehiculeA           Nom du véhicule A (ex. "EP Branche 23")
 * @param nomVehiculeB           Nom du véhicule B (ex. "Compte-Titres")
 * @param versementMin           Borne inférieure de la plage simulée (€)
 * @param versementMax           Borne supérieure de la plage simulée (€)
 * @param nbPointsTotal          Nombre de versements simulés (versementMax - versementMin + 1)
 * @param nbPointsADomine        Nombre de versements où VAN(A) > VAN(B)
 * @param nbPointsBDomine        Nombre de versements où VAN(B) > VAN(A)
 * @param nbPointsEgaux          Nombre de versements où VAN(A) = VAN(B) (dans la précision numérique)
 * @param tauxDominanceA         Part de la plage où A domine (en %)
 * @param tauxDominanceB         Part de la plage où B domine (en %)
 * @param croisements            Points de croisement interpolés entre les deux courbes
 * @param premierCroisement      Versement au premier croisement (NaN si aucun croisement)
 * @param dernierCroisement      Versement au dernier croisement (NaN si <= 1 croisement)
 * @param vanDiffMoyenneADomine  VAN(A) - VAN(B) moyenne quand A domine (€, toujours >= 0)
 * @param vanDiffMoyenneBDomine  VAN(B) - VAN(A) moyenne quand B domine (€, toujours >= 0)
 * @param vanDiffMaxA            Avantage maximum de A sur B (€)
 * @param versementAuMaxA        Versement correspondant à l'avantage max de A (€/an)
 * @param vanDiffMaxB            Avantage maximum de B sur A (€)
 * @param versementAuMaxB        Versement correspondant à l'avantage max de B (€/an)
 * @param aireEcartsAvsB         Intégrale discrète de (VAN(A) - VAN(B)) sur toute la plage (€²)
 *                               Positive = A avantageux en espérance, négative = B avantageux
 * @param vanDiffAuVersementMax  VAN(A) - VAN(B) au versement maximum de la plage (€)
 * @param instrumentDominantGlobal "A", "B", ou "Aucun" (ex æquo) selon l'aire des écarts
 */
public record StatistiquesComparaison(
        String             nomVehiculeA,
        String             nomVehiculeB,
        int                versementMin,
        int                versementMax,
        int                nbPointsTotal,
        long               nbPointsADomine,
        long               nbPointsBDomine,
        long               nbPointsEgaux,
        double             tauxDominanceA,
        double             tauxDominanceB,
        List<PointCroisement> croisements,
        double             premierCroisement,
        double             dernierCroisement,
        double             vanDiffMoyenneADomine,
        double             vanDiffMoyenneBDomine,
        double             vanDiffMaxA,
        double             versementAuMaxA,
        double             vanDiffMaxB,
        double             versementAuMaxB,
        double             aireEcartsAvsB,
        double             vanDiffAuVersementMax,
        String             instrumentDominantGlobal
) {
    public StatistiquesComparaison {
        croisements = List.copyOf(croisements);
    }

    /** Résumé lisible en console. */
    public String toResume() {
        return """
                ══════════════════════════════════════════════════════════════
                 %s  vs  %s
                 Plage : %d€ → %d€  (%d points)
                ──────────────────────────────────────────────────────────────
                 Dominance %s   : %5.1f%%  (%d / %d versements)
                 Dominance %s   : %5.1f%%  (%d / %d versements)
                 Points égaux            : %d
                ──────────────────────────────────────────────────────────────
                 Croisements détectés    : %d
                 Premier croisement      : %s
                 Dernier croisement      : %s
                ──────────────────────────────────────────────────────────────
                 Avantage moy. %s       : %,.0f €  (quand il domine)
                 Avantage moy. %s       : %,.0f €  (quand il domine)
                 Avantage max  %s       : %,.0f €  (à %,.0f €/an)
                 Avantage max  %s       : %,.0f €  (à %,.0f €/an)
                ──────────────────────────────────────────────────────────────
                 Écart au versement max  : %+,.0f €  (+ = %s avantageux)
                 Instrument dominant     : %s  (selon aire des écarts)
                ══════════════════════════════════════════════════════════════
                """.formatted(
                nomVehiculeA, nomVehiculeB,
                versementMin, versementMax, nbPointsTotal,
                nomVehiculeA, tauxDominanceA, nbPointsADomine, nbPointsTotal,
                nomVehiculeB, tauxDominanceB, nbPointsBDomine, nbPointsTotal,
                nbPointsEgaux,
                croisements.size(),
                Double.isNaN(premierCroisement) ? "aucun" : "%,.0f €/an".formatted(premierCroisement),
                Double.isNaN(dernierCroisement) ? "aucun" : "%,.0f €/an".formatted(dernierCroisement),
                nomVehiculeA, vanDiffMoyenneADomine,
                nomVehiculeB, vanDiffMoyenneBDomine,
                nomVehiculeA, vanDiffMaxA, versementAuMaxA,
                nomVehiculeB, vanDiffMaxB, versementAuMaxB,
                vanDiffAuVersementMax, vanDiffAuVersementMax >= 0 ? nomVehiculeA : nomVehiculeB,
                instrumentDominantGlobal
        );
    }
}
