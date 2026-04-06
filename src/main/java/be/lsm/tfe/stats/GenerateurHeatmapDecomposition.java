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
 * Génère une heatmap 2D (âge de début × rendement) en 3 couleurs,
 * révélant l'origine de l'avantage comparatif de la Branche 23.
 *
 * <h3>Logique des 3 couleurs</h3>
 *
 * Pour chaque cellule (ageDebut, rendement), on compare les VAN moyennes
 * des 3 composantes issues du CSV :
 *
 * <ul>
 *   <li><b>Bleu foncé — B23 structurellement supérieur</b> :
 *       {@code vanCapitalB23 > vanCapitalCT}
 *       → Le capital seul (hors économies fiscales) du B23 dépasse déjà le CT.
 *       L'avantage est structurel et ne repose pas sur la fiscalité d'entrée.</li>
 *
 *   <li><b>Bleu clair — B23 supérieur grâce aux économies fiscales uniquement</b> :
 *       {@code vanCapitalB23 <= vanCapitalCT}
 *       ET {@code vanCapitalB23 + vanEcoFiscales > vanCapitalCT}
 *       → Sans les réductions d'impôt, le CT serait meilleur.
 *       C'est l'avantage fiscal d'entrée qui fait basculer la balance.</li>
 *
 *   <li><b>Vert — CT supérieur</b> :
 *       {@code vanCapitalB23 + vanEcoFiscales <= vanCapitalCT}
 *       → Même en cumulant capital et économies fiscales, le CT l'emporte.</li>
 * </ul>
 *
 * <h3>Axes</h3>
 * X = Rendement annuel brut (1 % → 15 %) |
 * Y = Âge de début des versements (18 → 54 ans)
 */
public final class GenerateurHeatmapDecomposition {

    // ── Couleurs ──────────────────────────────────────────────────────────────
    /** B23 domine structurellement (capital seul > CT). */
    private static final Color BLEU_FONCE  = new Color(0x1a5276);
    /** B23 domine uniquement grâce aux économies fiscales. */
    private static final Color BLEU_CLAIR  = new Color(0x85c1e9);
    /** CT domine même après économies fiscales. */
    private static final Color VERT        = new Color(0x1e8449);

    // ── Dimensions ────────────────────────────────────────────────────────────
    private static final int CELL_W   = 62;
    private static final int CELL_H   = 28;
    private static final int MARGIN_L = 72;
    private static final int MARGIN_T = 95;
    private static final int MARGIN_R = 300;
    private static final int MARGIN_B = 50;
    private static final int NOTE_H   = 22;

    private GenerateurHeatmapDecomposition() {}

    // ── Point d'entrée public ─────────────────────────────────────────────────

    /**
     * Génère et sauvegarde la heatmap de décomposition.
     *
     * @param lignes        Toutes les lignes du CSV (tous âges, tous rendements)
     * @param titre         Titre principal affiché en haut
     * @param nomB23        Nom court du véhicule B23 (ex. "EP B23")
     * @param nomCT         Nom court du CT (ex. "CT")
     * @param cheminSortie  Chemin du PNG de sortie
     */
    public static void generer(
            List<LigneRapportCSV> lignes,
            String titre,
            String nomB23,
            String nomCT,
            String cheminSortie) throws IOException {

        List<Integer> ages = lignes.stream()
                .map(LigneRapportCSV::ageDebut).distinct().sorted()
                .collect(Collectors.toList());

        List<Double> rendements = lignes.stream()
                .map(LigneRapportCSV::rendementPct).distinct().sorted()
                .collect(Collectors.toList());

        Map<String, LigneRapportCSV> index = new HashMap<>();
        lignes.forEach(l -> index.put(cle(l.ageDebut(), l.rendementPct()), l));

        int nCols = rendements.size();
        int nRows = ages.size();

        int imgW = MARGIN_L + nCols * CELL_W + MARGIN_R;
        int imgH = MARGIN_T + nRows * CELL_H + MARGIN_B + NOTE_H;

        BufferedImage image = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = creerGraphics(image, imgW, imgH);

        dessinerTitre(g2, titre, imgW);
        dessinerEtiquettesRendement(g2, rendements);
        dessinerCellules(g2, ages, rendements, index);
        dessinerEtiquettesAge(g2, ages);
        dessinerLegende(g2, nomB23, nomCT, imgW);
        dessinerNote(g2, imgW, imgH);

        g2.dispose();

        File fichier = new File(cheminSortie);
        fichier.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", fichier);
        System.out.println("✓ Heatmap décomposition sauvegardée : " + fichier.getAbsolutePath());
    }

    // ── Catégorisation ────────────────────────────────────────────────────────

    private enum Categorie {
        B23_STRUCTUREL,   // capital B23 seul > CT
        B23_FISCAL,       // capital B23 <= CT, mais capital B23 + éco. fiscales > CT
        CT_DOMINANT       // CT gagne même après éco. fiscales
    }

    /**
     * Détermine la catégorie d'une cellule à partir des 3 VAN moyennes.
     * Une tolérance numérique de 1€ évite les faux positifs sur les valeurs égales.
     */
    private static Categorie categoriser(LigneRapportCSV l) {
        double capB23 = l.vanMoyCapitalB23();
        double ecoB23 = l.vanMoyEcoFiscalesB23();
        double capCT  = l.vanMoyCapitalCT();

        if (capB23 > capCT + 1.0) {
            return Categorie.B23_STRUCTUREL;
        } else if (capB23 + ecoB23 > capCT + 1.0) {
            return Categorie.B23_FISCAL;
        } else {
            return Categorie.CT_DOMINANT;
        }
    }

    private static Color couleurDe(Categorie cat) {
        return switch (cat) {
            case B23_STRUCTUREL -> BLEU_FONCE;
            case B23_FISCAL     -> BLEU_CLAIR;
            case CT_DOMINANT    -> VERT;
        };
    }

    // ── Dessin des cellules ───────────────────────────────────────────────────

    private static void dessinerCellules(
            Graphics2D g2,
            List<Integer> ages,
            List<Double> rendements,
            Map<String, LigneRapportCSV> index) {

        for (int row = 0; row < ages.size(); row++) {
            for (int col = 0; col < rendements.size(); col++) {

                LigneRapportCSV l = index.get(cle(ages.get(row), rendements.get(col)));

                int x = MARGIN_L + col * CELL_W;
                int y = MARGIN_T + row * CELL_H;

                // ── Fond ─────────────────────────────────────────────────────
                if (l == null) {
                    g2.setColor(Color.LIGHT_GRAY);
                    g2.fillRect(x, y, CELL_W, CELL_H);
                } else {
                    Categorie cat   = categoriser(l);
                    Color     fond  = couleurDe(cat);
                    g2.setColor(fond);
                    g2.fillRect(x, y, CELL_W, CELL_H);

                    // ── Annotation : écart en € entre B23 total et CT ─────────
                    // Montre de combien (en VAN moyenne) l'instrument gagnant domine.
                    double ecart = (l.vanMoyCapitalB23() + l.vanMoyEcoFiscalesB23())
                                 - l.vanMoyCapitalCT();
                    String texte = "%+,.0f€".formatted(ecart);

                    g2.setFont(new Font("SansSerif", Font.PLAIN, 8));
                    g2.setColor(couleurTexte(fond));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_W - fm.stringWidth(texte)) / 2;
                    int ty = y + CELL_H / 2 + fm.getAscent() / 2 - 1;
                    g2.drawString(texte, tx, ty);
                }

                // ── Bordure fine ──────────────────────────────────────────────
                g2.setColor(new Color(255, 255, 255, 50));
                g2.drawRect(x, y, CELL_W - 1, CELL_H - 1);
            }
        }
    }

    // ── Étiquettes ────────────────────────────────────────────────────────────

    private static void dessinerEtiquettesRendement(Graphics2D g2, List<Double> rendements) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2.setColor(Color.BLACK);
        String labelX = "Rendement annuel brut";
        FontMetrics fm = g2.getFontMetrics();
        int totalL = rendements.size() * CELL_W;
        g2.drawString(labelX, MARGIN_L + (totalL - fm.stringWidth(labelX)) / 2, MARGIN_T - 52);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        fm = g2.getFontMetrics();
        for (int col = 0; col < rendements.size(); col++) {
            String label = "%.0f%%".formatted(rendements.get(col));
            int x = MARGIN_L + col * CELL_W + (CELL_W - fm.stringWidth(label)) / 2;
            g2.drawString(label, x, MARGIN_T - 30);
        }

        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(MARGIN_L, MARGIN_T - 14,
                MARGIN_L + rendements.size() * CELL_W, MARGIN_T - 14);
    }

    private static void dessinerEtiquettesAge(Graphics2D g2, List<Integer> ages) {
        // Label axe Y tourné
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2r.setFont(new Font("SansSerif", Font.BOLD, 11));
        g2r.setColor(Color.BLACK);
        int totalH = ages.size() * CELL_H;
        g2r.rotate(-Math.PI / 2, 18, MARGIN_T + totalH / 2);
        String labelY = "Âge de début des versements";
        FontMetrics fm = g2r.getFontMetrics();
        g2r.drawString(labelY, 18 - fm.stringWidth(labelY) / 2,
                MARGIN_T + totalH / 2 + fm.getAscent() / 2);
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

    private static void dessinerLegende(Graphics2D g2, String nomB23, String nomCT, int imgW) {

        int lx = imgW - MARGIN_R + 24;
        int ly = MARGIN_T;

        // ── Titre légende ─────────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(Color.BLACK);
        g2.drawString("Légende", lx, ly);
        ly += 26;

        // ── Case 1 : B23 structurel ───────────────────────────────────────────
        g2.setColor(BLEU_FONCE);
        g2.fillRect(lx, ly, 22, 22);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(lx, ly, 21, 21);

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString(nomB23 + " domine sans éco. fiscales", lx + 28, ly + 9);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("vanCapital(B23)  >  vanCapital(CT)", lx + 28, ly + 20);
        ly += 38;

        // ── Explication case 1 ────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(80, 80, 80));
        String[] exp1 = {
                "Le capital accumulé dans le contrat",
                "B23, net de taxe anticipative et",
                "actualisé, dépasse déjà le CT.",
                "Les réductions d'impôt sont un bonus."
        };
        for (String s : exp1) { g2.drawString(s, lx + 6, ly); ly += 13; }
        ly += 10;

        // ── Case 2 : B23 fiscal ───────────────────────────────────────────────
        g2.setColor(BLEU_CLAIR);
        g2.fillRect(lx, ly, 22, 22);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(lx, ly, 21, 21);

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString(nomB23 + " domine grâce aux éco. fiscales", lx + 28, ly + 9);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("vanCapital(B23)  ≤  vanCapital(CT)", lx + 28, ly + 20);
        ly += 38;

        // ── Explication case 2 ────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(80, 80, 80));
        String[] exp2 = {
                "Sans les réductions d'impôt, le CT",
                "serait plus avantageux. C'est l'avantage",
                "fiscal à l'entrée (30%/25% sur les",
                "versements) qui fait basculer la balance."
        };
        for (String s : exp2) { g2.drawString(s, lx + 6, ly); ly += 13; }
        ly += 10;

        // ── Case 3 : CT dominant ──────────────────────────────────────────────
        g2.setColor(VERT);
        g2.fillRect(lx, ly, 22, 22);
        g2.setColor(new Color(200, 200, 200));
        g2.drawRect(lx, ly, 21, 21);

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString(nomCT + " domine", lx + 28, ly + 9);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("vanCapital(B23) + vanEco  ≤  vanCapital(CT)", lx + 28, ly + 20);
        ly += 38;

        // ── Explication case 3 ────────────────────────────────────────────────
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(80, 80, 80));
        String[] exp3 = {
                "Même en cumulant capital B23 et",
                "économies fiscales, le CT l'emporte.",
                "Typiquement : rendement élevé où",
                "la taxe anticipative pèse lourd, ou",
                "horizon court (peu de réductions)."
        };
        for (String s : exp3) { g2.drawString(s, lx + 6, ly); ly += 13; }
        ly += 14;

        // ── Explication annotation ────────────────────────────────────────────
        g2.setColor(new Color(100, 100, 100));
        g2.drawLine(lx, ly, lx + 260, ly);
        ly += 12;

        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.setColor(Color.BLACK);
        g2.drawString("Chiffre dans la cellule", lx, ly);
        ly += 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
        g2.setColor(new Color(80, 80, 80));
        String[] expAnn = {
                "Écart moyen en VAN sur la plage",
                "de versements : (capB23 + écoB23) − capCT",
                "+ = B23 avantageux",
                "− = CT avantageux"
        };
        for (String s : expAnn) { g2.drawString(s, lx + 6, ly); ly += 13; }
    }

    // ── Éléments de page ──────────────────────────────────────────────────────

    private static void dessinerTitre(Graphics2D g2, String titre, int imgW) {
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        g2.setColor(Color.BLACK);
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(titre, (imgW - MARGIN_R - fm.stringWidth(titre)) / 2 - 10, 28);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(80, 80, 80));
        String sub = "Origine de l'avantage : structurel (capital seul) vs fiscal (réductions d'impôt) vs CT dominant";
        fm = g2.getFontMetrics();
        g2.drawString(sub, (imgW - MARGIN_R - fm.stringWidth(sub)) / 2 - 10, 48);
    }

    private static void dessinerNote(Graphics2D g2, int imgW, int imgH) {
        String note = "⚠ Simulation illustrative — frais à 0%, actif sous-jacent identique, "
                + "VAN moyennées sur la plage de versements — fiscalité belge 2026.";
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

    private static Color couleurTexte(Color fond) {
        double lum = 0.299 * fond.getRed() + 0.587 * fond.getGreen() + 0.114 * fond.getBlue();
        return lum < 140 ? Color.WHITE : new Color(30, 30, 30);
    }

    private static String cle(int age, double rend) {
        return age + "_" + ("%.2f".formatted(rend).replace(',', '.'));
    }
}
