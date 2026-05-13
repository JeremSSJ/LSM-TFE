package tfe.common;

import be.lsm.tfe.common.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ComparateurVehicules")
class ComparateurVehiculesTest {

    // ── Stub minimal d'un Simulateur ─────────────────────────────────────────

    /**
     * Simulateur bouchon dont la VAN totale = versementAnnuel × multiplicateur.
     * Cela permet de construire des courbes parfaitement prévisibles pour les tests.
     */
    private static Simulateur stubSimulateur(String nom, double multiplicateur) {
        return new Simulateur() {
            @Override
            public ResultatSimulation simuler(ProfilInvestisseur p,
                                              double versementAnnuel,
                                              ParametresRendement r) {
                double vt = versementAnnuel * multiplicateur;
                return new ResultatSimulation(versementAnnuel, vt, vt, 0.0, 0.0, vt, 0.0, List.of());
            }
            @Override
            public String nomVehicule() { return nom; }
        };
    }

    private ProfilInvestisseur profil;
    private ParametresRendement rendement;

    @BeforeEach
    void setup() {
        profil    = new ProfilInvestisseur("T", "T", 1990, Constants.AGE_DEBUT_DEFAUT,
                Constants.AGE_FIN_HORIZON);
        rendement = new ParametresRendement(Constants.RENDEMENT_DEFAUT);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("simulerPlage()")
    class SimulerPlage {

        @Test
        @DisplayName("Retourne (max - min + 1) résultats")
        void tailleCorrecteDeLaListe() {
            Simulateur sim = stubSimulateur("X", 1.0);
            List<ResultatSimulation> resultats = ComparateurVehicules.simulerPlage(
                    sim, profil, rendement, 0, 100);
            assertThat(resultats).hasSize(101);
        }

        @Test
        @DisplayName("Premier élément a versementAnnuel = versementMin")
        void premierElementCorrespondAuMin() {
            Simulateur sim = stubSimulateur("X", 1.0);
            List<ResultatSimulation> resultats = ComparateurVehicules.simulerPlage(
                    sim, profil, rendement, 50, 200);
            assertThat(resultats.get(0).versementAnnuel()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("Dernier élément a versementAnnuel = versementMax")
        void dernierElementCorrespondAuMax() {
            Simulateur sim = stubSimulateur("X", 1.0);
            List<ResultatSimulation> resultats = ComparateurVehicules.simulerPlage(
                    sim, profil, rendement, 0, 500);
            assertThat(resultats.get(resultats.size() - 1).versementAnnuel()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("Chaque résultat a la valeur terminale = versement × multiplicateur")
        void chaquResultatVANCorrecte() {
            double mult = 2.5;
            Simulateur sim = stubSimulateur("X", mult);
            List<ResultatSimulation> resultats = ComparateurVehicules.simulerPlage(
                    sim, profil, rendement, 0, 10);
            IntStream.rangeClosed(0, 10).forEach(v ->
                    assertThat(resultats.get(v).valeurTerminale())
                            .isCloseTo(v * mult, within(1e-9)));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("trouverCroisements()")
    class TrouverCroisements {

        @Test
        @DisplayName("Aucun croisement si A toujours supérieur à B")
        void aToujorsSuperieur_aucunCroisement() {
            // A = v * 3,  B = v * 1 — A domine partout
            List<ResultatSimulation> a = ComparateurVehicules.simulerPlage(
                    stubSimulateur("A", 3.0), profil, rendement, 0, 100);
            List<ResultatSimulation> b = ComparateurVehicules.simulerPlage(
                    stubSimulateur("B", 1.0), profil, rendement, 0, 100);
            assertThat(ComparateurVehicules.trouverCroisements(a, b)).isEmpty();
        }

        @Test
        @DisplayName("Aucun croisement si B toujours supérieur à A")
        void bToujoursSuperieur_aucunCroisement() {
            List<ResultatSimulation> a = ComparateurVehicules.simulerPlage(
                    stubSimulateur("A", 1.0), profil, rendement, 0, 100);
            List<ResultatSimulation> b = ComparateurVehicules.simulerPlage(
                    stubSimulateur("B", 3.0), profil, rendement, 0, 100);
            assertThat(ComparateurVehicules.trouverCroisements(a, b)).isEmpty();
        }

        @Test
        @DisplayName("Détecte un croisement quand A commence au-dessus puis passe en-dessous")
        void unCroisement_aPasseEnDessous() {
            /*
             * A(v) = 1000 - v  (décroissante : commence haute, finit basse)
             * B(v) = v          (croissante)
             * Croisement théorique : 1000 - v = v → v = 500
             */
            Simulateur simA = buildSimulateurAvecVanPersonnalisee(v -> 1_000.0 - v);
            Simulateur simB = buildSimulateurAvecVanPersonnalisee(v -> v);

            List<ResultatSimulation> ra = ComparateurVehicules.simulerPlage(
                    simA, profil, rendement, 0, 1_000);
            List<ResultatSimulation> rb = ComparateurVehicules.simulerPlage(
                    simB, profil, rendement, 0, 1_000);

            List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                    ra, rb);

            assertThat(croisements).hasSize(1);
            assertThat(croisements.get(0).versementEuros())
                    .isCloseTo(500.0, within(1.0));
        }

        @Test
        @DisplayName("Tailles différentes lèvent une exception")
        void taillesDifferentes_leveException() {
            List<ResultatSimulation> a = ComparateurVehicules.simulerPlage(
                    stubSimulateur("A", 1.0), profil, rendement, 0, 10);
            List<ResultatSimulation> b = ComparateurVehicules.simulerPlage(
                    stubSimulateur("B", 2.0), profil, rendement, 0, 20);
            assertThatThrownBy(() -> ComparateurVehicules.trouverCroisements(a, b))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        // ── Helpers ─────────────────────────────────────────────────────────

        @FunctionalInterface
        interface FonctionVAN { double calculer(double versement); }

        private Simulateur buildSimulateurAvecVanPersonnalisee(FonctionVAN fn) {
            return new Simulateur() {
                @Override
                public ResultatSimulation simuler(ProfilInvestisseur p,
                                                  double versementAnnuel,
                                                  ParametresRendement r) {
                    double vt = fn.calculer(versementAnnuel);
                    return new ResultatSimulation(versementAnnuel, vt, vt, 0.0, 0.0, vt, 0.0, List.of());
                }
                @Override
                public String nomVehicule() { return "test"; }
            };
        }
    }
}
