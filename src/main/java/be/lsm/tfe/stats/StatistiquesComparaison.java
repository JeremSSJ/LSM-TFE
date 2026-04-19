package be.lsm.tfe.stats;

import java.util.List;
import be.lsm.tfe.common.PointCroisement;

/**
 * Résultat statistique d'une comparaison entre deux véhicules d'investissement
 * sur une plage de versements annuels.
 *
 * @param nomVehiculeA              Nom du véhicule A (ex. "EP Branche 23")
 * @param nomVehiculeB              Nom du véhicule B (ex. "Compte-Titres")
 * @param versementMin              Borne inférieure de la plage simulée (€)
 * @param versementMax              Borne supérieure de la plage simulée (€)
 * @param nbPointsTotal             Nombre de versements simulés
 * @param nbPointsADomine           Nombre de versements où VAN(A) > VAN(B)
 * @param nbPointsBDomine           Nombre de versements où VAN(B) > VAN(A)
 * @param nbPointsEgaux             Nombre de versements où VAN(A) = VAN(B)
 * @param tauxDominanceA            Part de la plage où A domine (en %)
 * @param tauxDominanceB            Part de la plage où B domine (en %)
 * @param croisements               Points de croisement interpolés
 * @param premierCroisement         Versement au premier croisement (NaN si aucun)
 * @param dernierCroisement         Versement au dernier croisement (NaN si <= 1)
 * @param instrumentDominantGlobal  "A", "B", ou "Aucun (ex æquo)" selon l'aire
 * @param vanMoyenneCapitalB23      Moyenne de vanCapital(A) sur toute la plage (€)
 * @param vanMoyenneEcoFiscalesB23  Moyenne de vanEconomiesFiscales(A) sur toute la plage (€)
 * @param vanMoyenneCapitalCT       Moyenne de vanTotale(B) sur toute la plage (€)
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
        double                vanMoyenneCapitalB23,
        double                vanMoyenneEcoFiscalesB23,
        double                vanMoyenneCapitalCT
) {
    public StatistiquesComparaison {
        croisements = List.copyOf(croisements);
    }
}
