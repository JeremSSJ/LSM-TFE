package be.lsm.tfe.stats;

import be.lsm.tfe.common.ComparateurVehicules;
import be.lsm.tfe.common.PointCroisement;
import be.lsm.tfe.common.ResultatSimulation;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

/**
 * Calcule les statistiques comparatives entre deux véhicules d'investissement.
 *
 * <p>Non instanciable — toutes les méthodes sont statiques et testables isolément.</p>
 *
 * <p>Les deux listes de résultats doivent~:
 * <ul>
 *   <li>avoir la même taille,</li>
 *   <li>être indexées dans le même ordre de versements croissants (versementMin → versementMax).</li>
 * </ul>
 * </p>
 */
public final class CalculateurStatistiques {

    private CalculateurStatistiques() {}

    // ── Point d'entrée principal ──────────────────────────────────────────────

    /**
     * Calcule l'ensemble des statistiques comparatives entre les deux séries de résultats.
     *
     * @param resultatsA  Résultats du véhicule A (EP ou ELT)
     * @param resultatsB  Résultats du véhicule B (CT)
     * @param nomA        Nom du véhicule A
     * @param nomB        Nom du véhicule B
     * @return Objet statistiques complet
     */
    public static StatistiquesComparaison calculer(
            List<ResultatSimulation> resultatsA,
            List<ResultatSimulation> resultatsB,
            String nomA,
            String nomB) {

        if (resultatsA.size() != resultatsB.size() || resultatsA.isEmpty()) {
            throw new IllegalArgumentException(
                    "Les deux listes doivent être non vides et de même taille.");
        }

        int    n            = resultatsA.size();
        int    versementMin = (int) resultatsA.get(0).versementAnnuel();
        int    versementMax = (int) resultatsA.get(n - 1).versementAnnuel();

        // Tableau des différences VAN(A) - VAN(B) pour chaque versement
        double[] diffs = calculerDifferences(resultatsA, resultatsB);

        // ── Comptages de dominance ────────────────────────────────────────────
        long nbADomine = IntStream.range(0, n).filter(i -> diffs[i] > 0).count();
        long nbBDomine = IntStream.range(0, n).filter(i -> diffs[i] < 0).count();
        long nbEgaux   = n - nbADomine - nbBDomine;

        double tauxA = 100.0 * nbADomine / n;
        double tauxB = 100.0 * nbBDomine / n;

        // ── Croisements ───────────────────────────────────────────────────────
        List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                resultatsA, resultatsB, nomA, nomB);

        double premierCroisement = croisements.isEmpty()
                ? Double.NaN
                : croisements.get(0).versementEuros();
        double dernierCroisement = croisements.size() <= 1
                ? Double.NaN
                : croisements.get(croisements.size() - 1).versementEuros();

        // ── Avantages moyens (en valeur absolue) ──────────────────────────────
        double moyenneADomine = IntStream.range(0, n)
                .filter(i -> diffs[i] > 0)
                .mapToDouble(i -> diffs[i])
                .average()
                .orElse(0.0);

        double moyenneBDomine = IntStream.range(0, n)
                .filter(i -> diffs[i] < 0)
                .mapToDouble(i -> -diffs[i])
                .average()
                .orElse(0.0);

        // ── Avantages maximaux ────────────────────────────────────────────────
        double vanDiffMaxA       = maxPositif(diffs);
        double versementAuMaxA   = versementAuMax(resultatsA, diffs, true);
        double vanDiffMaxB       = maxNegatif(diffs);
        double versementAuMaxB   = versementAuMax(resultatsA, diffs, false);

        // ── Aire des écarts (intégrale discrète, pas = 1€) ───────────────────
        double aire = IntStream.range(0, n)
                .mapToDouble(i -> diffs[i])
                .sum();

        // ── Écart au versement maximal ────────────────────────────────────────
        double diffAuMax = diffs[n - 1];

        // ── Instrument dominant global ────────────────────────────────────────
        String dominant;
        if (Math.abs(aire) < 1.0) {
            dominant = "Aucun (ex æquo)";
        } else if (aire > 0) {
            dominant = nomA;
        } else {
            dominant = nomB;
        }

        return new StatistiquesComparaison(
                nomA, nomB,
                versementMin, versementMax, n,
                nbADomine, nbBDomine, nbEgaux,
                tauxA, tauxB,
                croisements,
                premierCroisement, dernierCroisement,
                moyenneADomine, moyenneBDomine,
                vanDiffMaxA, versementAuMaxA,
                vanDiffMaxB, versementAuMaxB,
                aire,
                diffAuMax,
                dominant
        );
    }

    // ── Méthodes de calcul atomiques (testables unitairement) ─────────────────

    /**
     * Calcule le tableau des différences VAN(A) - VAN(B) pour chaque index.
     */
    public static double[] calculerDifferences(
            List<ResultatSimulation> a,
            List<ResultatSimulation> b) {
        return IntStream.range(0, a.size())
                .mapToDouble(i -> a.get(i).vanTotale() - b.get(i).vanTotale())
                .toArray();
    }

    /**
     * Retourne la plus grande valeur positive du tableau (avantage max de A).
     * Retourne 0.0 si A ne domine jamais.
     */
    public static double maxPositif(double[] diffs) {
        return IntStream.range(0, diffs.length)
                .filter(i -> diffs[i] > 0)
                .mapToDouble(i -> diffs[i])
                .max()
                .orElse(0.0);
    }

    /**
     * Retourne la valeur absolue du minimum négatif (avantage max de B).
     * Retourne 0.0 si B ne domine jamais.
     */
    public static double maxNegatif(double[] diffs) {
        return IntStream.range(0, diffs.length)
                .filter(i -> diffs[i] < 0)
                .mapToDouble(i -> -diffs[i])
                .max()
                .orElse(0.0);
    }

    /**
     * Retourne le versement annuel correspondant au maximum d'avantage de A (posMax=true)
     * ou de B (posMax=false).
     */
    public static double versementAuMax(
            List<ResultatSimulation> resultatsA,
            double[] diffs,
            boolean posMax) {

        OptionalDouble max = posMax
                ? IntStream.range(0, diffs.length).filter(i -> diffs[i] > 0)
                           .mapToDouble(i -> diffs[i]).max()
                : IntStream.range(0, diffs.length).filter(i -> diffs[i] < 0)
                           .mapToDouble(i -> -diffs[i]).max();

        if (max.isEmpty()) return Double.NaN;

        double valMax = max.getAsDouble();
        return IntStream.range(0, diffs.length)
                .filter(i -> Math.abs((posMax ? diffs[i] : -diffs[i]) - valMax) < 1e-6)
                .mapToDouble(i -> resultatsA.get(i).versementAnnuel())
                .findFirst()
                .orElse(Double.NaN);
    }
}
