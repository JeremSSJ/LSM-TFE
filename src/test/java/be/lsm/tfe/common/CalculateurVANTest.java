package be.lsm.tfe.common;


import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculateurVANTest {

    @Test
    void vanCapital() {
        assertEquals(6729.713331080575, CalculateurVAN.vanCapital(10000, 0.02, 20));
    }

    @Test
    void vanEconomiesFiscales() {
        List<ResultatAnnuel> resultatsAnnuels = List.of(
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build(),
                ResultatAnnuel.builder().economiesFiscales(335).build()
        );

        assertEquals(3009.1659770911488, CalculateurVAN.vanEconomiesFiscales(resultatsAnnuels, 0.02));
    }

    @Test
    void actualiser() {
        assertEquals(6729.713331080575, CalculateurVAN.actualiser(10000, 0.02, 20));
    }
}
