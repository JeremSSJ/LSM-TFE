package be.lsm.tfe.common;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfilInvestisseurTest {


    @Test
    void anneeDeAge() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 18, 64);

        assertEquals(2050, profil.anneeDeAge(50));
    }

    @Test
    void ageEnAnnee() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 18, 64);

        assertEquals(50, profil.ageEnAnnee(2050));
    }

    @Test
    void anneeDebutVersements() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 18, 64);

        assertEquals(2018, profil.anneeDebutVersements());
    }

    @Test
    void anneeFinVersements() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 18, 64);

        assertEquals(2064, profil.anneeFinVersements());
    }

    @Test
    void dureeAnnees() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 18, 64);

        assertEquals(46, profil.dureeAnnees());
    }

    @Test
    void souscritApres55Ans() {
        ProfilInvestisseur profil = new ProfilInvestisseur("", "", 2000, 54, 64);

        assertFalse(profil.souscritApres55Ans());

        profil = new ProfilInvestisseur("", "", 2000, 55, 64);

        assertTrue(profil.souscritApres55Ans());

        profil = new ProfilInvestisseur("", "", 2000, 56, 64);

        assertTrue(profil.souscritApres55Ans());
    }
}
