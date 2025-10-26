package com.example.mst;

import com.example.mst.algorithms.KruskalAlgorithm;
import com.example.mst.algorithms.PrimAlgorithm;
import com.example.mst.io.JsonReader;
import com.example.mst.io.JsonWriter;
import com.example.mst.io.JsonWriter.ResultData;
import com.example.mst.model.Graph;
import com.example.mst.visualization.GraphVisualizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class to read graphs from JSON, execute both MST algorithms,
 * write results to JSON and CSV files, and generate visualizations.
 *
 * BONUS SECTION: Demonstrates proper OOP design with Graph and Edge classes,
 * and includes visualization capabilities.
 */
public class Main {

    public static void main(String[] args) {
        // Default file paths
        String inputFile = "src/main/resources/input/assign_3_input.json";
        String outputJsonFile = "src/main/resources/output/results.json";
        String outputCsvFile = "src/main/resources/output/summary.csv";
        String visualizationDir = "src/main/resources/output/visualizations/";

        // Enable/disable visualization
        boolean enableVisualization = true;

        // Allow custom input file via command line argument
        if (args.length >= 1) {
            inputFile = args[0];
        }
        if (args.length >= 2) {
            outputJsonFile = args[1];
        }
        if (args.length >= 3) {
            outputCsvFile = args[2];
        }
        if (args.length >= 4) {
            enableVisualization = Boolean.parseBoolean(args[3]);
        }

        try {
            printHeader();

            System.out.println("Reading graphs from: " + inputFile);

            // Read graphs from JSON file
            JsonReader reader = new JsonReader();
            List<Graph> graphs = reader.readGraphs(inputFile);

            System.out.println("✓ Successfully loaded " + graphs.size() + " graph(s)\n");

            List<ResultData> results = new ArrayList<>();
            GraphVisualizer visualizer = new GraphVisualizer();

            // Process each graph
            for (Graph graph : graphs) {
                printSeparator();
                System.out.println(" Processing " + graph);
                System.out.println();

                // Print detailed graph information
                printGraphInfo(graph);

                // Check if graph is connected
                if (!graph.isConnected()) {
                    System.out.println("  WARNING: Graph is disconnected!");
                    System.out.println("   MST may not span all vertices.\n");
                }

                // Visualize original graph
                if (enableVisualization) {
                    try {
                        String graphImagePath = visualizationDir + "graph_" + graph.getId() + "_original.png";
                        visualizer.visualizeGraph(graph, graphImagePath);
                        System.out.println("  Graph visualization saved: " + graphImagePath);
                    } catch (IOException e) {
                        System.err.println("  Could not save graph visualization: " + e.getMessage());
                    }
                }

                // Execute Prim's algorithm
                System.out.println("\n Running Prim's Algorithm...");
                PrimAlgorithm prim = new PrimAlgorithm();
                prim.execute(graph);
                prim.printResult();

                // Visualize Prim's MST
                if (enableVisualization) {
                    try {
                        String primImagePath = visualizationDir + "graph_" + graph.getId() + "_prim.png";
                        visualizer.visualizeMST(graph, prim.getMstEdges(),
                                "Prim's Algorithm", prim.getTotalCost(), primImagePath);
                        System.out.println("  Prim's MST visualization saved: " + primImagePath);
                    } catch (IOException e) {
                        System.err.println("⚠  Could not save Prim's visualization: " + e.getMessage());
                    }
                }

                // Execute Kruskal's algorithm
                System.out.println("\nRunning Kruskal's Algorithm...");
                KruskalAlgorithm kruskal = new KruskalAlgorithm();
                kruskal.execute(graph);
                kruskal.printResult();

                // Visualize Kruskal's MST
                if (enableVisualization) {
                    try {
                        String kruskalImagePath = visualizationDir + "graph_" + graph.getId() + "_kruskal.png";
                        visualizer.visualizeMST(graph, kruskal.getMstEdges(),
                                "Kruskal's Algorithm", kruskal.getTotalCost(), kruskalImagePath);
                        System.out.println("  Kruskal's MST visualization saved: " + kruskalImagePath);
                    } catch (IOException e) {
                        System.err.println("  Could not save Kruskal's visualization: " + e.getMessage());
                    }
                }

                // Create comparison visualization
                if (enableVisualization) {
                    try {
                        String comparisonPath = visualizationDir + "graph_" + graph.getId() + "_comparison.png";
                        visualizer.visualizeComparison(graph,
                                prim.getMstEdges(), kruskal.getMstEdges(),
                                prim.getTotalCost(), kruskal.getTotalCost(),
                                comparisonPath);
                        System.out.println("  Comparison visualization saved: " + comparisonPath);
                    } catch (IOException e) {
                        System.err.println("  Could not save comparison visualization: " + e.getMessage());
                    }
                }

                // Compare results
                System.out.println("\n Algorithm Comparison:");
                printComparison(prim, kruskal);

                // Collect results for output
                ResultData result = new ResultData(graph, prim, kruskal);
                results.add(result);

                System.out.println();
            }

            // Write results to files
            printSeparator();
            System.out.println(" Saving results...\n");

            JsonWriter writer = new JsonWriter();

            writer.writeResults(results, outputJsonFile);
            System.out.println("✓ JSON results written to: " + outputJsonFile);

            writer.writeCsvSummary(results, outputCsvFile);
            System.out.println("✓ CSV summary written to: " + outputCsvFile);

            if (enableVisualization) {
                System.out.println("✓ Visualizations saved to: " + visualizationDir);
            }

            printFooter();

        } catch (IOException e) {
            System.err.println("\n ERROR: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Print detailed graph information.
     */
    private static void printGraphInfo(Graph graph) {
        System.out.println("Graph Information:");
        System.out.println("  ID: " + graph.getId());
        System.out.println("  Vertices: " + graph.getNodeCount());
        System.out.println("  Edges: " + graph.getEdgeCount());
        System.out.println("  Connected: " + (graph.isConnected() ? "✓" : "✗"));
        System.out.println("  Density: " + String.format("%.2f", graph.getDensity()));
        System.out.println("  Total Weight: " + graph.getTotalWeight());
        System.out.println("  Weight Range: [" + graph.getMinEdgeWeight() +
                ", " + graph.getMaxEdgeWeight() + "]");
    }

    /**
     * Print comparison between Prim's and Kruskal's results.
     */
    private static void printComparison(PrimAlgorithm prim, KruskalAlgorithm kruskal) {
        boolean sameCost = prim.getTotalCost() == kruskal.getTotalCost();

        System.out.println("  ┌─────────────────────┬─────────────┬─────────────┐");
        System.out.println("  │ Metric              │ Prim's      │ Kruskal's   │");
        System.out.println("  ├─────────────────────┼─────────────┼─────────────┤");
        System.out.printf("  │ Total Cost          │ %-11d │ %-11d │%n",
                prim.getTotalCost(), kruskal.getTotalCost());
        System.out.printf("  │ Execution Time (ms) │ %-11.2f │ %-11.2f │%n",
                prim.getExecutionTimeMs(), kruskal.getExecutionTimeMs());
        System.out.printf("  │ Operations Count    │ %-11d │ %-11d │%n",
                prim.getOperationsCount(), kruskal.getOperationsCount());
        System.out.printf("  │ MST Edges           │ %-11d │ %-11d │%n",
                prim.getMstEdges().size(), kruskal.getMstEdges().size());
        System.out.println("  └─────────────────────┴─────────────┴─────────────┘");

        System.out.println("\n  Result: " + (sameCost ? "✓ Same total cost" : "✗ Different costs (ERROR!)"));

        // Determine faster algorithm
        if (prim.getExecutionTimeMs() < kruskal.getExecutionTimeMs()) {
            double speedup = kruskal.getExecutionTimeMs() / prim.getExecutionTimeMs();
            System.out.printf("  ⚡ Prim's was %.2fx faster%n", speedup);
        } else if (kruskal.getExecutionTimeMs() < prim.getExecutionTimeMs()) {
            double speedup = prim.getExecutionTimeMs() / kruskal.getExecutionTimeMs();
            System.out.printf("  ⚡ Kruskal's was %.2fx faster%n", speedup);
        } else {
            System.out.println("  ⚡ Both algorithms had equal execution time");
        }

        // Determine more efficient algorithm (fewer operations)
        if (prim.getOperationsCount() < kruskal.getOperationsCount()) {
            System.out.println("   Prim's performed fewer operations");
        } else if (kruskal.getOperationsCount() < prim.getOperationsCount()) {
            System.out.println("   Kruskal's performed fewer operations");
        } else {
            System.out.println("   Both algorithms performed equal operations");
        }
    }

    /**
     * Print decorative header.
     */
    private static void printHeader() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        MST Assignment - Prim's & Kruskal's Algorithms          ║");
        System.out.println("║              Minimum Spanning Tree Computation                 ║");
        System.out.println("║                    BONUS: Graph Visualization                  ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Print decorative footer.
     */
    private static void printFooter() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ✓ Processing Complete!                        ║");
        System.out.println("║                                                                  ║");
        System.out.println("║  All results have been saved successfully.                       ║");
        System.out.println("║  Check the output directory for JSON, CSV, and images.           ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
    }

    /**
     * Print separator line.
     */
    private static void printSeparator() {
        System.out.println("═".repeat(68));
    }
}