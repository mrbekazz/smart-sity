package shortestpath;

import graph.Graph;
import graph.Graph.Edge;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public final class SP {
    private static final long INF = Long.MAX_VALUE;

    private static final class Q {
        final int v;
        final long d;

        Q(int v, long d) {
            this.v = v;
            this.d = d;
        }
    }

    public static final class Result {
        public final int source;
        public final long[] dist;
        public final int[] prev;
        public final long edgeScans;
        public final long pqPushes;
        public final long pqPops;
        public final long relaxAttempts;
        public final long relaxSuccesses;

        private Result(int source, long[] dist, int[] prev, long edgeScans,
                       long pqPushes, long pqPops, long relaxAttempts, long relaxSuccesses) {
            this.source = source;
            this.dist = dist.clone();
            this.prev = prev.clone();
            this.edgeScans = edgeScans;
            this.pqPushes = pqPushes;
            this.pqPops = pqPops;
            this.relaxAttempts = relaxAttempts;
            this.relaxSuccesses = relaxSuccesses;
        }

        public boolean reachable(int v) {
            check(v);
            return dist[v] != INF;
        }

        public List<Integer> pathTo(int v) {
            check(v);
            if (!reachable(v)) {
                return List.of();
            }
            List<Integer> path = new ArrayList<>();
            int cur = v;
            while (cur != -1) {
                path.add(cur);
                if (cur == source) {
                    break;
                }
                cur = prev[cur];
            }
            if (path.get(path.size() - 1) != source) {
                return List.of();
            }
            Collections.reverse(path);
            return List.copyOf(path);
        }

        private void check(int v) {
            if (v < 0 || v >= dist.length) {
                throw new IndexOutOfBoundsException("Invalid vertex: " + v);
            }
        }
    }

    private SP() {
    }

    public static Result dijkstra(Graph g, int source) {
        if (source < 0 || source >= g.size()) {
            throw new IndexOutOfBoundsException("Invalid source: " + source);
        }
        long scans = 0;
        for (Edge e : g.edges()) {
            scans++;
            if (e.weight < 0) {
                throw new IllegalArgumentException("Dijkstra requires non-negative edge weights");
            }
        }
        int n = g.size();
        long[] dist = new long[n];
        int[] prev = new int[n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(prev, -1);
        dist[source] = 0;
        Comparator<Q> cmp = Comparator.comparingLong((Q q) -> q.d).thenComparingInt(q -> q.v);
        PriorityQueue<Q> pq = new PriorityQueue<>(cmp);
        pq.offer(new Q(source, 0));
        long pushes = 1;
        long pops = 0;
        long attempts = 0;
        long successes = 0;
        while (!pq.isEmpty()) {
            Q cur = pq.poll();
            pops++;
            if (cur.d != dist[cur.v]) {
                continue;
            }
            for (Edge e : g.adj(cur.v)) {
                scans++;
                attempts++;
                if (cur.d > Long.MAX_VALUE - e.weight) {
                    continue;
                }
                long nd = cur.d + e.weight;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    prev[e.to] = cur.v;
                    pq.offer(new Q(e.to, nd));
                    pushes++;
                    successes++;
                }
            }
        }
        return new Result(source, dist, prev, scans, pushes, pops, attempts, successes);
    }
}
