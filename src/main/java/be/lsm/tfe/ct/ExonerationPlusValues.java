package be.lsm.tfe.ct;

public record ExonerationPlusValues(
        double montantBase,
        double montantAnnuel,
        int    anneesMaxCumul
) {

    public double calculerExonerationTotale(int dureeContratAnnees) {
        int anneesCumul = Math.min(dureeContratAnnees, anneesMaxCumul);
        return montantBase + anneesCumul * montantAnnuel;
    }

    public double calculerTaxe(double plusValue, double tauxTaxe, int dureeContratAnnees) {
        double exoneration     = calculerExonerationTotale(dureeContratAnnees);
        double plusValueTaxable = Math.max(0.0, plusValue - exoneration);
        return plusValueTaxable * tauxTaxe;
    }
}
