package be.lsm.tfe.common;

import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.IntStream;

@Deprecated
public final class RendeurGraphique {

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color C_VEHICULE_A  = new Color(0x2980b9);  // bleu
    private static final Color C_VEHICULE_B  = new Color(0x27ae60);  // vert
    private static final Color C_CROISEMENT  = new Color(0xe74c3c);  // rouge
    private static final Color C_FOND        = Color.WHITE;

    // ── Dimensions ────────────────────────────────────────────────────────────
    private static final int IMG_W    = 1200;
    private static final int IMG_H    = 700;
    private static final int TITLE_H  = 55;
    private static final int NOTE_H   = 22;
    private static final int CHART_H  = IMG_H - TITLE_H - NOTE_H;

    private RendeurGraphique() {}

    public static void generer(
            List<ResultatSimulation> resultatsA,
            List<ResultatSimulation> resultatsB,
            List<PointCroisement>    croisements,
            String                   nomA,
            String                   nomB,
            String                   titrePrincipal,
            ProfilInvestisseur       profil,
            ParametresRendement      rendement,
            String                   cheminSortie) throws IOException {

        JFreeChart chart = construireChart(
                resultatsA, resultatsB, croisements, nomA, nomB);

        BufferedImage image = new BufferedImage(IMG_W, IMG_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = creerGraphics(image);

        dessinerTitre(g2, titrePrincipal, profil, rendement);
        chart.draw(g2, new Rectangle2D.Double(0, TITLE_H, IMG_W, CHART_H));
        dessinerNote(g2);

        g2.dispose();

        File fichier = new File(cheminSortie);
        fichier.getParentFile().mkdirs();
        ImageIO.write(image, "PNG", fichier);
        System.out.println("✓ Graphique sauvegardé : " + fichier.getAbsolutePath());
    }

    // ── Construction du graphique JFreeChart ──────────────────────────────────

    private static JFreeChart construireChart(
            List<ResultatSimulation> resultatsA,
            List<ResultatSimulation> resultatsB,
            List<PointCroisement>    croisements,
            String nomA, String nomB) {

        XYSeriesCollection dataset = construireDataset(resultatsA, resultatsB, nomA, nomB);
        JFreeChart chart = ChartFactory.createXYLineChart(
                null, "Versement annuel (€)", "VAN à l'échéance (€)",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        chart.setBackgroundPaint(C_FOND);
        chart.getLegend().setBackgroundPaint(C_FOND);

        XYPlot plot = chart.getXYPlot();
        configurerPlot(plot);
        configurerAxes(plot);
        configurerRenderer(plot, nomA, nomB);
        ajouterCroisements(plot, croisements);

        return chart;
    }

    private static XYSeriesCollection construireDataset(
            List<ResultatSimulation> a, List<ResultatSimulation> b,
            String nomA, String nomB) {

        XYSeries serA = new XYSeries(nomA);
        XYSeries serB = new XYSeries(nomB);

        IntStream.range(0, a.size()).forEach(i -> {
            serA.add(a.get(i).versementAnnuel(), a.get(i).vanTotale());
            serB.add(b.get(i).versementAnnuel(), b.get(i).vanTotale());
        });

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serA);
        dataset.addSeries(serB);
        return dataset;
    }

    private static void configurerPlot(XYPlot plot) {
        plot.setBackgroundPaint(C_FOND);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));
    }

    private static void configurerAxes(XYPlot plot) {
        NumberAxis axeX = (NumberAxis) plot.getDomainAxis();
        axeX.setNumberFormatOverride(new DecimalFormat("#,###€"));
        axeX.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        NumberAxis axeY = (NumberAxis) plot.getRangeAxis();
        axeY.setNumberFormatOverride(new DecimalFormat("#,###€"));
        axeY.setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
    }

    private static void configurerRenderer(XYPlot plot, String nomA, String nomB) {
        XYLineAndShapeRenderer r = new XYLineAndShapeRenderer(true, false);
        r.setSeriesPaint(0, C_VEHICULE_A);
        r.setSeriesPaint(1, C_VEHICULE_B);
        r.setSeriesStroke(0, new BasicStroke(2.2f));
        r.setSeriesStroke(1, new BasicStroke(2.2f));
        plot.setRenderer(r);
    }

    private static void ajouterCroisements(XYPlot plot, List<PointCroisement> croisements) {
        croisements.forEach(c -> {
            ValueMarker marker = new ValueMarker(c.versementEuros(), C_CROISEMENT,
                    new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                            1f, new float[]{6f, 4f}, 0f));
            marker.setLabel(String.format("%.0f€/an\n→ %s", c.versementEuros(), c.vehiculeAvantageuxApres()));
            marker.setLabelFont(new Font("SansSerif", Font.BOLD, 9));
            marker.setLabelPaint(C_CROISEMENT);
            marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_RIGHT);
            marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.TOP_LEFT);
            plot.addDomainMarker(marker);
        });
    }

    // ── Dessin des éléments textuels ──────────────────────────────────────────

    private static Graphics2D creerGraphics(BufferedImage image) {
        Graphics2D g2 = image.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g2.setColor(C_FOND);
        g2.fillRect(0, 0, IMG_W, IMG_H);
        return g2;
    }

    private static void dessinerTitre(Graphics2D g2, String titre,
                                       ProfilInvestisseur profil,
                                       ParametresRendement rendement) {
        g2.setColor(Color.BLACK);
        g2.setFont(new Font("SansSerif", Font.BOLD, 15));
        centrer(g2, titre, 22);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        String sousTitre = "%s  —  Rendement %.0f%%/an  —  OLO %.1f%%/an"
                .formatted(profil, rendement.rendementAnnuel() * 100, rendement.tauxOLO() * 100);
        centrer(g2, sousTitre, 40);
    }

    private static void dessinerNote(Graphics2D g2) {
        String note = "⚠ Simulation à titre illustratif — frais, rendement et fiscalité à adapter à votre situation réelle.";
        g2.setFont(new Font("SansSerif", Font.ITALIC, 8));
        g2.setColor(Color.GRAY);
        centrer(g2, note, IMG_H - 6);
    }

    private static void centrer(Graphics2D g2, String texte, int y) {
        int x = (IMG_W - g2.getFontMetrics().stringWidth(texte)) / 2;
        g2.drawString(texte, x, y);
    }
}
