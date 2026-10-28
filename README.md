Smart City Infrastructure and Route Planning
1. Overview
This project represents a city as a weighted graph. Vertices stand for districts, and edges represent roads or other connections. The weight of an edge can represent distance or construction cost.

The program includes algorithms for building a low-cost network, checking directed connections, and finding shortest paths. It is written in Java and can be run from the command line.

2. Implementation
The graph is stored as an adjacency list. Each vertex has a list of its outgoing or neighbouring edges, and the graph also stores its edge list for the MST algorithms. Vertices are numbered from 0 to V - 1.

The source files are organised as follows:

src/
├── graph/Graph.java
├── dsu/DSU.java
├── mst/MST.java
├── connectivity/Conn.java
├── shortestpath/SP.java
├── app/Main.java
├── app/Bench.java
└── test/Test.java
The main algorithms are:

DSU: find uses path compression, and union uses union by size.
Kruskal: sorts the edges by weight and uses DSU to avoid adding a cycle to the spanning tree.
Prim: uses a priority queue to choose the next low-weight edge.
Topological ordering: uses Kahn's algorithm. If a cycle prevents all vertices from being ordered, the program reports that no valid order exists.
SCC: uses Kosaraju's algorithm to find strongly connected components in a directed graph.
Dijkstra: uses a priority queue to find shortest paths from a starting vertex. It reports the distance and path for each reachable vertex and does not accept negative edge weights.
Main prints an example of the algorithms. Test checks the main expected results and edge cases. Bench creates graphs, measures the algorithms, and saves the results in the results directory.

3. Complexity Analysis
Here, V is the number of vertices and E is the number of edges. The graph uses an adjacency list. Prim and Dijkstra use Java's PriorityQueue, which is implemented with a binary heap.

Algorithm	Time complexity	Space complexity	Notes
DSU find	Amortized O(α(V))	O(V) for the DSU structure	Uses path compression and union by size.
DSU union	Amortized O(α(V))	O(V) for the DSU structure	Calls find and joins the smaller set to the larger one.
Kruskal	O(E log E + E α(V))	O(V + E)	Sorting the edges is the main cost.
Prim	O((V + E) log V) for a simple graph	O(V + E)	The priority queue stores candidate edges.
Kosaraju SCC	O(V + E)	O(V + E)	Uses the reversed directed graph and DFS passes.
Topological ordering	O(V + E)	O(V) additional space	Each vertex and edge is processed a constant number of times.
Dijkstra	O((V + E) log V) for a simple graph	O(V + E)	Assumes non-negative weights and a binary-heap priority queue.
The number of vertices and edges affects each algorithm differently. Kruskal has to sort the edges. Prim and Dijkstra manage a priority queue. SCC and topological ordering are linear in V + E. The DSU optimisations keep its operations very efficient over a sequence of operations. Actual running time also depends on the computer and the Java runtime.

4. Experimental Setup
The benchmark uses the three graph sizes required by the assignment.

Graph size	Vertices (V)	Edges (E)
Small	10	30
Medium	100	500
Large	1,000	5,000
The random seed is fixed at 20261022, so the generated graphs can be reproduced with the same code and Java environment.

MST graphs: The generator creates a spanning tree first, so the undirected graph is connected. It then adds edges until it reaches the requested edge count. The weights are between 1 and 100.
Directed graphs for SCC and cycle detection: The generator first adds a directed cycle, then adds more edges. These graphs therefore contain a cycle.
DAGs: Edges are only added from a lower-numbered vertex to a higher-numbered vertex. This prevents directed cycles.
Each algorithm is run once as a warm-up and then five times for timing. The program uses System.nanoTime() and records the average of the five runs. Graph generation is outside the timed part. The benchmark also records operation counts, including edge scans, sorting comparisons, DSU calls, and priority-queue pushes and pops.

The full results are in results/tables/benchmark.csv and results/tables/benchmark.md. The CSV file can be opened in a spreadsheet program.

5. Results
The table below shows the average times from the current benchmark run. Times are in nanoseconds.

Algorithm	Graph used	10 vertices, 30 edges	100 vertices, 500 edges	1,000 vertices, 5,000 edges
Kruskal	Connected, undirected	46,151	458,784	2,953,618
Prim	Connected, undirected	159,175	471,258	5,574,853
SCC (Kosaraju)	Directed graph with a cycle	88,856	294,649	1,102,621
Topological ordering	DAG	38,696	189,211	915,620
Topological cycle detection	Directed graph with a cycle	4,919	30,151	122,804
Dijkstra	Directed graph with non-negative weights	46,045	245,391	3,937,521
Execution time vs. number of vertices
Execution time vs. number of vertices

Execution time vs. number of edges
Execution time vs. number of edges

The recorded times are from one run and will differ between computers. Run the benchmark again to get measurements for your own environment. Running it updates the CSV, Markdown table, and plots.

6. Comparison and Discussion
Kruskal and Prim. Both algorithms returned the same MST weight for the connected undirected graphs. Kruskal was faster in this run, especially for the largest graph. This is an observation from these measurements, not a rule that Kruskal must always be faster. The algorithms use different ways to choose the next edge, so the graph and implementation can affect the result.

DSU. Path compression shortens the paths followed by find. Union by size keeps the trees from becoming unnecessarily tall. Both optimisations help Kruskal check whether the endpoints of an edge are already connected.

SCC and topological ordering. SCC execution time increased as the graph got larger, which is consistent with the expected O(V + E) complexity. Topological ordering completed for the generated DAGs and detected cycles in the cyclic graphs. Cycle detection can finish before processing every vertex, because the remaining vertices still have incoming edges when a cycle exists.

Dijkstra. The algorithm found shortest paths for reachable vertices and marked the remaining vertices as unreachable. It is suitable for non-negative edge weights; it is not suitable for negative edge weights because a distance that was already treated as final could later be reduced by a path containing a negative edge.

In these configurations, the number of vertices and edges increases together. The plots show the requested comparisons, but they do not measure the separate effect of changing only V or only E.

7. Testing
The test program currently passes 45 of 45 checks. It covers:

DSU operations and component count.
Kruskal and Prim returning the same MST weight.
Disconnected graphs, multiple valid MSTs, duplicate weights, and a single vertex.
Valid topological ordering and cycle detection.
SCC membership, with every vertex assigned to exactly one component.
Dijkstra distances, reconstructed paths, unreachable vertices, and rejection of negative weights.
Reproducible graph generation and the required graph sizes.
The test output is saved in results/test-results.txt. Tests should be run again after any changes to the source code.

8. Conclusion
The project implements the graph algorithms required by the assignment and includes correctness tests and performance measurements. Kruskal and Prim find a minimum spanning tree for connected undirected graphs. Kosaraju finds strongly connected components, topological ordering works on DAGs, and Dijkstra finds shortest paths when edge weights are non-negative.

The benchmark shows how execution time changes as the graphs grow. The results also show why it is useful to check both the output and the operation counts instead of comparing algorithms by timing alone.

How to Run
You need JDK 11 or newer.

On Windows, open Command Prompt in the project folder and run:

run.bat
run.bat test
run.bat bench
On Linux or macOS, run:

./run.sh demo
./run.sh test
./run.sh bench
The first command runs the example. The second runs the tests. The third runs the benchmark and regenerates the result tables and plots. No external Java libraries are required.
