package com.google.zetasql.toolkit.antipattern.parser;

import static org.junit.Assert.assertEquals;

import com.google.zetasql.LanguageOptions;
import com.google.zetasql.Parser;
import com.google.zetasql.parser.ASTNodes;
import com.google.zetasql.toolkit.antipattern.parser.visitors.IdentifyDeleteTrueVisitor;
import org.junit.Before;
import org.junit.Test;

public class IdentifyDeleteTrueTest {

    LanguageOptions languageOptions;

    @Before
    public void setUp() {
        languageOptions = new LanguageOptions();
        languageOptions.enableMaximumLanguageFeatures();
        languageOptions.setSupportsAllStatementKinds();
    }

    @Test
    public void DeleteTrueStatementTest() {
        String expected =
                "Using a DELETE with True predicate at line 1. Convert this to a TRUNCATE statement for better performance.";
        String query = "DELETE FROM table WHERE true";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void Delete1Equals1StatementTest() {
        String expected =
                "Using a DELETE with True predicate at line 1. Convert this to a TRUNCATE statement for better performance.";
        String query = "DELETE FROM table WHERE 1=1";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void Delete1Equals1StringsStatementTest() {
        String expected =
                "Using a DELETE with True predicate at line 1. Convert this to a TRUNCATE statement for better performance.";
        String query = "DELETE FROM table WHERE '1'='1'";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void DeleteCorrectPredicateStatementTest() {
        String expected = "";
        String query = "DELETE FROM table WHERE col1='SomeVal'";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void DeleteTrueORSomethingStatementTest() {
        String expected =
                "Using a DELETE with True predicate at line 1. Convert this to a TRUNCATE statement for better performance.";
        String query = "DELETE FROM table WHERE col1='someVal' OR TRUE";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }

    @Test
    public void DeleteTrueMultipleORSomethingStatementTest() {
        String expected =
                "Using a DELETE with True predicate at line 1. Convert this to a TRUNCATE statement for better performance.";
        String query = "DELETE FROM table WHERE col1='someVal' OR col2='someOtherVal' OR TRUE";
        ASTNodes.ASTStatement parsedQuery = Parser.parseStatement(query, languageOptions);
        IdentifyDeleteTrueVisitor visitor = new IdentifyDeleteTrueVisitor(query);
        parsedQuery.accept(visitor);
        String recommendation = visitor.getResult();
        assertEquals(expected, recommendation);
    }
}