package test;

import app.Bench;
import connectivity.Conn;
import dsu.DSU;
import graph.Graph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mst.MST;
import shortestpath.SP;

public final class Test {
    private static int passed;

    private Test() {
    }

    public static void main(String[] args) {
        testDsu();
        testMst();
        testDisconnected();
        testSingleVertex();
        testTopo();
        testScc();
        testDijkstra();
        testNegativeWeight();
        testGeneratedGraphs();
        System.out.println("Tests passed: " + passed + "/" + passed);
    }

    private static void testDsu() {
        DSU d = new DSU(5);
        check(d.components() == 5, "DSU initial components");
        check(d.union(0, 1), "DSU first union");
        check(d.union(1, 2), "DSU second union");
        check(!d.union(0, 2), "DSU repeated union");
        check(d.find(0) == d.find(2), "DSU find representative");
        check(d.components() == 3, "DSU component count");
        check(d.findCount() > 0 && d.unionCount() == 3, "DSU operation counters");
    }

    private static void testMst() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 2, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(1, 3, 2);
        g.addEdge(2, 3, 2);
        g.addEdge(0, 3, 5);
        MST.Result k = MST.kruskal(g);
        MST.Result p = MST.prim(g);
        check(k.weight == 4, "Kruskal MST weight");
        check(p.weight == 4, "Prim MST weight");
        check(k.weight == p.weight, "Kruskal and Prim match");
        check(k.edges.size() == 3 && p.edges.size() == 3, "MST edge count");
        check(k.connected(g.size()) && p.connected(g.size()), "MST connected status");
    }

    private static void testDisconnected() {
        Graph g = new Graph(3, false);
        g.addEdge(0, 1, 4);
        MST.Result k = MST.kruskal(g);
        MST.Result p = MST.prim(g);
        check(!k.connected(g.size()), "Kruskal detects disconnected graph");
        check(!p.connected(g.size()), "Prim detects disconnected graph");
        check(k.components == 2 && p.components == 2, "Disconnected components count");
        check(k.weight == 4 && p.weight == 4, "Minimum spanning forest weight");
    }

    private static void testSingleVertex() {
        Graph u = new Graph(1, false);
        Graph d = new Graph(1, true);
        check(MST.kruskal(u).connected(1) && MST.kruskal(u).weight == 0, "Single-vertex Kruskal");
        check(MST.prim(u).connected(1) && MST.prim(u).weight == 0, "Single-vertex Prim");
        check(Conn.topo(d).acyclic && Conn.topo(d).order.equals(List.of(0)), "Single-vertex topology");
        check(Conn.scc(d).groups.size() == 1, "Single-vertex SCC");
        SP.Result r = SP.dijkstra(d, 0);
        check(r.dist[0] == 0 && r.pathTo(0).equals(List.of(0)), "Single-vertex Dijkstra");
    }

    private static void testTopo() {
        Graph dag = new Graph(4, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        Conn.TopoResult t = Conn.topo(dag);
        check(t.acyclic, "DAG accepted");
        check(validTopo(dag, t.order), "DAG order respects every edge");
        Graph cyc = new Graph(3, true);
        cyc.addEdge(0, 1, 1);
        cyc.addEdge(1, 2, 1);
        cyc.addEdge(2, 0, 1);
        check(!Conn.topo(cyc).acyclic, "Cycle rejected by topological ordering");
    }

    private static void testScc() {
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 3, 1);
        Conn.SCCResult r = Conn.scc(g);
        Set<Set<Integer>> got = new HashSet<>();
        for (List<Integer> group : r.groups) {
            got.add(new HashSet<>(group));
        }
        Set<Set<Integer>> expected = Set.of(Set.of(0, 1, 2), Set.of(3, 4), Set.of(5));
        check(got.equals(expected), "Kosaraju SCC groups");
        List<Integer> all = new ArrayList<>();
        for (List<Integer> group : r.groups) {
            all.addAll(group);
        }
        check(all.size() == g.size() && new HashSet<>(all).size() == g.size(), "Every vertex in exactly one SCC");
    }

    private static void testDijkstra() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 5);
        SP.Result r = SP.dijkstra(g, 0);
        check(r.dist[0] == 0 && r.dist[1] == 3 && r.dist[2] == 1 && r.dist[3] == 4, "Dijkstra distances");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "Dijkstra path reconstruction");
        check(!r.reachable(4) && r.pathTo(4).isEmpty(), "Unreachable vertex handling");
    }

    private static void testNegativeWeight() {
        Graph g = new Graph(2, true);
        g.addEdge(0, 1, -1);
        boolean rejected = false;
        try {
            SP.dijkstra(g, 0);
        } catch (IllegalArgumentException e) {
            rejected = true;
        }
        check(rejected, "Dijkstra rejects negative weights");
    }

    private static void testGeneratedGraphs() {
        int[][] sizes = {{10, 30}, {100, 500}, {1000, 5000}};
        for (int[] cfg : sizes) {
            Graph g1 = Bench.genUndirected(cfg[0], cfg[1], 501L + cfg[0]);
            Graph g2 = Bench.genUndirected(cfg[0], cfg[1], 501L + cfg[0]);
            check(g1.size() == cfg[0] && g1.edges().size() == cfg[1], "Generated graph size " + cfg[0]);
            check(g1.edges().get(0).from == g2.edges().get(0).from
                    && g1.edges().get(0).to == g2.edges().get(0).to
                    && g1.edges().get(0).weight == g2.edges().get(0).weight,
                    "Fixed-seed reproducibility " + cfg[0]);
            check(MST.kruskal(g1).connected(cfg[0]), "Generated graph connected " + cfg[0]);
            Graph dag = Bench.genDag(cfg[0], cfg[1], 801L + cfg[0]);
            check(Conn.topo(dag).acyclic && validTopo(dag, Conn.topo(dag).order), "Generated DAG " + cfg[0]);
            Graph cyc = Bench.genDirected(cfg[0], cfg[1], 901L + cfg[0]);
            check(!Conn.topo(cyc).acyclic, "Generated directed cycle " + cfg[0]);
        }
    }

    private static boolean validTopo(Graph g, List<Integer> order) {
        if (order.size() != g.size()) {
            return false;
        }
        int[] pos = new int[g.size()];
        boolean[] used = new boolean[g.size()];
        for (int i = 0; i < order.size(); i++) {
            int v = order.get(i);
            if (v < 0 || v >= g.size() || used[v]) {
                return false;
            }
            used[v] = true;
            pos[v] = i;
        }
        for (graph.Graph.Edge e : g.edges()) {
            if (pos[e.from] >= pos[e.to]) {
                return false;
            }
        }
        return true;
    }

    private static void check(boolean ok, String name) {
        if (!ok) {
            throw new AssertionError("FAILED: " + name);
        }
        passed++;
        System.out.println("PASS: " + name);
    }
}
