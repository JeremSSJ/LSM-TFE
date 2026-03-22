package be.lsm.tfe.common;

public record ParametresRendement(double rendementAnnuel, double tauxOLO) {

    public ParametresRendement {
        if (rendementAnnuel < 0) throw new IllegalArgumentException("rendementAnnuel ne peut être négatif");
        if (tauxOLO < 0)         throw new IllegalArgumentException("tauxOLO ne peut être négatif");
    }
}
