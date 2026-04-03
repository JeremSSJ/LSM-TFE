package be.lsm.tfe.scenarios;

import be.lsm.tfe.stats.GenerateurHeatmap;
import be.lsm.tfe.stats.LecteurCSV;
import be.lsm.tfe.stats.LigneRapportCSV;

import java.util.List;

/**
 * Génère les 4 heatmaps à partir des fichiers CSV produits par
 * {@link ScenarioStatistiquesComplet}.
 *
 * <p>Chaque heatmap représente~:
 * <ul>
 *   <li><b>Axe X</b> : rendement annuel brut (1 % → 15 %)</li>
 *   <li><b>Axe Y</b> : âge de début des versements (18 → 54 ans)</li>
 *   <li><b>Couleur</b> : instrument dominant (bleu = B23, vert = CT, gris = ex æquo)</li>
 *   <li><b>Intensité</b> : force de la dominance (taux de la plage de versements où l'instrument gagne)</li>
 *   <li><b>Annotation</b> : versement au premier croisement (€/an), quand il existe</li>
 * </ul>
 * </p>
 *
 * <p>Lancez ce main <b>après</b> avoir exécuté {@link ScenarioStatistiquesComplet}.</p>
 */
public final class ScenarioGraphiquesStats {

    // ════════════════════════════════════════════════════════════════════
    //  ★ CHEMINS — à adapter si vous avez changé les chemins dans
    //              ScenarioStatistiquesComplet
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
            "C:/Users/jerem/Downloads/stats/heatmap_ep_vs_ct_sans_taxe_pv.png";
    private static final String PNG_EP_AVEC_PV  =
            "C:/Users/jerem/Downloads/stats/heatmap_ep_vs_ct_avec_taxe_pv.png";
    private static final String PNG_ELT_SANS_PV =
            "C:/Users/jerem/Downloads/stats/heatmap_elt_vs_ct_sans_taxe_pv.png";
    private static final String PNG_ELT_AVEC_PV =
            "C:/Users/jerem/Downloads/stats/heatmap_elt_vs_ct_avec_taxe_pv.png";

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

        System.out.println("Génération des heatmaps...");

        GenerateurHeatmap.generer(
                epSans,
                "EP Branche 23 vs Compte-Titres  —  Sans taxe sur les plus-values",
                "EP B23", "CT",
                PNG_EP_SANS_PV);

        GenerateurHeatmap.generer(
                epAvec,
                "EP Branche 23 vs Compte-Titres  —  Avec taxe PV 10%",
                "EP B23", "CT",
                PNG_EP_AVEC_PV);

        GenerateurHeatmap.generer(
                eltSans,
                "ELT Branche 23 vs Compte-Titres  —  Sans taxe sur les plus-values",
                "ELT B23", "CT",
                PNG_ELT_SANS_PV);

        GenerateurHeatmap.generer(
                eltAvec,
                "ELT Branche 23 vs Compte-Titres  —  Avec taxe PV 10%",
                "ELT B23", "CT",
                PNG_ELT_AVEC_PV);

        System.out.println("\n✓ 4 heatmaps générées dans C:/Users/jerem/Downloads/stats/");
    }
}
