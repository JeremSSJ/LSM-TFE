package tfe.scenarios;

import be.lsm.tfe.branche23.*;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.*;
import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests d'intégration : vérifient le comportement bout-en-bout
 * des deux scénarios (EP vs CT et ELT vs CT).
 *
 * Ces tests ne portent pas sur des valeurs mathématiques précises
 * mais sur des propriétés économiques invariantes.
 */
@DisplayName("Intégration — Scénarios EP/ELT vs CT")
class IntegrationEPvsCTTest {
    public static ParametresBranche23 defautEpargnePension() {
        return ParametresBranche23.builder()
                .taxeOperationsAssurance(0.0)          // EP : 0% — aucune taxe sur primes (légal belge)
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(Constants.EP_TAXE_ANTICIPATIVE)
                .ageTaxeAnticipative(Constants.EP_AGE_TAXE_ANTICIPATIVE)
                .dureeMinAvantAnticipativeSiSouscritTard(Constants.EP_ANNEES_CONTRAT_MIN)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                .ageLimiteReductionFiscale(Constants.EP_AGE_LIMITE_CONTRIBUTIONS)
                .build();
    }
    private ProfilInvestisseur profil;
    private ParametresRendement rendement;

    @BeforeEach
    void setup() {
        profil    = new ProfilInvestisseur("Test", "User", 1980, 18, 64);
        rendement = new ParametresRendement(0.07);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Scénario EP vs CT")
    class EPvsCT {

        private Simulateur simEP;
        private Simulateur simCT;

        @BeforeEach
        void setupSimulateurs() {
            simEP = new SimulateurBranche23(defautEpargnePension(), "EP");
            simCT = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());
        }

        @Test
        @DisplayName("Simulation produit 2001 résultats pour plage 0–2000€")
        void plageDe2001Resultats() {
            List<ResultatSimulation> res = ComparateurVehicules.simulerPlage(
                    simEP, profil, rendement, 0, 2_000);
            assertThat(res).hasSize(2_001);
        }

        @Test
        @DisplayName("Valeur terminale EP et CT sont toutes positives pour versement > 0")
        void vanPositivesPourVersementPositif() {
            List<ResultatSimulation> resEP = ComparateurVehicules.simulerPlage(
                    simEP, profil, rendement, 1, 2_000);
            List<ResultatSimulation> resCT = ComparateurVehicules.simulerPlage(
                    simCT, profil, rendement, 1, 2_000);

            assertThat(resEP).allMatch(r -> r.valeurTerminale() > 0, "VT EP > 0");
            assertThat(resCT).allMatch(r -> r.valeurTerminale() > 0, "VT CT > 0");
        }

        @Test
        @DisplayName("Valeur terminale EP > CT pour versement ≤ 1 050€ (avantage fiscal EP dominant)")
        void epPlusVantageuxSousSeuilFiscal() {
            // Pour les petits versements (≤ 1 050€), la réduction 30% EP devrait
            // compenser la TOA et taxe anticipative (dans les paramètres défaut 0%)
            List<ResultatSimulation> resEP = ComparateurVehicules.simulerPlage(
                    simEP, profil, rendement, 500, 1_000);
            List<ResultatSimulation> resCT = ComparateurVehicules.simulerPlage(
                    simCT, profil, rendement, 500, 1_000);

            for (int i = 0; i < resEP.size(); i++) {
                assertThat(resEP.get(i).valeurTerminale())
                        .as("EP doit être > CT pour versement %.0f€",
                                resEP.get(i).versementAnnuel())
                        .isGreaterThan(resCT.get(i).valeurTerminale());
            }
        }

        @Test
        @DisplayName("L'ajout de frais annuels EP réduit la valeur terminale EP")
        void fraisAnnuelsEP_reduisent_vanEP() {
            ParametresBranche23 sansFreais = defautEpargnePension();
            ParametresBranche23 avecFrais  = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.02)  // 2%/an
                    .tauxTaxeAnticipative(0.08)
                    .ageTaxeAnticipative(60)
                    .dureeMinAvantAnticipativeSiSouscritTard(10)
                    .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                    .ageLimiteReductionFiscale(65)
                    .build();

            double vtSans = new SimulateurBranche23(sansFreais, "EP")
                    .simuler(profil, 1_000.0, rendement).valeurTerminale();
            double vtAvec = new SimulateurBranche23(avecFrais, "EP avec frais")
                    .simuler(profil, 1_000.0, rendement).valeurTerminale();

            assertThat(vtAvec).isLessThan(vtSans);
        }

        @Test
        @DisplayName("Les croisements sont dans la plage [versementMin, versementMax]")
        void croisementsInRange() {
            List<ResultatSimulation> resEP = ComparateurVehicules.simulerPlage(
                    simEP, profil, rendement, 0, 2_000);
            List<ResultatSimulation> resCT = ComparateurVehicules.simulerPlage(
                    simCT, profil, rendement, 0, 2_000);

            List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                    resEP, resCT);

            croisements.forEach(c ->
                    assertThat(c.versementEuros())
                            .isBetween(0.0, 2_001.0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Scénario ELT vs CT")
    class ELTvsCT {

        public static ParametresBranche23 defautEpargneLongTerme() {
            return ParametresBranche23.builder()
                    .taxeOperationsAssurance(Constants.ELT_TAXE_ASSURANCE)   // 2% fixe
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(Constants.ELT_TAXE_ANTICIPATIVE)
                    .ageTaxeAnticipative(Constants.EP_AGE_TAXE_ANTICIPATIVE)  // aussi à 60 ans
                    .dureeMinAvantAnticipativeSiSouscritTard(Constants.EP_ANNEES_CONTRAT_MIN)
                    .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                    .ageLimiteReductionFiscale(Constants.AGE_FIN_HORIZON)
                    .build();
        }


        @Test
        @DisplayName("ELT avec TOA 2% a une valeur terminale inférieure à ELT sans TOA (toutes choses égales)")
        void toa2pct_reduit_vanELT() {
            ParametresBranche23 avecTOA = defautEpargneLongTerme();  // TOA 2%
            ParametresBranche23 sansTOA = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)   // sans TOA pour comparaison
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(Constants.ELT_TAXE_ANTICIPATIVE)
                    .ageTaxeAnticipative(60)
                    .dureeMinAvantAnticipativeSiSouscritTard(10)
                    .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                    .ageLimiteReductionFiscale(65)
                    .build();

            double vtAvec = new SimulateurBranche23(avecTOA, "ELT").simuler(profil, 1_000.0, rendement).valeurTerminale();
            double vtSans = new SimulateurBranche23(sansTOA, "ELT0").simuler(profil, 1_000.0, rendement).valeurTerminale();

            assertThat(vtAvec).isLessThan(vtSans);
        }

        @Test
        @DisplayName("ELT plus généreux qu'EP pour versement > 1 050€ (réduction 30% vs 25%)")
        void eltPlusGenereux_entre1050Et1350() {
            SimulateurBranche23 simEP  = new SimulateurBranche23(defautEpargnePension(), "EP");
            SimulateurBranche23 simELT = new SimulateurBranche23(defautEpargneLongTerme(), "ELT");

            // À 1 200€ : EP donne 25% × 1200 = 300€ de réduction, ELT donne 30% × 1200 = 360€
            // MAIS ELT a TOA 2% qui pénalise le capital → résultat net dépend du bilan global
            // On vérifie uniquement que les économies fiscales capitalisées sont plus grandes pour ELT
            double ecoEP  = simEP.simuler(profil, 1_200.0, rendement).economiesFiscalesCapitalisees();
            double ecoELT = simELT.simuler(profil, 1_200.0, rendement).economiesFiscalesCapitalisees();

            assertThat(ecoELT).isGreaterThan(ecoEP);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Propriétés mathématiques invariantes")
    class ProprietesMathematiques {

        @Test
        @DisplayName("Valeur terminale est monotone croissante avec le versement")
        void vanMonotoneCroissante() {
            SimulateurBranche23 sim = new SimulateurBranche23(
                    defautEpargnePension(), "EP");

            List<ResultatSimulation> resultats = ComparateurVehicules.simulerPlage(
                    sim, profil, rendement, 0, 200);

            for (int i = 1; i < resultats.size(); i++) {
                assertThat(resultats.get(i).valeurTerminale())
                        .as("Valeur terminale doit croître entre versement %d et %d",
                                (int) resultats.get(i-1).versementAnnuel(),
                                (int) resultats.get(i).versementAnnuel())
                        .isGreaterThanOrEqualTo(resultats.get(i-1).valeurTerminale());
            }
        }

        @Test
        @DisplayName("Valeur terminale à versement 0 est nulle (ou très proche) pour EP et CT")
        void vanNulleAVersement0() {
            SimulateurBranche23 simEP = new SimulateurBranche23(
                    defautEpargnePension(), "EP");
            SimulateurCT simCT = new SimulateurCT(ParametresCT.builder().exoneration(new ExonerationPlusValues(
                    Constants.CT_EXONERATION_BASE_DEFAUT,
                    Constants.CT_EXONERATION_ANNUELLE_DEFAUT,
                    Constants.CT_EXONERATION_ANNEES_MAX_DEFAUT)).build());

            assertThat(simEP.simuler(profil, 0.0, rendement).valeurTerminale())
                    .isCloseTo(0.0, within(1e-6));
            assertThat(simCT.simuler(profil, 0.0, rendement).valeurTerminale())
                    .isCloseTo(0.0, within(1e-6));
        }
    }
}
