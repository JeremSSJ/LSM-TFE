package be.lsm.tfe.stats;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Génère une heatmap 2D (âge de début × rendement) représentant, pour chaque
 * cellule, quel instrument est dominant et avec quelle force.
 *
 * <h3>Lecture de la heatmap</h3>
 * <ul>
 *   <li><b>Couleur bleue</b> : le véhicule B23 (EP ou ELT) domine.</li>
 *   <li><b>Couleur verte</b> : le Compte-Titres domine.</li>
 *   <li><b>Couleur grise</b> : ex æquo.</li>
 *   <li><b>Intensité</b> : plus la couleur est foncée, plus la dominance
 *       est forte (proportionnelle au taux de dominance sur la plage de versements).</li>
 *   <li><b>Annotation blanche</b> : versement au premier croisement (€/an),
 *       affiché dans la cellule quand il existe.</li>
 * </ul>
 *
 * <h3>Axe X</h3> Rendement annuel brut (1 % → 15 %)
 * <h3>Axe Y</h3> Âge de début des versements (18 → 54 ans)
 */
public final class GenerateurHeatmap {

    // ── Couleurs de base ──────────────────────────────────────────────────────
    /** Teinte B23 (bleu foncé → bleu clair selon dominance). */
    private static final Color BASE_B23 = new Color(0x1a5276); // bleu profond
    /** Teinte CT (vert foncé → vert clair selon dominance). */
    private static final Color BASE_CT  = new Color(0x1e8449); // vert forêt
    /** Teinte ex æquo. */
    private static final Color BASE_EX  = new Color(0x7f8c8d); // gris

    // ── Dimensions ────────────────────────────────────────────────────────────
    private static final int CELL_W     = 62;   // largeur d'une cellule
    private static final int CELL_H     = 28;   // hauteur d'une cellule
    private static final int MARGIN_L   = 72;   // marge gauche (étiquettes âge)
    private static final int MARGIN_T   = 95;   // marge haute (titre + étiquettes rendement)
    private static final int MARGIN_R   = 260;  // marge droite (légende)
    private static final int MARGIN_B   = 50;   // marge basse
    private static final int NOTE_H     = 22;

    private GenerateurHeatmap() {}

    // ── Point d'entrée public ─────────────────────────────────────────────────

    /**
     * Génère et sauvegarde la heatmap depuis une liste de lignes CSV.
     *
     * @param lignes         Toutes les lignes du CSV (tous âges, tous rendements)
     * @param titre          Titre principal affiché en haut
     * @param nomB23         Nom court du véhicule B23 (ex. "EP B23")
     * @param nomCT          Nom court du véhicule CT (ex. "CT")
     * @param cheminSortie   Chemin du PNG de sortie
     */
    public static void generer(
            List<LigneRapportCSV> lignes,
            String titre,
            String nomB23,
            String nomCT,
            String cheminSortie) throws IOException {

        // ── Extraction des axes ───────────────────────────────────────────────
        List<Integer> ages = lignes.stream()
                .map(LigneRapportCSV::ageDebut)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Double> rendements = lignes.stream()
                .map(LigneRapportCSV::rendementPct)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Index rapide : (ageDebut, rendement) → ligne
        Map<String, LigneRapportCSV> index = new HashMap<>();
        lignes.forEach(l -> index.put(cle(l.ageDebut(), l.rendementPct()), l));

        int nCols = rendements.size();  // X
        int nRows = ages.size();        // Y

        int imgW = MARGIN_L + nCols * CELL_W + MARGIN_R;
        int imgH = MARGIN_T + nRows * CELL_H + MARGIN_B + NOTE_H;

        BufferedImage image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D    g2    = creerGraphics(image, imgW, imgH);

        dessinerTitre(g2, titre, imgW);
        dessinerEtiquettesRendement(g2, rendements);
        dessinerCellules(g2, ages, rendements, index, nomB23, nomCT);
        dessinerEtiquettesAge(g2, ages);
        dessinerLegende(g2, nomB23, nomCT, imgW, imgH);
        dessinerNote(g2, imgW, imgH);

        g2.dispose();

        File fichier = new File(cheminSortie);
        fichier.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", fichier);
        System.out.println("✓ Heatmap sauvegardée : " + fichier.getAbsolutePath());
    }

    // ── Dessin des cellules ───────────────────────────────────────────────────

    private static void dessinerCellules(
            Graphics2D g2,
            List<Integer> ages,
            List<Double> rendements,
            Map<String, LigneRapportCSV> index,
            String nomB23,
            String nomCT) {

        for (int row = 0; row < ages.size(); row++) {
            int age = ages.get(row);

            for (int col = 0; col < rendements.size(); col++) {
                double rend = rendements.get(col);
                LigneRapportCSV l = index.get(cle(age, rend));

                int x = MARGIN_L + col * CELL_W;
                int y = MARGIN_T + row * CELL_H;

                // ── Couleur de fond ───────────────────────────────────────────
                Color fond = couleurCellule(l, nomB23);
                g2.setColor(fond);
                g2.fillRect(x, y, CELL_W, CELL_H);

                // ── Bordure fine ──────────────────────────────────────────────
                g2.setColor(new Color(255, 255, 255, 60));
                g2.drawRect(x, y, CELL_W - 1, CELL_H - 1);

                // ── Annotation : premier croisement ──────────────────────────
                if (l != null && !Double.isNaN(l.premierCroisement())
                        && l.premierCroisement() > 1.0) {
                    String texte = "%,.0f€".formatted(l.premierCroisement());
                    g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g2.setColor(couleurTexte(fond));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_W - fm.stringWidth(texte)) / 2;
                    int ty = y + CELL_H / 2 + fm.getAscent() / 2 - 1;
                    g2.drawString(texte, tx, ty);
                }
            }
        }
    }

    /**
     * Calcule la couleur de fond d'une cellule selon le dominant et le taux de dominance.
     * Plus le taux est élevé, plus la couleur est foncée.
     */
    private static Color couleurCellule(LigneRapportCSV l, String nomB23) {
        if (l == null) return Color.LIGHT_GRAY;

        boolean b23Domine = l.dominant().contains(nomB23)
                || l.dominant().startsWith("EP")
                || l.dominant().startsWith("ELT");
        boolean exAequo   = l.dominant().contains("æquo") || l.dominant().equals("Aucun");

        if (exAequo) return BASE_EX;

        // Intensité : 0.3 (dominance faible) → 1.0 (dominance totale)
        double taux = b23Domine ? l.tauxDominanceA() : l.tauxDominanceB();
        double intensite = 0.30 + (taux / 100.0) * 0.70;

        Color base = b23Domine ? BASE_B23 : BASE_CT;
        return melangerAvecBlanc(base, intensite);
    }

    /**
     * Mélange une couleur de base avec du blanc selon l'intensité.
     * intensite=1.0 → couleur pure ; intensite=0.0 → blanc.
     */
    private static Color melangerAvecBlanc(Color base, double intensite) {
        int r = (int) (base.getRed()   * intensite + 255 * (1 - intensite));
        int g = (int) (base.getGreen() * intensite + 255 * (1 - intensite));
        int b = (int) (base.getBlue()  * intensite + 255 * (1 - intensite));
        return new Color(
                Math.min(255, Math.max(0, r)),
                Math.min(255, Math.max(0, g)),
                Math.min(255, Math.max(0, b)));
    }

    /**
     * Choisit blanc ou noir pour le texte selon la luminosité du fond.
     */
    private static Color couleurTexte(Color fond) {
        double luminosite = 0.299 * fond.getRed()
                + 0.587 * fond.getGreen()
                + 0.114 * fond.getBlue();
        return luminosite < 140 ? Color.WHITE : new Color(30, 30, 30);
    }

    // ── Étiquettes ────────────────────────────────────────────────────────────

    private static void dessinerEtiquettesRendement(Graphics2D g2, List<Double> rendements) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.BLACK);

        // Label axe X
        String labelX = "Rendement annuel brut";
        FontMetrics fm = g2.getFontMetrics();
        int totalLargeur = rendements.size() * CELL_W;
        g2.drawString(labelX,
                MARGIN_L + (totalLargeur - fm.stringWidth(labelX)) / 2,
                MARGIN_T - 52);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();

        for (int col = 0; col < rendements.size(); col++) {
            String label = "%.0f%%".formatted(rendements.get(col));
            int x = MARGIN_L + col * CELL_W + (CELL_W - fm.stringWidth(label)) / 2;
            g2.drawString(label, x, MARGIN_T - 30);
        }

        // Ligne séparatrice sous les étiquettes
        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(MARGIN_L, MARGIN_T - 14,
                MARGIN_L + rendements.size() * CELL_W, MARGIN_T - 14);
        g2.setColor(Color.BLACK);
    }

    private static void dessinerEtiquettesAge(Graphics2D g2, List<Integer> ages) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.BLACK);

        // Label axe Y
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2r.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2r.setColor(Color.BLACK);

        int totalHauteur = ages.size() * CELL_H;
        g2r.rotate(-Math.PI / 2,
                18,
                MARGIN_T + totalHauteur / 2);
        String labelY = "Âge de début des versements";
        FontMetrics fm = g2r.getFontMetrics();
        g2r.drawString(labelY,
                18 - fm.stringWidth(labelY) / 2,
                MARGIN_T + totalHauteur / 2 + fm.getAscent() / 2);
        g2r.dispose();

        // Étiquettes numériques
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        for (int row = 0; row < ages.size(); row++) {
            String label = ages.get(row) + " ans";
            int x = MARGIN_L - fm.stringWidth(label) - 6;
            int y = MARGIN_T + row * CELL_H + CELL_H / 2 + fm.getAscent() / 2 - 2;
            g2.setColor(Color.BLACK);
            g2.drawString(label, x, y);
        }
    }

    // ── Légende ───────────────────────────────────────────────────────────────

    private static void dessinerLegende(Graphics2D g2, String nomB23, String nomCT,
                                         int imgW, int imgH) {
        int lx = imgW - MARGIN_R + 24;
        int ly = MARGIN_T;

        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.BLACK);
        g2.drawString("Légende", lx, ly);
        ly += 22;

        // ── Dégradé B23 ──────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(nomB23 + " dominant", lx, ly);
        ly += 14;
        dessinerGradientLegend(g2, lx, ly, BASE_B23);
        ly += 22;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("← faible  |  fort →", lx, ly);
        ly += 22;

        // ── Dégradé CT ───────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString(nomCT + " dominant", lx, ly);
        ly += 14;
        dessinerGradientLegend(g2, lx, ly, BASE_CT);
        ly += 22;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("← faible  |  fort →", lx, ly);
        ly += 22;

        // ── Ex æquo ──────────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString("Ex æquo", lx, ly);
        ly += 14;
        g2.setColor(BASE_EX);
        g2.fillRect(lx, ly, 120, 18);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(lx, ly, 119, 17);
        ly += 34;

        // ── Explication annotation ────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString("Annotation dans la cellule", lx, ly);
        ly += 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.DARK_GRAY);
        String[] lignesAnnot = {
                "→ Versement annuel (€/an)",
                "  au premier croisement.",
                "Si cellule vide : pas de",
                "croisement dans la plage."
        };
        for (String ligne : lignesAnnot) {
            g2.drawString(ligne, lx, ly);
            ly += 13;
        }
        ly += 8;

        // ── Interprétation ────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString("Comment lire :", lx, ly);
        ly += 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(Color.DARK_GRAY);
        String[] lignesLire = {
                "Couleur = quel instrument",
                "est globalement avantageux",
                "sur toute la plage de",
                "versements (0–max €/an).",
                " ",
                "Intensité = force de cette",
                "dominance (taux de la plage",
                "où il est en tête).",
                " ",
                "Cellule bleue foncée =",
                "B23 domine fortement sur",
                "presque tous les versements.",
                " ",
                "Cellule verte avec chiffre =",
                "CT gagne à partir de ce",
                "versement annuel."
        };
        for (String ligne : lignesLire) {
            g2.drawString(ligne, lx, ly);
            ly += 13;
        }
    }

    private static void dessinerGradientLegend(Graphics2D g2, int x, int y, Color base) {
        int w = 120, h = 18;
        for (int i = 0; i < w; i++) {
            double intensite = 0.30 + (i / (double) w) * 0.70;
            g2.setColor(melangerAvecBlanc(base, intensite));
            g2.fillRect(x + i, y, 1, h);
        }
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(x, y, w - 1, h - 1);
    }

    // ── Éléments de page ──────────────────────────────────────────────────────

    private static void dessinerTitre(Graphics2D g2, String titre, int imgW) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titre, (imgW - MARGIN_R - fm.stringWidth(titre)) / 2 - 10, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(80, 80, 80));
        String sub = "Dominance globale par (âge de début × rendement)  —  intensité = force de la dominance";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (imgW - MARGIN_R - fm.stringWidth(sub)) / 2 - 10, 48);
    }

    private static void dessinerNote(Graphics2D g2, int imgW, int imgH) {
        String note = "⚠ Simulation à titre illustratif — frais à 0%, actif sous-jacent identique, "
                    + "fiscalité belge 2026 — résultats sensibles aux hypothèses.";
        g2.setFont(new Font("SansSerif", Font.ITALIC, 8));
        g2.setColor(Color.GRAY);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(note, (imgW - fm.stringWidth(note)) / 2, imgH - 6);
    }

    private static Graphics2D creerGraphics(BufferedImage image, int w, int h) {
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, h);
        return g2;
    }

    private static String cle(int age, double rend) {
        return age + "_" + ("%.2f".formatted(rend).replace(',', '.'));
    }
}
