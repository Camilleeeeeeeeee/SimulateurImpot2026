package com.kerware.simulateur;

// Adaptateur exposant le code hérité (Simulateur) via l'interface ICalculateurImpot.
// La validation des entrées est ajoutée ici car elle est absente du code original.
public class AdaptateurCodeHerite implements ICalculateurImpot {

    private final Simulateur simulateur = new Simulateur();

    private int revenuNetDeclarant1 = 0;
    private int revenuNetDeclarant2 = 0;
    private SituationFamiliale situationFamiliale;
    private int nbEnfantsACharge = 0;
    private int nbEnfantsSituationHandicap = 0;
    private boolean parentIsole = false;

    private int revenuFiscalReference = 0;
    private int abattement = 0;
    private double nbPartsFoyerFiscal = 0;
    private int impotAvantDecote = 0;
    private int decote = 0;
    private int impotSurRevenuNet = 0;

    @Override
    public void setRevenusNetDeclarant1(int rn) {
        this.revenuNetDeclarant1 = rn;
    }

    @Override
    public void setRevenusNetDeclarant2(int rn) {
        this.revenuNetDeclarant2 = rn;
    }

    @Override
    public void setSituationFamiliale(SituationFamiliale sf) {
        this.situationFamiliale = sf;
    }

    @Override
    public void setNbEnfantsACharge(int nbe) {
        this.nbEnfantsACharge = nbe;
    }

    @Override
    public void setNbEnfantsSituationHandicap(int nbesh) {
        this.nbEnfantsSituationHandicap = nbesh;
    }

    @Override
    public void setParentIsole(boolean pi) {
        this.parentIsole = pi;
    }

    @Override
    public void calculImpotSurRevenuNet() {
        validerEntrees();
        // Le code hérité attend un revenu total unique (pas séparé par déclarant)
        int revenuTotal = revenuNetDeclarant1 + revenuNetDeclarant2;
        long resultat = simulateur.calculImpot(
                revenuTotal, situationFamiliale,
                nbEnfantsACharge, nbEnfantsSituationHandicap, parentIsole);

        revenuFiscalReference = simulateur.getRevenuFiscalReference();
        abattement = simulateur.getAbattement();
        nbPartsFoyerFiscal = simulateur.getNbPartsFoyerFiscal();
        impotAvantDecote = simulateur.getImpotAvantDecote();
        decote = simulateur.getDecote();
        impotSurRevenuNet = (int) resultat;
    }

    private void validerEntrees() {
        if (revenuNetDeclarant1 < 0 || revenuNetDeclarant2 < 0) {
            throw new IllegalArgumentException("Le revenu net ne peut pas être négatif.");
        }
        if (nbEnfantsACharge < 0 || nbEnfantsACharge > 7) {
            throw new IllegalArgumentException("Le nombre d'enfants doit être entre 0 et 7.");
        }
        if (nbEnfantsSituationHandicap < 0) {
            throw new IllegalArgumentException("Le nombre d'enfants en situation de handicap ne peut pas être négatif.");
        }
        if (nbEnfantsSituationHandicap > nbEnfantsACharge) {
            throw new IllegalArgumentException(
                    "Le nombre d'enfants en situation de handicap ne peut pas dépasser le nombre d'enfants à charge.");
        }
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale ne peut pas être nulle.");
        }
    }

    @Override
    public int getRevenuFiscalReference() { return revenuFiscalReference; }

    @Override
    public int getAbattement() { return abattement; }

    @Override
    public double getNbPartsFoyerFiscal() { return nbPartsFoyerFiscal; }

    @Override
    public int getImpotAvantDecote() { return impotAvantDecote; }

    @Override
    public int getDecote() { return decote; }

    @Override
    public int getImpotSurRevenuNet() { return impotSurRevenuNet; }
}
