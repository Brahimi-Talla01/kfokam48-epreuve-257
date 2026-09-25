package epreuve.kfokam48.backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire sur une règle réelle — B6, issue #10.
 * Règle RG12 / Q13 tranchée au cahier des charges §7.2 :
 * « personne n'a commencé à le relire » signifie <b>aucune note rendue</b>, donc statut EN_ATTENTE.
 */
class RemplacementLienTest {

    private Exercice nouvelExercice() {
        Promotion promotion = new Promotion("Promotion test");
        LocalDateTime ouverture = LocalDateTime.now();
        Session session = new Session("Cours", promotion, "K7F2M48",
                ouverture, ouverture.plusMinutes(15));
        Etudiant etudiant = new Etudiant("Etudiant test", promotion);
        return new Exercice(session, etudiant, "https://example.com/devoir");
    }

    @Test
    @DisplayName("RG12 : le lien se remplace tant qu'aucune note n'est rendue")
    void modifiableTantQueEnAttente() {
        Exercice exercice = nouvelExercice();

        assertTrue(exercice.estModifiable(), "aucune relecture n'a été rendue");
        assertEquals(StatutExercice.EN_ATTENTE, exercice.getStatut());

        exercice.remplacerLien("https://example.com/devoir-corrige");
        assertEquals("https://example.com/devoir-corrige", exercice.getLien());
        assertEquals(StatutExercice.EN_ATTENTE, exercice.getStatut(),
                "remplacer un lien ne change pas le statut de la relecture");
    }

    @Test
    @DisplayName("Q13 : dès qu'une note est rendue, le lien n'est plus modifiable")
    void plusModifiableApresRendu() {
        Exercice exercice = nouvelExercice();
        exercice.marquerRendu();

        assertEquals(StatutExercice.RENDU, exercice.getStatut());
        assertFalse(exercice.estModifiable());
    }
}
