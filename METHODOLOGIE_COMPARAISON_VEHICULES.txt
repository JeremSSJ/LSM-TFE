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
  4. Le critère de comparaison : la Valeur Actuelle Nette (VAN)
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
exprimé en Valeur Actuelle Nette (VAN). Cette comparaison est répétée pour l'ensemble
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

Les versements sont supposés constants d'année en année (annuités constantes). Ils sont
effectués en début d'année (premier versement à l'âge de début, dernier à l'âge de fin).

Règle particulière pour la Branche 23 :
  Si l'investisseur souscrit après l'âge de 55 ans, la règle des « 10 ans minimum avant
  la taxe anticipative » s'applique, ce qui décale l'année de prélèvement de la taxe.


═══════════════════════════════════════════════════════════════════════════════════════
4. LE CRITÈRE DE COMPARAISON : LA VALEUR ACTUELLE NETTE (VAN)
═══════════════════════════════════════════════════════════════════════════════════════

La comparaison entre les deux instruments repose exclusivement sur leur VAN totale,
définie comme la somme actualisée de tous les flux nets de l'investisseur.

Le taux d'actualisation retenu est le taux OLO (Obligations Linéaires) à 10 ans, fixé
à 3 % dans les scénarios, représentant le coût d'opportunité sans risque de l'investisseur.

4.1 VAN d'un instrument de la Branche 23
─────────────────────────────────────────
La VAN B23 est décomposée en deux composantes :

  VAN_B23 = VAN_Capital_B23 + VAN_EconomiesFiscales_B23

  • VAN_Capital_B23 :
    Valeur actuelle du capital net accumulé à l'échéance (après déduction de la taxe
    anticipative), actualisé sur toute la durée de l'investissement.

        VAN_Capital = CapitalFinalNet / (1 + tauxOLO)^durée

  • VAN_EconomiesFiscales_B23 :
    Somme actualisée des réductions d'impôt annuelles obtenues grâce à la déduction
    fiscale. Chaque économie fiscale encaissée à l'année t est actualisée au facteur
    (1 + tauxOLO)^(t+1) car elle est perçue l'année suivante (remboursement d'impôt).

        VAN_Eco = Σ [EconomieFiscale(t) / (1 + tauxOLO)^(t+1)]  pour t = 0 à N-1

4.2 VAN d'un Compte-Titres
──────────────────────────
La VAN du CT intègre le capital net et les coûts récurrents :

  VAN_CT = VAN_CapitalNet_CT − VAN_TOB − VAN_FraisParVersement

  • VAN_CapitalNet_CT :
    Valeur actuelle du capital brut après déduction de la taxe éventuelle sur les
    plus-values, actualisé sur la durée.

  • VAN_TOB (Taxe sur les Opérations de Bourse) :
    Actualisée comme une rente annuelle sur toute la durée :
        VAN_TOB = Σ [versement × tauxTOB / (1 + tauxOLO)^t]  pour t = 0 à N

  • VAN_FraisParVersement :
    Calculée de la même façon que la VAN_TOB.

Note : le CT ne génère pas d'économies fiscales à l'entrée (vanEconomiesFiscales = 0).

4.3 Comparaison
───────────────
Pour chaque versement annuel v de la plage testée, on calcule la différence :

  Δ(v) = VAN_B23(v) − VAN_CT(v)

  • Si Δ(v) > 0 → la Branche 23 est plus avantageuse pour ce versement
  • Si Δ(v) < 0 → le Compte-Titres est plus avantageux
  • Si Δ(v) ≈ 0 → ex æquo (|Δ| < 1 € de VAN)


═══════════════════════════════════════════════════════════════════════════════════════
5. DESCRIPTION DES QUATRE SCÉNARIOS
═══════════════════════════════════════════════════════════════════════════════════════

Les quatre scénarios résultent du croisement de deux dimensions :
  • Le type de déduction fiscale appliquée à la Branche 23 : EP ou ELT
  • Le régime fiscal appliqué au Compte-Titres : sans ou avec taxe sur les plus-values

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


═══════════════════════════════════════════════════════════════════════════════════════
6. PARAMÈTRES COMMUNS À TOUS LES SCÉNARIOS
═══════════════════════════════════════════════════════════════════════════════════════

  Paramètre                          Valeur
  ─────────────────────────────────────────────────────────────────
  Âge de fin de l'horizon            65 ans
  Âge de début (plage simulée)       18 à 55 ans (pas de 1 an)
  Année de naissance (référence)     2008
  Taux OLO (actualisation)           3 %
  Rendement annuel brut (plage)      3 % à 10 % (pas de 1 %)
  Frais par versement / prime        2 % (CT et B23)
  Frais de gestion annuels           0 % (les deux véhicules)
  Exonération PV de base (CT)        10 000 €
  Exonération PV annuelle (CT)       1 000 € par année de contrat
  Durée maximale de cumul exo.       5 ans (exonération max. = 15 000 €)

Note sur les frais de gestion : ils sont fixés à 0 % pour isoler l'effet fiscal pur.
En pratique, les fonds Branche 23 ont des frais de gestion internes (TER) qui viendraient
dégrader leur performance, mais ceux-ci ne sont pas l'objet de ce travail.


═══════════════════════════════════════════════════════════════════════════════════════
7. MODÉLISATION DU COMPTE-TITRES (CT)
═══════════════════════════════════════════════════════════════════════════════════════

7.1 Accumulation du capital
────────────────────────────
À chaque année t (t = 0 étant l'année du premier versement) :

  Réserve(t=0) = versementAnnuel
  Réserve(t)   = Réserve(t-1) × (1 + rendement) × (1 − fraisGestion) + versementAnnuel

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

Note méthodologique : le prix de revient est calculé par la méthode du coût total
(équivalente à FIFO quand toutes les parts sont vendues simultanément à l'échéance).

7.3 Coûts récurrents modélisés
───────────────────────────────
  • Taxe sur les Opérations de Bourse (TOB) : 0,12 % par versement annuel
  • Frais de courtage par versement         : 2,00 % par versement annuel

Ces coûts sont intégrés dans la VAN via une rente annuelle actualisée (et non déduits
du capital, afin de conserver une cohérence avec la structure de la VAN B23).


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

8.3 Accumulation du capital
────────────────────────────
Le capital s'accumule de la même manière qu'au CT, mais sur la base du versement net
(après frais de chargement, sans taxe TOA) :

  Réserve(t=0) = versementNet
  Réserve(t)   = Réserve(t-1) × (1 + rendement) × (1 − fraisGestion) + versementNet

8.4 Taxe anticipative à 60 ans
───────────────────────────────
La taxe anticipative est un prélèvement unique effectué sur la réserve constituée au
moment où l'investisseur atteint ses 60 ans (hors versement de cette année-là) :

  réserveAvantVersement = Réserve(59 ans) × (1 + rendement)
  réserveAprèsTaxe      = réserveAvantVersement × (1 − 8 %)

Le versement de l'année du 60e anniversaire est ensuite ajouté après le prélèvement.

Règle pour les souscriptions tardives (après 55 ans) :
  Si l'investisseur commence après 55 ans, la taxe anticipative n'est perçue qu'après
  10 ans de contrat (et non obligatoirement à 60 ans), conformément à la réglementation.

8.5 Pas de taxe à la sortie
────────────────────────────
Contrairement au CT, la Branche 23 ne supporte aucune taxe supplémentaire à l'échéance.
Le capital final net est directement le solde de la réserve après taxe anticipative.


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
  La capitalisation suit la même formule qu'en section 8.3.
  La taxe anticipative de 10 % est appliquée dans les mêmes conditions qu'en 8.4.


═══════════════════════════════════════════════════════════════════════════════════════
10. LA PLAGE DE VERSEMENTS ET LA NOTION DE DOMINANCE
═══════════════════════════════════════════════════════════════════════════════════════

Pour chaque combinaison (ageDebut, rendement, scénario), on simule les deux instruments
pour chaque versement annuel entier de la plage :
  • EP  : v ∈ {1, 2, 3, …, 1 350} €/an → 1 350 simulations par instrument
  • ELT : v ∈ {1, 2, 3, …, 2 450} €/an → 2 450 simulations par instrument

Pour chaque valeur de versement v, on compare VAN_B23(v) et VAN_CT(v).

Indicateurs de dominance calculés sur la plage :
  • Taux de dominance A (%) = 100 × nb(v où VAN_B23 > VAN_CT) / nb(v total)
  • Taux de dominance B (%) = 100 × nb(v où VAN_CT > VAN_B23) / nb(v total)
  • Taux d'égalité (%)      = 100 × nb(v où VAN_B23 = VAN_CT) / nb(v total)

Dominant global :
  Le dominant global est déterminé en comparant les taux de dominance respectifs des
  deux instruments sur l'ensemble de la plage de versements :

    • Si tauxDominance_B23 > tauxDominance_CT  → Branche 23 est l'instrument dominant global
    • Si tauxDominance_CT  > tauxDominance_B23  → Compte-Titres est l'instrument dominant global
    • Si tauxDominance_B23 = tauxDominance_CT   → Ex æquo (aucun instrument ne domine l'autre)

  Exemple d'interprétation : un taux de dominance B23 de 80 % signifie que, pour 80 %
  des versements annuels testés dans la plage légale, la Branche 23 offre une VAN
  supérieure à celle du Compte-Titres.


═══════════════════════════════════════════════════════════════════════════════════════
11. DÉTECTION DES POINTS DE CROISEMENT
═══════════════════════════════════════════════════════════════════════════════════════

Un point de croisement est le versement annuel v* à partir duquel la hiérarchie entre
les deux instruments s'inverse (B23 passe devant CT, ou l'inverse).

Méthode de détection :
  On parcourt la série des différences Δ(v) = VAN_B23(v) − VAN_CT(v) et on repère
  tout changement de signe entre deux versements consécutifs v_i et v_{i+1} :

    si signe(Δ(v_i)) ≠ signe(Δ(v_{i+1}))  →  il existe un croisement entre v_i et v_{i+1}

  Ce croisement est localisé par interpolation linéaire :

    v* = v_i − Δ(v_i) × (v_{i+1} − v_i) / (Δ(v_{i+1}) − Δ(v_i))

Filtrage des faux positifs :
  Une condition supplémentaire exige que l'un des deux instruments ait une VAN non nulle
  (|VAN| > 10^−6 €) afin d'éliminer les artefacts numériques en début de plage.

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
  van_moy_capital_b23_eur      VAN moyenne du capital B23 sur la plage (€)
  van_moy_eco_fiscales_b23_eur VAN moyenne des économies fiscales B23 (€)
  van_moy_capital_ct_eur       VAN moyenne du capital CT net sur la plage (€)

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

  c) FRAIS DE GESTION NULS
     Les frais de gestion annuels (TER des fonds) sont fixés à 0 % pour les deux
     instruments. En pratique, les fonds Branche 23 ont des coûts internes plus
     élevés que les ETF passifs.

  d) FISCALITÉ FIGÉE
     La réglementation fiscale est celle en vigueur au moment de la rédaction. Tout
     changement législatif futur (modification des taux, des plafonds, etc.) n'est
     pas pris en compte.

  e) COMPARAISON EN VAN ET NON EN CAPITAL BRUT
     Le critère de comparaison est la VAN et non le capital accumulé. Ce choix permet
     de tenir compte du moment où les flux sont perçus (économies fiscales annuelles
     vs capital à terme), mais il dépend du taux d'actualisation retenu (OLO 3 %).

  f) ABSENCE DE RISQUE DE CONTREPARTIE
     Les deux instruments sont supposés sans risque de défaut ou de faillite de
     l'assureur / du courtier.

  g) HORIZON RETRAITE FIXE
     L'horizon est fixé à 65 ans pour tous les profils. La possibilité de rachat
     anticipé ou de transfert de contrat n'est pas modélisée.

  h) MÉTHODE D'EXONÉRATION PV SIMPLIFIÉE
     L'exonération des plus-values pour le CT est calculée de manière forfaitaire
     (montant de base + cumul annuel plafonné), ce qui est une approximation du
     régime réel.

═══════════════════════════════════════════════════════════════════════════════════════
FIN DU DOCUMENT
═══════════════════════════════════════════════════════════════════════════════════════

