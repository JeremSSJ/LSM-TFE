package be.lsm.tfe.branche23;


import be.lsm.tfe.common.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulateurBranche23Test {

    //todo tester simulerAnnees
    @Test
    void simulerAnnees() {
        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Marie", "Dupont",
                2000,
                53,
                64
        );

        double versementAnnuel = 1350.0;

        ParametresRendement rendement = new ParametresRendement(0.15, 0.03 );

        ParametresBranche23 paramsELT = ParametresBranche23.builder()
                .taxeOperationsAssurance(0.02)
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.10)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                .ageLimiteReductionFiscale(64)
                .build();

        SimulateurBranche23 simELT = new SimulateurBranche23(paramsELT, "ELT Branche 23");

/*        List<ResultatAnnuel> resultatAnnuels = simELT.simulerAnnees(profil, versementAnnuel, rendement);

        ResultatAnnuel resultatAnnuel1 = resultatAnnuels.get(0);

        assertEquals(, resultatAnnuel1.anneeCalendaire());
        assertEquals(, resultatAnnuel1.age());
        assertEquals(, resultatAnnuel1.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel1.versementNet());
        assertEquals(, resultatAnnuel1.economiesFiscales());
        assertEquals(, resultatAnnuel1.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel2 = resultatAnnuels.get(1);

        assertEquals(, resultatAnnuel2.anneeCalendaire());
        assertEquals(, resultatAnnuel2.age());
        assertEquals(, resultatAnnuel2.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel2.versementNet());
        assertEquals(, resultatAnnuel2.economiesFiscales());
        assertEquals(, resultatAnnuel2.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel3 = resultatAnnuels.get(2);

        assertEquals(, resultatAnnuel3.anneeCalendaire());
        assertEquals(, resultatAnnuel3.age());
        assertEquals(, resultatAnnuel3.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel3.versementNet());
        assertEquals(, resultatAnnuel3.economiesFiscales());
        assertEquals(, resultatAnnuel3.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel4 = resultatAnnuels.get(3);

        assertEquals(, resultatAnnuel4.anneeCalendaire());
        assertEquals(, resultatAnnuel4.age());
        assertEquals(, resultatAnnuel4.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel4.versementNet());
        assertEquals(, resultatAnnuel4.economiesFiscales());
        assertEquals(, resultatAnnuel4.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel5 = resultatAnnuels.get(4);

        assertEquals(, resultatAnnuel5.anneeCalendaire());
        assertEquals(, resultatAnnuel5.age());
        assertEquals(, resultatAnnuel5.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel5.versementNet());
        assertEquals(, resultatAnnuel5.economiesFiscales());
        assertEquals(, resultatAnnuel5.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel6 = resultatAnnuels.get(5);

        assertEquals(, resultatAnnuel6.anneeCalendaire());
        assertEquals(, resultatAnnuel6.age());
        assertEquals(, resultatAnnuel6.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel6.versementNet());
        assertEquals(, resultatAnnuel6.economiesFiscales());
        assertEquals(, resultatAnnuel6.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel7 = resultatAnnuels.get(6);

        assertEquals(, resultatAnnuel7.anneeCalendaire());
        assertEquals(, resultatAnnuel7.age());
        assertEquals(, resultatAnnuel7.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel7.versementNet());
        assertEquals(, resultatAnnuel7.economiesFiscales());
        assertEquals(, resultatAnnuel7.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel8 = resultatAnnuels.get(7);

        assertEquals(, resultatAnnuel8.anneeCalendaire());
        assertEquals(, resultatAnnuel8.age());
        assertEquals(, resultatAnnuel8.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel8.versementNet());
        assertEquals(, resultatAnnuel8.economiesFiscales());
        assertEquals(, resultatAnnuel8.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel9 = resultatAnnuels.get(8);

        assertEquals(, resultatAnnuel9.anneeCalendaire());
        assertEquals(, resultatAnnuel9.age());
        assertEquals(, resultatAnnuel9.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel9.versementNet());
        assertEquals(, resultatAnnuel9.economiesFiscales());
        assertEquals(, resultatAnnuel9.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel10 = resultatAnnuels.get(9);

        assertEquals(, resultatAnnuel10.anneeCalendaire());
        assertEquals(, resultatAnnuel10.age());
        assertEquals(, resultatAnnuel10.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel10.versementNet());
        assertEquals(, resultatAnnuel10.economiesFiscales());
        assertEquals(, resultatAnnuel10.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel11 = resultatAnnuels.get(10);

        assertEquals(, resultatAnnuel11.anneeCalendaire());
        assertEquals(, resultatAnnuel11.age());
        assertEquals(, resultatAnnuel11.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel11.versementNet());
        assertEquals(, resultatAnnuel11.economiesFiscales());
        assertEquals(, resultatAnnuel11.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel12 = resultatAnnuels.get(11);

        assertEquals(, resultatAnnuel12.anneeCalendaire());
        assertEquals(, resultatAnnuel12.age());
        assertEquals(, resultatAnnuel12.reserveEnFinAnnee());
        assertEquals(, resultatAnnuel12.versementNet());
        assertEquals(, resultatAnnuel12.economiesFiscales());
        assertEquals(, resultatAnnuel12.taxeAnticipativeAppliquee());*/
    }

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
