package be.lsm.tfe.common;


import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculateurVANTest {

    @Test
    void vanCapital() {
        assertEquals(4383.409878895794, CalculateurVAN.vanCapital(10000, 20), 1e-12);
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

        assertEquals(2841.36095854961, CalculateurVAN.vanEconomiesFiscales(resultatsAnnuels), 1e-12);
    }

    @Test
    void vanEconomiesFiscales_48_elements_150_taux003() {
        List<ResultatAnnuel> resultats = IntStream.range(0, 48)
                .mapToObj(i -> ResultatAnnuel.builder().economiesFiscales(150.0).build())
                .toList();

        double van = CalculateurVAN.vanEconomiesFiscales(resultats);

        assertEquals(3092.35, van, 1e-2);
    }

    @Test
    void actualiser() {
        assertEquals(4383.409878895794, CalculateurVAN.actualiser(10000, 20), 1e-12);
    }

    @Test
    void actualiser_auDelaDe30Ans_utiliseLeTaux30Ans() {
        double attendu = 10_000 / Math.pow(1.0 + oloReferential.tauxPourDuree(30), 31);

        assertEquals(attendu, CalculateurVAN.actualiser(10_000, 31), 1e-12);
    }
}
