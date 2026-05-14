package be.lsm.tfe.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@DisplayName("CalculateurCapitalisation")
class CalculateurCapitalisationTest {

    // ── capitaliserEconomiesFiscales ──────────────────────────────────────────

    @Test
    @DisplayName("Économies de 0 → résultat nul")
    void economiesNulles_resultatNul() {
        List<ResultatAnnuel> annees = IntStream.range(0, 10)
                .mapToObj(i -> ResultatAnnuel.builder().economiesFiscales(0.0).build())
                .toList();

        double resultat = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees, 10);

        assertThat(resultat).isCloseTo(0.0, within(1e-9));
    }

    @Test
    @DisplayName("Économies positives → résultat positif, supérieur à la somme brute")
    void economiesPositives_resultatPositifEtCapitalise() {
        // 10 années × 337.5€ = 3 375€ bruts
        // avec capitalisation on doit obtenir plus que 3 375€
        List<ResultatAnnuel> annees = IntStream.range(0, 10)
                .mapToObj(i -> ResultatAnnuel.builder().economiesFiscales(337.5).build())
                .toList();

        double resultat = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees, 10);

        assertThat(resultat).isGreaterThan(337.5 * 10); // > somme brute grâce à la capitalisation
    }

    @Test
    @DisplayName("Durée plus longue → capitalisation plus grande (toutes choses égales)")
    void dureePlusLongue_capitalisationPlusGrande() {
        List<ResultatAnnuel> annees20 = IntStream.range(0, 20)
                .mapToObj(i -> ResultatAnnuel.builder().economiesFiscales(150.0).build())
                .toList();
        List<ResultatAnnuel> annees10 = annees20.subList(0, 10);

        double cap20 = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees20, 20);
        double cap10 = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees10, 10);

        // 20 ans = plus de versements + taux OLO plus élevé → capitalisation plus grande
        assertThat(cap20).isGreaterThan(cap10);
    }

    @Test
    @DisplayName("Économie à la dernière année → facteur de capitalisation = 1 (aucun intérêt)")
    void economieDerniereAnnee_facteur1() {
        // Un seul élément à l'index 0 → reçu en fin d'année 1 → capitalisé sur (1-1)=0 ans → facteur 1
        List<ResultatAnnuel> annees = List.of(
                ResultatAnnuel.builder().economiesFiscales(500.0).build()
        );

        double resultat = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees, 1);

        assertThat(resultat).isCloseTo(500.0, within(1e-9));
    }

    @Test
    @DisplayName("Économie reçue après échéance → flux actualisé vers l'échéance avec taux OLO propre à la durée de l'écart")
    void economieApresEcheance_estActualisee() {
        // dureeTotal = 1 :
        //   t=0 → reçue à l'année 1 → anneesRestantes = 0 → facteur 1 (taux OLO(0)=0)
        //   t=1 → reçue à l'année 2 → anneesRestantes = -1 → actualisation 1 an au taux OLO(1)
        List<ResultatAnnuel> annees = List.of(
                ResultatAnnuel.builder().economiesFiscales(500.0).build(),
                ResultatAnnuel.builder().economiesFiscales(500.0).build()
        );

        double tauxOlo1an = oloReferential.tauxPourDuree(1);
        double attendu = 500.0 + (500.0 / (1.0 + tauxOlo1an));

        double resultat = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees, 1);

        assertThat(resultat).isCloseTo(attendu, within(1e-9));
    }

    @Test
    @DisplayName("Chaque flux utilise le taux OLO de sa propre durée résiduelle")
    void chaqueCashflowUtiliseSonPropreTauxOlo() {
        // dureeTotal = 3 : 4 éléments (t=0,1,2,3)
        //   t=0 → anneesRestantes=2 → taux OLO(2)
        //   t=1 → anneesRestantes=1 → taux OLO(1)
        //   t=2 → anneesRestantes=0 → facteur 1
        //   t=3 → anneesRestantes=-1 → taux OLO(1) en actualisation
        List<ResultatAnnuel> annees = List.of(
                ResultatAnnuel.builder().economiesFiscales(100.0).build(),
                ResultatAnnuel.builder().economiesFiscales(100.0).build(),
                ResultatAnnuel.builder().economiesFiscales(100.0).build(),
                ResultatAnnuel.builder().economiesFiscales(100.0).build()
        );

        double t2 = oloReferential.tauxPourDuree(2);
        double t1 = oloReferential.tauxPourDuree(1);
        double attendu = 100.0 * Math.pow(1 + t2, 2)   // t=0
                       + 100.0 * Math.pow(1 + t1, 1)    // t=1
                       + 100.0                           // t=2
                       + 100.0 * Math.pow(1 + t1, -1);  // t=3

        double resultat = CalculateurCapitalisation.capitaliserEconomiesFiscales(annees, 3);

        assertThat(resultat).isCloseTo(attendu, within(1e-9));
    }

    @Test
    @DisplayName("Coût de 0 → résultat nul")
    void coutNul_resultatNul() {
        double resultat = CalculateurCapitalisation.capitaliserCoutsAnnuels(0.0, 10);

        assertThat(resultat).isCloseTo(0.0, within(1e-9));
    }

    @Test
    @DisplayName("Coût positif → capitalisation > coût × (duree+1)")
    void coutPositif_resultatCapitaliseSupérieur() {
        // Durée = 5 ans, coût 100€/an → 6 périodes (t=0 à 5) × 100 = 600€ bruts
        // avec capitalisation → plus de 600€
        double resultat = CalculateurCapitalisation.capitaliserCoutsAnnuels(100.0, 5);

        assertThat(resultat).isGreaterThan(100.0 * 6);
    }

    @Test
    @DisplayName("Dernier coût (t=duree) → facteur de capitalisation = 1")
    void coutDernierePeriode_facteur1() {
        // La seule contribution avec duree=0 est t=0 → facteur (1+r)^(0-0) = 1
        double resultat = CalculateurCapitalisation.capitaliserCoutsAnnuels(250.0, 0);

        assertThat(resultat).isCloseTo(250.0, within(1e-9));
    }

    @Test
    @DisplayName("Linearité : coût × 2 → capitalisation × 2")
    void linearite() {
        double cap1 = CalculateurCapitalisation.capitaliserCoutsAnnuels(100.0, 10);
        double cap2 = CalculateurCapitalisation.capitaliserCoutsAnnuels(200.0, 10);

        assertThat(cap2).isCloseTo(2 * cap1, within(1e-6));
    }

    @Test
    @DisplayName("Taux OLO est plafonné à 30 ans (duree 31 = taux 30 ans)")
    void tauxPlafonne30Ans() {
        // Les deux capitalisations doivent utiliser le même taux OLO (taux à 30 ans)
        double cap30 = CalculateurCapitalisation.capitaliserCoutsAnnuels(100.0, 30);
        double cap31 = CalculateurCapitalisation.capitaliserCoutsAnnuels(100.0, 31);

        // cap31 > cap30 car il y a une période de plus, mais le taux est le même
        // vérification : ratio cohérent avec 1 période supplémentaire
        double taux30 = oloReferential.tauxPourDuree(30);
        double taux31 = oloReferential.tauxPourDuree(31);
        assertThat(taux30).isCloseTo(taux31, within(1e-12)); // même taux
        assertThat(cap31).isGreaterThan(cap30); // une période supplémentaire
    }
}

