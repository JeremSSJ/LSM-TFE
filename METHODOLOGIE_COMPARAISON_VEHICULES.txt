╔══════════════════════════════════════════════════════════════════════════════════════╗
║         MÉTHODOLOGIE DE COMPARAISON DES VÉHICULES D'INVESTISSEMENT                  ║
║         Travail de Fin d'Études — Analyse quantitative                               ║
╚══════════════════════════════════════════════════════════════════════════════════════╝

═══════════════════════════════════════════════════════════════════════════════════════
TABLE DES MATIÈRES
═══════════════════════════════════════════════════════════════════════════════════════

  1. Présentation générale de l'approche
  2. Les véhicules d'investissement analysés
  3. Le profil investisseur
  4. Le critère de comparaison : la Valeur Terminale nette (VT)
  5. Description des quatre scénarios
  6. Paramètres communs à tous les scénarios
  7. Modélisation du Compte-Titres (CT)
  8. Modélisation de la Branche 23 — Épargne Pension (EP)
  9. Modélisation de la Branche 23 — Épargne Long Terme (ELT)
 10. La plage de versements et la notion de dominance
 11. Détection des points de croisement
 12. Statistiques et indicateurs produits
 13. Représentation graphique : heatmaps
 14. Limites et hypothèses de la méthodologie


═══════════════════════════════════════════════════════════════════════════════════════
1. PRÉSENTATION GÉNÉRALE DE L'APPROCHE
═══════════════════════════════════════════════════════════════════════════════════════

L'objectif central de ce travail est de déterminer, dans quelle mesure et dans quelles
conditions, les produits d'assurance-vie de la Branche 23 bénéficiant d'un avantage
fiscal (Épargne Pension et Épargne Long Terme) sont financièrement plus avantageux
qu'un investissement équivalent via un Compte-Titres en Belgique.

La méthodologie repose sur une simulation déterministe et systématique. Pour chaque
combinaison possible (âge de début × taux de rendement), on simule deux instruments
sur la même durée, avec le même versement annuel, et on compare leur résultat net
exprimé en Valeur Terminale nette (VT). Cette comparaison est répétée pour l'ensemble
d'une plage de versements annuels, ce qui permet de quantifier non seulement qui
« gagne », mais à partir de quel niveau de versement et dans quelle proportion.

L'espace d'exploration est le suivant :
  - 38 profils d'âge (de 18→65 ans jusqu'à 55→65 ans, par pas de 1 an)
  - 8 niveaux de rendement annuel brut (de 3 % à 10 %, par pas de 1 %)
  - Plages de versements :
      • EP  : de 1 € à 1 350 €/an (plafond légal de la déduction fiscale EP)
      • ELT : de 1 € à 2 450 €/an (plafond légal de la déduction fiscale ELT)
  - Quatre configurations fiscales (4 scénarios)

Cela représente 38 × 8 = 304 lignes par fichier CSV, soit 1 216 simulations de plages
au total. Chaque simulation de plage compare les deux instruments pour chaque versement
entier de la plage, soit entre 1 350 et 2 450 simulations par ligne.


═══════════════════════════════════════════════════════════════════════════════════════
2. LES VÉHICULES D'INVESTISSEMENT ANALYSÉS
═══════════════════════════════════════════════════════════════════════════════════════

Trois véhicules sont modélisés ; ils sont ensuite combinés en quatre scénarios :

  A. ÉPARGNE PENSION — BRANCHE 23 (EP B23)
     Produit d'assurance-vie en unités de compte bénéficiant de la réduction fiscale
     pour épargne-pension (art. 145/1 CIR92).

  B. ÉPARGNE LONG TERME — BRANCHE 23 (ELT B23)
     Produit d'assurance-vie en unités de compte bénéficiant de la réduction fiscale
     pour épargne long terme (art. 145/3 CIR92).

  C. COMPTE-TITRES (CT)
     Portefeuille d'actions/ETF géré via un courtier, sans avantage fiscal à l'entrée
     mais sans taxe anticipative. Deux variantes sont modélisées :
       • CT sans taxe sur les plus-values (taux PV = 0 %)
       • CT avec taxe sur les plus-values de 10 %


═══════════════════════════════════════════════════════════════════════════════════════
3. LE PROFIL INVESTISSEUR
═══════════════════════════════════════════════════════════════════════════════════════

Un profil investisseur est défini par :
  - Son année de naissance (fixée à 2008 dans les scénarios statistiques complets)
  - Son âge de début des versements (variable, de 18 à 55 ans)
  - Son âge de fin des versements / horizon de retraite (fixé à 65 ans)

La durée de l'investissement est donc : durée = ageFin − ageDebut années.

Les versements sont supposés constants d'année en année (annuités constantes).
Dans l'implémentation actuelle, le versement de l'année est ajouté après les
prélèvements annuels éventuels (taxe anticipative et/ou TCT).

Règle particulière pour la Branche 23 :
  Si l'investisseur souscrit après l'âge de 55 ans, la règle des « 10 ans minimum avant
  la taxe anticipative » s'applique, ce qui décale l'année de prélèvement de la taxe.


═══════════════════════════════════════════════════════════════════════════════════════
4. LE CRITÈRE DE COMPARAISON : LA VALEUR TERMINALE NETTE (VT)
═══════════════════════════════════════════════════════════════════════════════════════

La comparaison entre les deux instruments repose exclusivement sur leur valeur
terminale nette (VT), calculée à l'échéance.

Le projet n'utilise plus de logique de VAN : la classe `CalculateurVAN` est
dépréciée et conservée uniquement pour compatibilité transitoire.

La capitalisation des flux annexes repose sur un référentiel OLO dépendant de la durée
(1 à 30 ans),
avec une correspondance directe année → taux. Pour toute durée supérieure à 30 ans,
le taux de la 30e année est réutilisé.

Notations :
  - r(n) = taux OLO correspondant à la durée n (via le référentiel)
  - r(n) = r(30) si n > 30
  - VC(m, n) = m × (1 + r(n))^n

4.0 Référentiel OLO utilisé (code)
──────────────────────────────────
Les taux utilisés pour la capitalisation sont ceux de `oloReferential`, définis pour les
durées 1 à 30 ans.

Le code applique un facteur 0,7 à chaque taux OLO brut :

  taux_net = taux_brut × 0,7

Interprétation économique : le facteur 0,7 traduit un rendement net attendu pour un
investisseur belge après précompte mobilier de 30 % sur le rendement des OLO.

Formellement :

  taux_net = taux_brut × (1 − 30 %) = taux_brut × 0,7

Table des taux du référentiel (brut → net utilisé pour la capitalisation) :

  Année   OLO brut (%)   OLO net utilisé (%)
  ------------------------------------------
    1       2,62           1,834
    2       2,72           1,904
    3       2,83           1,981
    4       2,95           2,065
    5       3,06           2,142
    6       3,17           2,219
    7       3,27           2,289
    8       3,39           2,373
    9       3,51           2,457
   10       3,63           2,541
   11       3,73           2,611
   12       3,81           2,667
   13       3,87           2,709
   14       3,93           2,751
   15       3,98           2,786
   16       4,03           2,821
   17       4,08           2,856
   18       4,13           2,891
   19       4,17           2,919
   20       4,21           2,947
   21       4,25           2,975
   22       4,29           3,003
   23       4,32           3,024
   24       4,35           3,045
   25       4,37           3,059
   26       4,39           3,073
   27       4,41           3,087
   28       4,42           3,094
   29       4,43           3,101
   30       4,43           3,101

Pour une durée > 30 ans, le modèle applique le taux net de l'année 30.

4.1 VT d'un instrument de la Branche 23
─────────────────────────────────────────
La valeur terminale B23 est décomposée en deux composantes :

  VT_B23 = CapitalFinalNet_B23 + EcoFiscalesCapitalisees_B23

  • CapitalFinalNet_B23 :
    Capital net en fin de simulation (après taxe anticipative éventuelle et TCT
    annuelle éventuelle).

  • EcoFiscalesCapitalisees_B23 :
    Somme des réductions d'impôt annuelles capitalisées jusqu'à l'échéance.
    Pour l'économie fiscale de l'année t (perçue en t+1), le nombre d'années
    restantes est : (dureeTotale - (t+1)).

        EcoCap = Σ [Economie(t) × (1 + r(anneesRestantes))^(anneesRestantes)]
                 pour t = 0 à N-1

4.2 VT d'un Compte-Titres
──────────────────────────
La valeur terminale du CT intègre le capital net et les coûts capitalisés :

  VT_CT = CapitalNet_CT − FeesCapitalises_CT

  • CapitalNet_CT :
    Capital final net après taxe éventuelle sur les plus-values.

  • FeesCapitalises_CT :
    Coûts annuels (TOB + frais par versement) capitalisés à l'échéance :

        FeesCap = Σ [CoutAnnuel × (1 + r(duree - t))^(duree - t)]
                  pour t = 0 à duree

4.3 Comparaison
───────────────
Pour chaque versement annuel v de la plage testée, on calcule la différence :

  Δ(v) = VT_B23(v) − VT_CT(v)

  • Si Δ(v) > 0 → la Branche 23 est plus avantageuse pour ce versement
  • Si Δ(v) < 0 → le Compte-Titres est plus avantageux
  • Si Δ(v) = 0 → ex aequo strict sur ce versement

Remarque implémentation : les tests de croisement filtrent les quasi-zéros numériques
avec un seuil de 1e-6 sur la valeur terminale.


═══════════════════════════════════════════════════════════════════════════════════════
5. DESCRIPTION DES QUATRE SCÉNARIOS
═══════════════════════════════════════════════════════════════════════════════════════

Les quatre scénarios résultent du croisement de deux dimensions :
  • Le type de déduction fiscale appliquée à la Branche 23 : EP ou ELT
  • Le régime fiscal appliqué au Compte-Titres : sans ou avec taxe sur les plus-values

En complément, la taxe sur les comptes-titres (TCT) est appliquée de manière
transversale dans la simulation dès que la réserve annuelle dépasse le seuil légal.

┌──────┬─────────────────────────────────┬───────────────────────────────────┐
│  N°  │  Véhicule A (Branche 23)        │  Véhicule B (Compte-Titres)       │
├──────┼─────────────────────────────────┼───────────────────────────────────┤
│  1   │  Épargne Pension (EP) B23       │  CT sans taxe sur les PV (0 %)    │
│  2   │  Épargne Pension (EP) B23       │  CT avec taxe sur les PV (10 %)   │
│  3   │  Épargne Long Terme (ELT) B23   │  CT sans taxe sur les PV (0 %)    │
│  4   │  Épargne Long Terme (ELT) B23   │  CT avec taxe sur les PV (10 %)   │
└──────┴─────────────────────────────────┴───────────────────────────────────┘

SCÉNARIO 1 — EP B23 vs CT sans taxe PV
  Ce scénario représente le cas le plus défavorable pour la Branche 23, car le CT
  n'est soumis à aucune taxe sur les plus-values. La Branche 23 EP doit compenser
  intégralement sa taxe anticipative de 8 % par ses économies fiscales à l'entrée.
  C'est le « test de robustesse » de la déduction EP.

SCÉNARIO 2 — EP B23 vs CT avec taxe PV 10 %
  Ce scénario introduit une pénalité fiscale à la sortie pour le CT. La taxe de 10 %
  s'applique sur les plus-values dépassant le montant exonéré, ce qui améliore
  relativement la position de la Branche 23 EP.

SCÉNARIO 3 — ELT B23 vs CT sans taxe PV
  Ce scénario teste la compétitivité de l'Épargne Long Terme. L'ELT est soumise à
  une taxe d'assurance de 2 % sur chaque prime (non applicable à l'EP) ainsi qu'à une
  taxe anticipative de 10 % (vs 8 % pour l'EP), mais bénéficie d'un plafond de
  déduction plus élevé (2 450 € vs 1 350 €) et d'un taux de réduction de 30 %.

SCÉNARIO 4 — ELT B23 vs CT avec taxe PV 10 %
  Combinaison la plus favorable à la Branche 23 ELT : le CT subit une taxe PV de
  10 % à la sortie, réduisant son avantage lié à l'absence de taxe anticipative.

Implémentation actuelle : le code contient deux lanceurs statistiques complets,
`ScenarioStatistiquesComplet` et `ScenarioStatistiquesCompletLast`, qui utilisent la
même structure de 4 scénarios mais des calibrations de frais/rendements différentes.


═══════════════════════════════════════════════════════════════════════════════════════
6. PARAMÈTRES COMMUNS À TOUS LES SCÉNARIOS
═══════════════════════════════════════════════════════════════════════════════════════

  Paramètre                          Valeur (configuration actuelle)
  ─────────────────────────────────────────────────────────────────
  Âge de fin de l'horizon            65 ans
  Âge de début (plage simulée)       18 à 55 ans (pas de 1 an)
  Année de naissance (référence)     2008
  Référentiel OLO net (capitalisation)
                                     Référentiel par durée (1..30 ans),
                                     puis taux 30 ans au-delà
  Rendement annuel brut (plage)      3 % à 10 % (pas de 1 %)
  TOB CT                             0,12 % par versement
  Frais par versement CT             0,80 %
  Frais par prime B23                2,00 %
  Frais de gestion annuels CT        0,12 %
  Frais de gestion annuels B23       1,50 %
  Exonération PV de base (CT)        10 000 €
  Exonération PV annuelle (CT)       1 000 € par année de contrat
  Durée maximale de cumul exo.       5 ans (exonération max. = 15 000 €)
  Seuil TCT                          1 000 000 €
  Taux TCT                           0,15 %
  Plafond TCT                        10 % du dépassement du seuil

Note : la table ci-dessus correspond à `ScenarioStatistiquesComplet`.
Dans `ScenarioStatistiquesCompletLast`, les principales différences sont :
  - rendement balayé de 2 % à 10 % (pas 1 %),
  - CT : frais par versement = 0, frais de gestion annuels = 0,
  - B23 : frais par prime = 2,83 %, frais de gestion annuels = 2,34 %.
La structure de calcul (VT, TCT, taxe anticipative, taxe PV) reste identique.


═══════════════════════════════════════════════════════════════════════════════════════
7. MODÉLISATION DU COMPTE-TITRES (CT)
═══════════════════════════════════════════════════════════════════════════════════════

7.1 Accumulation du capital (ordre de calcul annuel)
────────────────────────────
À chaque année t (t = 0 étant l'année du premier versement) :

  reserveAvantVersement(t) = 0                                           si t = 0
                           = Reserve(t-1) × (1 + rendement) × (1 − fraisGestion) sinon

  TCT(t)                    = taxeCompteTitres(reserveAvantVersement(t))
  reserveApresTaxes(t)      = reserveAvantVersement(t) − TCT(t)
  Reserve(t)                = reserveApresTaxes(t) + versementAnnuel

Les versements ne subissent pas de déduction à l'entrée (pas de taxe TOA pour le CT).

7.2 Calcul du capital net à l'échéance
───────────────────────────────────────
Le capital net tient compte de la taxe éventuelle sur les plus-values :

  plusValue      = max(0 ; Réserve_finale − coûtDeBase)
  coûtDeBase     = Σ versementsAnnuels sur toute la durée (prix de revient total)
  exonération    = montantBase + min(durée, 5) × montantAnnuel
                 = 10 000 € + min(durée, 5) × 1 000 €

  plusValueTaxable = max(0 ; plusValue − exonération)
  taxePV           = plusValueTaxable × tauxTaxePV    (0 % ou 10 % selon le scénario)
  capitalNet       = Réserve_finale − taxePV

Important : dans l'implémentation actuelle, la réserve finale est déjà nette de TCT
annuelles prélevées pendant la phase d'accumulation.

Note méthodologique : le prix de revient est calculé par la méthode du coût total
(équivalente à FIFO quand toutes les parts sont vendues simultanément à l'échéance).

7.3 Coûts récurrents modélisés
───────────────────────────────
  • Taxe sur les Opérations de Bourse (TOB) : 0,12 % par versement annuel
  • Frais de courtage par versement         : 0,80 % par versement annuel

Ces coûts sont capitalisés à l'échéance puis retranchés de la valeur terminale CT :

  coutAnnuel      = versementAnnuel × (tauxTOB + tauxFraisVersement)
  feesCapitalises = Σ [coutAnnuel × (1 + r(duree - t))^(duree - t)]  pour t = 0 à duree
  VT_CT           = capitalNet − feesCapitalises

7.4 Taxe annuelle sur les comptes-titres (TCT)
───────────────────────────────────────────────
La TCT est calculée chaque année sur la réserve avant versement de l'année.

  Si Réserve_fin ≤ 1 000 000 € :
      TCT = 0

  Si Réserve_fin > 1 000 000 € :
      taxeNormale = Réserve_fin × 0,15 %
      plafond     = (Réserve_fin − 1 000 000) × 10 %
      TCT         = min(taxeNormale, plafond)

La TCT annuelle est ensuite déduite de la réserve, puis le versement de l'année est ajouté.
La TCT est cumulée sur tout l'horizon (`taxeCompteTitresTotale`).


═══════════════════════════════════════════════════════════════════════════════════════
8. MODÉLISATION DE LA BRANCHE 23 — ÉPARGNE PENSION (EP)
═══════════════════════════════════════════════════════════════════════════════════════

8.1 Fiscalité à l'entrée
─────────────────────────
L'Épargne Pension ne supporte pas de taxe sur les opérations d'assurance (0 %).
Des frais de chargement (frais par prime) de 2 % sont prélevés sur chaque versement :

  versementNet = versementAnnuel × (1 − 0 %) × (1 − 2 %)
               = versementAnnuel × 0,98

8.2 Réduction fiscale à l'entrée
──────────────────────────────────
La déduction fiscale pour épargne-pension génère une économie d'impôt chaque année
(remboursée l'année suivante via la déclaration fiscale), selon le barème suivant :

  Si versement ≤ 1 050 € : économie = versement × 30 %
  Si versement ≤ 1 350 € : économie = versement × 25 %  (taux réduit au-delà du seuil)

  Note : le choix entre les deux taux est déterminé par le montant total de la prime,
  et non de manière progressive. Passer de 1 050 € à 1 051 € entraîne un saut fiscal
  (passage de 30 % à 25 % sur la totalité), ce qui est un point d'attention important.

La réduction fiscale est applicable uniquement entre 18 ans et 64 ans inclus.

8.3 Accumulation du capital (ordre de calcul annuel)
────────────────────────────
Le capital s'accumule de la même manière qu'au CT, mais sur la base du versement net
(après frais de chargement, sans taxe TOA) :

  reserveAvantVersement(t) = 0                                           si t = 0
                           = Reserve(t-1) × (1 + rendement) × (1 − fraisGestion) sinon

  si année anticipative :
      reserveAvantVersement(t) = reserveAvantVersement(t) × (1 − tauxTaxeAnticipative)

  TCT(t)       = taxeCompteTitres(reserveAvantVersement(t))
  Reserve(t)   = reserveAvantVersement(t) − TCT(t) + versementNet

8.4 Taxe anticipative à 60 ans
───────────────────────────────
La taxe anticipative est un prélèvement unique effectué sur la réserve constituée au
moment où l'investisseur atteint ses 60 ans (hors versement de cette année-là) :

  réserveAvantVersement = Réserve(59 ans) × (1 + rendement)
  réserveAprèsTaxe      = réserveAvantVersement × (1 − 8 %)

Le versement de l'année concernée est ajouté après les prélèvements (taxe anticipative,
puis TCT éventuelle).

Règle pour les souscriptions tardives (après 55 ans) :
  Si l'investisseur commence après 55 ans, la taxe anticipative n'est perçue qu'après
  10 ans de contrat (et non obligatoirement à 60 ans), conformément à la réglementation.

8.5 Pas de taxe à la sortie (hors TCT annuelle)
────────────────────────────
Contrairement au CT, la Branche 23 ne supporte pas de taxe PV de sortie à l'échéance.
La comparaison se fait sur :

  VT_B23 = capitalFinalNet + ecoFiscalesCapitalisees

où `capitalFinalNet` inclut déjà les éventuelles TCT annuelles prélevées en cours de vie.


═══════════════════════════════════════════════════════════════════════════════════════
9. MODÉLISATION DE LA BRANCHE 23 — ÉPARGNE LONG TERME (ELT)
═══════════════════════════════════════════════════════════════════════════════════════

9.1 Différences par rapport à l'Épargne Pension
─────────────────────────────────────────────────
L'ELT reprend la même structure de simulation que l'EP, avec les différences suivantes :

  Paramètre                    EP Branche 23         ELT Branche 23
  ──────────────────────────────────────────────────────────────────
  Taxe sur opér. d'assurance   0 %                   2 % (taxe TOA)
  Frais par prime              2 %                   2 %
  Taux de la taxe anticipative 8 %                   10 %
  Plafond de déduction         1 350 €/an            2 450 €/an
  Taux de réduction fiscale    30 % (≤1 050 €)        30 % (taux unique)
                               25 % (≤1 350 €)
  Plage de versements simulés  1 € → 1 350 €         1 € → 2 450 €

9.2 Fiscalité à l'entrée pour l'ELT
──────────────────────────────────────
  versementNet = versementAnnuel × (1 − 2 % taxe TOA) × (1 − 2 % frais)
               = versementAnnuel × 0,98 × 0,98
               = versementAnnuel × 0,9604

9.3 Réduction fiscale ELT
──────────────────────────
  Si versement ≤ 2 450 € : économie = min(versement, 2 450 €) × 30 %

  Le taux est unique à 30 % quelle que soit la tranche (contrairement à l'EP).
  La déduction est applicable entre 18 et 64 ans inclus.

9.4 Accumulation et taxe anticipative
───────────────────────────────────────
  La capitalisation suit la même séquence annuelle qu'en section 8.3
  (versement ajouté après taxe anticipative/TCT éventuelles).
  La taxe anticipative de 10 % est appliquée dans les mêmes conditions qu'en 8.4.

9.5 TCT annuelle dans le modèle
────────────────────────────────
Comme pour l'EP et le CT, une TCT annuelle est appliquée dans l'implémentation
actuelle lorsque la réserve avant versement de l'année dépasse 1 000 000 €,
selon la même règle de taux/plafond décrite en section 7.4.


═══════════════════════════════════════════════════════════════════════════════════════
10. LA PLAGE DE VERSEMENTS ET LA NOTION DE DOMINANCE
═══════════════════════════════════════════════════════════════════════════════════════

Pour chaque combinaison (ageDebut, rendement, scénario), on simule les deux instruments
pour chaque versement annuel entier de la plage :
  • EP  : v ∈ {1, 2, 3, …, 1 350} €/an → 1 350 simulations par instrument
  • ELT : v ∈ {1, 2, 3, …, 2 450} €/an → 2 450 simulations par instrument

Pour chaque valeur de versement v, on compare VT_B23(v) et VT_CT(v).

Indicateurs de dominance calculés sur la plage :
  • Taux de dominance A (%) = 100 × nb(v où VT_B23 > VT_CT) / nb(v total)
  • Taux de dominance B (%) = 100 × nb(v où VT_CT > VT_B23) / nb(v total)
  • Taux d'égalité (%)      = 100 × nb(v où VT_B23 = VT_CT) / nb(v total)

Dominant global :
  Le dominant global est déterminé en comparant les taux de dominance respectifs des
  deux instruments sur l'ensemble de la plage de versements :

    • Si tauxDominance_B23 > tauxDominance_CT  → Branche 23 est l'instrument dominant global
    • Si tauxDominance_CT  > tauxDominance_B23  → Compte-Titres est l'instrument dominant global
    • Si tauxDominance_B23 = tauxDominance_CT   → Ex æquo (aucun instrument ne domine l'autre)

  Exemple d'interprétation : un taux de dominance B23 de 80 % signifie que, pour 80 %
  des versements annuels testés dans la plage légale, la Branche 23 offre une VT
  supérieure à celle du Compte-Titres.


═══════════════════════════════════════════════════════════════════════════════════════
11. DÉTECTION DES POINTS DE CROISEMENT
═══════════════════════════════════════════════════════════════════════════════════════

Un point de croisement est le versement annuel v* à partir duquel la hiérarchie entre
les deux instruments s'inverse (B23 passe devant CT, ou l'inverse).

Méthode de détection :
  On parcourt la série des différences Δ(v) = VT_B23(v) − VT_CT(v) et on repère
  tout changement de signe entre deux versements consécutifs v_i et v_{i+1} :

    si signe(Δ(v_i)) ≠ signe(Δ(v_{i+1}))  →  il existe un croisement entre v_i et v_{i+1}

  Ce croisement est localisé par interpolation linéaire :

    v* = v_i − Δ(v_i) × (v_{i+1} − v_i) / (Δ(v_{i+1}) − Δ(v_i))

Filtrage des faux positifs :
  Une condition supplémentaire exige que l'un des deux instruments ait une valeur
  terminale non nulle (|VT| > 10^−6 €) afin d'éliminer les artefacts numériques.

Interprétation des points de croisement :
  • Premier croisement  : versement à partir duquel l'avantage bascule pour la première fois.
    C'est le seuil critique le plus important pour l'investisseur.
  • Dernier croisement  : si plusieurs inversions existent, le dernier donne le versement
    au-delà duquel l'avantage est stable et définitif.
  • Absence de croisement : l'un des instruments domine sur l'intégralité de la plage.


═══════════════════════════════════════════════════════════════════════════════════════
12. STATISTIQUES ET INDICATEURS PRODUITS
═══════════════════════════════════════════════════════════════════════════════════════

Pour chaque combinaison (ageDebut, rendement), une ligne CSV est produite contenant :

  Colonne CSV                  Description
  ──────────────────────────────────────────────────────────────────────────────────
  vehiculeA                    Nom de l'instrument A (ex : "EP Branche 23")
  vehiculeB                    Nom de l'instrument B (ex : "CT sans taxe PV")
  rendement_pct                Rendement annuel brut testé (en %)
  age_debut                    Âge de début des versements
  age_fin                      Âge de fin de l'horizon (65 ans)
  taux_taxe_pv_pct             Taux de la taxe PV appliqué au CT (0 % ou 10 %)
  versement_min                Borne inférieure de la plage (1 €)
  versement_max                Borne supérieure (1 350 € ou 2 450 €)
  taux_dominance_A_pct         % des versements où la B23 domine
  taux_dominance_B_pct         % des versements où le CT domine
  taux_egaux_pct               % des versements en situation d'égalité
  nb_croisements               Nombre de points de croisement détectés
  premier_croisement_eur       Versement du 1er croisement en € (ou NA si absent)
  dominant                     Instrument dominant global (par l'aire)
  capital_final_net_moyen_b23_eur            Capital final net moyen B23 (€)
  eco_fiscales_capitalisees_moyennes_b23_eur Économies fiscales capitalisées moyennes B23 (€)
  val_terminale_moyenne_ct_eur               Valeur terminale moyenne CT (€)

Indicateurs internes supplémentaires (niveau simulation unitaire) :
  • taxeCompteTitresTotale : cumul des TCT annuelles sur toute la durée.

Remarque : à ce stade, ce cumul TCT n'est pas encore exporté comme colonne dédiée
dans le CSV agrégé des statistiques.

Quatre fichiers CSV sont générés, un par scénario :
  1. ep_vs_ct_sans_taxe_pv.csv
  2. ep_vs_ct_avec_taxe_pv.csv
  3. elt_vs_ct_sans_taxe_pv.csv
  4. elt_vs_ct_avec_taxe_pv.csv


═══════════════════════════════════════════════════════════════════════════════════════
13. REPRÉSENTATION GRAPHIQUE : HEATMAPS
═══════════════════════════════════════════════════════════════════════════════════════

Les résultats statistiques sont visualisés sous forme de heatmaps bidimensionnelles.

  Axe X (colonnes) : rendement annuel brut (de 3 % à 10 %)
  Axe Y (lignes)   : âge de début des versements (de 18 à 55 ans)

  Chaque cellule (ageDebut, rendement) représente une combinaison unique et indique :
    • La couleur principale : instrument dominant global
        – Bleu  : la Branche 23 (EP ou ELT) est globalement dominante
        – Vert  : le Compte-Titres est globalement dominant
        – Gris  : ex æquo (aucun dominant)
    • L'intensité de la couleur : force de la dominance (taux de dominance A ou B)
        – Plus le taux de dominance est élevé, plus la couleur est saturée
    • L'annotation textuelle : versement au premier croisement en €/an
        (si un croisement existe, indiquant le seuil de bascule)

La heatmap permet d'identifier visuellement :
  1. Les zones où la Branche 23 est systématiquement avantageuse (quel que soit le
     versement dans la plage légale)
  2. Les zones où le Compte-Titres est systématiquement plus performant
  3. Les zones mixtes, avec le versement-seuil à partir duquel la hiérarchie s'inverse

Quatre heatmaps sont produites, une par scénario.


═══════════════════════════════════════════════════════════════════════════════════════
14. LIMITES ET HYPOTHÈSES DE LA MÉTHODOLOGIE
═══════════════════════════════════════════════════════════════════════════════════════

Les hypothèses suivantes constituent des simplifications par rapport à la réalité :

  a) RENDEMENT CONSTANT
     Le taux de rendement annuel brut est supposé constant sur toute la durée. En
     pratique, les marchés financiers sont volatils. Cette hypothèse est nécessaire
     pour rendre la comparaison déterministe et systématique.

  b) VERSEMENTS CONSTANTS
     Les versements annuels sont supposés identiques chaque année. L'inflation et
     l'évolution du revenu de l'investisseur ne sont pas modélisés.

  c) FRAIS CONSTANTS PAR SCÉNARIO
     Les frais (versement, prime, gestion) sont constants dans un scénario donné
     et ne varient pas dans le temps. Les variations de tarification réelles ne
     sont pas modélisées.

  d) FISCALITÉ FIGÉE
     La réglementation fiscale est celle en vigueur au moment de la rédaction. Tout
     changement législatif futur (modification des taux, des plafonds, etc.) n'est
     pas pris en compte.

  e) COMPARAISON EN VALEUR TERMINALE (ET NON EN VAN)
     Le critère de comparaison est la valeur terminale nette à l'échéance. Le moment
     des flux est pris en compte par leur capitalisation (économies fiscales et coûts)
     via la courbe OLO nette retenue.

  f) CHOIX DU MOTEUR DE CAPITALISATION
     La classe `CalculateurVAN` est conservée pour compatibilité mais n'est plus
     utilisée dans la chaîne de calcul. Les résultats reposent sur
     `CalculateurCapitalisation`.

  g) ABSENCE DE RISQUE DE CONTREPARTIE
     Les deux instruments sont supposés sans risque de défaut ou de faillite de
     l'assureur / du courtier.

  h) HORIZON RETRAITE FIXE
     L'horizon est fixé à 65 ans pour tous les profils. La possibilité de rachat
     anticipé ou de transfert de contrat n'est pas modélisée.

  i) MÉTHODE D'EXONÉRATION PV SIMPLIFIÉE
     L'exonération des plus-values pour le CT est calculée de manière forfaitaire
     (montant de base + cumul annuel plafonné), ce qui est une approximation du
     régime réel.

  j) ASSIETTE TCT APPROCHÉE
     La TCT est évaluée sur la réserve avant versement de l'année, ce qui
     constitue une approximation par rapport à un calcul fondé sur des moyennes
     infra-annuelles plus fines.

═══════════════════════════════════════════════════════════════════════════════════════
FIN DU DOCUMENT
═══════════════════════════════════════════════════════════════════════════════════════

