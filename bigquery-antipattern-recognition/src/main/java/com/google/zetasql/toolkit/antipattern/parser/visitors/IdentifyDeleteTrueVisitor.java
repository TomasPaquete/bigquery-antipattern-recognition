package com.google.zetasql.toolkit.antipattern.parser.visitors;

import com.google.zetasql.parser.ASTNodes;
import com.google.zetasql.parser.ParseTreeVisitor;
import com.google.zetasql.toolkit.antipattern.AntiPatternVisitor;
import com.google.zetasql.toolkit.antipattern.util.ZetaSQLStringParsingHelper;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class IdentifyDeleteTrueVisitor extends ParseTreeVisitor implements AntiPatternVisitor {
    public static final String NAME = "DeleteTrue";
    private String query;
    private final String DELETE_TRUE_SUGGESTION_MESSAGE =
            "Using a DELETE with True predicate at line %d. Convert this to a TRUNCATE statement for better performance.";
    private ArrayList<String> result = new ArrayList<String>();

    public IdentifyDeleteTrueVisitor(String query) {
        this.query = query;
    }

    @Override
    public void visit(ASTNodes.ASTDeleteStatement node) {
        node.getWhere().accept(this);
    }

    @Override
    public void visit(ASTNodes.ASTBooleanLiteral node) {
        if (this.detectTautology(node)) {
            int location =
                    ZetaSQLStringParsingHelper.countLine(query, node.getParseLocationRange().start());
            result.add(String.format(DELETE_TRUE_SUGGESTION_MESSAGE, location));
        }
    }

    @Override
    public void visit(ASTNodes.ASTBinaryExpression node) {
        if (this.detectTautology(node)) {
            int location =
                    ZetaSQLStringParsingHelper.countLine(query, node.getParseLocationRange().start());
            result.add(String.format(DELETE_TRUE_SUGGESTION_MESSAGE, location));
        }
    }

    @Override
    public void visit(ASTNodes.ASTOrExpr node) {
        for (ASTNodes.ASTExpression disjunct : node.getDisjuncts()) {
            disjunct.accept(this);
        }
    }

    public String getResult() {
        return result.stream().distinct().collect(Collectors.joining("\n"));
    }

    @Override
    public String getName() {
        return NAME;
    }

    private Boolean detectTautology(ASTNodes.ASTBinaryExpression node) {
        return node.getLhs().toString().equals(node.getRhs().toString());
    }

    private Boolean detectTautology(ASTNodes.ASTBooleanLiteral node) {
        return node.getValue();
    }
}