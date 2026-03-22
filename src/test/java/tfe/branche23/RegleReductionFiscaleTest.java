package tfe.branche23;

import be.lsm.tfe.branche23.RegleReductionFiscale;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RegleReductionFiscale")
class RegleReductionFiscaleTest {

    private static final double DELTA = 1e-4;

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("pourEpargnePension()")
    class EpargnePension {

        private final RegleReductionFiscale regle = RegleReductionFiscale.pourEpargnePension();

        @Test
        @DisplayName("Prime 0€ → réduction 0€")
        void prime0_reduction0() {
            assertThat(regle.calculer(0.0)).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime négative → réduction 0€")
        void primeNegative_reduction0() {
            assertThat(regle.calculer(-100.0)).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime 1 000€ (≤ 1 050€) → 30% = 300€")
        void prime1000_taux30() {
            assertThat(regle.calculer(1_000.0)).isCloseTo(300.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime exactement 1 050€ → 30% = 315€ (maximum tranche basse)")
        void prime1050_max315() {
            assertThat(regle.calculer(1_050.0)).isCloseTo(315.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime 1 051€ (> 1 050€) → 25% sur totalité ≤ 1 350€")
        void prime1051_basculeVers25() {
            // 1051 × 0.25 = 262.75
            assertThat(regle.calculer(1_051.0)).isCloseTo(262.75, within(DELTA));
        }

        @Test
        @DisplayName("Prime 1 200€ → 25% = 300€")
        void prime1200_taux25() {
            assertThat(regle.calculer(1_200.0)).isCloseTo(300.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime exactement 1 350€ → 25% × 1 350 = 337.50€ (maximum)")
        void prime1350_max33750() {
            assertThat(regle.calculer(1_350.0)).isCloseTo(337.50, within(DELTA));
        }

        @Test
        @DisplayName("Prime 2 000€ (> 1 350€) → plafonnée à 1 350€ → 25% × 1 350 = 337.50€")
        void primeDepassePlafond_plafonneA1350() {
            assertThat(regle.calculer(2_000.0)).isCloseTo(337.50, within(DELTA));
        }

        @ParameterizedTest(name = "prime={0}€ → réduction={1}€")
        @CsvSource({
            "500,   150.0",
            "1050,  315.0",
            "1100,  275.0",
            "1350,  337.5",
            "1500,  337.5"
        })
        @DisplayName("Cas paramétrés EP")
        void casParametres(double prime, double attendu) {
            assertThat(regle.calculer(prime)).isCloseTo(attendu, within(DELTA));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("pourEpargneLongTerme()")
    class EpargneLongTerme {

        private final RegleReductionFiscale regle = RegleReductionFiscale.pourEpargneLongTerme();

        @Test
        @DisplayName("Prime 0€ → réduction 0€")
        void prime0_reduction0() {
            assertThat(regle.calculer(0.0)).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime 1 000€ → 30% = 300€")
        void prime1000_taux30() {
            assertThat(regle.calculer(1_000.0)).isCloseTo(300.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime exactement 2 450€ → 30% × 2 450 = 735€ (maximum)")
        void prime2450_max735() {
            assertThat(regle.calculer(2_450.0)).isCloseTo(735.0, within(DELTA));
        }

        @Test
        @DisplayName("Prime 3 000€ (> 2 450€) → plafonnée → 735€")
        void primeSurPlafond_plafonneeA2450() {
            assertThat(regle.calculer(3_000.0)).isCloseTo(735.0, within(DELTA));
        }

        @Test
        @DisplayName("ELT plus généreux qu'EP pour prime entre 1 050€ et 1 350€")
        void eltPlusGenereuQeEP_dansFourchette() {
            RegleReductionFiscale ep  = RegleReductionFiscale.pourEpargnePension();
            RegleReductionFiscale elt = RegleReductionFiscale.pourEpargneLongTerme();
            double prime = 1_200.0;
            // ELT: 1200 × 30% = 360   EP: 1200 × 25% = 300
            assertThat(elt.calculer(prime)).isGreaterThan(ep.calculer(prime));
        }
    }
}
