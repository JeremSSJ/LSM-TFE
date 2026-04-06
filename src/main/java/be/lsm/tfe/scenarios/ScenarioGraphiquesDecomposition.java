package be.lsm.tfe.scenarios;

import be.lsm.tfe.stats.GenerateurHeatmapDecomposition;
import be.lsm.tfe.stats.LecteurCSV;
import be.lsm.tfe.stats.LigneRapportCSV;

import java.util.List;

/**
 * Génère les 4 heatmaps de décomposition à partir des fichiers CSV produits par
 * {@link ScenarioStatistiquesComplet}.
 *
 * <p>Chaque heatmap utilise 3 couleurs selon l'origine de l'avantage comparatif :
 * <ul>
 *   <li><b>Bleu foncé</b> : B23 domine même sans les économies fiscales
 *       (vanCapital B23 > vanCapital CT)</li>
 *   <li><b>Bleu clair</b> : B23 domine uniquement grâce aux réductions d'impôt
 *       (vanCapital B23 ≤ CT, mais total B23 > CT)</li>
 *   <li><b>Vert</b> : CT domine même après les économies fiscales</li>
 * </ul>
 * </p>
 *
 * <p>L'annotation dans chaque cellule montre l'écart moyen en VAN (€) entre
 * la branche 23 totale et le CT, positif quand B23 gagne, négatif quand CT gagne.</p>
 *
 * <p>Lancez ce main <b>après</b> {@link ScenarioStatistiquesComplet}.</p>
 */
public final class ScenarioGraphiquesDecomposition {

    // ════════════════════════════════════════════════════════════════════
    //  ★ CHEMINS — à adapter si nécessaire
    // ════════════════════════════════════════════════════════════════════

    private static final String CSV_EP_SANS_PV  =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_sans_taxe_pv.csv";
    private static final String CSV_EP_AVEC_PV  =
            "C:/Users/jerem/Downloads/stats/ep_vs_ct_avec_taxe_pv.csv";
    private static final String CSV_ELT_SANS_PV =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_sans_taxe_pv.csv";
    private static final String CSV_ELT_AVEC_PV =
            "C:/Users/jerem/Downloads/stats/elt_vs_ct_avec_taxe_pv.csv";

    private static final String PNG_EP_SANS_PV  =
            "C:/Users/jerem/Downloads/stats/decomp_ep_vs_ct_sans_taxe_pv.png";
    private static final String PNG_EP_AVEC_PV  =
            "C:/Users/jerem/Downloads/stats/decomp_ep_vs_ct_avec_taxe_pv.png";
    private static final String PNG_ELT_SANS_PV =
            "C:/Users/jerem/Downloads/stats/decomp_elt_vs_ct_sans_taxe_pv.png";
    private static final String PNG_ELT_AVEC_PV =
            "C:/Users/jerem/Downloads/stats/decomp_elt_vs_ct_avec_taxe_pv.png";

    // ════════════════════════════════════════════════════════════════════

    public static void main(String[] args) throws Exception {

        System.out.println("Lecture des fichiers CSV...");
        List<LigneRapportCSV> epSans  = LecteurCSV.lire(CSV_EP_SANS_PV);
        List<LigneRapportCSV> epAvec  = LecteurCSV.lire(CSV_EP_AVEC_PV);
        List<LigneRapportCSV> eltSans = LecteurCSV.lire(CSV_ELT_SANS_PV);
        List<LigneRapportCSV> eltAvec = LecteurCSV.lire(CSV_ELT_AVEC_PV);

        System.out.printf(
                "  EP sans PV  : %d lignes%n" +
                "  EP avec PV  : %d lignes%n" +
                "  ELT sans PV : %d lignes%n" +
                "  ELT avec PV : %d lignes%n%n",
                epSans.size(), epAvec.size(), eltSans.size(), eltAvec.size());

        System.out.println("Génération des heatmaps de décomposition...");

        GenerateurHeatmapDecomposition.generer(
                epSans,
                "EP Branche 23 vs CT  —  Sans taxe sur les plus-values",
                "EP B23", "CT",
                PNG_EP_SANS_PV);

        GenerateurHeatmapDecomposition.generer(
                epAvec,
                "EP Branche 23 vs CT  —  Avec taxe PV 10%",
                "EP B23", "CT",
                PNG_EP_AVEC_PV);

        GenerateurHeatmapDecomposition.generer(
                eltSans,
                "ELT Branche 23 vs CT  —  Sans taxe sur les plus-values",
                "ELT B23", "CT",
                PNG_ELT_SANS_PV);

        GenerateurHeatmapDecomposition.generer(
                eltAvec,
                "ELT Branche 23 vs CT  —  Avec taxe PV 10%",
                "ELT B23", "CT",
                PNG_ELT_AVEC_PV);

        System.out.println("\n✓ 4 heatmaps de décomposition générées dans C:/Users/jerem/Downloads/stats/");
        System.out.println("  Fichiers : decomp_ep_vs_ct_sans_taxe_pv.png");
        System.out.println("             decomp_ep_vs_ct_avec_taxe_pv.png");
        System.out.println("             decomp_elt_vs_ct_sans_taxe_pv.png");
        System.out.println("             decomp_elt_vs_ct_avec_taxe_pv.png");
    }
}
