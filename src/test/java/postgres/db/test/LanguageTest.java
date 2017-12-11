package postgres.db.test;

import org.junit.Test;

import static org.junit.Assert.*;

import postgresql.db.models.Language;


public class LanguageTest {
    @Test
    public void LanguageTest() throws Exception {
        Language language = new Language("Java");
        assertEquals(language.getName(), "Java");
        assertEquals(language.toString(), "Java");
    }
}