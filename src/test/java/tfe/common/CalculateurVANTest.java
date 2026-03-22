package tfe.common;

import be.lsm.tfe.common.CalculateurVAN;
import be.lsm.tfe.common.ResultatAnnuel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CalculateurVAN")
class CalculateurVANTest {

    private static final double DELTA = 1e-6;

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("actualiser()")
    class Actualiser {

        @Test
        @DisplayName("Montant à l'année 0 reste inchangé")
        void annee0_retourneValeurOriginale() {
            assertThat(CalculateurVAN.actualiser(1_000.0, 0.03, 0))
                    .isCloseTo(1_000.0, within(DELTA));
        }

        @Test
        @DisplayName("Actualisation sur 1 an à 3%")
        void annee1_taux3() {
            // 1000 / 1.03 = 970.873786...
            assertThat(CalculateurVAN.actualiser(1_000.0, 0.03, 1))
                    .isCloseTo(970.8737864, within(DELTA));
        }

        @Test
        @DisplayName("Actualisation sur 10 ans à 3%")
        void annee10_taux3() {
            // 1000 / 1.03^10 = 744.093915...
            assertThat(CalculateurVAN.actualiser(1_000.0, 0.03, 10))
                    .isCloseTo(744.0939148, within(DELTA));
        }

        @Test
        @DisplayName("Taux 0% retourne le montant exact")
        void taux0_retourneMontantExact() {
            assertThat(CalculateurVAN.actualiser(5_000.0, 0.0, 20))
                    .isCloseTo(5_000.0, within(DELTA));
        }

        @Test
        @DisplayName("Année négative lève une exception")
        void anneeNegative_leveException() {
            assertThatThrownBy(() -> CalculateurVAN.actualiser(1_000.0, 0.03, -1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("négative");
        }

        @ParameterizedTest(name = "montant={0}, taux={1}, n={2} → {3}")
        @CsvSource({
            "1000, 0.05, 1,  952.380952",
            "1000, 0.05, 2,  907.029478",
            "500,  0.10, 3,  375.657404",
            "0,    0.03, 5,  0.0"
        })
        @DisplayName("Cas paramétrés")
        void casParametres(double montant, double taux, int n, double attendu) {
            assertThat(CalculateurVAN.actualiser(montant, taux, n))
                    .isCloseTo(attendu, within(1e-4));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("vanCapital()")
    class VanCapital {

        @Test
        @DisplayName("VAN capital = actualiser(capital, taux, durée)")
        void correspondAAactualiser() {
            double capital = 50_000.0;
            double taux    = 0.03;
            int    duree   = 30;
            assertThat(CalculateurVAN.vanCapital(capital, taux, duree))
                    .isCloseTo(CalculateurVAN.actualiser(capital, taux, duree), within(DELTA));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("vanEconomiesFiscales()")
    class VanEconomiesFiscales {

        @Test
        @DisplayName("Liste vide retourne 0")
        void listeVide_retourne0() {
            assertThat(CalculateurVAN.vanEconomiesFiscales(List.of(), 0.03))
                    .isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Une seule économie à l'année 0 est actualisée en t+1")
        void uneEconomieAnCero_actualiseeEnT1() {
            // Économie de 300€ générée en t=0, encaissée en t=1 → 300/1.03
            ResultatAnnuel ra = ResultatAnnuel.sansAnticipative(2025, 18, 10_000, 1000, 300.0);
            double attendu = 300.0 / 1.03;
            assertThat(CalculateurVAN.vanEconomiesFiscales(List.of(ra), 0.03))
                    .isCloseTo(attendu, within(1e-4));
        }

        @Test
        @DisplayName("Deux économies sont correctement actualisées avec décalage t+1")
        void deuxEconomies_actualisationCorrect() {
            // t=0 → 300€ encaissés en t=1 : 300/1.03
            // t=1 → 300€ encaissés en t=2 : 300/1.03^2
            ResultatAnnuel r0 = ResultatAnnuel.sansAnticipative(2025, 18, 10_000, 1_000, 300.0);
            ResultatAnnuel r1 = ResultatAnnuel.sansAnticipative(2026, 19, 11_000, 1_000, 300.0);
            double attendu = 300.0 / 1.03 + 300.0 / (1.03 * 1.03);
            assertThat(CalculateurVAN.vanEconomiesFiscales(List.of(r0, r1), 0.03))
                    .isCloseTo(attendu, within(1e-4));
        }

        @Test
        @DisplayName("Économies de 0€ contribuent 0 à la VAN")
        void economiesNulles_contribution0() {
            List<ResultatAnnuel> annees = List.of(
                    ResultatAnnuel.sansAnticipative(2025, 18, 1_000, 1_000, 0.0),
                    ResultatAnnuel.sansAnticipative(2026, 19, 2_000, 1_000, 0.0)
            );
            assertThat(CalculateurVAN.vanEconomiesFiscales(annees, 0.03))
                    .isCloseTo(0.0, within(DELTA));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("vanTotale()")
    class VanTotale {

        @Test
        @DisplayName("vanTotale = vanCapital + vanEconomiesFiscales")
        void sommeDesDeuxComposantes() {
            ResultatAnnuel ra = ResultatAnnuel.sansAnticipative(2025, 18, 50_000, 1_000, 300.0);
            List<ResultatAnnuel> annees = List.of(ra);
            double taux  = 0.03;
            int duree = 1;
            double capital = 50_000.0;

            double vanCap    = CalculateurVAN.vanCapital(capital, taux, duree);
            double vanEco    = CalculateurVAN.vanEconomiesFiscales(annees, taux);
            double attendu   = vanCap + vanEco;

            assertThat(CalculateurVAN.vanTotale(capital, annees, taux, duree))
                    .isCloseTo(attendu, within(DELTA));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("facteurActualisation()")
    class FacteurActualisation {

        @Test
        @DisplayName("Facteur à n=0 vaut 1.0")
        void n0_vaut1() {
            assertThat(CalculateurVAN.facteurActualisation(0.03, 0))
                    .isCloseTo(1.0, within(DELTA));
        }

        @Test
        @DisplayName("Facteur à n=1 taux 3% vaut 1/1.03")
        void n1_taux3() {
            assertThat(CalculateurVAN.facteurActualisation(0.03, 1))
                    .isCloseTo(1.0 / 1.03, within(DELTA));
        }
    }
}
