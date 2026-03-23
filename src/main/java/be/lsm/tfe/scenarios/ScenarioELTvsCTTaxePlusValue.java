package be.lsm.tfe.scenarios;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.common.*;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;

import java.util.List;

public final class ScenarioELTvsCTTaxePlusValue {

    public static void main(String[] args) throws Exception {

        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Marie", "Dupont",
                2008,
                53,
                64
        );

        ParametresRendement rendement = new ParametresRendement(0.15, 0.03 );

        int versementMin = 0;
        int versementMax = 1600;

        ParametresBranche23 paramsELT = ParametresBranche23.builder()
                .taxeOperationsAssurance(Constants.ELT_TAXE_ASSURANCE)  // 2%
                .fraisParPrime(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.10)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())  // 30% ≤ 2 450€
                .ageLimiteReductionFiscale(64)
                .build();

        ParametresCT paramsCT = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.0)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.10)
                .exoneration(new ExonerationPlusValues(
                        10_000.0,
                        1_000.0,
                        5
                ))
                .build();








        // ════════════════════════════════════════════════════════════════════
        //  SIMULATION
        // ════════════════════════════════════════════════════════════════════
        Simulateur simELT = new SimulateurBranche23(paramsELT, "ELT Branche 23");
        Simulateur simCT  = new SimulateurCT(paramsCT);

        System.out.println("Simulation ELT vs CT en cours…");
        System.out.println("Profil : " + profil);
        System.out.printf("Rendement : %.1f%%/an  |  OLO : %.1f%%/an%n",
                rendement.rendementAnnuel() * 100, rendement.tauxOLO() * 100);
        System.out.printf("Plage : %d€ → %d€ (%d simulations)%n%n",
                versementMin, versementMax, versementMax - versementMin + 1);

        List<ResultatSimulation> resultatsELT = ComparateurVehicules.simulerPlage(
                simELT, profil, rendement, versementMin, versementMax);

        List<ResultatSimulation> resultatsCT = ComparateurVehicules.simulerPlage(
                simCT, profil, rendement, versementMin, versementMax);

        // ════════════════════════════════════════════════════════════════════
        //  CROISEMENTS
        // ════════════════════════════════════════════════════════════════════
        List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(
                resultatsELT, resultatsCT, simELT.nomVehicule(), simCT.nomVehicule());

        if (croisements.isEmpty()) {
            ResultatSimulation dernierELT = resultatsELT.get(resultatsELT.size() - 1);
            ResultatSimulation dernierCT  = resultatsCT.get(resultatsCT.size() - 1);
            String avantageuxPartout = dernierELT.vanTotale() > dernierCT.vanTotale()
                    ? simELT.nomVehicule() : simCT.nomVehicule();
            System.out.println("Aucun croisement — " + avantageuxPartout + " est avantageux sur toute la plage.");
        } else {
            System.out.println("Points de croisement détectés :");
            croisements.forEach(c -> System.out.println("  " + c));
        }

        // ════════════════════════════════════════════════════════════════════
        //  ★ GÉNÉRATION DU GRAPHIQUE
        // ════════════════════════════════════════════════════════════════════
        String cheminSortie = "C:/Users/jerem/Downloads/tteeeeee/scenario_elt_vs_ct_taxe_plus_value.png";  // ★ à adapter

        RendeurGraphique.generer(
                resultatsELT, resultatsCT, croisements,
                simELT.nomVehicule(), simCT.nomVehicule(),
                "Épargne Long Terme Branche 23 vs Compte-Titres",
                profil, rendement, cheminSortie);
    }
}
