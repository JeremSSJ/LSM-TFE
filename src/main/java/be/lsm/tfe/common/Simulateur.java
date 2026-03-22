package be.lsm.tfe.common;

public interface Simulateur {

    ResultatSimulation simuler(ProfilInvestisseur profil,
                               double versementAnnuel,
                               ParametresRendement rendement);

    String nomVehicule();
}
