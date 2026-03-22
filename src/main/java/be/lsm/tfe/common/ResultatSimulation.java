package be.lsm.tfe.common;

import java.util.List;

public record ResultatSimulation(
        double              versementAnnuel,
        double              capitalFinal,
        double              capitalFinalNet,
        double              vanCapital,
        double              vanEconomiesFiscales,
        double              vanTotale,
        List<ResultatAnnuel> resultatParAnnee
) {
    public ResultatSimulation {
        resultatParAnnee = List.copyOf(resultatParAnnee);
    }
}
