package tfe.common;

import be.lsm.tfe.common.Constants;
import be.lsm.tfe.common.ProfilInvestisseur;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ProfilInvestisseur")
class ProfilInvestisseurTest {

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Validation de construction")
    class Construction {

        @Test
        @DisplayName("Profil valide se crée sans exception")
        void profilValide_seCree() {
            assertThatNoException().isThrownBy(() ->
                    new ProfilInvestisseur("Marie", "Dupont", 1995, 18, 64));
        }

        @Test
        @DisplayName("ageDebut >= ageFin lève IllegalArgumentException")
        void ageDebutSuperieurAgeFin_leveException() {
            assertThatThrownBy(() -> new ProfilInvestisseur("X", "Y", 1990, 65, 64))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("ageDebut = ageFin lève IllegalArgumentException")
        void ageDebutEgalAgeFin_leveException() {
            assertThatThrownBy(() -> new ProfilInvestisseur("X", "Y", 1990, 64, 64))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Factory defaut() crée profil 18→64")
        void factoryDefaut_creeProfilStandard() {
            ProfilInvestisseur p = new ProfilInvestisseur("A", "B", 1990,
                    Constants.AGE_DEBUT_DEFAUT,
                    Constants.AGE_FIN_HORIZON);
            assertThat(p.ageDebut()).isEqualTo(18);
            assertThat(p.ageFin()).isEqualTo(64);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Calculs temporels")
    class CalculsTemporels {

        private final ProfilInvestisseur profil =
                new ProfilInvestisseur("Marie", "Dupont", 1995, 18, 64);

        @Test
        @DisplayName("anneeDeAge(18) = 1995 + 18 = 2013")
        void anneeDeAge18() {
            assertThat(profil.anneeDeAge(18)).isEqualTo(2013);
        }

        @Test
        @DisplayName("ageEnAnnee(2025) = 2025 - 1995 = 30")
        void ageEnAnnee2025() {
            assertThat(profil.ageEnAnnee(2025)).isEqualTo(30);
        }

        @Test
        @DisplayName("anneeDebutVersements = anneeDeAge(ageDebut)")
        void anneeDebutVersements() {
            assertThat(profil.anneeDebutVersements()).isEqualTo(profil.anneeDeAge(18));
        }

        @Test
        @DisplayName("anneeFinVersements = anneeDeAge(ageFin)")
        void anneeFinVersements() {
            assertThat(profil.anneeFinVersements()).isEqualTo(profil.anneeDeAge(64));
        }

        @Test
        @DisplayName("dureeAnnees = ageFin - ageDebut = 46")
        void dureeAnnees() {
            assertThat(profil.dureeAnnees()).isEqualTo(46);
        }

        @ParameterizedTest(name = "ageDebut={0} → souscritApres55Ans={1}")
        @CsvSource({
            "18, false",
            "55, false",
            "56, true",
            "60, true"
        })
        @DisplayName("souscritApres55Ans()")
        void souscritApres55Ans(int ageDebut, boolean attendu) {
            ProfilInvestisseur p = new ProfilInvestisseur("X", "Y", 1970, ageDebut, 70);
            assertThat(p.souscritApres55Ans()).isEqualTo(attendu);
        }
    }
}
