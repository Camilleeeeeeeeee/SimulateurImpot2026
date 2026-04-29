package com.kerware.simulateurreusine;

import com.kerware.simulateur.SituationFamiliale;

/**
 * Simulateur de calcul de l'impôt sur le revenu 2024 (revenus 2023).
 * Implémente les exigences EXG_IMPOT_01 à EXG_IMPOT_07.
 */
public class Simulateur {

    // EXG_IMPOT_02 : Abattement
    private static final double TAUX_ABATTEMENT = 0.10;
    private static final int ABATTEMENT_MIN = 495;
    private static final int ABATTEMENT_MAX = 14171;

    // EXG_IMPOT_04 : Barème progressif
    private static final int[] LIMITES_TRANCHES = {0, 11294, 28797, 82341, 177106};
    private static final double[] TAUX_TRANCHES  = {0.0, 0.11, 0.30, 0.41, 0.45};

    // EXG_IMPOT_05 : Plafonnement demi-part
    private static final double GAIN_MAX_PAR_DEMI_PART = 1759.0;

    // EXG_IMPOT_06 : Décote
    private static final int    SEUIL_DECOTE_SEUL    = 1929;
    private static final int    SEUIL_DECOTE_COUPLE  = 3191;
    private static final double DECOTE_BASE_SEUL     = 873.0;
    private static final double DECOTE_BASE_COUPLE   = 1444.0;
    private static final double TAUX_DECOTE          = 0.4525;

    // EXG_IMPOT_07 : Contribution exceptionnelle hauts revenus
    private static final int    SEUIL_CEHR_SEUL_BAS   = 250_000;
    private static final int    SEUIL_CEHR_SEUL_HAUT  = 500_000;
    private static final int    SEUIL_CEHR_COUPLE_BAS = 500_000;
    private static final int    SEUIL_CEHR_TRES_HAUT  = 1_000_000;
    private static final double TAUX_CEHR_TRANCHE_1   = 0.03;
    private static final double TAUX_CEHR_TRANCHE_2   = 0.04;

    private static final int NB_ENFANTS_MAX = 7;

    private int revenuNetDeclarant1;
    private int revenuNetDeclarant2;
    private SituationFamiliale situationFamiliale;
    private int nbEnfantsACharge;
    private int nbEnfantsSituationHandicap;
    private boolean parentIsole;

    private int    revenuFiscalReference;
    private int    abattement;
    private double nbPartsFoyerFiscal;
    private int    impotAvantDecote;
    private int    decote;
    private int    impotSurRevenuNet;

    public void setRevenuNetDeclarant1(int rn) { this.revenuNetDeclarant1 = rn; }
    public void setRevenuNetDeclarant2(int rn) { this.revenuNetDeclarant2 = rn; }
    public void setSituationFamiliale(SituationFamiliale sf) { this.situationFamiliale = sf; }
    public void setNbEnfantsACharge(int nbe) { this.nbEnfantsACharge = nbe; }
    public void setNbEnfantsSituationHandicap(int nbesh) { this.nbEnfantsSituationHandicap = nbesh; }
    public void setParentIsole(boolean pi) { this.parentIsole = pi; }

    /**
     * Lance le calcul de l'impôt sur le revenu net.
     * Les résultats sont accessibles via les getters après appel.
     *
     * @throws IllegalArgumentException si les données d'entrée sont invalides
     */
    public void calculImpotSurRevenuNet() {
        validerEntrees();

        // EXG_IMPOT_02
        abattement = calculerAbattementTotal();
        revenuFiscalReference = revenuNetDeclarant1 + revenuNetDeclarant2 - abattement;

        // EXG_IMPOT_03
        nbPartsFoyerFiscal = calculerNbParts();
        double nbPartsDeclarants = calculerNbPartsDeclarants();

        // EXG_IMPOT_04 — impôt des déclarants seuls (base pour EXG_IMPOT_05)
        double impotDeclarantsSeuls = calculerImpotProgressif(
                (double) revenuFiscalReference / nbPartsDeclarants) * nbPartsDeclarants;
        impotDeclarantsSeuls = Math.round(impotDeclarantsSeuls);

        // EXG_IMPOT_04 — impôt foyer fiscal complet
        double impotFoyer = calculerImpotProgressif(
                (double) revenuFiscalReference / nbPartsFoyerFiscal) * nbPartsFoyerFiscal;
        impotFoyer = Math.round(impotFoyer);

        // EXG_IMPOT_05 — plafonnement : le gain fiscal des enfants ne peut pas dépasser
        // GAIN_MAX_PAR_DEMI_PART par demi-part supplémentaire
        double partsEnfants = nbPartsFoyerFiscal - nbPartsDeclarants;
        double plafondGain = (partsEnfants / 0.5) * GAIN_MAX_PAR_DEMI_PART;
        if (impotDeclarantsSeuls - impotFoyer > plafondGain) {
            impotFoyer = impotDeclarantsSeuls - plafondGain;
        }

        // EXG_IMPOT_01 — arrondi
        impotAvantDecote = (int) Math.round(impotFoyer);

        // EXG_IMPOT_06 — décote
        decote = calculerDecote(impotAvantDecote, nbPartsDeclarants);

        // EXG_IMPOT_07 — contribution exceptionnelle hauts revenus
        int cehr = calculerCEHR(revenuFiscalReference, nbPartsDeclarants);

        impotSurRevenuNet = Math.max(0, impotAvantDecote - decote) + cehr;
    }

    // EXG_IMPOT_02
    private int calculerAbattementTotal() {
        if (estCouple()) {
            return abattementParDeclarant(revenuNetDeclarant1)
                 + abattementParDeclarant(revenuNetDeclarant2);
        }
        return abattementParDeclarant(revenuNetDeclarant1);
    }

    private int abattementParDeclarant(int revenu) {
        double abt = revenu * TAUX_ABATTEMENT;
        abt = Math.min(abt, ABATTEMENT_MAX);
        abt = Math.max(abt, ABATTEMENT_MIN);
        return (int) Math.round(abt);
    }

    // EXG_IMPOT_03
    private double calculerNbPartsDeclarants() {
        // Veuf avec enfants : conserve la part du conjoint décédé
        if (situationFamiliale == SituationFamiliale.VEUF && nbEnfantsACharge > 0) {
            return 2.0;
        }
        return estCouple() ? 2.0 : 1.0;
    }

    private double calculerNbParts() {
        double nbParts = calculerNbPartsDeclarants();

        // 0.5 part par enfant pour les 2 premiers, 1 part entière à partir du 3e
        if (nbEnfantsACharge <= 2) {
            nbParts += nbEnfantsACharge * 0.5;
        } else {
            nbParts += 1.0 + (nbEnfantsACharge - 2);
        }

        // 0.5 part supplémentaire par enfant en situation de handicap
        nbParts += nbEnfantsSituationHandicap * 0.5;

        // 0.5 part supplémentaire pour le parent isolé (uniquement s'il a des enfants)
        if (parentIsole && nbEnfantsACharge > 0) {
            nbParts += 0.5;
        }

        return nbParts;
    }

    // EXG_IMPOT_04 : calcul progressif tranche par tranche pour 1 part
    private double calculerImpotProgressif(double revenuParPart) {
        double impot = 0.0;
        for (int i = 0; i < TAUX_TRANCHES.length; i++) {
            int plafondTranche = (i + 1 < LIMITES_TRANCHES.length)
                    ? LIMITES_TRANCHES[i + 1] : Integer.MAX_VALUE;
            if (revenuParPart <= LIMITES_TRANCHES[i]) {
                break;
            }
            double baseImposable = Math.min(revenuParPart, plafondTranche) - LIMITES_TRANCHES[i];
            impot += baseImposable * TAUX_TRANCHES[i];
        }
        return impot;
    }

    // EXG_IMPOT_06
    private int calculerDecote(int montantImpot, double nbPartsDeclarants) {
        boolean couple = nbPartsDeclarants >= 2 && estCouple();
        double seuilDecote = couple ? SEUIL_DECOTE_COUPLE : SEUIL_DECOTE_SEUL;
        double decoteBase  = couple ? DECOTE_BASE_COUPLE  : DECOTE_BASE_SEUL;

        if (montantImpot >= seuilDecote) {
            return 0;
        }
        double montantDecote = Math.round(decoteBase - TAUX_DECOTE * montantImpot);
        return (int) Math.min(montantDecote, montantImpot);
    }

    // EXG_IMPOT_07
    private int calculerCEHR(int rfr, double nbPartsDeclarants) {
        boolean couple = nbPartsDeclarants >= 2 && estCouple();
        double cehr = 0.0;

        if (couple) {
            if (rfr > SEUIL_CEHR_COUPLE_BAS) {
                cehr += (Math.min(rfr, SEUIL_CEHR_TRES_HAUT) - SEUIL_CEHR_COUPLE_BAS) * TAUX_CEHR_TRANCHE_1;
            }
            if (rfr > SEUIL_CEHR_TRES_HAUT) {
                cehr += (rfr - SEUIL_CEHR_TRES_HAUT) * TAUX_CEHR_TRANCHE_2;
            }
        } else {
            if (rfr > SEUIL_CEHR_SEUL_BAS) {
                cehr += (Math.min(rfr, SEUIL_CEHR_SEUL_HAUT) - SEUIL_CEHR_SEUL_BAS) * TAUX_CEHR_TRANCHE_1;
            }
            if (rfr > SEUIL_CEHR_SEUL_HAUT) {
                cehr += (Math.min(rfr, SEUIL_CEHR_TRES_HAUT) - SEUIL_CEHR_SEUL_HAUT) * TAUX_CEHR_TRANCHE_2;
            }
            if (rfr > SEUIL_CEHR_TRES_HAUT) {
                cehr += (rfr - SEUIL_CEHR_TRES_HAUT) * TAUX_CEHR_TRANCHE_2;
            }
        }
        return (int) Math.round(cehr);
    }

    private void validerEntrees() {
        if (revenuNetDeclarant1 < 0 || revenuNetDeclarant2 < 0) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif.");
        }
        if (nbEnfantsACharge < 0 || nbEnfantsACharge > NB_ENFANTS_MAX) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants doit être compris entre 0 et " + NB_ENFANTS_MAX + ".");
        }
        if (nbEnfantsSituationHandicap < 0) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants en situation de handicap ne peut pas être négatif.");
        }
        if (nbEnfantsSituationHandicap > nbEnfantsACharge) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants en situation de handicap ne peut pas dépasser le nombre d'enfants à charge.");
        }
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être nulle.");
        }
    }

    private boolean estCouple() {
        return situationFamiliale == SituationFamiliale.MARIE
            || situationFamiliale == SituationFamiliale.PACSE;
    }

    public int getRevenuFiscalReference()  { return revenuFiscalReference; }
    public int getAbattement()             { return abattement; }
    public double getNbPartsFoyerFiscal()  { return nbPartsFoyerFiscal; }
    public int getImpotAvantDecote()       { return impotAvantDecote; }
    public int getDecote()                 { return decote; }
    public int getImpotSurRevenuNet()      { return impotSurRevenuNet; }
}
