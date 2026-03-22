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
    public String nomVehicule() { return "Compte-Titres"; }

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

        List<ResultatAnnuel> annees     = new ArrayList<>();
        double               reserve    = 0.0;
        double               coutDeBase = 0.0;

        for (int annee = profil.anneeDebutVersements();
             annee <= profil.anneeFinVersements();
             annee++) {

            int    age          = profil.ageEnAnnee(annee);
            double versementNet = calculerVersementNet(versementAnnuel);

            reserve    += versementNet;
            coutDeBase += versementNet;

            reserve = capitaliserReserve(reserve, rendement);

            annees.add(ResultatAnnuel.sansAnticipative(annee, age, reserve, versementNet, 0.0));
        }

        return new AccumulationResult(annees, reserve, coutDeBase);
    }

    public ResultatSimulation construireResultat(ProfilInvestisseur profil,
                                                  double versementAnnuel,
                                                  ParametresRendement rendement,
                                                  AccumulationResult accumulation) {

        double capitalNet = calculerCapitalNet(accumulation.reserve(),
                accumulation.coutDeBase(),
                profil.dureeAnnees());

        double vanCap = CalculateurVAN.vanCapital(capitalNet, rendement.tauxOLO(), profil.dureeAnnees());

        return new ResultatSimulation(
                versementAnnuel,
                accumulation.reserve(),
                capitalNet,
                vanCap,
                0.0,
                vanCap,
                accumulation.annees());
    }

    public double calculerVersementNet(double versementAnnuel) {
        return versementAnnuel
                * (1.0 - params.taxeOperationsBourse())
                * (1.0 - params.fraisParVersement());
    }

    public double capitaliserReserve(double reserve, ParametresRendement rendement) {
        return reserve
                * (1.0 + rendement.rendementAnnuel())
                * (1.0 - params.fraisGestionAnnuels());
    }

    public double calculerCapitalNet(double reserve, double coutDeBase, int dureeAnnees) {
        double plusValue = Math.max(0.0, reserve - coutDeBase);
        double taxePV    = params.exoneration().calculerTaxe(
                plusValue, params.tauxTaxePlusValues(), dureeAnnees);
        return reserve - taxePV;
    }

    public record AccumulationResult(
            List<ResultatAnnuel> annees,
            double               reserve,
            double               coutDeBase
    ) {}
}
