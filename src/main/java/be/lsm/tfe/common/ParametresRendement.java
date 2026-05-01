package be.lsm.tfe.common;

public record ParametresRendement(double rendementAnnuel) {

    public ParametresRendement {
        if (rendementAnnuel < 0) throw new IllegalArgumentException("rendementAnnuel ne peut être négatif");
    }
}
