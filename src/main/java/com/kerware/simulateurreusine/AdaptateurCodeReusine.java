package com.kerware.simulateurreusine;

import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateur.SituationFamiliale;

// Adaptateur exposant le code réusiné via l'interface ICalculateurImpot.
// Délègue entièrement au Simulateur réusiné, sans logique propre.
public class AdaptateurCodeReusine implements ICalculateurImpot {

    private final Simulateur simulateur = new Simulateur();

    @Override
    public void setRevenusNetDeclarant1(int rn) {
        simulateur.setRevenuNetDeclarant1(rn);
    }

    @Override
    public void setRevenusNetDeclarant2(int rn) {
        simulateur.setRevenuNetDeclarant2(rn);
    }

    @Override
    public void setSituationFamiliale(SituationFamiliale sf) {
        simulateur.setSituationFamiliale(sf);
    }

    @Override
    public void setNbEnfantsACharge(int nbe) {
        simulateur.setNbEnfantsACharge(nbe);
    }

    @Override
    public void setNbEnfantsSituationHandicap(int nbesh) {
        simulateur.setNbEnfantsSituationHandicap(nbesh);
    }

    @Override
    public void setParentIsole(boolean pi) {
        simulateur.setParentIsole(pi);
    }

    @Override
    public void calculImpotSurRevenuNet() {
        simulateur.calculImpotSurRevenuNet();
    }

    @Override
    public int getRevenuFiscalReference() { return simulateur.getRevenuFiscalReference(); }

    @Override
    public int getAbattement() { return simulateur.getAbattement(); }

    @Override
    public double getNbPartsFoyerFiscal() { return simulateur.getNbPartsFoyerFiscal(); }

    @Override
    public int getImpotAvantDecote() { return simulateur.getImpotAvantDecote(); }

    @Override
    public int getDecote() { return simulateur.getDecote(); }

    @Override
    public int getImpotSurRevenuNet() { return simulateur.getImpotSurRevenuNet(); }
}
