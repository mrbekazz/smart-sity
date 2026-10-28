package app;

import connectivity.Conn;
import graph.Graph;
import graph.Graph.Edge;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;
import mst.MST;
import shortestpath.SP;

public final class Bench {
    private static final long SEED = 20261022L;
    private static final int REPEATS = 5;
    private static final int[][] SIZES = {{10, 30}, {100, 500}, {1000, 5000}};
    private static final String[] ALGORITHMS = {
            "Kruskal", "Prim", "SCC", "Topological-DAG", "Topological-Cyclic", "Dijkstra"
    };
    private static final String[] COLORS = {
            "#2563eb", "#dc2626", "#059669", "#7c3aed", "#d97706", "#0891b2"
    };

    private Bench() {
    }

    public static void main(String[] args) throws IOException {
        Path root = Path.of("results");
        Path tables = root.resolve("tables");
        Path plots = root.resolve("plots");
        Files.createDirectories(tables);
        Files.createDirectories(plots);
        List<Row> rows = new ArrayList<>();
        for (int[] cfg : SIZES) {
            int n = cfg[0];
            int m = cfg[1];
            Graph undirected = genUndirected(n, m, seed(n, m, 1));
            Graph directed = genDirected(n, m, seed(n, m, 2));
            Graph dag = genDag(n, m, seed(n, m, 3));
            MST.Result kCheck = MST.kruskal(undirected);
            MST.Result pCheck = MST.prim(undirected);
            if (!kCheck.connected(n) || !pCheck.connected(n) || kCheck.weight != pCheck.weight) {
                throw new IllegalStateException("MST validation failed for V=" + n + ", E=" + m);
            }
            Conn.TopoResult tCheck = Conn.topo(dag);
            if (!tCheck.acyclic || !validTopo(dag, tCheck.order)) {
                throw new IllegalStateException("DAG validation failed for V=" + n + ", E=" + m);
            }
            if (Conn.topo(directed).acyclic) {
                throw new IllegalStateException("Cycle graph was incorrectly accepted as a DAG");
            }

            Measurement<MST.Result> km = measure(() -> MST.kruskal(undirected));
            MST.Result kr = km.value;
            rows.add(new Row("Kruskal", "Undirected-connected", n, undirected.edges().size(),
                    km.average, kr.edgeScans, kr.sortComparisons, kr.findCalls, kr.unionCalls,
                    kr.pqPushes, kr.pqPops, kr.components, kr.connected(n)));

            Measurement<MST.Result> pm = measure(() -> MST.prim(undirected));
            MST.Result pr = pm.value;
            rows.add(new Row("Prim", "Undirected-connected", n, undirected.edges().size(),
                    pm.average, pr.edgeScans, pr.sortComparisons, pr.findCalls, pr.unionCalls,
                    pr.pqPushes, pr.pqPops, pr.components, pr.connected(n)));

            Measurement<Conn.SCCResult> sm = measure(() -> Conn.scc(directed));
            Conn.SCCResult sr = sm.value;
            rows.add(new Row("SCC", "Directed-with-cycle", n, directed.edges().size(),
                    sm.average, sr.edgeScans, 0, 0, 0, 0, 0, sr.groups.size(), true));

            Measurement<Conn.TopoResult> tm = measure(() -> Conn.topo(dag));
            Conn.TopoResult tr = tm.value;
            rows.add(new Row("Topological-DAG", "Directed-DAG", n, dag.edges().size(),
                    tm.average, tr.edgeScans, 0, 0, 0, 0, 0, 0, tr.acyclic));

            Measurement<Conn.TopoResult> tcm = measure(() -> Conn.topo(directed));
            Conn.TopoResult tcr = tcm.value;
            rows.add(new Row("Topological-Cyclic", "Directed-with-cycle", n, directed.edges().size(),
                    tcm.average, tcr.edgeScans, 0, 0, 0, 0, 0, 0, !tcr.acyclic));

            Measurement<SP.Result> dm = measure(() -> SP.dijkstra(directed, 0));
            SP.Result dr = dm.value;
            int reachable = 0;
            for (long d : dr.dist) {
                if (d != Long.MAX_VALUE) {
                    reachable++;
                }
            }
            rows.add(new Row("Dijkstra", "Directed-nonnegative", n, directed.edges().size(),
                    dm.average, dr.edgeScans, 0, 0, 0, dr.pqPushes, dr.pqPops, reachable, true));
        }
        writeCsv(tables.resolve("benchmark.csv"), rows);
        writeMarkdown(tables.resolve("benchmark.md"), rows);
        writePlot(plots.resolve("time-v.svg"), rows, false);
        writePlot(plots.resolve("time-e.svg"), rows, true);
        System.out.println("Seed: " + SEED);
        System.out.println("Repeats per algorithm and graph size: " + REPEATS);
        System.out.println("CSV: " + tables.resolve("benchmark.csv"));
        System.out.println("Table: " + tables.resolve("benchmark.md"));
        System.out.println("Plots: " + plots.resolve("time-v.svg") + ", " + plots.resolve("time-e.svg"));
        for (Row r : rows) {
            System.out.printf(Locale.ROOT, "%s | V=%d E=%d | avg=%d ns | scans=%d | success=%s%n",
                    r.algorithm, r.v, r.e, r.avgNs, r.edgeScans, r.success);
        }
    }

    private static long seed(int n, int m, int kind) {
        return SEED + n * 1009L + m * 9176L + kind * 65537L;
    }

    public static Graph genUndirected(int n, int m, long seed) {
        if (n < 1 || m < n - 1 || m > (long) n * (n - 1) / 2) {
            throw new IllegalArgumentException("Invalid connected simple graph size");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        for (int v = 1; v < n; v++) {
            int u = rnd.nextInt(v);
            addUndirected(g, used, u, v, 1 + rnd.nextInt(100));
        }
        while (g.edges().size() < m) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) {
                continue;
            }
            addUndirected(g, used, u, v, 1 + rnd.nextInt(100));
        }
        return g;
    }

    public static Graph genDirected(int n, int m, long seed) {
        if (n < 3 || m < 3 || m > (long) n * (n - 1)) {
            throw new IllegalArgumentException("Invalid directed graph size");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        addDirected(g, used, 0, 1, 1 + rnd.nextInt(100));
        addDirected(g, used, 1, 2, 1 + rnd.nextInt(100));
        addDirected(g, used, 2, 0, 1 + rnd.nextInt(100));
        while (g.edges().size() < m) {
            int u = rnd.nextInt(n);
            int v = rnd.nextInt(n);
            if (u == v) {
                continue;
            }
            addDirected(g, used, u, v, 1 + rnd.nextInt(100));
        }
        return g;
    }

    public static Graph genDag(int n, int m, long seed) {
        if (n < 2 || m < 0 || m > (long) n * (n - 1) / 2) {
            throw new IllegalArgumentException("Invalid DAG size");
        }
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edges().size() < m) {
            int u = rnd.nextInt(n - 1);
            int v = u + 1 + rnd.nextInt(n - u - 1);
            addDirected(g, used, u, v, 1 + rnd.nextInt(100));
        }
        return g;
    }

    private static void addUndirected(Graph g, Set<Long> used, int u, int v, int w) {
        int a = Math.min(u, v);
        int b = Math.max(u, v);
        long key = (((long) a) << 32) | (b & 0xffffffffL);
        if (used.add(key)) {
            g.addEdge(a, b, w);
        }
    }

    private static void addDirected(Graph g, Set<Long> used, int u, int v, int w) {
        long key = (((long) u) << 32) | (v & 0xffffffffL);
        if (used.add(key)) {
            g.addEdge(u, v, w);
        }
    }

    private static boolean validTopo(Graph g, List<Integer> order) {
        if (order.size() != g.size()) {
            return false;
        }
        int[] pos = new int[g.size()];
        for (int i = 0; i < order.size(); i++) {
            pos[order.get(i)] = i;
        }
        for (Edge e : g.edges()) {
            if (pos[e.from] >= pos[e.to]) {
                return false;
            }
        }
        return true;
    }

    private static <T> Measurement<T> measure(Supplier<T> job) {
        job.get();
        long total = 0;
        T value = null;
        for (int i = 0; i < REPEATS; i++) {
            long start = System.nanoTime();
            value = job.get();
            total += System.nanoTime() - start;
        }
        return new Measurement<>(value, total / REPEATS);
    }

    private static void writeCsv(Path file, List<Row> rows) throws IOException {
        StringBuilder s = new StringBuilder("algorithm,graph,V,E,repeats,avg_time_ns,edge_scans,sort_comparisons,find_calls,union_calls,pq_pushes,pq_pops,components,success\n");
        for (Row r : rows) {
            s.append(r.algorithm).append(',').append(r.graph).append(',').append(r.v).append(',')
                    .append(r.e).append(',').append(REPEATS).append(',').append(r.avgNs).append(',')
                    .append(r.edgeScans).append(',').append(r.sortComparisons).append(',')
                    .append(r.findCalls).append(',').append(r.unionCalls).append(',')
                    .append(r.pqPushes).append(',').append(r.pqPops).append(',')
                    .append(r.components).append(',').append(r.success).append('\n');
        }
        Files.writeString(file, s);
    }

    private static void writeMarkdown(Path file, List<Row> rows) throws IOException {
        StringBuilder s = new StringBuilder();
        s.append("| Algorithm | Graph | V | E | Average time (ns) | Edge scans | Sort comparisons | DSU finds | DSU unions | PQ pushes | PQ pops | Components / reachable | Success |\n");
        s.append("|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|:---:|\n");
        for (Row r : rows) {
            s.append('|').append(r.algorithm).append('|').append(r.graph).append('|').append(r.v).append('|')
                    .append(r.e).append('|').append(r.avgNs).append('|').append(r.edgeScans).append('|')
                    .append(r.sortComparisons).append('|').append(r.findCalls).append('|').append(r.unionCalls)
                    .append('|').append(r.pqPushes).append('|').append(r.pqPops).append('|')
                    .append(r.components).append('|').append(r.success).append("|\n");
        }
        Files.writeString(file, s);
    }

    private static void writePlot(Path file, List<Row> rows, boolean byEdges) throws IOException {
        List<Row> plotRows = new ArrayList<>(rows);
        plotRows.sort(Comparator.comparingInt(r -> r.v));
        long min = Long.MAX_VALUE;
        long max = 1;
        for (Row r : plotRows) {
            min = Math.min(min, Math.max(1, r.avgNs));
            max = Math.max(max, Math.max(1, r.avgNs));
        }
        int lo = (int) Math.floor(Math.log10(min));
        int hi = (int) Math.ceil(Math.log10(max));
        if (lo == hi) {
            hi++;
        }
        double top = 75;
        double bottom = 360;
        double left = 110;
        double right = 810;
        double[] xs = {left, (left + right) / 2, right};
        int[] sizes = {0, 0, 0};
        for (int i = 0; i < SIZES.length; i++) {
            sizes[i] = byEdges ? SIZES[i][1] : SIZES[i][0];
        }
        String title = byEdges ? "Execution time vs. number of edges" : "Execution time vs. number of vertices";
        String xLabel = byEdges ? "Number of edges (E)" : "Number of vertices (V)";
        StringBuilder s = new StringBuilder();
        s.append("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"920\" height=\"520\" viewBox=\"0 0 920 520\">");
        s.append("<rect width=\"920\" height=\"520\" fill=\"white\"/>");
        s.append("<text x=\"460\" y=\"34\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"22\" font-weight=\"700\">")
                .append(title).append("</text>");
        for (int exp = lo; exp <= hi; exp++) {
            double val = Math.pow(10, exp);
            double y = bottom - (exp - lo) / (double) (hi - lo) * (bottom - top);
            s.append("<line x1=\"").append(left).append("\" y1=\"").append(y)
                    .append("\" x2=\"").append(right).append("\" y2=\"").append(y)
                    .append("\" stroke=\"#e5e7eb\" stroke-width=\"1\"/>");
            s.append("<text x=\"").append(left - 12).append("\" y=\"").append(y + 4)
                    .append("\" text-anchor=\"end\" font-family=\"Arial\" font-size=\"12\" fill=\"#374151\">1e")
                    .append(exp).append("</text>");
        }
        s.append("<line x1=\"").append(left).append("\" y1=\"").append(top).append("\" x2=\"").append(left)
                .append("\" y2=\"").append(bottom).append("\" stroke=\"#374151\"/>");
        s.append("<line x1=\"").append(left).append("\" y1=\"").append(bottom).append("\" x2=\"").append(right)
                .append("\" y2=\"").append(bottom).append("\" stroke=\"#374151\"/>");
        for (int i = 0; i < 3; i++) {
            s.append("<text x=\"").append(xs[i]).append("\" y=\"").append(bottom + 23)
                    .append("\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"13\" fill=\"#111827\">")
                    .append(sizes[i]).append("</text>");
        }
        s.append("<text x=\"460\" y=\"414\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"14\">")
                .append(xLabel).append("</text>");
        s.append("<text x=\"24\" y=\"218\" transform=\"rotate(-90 24 218)\" text-anchor=\"middle\" font-family=\"Arial\" font-size=\"14\">Average time (ns, logarithmic scale)</text>");
        for (int a = 0; a < ALGORITHMS.length; a++) {
            List<Row> series = new ArrayList<>();
            for (Row r : rows) {
                if (r.algorithm.equals(ALGORITHMS[a])) {
                    series.add(r);
                }
            }
            series.sort(Comparator.comparingInt(r -> r.v));
            if (series.size() != 3) {
                continue;
            }
            s.append("<polyline fill=\"none\" stroke=\"").append(COLORS[a]).append("\" stroke-width=\"2.5\" points=\"");
            for (int i = 0; i < series.size(); i++) {
                double value = Math.max(1, series.get(i).avgNs);
                double y = bottom - (Math.log10(value) - lo) / (hi - lo) * (bottom - top);
                s.append(xs[i]).append(',').append(y).append(' ');
            }
            s.append("\"/>");
            for (int i = 0; i < series.size(); i++) {
                double value = Math.max(1, series.get(i).avgNs);
                double y = bottom - (Math.log10(value) - lo) / (hi - lo) * (bottom - top);
                s.append("<circle cx=\"").append(xs[i]).append("\" cy=\"").append(y).append("\" r=\"4\" fill=\"")
                        .append(COLORS[a]).append("\"/>");
            }
            double lx = 125 + (a % 3) * 245;
            double ly = 452 + (a / 3) * 30;
            s.append("<line x1=\"").append(lx).append("\" y1=\"").append(ly)
                    .append("\" x2=\"").append(lx + 28).append("\" y2=\"").append(ly)
                    .append("\" stroke=\"").append(COLORS[a]).append("\" stroke-width=\"3\"/>");
            s.append("<text x=\"").append(lx + 35).append("\" y=\"").append(ly + 4)
                    .append("\" font-family=\"Arial\" font-size=\"12\" fill=\"#111827\">")
                    .append(ALGORITHMS[a]).append("</text>");
        }
        s.append("</svg>");
        Files.writeString(file, s);
    }

    private static final class Measurement<T> {
        final T value;
        final long average;

        Measurement(T value, long average) {
            this.value = value;
            this.average = average;
        }
    }

    private static final class Row {
        final String algorithm;
        final String graph;
        final int v;
        final int e;
        final long avgNs;
        final long edgeScans;
        final long sortComparisons;
        final long findCalls;
        final long unionCalls;
        final long pqPushes;
        final long pqPops;
        final int components;
        final boolean success;

        Row(String algorithm, String graph, int v, int e, long avgNs, long edgeScans,
            long sortComparisons, long findCalls, long unionCalls, long pqPushes,
            long pqPops, int components, boolean success) {
            this.algorithm = algorithm;
            this.graph = graph;
            this.v = v;
            this.e = e;
            this.avgNs = avgNs;
            this.edgeScans = edgeScans;
            this.sortComparisons = sortComparisons;
            this.findCalls = findCalls;
            this.unionCalls = unionCalls;
            this.pqPushes = pqPushes;
            this.pqPops = pqPops;
            this.components = components;
            this.success = success;
        }
    }
}
