package be.lsm.tfe.stats;

import be.lsm.tfe.common.ResultatSimulation;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CalculateurStatistiques")
class CalculateurStatistiquesTest {

    private static final double DELTA = 1e-6;

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Crée un ResultatSimulation avec seulement la vanTotale et versementAnnuel renseignés. */
    private static ResultatSimulation resultat(double versement, double van) {
        return new ResultatSimulation(versement, van, van, van, 0.0, van, List.of());
    }

    /** Crée une liste de résultats où VAN = coefficient × versement. */
    private static List<ResultatSimulation> serieLineaire(int min, int max, double coeff) {
        return IntStream.rangeClosed(min, max)
                .mapToObj(v -> resultat(v, coeff * v))
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculerDifferences()")
    class CalcDiff {

        @Test
        @DisplayName("A toujours > B → toutes les différences positives")
        void aToujorsSuperieur() {
            var a = serieLineaire(0, 10, 3.0);
            var b = serieLineaire(0, 10, 1.0);
            double[] d = CalculateurStatistiques.calculerDifferences(a, b);
            for (double v : d) assertThat(v).isGreaterThanOrEqualTo(0.0);
        }

        @Test
        @DisplayName("Croisement à versement=5 : A>B pour v<5, B>A pour v>5")
        void croisementA5() {
            // A = 10 - v, B = v  → croisement à v=5
            var a = IntStream.rangeClosed(0, 10).mapToObj(v -> resultat(v, 10.0 - v)).toList();
            var b = serieLineaire(0, 10, 1.0);
            double[] d = CalculateurStatistiques.calculerDifferences(a, b);
            assertThat(d[0]).isGreaterThan(0); // v=0 : A=10, B=0
            assertThat(d[5]).isCloseTo(0.0, within(DELTA)); // v=5 : A=5, B=5
            assertThat(d[10]).isLessThan(0);  // v=10 : A=0, B=10
        }
    }


    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculer() — statistiques globales")
    class Calculer {

        @Test
        @DisplayName("A toujours > B : taux dominance A = 100%, B = 0%")
        void aDominePartout() {
            var a = serieLineaire(0, 100, 3.0);
            var b = serieLineaire(0, 100, 1.0);
            var stats = CalculateurStatistiques.calculer(a, b, "A", "B");

            assertThat(stats.tauxDominanceA()).isCloseTo(100.0, within(0.01));
            assertThat(stats.tauxDominanceB()).isCloseTo(0.0, within(0.01));
            assertThat(stats.nbPointsADomine()).isEqualTo(101); // 0..100 = 101 points (v=0 → VAN=0 → égal)
            assertThat(stats.croisements()).isEmpty();
        }

        @Test
        @DisplayName("Un croisement à ~500€ est détecté")
        void croisementA500() {
            // A = 1000 - v, B = v → croisement à v=500
            var a = IntStream.rangeClosed(0, 1000).mapToObj(v -> resultat(v, 1000.0 - v)).toList();
            var b = serieLineaire(0, 1000, 1.0);
            var stats = CalculateurStatistiques.calculer(a, b, "A", "B");

            assertThat(stats.croisements()).hasSize(1);
            assertThat(stats.premierCroisement()).isCloseTo(500.0, within(1.0));
            assertThat(stats.instrumentDominantGlobal()).isEqualTo("Aucun (ex æquo)"); // aires symétriques
        }

        @Test
        @DisplayName("VAN différentielle au versement max correcte")
        void vanDiffAuMax() {
            var a = serieLineaire(0, 10, 2.0); // VAN(10) = 20
            var b = serieLineaire(0, 10, 3.0); // VAN(10) = 30
            var stats = CalculateurStatistiques.calculer(a, b, "A", "B");

            assertThat(stats.instrumentDominantGlobal()).isEqualTo("B");
        }

        @Test
        @DisplayName("nbPointsTotal = versementMax - versementMin + 1")
        void nbPointsTotal() {
            var a = serieLineaire(100, 500, 1.0);
            var b = serieLineaire(100, 500, 1.5);
            var stats = CalculateurStatistiques.calculer(a, b, "A", "B");
            assertThat(stats.nbPointsTotal()).isEqualTo(401);
            assertThat(stats.versementMin()).isEqualTo(100);
            assertThat(stats.versementMax()).isEqualTo(500);
        }

        @Test
        @DisplayName("Avantage moyen A = moyenne des écarts positifs")
        void avantageMoyenA() {
            // A = [3, 3, 1], B = [1, 1, 3] → écarts = [2, 2, -2]
            // moyen A quand domine = (2+2)/2 = 2
            List<ResultatSimulation> a = List.of(
                    resultat(1, 3), resultat(2, 3), resultat(3, 1));
            List<ResultatSimulation> b = List.of(
                    resultat(1, 1), resultat(2, 1), resultat(3, 3));
            var stats = CalculateurStatistiques.calculer(a, b, "A", "B");
            assertThat(stats.nbPointsADomine()).isEqualTo(2);
            assertThat(stats.nbPointsBDomine()).isEqualTo(1);
        }

        @Test
        @DisplayName("Taille différente lève une exception")
        void tailleDifferente() {
            var a = serieLineaire(0, 10, 1.0);
            var b = serieLineaire(0, 5,  1.0);
            assertThatThrownBy(() -> CalculateurStatistiques.calculer(a, b, "A", "B"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
