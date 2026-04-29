package com.kerware.simulateur;

// Interface commune aux deux implémentations (code hérité et code réusiné)
public interface ICalculateurImpot {

    // Entrées
    void setRevenusNetDeclarant1(int rn);
    void setRevenusNetDeclarant2(int rn);
    void setSituationFamiliale(SituationFamiliale sf);
    void setNbEnfantsACharge(int nbe);
    void setNbEnfantsSituationHandicap(int nbesh);
    void setParentIsole(boolean pi);

    // Déclenche le calcul — à appeler après avoir renseigné toutes les entrées
    void calculImpotSurRevenuNet();

    // Résultats intermédiaires et final
    int getRevenuFiscalReference();
    int getAbattement();
    double getNbPartsFoyerFiscal();
    int getImpotAvantDecote();
    int getDecote();
    int getImpotSurRevenuNet();
}
