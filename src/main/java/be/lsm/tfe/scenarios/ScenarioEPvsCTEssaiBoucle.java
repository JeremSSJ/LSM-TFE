package be.lsm.tfe.scenarios;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;

import java.io.IOException;
import java.util.List;

@Deprecated
public final class ScenarioEPvsCTEssaiBoucle {

    public static void main(String[] args) throws Exception {

        for (double i = 0.01; i < 0.16; i+=0.01) {
            extracted(i);
        }
    }

    private static void extracted(double rendementAnnuel) throws IOException {
        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Marie", "Dupont",
                2008,
                18,
                64
        );

        ParametresRendement rendement = new ParametresRendement(rendementAnnuel,0.03  );

        int versementMin = 0;
        int versementMax = 1600;

        ParametresBranche23 paramsEP = ParametresBranche23.builder()
                .taxeOperationsAssurance(0.0)
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.08)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargnePension())
                .ageLimiteReductionFiscale(64)
                .build();

        ParametresCT paramsCT = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.0)
                .exoneration(new ExonerationPlusValues(
                        10_000.0,
                        1_000.0,
                        5
                ))
                .build();


        // ════════════════════════════════════════════════════════════════════
        //  SIMULATION
        // ════════════════════════════════════════════════════════════════════
        Simulateur simEP = new SimulateurBranche23(paramsEP, "EP Branche 23");
        Simulateur simCT = new SimulateurCT(paramsCT);

        System.out.println("Simulation EP vs CT en cours…");
        System.out.println("Profil : " + profil);
        System.out.printf("Rendement : %.1f%%/an  |  OLO : %.1f%%/an%n",
                rendement.rendementAnnuel() * 100, rendement.tauxOLO() * 100);
        System.out.printf("Plage : %d€ → %d€ (%d simulations)%n%n",
                versementMin, versementMax, versementMax - versementMin + 1);

        List<ResultatSimulation> resultatsEP = ComparateurVehicules.simulerPlage(
                simEP, profil, rendement, versementMin, versementMax);

        List<ResultatSimulation> resultatsCT = ComparateurVehicules.simulerPlage(
                simCT, profil, rendement, versementMin, versementMax);

        // ════════════════════════════════════════════════════════════════════
        //  CROISEMENTS
        // ════════════════════════════════════════════════════════════════════
        List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                resultatsEP, resultatsCT, simEP.nomVehicule(), simCT.nomVehicule());

        if (croisements.isEmpty()) {
            ResultatSimulation dernierEP = resultatsEP.get(resultatsEP.size() - 1);
            ResultatSimulation dernierCT = resultatsCT.get(resultatsCT.size() - 1);
            String avantageuxPartout = dernierEP.vanTotale() > dernierCT.vanTotale()
                    ? simEP.nomVehicule() : simCT.nomVehicule();
            System.out.println("Aucun croisement — " + avantageuxPartout + " est avantageux sur toute la plage.");
        } else {
            System.out.println("Points de croisement détectés :");
            croisements.forEach(c -> System.out.println("  " + c));
        }

        // ════════════════════════════════════════════════════════════════════
        //  ★ GÉNÉRATION DU GRAPHIQUE
        // ════════════════════════════════════════════════════════════════════
        String cheminSortie = "C:/Users/jerem/Downloads/dfsdfsdff/scenario_ep_vs_ct" + rendementAnnuel + ".png";  // ★ à adapter

        RendeurGraphique.generer(
                resultatsEP, resultatsCT, croisements,
                simEP.nomVehicule(), simCT.nomVehicule(),
                "Épargne Pension Branche 23 vs Compte-Titres",
                profil, rendement, cheminSortie);
    }
}
