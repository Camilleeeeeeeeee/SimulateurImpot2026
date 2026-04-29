package com.kerware;

import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateurreusine.AdaptateurCodeReusine;
import org.junit.jupiter.api.DisplayName;

// Suite principale : tous les tests doivent passer sur le code réusiné
@DisplayName("Simulateur réusiné — tous les tests doivent passer")
class SimulateurImpotReusineTest extends AbstractSimulateurImpotTest {

    @Override
    protected ICalculateurImpot creerCalculateur() {
        return new AdaptateurCodeReusine();
    }
}
