package tfe.ct;

import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimulateurCT")
class SimulateurCTTest {

    private ProfilInvestisseur profil;
    private ParametresRendement rendementNul;
    private ParametresRendement rendement5pct;

    @BeforeEach
    void setup() {
        profil        = new ProfilInvestisseur("Test", "User", 1980, 18, 64);
        rendementNul  = new ParametresRendement(0.0, 0.0);
        rendement5pct = new ParametresRendement(0.05, 0.03);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Versement 0€")
    class VersementNul {

        @Test
        @DisplayName("Capital final net = 0")
        void capitalNul() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            assertThat(sim.simuler(profil, 0.0, rendementNul).capitalFinalNet())
                    .isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("VAN totale = 0")
        void vanNulle() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            assertThat(sim.simuler(profil, 0.0, rendementNul).vanTotale())
                    .isCloseTo(0.0, within(1e-9));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("TOB et frais par versement")
    class FraisVersement {

        @Test
        @DisplayName("TOB 0.12% réduit le capital investi")
        void tob_reduitCapital() {
            ParametresCT avecTOB  = ParametresCT.builder().taxeOperationsBourse(0.0012).exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build();
            ParametresCT sansTOB  = ParametresCT.builder().taxeOperationsBourse(0.0).exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build();

            SimulateurCT simAvec  = new SimulateurCT(avecTOB);
            SimulateurCT simSans  = new SimulateurCT(sansTOB);

            double capAvec = simAvec.simuler(profil, 1_000.0, rendementNul).capitalFinal();
            double capSans = simSans.simuler(profil, 1_000.0, rendementNul).capitalFinal();

            assertThat(capAvec).isLessThan(capSans);
        }

        @Test
        @DisplayName("Frais/versement 1% réduit le capital")
        void fraisVersement_reduitCapital() {
            ParametresCT avecFrais = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.01)
                    .exoneration(new ExonerationPlusValues(
                            Constants.CT_EXONERATION_BASE_DEFAUT,
                            Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                            Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT))
                    .build();
            ParametresCT sansFrais = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.0)
                    .exoneration(new ExonerationPlusValues(
                            Constants.CT_EXONERATION_BASE_DEFAUT,
                            Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                            Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT))
                    .build();

            double capAvec = new SimulateurCT(avecFrais).simuler(profil, 1_000.0, rendementNul).capitalFinal();
            double capSans = new SimulateurCT(sansFrais).simuler(profil, 1_000.0, rendementNul).capitalFinal();
            assertThat(capAvec).isLessThan(capSans);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Taxe sur les plus-values")
    class TaxePlusValues {

        @Test
        @DisplayName("Sans rendement et sans frais : réserve = Σ versements nets → PV = 0 → taxe = 0")
        void sansRendement_pasDePV() {
            // Rendement 0%, TOB 0%, frais 0% → versementNet = versementBrut
            // Réserve = Σ versementsNets = coutDeBase → PV = 0 → taxe = 0
            // capitalFinal = capitalFinalNet
            ParametresCT params = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxePlusValues(0.10)
                    .exoneration(new ExonerationPlusValues(0.0, 0.0, 0))
                    .build();

            SimulateurCT sim = new SimulateurCT(params);
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendementNul);

            // Sans rendement et sans frais : reserve = coutDeBase → PV = 0 → taxe = 0
            assertThat(res.capitalFinal()).isCloseTo(res.capitalFinalNet(), within(1e-6));
        }

        @Test
        @DisplayName("Avec rendement, capital net < capital brut car PV taxée")
        void avecRendement_capitalNetInferieur() {
            ParametresCT params = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxePlusValues(0.10)
                    .exoneration(new ExonerationPlusValues(0.0, 0.0, 0))
                    .build();

            SimulateurCT sim = new SimulateurCT(params);
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);

            assertThat(res.capitalFinalNet()).isLessThan(res.capitalFinal());
        }

        @Test
        @DisplayName("Avec TOB élevée : la PV diminue car coutDeBase (versementNet) est aussi réduit")
        void cohérenceDynamique_cobDePlusFraisElevés() {
            // Avec TOB 10% : versementNet = 0.9 × versementBrut
            // coutDeBase = Σ versementsNets → PV = reserve - coutDeBase
            // La PV reflète uniquement la croissance du capital investi, pas les frais d'entrée.
            ParametresCT avecTOB = ParametresCT.builder()
                    .taxeOperationsBourse(0.10)
                    .fraisParVersement(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxePlusValues(0.10)
                    .exoneration(new ExonerationPlusValues(0.0, 0.0, 0))
                    .build();
            ParametresCT sansTOB = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxePlusValues(0.10)
                    .exoneration(new ExonerationPlusValues(0.0, 0.0, 0))
                    .build();

            ResultatSimulation resAvec = new SimulateurCT(avecTOB).simuler(profil, 1_000.0, rendement5pct);
            ResultatSimulation resSans = new SimulateurCT(sansTOB).simuler(profil, 1_000.0, rendement5pct);

            // Avec TOB 10% : versementNet est 10% plus faible, réserve et coutDeBase
            // sont réduits proportionnellement → PV = reserve - coutDeBase reste
            // proportionnelle, et capitalNet < capitalNet sans TOB.
            assertThat(resAvec.capitalFinalNet()).isLessThan(resSans.capitalFinalNet());

            // Vérification de la cohérence : les deux produisent PV > 0 (rendement 5%)
            assertThat(resAvec.capitalFinal()).isGreaterThan(0);
            assertThat(resSans.capitalFinal()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Exonération totale couvre toute la PV → capital net = capital brut")
        void exonerationMaximale_capitalNetEgalBrut() {
            // Exonération énorme pour couvrir toute PV possible
            ExonerationPlusValues exoMax = new ExonerationPlusValues(1_000_000.0, 0.0, 0);
            ParametresCT params = ParametresCT.builder()
                    .taxeOperationsBourse(0.0)
                    .fraisParVersement(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxePlusValues(0.10)
                    .exoneration(exoMax)
                    .build();

            ResultatSimulation res = new SimulateurCT(params).simuler(profil, 1_000.0, rendement5pct);

            assertThat(res.capitalFinalNet()).isCloseTo(res.capitalFinal(), within(1e-4));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Cohérence générale")
    class Coherence {

        @Test
        @DisplayName("vanEconomiesFiscales = 0 pour le CT (pas d'avantage fiscal)")
        void vanEconomiesFiscalesNulle() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.vanEconomiesFiscales()).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("vanTotale = vanCapital pour le CT")
        void vanTotaleEgaleVanCapital() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.vanTotale()).isCloseTo(res.vanCapital(), within(1e-9));
        }

        @Test
        @DisplayName("Capital croît avec le versement (frais constants)")
        void capitalCroissantAvecVersement() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            double cap500  = sim.simuler(profil, 500.0,  rendement5pct).capitalFinalNet();
            double cap1000 = sim.simuler(profil, 1_000.0, rendement5pct).capitalFinalNet();
            assertThat(cap1000).isGreaterThan(cap500);
        }

        @Test
        @DisplayName("Nombre d'années dans le détail = duréeAnnées du profil")
        void nbAnneesEgalDuree() {
            SimulateurCT sim = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.resultatParAnnee()).hasSize(profil.dureeAnnees());
        }

        @Test
        @DisplayName("nomVehicule() retourne 'Compte-Titres'")
        void nomVehicule() {
            assertThat(new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build()).nomVehicule())
                    .isEqualTo("Compte-Titres");
        }
    }
}