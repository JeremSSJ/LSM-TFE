package be.lsm.tfe.stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exporte les rapports statistiques multi-scénarios vers la console et/ou un fichier CSV.
 */
public final class ExportateurRapport {

    private ExportateurRapport() {}

    // ── Export CSV ────────────────────────────────────────────────────────────

    /**
     * Écrit la liste de lignes dans un fichier CSV (séparateur point-virgule).
     * Crée les répertoires parents si nécessaire.
     *
     * @param lignes      Liste de lignes à écrire
     * @param cheminFichier Chemin complet du fichier de sortie (.csv)
     * @throws IOException si l'écriture échoue
     */
    public static void ecrireCSV(List<LigneRapportCSV> lignes, String cheminFichier) throws IOException {
        File fichier = new File(cheminFichier);
        fichier.getParentFile().mkdirs();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fichier))) {
            writer.write(LigneRapportCSV.entete());
            writer.newLine();
            for (LigneRapportCSV ligne : lignes) {
                writer.write(ligne.toCSV());
                writer.newLine();
            }
        }

        System.out.println("✓ Rapport CSV sauvegardé : " + fichier.getAbsolutePath()
                + " (" + lignes.size() + " lignes)");
    }

    // ── Résumé console ────────────────────────────────────────────────────────

    /**
     * Affiche en console un tableau de synthèse lisible des lignes du rapport.
     * Utile pour une lecture rapide sans ouvrir le CSV.
     */
    public static void afficherTableauConsole(List<LigneRapportCSV> lignes) {
        System.out.println();
        System.out.println("╔══════════╦═════╦══════╦══════════╦══════════╦═══════════╦════════════════╦═══════════════════╗");
        System.out.println("║ Rend. %  ║ Dbt ║  Fin ║ Dom. A % ║ Dom. B % ║ Croisem.  ║ Premier crois. ║ Dominant global   ║");
        System.out.println("╠══════════╬═════╬══════╬══════════╬══════════╬═══════════╬════════════════╬═══════════════════╣");

        lignes.forEach(l -> System.out.printf(
                "║ %7.1f%% ║ %3d ║  %3d ║ %7.1f%% ║ %7.1f%% ║ %9d ║ %13s ║ %-17s ║%n",
                l.rendementPct(),
                l.ageDebut(),
                l.ageFin(),
                l.tauxDominanceA(),
                l.tauxDominanceB(),
                l.nbCroisements(),
                Double.isNaN(l.premierCroisement())
                        ? "   aucun  "
                        : "%,.0f €/an".formatted(l.premierCroisement()),
                l.dominant().length() > 17
                        ? l.dominant().substring(0, 14) + "..."
                        : l.dominant()
        ));

        System.out.println("╚══════════╩═════╩══════╩══════════╩══════════╩═══════════╩════════════════╩═══════════════════╝");
        System.out.printf("   A = %s   |   B = %s%n",
                lignes.get(0).vehiculeA(), lignes.get(0).vehiculeB());
        System.out.println();
    }

    // ── Synthèse narrative ────────────────────────────────────────────────────

    /**
     * Affiche un bilan narratif global~: à quel rendement le basculement se produit,
     * combien de scénarios A domine, etc.
     */
    public static void afficherBilanNarratif(List<LigneRapportCSV> lignes, String nomA, String nomB) {
        long nA = lignes.stream().filter(l -> l.dominant().equals(nomA)).count();
        long nB = lignes.stream().filter(l -> l.dominant().equals(nomB)).count();
        long nEx = lignes.size() - nA - nB;

        System.out.println("═════════════════════════════════════════════════════════════");
        System.out.printf("  BILAN SUR %d SCÉNARIOS%n", lignes.size());
        System.out.println("─────────────────────────────────────────────────────────────");
        System.out.printf("  %s domine globalement dans : %d scénarios (%.0f%%)%n",
                nomA, nA, 100.0 * nA / lignes.size());
        System.out.printf("  %s domine globalement dans : %d scénarios (%.0f%%)%n",
                nomB, nB, 100.0 * nB / lignes.size());
        System.out.printf("  Ex æquo                    : %d scénarios%n", nEx);

        // Seuil de rendement où le basculement se produit
        for (int i = 1; i < lignes.size(); i++) {
            LigneRapportCSV prev = lignes.get(i - 1);
            LigneRapportCSV curr = lignes.get(i);
            if (!prev.dominant().equals(curr.dominant())
                    && !curr.dominant().contains("æquo")
                    && !prev.dominant().contains("æquo")) {
                System.out.printf("  → Basculement de dominance entre %.1f%% et %.1f%% de rendement%n",
                        prev.rendementPct(), curr.rendementPct());
            }
        }

        // Point de croisement moyen sur les scénarios où il en existe un
        lignes.stream()
                .filter(l -> !Double.isNaN(l.premierCroisement()))
                .mapToDouble(LigneRapportCSV::premierCroisement)
                .average()
                .ifPresent(moy -> System.out.printf(
                        "  → Point de croisement moyen (scénarios avec croisement) : %,.0f €/an%n", moy));

        System.out.println("═════════════════════════════════════════════════════════════");
    }
}
