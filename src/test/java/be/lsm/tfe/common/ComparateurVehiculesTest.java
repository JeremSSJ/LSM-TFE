package be.lsm.tfe.common;

import be.lsm.tfe.branche23.ParametresBranche23;
import be.lsm.tfe.branche23.RegleReductionFiscale;
import be.lsm.tfe.branche23.SimulateurBranche23;
import be.lsm.tfe.ct.ExonerationPlusValues;
import be.lsm.tfe.ct.ParametresCT;
import be.lsm.tfe.ct.SimulateurCT;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ComparateurVehiculesTest {


    @Test
    void trouverCroisement() {
        ProfilInvestisseur profil = new ProfilInvestisseur(
                "Investisseur", "Type", 2000, 23, 65);

        ParametresRendement rendement = new ParametresRendement(0.07, 0.03);

        ParametresBranche23 paramsELT = ParametresBranche23.builder()
                .taxeOperationsAssurance(Constants.ELT_TAXE_ASSURANCE)
                .fraisParPrime(0.02)
                .fraisGestionAnnuels(0.0)
                .tauxTaxeAnticipative(0.10)
                .ageTaxeAnticipative(60)
                .dureeMinAvantAnticipativeSiSouscritTard(10)
                .regleReductionFiscale(RegleReductionFiscale.pourEpargneLongTerme())
                .ageLimiteReductionFiscale(65)
                .build();

        Simulateur simELT = new SimulateurBranche23(paramsELT, "ELT Branche 23");

        List<ResultatSimulation> resELT = ComparateurVehicules.simulerPlage(
                simELT, profil, rendement, 1, 2450);

        ParametresCT paramsCTavec = ParametresCT.builder()
                .taxeOperationsBourse(0.0012)
                .fraisParVersement(0.02)
                .fraisGestionAnnuels(0.0)
                .tauxTaxePlusValues(0.10)
                .exoneration(new ExonerationPlusValues(10_000.0, 1_000.0, 5))
                .build();

        Simulateur simCTav = new SimulateurCT(paramsCTavec);

        List<ResultatSimulation> resCTavELT = ComparateurVehicules.simulerPlage(
                simCTav, profil, rendement, 1, 2450);

        List<PointCroisement> croisements = ComparateurVehicules.trouverCroisements(resELT, resCTavELT);

        assertEquals(1, croisements.size());
        assertEquals(105.14, croisements.get(0).versementEuros(), 1e-2);
    }
}