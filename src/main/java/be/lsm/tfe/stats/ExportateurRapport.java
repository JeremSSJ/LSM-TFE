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
}
