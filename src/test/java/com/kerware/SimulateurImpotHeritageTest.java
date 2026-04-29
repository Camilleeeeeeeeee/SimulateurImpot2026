package com.kerware;

import com.kerware.simulateur.AdaptateurCodeHerite;
import com.kerware.simulateur.ICalculateurImpot;
import org.junit.jupiter.api.DisplayName;

// Suite de détection des bugs du code hérité.
// Les échecs attendus correspondent aux bugs connus :
//   - VEUF avec enfants : nbPtsDecl écrasé à 1 au lieu de 2
//   - PACSE non géré dans le switch → nbPtsDecl = 0 → impôt incorrect
//   - Abattement calculé sur le revenu total et non par déclarant
//   - Contribution exceptionnelle hauts revenus (CEHR) absente
@DisplayName("Simulateur hérité — détection des bugs")
class SimulateurImpotHeritageTest extends AbstractSimulateurImpotTest {

    @Override
    protected ICalculateurImpot creerCalculateur() {
        return new AdaptateurCodeHerite();
    }
}
