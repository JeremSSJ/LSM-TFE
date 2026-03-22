package tfe.branche23;

import be.lsm.tfe.common.*;
import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.ParametresRendement;
import be.lsm.tfe.common.ProfilInvestisseur;
import be.lsm.tfe.common.ResultatAnnuel;
import be.lsm.tfe.common.ResultatSimulation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimulateurBranche23")
class SimulateurBranche23Test {


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

    // Profil standard : 18→64, né en 1980 (début 1998)
    private ProfilInvestisseur profil;
    // Rendement déterministe : 0% pour isoler les autres effets
    private ParametresRendement rendementNul;
    // Rendement non nul pour les tests de capitalisation
    private ParametresRendement rendement5pct;

    @BeforeEach
    void setup() {
        profil         = new ProfilInvestisseur("Test", "User", 1980, 18, 64);
        rendementNul   = new ParametresRendement(0.0, 0.0);
        rendement5pct  = new ParametresRendement(0.05, 0.03);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Versement 0€ → tout est nul")
    class VersementNul {

        @Test
        @DisplayName("Capital final = 0 avec versement 0€")
        void capitalFinalNul() {
            ParametresBranche23 p = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(p, "EP");
            ResultatSimulation res = sim.simuler(profil, 0.0, rendementNul);
            assertThat(res.capitalFinalNet()).isCloseTo(0.0, within(1e-9));
        }

        @Test
        @DisplayName("VAN totale = 0 avec versement 0€")
        void vanTotaleNulle() {
            ParametresBranche23 p = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(p, "EP");
            ResultatSimulation res = sim.simuler(profil, 0.0, rendementNul);
            assertThat(res.vanTotale()).isCloseTo(0.0, within(1e-9));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Frais d'entrée (TOA + frais/prime)")
    class FraisEntree {

        @Test
        @DisplayName("TOA 10% réduit le capital proportionnellement")
        void toa10pct_reduitCapital() {
            ParametresBranche23 avecTOA = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.10)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.0)       // désactiver pour isoler l'effet TOA
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            ParametresBranche23 sansTOA = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.0)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            SimulateurBranche23 simAvec = new SimulateurBranche23(avecTOA, "avecTOA");
            SimulateurBranche23 simSans = new SimulateurBranche23(sansTOA, "sansTOA");

            double versement = 1_000.0;
            double capitalAvec = simAvec.simuler(profil, versement, rendementNul).capitalFinalNet();
            double capitalSans = simSans.simuler(profil, versement, rendementNul).capitalFinalNet();

            // Avec TOA 10% : chaque versement est réduit à 90%
            assertThat(capitalAvec).isCloseTo(capitalSans * 0.9, within(capitalSans * 0.001));
        }

        @Test
        @DisplayName("Frais/prime 5% réduit le capital proportionnellement")
        void fraisPrime5pct_reduitCapital() {
            ParametresBranche23 avecFrais = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.05)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.0)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            ParametresBranche23 sansFrais = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.0)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            double versement = 1_000.0;
            double capAvec = new SimulateurBranche23(avecFrais, "a").simuler(profil, versement, rendementNul).capitalFinalNet();
            double capSans = new SimulateurBranche23(sansFrais, "s").simuler(profil, versement, rendementNul).capitalFinalNet();

            assertThat(capAvec).isCloseTo(capSans * 0.95, within(capSans * 0.001));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Taxe anticipative")
    class TaxeAnticipative {

        @Test
        @DisplayName("La taxe anticipative est appliquée exactement une fois")
        void taxeAnticipativeUneSeuleFois() {
            ParametresBranche23 params = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.08)
                    .ageTaxeAnticipative(60)
                    .dureeMinAvantAnticipativeSiSouscritTard(10)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendementNul);

            long nbAnticipatives = res.resultatParAnnee().stream()
                    .filter(ResultatAnnuel::taxeAnticipativeAppliquee)
                    .count();
            assertThat(nbAnticipatives).isEqualTo(1);
        }

        @Test
        @DisplayName("La taxe anticipative s'applique à l'année de l'âge 60 (souscrit à 18 ans)")
        void taxeAnticipativeAAge60() {
            ParametresBranche23 params = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.08)
                    .ageTaxeAnticipative(60)
                    .dureeMinAvantAnticipativeSiSouscritTard(10)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendementNul);

            Optional<ResultatAnnuel> anneeAnticipative = res.resultatParAnnee().stream()
                    .filter(ResultatAnnuel::taxeAnticipativeAppliquee)
                    .findFirst();

            assertThat(anneeAnticipative).isPresent();
            assertThat(anneeAnticipative.get().age()).isEqualTo(60);
        }

        @Test
        @DisplayName("Souscrit à 58 ans → taxe au 10e anniversaire (à 68 ans) — mais 68 > 64 donc jamais appliquée")
        void souscritA58Ans_taxeApres64Ans_nonAppliquee() {
            // Profil 58 → 64 : durée 6 ans → anniversaire 10 = 68 ans > 64 → pas de taxe
            ProfilInvestisseur profilTard = new ProfilInvestisseur("X", "Y", 1960, 58, 64);

            ParametresBranche23 params = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.08)
                    .ageTaxeAnticipative(60)
                    .dureeMinAvantAnticipativeSiSouscritTard(10)
                    .regleReductionFiscale(prime -> 0.0)
                    .ageLimiteReductionFiscale(0)
                    .build();

            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profilTard, 1_000.0, rendementNul);

            long nbAnticipatives = res.resultatParAnnee().stream()
                    .filter(ResultatAnnuel::taxeAnticipativeAppliquee)
                    .count();
            assertThat(nbAnticipatives).isEqualTo(0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Économies fiscales")
    class EconomiesFiscales {

        @Test
        @DisplayName("Réduction fiscale EP 30% pour prime 1 000€ → 300€/an")
        void reductionEP_1000euros_300parAn() {
            ParametresBranche23 params = ParametresBranche23.builder()
                    .taxeOperationsAssurance(0.0)
                    .fraisParPrime(0.0)
                    .fraisGestionAnnuels(0.0)
                    .tauxTaxeAnticipative(0.0)
                    .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                    .ageLimiteReductionFiscale(64)
                    .build();

            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendementNul);

            // Toutes les années éligibles (18→64) doivent avoir 300€ d'économie
            res.resultatParAnnee().stream()
                    .filter(r -> r.age() >= 18 && r.age() <= 64)
                    .forEach(r -> assertThat(r.economiesFiscales())
                            .as("Économie à l'âge %d", r.age())
                            .isCloseTo(300.0, within(1e-4)));
        }

        @Test
        @DisplayName("Aucune réduction si versement dépasse le plafond EP (1 350€ max)")
        void reductionPlafonneeAu1350() {
            ParametresBranche23 params = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation avec1350 = sim.simuler(profil, 1_350.0, rendementNul);
            ResultatSimulation avec2000 = sim.simuler(profil, 2_000.0, rendementNul);

            // Les économies fiscales doivent être identiques (plafond 1 350€)
            assertThat(avec2000.vanEconomiesFiscales())
                    .isCloseTo(avec1350.vanEconomiesFiscales(), within(1e-4));
        }

        @Test
        @DisplayName("vanEconomiesFiscales > 0 quand réduction > 0")
        void vanEconomiesFiscalesPositive() {
            ParametresBranche23 params = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.vanEconomiesFiscales()).isGreaterThan(0.0);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Cohérence des résultats")
    class Coherence {

        @Test
        @DisplayName("Nombre d'années dans le détail = duréeAnnées du profil")
        void nbAnneesEgalDuree() {
            ParametresBranche23 params = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.resultatParAnnee()).hasSize(profil.dureeAnnees());
        }

        @Test
        @DisplayName("vanTotale = vanCapital + vanEconomiesFiscales")
        void vanTotaleEstSomme() {
            ParametresBranche23 params = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            ResultatSimulation res = sim.simuler(profil, 1_000.0, rendement5pct);
            assertThat(res.vanTotale())
                    .isCloseTo(res.vanCapital() + res.vanEconomiesFiscales(), within(1e-6));
        }

        @Test
        @DisplayName("Capital augmente avec le versement (frais constants)")
        void capitalCroissantAvecVersement() {
            ParametresBranche23 params = defautEpargnePension();
            SimulateurBranche23 sim = new SimulateurBranche23(params, "EP");
            double cap500  = sim.simuler(profil, 500.0,  rendement5pct).capitalFinalNet();
            double cap1000 = sim.simuler(profil, 1_000.0, rendement5pct).capitalFinalNet();
            assertThat(cap1000).isGreaterThan(cap500);
        }

        @Test
        @DisplayName("nomVehicule() retourne le nom fourni au constructeur")
        void nomVehiculeRetourneNomFourni() {
            SimulateurBranche23 sim = new SimulateurBranche23(
                    defautEpargnePension(), "Mon EP");
            assertThat(sim.nomVehicule()).isEqualTo("Mon EP");
        }
    }
}
