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
 * Scénario statistique : EP Branche 23 vs Compte-Titres.
 *
 * <p>Balaye les rendements de 1% à 15% par pas de 1% et produit~:
 * <ul>
 *   <li>Un tableau console lisible par rendement</li>
 *   <li>Un CSV complet avec toutes les statistiques</li>
 *   <li>Un bilan narratif global</li>
 * </ul>
 * </p>
 *
 * <p>Deux variantes sont simulées en parallèle~:
 * <ul>
 *   <li>Sans taxe sur les plus-values CT (tauxTaxePV = 0%)</li>
 *   <li>Avec taxe sur les plus-values CT (tauxTaxePV = 10%)</li>
 * </ul>
 * </p>
 */
public final class ScenarioStatistiquesEPvsCT {

    // ════════════════════════════════════════════════════════════════════
    //  ★ PARAMÈTRES MODIFIABLES
    // ════════════════════════════════════════════════════════════════════

    private static final int    AGE_DEBUT     = 18;
    private static final int    AGE_FIN       = 64;
    private static final int    ANNEE_NAISS   = 2008;
    private static final int    VERSEMENT_MIN = 0;
    private static final int    VERSEMENT_MAX = 1_600;
    private static final double OLO           = 0.03;

    // Plage de rendements à tester (en fraction, pas en %)
    private static final double RENDEMENT_MIN  = 0.01;
    private static final double RENDEMENT_MAX  = 0.15;
    private static final double RENDEMENT_PAS  = 0.01;

    private static final String CHEMIN_CSV_SANS_TAXE =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_sans_taxe_pv.csv";
    private static final String CHEMIN_CSV_AVEC_TAXE =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_avec_taxe_pv.csv";

    // ════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {

        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Marie", "Dupont", ANNEE_NAISS, AGE_DEBUT, AGE_FIN);

        ParametresBranche23 paramsEP = ParametresBranche23.builder()
                .taxeOperationsAssurance(0.0)
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.08)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                .ageLimiteReductionFiscale(64)
                .build();

        // Compte-Titres sans taxe PV
        ParametresCT paramsCTsans = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.0)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        // Compte-Titres avec taxe PV 10%
        ParametresCT paramsCTavec = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.10)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        Simulateur simEP       = new SimulateurBranche23(paramsEP, "EP Branche 23");
        Simulateur simCTsans   = new SimulateurCT(paramsCTsans);
        Simulateur simCTavec   = new SimulateurCT(paramsCTavec);

        List<LigneRapportCSV> lignesSans = new ArrayList<>();
        List<LigneRapportCSV> lignesAvec = new ArrayList<>();

        System.out.printf("%nSimulation EP vs CT — balayage rendements %.0f%% → %.0f%% (pas %.0f%%)%n%n",
                RENDEMENT_MIN * 100, RENDEMENT_MAX * 100, RENDEMENT_PAS * 100);

        // ── Boucle sur les rendements ─────────────────────────────────────────
        for (double rend = RENDEMENT_MIN;
             rend <= RENDEMENT_MAX + 1e-9;
             rend += RENDEMENT_PAS) {

            double rendArrondi = Math.round(rend * 1000) / 1000.0;
            ParametresRendement rendement = new ParametresRendement(rendArrondi, OLO);

            System.out.printf("  Rendement %5.1f%%...", rendArrondi * 100);

            List<ResultatSimulation> resultatsEP    = ComparateurVehicules.simulerPlage(
                    simEP, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);
            List<ResultatSimulation> resultatsCTsans = ComparateurVehicules.simulerPlage(
                    simCTsans, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);
            List<ResultatSimulation> resultatsCTavec = ComparateurVehicules.simulerPlage(
                    simCTavec, profil, rendement, VERSEMENT_MIN, VERSEMENT_MAX);

            // Stats sans taxe PV
            StatistiquesComparaison statsSans = CalculateurStatistiques.calculer(
                    resultatsEP, resultatsCTsans, simEP.nomVehicule(), "CT (sans taxe PV)");
            lignesSans.add(LigneRapportCSV.depuis(
                    statsSans, rendArrondi * 100, AGE_DEBUT, AGE_FIN, 0.0));

            // Stats avec taxe PV
            StatistiquesComparaison statsAvec = CalculateurStatistiques.calculer(
                    resultatsEP, resultatsCTavec, simEP.nomVehicule(), "CT (taxe PV 10%)");
            lignesAvec.add(LigneRapportCSV.depuis(
                    statsAvec, rendArrondi * 100, AGE_DEBUT, AGE_FIN, 10.0));

            System.out.printf("  dominant (sans PV) : %-20s | dominant (avec PV) : %s%n",
                    statsSans.instrumentDominantGlobal(),
                    statsAvec.instrumentDominantGlobal());
        }

        // ── Tableaux console ─────────────────────────────────────────────────
        System.out.println("\n── EP vs CT sans taxe PV ──────────────────────────────────────");
        ExportateurRapport.afficherTableauConsole(lignesSans);
        ExportateurRapport.afficherBilanNarratif(lignesSans,
                simEP.nomVehicule(), "CT (sans taxe PV)");

        System.out.println("\n── EP vs CT avec taxe PV 10% ──────────────────────────────────");
        ExportateurRapport.afficherTableauConsole(lignesAvec);
        ExportateurRapport.afficherBilanNarratif(lignesAvec,
                simEP.nomVehicule(), "CT (taxe PV 10%)");

        // ── Export CSV ───────────────────────────────────────────────────────
        ExportateurRapport.ecrireCSV(lignesSans, CHEMIN_CSV_SANS_TAXE);
        ExportateurRapport.ecrireCSV(lignesAvec, CHEMIN_CSV_AVEC_TAXE);
    }
}
