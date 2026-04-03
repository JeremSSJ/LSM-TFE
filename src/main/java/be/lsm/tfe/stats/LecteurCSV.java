package be.lsm.tfe.stats;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit un fichier CSV généré par {@link ExportateurRapport} et retourne
 * la liste des lignes sous forme de {@link LigneRapportCSV}.
 */
public final class LecteurCSV {

    private LecteurCSV() {}

    /**
     * @param cheminFichier Chemin complet du fichier CSV
     * @return Liste des lignes (sans l'en-tête)
     * @throws IOException si le fichier est introuvable ou illisible
     */
    public static List<LigneRapportCSV> lire(String cheminFichier) throws IOException {
        List<LigneRapportCSV> lignes = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(cheminFichier))) {
            reader.readLine(); // ignorer l'en-tête
            String ligne;
            while ((ligne = reader.readLine()) != null) {
                if (!ligne.isBlank()) {
                    lignes.add(parseLigne(ligne));
                }
            }
        }

        return lignes;
    }

    private static LigneRapportCSV parseLigne(String csv) {
        String[] c = csv.split(";", -1);
        return new LigneRapportCSV(
                c[0].trim(),                          // vehiculeA
                c[1].trim(),                          // vehiculeB
                parseDouble(c[2]),                    // rendementPct
                parseInt(c[3]),                       // ageDebut
                parseInt(c[4]),                       // ageFin
                parseDouble(c[5]),                    // tauxTaxePVpct
                parseInt(c[6]),                       // versementMin
                parseInt(c[7]),                       // versementMax
                parseDouble(c[8]),                    // tauxDominanceA
                parseDouble(c[9]),                    // tauxDominanceB
                parseInt(c[10]),                      // nbCroisements
                parseDoubleOuNaN(c[11]),              // premierCroisement
                parseDouble(c[12]),                   // avantMoyA
                parseDouble(c[13]),                   // avantMoyB
                parseDouble(c[14]),                   // avantMaxA
                parseDouble(c[15]),                   // avantMaxB
                parseDouble(c[16]),                   // aireEcarts
                c[17].trim()                          // dominant
        );
    }

    private static double parseDouble(String s) {
        return Double.parseDouble(s.trim().replace(',', '.'));
    }

    private static double parseDoubleOuNaN(String s) {
        s = s.trim();
        if (s.equalsIgnoreCase("NA") || s.equalsIgnoreCase("NaN") || s.isEmpty()) {
            return Double.NaN;
        }
        return parseDouble(s);
    }

    private static int parseInt(String s) {
        return Integer.parseInt(s.trim());
    }
}
