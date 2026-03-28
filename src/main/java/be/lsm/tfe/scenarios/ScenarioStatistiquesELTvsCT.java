package be.lsm.tfe.scenarios;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;
import be.lsm.tfe.stats.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Scénario statistique : ELT Branche 23 vs Compte-Titres.
 *
 * <p>Structure identique à {@link ScenarioStatistiquesEPvsCT}, adaptée
 * aux paramètres spécifiques de l'épargne à long terme~:
 * TOA 2%, taxe anticipative 10%, réduction 30% ≤ 2 450€,
 * profil de souscription tardive (53 → 64 ans).</p>
 */
public final class ScenarioStatistiquesELTvsCT {

    // ════════════════════════════════════════════════════════════════════
    //  ★ PARAMÈTRES MODIFIABLES
    // ════════════════════════════════════════════════════════════════════

    private static final int    AGE_DEBUT     = 53;
    private static final int    AGE_FIN       = 64;
    private static final int    ANNEE_NAISS   = 2008;
    private static final int    VERSEMENT_MIN = 400;
    private static final int    VERSEMENT_MAX = 1_400;
    private static final double OLO           = 0.03;

    private static final double RENDEMENT_MIN  = 0.01;
    private static final double RENDEMENT_MAX  = 0.15;
    private static final double RENDEMENT_PAS  = 0.01;

    private static final String CHEMIN_CSV_SANS_TAXE =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_sans_taxe_pv.csv";
    private static final String CHEMIN_CSV_AVEC_TAXE =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_avec_taxe_pv.csv";

    // ════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {

        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Marie", "Dupont", ANNEE_NAISS, AGE_DEBUT, AGE_FIN);

        ParametresBranche23 paramsELT = ParametresBranche23.builder()
                .taxeOperationsAssurance(Constants.ELT_TAXE_ASSURANCE)  // 2%
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.10)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                .ageLimiteReductionFiscale(64)
                .build();

        ParametresCT paramsCTsans = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.0)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        ParametresCT paramsCTavec = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.10)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        Simulateur simELT      = new SimulateurBranche23(paramsELT, "ELT Branche 23");
        Simulateur simCTsans   = new SimulateurCT(paramsCTsans);
        Simulateur simCTavec   = new SimulateurCT(paramsCTavec);

        List<LigneRapportCSV> lignesSans = new ArrayList<>();
        List<LigneRapportCSV> lignesAvec = new ArrayList<>();

        System.out.printf("%nSimulation ELT vs CT — balayage rendements %.0f%% → %.0f%% (pas %.0f%%)%n%n",
                RENDEMENT_MIN * 100, RENDEMENT_MAX * 100, RENDEMENT_PAS * 100);

        for (double rend = RENDEMENT_MIN;
             rend <= RENDEMENT_MAX + 1e-9;
             rend += RENDEMENT_PAS) {

            double rendArrondi = Math.round(rend * 1000) / 1000.0;
            ParametresRendement rendement = new ParametresRendement(rendArrondi, OLO);

            System.out.printf("  Rendement %5.1f%%...", rendArrondi * 100);

            List<ResultatSimulation> resultatsELT    = ComparateurVehicules.simulerPlage(
                    simELT, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);
            List<ResultatSimulation> resultatsCTsans = ComparateurVehicules.simulerPlage(
                    simCTsans, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);
            List<ResultatSimulation> resultatsCTavec = ComparateurVehicules.simulerPlage(
                    simCTavec, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);

            StatistiquesComparaison statsSans = CalculateurStatistiques.calculer(
                    resultatsELT, resultatsCTsans, simELT.nomVehicule(), "CT (sans taxe PV)");
            lignesSans.add(LigneRapportCSV.depuis(
                    statsSans, rendArrondi * 100, AGE_DEBUT, AGE_FIN, 0.0));

            StatistiquesComparaison statsAvec = CalculateurStatistiques.calculer(
                    resultatsELT, resultatsCTavec, simELT.nomVehicule(), "CT (taxe PV 10%)");
            lignesAvec.add(LigneRapportCSV.depuis(
                    statsAvec, rendArrondi * 100, AGE_DEBUT, AGE_FIN, 10.0));

            System.out.printf("  dominant (sans PV) : %-20s | dominant (avec PV) : %s%n",
                    statsSans.instrumentDominantGlobal(),
                    statsAvec.instrumentDominantGlobal());
        }

        System.out.println("\n── ELT vs CT sans taxe PV ──────────────────────────────────────");
        ExportateurRapport.afficherTableauConsole(lignesSans);
        ExportateurRapport.afficherBilanNarratif(lignesSans,
                simELT.nomVehicule(), "CT (sans taxe PV)");

        System.out.println("\n── ELT vs CT avec taxe PV 10% ──────────────────────────────────");
        ExportateurRapport.afficherTableauConsole(lignesAvec);
        ExportateurRapport.afficherBilanNarratif(lignesAvec,
                simELT.nomVehicule(), "CT (taxe PV 10%)");

        ExportateurRapport.ecrireCSV(lignesSans, CHEMIN_CSV_SANS_TAXE);
        ExportateurRapport.ecrireCSV(lignesAvec, CHEMIN_CSV_AVEC_TAXE);
    }
}
