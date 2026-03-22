package tfe.ct;

import be.lsm.tfe.common.Constants;
import be.lsm.tfe.ct.ExonerationPlusValues;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ExonerationPlusValues")
class ExonerationPlusValuesTest {

    private static final double DELTA = 1e-6;

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculerExonerationTotale()")
    class ExonerationTotale {

        public static ExonerationPlusValues defaut() {
            return new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT);
        }

        @Test
        @DisplayName("0 année → exonération = base seule (10 000€)")
        void aucuneAnnee_exonerationBase() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerExonerationTotale(0)).isCloseTo(10_000.0, within(DELTA));
        }

        @Test
        @DisplayName("3 années → 10 000 + 3 × 1 000 = 13 000€")
        void troisAnnees_13000() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerExonerationTotale(3)).isCloseTo(13_000.0, within(DELTA));
        }

        @Test
        @DisplayName("5 années (= max) → 10 000 + 5 × 1 000 = 15 000€")
        void cinqAnnees_15000_maximum() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerExonerationTotale(5)).isCloseTo(15_000.0, within(DELTA));
        }

        @Test
        @DisplayName("10 années (> max 5) → plafonnée à 15 000€")
        void dixAnnees_plafonneeA15000() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerExonerationTotale(10)).isCloseTo(15_000.0, within(DELTA));
        }

        @Test
        @DisplayName("Configuration personnalisée respectée")
        void configurationPersonnalisee() {
            // base = 5 000, annuelle = 500, max = 3 → max total = 6 500
            ExonerationPlusValues e = new ExonerationPlusValues(5_000.0, 500.0, 3);
            assertThat(e.calculerExonerationTotale(3)).isCloseTo(6_500.0, within(DELTA));
            assertThat(e.calculerExonerationTotale(10)).isCloseTo(6_500.0, within(DELTA));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("calculerTaxe()")
    class CalculerTaxe {

        public static ExonerationPlusValues defaut() {
            return new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT);
        }

        @Test
        @DisplayName("Plus-value sous l'exonération → taxe 0")
        void pvSousExoneration_taxe0() {
            ExonerationPlusValues e = defaut(); // max 15 000
            // PV = 5 000 < 15 000 → 0 taxe
            assertThat(e.calculerTaxe(5_000.0, 0.10, 10)).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Plus-value exactement égale à l'exonération → taxe 0")
        void pvEgaleExoneration_taxe0() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerTaxe(15_000.0, 0.10, 10)).isCloseTo(0.0, within(DELTA));
        }

        @Test
        @DisplayName("Plus-value de 25 000€, exonération 15 000€ → 10% × 10 000 = 1 000€")
        void pv25000_exo15000_taxe1000() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerTaxe(25_000.0, 0.10, 10)).isCloseTo(1_000.0, within(DELTA));
        }

        @Test
        @DisplayName("Plus-value négative → taxe 0 (protection Math.max)")
        void pvNegative_taxe0() {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerTaxe(-5_000.0, 0.10, 5)).isCloseTo(0.0, within(DELTA));
        }

        @ParameterizedTest(name = "PV={0}, taux={1}, duree={2} → taxe={3}")
        @CsvSource({
            "20000, 0.10, 10, 500.0",    // 20000 - 15000 = 5000 × 10% = 500
            "10000, 0.10,  0, 0.0",      // 10000 - 10000 = 0 (0 ans cumul)
            "15000, 0.15,  5, 0.0",      // 15000 - 15000 = 0
            "30000, 0.10,  5, 1500.0"    // 30000 - 15000 = 15000 × 10% = 1500
        })
        @DisplayName("Cas paramétrés")
        void casParametres(double pv, double taux, int duree, double attendu) {
            ExonerationPlusValues e = defaut();
            assertThat(e.calculerTaxe(pv, taux, duree)).isCloseTo(attendu, within(DELTA));
        }
    }
}
