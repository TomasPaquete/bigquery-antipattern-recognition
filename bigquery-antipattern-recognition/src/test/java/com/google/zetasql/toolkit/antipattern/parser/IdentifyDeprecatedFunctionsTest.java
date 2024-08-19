package com.google.zetasql.toolkit.antipattern.parser;

import static org.junit.Assert.assertEquals;

import com.google.zetasql.LanguageOptions;
import com.google.zetasql.Parser;
import com.google.zetasql.parser.ASTNodes;
import com.google.zetasql.toolkit.antipattern.parser.visitors.IdentifyDeprecatedFunctionsVisitor;
import org.junit.Before;
import org.junit.Test;

public class IdentifyDeprecatedFunctionsTest {

    LanguageOptions languageOptions;

    @Before
    public void setUp() {
        languageOptions = new LanguageOptions();
        languageOptions.enableMaximumLanguageFeatures();
        languageOptions.setSupportsAllStatementKinds();
    }

    @Test
    public void deprecatedJSONFunctionTest() {
        String expected = "JSON_EXTRACT at line 1. Avoid using deprecated/legacy functions.";
        String query = "SELECT JSON_EXTRACT(dim1, '$') \n" + "FROM dataset.table";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeprecatedFunctionsVisitor visitor = new IdentifyDeprecatedFunctionsVisitor(query);
        parsedQuery.accept(visitor);
        String recommendations = visitor.getResult();
        assertEquals(expected, recommendations);
    }

    @Test
    public void otherFunctionsDeprecatedJSONFunctionTest() {
        String expected = "JSON_EXTRACT at line 1. Avoid using deprecated/legacy functions.";
        String query = "SELECT JSON_EXTRACT(dim1, '$') \n" + "FROM dataset.table";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeprecatedFunctionsVisitor visitor = new IdentifyDeprecatedFunctionsVisitor(query);
        parsedQuery.accept(visitor);
        String recommendations = visitor.getResult();
        assertEquals(expected, recommendations);
    }

    @Test
    public void multipleDeprecatedJSONFunctionTest() {
        String expected =
                "JSON_EXTRACT at line 1. Avoid using deprecated/legacy functions.\n"
                        + "JSON_EXTRACT at line 4. Avoid using deprecated/legacy functions.";
        String query =
                "SELECT JSON_EXTRACT(dim1, '$') \n"
                        + "FROM dataset.table \n"
                        + "WHERE date_dim = CURRENT_DATE() \n"
                        + "AND JSON_EXTRACT(dim2, '$') IS NOT NULL";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeprecatedFunctionsVisitor visitor = new IdentifyDeprecatedFunctionsVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void allDeprecatedJSONFunctionsTest() {
        String expected =
                "JSON_EXTRACT at line 1. Avoid using deprecated/legacy functions.\n"
                        + "JSON_EXTRACT_SCALAR at line 2. Avoid using deprecated/legacy functions.\n"
                        + "JSON_EXTRACT_ARRAY at line 3. Avoid using deprecated/legacy functions.\n"
                        + "JSON_EXTRACT_STRING_ARRAY at line 4. Avoid using deprecated/legacy functions.";
        String query =
                "SELECT JSON_EXTRACT(dim1, '$'), \n"
                        + "JSON_EXTRACT_SCALAR(dim1, '$'), \n"
                        + "JSON_EXTRACT_ARRAY(dim1, '$'), \n"
                        + "JSON_EXTRACT_STRING_ARRAY(dim1, '$') \n"
                        + "FROM dataset.table \n";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeprecatedFunctionsVisitor visitor = new IdentifyDeprecatedFunctionsVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }
}
