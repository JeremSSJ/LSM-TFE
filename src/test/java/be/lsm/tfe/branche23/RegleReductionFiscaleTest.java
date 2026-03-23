package be.lsm.tfe.branche23;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RegleReductionFiscaleTest {

    @Test
    void pourEpargnePension() {
        RegleReductionFiscale regle = RegleReductionFiscale.pourEpargnePension();

        assertEquals(0.0, regle.calculer(0.0));

        assertEquals(314.997, regle.calculer(1049.99));

        assertEquals(315, regle.calculer(1050));

        assertEquals(262.525, regle.calculer(1050.1));

        assertEquals(337.5, regle.calculer(1350));

        assertEquals(337.5, regle.calculer(1350.1));
    }

    @Test
    void pourEpargneLongTerme() {
        RegleReductionFiscale regle = RegleReductionFiscale.pourEpargneLongTerme();

        assertEquals(0.0, regle.calculer(0.0));

        assertEquals(734.997, regle.calculer(2449.99));

        assertEquals(735, regle.calculer(2450));

        assertEquals(735, regle.calculer(2450.1));
    }
}
