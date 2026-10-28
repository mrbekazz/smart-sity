package dsu;

public final class DSU {
    private final int[] parent;
    private final int[] size;
    private int components;
    private long finds;
    private long unions;

    public DSU(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }
        parent = new int[n];
        size = new int[n];
        components = n;
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            size[i] = 1;
        }
    }

    public int find(int x) {
        check(x);
        finds++;
        int root = x;
        while (root != parent[root]) {
            root = parent[root];
        }
        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    public boolean union(int a, int b) {
        unions++;
        int ra = find(a);
        int rb = find(b);
        if (ra == rb) {
            return false;
        }
        if (size[ra] < size[rb]) {
            int t = ra;
            ra = rb;
            rb = t;
        }
        parent[rb] = ra;
        size[ra] += size[rb];
        components--;
        return true;
    }

    public int components() {
        return components;
    }

    public long findCount() {
        return finds;
    }

    public long unionCount() {
        return unions;
    }

    private void check(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException("Invalid element: " + x);
        }
    }
}
