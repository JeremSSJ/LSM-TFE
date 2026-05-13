package be.lsm.tfe.stats;

/**
 * Représente une ligne du rapport CSV multi-scénarios.
 *
 * @param vehiculeA                          Nom du véhicule A (Branche 23)
 * @param vehiculeB                          Nom du véhicule B (CT)
 * @param rendementPct                       Rendement annuel brut (en %)
 * @param ageDebut                           Âge de début des versements
 * @param ageFin                             Âge de fin des versements
 * @param tauxTaxePVpct                      Taux de la taxe PV sur le CT (en %)
 * @param versementMin                       Versement minimal de la plage (€)
 * @param versementMax                       Versement maximal de la plage (€)
 * @param tauxDominanceA                     % de la plage où A domine
 * @param tauxDominanceB                     % de la plage où B domine
 * @param tauxEgaux                          % de la plage où A et B sont ex æquo
 * @param nbCroisements                      Nombre de points de croisement détectés
 * @param premierCroisement                  Versement au premier croisement (ou NaN)
 * @param dominant                           Instrument dominant global
 * @param capitalFinalNetMoyenB23            Moyenne du capital final net B23 (€)
 *                                           = capital net après taxe anticipative, non actualisé
 * @param ecoFiscalesCapitaliseesMoyennesB23 Moyenne des économies fiscales capitalisées B23 (€)
 *                                           = économies fiscales capitalisées au taux OLO de la durée
 * @param valTerminaleMoyenneCT              Moyenne de la valeur terminale CT (€)
 *                                           = capital net après taxe PV − frais capitalisés
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
        double tauxEgaux,
        int    nbCroisements,
        double premierCroisement,
        String dominant,
        double capitalFinalNetMoyenB23,
        double ecoFiscalesCapitaliseesMoyennesB23,
        double valTerminaleMoyenneCT
) {
    /** En-tête CSV. */
    public static String entete() {
        return String.join(";",
                "vehiculeA", "vehiculeB",
                "rendement_pct", "age_debut", "age_fin", "taux_taxe_pv_pct",
                "versement_min", "versement_max",
                "taux_dominance_A_pct", "taux_dominance_B_pct", "taux_egaux_pct",
                "nb_croisements", "premier_croisement_eur",
                "dominant",
                "capital_final_net_moyen_b23_eur",
                "eco_fiscales_capitalisees_moyennes_b23_eur",
                "val_terminale_moyenne_ct_eur"
        );
    }

    /** Ligne CSV formatée (séparateur point-virgule, décimales avec point). */
    public String toCSV() {
        return String.join(";",
                vehiculeA, vehiculeB,
                fmt(rendementPct), String.valueOf(ageDebut), String.valueOf(ageFin),
                fmt(tauxTaxePVpct),
                String.valueOf(versementMin), String.valueOf(versementMax),
                fmt(tauxDominanceA), fmt(tauxDominanceB), fmt(tauxEgaux),
                String.valueOf(nbCroisements),
                Double.isNaN(premierCroisement) ? "NA" : fmt(premierCroisement),
                dominant,
                fmt(capitalFinalNetMoyenB23),
                fmt(ecoFiscalesCapitaliseesMoyennesB23),
                fmt(valTerminaleMoyenneCT)
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
                100.0 * stats.nbPointsEgaux() / stats.nbPointsTotal(),
                stats.croisements().size(),
                stats.premierCroisement(),
                stats.instrumentDominantGlobal(),
                stats.capitalFinalNetMoyenB23(),
                stats.ecoFiscalesCapitaliseesMoyennesB23(),
                stats.valTerminaleMoyenneCT()
        );
    }
}
