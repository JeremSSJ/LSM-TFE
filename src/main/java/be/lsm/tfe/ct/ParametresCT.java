package be.lsm.tfe.ct;

import lombok.Builder;

@Builder
public record ParametresCT(double taxeOperationsBourse,
                           double fraisParVersement,
                           double fraisGestionAnnuels,
                           double tauxTaxePlusValues,
                           ExonerationPlusValues exoneration) {
}
