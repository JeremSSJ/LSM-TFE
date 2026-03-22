package be.lsm.tfe.branche23;

import lombok.Builder;

@Builder
public record ParametresBranche23(double taxeOperationsAssurance,
                                  double fraisParPrime,
                                  double fraisGestionAnnuels,
                                  double tauxTaxeAnticipative,
                                  int ageTaxeAnticipative,
                                  int dureeMinAvantAnticipativeSiSouscritTard,
                                  RegleReductionFiscale regleReductionFiscale,
                                  int ageLimiteReductionFiscale) {
}