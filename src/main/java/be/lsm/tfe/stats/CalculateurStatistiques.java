package be.lsm.tfe.stats;

import be.lsm.tfe.common.ComparateurVehicules;
import be.lsm.tfe.common.PointCroisement;
import be.lsm.tfe.common.ResultatSimulation;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Calcule les statistiques comparatives entre deux véhicules d'investissement.
 *
 * <p>Non instanciable — toutes les méthodes sont statiques et testables isolément.</p>
 */
public final class CalculateurStatistiques {

    private CalculateurStatistiques() {}

    // ── Point d'entrée principal ──────────────────────────────────────────────

    /**
     * Calcule l'ensemble des statistiques comparatives entre les deux séries.
     *
     * @param resultatsA  Résultats du véhicule A (EP ou ELT — Branche 23)
     * @param resultatsB  Résultats du véhicule B (Compte-Titres)
     * @param nomA        Nom du véhicule A
     * @param nomB        Nom du véhicule B
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

        int n            = resultatsA.size();
        int versementMin = (int) resultatsA.get(0).versementAnnuel();
        int versementMax = (int) resultatsA.get(n - 1).versementAnnuel();

        double[] diffs = calculerDifferences(resultatsA, resultatsB);

        // ── Dominance ─────────────────────────────────────────────────────────
        long nbADomine = IntStream.range(0, n).filter(i -> diffs[i] > 0).count();
        long nbBDomine = IntStream.range(0, n).filter(i -> diffs[i] < 0).count();
        long nbEgaux   = n - nbADomine - nbBDomine;
        double tauxA   = 100.0 * nbADomine / n;
        double tauxB   = 100.0 * nbBDomine / n;

        // ── Croisements ───────────────────────────────────────────────────────
        List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                resultatsA, resultatsB, nomA, nomB);

        double premierCroisement = croisements.isEmpty()
                ? Double.NaN : croisements.get(0).versementEuros();
        double dernierCroisement = croisements.size() <= 1
                ? Double.NaN : croisements.get(croisements.size() - 1).versementEuros();

        // ── Aire des écarts (utilisée uniquement pour déterminer le dominant) ─
        double aire = IntStream.range(0, n).mapToDouble(i -> diffs[i]).sum();

        // ── Dominant global ───────────────────────────────────────────────────
        String dominant;
        if (Math.abs(aire) < 1.0)  dominant = "Aucun (ex æquo)";
        else if (aire > 0)          dominant = nomA;
        else                        dominant = nomB;

        // ── VAN moyennes des 3 composantes ───────────────────────────────────
        double vanMoyCapB23 = resultatsA.stream()
                .mapToDouble(ResultatSimulation::vanCapital)
                .average().orElse(0.0);

        double vanMoyEcoB23 = resultatsA.stream()
                .mapToDouble(ResultatSimulation::vanEconomiesFiscales)
                .average().orElse(0.0);

        double vanMoyCapCT = resultatsB.stream()
                .mapToDouble(ResultatSimulation::vanTotale)
                .average().orElse(0.0);

        return new StatistiquesComparaison(
                nomA, nomB,
                versementMin, versementMax, n,
                nbADomine, nbBDomine, nbEgaux,
                tauxA, tauxB,
                croisements,
                premierCroisement, dernierCroisement,
                dominant,
                vanMoyCapB23,
                vanMoyEcoB23,
                vanMoyCapCT
        );
    }

    // ── Méthodes atomiques (testables unitairement) ───────────────────────────

    /** Calcule le tableau des différences VAN(A) - VAN(B) pour chaque index. */
    public static double[] calculerDifferences(
            List<ResultatSimulation> a, List<ResultatSimulation> b) {
        return IntStream.range(0, a.size())
                .mapToDouble(i -> a.get(i).vanTotale() - b.get(i).vanTotale())
                .toArray();
    }
}
