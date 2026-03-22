package be.lsm.tfe.common;

public record PointCroisement(
        double versementEuros,
        double vanVehiculeA,
        double vanVehiculeB,
        String vehiculeAvantageuxApres
) {
    @Override
    public String toString() {
        return "Croisement à %.0f €/an — VAN(A)=%.0f €, VAN(B)=%.0f € → %s avantageux au-delà"
                .formatted(versementEuros, vanVehiculeA, vanVehiculeB, vehiculeAvantageuxApres);
    }
}
