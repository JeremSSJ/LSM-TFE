package be.lsm.tfe.branche23;

import be.lsm.tfe.common.*;

import java.util.ArrayList;
import java.util.List;

public final class SimulateurBranche23 implements Simulateur {

    private final ParametresBranche23 params;
    private final String              nom;

    public SimulateurBranche23(ParametresBranche23 params, String nom) {
        this.params = params;
        this.nom    = nom;
    }

    @Override
    public String nomVehicule() { return nom; }

    @Override
    public ResultatSimulation simuler(ProfilInvestisseur profil,
                                      double versementAnnuel,
                                      ParametresRendement rendement) {

        List<ResultatAnnuel> annees = simulerAnnees(profil, versementAnnuel, rendement);
        return construireResultat(profil, versementAnnuel, rendement, annees);
    }

    public List<ResultatAnnuel> simulerAnnees(ProfilInvestisseur profil,
                                               double versementAnnuel,
                                               ParametresRendement rendement) {

        int  anneeAnticipative = determinerAnneeAnticipative(profil);
        List<ResultatAnnuel> annees = new ArrayList<>();

        double  reserve      = 0.0;
        boolean taxeApplique = false;

        for (int annee = profil.anneeDebutVersements();
             annee <= profil.anneeFinVersements();
             annee++) {

            int    age          = profil.ageEnAnnee(annee);
            double versementNet = calculerVersementNet(versementAnnuel);

            reserve = capitaliserReserve(reserve + versementNet, rendement);

            boolean anticipativeCetteAnnee = false;
            if (!taxeApplique && annee == anneeAnticipative) {
                reserve                = appliquerTaxeAnticipative(reserve);
                taxeApplique           = true;
                anticipativeCetteAnnee = true;
            }

            double economie = calculerEconomieFiscale(age, versementAnnuel);

            annees.add(anticipativeCetteAnnee
                    ? ResultatAnnuel.avecAnticipative(annee, age, reserve, versementNet, economie)
                    : ResultatAnnuel.sansAnticipative(annee, age, reserve, versementNet, economie));
        }

        return annees;
    }

    public ResultatSimulation construireResultat(ProfilInvestisseur profil,
                                                  double versementAnnuel,
                                                  ParametresRendement rendement,
                                                  List<ResultatAnnuel> annees) {

        // Branche 23 : pas de taxe PV à la sortie → capitalFinal = capitalFinalNet
        double capitalFinalNet = annees.get(annees.size() - 1).reserveEnFinAnnee();
        int    duree           = profil.dureeAnnees();

        double vanCap = CalculateurVAN.vanCapital(capitalFinalNet, rendement.tauxOLO(), duree);
        double vanEco = CalculateurVAN.vanEconomiesFiscales(annees, rendement.tauxOLO());
        double vanTot = vanCap + vanEco;

        return new ResultatSimulation(
                versementAnnuel,
                capitalFinalNet,
                capitalFinalNet,
                vanCap,
                vanEco,
                vanTot,
                annees);
    }

    public double calculerVersementNet(double versementAnnuel) {
        return versementAnnuel
                * (1.0 - params.taxeOperationsAssurance())
                * (1.0 - params.fraisParPrime());
    }

    public double capitaliserReserve(double reserve, ParametresRendement rendement) {
        return reserve
                * (1.0 + rendement.rendementAnnuel())
                * (1.0 - params.fraisGestionAnnuels());
    }

    public double appliquerTaxeAnticipative(double reserve) {
        return reserve * (1.0 - params.tauxTaxeAnticipative());
    }

    public int determinerAnneeAnticipative(ProfilInvestisseur profil) {
        if (profil.souscritApres55Ans()) {
            return profil.anneeDebutVersements()
                    + params.dureeMinAvantAnticipativeSiSouscritTard();
        }
        return profil.anneeDeAge(params.ageTaxeAnticipative());
    }

    public double calculerEconomieFiscale(int age, double versement) {
        if (age < Constants.EP_AGE_MIN_REDUCTION || age > params.ageLimiteReductionFiscale()) {
            return 0.0;
        }
        return params.regleReductionFiscale().calculer(versement);
    }
}