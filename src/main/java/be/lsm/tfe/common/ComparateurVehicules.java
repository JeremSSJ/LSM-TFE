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
            List<ResultatSimulation> resultatsB,
            String nomA,
            String nomB) {

        if (resultatsA.size() != resultatsB.size()) {
            throw new IllegalArgumentException(
                    "Les deux listes de résultats doivent avoir la même taille. A=%d, B=%d"
                            .formatted(resultatsA.size(), resultatsB.size()));
        }

        return IntStream.range(0, resultatsA.size() - 1)
                .filter(i -> signeDifferent(
                        diffVAN(resultatsA, resultatsB, i),
                        diffVAN(resultatsA, resultatsB, i + 1)))
                .filter(i -> Math.abs(resultatsA.get(i).vanTotale()) > 1e-6
                        || Math.abs(resultatsB.get(i).vanTotale()) > 1e-6)
                .mapToObj(i -> interpolerCroisement(resultatsA, resultatsB, i, nomA, nomB))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private static double diffVAN(List<ResultatSimulation> a, List<ResultatSimulation> b, int i) {
        return a.get(i).vanTotale() - b.get(i).vanTotale();
    }

    private static boolean signeDifferent(double d1, double d2) {
        return Math.signum(d1) != Math.signum(d2) && d1 != d2;
    }

    private static PointCroisement interpolerCroisement(
            List<ResultatSimulation> a,
            List<ResultatSimulation> b,
            int i,
            String nomA,
            String nomB) {

        double v0 = a.get(i).versementAnnuel();
        double v1 = a.get(i + 1).versementAnnuel();
        double d0 = diffVAN(a, b, i);
        double d1 = diffVAN(a, b, i + 1);

        // Interpolation linéaire : v* = v0 - d0 * (v1-v0) / (d1-d0)
        double vCroisement = v0 - d0 * (v1 - v0) / (d1 - d0);

        double vanACrois = interpoler(a.get(i).vanTotale(), a.get(i + 1).vanTotale(), v0, v1, vCroisement);
        double vanBCrois = interpoler(b.get(i).vanTotale(), b.get(i + 1).vanTotale(), v0, v1, vCroisement);

        // Après le croisement, A est avantageux si d1 > 0 (A repasse au-dessus)
        String avantageuxApres = d1 > 0 ? nomA : nomB;

        return new PointCroisement(vCroisement, vanACrois, vanBCrois, avantageuxApres);
    }

    private static double interpoler(double y0, double y1, double x0, double x1, double x) {
        if (x1 == x0) return y0;
        return y0 + (y1 - y0) * (x - x0) / (x1 - x0);
    }
}
