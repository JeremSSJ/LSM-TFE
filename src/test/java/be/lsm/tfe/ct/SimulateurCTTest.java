package be.lsm.tfe.ct;

import be.lsm.tfe.common.ParametresRendement;
import be.lsm.tfe.common.ProfilInvestisseur;
import be.lsm.tfe.common.ResultatAnnuel;
import be.lsm.tfe.common.ResultatSimulation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimulateurCTTest {

    @Test
    void simuler() {

        ProfilInvestisseur pi = new ProfilInvestisseur("A", "B", 2000, 18, 65);

        ParametresCT pct = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.02)
                .fraisGestionAnnuels(0.00)
                .tauxTaxePlusValues(0.1)
                .exoneration(new ExonerationPlusValues(10000, 1000, 5))
                .build();

        SimulateurCT simulateurCT = new SimulateurCT(pct);

        ResultatSimulation resultatSimulation = simulateurCT.simuler(pi,
                500,
                new ParametresRendement(0.1));

        assertEquals(500, resultatSimulation.versementAnnuel());
        assertEquals(480_086.17, resultatSimulation.capitalFinal(), 1e-2);
        assertEquals(435_977.55, resultatSimulation.capitalFinalNet(), 1e-2);
        assertEquals(56_615.07, resultatSimulation.vanCapital(), 1e-2);
        assertEquals(0.00, resultatSimulation.vanEconomiesFiscales());
        assertEquals(56_615.07, resultatSimulation.vanTotale(), 1e-2);

        List<ResultatAnnuel> resultatAnnuels = resultatSimulation.resultatParAnnee();

        ResultatAnnuel resultatAnnuel18 = resultatAnnuels.get(0);

        assertEquals(2018, resultatAnnuel18.anneeCalendaire());
        assertEquals(18, resultatAnnuel18.age());
        assertEquals(500, resultatAnnuel18.versementNet());
        assertEquals(false, resultatAnnuel18.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel19 = resultatAnnuels.get(1);

        assertEquals(2019, resultatAnnuel19.anneeCalendaire());
        assertEquals(19, resultatAnnuel19.age());
        assertEquals(500, resultatAnnuel19.versementNet());
        assertEquals(false, resultatAnnuel19.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel20 = resultatAnnuels.get(2);

        assertEquals(2020, resultatAnnuel20.anneeCalendaire());
        assertEquals(20, resultatAnnuel20.age());
        assertEquals(500, resultatAnnuel20.versementNet());
        assertEquals(false, resultatAnnuel20.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel21 = resultatAnnuels.get(3);

        assertEquals(2021, resultatAnnuel21.anneeCalendaire());
        assertEquals(21, resultatAnnuel21.age());
        assertEquals(500, resultatAnnuel21.versementNet());
        assertEquals(false, resultatAnnuel21.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel22 = resultatAnnuels.get(4);

        assertEquals(2022, resultatAnnuel22.anneeCalendaire());
        assertEquals(22, resultatAnnuel22.age());
        assertEquals(500, resultatAnnuel22.versementNet());
        assertEquals(false, resultatAnnuel22.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel23 = resultatAnnuels.get(5);

        assertEquals(2023, resultatAnnuel23.anneeCalendaire());
        assertEquals(23, resultatAnnuel23.age());
        assertEquals(500, resultatAnnuel23.versementNet());
        assertEquals(false, resultatAnnuel23.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel24 = resultatAnnuels.get(6);

        assertEquals(2024, resultatAnnuel24.anneeCalendaire());
        assertEquals(24, resultatAnnuel24.age());
        assertEquals(500, resultatAnnuel24.versementNet());
        assertEquals(false, resultatAnnuel24.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel25 = resultatAnnuels.get(7);

        assertEquals(2025, resultatAnnuel25.anneeCalendaire());
        assertEquals(25, resultatAnnuel25.age());
        assertEquals(500, resultatAnnuel25.versementNet());
        assertEquals(false, resultatAnnuel25.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel26 = resultatAnnuels.get(8);

        assertEquals(2026, resultatAnnuel26.anneeCalendaire());
        assertEquals(26, resultatAnnuel26.age());
        assertEquals(500, resultatAnnuel26.versementNet());
        assertEquals(false, resultatAnnuel26.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel27 = resultatAnnuels.get(9);

        assertEquals(2027, resultatAnnuel27.anneeCalendaire());
        assertEquals(27, resultatAnnuel27.age());
        assertEquals(500, resultatAnnuel27.versementNet());
        assertEquals(false, resultatAnnuel27.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel28 = resultatAnnuels.get(10);

        assertEquals(2028, resultatAnnuel28.anneeCalendaire());
        assertEquals(28, resultatAnnuel28.age());
        assertEquals(500, resultatAnnuel28.versementNet());
        assertEquals(false, resultatAnnuel28.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel29 = resultatAnnuels.get(11);

        assertEquals(2029, resultatAnnuel29.anneeCalendaire());
        assertEquals(29, resultatAnnuel29.age());
        assertEquals(500, resultatAnnuel29.versementNet());
        assertEquals(false, resultatAnnuel29.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel30 = resultatAnnuels.get(12);

        assertEquals(2030, resultatAnnuel30.anneeCalendaire());
        assertEquals(30, resultatAnnuel30.age());
        assertEquals(500, resultatAnnuel30.versementNet());
        assertEquals(false, resultatAnnuel30.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel31 = resultatAnnuels.get(13);

        assertEquals(2031, resultatAnnuel31.anneeCalendaire());
        assertEquals(31, resultatAnnuel31.age());
        assertEquals(500, resultatAnnuel31.versementNet());
        assertEquals(false, resultatAnnuel31.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel32 = resultatAnnuels.get(14);

        assertEquals(2032, resultatAnnuel32.anneeCalendaire());
        assertEquals(32, resultatAnnuel32.age());
        assertEquals(500, resultatAnnuel32.versementNet());
        assertEquals(false, resultatAnnuel32.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel33 = resultatAnnuels.get(15);

        assertEquals(2033, resultatAnnuel33.anneeCalendaire());
        assertEquals(33, resultatAnnuel33.age());
        assertEquals(500, resultatAnnuel33.versementNet());
        assertEquals(false, resultatAnnuel33.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel34 = resultatAnnuels.get(16);

        assertEquals(2034, resultatAnnuel34.anneeCalendaire());
        assertEquals(34, resultatAnnuel34.age());
        assertEquals(500, resultatAnnuel34.versementNet());
        assertEquals(false, resultatAnnuel34.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel35 = resultatAnnuels.get(17);

        assertEquals(2035, resultatAnnuel35.anneeCalendaire());
        assertEquals(35, resultatAnnuel35.age());
        assertEquals(500, resultatAnnuel35.versementNet());
        assertEquals(false, resultatAnnuel35.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel36 = resultatAnnuels.get(18);

        assertEquals(2036, resultatAnnuel36.anneeCalendaire());
        assertEquals(36, resultatAnnuel36.age());
        assertEquals(500, resultatAnnuel36.versementNet());
        assertEquals(false, resultatAnnuel36.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel37 = resultatAnnuels.get(19);

        assertEquals(2037, resultatAnnuel37.anneeCalendaire());
        assertEquals(37, resultatAnnuel37.age());
        assertEquals(500, resultatAnnuel37.versementNet());
        assertEquals(false, resultatAnnuel37.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel38 = resultatAnnuels.get(20);

        assertEquals(2038, resultatAnnuel38.anneeCalendaire());
        assertEquals(38, resultatAnnuel38.age());
        assertEquals(500, resultatAnnuel38.versementNet());
        assertEquals(false, resultatAnnuel38.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel39 = resultatAnnuels.get(21);

        assertEquals(2039, resultatAnnuel39.anneeCalendaire());
        assertEquals(39, resultatAnnuel39.age());
        assertEquals(500, resultatAnnuel39.versementNet());
        assertEquals(false, resultatAnnuel39.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel40 = resultatAnnuels.get(22);

        assertEquals(2040, resultatAnnuel40.anneeCalendaire());
        assertEquals(40, resultatAnnuel40.age());
        assertEquals(500, resultatAnnuel40.versementNet());
        assertEquals(false, resultatAnnuel40.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel41 = resultatAnnuels.get(23);

        assertEquals(2041, resultatAnnuel41.anneeCalendaire());
        assertEquals(41, resultatAnnuel41.age());
        assertEquals(500, resultatAnnuel41.versementNet());
        assertEquals(false, resultatAnnuel41.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel42 = resultatAnnuels.get(24);

        assertEquals(2042, resultatAnnuel42.anneeCalendaire());
        assertEquals(42, resultatAnnuel42.age());
        assertEquals(500, resultatAnnuel42.versementNet());
        assertEquals(false, resultatAnnuel42.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel43 = resultatAnnuels.get(25);

        assertEquals(2043, resultatAnnuel43.anneeCalendaire());
        assertEquals(43, resultatAnnuel43.age());
        assertEquals(500, resultatAnnuel43.versementNet());
        assertEquals(false, resultatAnnuel43.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel44 = resultatAnnuels.get(26);

        assertEquals(2044, resultatAnnuel44.anneeCalendaire());
        assertEquals(44, resultatAnnuel44.age());
        assertEquals(500, resultatAnnuel44.versementNet());
        assertEquals(false, resultatAnnuel44.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel45 = resultatAnnuels.get(27);

        assertEquals(2045, resultatAnnuel45.anneeCalendaire());
        assertEquals(45, resultatAnnuel45.age());
        assertEquals(500, resultatAnnuel45.versementNet());
        assertEquals(false, resultatAnnuel45.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel46 = resultatAnnuels.get(28);

        assertEquals(2046, resultatAnnuel46.anneeCalendaire());
        assertEquals(46, resultatAnnuel46.age());
        assertEquals(500, resultatAnnuel46.versementNet());
        assertEquals(false, resultatAnnuel46.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel47 = resultatAnnuels.get(29);

        assertEquals(2047, resultatAnnuel47.anneeCalendaire());
        assertEquals(47, resultatAnnuel47.age());
        assertEquals(500, resultatAnnuel47.versementNet());
        assertEquals(false, resultatAnnuel47.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel48 = resultatAnnuels.get(30);

        assertEquals(2048, resultatAnnuel48.anneeCalendaire());
        assertEquals(48, resultatAnnuel48.age());
        assertEquals(500, resultatAnnuel48.versementNet());
        assertEquals(false, resultatAnnuel48.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel49 = resultatAnnuels.get(31);

        assertEquals(2049, resultatAnnuel49.anneeCalendaire());
        assertEquals(49, resultatAnnuel49.age());
        assertEquals(500, resultatAnnuel49.versementNet());
        assertEquals(false, resultatAnnuel49.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel50 = resultatAnnuels.get(32);

        assertEquals(2050, resultatAnnuel50.anneeCalendaire());
        assertEquals(50, resultatAnnuel50.age());
        assertEquals(500, resultatAnnuel50.versementNet());
        assertEquals(false, resultatAnnuel50.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel51 = resultatAnnuels.get(33);

        assertEquals(2051, resultatAnnuel51.anneeCalendaire());
        assertEquals(51, resultatAnnuel51.age());
        assertEquals(500, resultatAnnuel51.versementNet());
        assertEquals(false, resultatAnnuel51.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel52 = resultatAnnuels.get(34);

        assertEquals(2052, resultatAnnuel52.anneeCalendaire());
        assertEquals(52, resultatAnnuel52.age());
        assertEquals(500, resultatAnnuel52.versementNet());
        assertEquals(false, resultatAnnuel52.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel53 = resultatAnnuels.get(35);

        assertEquals(2053, resultatAnnuel53.anneeCalendaire());
        assertEquals(53, resultatAnnuel53.age());
        assertEquals(500, resultatAnnuel53.versementNet());
        assertEquals(false, resultatAnnuel53.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel54 = resultatAnnuels.get(36);

        assertEquals(2054, resultatAnnuel54.anneeCalendaire());
        assertEquals(54, resultatAnnuel54.age());
        assertEquals(500, resultatAnnuel54.versementNet());
        assertEquals(false, resultatAnnuel54.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel55 = resultatAnnuels.get(37);

        assertEquals(2055, resultatAnnuel55.anneeCalendaire());
        assertEquals(55, resultatAnnuel55.age());
        assertEquals(500, resultatAnnuel55.versementNet());
        assertEquals(false, resultatAnnuel55.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel56 = resultatAnnuels.get(38);

        assertEquals(2056, resultatAnnuel56.anneeCalendaire());
        assertEquals(56, resultatAnnuel56.age());
        assertEquals(500, resultatAnnuel56.versementNet());
        assertEquals(false, resultatAnnuel56.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel57 = resultatAnnuels.get(39);

        assertEquals(2057, resultatAnnuel57.anneeCalendaire());
        assertEquals(57, resultatAnnuel57.age());
        assertEquals(500, resultatAnnuel57.versementNet());
        assertEquals(false, resultatAnnuel57.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel58 = resultatAnnuels.get(40);

        assertEquals(2058, resultatAnnuel58.anneeCalendaire());
        assertEquals(58, resultatAnnuel58.age());
        assertEquals(500, resultatAnnuel58.versementNet());
        assertEquals(false, resultatAnnuel58.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel59 = resultatAnnuels.get(41);

        assertEquals(2059, resultatAnnuel59.anneeCalendaire());
        assertEquals(59, resultatAnnuel59.age());
        assertEquals(500, resultatAnnuel59.versementNet());
        assertEquals(false, resultatAnnuel59.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel60 = resultatAnnuels.get(42);

        assertEquals(2060, resultatAnnuel60.anneeCalendaire());
        assertEquals(60, resultatAnnuel60.age());
        assertEquals(500, resultatAnnuel60.versementNet());
        assertEquals(false, resultatAnnuel60.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel61 = resultatAnnuels.get(43);

        assertEquals(2061, resultatAnnuel61.anneeCalendaire());
        assertEquals(61, resultatAnnuel61.age());
        assertEquals(500, resultatAnnuel61.versementNet());
        assertEquals(false, resultatAnnuel61.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel62 = resultatAnnuels.get(44);

        assertEquals(2062, resultatAnnuel62.anneeCalendaire());
        assertEquals(62, resultatAnnuel62.age());
        assertEquals(500, resultatAnnuel62.versementNet());
        assertEquals(false, resultatAnnuel62.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel63 = resultatAnnuels.get(45);

        assertEquals(2063, resultatAnnuel63.anneeCalendaire());
        assertEquals(63, resultatAnnuel63.age());
        assertEquals(500, resultatAnnuel63.versementNet());
        assertEquals(false, resultatAnnuel63.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel64 = resultatAnnuels.get(46);

        assertEquals(2064, resultatAnnuel64.anneeCalendaire());
        assertEquals(64, resultatAnnuel64.age());
        assertEquals(500, resultatAnnuel64.versementNet());
        assertEquals(false, resultatAnnuel64.taxeAnticipativeAppliquee());

        ResultatAnnuel resultatAnnuel65 = resultatAnnuels.get(47);

        assertEquals(2065, resultatAnnuel65.anneeCalendaire());
        assertEquals(65, resultatAnnuel65.age());
        assertEquals(500, resultatAnnuel65.versementNet());
        assertEquals(false, resultatAnnuel65.taxeAnticipativeAppliquee());
    }

    @Test
    void capitaliserReserve() {
        SimulateurCT simulateur = new SimulateurCT(ParametresCT.builder()
                .fraisGestionAnnuels(0.0)
                .build());

        ParametresRendement rendement = new ParametresRendement(0.07);

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

        assertEquals(139500, simulateur.calculerCapitalNet(150000, 30000, 50));

        assertEquals(150000, simulateur.calculerCapitalNet(150000, 150001, 50));
    }

    @Test
    void calculerTaxeCompteTitres() {
        SimulateurCT simulateur = new SimulateurCT(ParametresCT.builder().build());

        assertEquals(0.0, simulateur.calculerTaxeCompteTitres(999_999.99));
        assertEquals(1_000.0, simulateur.calculerTaxeCompteTitres(1_010_000), 1e-6);
        assertEquals(1500, simulateur.calculerTaxeCompteTitres(1_015_000), 1e-6);
        assertEquals(1_522.842, simulateur.calculerTaxeCompteTitres(1_015_228.42), 1e-6);
        assertEquals(1_800.0, simulateur.calculerTaxeCompteTitres(1_200_000), 1e-6);
    }

}