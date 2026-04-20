package be.lsm.tfe.common;

public record PointCroisement(
        double versementEuros
) {
    @Override
    public String toString() {
        return "Croisement à %.0f €/an"
                .formatted(versementEuros);
    }
}
