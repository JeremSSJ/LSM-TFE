package be.lsm.tfe.stats;

/**
 * Représente une ligne du rapport CSV multi-scénarios.
 *
 * @param vehiculeA              Nom du véhicule A (Branche 23)
 * @param vehiculeB              Nom du véhicule B (CT)
 * @param rendementPct           Rendement annuel brut (en %)
 * @param ageDebut               Âge de début des versements
 * @param ageFin                 Âge de fin des versements
 * @param tauxTaxePVpct          Taux de la taxe PV sur le CT (en %)
 * @param versementMin           Versement minimal de la plage (€)
 * @param versementMax           Versement maximal de la plage (€)
 * @param tauxDominanceA         % de la plage où A domine
 * @param tauxDominanceB         % de la plage où B domine
 * @param nbCroisements          Nombre de points de croisement détectés
 * @param premierCroisement      Versement au premier croisement (ou NaN)
 * @param avantMoyA              Avantage moyen de A quand il domine (€)
 * @param avantMoyB              Avantage moyen de B quand il domine (€)
 * @param avantMaxA              Avantage maximum de A sur B (€)
 * @param avantMaxB              Avantage maximum de B sur A (€)
 * @param aireEcarts             Intégrale des écarts VAN(A)-VAN(B) (€)
 * @param dominant               Instrument dominant global
 * @param vanMoyCapitalB23       VAN moyenne du capital B23 sur la plage (€)
 *                               = capital net après taxe anticipative, actualisé, hors éco. fiscales
 * @param vanMoyEcoFiscalesB23   VAN moyenne des économies fiscales B23 sur la plage (€)
 *                               = VAN des réductions d'impôt annuelles actualisées
 * @param vanMoyCapitalCT        VAN moyenne du capital CT sur la plage (€)
 *                               = capital net après taxe PV, actualisé
 */
public record LigneRapportCSV(
        String vehiculeA,
        String vehiculeB,
        double rendementPct,
        int    ageDebut,
        int    ageFin,
        double tauxTaxePVpct,
        int    versementMin,
        int    versementMax,
        double tauxDominanceA,
        double tauxDominanceB,
        int    nbCroisements,
        double premierCroisement,
        double avantMoyA,
        double avantMoyB,
        double avantMaxA,
        double avantMaxB,
        double aireEcarts,
        String dominant,
        // ── 3 nouvelles colonnes ──────────────────────────────────────────────
        double vanMoyCapitalB23,
        double vanMoyEcoFiscalesB23,
        double vanMoyCapitalCT
) {
    /** En-tête CSV. */
    public static String entete() {
        return String.join(";",
                "vehiculeA", "vehiculeB",
                "rendement_pct", "age_debut", "age_fin", "taux_taxe_pv_pct",
                "versement_min", "versement_max",
                "taux_dominance_A_pct", "taux_dominance_B_pct",
                "nb_croisements", "premier_croisement_eur",
                "avantage_moyen_A_eur", "avantage_moyen_B_eur",
                "avantage_max_A_eur", "avantage_max_B_eur",
                "aire_ecarts_eur", "dominant",
                "van_moy_capital_b23_eur",
                "van_moy_eco_fiscales_b23_eur",
                "van_moy_capital_ct_eur"
        );
    }

    /** Ligne CSV formatée (séparateur point-virgule, décimales avec point). */
    public String toCSV() {
        return String.join(";",
                vehiculeA, vehiculeB,
                fmt(rendementPct), String.valueOf(ageDebut), String.valueOf(ageFin),
                fmt(tauxTaxePVpct),
                String.valueOf(versementMin), String.valueOf(versementMax),
                fmt(tauxDominanceA), fmt(tauxDominanceB),
                String.valueOf(nbCroisements),
                Double.isNaN(premierCroisement) ? "NA" : fmt(premierCroisement),
                fmt(avantMoyA), fmt(avantMoyB),
                fmt(avantMaxA), fmt(avantMaxB),
                fmt(aireEcarts),
                dominant,
                fmt(vanMoyCapitalB23),
                fmt(vanMoyEcoFiscalesB23),
                fmt(vanMoyCapitalCT)
        );
    }

    private static String fmt(double v) {
        return "%.2f".formatted(v).replace(',', '.');
    }

    /** Construit une LigneRapportCSV depuis un StatistiquesComparaison et les méta-données. */
    public static LigneRapportCSV depuis(
            StatistiquesComparaison stats,
            double rendementPct,
            int    ageDebut,
            int    ageFin,
            double tauxTaxePVpct) {

        return new LigneRapportCSV(
                stats.nomVehiculeA(),
                stats.nomVehiculeB(),
                rendementPct, ageDebut, ageFin, tauxTaxePVpct,
                stats.versementMin(), stats.versementMax(),
                stats.tauxDominanceA(), stats.tauxDominanceB(),
                stats.croisements().size(),
                stats.premierCroisement(),
                stats.vanDiffMoyenneADomine(),
                stats.vanDiffMoyenneBDomine(),
                stats.vanDiffMaxA(),
                stats.vanDiffMaxB(),
                stats.aireEcartsAvsB(),
                stats.instrumentDominantGlobal(),
                // 3 nouvelles colonnes
                stats.vanMoyenneCapitalB23(),
                stats.vanMoyenneEcoFiscalesB23(),
                stats.vanMoyenneCapitalCT()
        );
    }
}
