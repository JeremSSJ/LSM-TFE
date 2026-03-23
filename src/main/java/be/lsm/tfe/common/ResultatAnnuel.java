package be.lsm.tfe.common;

import lombok.Builder;

@Builder
public record ResultatAnnuel(
        int    anneeCalendaire,
        int    age,
        double reserveEnFinAnnee,
        double versementNet,
        double economiesFiscales,
        boolean taxeAnticipativeAppliquee
) {
    public static ResultatAnnuel sansAnticipative(
            int annee, int age, double reserve, double versementNet, double economies) {
        return new ResultatAnnuel(annee, age, reserve, versementNet, economies, false);
    }

    public static ResultatAnnuel avecAnticipative(
            int annee, int age, double reserve, double versementNet, double economies) {
        return new ResultatAnnuel(annee, age, reserve, versementNet, economies, true);
    }
}
