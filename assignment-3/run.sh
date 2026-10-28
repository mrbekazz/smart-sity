#!/usr/bin/env sh
set -eu
mkdir -p out
javac -encoding UTF-8 -d out src/graph/Graph.java src/dsu/DSU.java src/mst/MST.java src/connectivity/Conn.java src/shortestpath/SP.java src/app/Bench.java src/app/Main.java src/test/Test.java
case "${1:-demo}" in
  demo) java -cp out app.Main ;;
  test) java -cp out test.Test ;;
  bench) java -cp out app.Bench ;;
  *) printf 'Usage: ./run.sh [demo|test|bench]\n' >&2; exit 2 ;;
esac
