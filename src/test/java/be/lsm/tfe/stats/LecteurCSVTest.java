package be.lsm.tfe.stats;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LecteurCSV")
class LecteurCSVTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Relit sans décalage le nouveau format avec tauxEgaux")
    void litFormatActuelAvecTauxEgaux() throws IOException {
        LigneRapportCSV ligne = new LigneRapportCSV(
                "EP Branche 23",
                "Compte-Titres",
                7.00,
                30,
                64,
                10.00,
                0,
                990,
                45.50,
                49.50,
                5.00,
                2,
                450.00,
                "Compte-Titres",
                12_345.67,
                890.12,
                11_999.99
        );

        Path csv = tempDir.resolve("stats-format-actuel.csv");
        ExportateurRapport.ecrireCSV(List.of(ligne), csv.toString());

        LigneRapportCSV relue = LecteurCSV.lire(csv.toString()).get(0);

        assertThat(relue).isEqualTo(ligne);
        assertThat(relue.tauxEgaux()).isEqualTo(5.00);
        assertThat(relue.nbCroisements()).isEqualTo(2);
        assertThat(relue.premierCroisement()).isEqualTo(450.00);
        assertThat(relue.dominant()).isEqualTo("Compte-Titres");
    }
}


