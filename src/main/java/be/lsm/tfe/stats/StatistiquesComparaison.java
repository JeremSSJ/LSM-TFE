package be.lsm.tfe.stats;

import java.util.List;
import be.lsm.tfe.common.PointCroisement;

/**
 * Résultat statistique d'une comparaison entre deux véhicules d'investissement
 * sur une plage de versements annuels.
 *
 * @param nomVehiculeA                        Nom du véhicule A (ex. "EP Branche 23")
 * @param nomVehiculeB                        Nom du véhicule B (ex. "Compte-Titres")
 * @param versementMin                        Borne inférieure de la plage simulée (€)
 * @param versementMax                        Borne supérieure de la plage simulée (€)
 * @param nbPointsTotal                       Nombre de versements simulés
 * @param nbPointsADomine                     Nombre de versements où VT(A) > VT(B)
 * @param nbPointsBDomine                     Nombre de versements où VT(B) > VT(A)
 * @param nbPointsEgaux                       Nombre de versements où VT(A) = VT(B)
 * @param tauxDominanceA                      Part de la plage où A domine (en %)
 * @param tauxDominanceB                      Part de la plage où B domine (en %)
 * @param croisements                         Points de croisement interpolés
 * @param premierCroisement                   Versement au premier croisement (NaN si aucun)
 * @param dernierCroisement                   Versement au dernier croisement (NaN si <= 1)
 * @param instrumentDominantGlobal            "A", "B", ou "Aucun (ex æquo)" selon l'aire
 * @param capitalFinalNetMoyenB23             Moyenne du capital final net B23 sur la plage (€)
 *                                            = capital net après taxe anticipative, non actualisé
 * @param ecoFiscalesCapitaliseesMoyennesB23  Moyenne des économies fiscales capitalisées B23 (€)
 *                                            = économies fiscales capitalisées au taux OLO de la durée
 * @param valTerminaleMoyenneCT               Moyenne de la valeur terminale CT sur la plage (€)
 *                                            = capital net après taxe PV − frais capitalisés
 */
public record StatistiquesComparaison(
        String                nomVehiculeA,
        String                nomVehiculeB,
        int                   versementMin,
        int                   versementMax,
        int                   nbPointsTotal,
        long                  nbPointsADomine,
        long                  nbPointsBDomine,
        long                  nbPointsEgaux,
        double                tauxDominanceA,
        double                tauxDominanceB,
        List<PointCroisement> croisements,
        double                premierCroisement,
        double                dernierCroisement,
        String                instrumentDominantGlobal,
        double                capitalFinalNetMoyenB23,
        double                ecoFiscalesCapitaliseesMoyennesB23,
        double                valTerminaleMoyenneCT
) {
    public StatistiquesComparaison {
        croisements = List.copyOf(croisements);
    }
}
