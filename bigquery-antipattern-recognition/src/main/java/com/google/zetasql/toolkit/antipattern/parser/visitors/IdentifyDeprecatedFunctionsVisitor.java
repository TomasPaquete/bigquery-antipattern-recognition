package com.google.zetasql.toolkit.antipattern.parser.visitors;

import com.google.common.collect.ImmutableList;
import com.google.zetasql.parser.ASTNodes;
import com.google.zetasql.parser.ParseTreeVisitor;
import com.google.zetasql.toolkit.antipattern.AntiPatternVisitor;
import com.google.zetasql.toolkit.antipattern.util.ZetaSQLStringParsingHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class IdentifyDeprecatedFunctionsVisitor extends ParseTreeVisitor
        implements AntiPatternVisitor {
    public static final String NAME = "DeprecatedFunctions";
    private static final String SUGGESTION_STR =
            "%s at line %d. Avoid using deprecated/legacy functions.";
    private static final ArrayList<String> DEPRECATED_FUNCTIONS =
            new ArrayList<String>(
                    Arrays.asList(
                            "JSON_EXTRACT",
                            "JSON_EXTRACT_SCALAR",
                            "JSON_EXTRACT_ARRAY",
                            "JSON_EXTRACT_STRING_ARRAY"));
    private ArrayList<String> result = new ArrayList<String>();
    private String query;

    @Override
    public void visit(ASTNodes.ASTFunctionCall node) {
        ImmutableList<ASTNodes.ASTIdentifier> identifiers = node.getFunction().getNames();

        for (ASTNodes.ASTIdentifier identifier : identifiers) {
            String functionCalled = identifier.getIdString();
            if (IdentifyDeprecatedFunctionsVisitor.DEPRECATED_FUNCTIONS.contains(functionCalled)) {
                int lineNum =
                        ZetaSQLStringParsingHelper.countLine(query, identifier.getParseLocationRange().start());
                String resultToAdd = String.format(SUGGESTION_STR, functionCalled, lineNum);
                result.add(resultToAdd);
            }
        }
    }

    public String getResult() {
        return result.stream().distinct().collect(Collectors.joining("\n"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    public IdentifyDeprecatedFunctionsVisitor(String query) {
        this.query = query;
    }
}