package be.lsm.tfe.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class ComparateurVehicules {

    private ComparateurVehicules() {
    }

    public static List<ResultatSimulation> simulerPlage(
            Simulateur simulateur,
            ProfilInvestisseur profil,
            ParametresRendement rendement,
            int versementMin,
            int versementMax) {

        return IntStream.rangeClosed(versementMin, versementMax)
                .mapToObj(v -> simulateur.simuler(profil, v, rendement))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static List<PointCroisement> trouverCroisements(
            List<ResultatSimulation> resultatsA,
            List<ResultatSimulation> resultatsB) {

        if (resultatsA.size() != resultatsB.size()) {
            throw new IllegalArgumentException(
                    "Les deux listes de résultats doivent avoir la même taille. A=%d, B=%d"
                            .formatted(resultatsA.size(), resultatsB.size()));
        }

        return IntStream.range(0, resultatsA.size() - 1)
                .filter(i -> signeDifferent(
                        diffValeurTerminale(resultatsA, resultatsB, i),
                        diffValeurTerminale(resultatsA, resultatsB, i + 1)))
                .filter(i -> Math.abs(resultatsA.get(i).valeurTerminale()) > 1e-6
                        || Math.abs(resultatsB.get(i).valeurTerminale()) > 1e-6)
                .mapToObj(i -> interpolerCroisement(resultatsA, resultatsB, i))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static double diffValeurTerminale(List<ResultatSimulation> a, List<ResultatSimulation> b, int i) {
        return a.get(i).valeurTerminale() - b.get(i).valeurTerminale();
    }

    private static boolean signeDifferent(double d1, double d2) {
        // Détecte uniquement la transition "non-négatif → négatif" ou "non-positif → positif",
        // ce qui évite de compter deux croisements quand diff passe exactement par 0.
        return (d1 > 0 && d2 <= 0) || (d1 < 0 && d2 >= 0);
    }

    private static PointCroisement interpolerCroisement(
            List<ResultatSimulation> a,
            List<ResultatSimulation> b,
            int i) {

        double v0 = a.get(i).versementAnnuel();
        double v1 = a.get(i + 1).versementAnnuel();
        double d0 = diffValeurTerminale(a, b, i);
        double d1 = diffValeurTerminale(a, b, i + 1);

        // Interpolation linéaire : v* = v0 - d0 * (v1-v0) / (d1-d0)
        double vCroisement = v0 - d0 * (v1 - v0) / (d1 - d0);

        return new PointCroisement(vCroisement);
    }
}
