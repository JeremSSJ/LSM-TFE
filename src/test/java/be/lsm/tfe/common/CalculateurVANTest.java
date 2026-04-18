package be.lsm.tfe.common;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculateurVANTest {

    @Test
    void vanCapital() {
        assertEquals(6729.713331080575, CalculateurVAN.vanCapital(10000, 0.02, 20));
    }

    @Test
    void vanEconomiesFiscales() {
        List<ResultatAnnuel> resultatsAnnuels = List.of(
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build(),
                ResultatAnnuel.builder().economiesFiscales(337.5).build()
        );

        assertEquals(3031.6224396067546, CalculateurVAN.vanEconomiesFiscales(resultatsAnnuels, 0.02));
    }

    @Test
    void vanEconomiesFiscales_48_elements_150_taux003() {
        double tauxOLO = 0.03;

        List<ResultatAnnuel> resultats = IntStream.range(0, 48)
                .mapToObj(i -> ResultatAnnuel.builder().economiesFiscales(150.0).build())
                .toList();

        double van = CalculateurVAN.vanEconomiesFiscales(resultats, tauxOLO);

        assertEquals(3790.01, van, 1e-2);
    }

    @Test
    void actualiser() {
        assertEquals(6729.713331080575, CalculateurVAN.actualiser(10000, 0.02, 20));
    }
}
