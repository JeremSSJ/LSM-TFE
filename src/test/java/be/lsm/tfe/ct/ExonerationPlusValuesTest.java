package be.lsm.tfe.ct;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExonerationPlusValuesTest {

    @Test
    void calculerTaxe() {
        ExonerationPlusValues exoneration = new ExonerationPlusValues(10000, 1000, 5);

        assertEquals(300, exoneration.calculerTaxe(18000, 0.1, 60));

        assertEquals(0.0, exoneration.calculerTaxe(15000, 0.1, 60));

        assertEquals(0.0, exoneration.calculerTaxe(10000, 0.1, 60));

        assertEquals(400, exoneration.calculerTaxe(18000, 0.1, 4));
    }

    @Test
    void calculerExonerationTotale() {
        ExonerationPlusValues exoneration = new ExonerationPlusValues(10000, 1000, 5);

        assertEquals(15000, exoneration.calculerExonerationTotale(6));

        assertEquals(14000, exoneration.calculerExonerationTotale(4));
    }
}
