package be.lsm.tfe.branche23;

import be.lsm.tfe.common.Constants;

@FunctionalInterface
public interface RegleReductionFiscale {

    double calculer(double prime);

    static RegleReductionFiscale pourEpargnePension() {
        return prime -> {
            if (prime <= 0) return 0.0;
            double base = Math.min(prime, Constants.EP_SEUIL_REDUCTION_HAUT);
            double taux = base <= Constants.EP_SEUIL_REDUCTION_BAS
                    ? Constants.EP_TAUX_REDUCTION_BAS
                    : Constants.EP_TAUX_REDUCTION_HAUT;
            return base * taux;
        };
    }

    static RegleReductionFiscale pourEpargneLongTerme() {
        return prime -> {
            if (prime <= 0) return 0.0;
            double base = Math.min(prime, Constants.ELT_SEUIL_REDUCTION);
            return base * Constants.ELT_TAUX_REDUCTION;
        };
    }
}