package mst;

import dsu.DSU;
import graph.Graph;
import graph.Graph.Edge;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class MST {
    public static final class Result {
        public final long weight;
        public final List<Edge> edges;
        public final int components;
        public final long edgeScans;
        public final long sortComparisons;
        public final long findCalls;
        public final long unionCalls;
        public final long pqPushes;
        public final long pqPops;

        private Result(long weight, List<Edge> edges, int components, long edgeScans,
                       long sortComparisons, long findCalls, long unionCalls,
                       long pqPushes, long pqPops) {
            this.weight = weight;
            this.edges = List.copyOf(edges);
            this.components = components;
            this.edgeScans = edgeScans;
            this.sortComparisons = sortComparisons;
            this.findCalls = findCalls;
            this.unionCalls = unionCalls;
            this.pqPushes = pqPushes;
            this.pqPops = pqPops;
        }

        public boolean connected(int n) {
            return n <= 1 || components == 1;
        }
    }

    private MST() {
    }

    public static Result kruskal(Graph g) {
        requireUndirected(g);
        List<Edge> es = new ArrayList<>(g.edges());
        long[] cmp = {0};
        es.sort((a, b) -> {
            cmp[0]++;
            int c = Integer.compare(a.weight, b.weight);
            if (c != 0) {
                return c;
            }
            c = Integer.compare(a.from, b.from);
            return c != 0 ? c : Integer.compare(a.to, b.to);
        });
        DSU d = new DSU(g.size());
        List<Edge> picked = new ArrayList<>();
        long sum = 0;
        long scans = 0;
        for (Edge e : es) {
            scans++;
            if (d.union(e.from, e.to)) {
                picked.add(e);
                sum += e.weight;
                if (picked.size() == Math.max(0, g.size() - 1)) {
                    break;
                }
            }
        }
        return new Result(sum, picked, d.components(), scans, cmp[0], d.findCount(),
                d.unionCount(), 0, 0);
    }

    public static Result prim(Graph g) {
        requireUndirected(g);
        int n = g.size();
        boolean[] seen = new boolean[n];
        Comparator<Edge> order = Comparator.comparingInt((Edge e) -> e.weight)
                .thenComparingInt(e -> e.from).thenComparingInt(e -> e.to);
        PriorityQueue<Edge> pq = new PriorityQueue<>(order);
        List<Edge> picked = new ArrayList<>();
        long sum = 0;
        long scans = 0;
        long pushes = 0;
        long pops = 0;
        int comps = 0;
        int reached = 0;
        for (int start = 0; start < n; start++) {
            if (seen[start]) {
                continue;
            }
            comps++;
            seen[start] = true;
            reached++;
            for (Edge e : g.adj(start)) {
                scans++;
                if (!seen[e.to]) {
                    pq.offer(e);
                    pushes++;
                }
            }
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                pops++;
                if (seen[e.to]) {
                    continue;
                }
                seen[e.to] = true;
                reached++;
                picked.add(e);
                sum += e.weight;
                for (Edge next : g.adj(e.to)) {
                    scans++;
                    if (!seen[next.to]) {
                        pq.offer(next);
                        pushes++;
                    }
                }
            }
        }
        return new Result(sum, picked, comps, scans, 0, 0, 0, pushes, pops);
    }

    private static void requireUndirected(Graph g) {
        if (g.isDirected()) {
            throw new IllegalArgumentException("MST requires an undirected graph");
        }
    }
}
