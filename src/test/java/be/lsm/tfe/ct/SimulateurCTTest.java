package be.lsm.tfe.ct;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.ParametresRendement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulateurCTTest {

    @Test
    void calculerVersementNet() {
        SimulateurCT simulateur = new SimulateurCT(ParametresCT.builder()
                .taxeOperationsBourse(0.0)
                .fraisParVersement(0.0)
                .build());

        assertEquals(1800, simulateur.calculerVersementNet(1800));

        //todo ici je dois savoir quelle hypothèse garder pour la tob, prélèvement à la source et avant autre frais ???
        simulateur = new SimulateurCT(ParametresCT.builder()
                .taxeOperationsBourse(0.02)
                .fraisParVersement(0.02)
                .build());

        assertEquals(1728.72, simulateur.calculerVersementNet(1800));
    }

    @Test
    void capitaliserReserve() {
        SimulateurCT simulateur = new SimulateurCT(ParametresCT.builder()
                .fraisGestionAnnuels(0.0)
                .build());

        ParametresRendement rendement = new ParametresRendement(0.07, 0.0);

        assertEquals(107000, simulateur.capitaliserReserve(100000, rendement));

        simulateur = new SimulateurCT(ParametresCT.builder()
                .fraisGestionAnnuels(0.02)
                .build());

        assertEquals(104860, simulateur.capitaliserReserve(100000, rendement));
    }

    @Test
    void calculerCapitalNet() {
        SimulateurCT simulateur = new SimulateurCT(ParametresCT.builder()
                .exoneration(new ExonerationPlusValues(10000, 1000, 5))
                .tauxTaxePlusValues(0.1)
                .build());

        assertEquals(139500 , simulateur.calculerCapitalNet(150000, 30000, 50));

        assertEquals(150000 , simulateur.calculerCapitalNet(150000, 150001, 50));
    }

}