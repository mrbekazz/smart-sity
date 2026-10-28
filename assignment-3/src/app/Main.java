package app;

import connectivity.Conn;
import graph.Graph;
import graph.Graph.Edge;
import java.util.List;
import mst.MST;
import shortestpath.SP;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        Graph road = new Graph(4, false);
        road.addEdge(0, 1, 5);
        road.addEdge(0, 2, 2);
        road.addEdge(1, 2, 3);
        road.addEdge(1, 3, 4);
        road.addEdge(2, 3, 7);
        MST.Result k = MST.kruskal(road);
        MST.Result p = MST.prim(road);
        System.out.println("Smart City Infrastructure and Route Planning");
        System.out.println("MST Kruskal: weight=" + k.weight + ", connected=" + k.connected(road.size())
                + ", edges=" + edgeList(k.edges));
        System.out.println("MST Prim: weight=" + p.weight + ", connected=" + p.connected(road.size())
                + ", edges=" + edgeList(p.edges));

        Graph dag = new Graph(5, true);
        dag.addEdge(0, 1, 1);
        dag.addEdge(0, 2, 1);
        dag.addEdge(1, 3, 1);
        dag.addEdge(2, 3, 1);
        dag.addEdge(3, 4, 1);
        Conn.TopoResult topo = Conn.topo(dag);
        System.out.println("Topological order (DAG): " + (topo.acyclic ? topo.order : "not possible: cycle detected"));

        Graph cyc = new Graph(5, true);
        cyc.addEdge(0, 1, 1);
        cyc.addEdge(1, 2, 1);
        cyc.addEdge(2, 0, 1);
        cyc.addEdge(2, 3, 1);
        Conn.TopoResult tc = Conn.topo(cyc);
        System.out.println("Topological order (cyclic graph): "
                + (tc.acyclic ? tc.order : "not possible: cycle detected"));

        Graph dir = new Graph(6, true);
        dir.addEdge(0, 1, 4);
        dir.addEdge(0, 2, 1);
        dir.addEdge(2, 1, 2);
        dir.addEdge(1, 0, 3);
        dir.addEdge(1, 3, 1);
        dir.addEdge(3, 4, 1);
        dir.addEdge(4, 3, 1);
        Conn.SCCResult scc = Conn.scc(dir);
        System.out.println("SCC groups: " + scc.groups);
        SP.Result sp = SP.dijkstra(dir, 0);
        for (int v = 0; v < dir.size(); v++) {
            if (sp.reachable(v)) {
                System.out.println("0 -> " + v + ": distance=" + sp.dist[v] + ", path=" + sp.pathTo(v));
            } else {
                System.out.println("0 -> " + v + ": unreachable");
            }
        }
    }

    private static String edgeList(List<Edge> edges) {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < edges.size(); i++) {
            Edge e = edges.get(i);
            if (i > 0) {
                s.append(", ");
            }
            s.append(e.from).append('-').append(e.to).append('(').append(e.weight).append(')');
        }
        return s.append(']').toString();
    }
}
