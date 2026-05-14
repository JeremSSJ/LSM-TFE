package be.lsm.tfe.ct;

import be.lsm.tfe.common.*;

import java.util.ArrayList;
import java.util.List;

public final class SimulateurCT implements Simulateur {

    private final ParametresCT params;

    public SimulateurCT(ParametresCT params) {
        this.params = params;
    }

    @Override
    public String nomVehicule() {
        return "Compte-Titres";
    }

    @Override
    public ResultatSimulation simuler(ProfilInvestisseur profil,
                                      double versementAnnuel,
                                      ParametresRendement rendement) {

        AccumulationResult accumulation = simulerAnnees(profil, versementAnnuel, rendement);
        return construireResultat(profil, versementAnnuel, rendement, accumulation);
    }

    public AccumulationResult simulerAnnees(ProfilInvestisseur profil,
                                            double versementAnnuel,
                                            ParametresRendement rendement) {

        List<ResultatAnnuel> annees = new ArrayList<>();
        double reserve = 0.0;
        double miseDeBase = 0.0;
        double taxeCompteTitresTotale = 0.0;

        for (int annee = profil.anneeDebutVersements();
             annee <= profil.anneeFinVersements();
             annee++) {

            int age = profil.ageEnAnnee(annee);

            miseDeBase += versementAnnuel;

            double reserveAvantVersement = age == profil.ageDebut()
                    ? 0.0
                    : capitaliserReserve(reserve, rendement);

            double taxeCompteTitresAnnuelle = 0.0;
            reserve = reserveAvantVersement - taxeCompteTitresAnnuelle + versementAnnuel;
            taxeCompteTitresTotale += taxeCompteTitresAnnuelle;

            annees.add(ResultatAnnuel.sansAnticipative(annee, age, reserve, versementAnnuel, 0.0));
        }

        return new AccumulationResult(annees, reserve, miseDeBase, taxeCompteTitresTotale);
    }

    public ResultatSimulation construireResultat(ProfilInvestisseur profil,
                                                 double versementAnnuel,
                                                 ParametresRendement rendement,
                                                 AccumulationResult accumulation) {

        double capitalNet = calculerCapitalNet(accumulation.reserve(),
                accumulation.coutDeBase(),
                profil.dureeAnnees());

        // Capitalisation des frais (TOB + courtage) vers l'échéance au taux OLO net de la durée
        double coutTOBParAn   = versementAnnuel * params.taxeOperationsBourse();
        double coutFraisParAn = versementAnnuel * params.fraisParVersement();
        double feesCapitalises = CalculateurCapitalisation.capitaliserCoutsAnnuels(
                coutTOBParAn + coutFraisParAn, profil.dureeAnnees());

        double valeurTerminale = capitalNet - feesCapitalises;

        return new ResultatSimulation(
                versementAnnuel,
                accumulation.reserve(),
                capitalNet,
                0.0,
                feesCapitalises,
                valeurTerminale,
                accumulation.taxeCompteTitresTotale(),
                accumulation.annees());
    }

    public double calculerTaxeCompteTitres(double reserveFinAnneeAvantTaxe) {
        if (reserveFinAnneeAvantTaxe <= Constants.TCT_SEUIL) {
            return 0.0;
        }

        double taxeNormale = reserveFinAnneeAvantTaxe * Constants.TCT_TAUX;
        double plafond = (reserveFinAnneeAvantTaxe - Constants.TCT_SEUIL)
                * Constants.TCT_TAUX_PLAFOND_DEPASSEMENT;

        return Math.min(taxeNormale, plafond);
    }

    public double capitaliserReserve(double reserve, ParametresRendement rendement) {
        return reserve
                * (1.0 + rendement.rendementAnnuel())
                * (1.0 - params.fraisGestionAnnuels());
    }

    //on utilise le prix de revient moyen pondéré même si en belgique on va faire du fifo
    //étant donné que dans notre cas on vend tout d'un coût et pas petit à petit dans le temps
    //les deux manières de faire sont équivalentes
    public double calculerCapitalNet(double reserve, double miseTotale, int dureeAnnees) {
        double plusValue = Math.max(0.0, reserve - miseTotale);
        double taxePV = params.exoneration().calculerTaxe(plusValue, params.tauxTaxePlusValues(), dureeAnnees);
        return reserve - taxePV;
    }

    public record AccumulationResult(
            List<ResultatAnnuel> annees,
            double reserve,
            double coutDeBase,
            double taxeCompteTitresTotale
    ) {
    }
}
