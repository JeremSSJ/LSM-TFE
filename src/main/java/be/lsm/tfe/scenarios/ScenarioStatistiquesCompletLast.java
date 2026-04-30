package be.lsm.tfe.scenarios;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;
import be.lsm.tfe.stats.CalculateurStatistiques;
import be.lsm.tfe.stats.ExportateurRapport;
import be.lsm.tfe.stats.LigneRapportCSV;

import java.util.ArrayList;
import java.util.List;

/**
 * Scénarios statistiques complets — 4 fichiers CSV.
 *
 * <p>Pour chaque combinaison (ageDebut × rendement), on simule les 4 cas~:
 * <ol>
 *   <li>EP Branche 23  vs  CT sans taxe PV</li>
 *   <li>EP Branche 23  vs  CT avec taxe PV 10%</li>
 *   <li>ELT Branche 23 vs  CT sans taxe PV</li>
 *   <li>ELT Branche 23 vs  CT avec taxe PV 10%</li>
 * </ol>
 * </p>
 *
 * <p>Les profils sont incrémentaux : de 18→64 jusqu'à 54→64 (pas de 1 an).
 * Pour chaque profil, les rendements sont balayés de 1% à 15% (pas 1%).
 * Chaque ligne du CSV correspond donc à une combinaison unique
 * (ageDebut, rendement), soit 37 âges × 15 rendements = 555 lignes par CSV.</p>
 */
public final class ScenarioStatistiquesCompletLast {

    // ════════════════════════════════════════════════════════════════════
    //  ★ PARAMÈTRES MODIFIABLES
    // ════════════════════════════════════════════════════════════════════

    /** Âge de fin — fixe pour tous les scénarios. */
    private static final int    AGE_FIN       = 65;

    /** Premier âge de début (profil le plus long). */
    private static final int    AGE_DEBUT_MIN = 18;

    /** Dernier âge de début (profil le plus court). */
    private static final int    AGE_DEBUT_MAX = 55;

    private static final int    ANNEE_NAISS   = 2008;
    private static final double OLO           = 0.03;

    private static final double RENDEMENT_MIN = 0.03;
    private static final double RENDEMENT_MAX = 0.1;
    private static final double RENDEMENT_PAS = 0.01;

    // Plages de versements
    private static final int VERSEMENT_MIN_EP  = 1;
    private static final int VERSEMENT_MAX_EP  = 1_350;
    private static final int VERSEMENT_MIN_ELT = 1;
    private static final int VERSEMENT_MAX_ELT = 2_450;

    // Chemins de sortie
    private static final String CSV_EP_SANS_PV  =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_sans_taxe_pv.csv";
    private static final String CSV_EP_AVEC_PV  =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_avec_taxe_pv.csv";
    private static final String CSV_ELT_SANS_PV =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_sans_taxe_pv.csv";
    private static final String CSV_ELT_AVEC_PV =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_avec_taxe_pv.csv";

    // ════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {

        // ── Paramètres CT (communs aux 4 scénarios) ───────────────────────────
        ParametresCT paramsCTsans = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.008)
                .fraisGestionAnnuels(0.0012)
                .tauxTaxePlusValues(0.0)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        ParametresCT paramsCTavec = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.008)
                .fraisGestionAnnuels(0.0012)
                .tauxTaxePlusValues(0.10)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        // ── Paramètres B23 EP et ELT (communs à tous les profils d'âge) ──────
        ParametresBranche23 paramsEP = ParametresBranche23.builder()
                .taxeOperationsAssurance(0.0)
                .fraisParPrime(0.02)
                .fraisGestionAnnuels(0.015)
                .tauxTaxeAnticipative(0.08)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                .ageLimiteReductionFiscale(65)
                .build();

        ParametresBranche23 paramsELT = ParametresBranche23.builder()
                .taxeOperationsAssurance(Constants.ELT_TAXE_ASSURANCE)
                .fraisParPrime(0.02)
                .fraisGestionAnnuels(0.015)
                .tauxTaxeAnticipative(0.10)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                .ageLimiteReductionFiscale(65)
                .build();

        Simulateur simEP    = new SimulateurBranche23(paramsEP,  "EP Branche 23");
        Simulateur simELT   = new SimulateurBranche23(paramsELT, "ELT Branche 23");
        Simulateur simCTsan = new SimulateurCT(paramsCTsans);
        Simulateur simCTav  = new SimulateurCT(paramsCTavec);

        // ── Accumulation des lignes pour les 4 CSV ────────────────────────────
        List<LigneRapportCSV> epSans  = new ArrayList<>();
        List<LigneRapportCSV> epAvec  = new ArrayList<>();
        List<LigneRapportCSV> eltSans = new ArrayList<>();
        List<LigneRapportCSV> eltAvec = new ArrayList<>();

        int nbProfilsAge = AGE_DEBUT_MAX - AGE_DEBUT_MIN + 1;
        int nbRendements = (int) Math.round((RENDEMENT_MAX - RENDEMENT_MIN) / RENDEMENT_PAS) + 1;
        int totalLignes  = nbProfilsAge * nbRendements;

        System.out.printf(
                "═══════════════════════════════════════════════════════%n" +
                "  Simulation complète — 4 scénarios%n" +
                "  Profils : %d→%d  jusqu'à  %d→%d  (pas 1 an) = %d profils%n" +
                "  Rendements : %.0f%% → %.0f%%  (pas %.0f%%) = %d valeurs%n" +
                "  Total lignes par CSV : %d%n" +
                "═══════════════════════════════════════════════════════%n%n",
                AGE_DEBUT_MIN, AGE_FIN, AGE_DEBUT_MAX, AGE_FIN, nbProfilsAge,
                RENDEMENT_MIN * 100, RENDEMENT_MAX * 100, RENDEMENT_PAS * 100, nbRendements,
                totalLignes
        );

        int lignesTraitees = 0;

        // ── Double boucle : âge de début × rendement ─────────────────────────
        for (int ageDebut = AGE_DEBUT_MIN; ageDebut <= AGE_DEBUT_MAX; ageDebut++) {

            ProfilInvestisseur profil = new ProfilInvestisseur(
                    "Investisseur", "Type", ANNEE_NAISS, ageDebut, AGE_FIN);

            System.out.printf("  ┌─ Profil %d → %d ans%n", ageDebut, AGE_FIN);

            for (double rend = RENDEMENT_MIN;
                 rend <= RENDEMENT_MAX + 1e-9;
                 rend += RENDEMENT_PAS) {

                double rendArrondi = Math.round(rend * 1000) / 1000.0;
                ParametresRendement rendement = new ParametresRendement(rendArrondi, OLO);

                // Simulations EP
                List<ResultatSimulation> resEP = ComparateurVehicules.simulerPlage(
                        simEP, profil, rendement, VERSEMENT_MIN_EP, VERSEMENT_MAX_EP);

                // Simulations ELT
                List<ResultatSimulation> resELT = ComparateurVehicules.simulerPlage(simELT, profil, rendement, VERSEMENT_MIN_ELT, VERSEMENT_MAX_ELT);

                // Simulations CT (sur les deux plages pour correspondre à EP et ELT)
                List<ResultatSimulation> resCTsanEP = ComparateurVehicules.simulerPlage(
                        simCTsan, profil, rendement, VERSEMENT_MIN_EP, VERSEMENT_MAX_EP);
                List<ResultatSimulation> resCTavEP  = ComparateurVehicules.simulerPlage(
                        simCTav, profil, rendement, VERSEMENT_MIN_EP, VERSEMENT_MAX_EP);
                List<ResultatSimulation> resCTsanELT = ComparateurVehicules.simulerPlage(
                        simCTsan, profil, rendement, VERSEMENT_MIN_ELT, VERSEMENT_MAX_ELT);
                List<ResultatSimulation> resCTavELT  = ComparateurVehicules.simulerPlage(
                        simCTav, profil, rendement, VERSEMENT_MIN_ELT, VERSEMENT_MAX_ELT);

                // ── EP vs CT sans taxe PV ─────────────────────────────────────
                epSans.add(LigneRapportCSV.depuis(
                        CalculateurStatistiques.calculer(
                                resEP, resCTsanEP, "EP Branche 23", "CT sans taxe PV"),
                        rendArrondi * 100, ageDebut, AGE_FIN, 0.0));

                // ── EP vs CT avec taxe PV ─────────────────────────────────────
                epAvec.add(LigneRapportCSV.depuis(
                        CalculateurStatistiques.calculer(
                                resEP, resCTavEP, "EP Branche 23", "CT taxe PV 10%"),
                        rendArrondi * 100, ageDebut, AGE_FIN, 10.0));

                // ── ELT vs CT sans taxe PV ────────────────────────────────────
                eltSans.add(LigneRapportCSV.depuis(
                        CalculateurStatistiques.calculer(
                                resELT, resCTsanELT, "ELT Branche 23", "CT sans taxe PV"),
                        rendArrondi * 100, ageDebut, AGE_FIN, 0.0));

                // ── ELT vs CT avec taxe PV ────────────────────────────────────
                eltAvec.add(LigneRapportCSV.depuis(
                        CalculateurStatistiques.calculer(
                                resELT, resCTavELT, "ELT Branche 23", "CT taxe PV 10%"),
                        rendArrondi * 100, ageDebut, AGE_FIN, 10.0));

                lignesTraitees++;
            }

            // Bilan intermédiaire par profil d'âge
            afficherBilanProfilAge(ageDebut, epSans, epAvec, eltSans, eltAvec);
            System.out.printf("  └─ %d / %d lignes traitées (%.0f%%)%n%n",
                    lignesTraitees, totalLignes,
                    100.0 * lignesTraitees / totalLignes);
        }

        // ── Export des 4 CSV ──────────────────────────────────────────────────
        System.out.println("Écriture des fichiers CSV...");
        ExportateurRapport.ecrireCSV(epSans,  CSV_EP_SANS_PV);
        ExportateurRapport.ecrireCSV(epAvec,  CSV_EP_AVEC_PV);
        ExportateurRapport.ecrireCSV(eltSans, CSV_ELT_SANS_PV);
        ExportateurRapport.ecrireCSV(eltAvec, CSV_ELT_AVEC_PV);
    }

    // ── Bilan intermédiaire par profil d'âge ─────────────────────────────────

    /**
     * Affiche en console, pour un profil d'âge donné, le résumé des 4 scénarios
     * sur les rendements déjà calculés pour cet âge.
     */
    private static void afficherBilanProfilAge(
            int ageDebut,
            List<LigneRapportCSV> epSans,
            List<LigneRapportCSV> epAvec,
            List<LigneRapportCSV> eltSans,
            List<LigneRapportCSV> eltAvec) {

        // On filtre uniquement les lignes correspondant à cet âge de début
        int nbRendements = (int) Math.round((RENDEMENT_MAX - RENDEMENT_MIN) / RENDEMENT_PAS) + 1;

        List<LigneRapportCSV> epSansProfil  = derniers(epSans,  nbRendements);
        List<LigneRapportCSV> epAvecProfil  = derniers(epAvec,  nbRendements);
        List<LigneRapportCSV> eltSansProfil = derniers(eltSans, nbRendements);
        List<LigneRapportCSV> eltAvecProfil = derniers(eltAvec, nbRendements);

        long epSansDomA  = epSansProfil.stream()
                .filter(l -> l.dominant().equals("EP Branche 23")).count();
        long epAvecDomA  = epAvecProfil.stream()
                .filter(l -> l.dominant().equals("EP Branche 23")).count();
        long eltSansDomA = eltSansProfil.stream()
                .filter(l -> l.dominant().equals("ELT Branche 23")).count();
        long eltAvecDomA = eltAvecProfil.stream()
                .filter(l -> l.dominant().equals("ELT Branche 23")).count();

        System.out.printf(
                "  │  Profil %d→%d : " +
                "EP/CT-sansPV %d/%d | EP/CT-avecPV %d/%d | " +
                "ELT/CT-sansPV %d/%d | ELT/CT-avecPV %d/%d  (B23 domine / total rend.)%n",
                ageDebut, AGE_FIN,
                epSansDomA,  nbRendements,
                epAvecDomA,  nbRendements,
                eltSansDomA, nbRendements,
                eltAvecDomA, nbRendements
        );
    }

    /** Retourne les {@code n} derniers éléments d'une liste. */
    private static <T> List<T> derniers(List<T> liste, int n) {
        int debut = Math.max(0, liste.size() - n);
        return liste.subList(debut, liste.size());
    }
}
