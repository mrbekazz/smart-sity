Smart City Infrastructure and Route Planning
Assignment 3 · Graph Algorithms · Java

A graph-based study of city infrastructure: building a low-cost road network, analysing directed connectivity, detecting cycles, and finding shortest routes.

Table of Contents
- 1. Overview
- 2. Problem Statement
- 3. Implementation
- 4. Complexity Analysis
- 5. Experimental Setup
- 6. Results
- 7. Comparison and Discussion
- 8. Testing
- 9. Conclusion
- 10. How to Run
1. Overview
This project models a city as a weighted graph. Vertices represent districts or locations, while edges represent roads or other connections. Edge weights can represent distance, cost, or another non-negative measure.

The goal is to implement and evaluate core graph algorithms that can support infrastructure planning and route analysis. The project includes Disjoint Set Union (DSU), Kruskal's and Prim's minimum spanning tree algorithms, topological ordering with cycle detection, strongly connected components (SCC), and Dijkstra's shortest-path algorithm.

Technology: Java, standard library only. No external libraries are required.
2. Problem Statement
A smart-city planning system needs to answer several different questions:
1. Network construction: How can all districts be connected with the lowest total connection cost?
2. Connectivity: Which locations belong to the same strongly connected group in a directed network?
3. Dependency/order analysis: Is a directed network acyclic, and if so, what valid topological order can be used?
4. Route planning: What is the shortest route from a selected source to each reachable location?
5. Scalability: How does the execution time of these algorithms change as graph size increases?
The implementation addresses these questions using standard graph representations and algorithmic techniques. The benchmark compares results on small, medium, and large generated graphs.
3. Implementation
The graph is represented with an adjacency list. The implementation also keeps an edge list for algorithms that need to process all edges. Vertices are numbered from 0 to V - 1.
Algorithms
Component	Approach	Purpose
DSU	Path compression + union by size	Maintains disjoint sets efficiently and tracks connected components.
Kruskal	Sort edges by weight + DSU	Builds a minimum spanning tree (MST) or minimum spanning forest.
Prim	Priority queue	Expands a minimum spanning tree by repeatedly selecting a low-weight candidate edge.
Topological ordering	Kahn's algorithm	Produces a valid order for a directed acyclic graph (DAG) and detects cycles.
SCC	Kosaraju's algorithm	Finds strongly connected components in a directed graph.
Dijkstra	Priority queue	Calculates shortest paths from a source when all edge weights are non-negative.


Project structure
assignment-3/
├── src/
│   ├── app/
│   │   ├── Main.java       # Demonstration program
│   │   └── Bench.java      # Benchmark generation and measurements
│   ├── connectivity/
│   │   └── Conn.java       # Topological ordering and SCC
│   ├── dsu/
│   │   └── DSU.java        # Disjoint Set Union
│   ├── graph/
│   │   └── Graph.java      # Graph and edge representation
│   ├── mst/
│   │   └── MST.java        # Kruskal and Prim
│   ├── shortestpath/
│   │   └── SP.java         # Dijkstra shortest paths
│   └── test/
│       └── Test.java       # Correctness and edge-case tests
├── results/
│   ├── plots/              # SVG benchmark plots
│   ├── tables/             # CSV and Markdown benchmark results
│   └── test-results.txt
├── run.bat                 # Windows runner
├── run.sh                  # Linux/macOS runner
└── README.md                # Report and analysis
Important implementation details
- DSU uses path compression in find and union by size in union.
- Kruskal sorts edges by weight and uses DSU to avoid adding edges that create cycles.
- Prim uses a priority queue to choose candidate edges. Both MST algorithms are evaluated on connected, undirected graphs.
- Topological ordering returns an order only when all vertices can be processed. If a directed cycle exists, the result indicates that a valid topological order is not possible.
- Kosaraju's algorithm performs DFS passes on the graph and its reverse to identify SCCs.
- Dijkstra returns distances and reconstructed paths for reachable vertices. Unreachable vertices are marked accordingly. Negative edge weights are rejected because Dijkstra does not support them.
4. Complexity Analysis
Let V be the number of vertices and E the number of edges. The graph is stored as an adjacency list. The priority queues used by Prim and Dijkstra are Java PriorityQueue binary heaps.
Algorithm	Time complexity	Auxiliary/overall space	Explanation
DSU find / union	Amortized O(α(V)) per operation	O(V) for DSU	Path compression and union by size keep the set trees shallow. α(V) is the inverse Ackermann function.
Kruskal	O(E log E + E α(V))	O(V + E)	Sorting the edges is the dominant step; DSU operations are nearly constant amortized time.
Prim	O((V + E) log V) for a simple graph	O(V + E)	Priority-queue operations determine the logarithmic factor.
Kosaraju SCC	O(V + E)	O(V + E)	Each vertex and edge is processed a constant number of times, including construction of the reverse graph.
Topological ordering	O(V + E)	O(V) auxiliary, excluding graph storage	Computes indegrees and processes vertices and outgoing edges.
Dijkstra	O((V + E) log V) for a simple graph	O(V + E)	Uses a binary heap and assumes non-negative edge weights.


These are asymptotic bounds, not predictions of exact runtime. Actual measurements are affected by graph structure, JVM warm-up, hardware, and other processes running on the system.
5. Experimental Setup
The benchmark uses the three graph sizes specified for the assignment:
Dataset	Vertices (V)	Edges (E)
Small	10	30
Medium	100	500
Large	1,000	5,000


Reproducibility and measurement
- Random seed: 20261022.
- Repetitions: 5 timed runs per algorithm and graph configuration, after one warm-up run.
- Timer: System.nanoTime().
- Timing scope: Graph generation is outside the timed section.
- Recorded measurements: Average elapsed time in nanoseconds and operation counters such as edge scans, sorting comparisons, DSU calls, and priority-queue pushes/pops.
The benchmark creates graph types suited to each algorithm:
- MST: Connected undirected graphs. A spanning tree is generated first, then additional edges are added. Edge weights range from 1 to 100.
- SCC and cycle detection: Directed graphs containing a cycle, with additional edges.
- Topological ordering: Directed acyclic graphs, generated by adding edges only from a lower-numbered vertex to a higher-numbered vertex.
- Dijkstra: Directed graphs with non-negative edge weights.
A fixed seed makes the generated inputs repeatable. However, execution times can still vary across machines and JVM runs. The results below are the measurements stored in this repository's benchmark output.
6. Results
The following table reports the recorded average execution time for each algorithm. All values are in nanoseconds (ns) and are based on five timed repetitions.
Algorithm	Graph type	10 V / 30 E	100 V / 500 E	1,000 V / 5,000 E
Kruskal	Connected, undirected	27,294	1,155,385	4,381,407
Prim	Connected, undirected	99,959	756,488	7,404,272
SCC (Kosaraju)	Directed, with cycle	377,181	1,012,190	1,585,876
Topological ordering	Directed DAG	232,164	603,626	1,230,365
Topological cycle detection	Directed, with cycle	7,650	17,997	303,108
Dijkstra	Directed, non-negative weights	78,779	1,649,114	1,681,049


Execution time vs. number of vertices

Execution time vs. number of edges

Full measurement records, including operation counts, are available in [`benchmark.csv`](results/tables/benchmark.csv) and the formatted table in [`benchmark.md`](results/tables/benchmark.md).
Reading the results
- The MST algorithms show different timing patterns as the graph grows. In this run, Kruskal is faster than Prim on the largest graph, while Prim is faster on the medium graph.
- SCC and topological ordering show increasing execution time as the graph size grows, which is consistent with their expected O(V + E) complexity.
- The cycle-detection run is relatively quick on the smaller graphs because the algorithm can identify a cycle without producing a complete topological order. The exact time is implementation- and input-dependent.
- Dijkstra's recorded time does not increase uniformly across all three measurements. This illustrates why benchmark timings should be interpreted cautiously rather than treated as a perfectly smooth growth curve.
Limitation: In these configurations, the number of vertices and edges increases together. The plots therefore show how runtime changes across the selected graph sizes, but they do not isolate the independent effect of changing only V or only E. The timings are also machine-dependent and should not be treated as universal performance rankings.
7. Comparison and Discussion
Kruskal vs. Prim
Both algorithms solve the minimum spanning tree problem for connected, weighted, undirected graphs. Kruskal globally sorts the edge list and uses DSU to reject cycle-forming edges. Prim grows a tree from a starting vertex using a priority queue. Their practical performance depends on graph density, data structures, and implementation details. The measurements in this report do not establish that one algorithm is always faster than the other.
DSU optimisation
Path compression reduces the depth of paths followed by find, while union by size attaches the smaller set to the larger one. Together, these techniques make repeated connectivity checks efficient and support Kruskal's cycle prevention.
SCC and topological ordering
Kosaraju identifies groups in which every vertex is reachable from every other vertex in the same component. Topological ordering is defined only for DAGs. The implementation returns a valid order for acyclic input and reports cycle detection when not all vertices can be processed.
Dijkstra
Dijkstra calculates shortest distances from one source and reconstructs paths for reachable vertices. It is appropriate for non-negative weights, but not for graphs with negative edge weights. The tests cover unreachable vertices and the rejection of negative weights.

Overall, the implementation and measured operation counts provide more information than elapsed time alone. Timing results can fluctuate, while operation counters help explain what work an algorithm performed on a particular generated input.
8. Testing
The included test program reports 45/45 checks passed. Test coverage includes:
- DSU initial state, union operations, representative lookup, component count, and operation counters.
- Kruskal and Prim MST weights and consistency.
- Disconnected graphs, minimum spanning forests, a single-vertex graph, and cases with multiple valid minimum spanning trees or equal edge weights.
- Valid topological ordering for a DAG and cycle detection for cyclic input.
- SCC membership and assignment of every vertex to exactly one component.
- Dijkstra distances, path reconstruction, unreachable vertices, and negative-weight rejection.
- Required graph sizes, fixed-seed reproducibility, connected MST inputs, generated DAGs, and generated directed cycles.
The captured output is stored in [`results/test-results.txt`](results/test-results.txt). Run the tests again after modifying the source code.
9. Conclusion
This project implements the main graph algorithms required for the smart-city infrastructure scenario. Kruskal and Prim provide minimum spanning tree approaches, DSU supports efficient connectivity checks, Kosaraju identifies strongly connected components, Kahn's algorithm handles topological ordering and cycle detection, and Dijkstra calculates shortest paths for non-negative edge weights.

The benchmark demonstrates how runtime changes across three graph sizes and records operation counts for further comparison. The results are consistent with the general complexity expectations, while also showing that individual timing measurements can vary by algorithm, input, and environment. Correctness tests complement the performance measurements by checking normal cases and important edge cases.
10. How to Run
Install JDK 11 or newer and open a terminal in the project root.
Windows
run.bat
run.bat test
run.bat bench
Linux / macOS
./run.sh demo
./run.sh test
./run.sh bench
The demo command runs example inputs, test runs the correctness checks, and bench regenerates the benchmark tables and plots. The project uses only the Java standard library.
