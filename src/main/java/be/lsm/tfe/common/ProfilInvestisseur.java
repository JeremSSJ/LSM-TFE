package be.lsm.tfe.common;

public record ProfilInvestisseur(
        String prenom,
        String nom,
        int    anneeNaissance,
        int    ageDebut,
        int    ageFin
) {
    public ProfilInvestisseur {
        if (ageDebut < 18 || ageDebut >= ageFin) {
            throw new IllegalArgumentException(
                "ageDebut (%d) doit être 18 ou plus et inférieur à ageFin (%d)"
                    .formatted(ageDebut, ageFin));
        }
    }

    public int anneeDeAge(int age)        { return anneeNaissance + age; }

    public int ageEnAnnee(int anneeCalendaire) { return anneeCalendaire - anneeNaissance; }

    public int anneeDebutVersements()     { return anneeDeAge(ageDebut); }

    public int anneeFinVersements()       { return anneeDeAge(ageFin); }

    public int dureeAnnees()              { return ageFin - ageDebut; }

    public boolean souscritApres55Ans()   { return ageDebut > 55; }

    @Override
    public String toString() {
        return "%s %s — né(e) en %d — versements de %d à %d ans"
                .formatted(prenom, nom, anneeNaissance, ageDebut, ageFin);
    }
}
