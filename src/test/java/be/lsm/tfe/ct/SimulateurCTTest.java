package be.lsm.tfe.ct;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.ParametresRendement;
import be.lsm.tfe.common.ProfilInvestisseur;
import be.lsm.tfe.common.ResultatAnnuel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

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

    public static Stream<Arguments> provideSimulerAnneesScenarios() {
        return Stream.of(
                arguments(
                        "deux ans sans frais ni rendement",
                        new ProfilInvestisseur("A", "B", 2000, 18, 19),
                        new ParametresCT(0.0, 0.0, 0.0, 0.0, new ExonerationPlusValues(0.0,0.0,0)),
                        new ParametresRendement(0.0, 0.0),
                        1000.0
                ),
                arguments(
                        "trois ans avec frais et rendement",
                        new ProfilInvestisseur("C", "D", 1990, 30, 32),
                        new ParametresCT(0.01, 0.02, 0.01, 0.0, new ExonerationPlusValues(1000.0,100.0,5)),
                        new ParametresRendement(0.05, 0.01),
                        2000.0
                ),
                arguments(
                        "aucun versement",
                        new ProfilInvestisseur("E", "F", 1980, 40, 42),
                        new ParametresCT(0.02, 0.03, 0.02, 0.0, new ExonerationPlusValues(0.0,0.0,0)),
                        new ParametresRendement(0.07, 0.02),
                        0.0
                )
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("provideSimulerAnneesScenarios")
    void simulerAnnees_parameterized(String desc,
                                     ProfilInvestisseur profil,
                                     ParametresCT params,
                                     ParametresRendement rendement,
                                     double versementAnnuel) {

        SimulateurCT simulateur = new SimulateurCT(params);
        SimulateurCT.AccumulationResult accumulation = simulateur.simulerAnnees(profil, versementAnnuel, rendement);

        int expectedYears = profil.anneeFinVersements() - profil.anneeDebutVersements() + 1;

        double taxeOps = params.taxeOperationsBourse();
        double fraisVerse = params.fraisParVersement();
        double fraisGest = params.fraisGestionAnnuels();

        double versementNet = versementAnnuel * (1.0 - taxeOps) * (1.0 - fraisVerse);
        double expectedCoutDeBase = versementNet * expectedYears;

        double reserve = 0.0;
        double[] expectedPerYear = new double[expectedYears];
        for (int i = 0; i < expectedYears; i++) {
            reserve += versementNet;
            reserve = reserve * (1.0 + rendement.rendementAnnuel()) * (1.0 - fraisGest);
            expectedPerYear[i] = reserve;
        }

        double expectedFinalReserve = expectedPerYear[expectedYears - 1];

        double delta = 1e-6;

        assertEquals(expectedYears, accumulation.annees().size());
        assertEquals(expectedCoutDeBase, accumulation.coutDeBase(), delta);
        assertEquals(expectedFinalReserve, accumulation.reserve(), delta);

        List<ResultatAnnuel> entries = accumulation.annees();
        for (int i = 0; i < entries.size(); i++) {
            ResultatAnnuel ra = entries.get(i);
            assertEquals(expectedPerYear[i], ra.reserveEnFinAnnee(), delta);
            assertEquals(versementNet, ra.versementNet(), delta);
        }
    }

}