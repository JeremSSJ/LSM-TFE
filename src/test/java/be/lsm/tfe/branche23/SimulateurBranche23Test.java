package be.lsm.tfe.branche23;


import be.lsm.tfe.common.ParametresRendement;
import be.lsm.tfe.common.ProfilInvestisseur;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulateurBranche23Test {

    //todo tester simulerAnnees

    //todo tester construireResultat

    @Test
    void calculerVersementNet() {
        SimulateurBranche23 simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .taxeOperationsAssurance(0.0)
                .fraisParPrime(0.0)
                .build(),
                "");

        assertEquals(1800, simulateur.calculerVersementNet(1800));

        //todo donc en partant du principe que frais par prime bien après taxe sur opération d assurance
        simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .taxeOperationsAssurance(0.02)
                .fraisParPrime(0.02)
                .build(),
                "");

        assertEquals(1728.72, simulateur.calculerVersementNet(1800));
    }

    @Test
    void capitaliserReserve() {
        SimulateurBranche23 simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .fraisGestionAnnuels(0.0)
                .build(),
                "");

        ParametresRendement rendement = new ParametresRendement(0.07, 0.0);

        assertEquals(107000, simulateur.capitaliserReserve(100000, rendement));

        simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .fraisGestionAnnuels(0.02)
                .build(),
                "");

        assertEquals(104860, simulateur.capitaliserReserve(100000, rendement));
    }

    @Test
    void appliquerTaxeAnticipative() {
        SimulateurBranche23 simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .tauxTaxeAnticipative(0.1)
                .build(),
                "");

        assertEquals(94500.441, simulateur.appliquerTaxeAnticipative(105000.49));
    }

    @Test
    void determinerAnneeAnticipative() {
        SimulateurBranche23 simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .ageTaxeAnticipative(60)
                .build(),
                "");

        //todo est ce qu après 55 ans c est 1 jour après on alors on a jusque 56 ?
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 55, 65);

        assertEquals(2060, simulateur.determinerAnneeAnticipative(profil));

        //todo meme qu avant
        profil = new ProfilInvestisseur("", "", 2000, 56, 65);

        assertEquals(2066, simulateur.determinerAnneeAnticipative(profil));
    }

    @Test
    void calculerEconomieFiscale() {
        SimulateurBranche23 simulateur = new SimulateurBranche23(ParametresBranche23.builder()
                .ageLimiteReductionFiscale(64)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                .build(),
                "");

        assertEquals(0.0, simulateur.calculerEconomieFiscale(17, 1350));

        assertEquals(0.0, simulateur.calculerEconomieFiscale(65, 1350));

        assertEquals(337.5, simulateur.calculerEconomieFiscale(18, 1350));

        //todo meme question qu au dessus
        assertEquals(337.5, simulateur.calculerEconomieFiscale(64, 1350));
    }
}
