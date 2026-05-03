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

        AccumulationResult accumulation = simulerAnneesAvecTaxeCompteTitres(profil, versementAnnuel, rendement);
        return construireResultat(profil, versementAnnuel, accumulation.annees(), accumulation.taxeCompteTitresTotale());
    }

    public List<ResultatAnnuel> simulerAnnees(ProfilInvestisseur profil,
                                               double versementAnnuel,
                                               ParametresRendement rendement) {
        return simulerAnneesAvecTaxeCompteTitres(profil, versementAnnuel, rendement).annees();
    }

    private AccumulationResult simulerAnneesAvecTaxeCompteTitres(ProfilInvestisseur profil,
                                                                  double versementAnnuel,
                                                                  ParametresRendement rendement) {

        int  anneeAnticipative = determinerAnneeAnticipative(profil);
        List<ResultatAnnuel> annees = new ArrayList<>();

        double  reserve      = 0.0;
        boolean taxeApplique = false;
        double taxeCompteTitresTotale = 0.0;

        for (int annee = profil.anneeDebutVersements();
             annee <= profil.anneeFinVersements();
             annee++) {

            int    age          = profil.ageEnAnnee(annee);
            double versementNet = calculerVersementNet(versementAnnuel);
            double reserveAvantVersement = age == profil.ageDebut()
                    ? 0.0
                    : capitaliserReserve(reserve, rendement);

            boolean anticipativeCetteAnnee = false;
            if (!taxeApplique && annee == anneeAnticipative) {
                reserveAvantVersement  = appliquerTaxeAnticipative(reserveAvantVersement);
                taxeApplique           = true;
                anticipativeCetteAnnee = true;
            }

            double economie = calculerEconomieFiscale(age, versementAnnuel);

            double taxeCompteTitresAnnuelle = calculerTaxeCompteTitres(reserveAvantVersement);
            reserve = reserveAvantVersement - taxeCompteTitresAnnuelle + versementNet;
            taxeCompteTitresTotale += taxeCompteTitresAnnuelle;

            annees.add(anticipativeCetteAnnee
                    ? ResultatAnnuel.avecAnticipative(annee, age, reserve, versementNet, economie)
                    : ResultatAnnuel.sansAnticipative(annee, age, reserve, versementNet, economie));
        }

        return new AccumulationResult(annees, taxeCompteTitresTotale);
    }

    public ResultatSimulation construireResultat(ProfilInvestisseur profil,
                                                 double versementAnnuel,
                                                 List<ResultatAnnuel> annees,
                                                 double taxeCompteTitresTotale) {

        // Branche 23 : pas de taxe PV à la sortie → capitalFinal = capitalFinalNet
        double capitalFinalNet = annees.get(annees.size() - 1).reserveEnFinAnnee();
        int    duree           = profil.dureeAnnees();

        double vanCap = CalculateurVAN.vanCapital(capitalFinalNet, duree);
        double vanEco = CalculateurVAN.vanEconomiesFiscales(annees);
        double vanTot = vanCap + vanEco;

        return new ResultatSimulation(
                versementAnnuel,
                capitalFinalNet,
                capitalFinalNet,
                vanCap,
                vanEco,
                vanTot,
                taxeCompteTitresTotale,
                annees);
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

    private record AccumulationResult(List<ResultatAnnuel> annees, double taxeCompteTitresTotale) {
    }
}