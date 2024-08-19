package com.google.zetasql.toolkit.antipattern.analyzer.visitors;

import com.google.cloud.bigquery.StandardTableDefinition;
import com.google.cloud.bigquery.Table;
import com.google.zetasql.resolvedast.ResolvedNodes;
import com.google.zetasql.toolkit.antipattern.AntiPatternVisitor;
import com.google.zetasql.toolkit.catalog.bigquery.BigQueryService;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class MergePartitionFilterPresentVisitor extends ResolvedNodes.Visitor
        implements AntiPatternVisitor {

    private static final String NAME = "MergePartitionFilterPresent";
    private final String RECOMMENDATION =
            "MERGE on partitioned table %s will perform better if filtered by %s partition field.";
    private BigQueryService service;
    private ArrayList<String> result = new ArrayList<String>();

    public MergePartitionFilterPresentVisitor(BigQueryService service) {
        this.service = service;
    }

    public String getResult() {
        return result.stream().distinct().collect(Collectors.joining("\n"));
    }

    public String getName() {
        return NAME;
    }

    private String partitionColumn = "";
    private Boolean inMergeExp = false;
    private Boolean partitionColumnFound = false;

    public void visit(ResolvedNodes.ResolvedMergeStmt mergeStmt) {
        ResolvedNodes.ResolvedTableScan targetTableScan = mergeStmt.getTableScan();
        this.partitionColumn = getPartitionColumns(targetTableScan);
        if (this.partitionColumn != null) {
            this.inMergeExp = true;
            mergeStmt.getMergeExpr().accept(this);
        }
        this.inMergeExp = false;
        if (!partitionColumnFound && this.partitionColumn != null) {
            String targetTableName = targetTableScan.getTable().getFullName();
            String resultAdded =
                    String.format(this.RECOMMENDATION, targetTableName, this.partitionColumn);
            this.result.add(resultAdded);
        }
    }

    public void visit(ResolvedNodes.ResolvedColumnRef colRef) {
        if (this.inMergeExp) {
            String col = colRef.getColumn().getTableName() + "." + colRef.getColumn().getName();
            this.partitionColumnFound = col.equals(this.partitionColumn);
        }
    }

    private String getPartitionColumns(ResolvedNodes.ResolvedTableScan table) {
    /*
    This is a helper function to get the partitions out of a table scan.
     */
        String[] tableName = table.getTable().getFullName().split("\\.");

        Table tableRef = this.service.fetchTable(tableName[0], tableName[1] + "." + tableName[2]).get();
        return downcastTableDefinition(tableRef);
    }

    private String downcastTableDefinition(Table table) {
    /*
    Method to downcast to get the partition. Returns the tableId + partition scheme as string.
     */
        if (table.getDefinition() instanceof StandardTableDefinition) {
            StandardTableDefinition def = (StandardTableDefinition) table.getDefinition();
            if (def.getTimePartitioning() != null || def.getRangePartitioning() != null) {
                return table.getTableId().getProject()
                        + "."
                        + table.getTableId().getDataset()
                        + "."
                        + table.getTableId().getTable()
                        + "."
                        + def.getTimePartitioning().getField();
            }
        }
        return null;
    }
}