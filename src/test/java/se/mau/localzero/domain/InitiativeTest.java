package se.mau.localzero.domain;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class InitiativeTest {

    @Test
    void testIsOfficialDefaultValue() {
        Initiative initiative = new Initiative();
        assertFalse(initiative.isOfficial(), "isOfficial should default to false");
    }

    @Test
    void testSetIsOfficial() {
        Initiative initiative = new Initiative();
        initiative.setOfficial(true);
        assertTrue(initiative.isOfficial(), "isOfficial should be true after setting it to true");
    }
}
