package com.kerware;

import com.kerware.simulateur.ICalculateurImpot;
import com.kerware.simulateur.SituationFamiliale;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Batterie de tests unitaires fonctionnels du simulateur d'impôt 2024.
 * Couvre les exigences EXG_IMPOT_01 à EXG_IMPOT_07.
 *
 * Cette classe abstraite est partagée par deux suites concrètes :
 *   - SimulateurImpotReusineTest  : tous les tests passent (code réusiné)
 *   - SimulateurImpotHeritageTest : certains tests échouent (bugs du code hérité détectés)
 */
@DisplayName("Simulateur d'impôt 2024 — Tests unitaires fonctionnels")
abstract class AbstractSimulateurImpotTest {

    private static long startSuite;
    private long startTest;

    protected ICalculateurImpot calculateur;

    protected abstract ICalculateurImpot creerCalculateur();

    @BeforeAll
    static void initSuite() {
        startSuite = System.currentTimeMillis();
    }

    @AfterAll
    static void tearDownSuite(TestInfo info) {
        long duree = System.currentTimeMillis() - startSuite;
        System.out.println("Durée suite " + info.getTestClass().map(Class::getSimpleName).orElse("?")
                + " : " + duree + " ms");
    }

    @BeforeEach
    void initTest() {
        startTest = System.currentTimeMillis();
        calculateur = creerCalculateur();
    }

    @AfterEach
    void tearDownTest(TestInfo info) {
        long duree = System.currentTimeMillis() - startTest;
        System.out.println("  [" + info.getDisplayName() + "] " + duree + " ms");
    }

    private int calculerImpot(int rev1, int rev2, SituationFamiliale sf,
                               int nbEnf, int nbEnfSH, boolean pi) {
        calculateur.setRevenusNetDeclarant1(rev1);
        calculateur.setRevenusNetDeclarant2(rev2);
        calculateur.setSituationFamiliale(sf);
        calculateur.setNbEnfantsACharge(nbEnf);
        calculateur.setNbEnfantsSituationHandicap(nbEnfSH);
        calculateur.setParentIsole(pi);
        calculateur.calculImpotSurRevenuNet();
        return calculateur.getImpotSurRevenuNet();
    }

    // ── Tests nominaux paramétrés (fichier CSV) — EXG_IMPOT_01 à 07 ────────

    @Tag("Nominal")
    @DisplayName("Tests nominaux depuis fichier CSV")
    @ParameterizedTest(name = "rev1={0} rev2={1} sit={2} enf={3} SH={4} pi={5} => {6}€")
    @CsvFileSource(resources = "/cas_impot.csv", numLinesToSkip = 1, delimiter = ',')
    void testNominauxCsv(int rev1, int rev2, SituationFamiliale sf,
                          int nbEnf, int nbEnfSH, boolean pi, int impotAttendu) {
        // EXG_IMPOT_01,02,03,04,05,06,07
        assertEquals(impotAttendu, calculerImpot(rev1, rev2, sf, nbEnf, nbEnfSH, pi));
    }

    // ── Tests valeurs limites abattement et tranches — EXG_IMPOT_02 et 04 ──

    @Tag("Nominal")
    @DisplayName("Tests valeurs limites abattement et tranches")
    @ParameterizedTest(name = "rev={0} => {1}€")
    @CsvSource({
        "4950,0",
        "20000,199",
        "12549,0",
        "30000,1637",
        "32997,2195",
        "60000,9486",
    })
    void testLimitesTranchesCelibataire(int revenu, int impotAttendu) {
        // EXG_IMPOT_02 + EXG_IMPOT_04
        assertEquals(impotAttendu,
                calculerImpot(revenu, 0, SituationFamiliale.CELIBATAIRE, 0, 0, false));
    }

    // ── Tests résultats intermédiaires ──────────────────────────────────────

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_02 : abattement 10% entre min et max pour célibataire 30000€")
    void testAbattementNominal() {
        // Arrange
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        // Act
        calculateur.calculImpotSurRevenuNet();
        // Assert
        assertEquals(3000, calculateur.getAbattement());
        assertEquals(27000, calculateur.getRevenuFiscalReference());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_02 : abattement plafonné à 14171 pour revenu élevé (200000€)")
    void testAbattementMax() {
        // EXG_IMPOT_02
        calculateur.setRevenusNetDeclarant1(200000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(14171, calculateur.getAbattement());
        assertEquals(185829, calculateur.getRevenuFiscalReference());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_02 : abattement minimum 495€ pour revenu très faible (1000€)")
    void testAbattementMin() {
        // EXG_IMPOT_02
        calculateur.setRevenusNetDeclarant1(1000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(495, calculateur.getAbattement());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_02 : abattement séparé pour couple marié (50000+0)")
    void testAbattementCoupleMarie() {
        // EXG_IMPOT_02 : abattement déclarant 2 avec revenu 0 = 495 (minimum)
        calculateur.setRevenusNetDeclarant1(50000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.MARIE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(5495, calculateur.getAbattement());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : célibataire = 1 part")
    void testPartsCelibataire() {
        // EXG_IMPOT_03
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(1.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : marié sans enfants = 2 parts")
    void testPartsMarieSansEnfants() {
        // EXG_IMPOT_03
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(30000);
        calculateur.setSituationFamiliale(SituationFamiliale.MARIE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(2.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : marié 2 enfants = 3 parts")
    void testPartsMarieDeuxEnfants() {
        // EXG_IMPOT_03 : 2 parts déclarants + 2 * 0.5 = 3
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(30000);
        calculateur.setSituationFamiliale(SituationFamiliale.MARIE);
        calculateur.setNbEnfantsACharge(2);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(3.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : marié 3 enfants = 4 parts (1 entière pour le 3e)")
    void testPartsMarieTroisEnfants() {
        // EXG_IMPOT_03 : 2 + 0.5 + 0.5 + 1 = 4
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(30000);
        calculateur.setSituationFamiliale(SituationFamiliale.MARIE);
        calculateur.setNbEnfantsACharge(3);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(4.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : enfant handicapé ajoute 0.5 part supplémentaire")
    void testPartsEnfantHandicap() {
        // EXG_IMPOT_03 : 1 + 0.5 (enfant) + 0.5 (handicap) = 2
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(1);
        calculateur.setNbEnfantsSituationHandicap(1);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(2.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : parent isolé avec 1 enfant ajoute 0.5 part")
    void testPartsParentIsole() {
        // EXG_IMPOT_03 : 1 + 0.5 (enfant) + 0.5 (parent isolé) = 2
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.DIVORCE);
        calculateur.setNbEnfantsACharge(1);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(true);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(2.0, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_03 : veuf avec enfants conserve 2 parts déclarants")
    void testPartsVeufAvecEnfants() {
        // EXG_IMPOT_03 : veuf + 1 enfant → 2 (déclarants) + 0.5 = 2.5
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.VEUF);
        calculateur.setNbEnfantsACharge(1);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(2.5, calculateur.getNbPartsFoyerFiscal());
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_06 : décote appliquée pour célibataire avec impôt modeste")
    void testDecoteCelibataire() {
        // EXG_IMPOT_06 : impôt avant décote < 1929 → décote positive
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertTrue(calculateur.getDecote() > 0,
                "La décote doit être positive pour un revenu modeste");
    }

    @Tag("Nominal")
    @Test
    @DisplayName("EXG_IMPOT_06 : pas de décote pour revenu élevé (célibataire 90000€)")
    void testPasDeDecoteRevenuEleve() {
        // EXG_IMPOT_06 : impôt > 1929 → décote = 0
        calculateur.setRevenusNetDeclarant1(90000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        calculateur.calculImpotSurRevenuNet();
        assertEquals(0, calculateur.getDecote(),
                "Pas de décote pour un revenu élevé");
    }

    // ── Tests négatifs — EXG_IMPOT préconditions ────────────────────────────

    @Tag("Negatif")
    @Test
    @DisplayName("Revenu déclarant 1 négatif → IllegalArgumentException")
    void testRevenuNegatif() {
        // EXG_IMPOT préconditions
        calculateur.setRevenusNetDeclarant1(-1);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }

    @Tag("Negatif")
    @Test
    @DisplayName("Nombre d'enfants négatif → IllegalArgumentException")
    void testNbEnfantsNegatif() {
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(-1);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }

    @Tag("Negatif")
    @Test
    @DisplayName("Nombre d'enfants > 7 → IllegalArgumentException")
    void testNbEnfantsTropEleve() {
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(8);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }

    @Tag("Negatif")
    @Test
    @DisplayName("Nombre d'enfants SH négatif → IllegalArgumentException")
    void testNbEnfantsSHNegatif() {
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(2);
        calculateur.setNbEnfantsSituationHandicap(-1);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }

    @Tag("Negatif")
    @Test
    @DisplayName("Nombre d'enfants SH > nb enfants → IllegalArgumentException")
    void testNbEnfantsSHSuperieurNbEnfants() {
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
        calculateur.setNbEnfantsACharge(1);
        calculateur.setNbEnfantsSituationHandicap(2);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }

    @Tag("Negatif")
    @Test
    @DisplayName("Situation familiale nulle → IllegalArgumentException")
    void testSituationFamilialeNulle() {
        calculateur.setRevenusNetDeclarant1(30000);
        calculateur.setRevenusNetDeclarant2(0);
        calculateur.setSituationFamiliale(null);
        calculateur.setNbEnfantsACharge(0);
        calculateur.setNbEnfantsSituationHandicap(0);
        calculateur.setParentIsole(false);
        assertThrows(IllegalArgumentException.class,
                () -> calculateur.calculImpotSurRevenuNet());
    }
}
