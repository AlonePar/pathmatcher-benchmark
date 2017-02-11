#!/bin/sh
mvn clean package

echo "*****************************"
echo "* BENCHMARK - SINGLE THREAD *"
echo "*****************************"
java -jar target/benchmarks.jar -f 1 -wi 5 -i 5 -r 3s -t1 -jvmArgs '-server -XX:+AggressiveOpts' .*Benchmark.*

echo "*****************************"
echo "* BENCHMARK - 5 THREAD      *"
echo "*****************************"
java -jar target/benchmarks.jar -f 1 -wi 5 -i 5 -r 3s -t5 -jvmArgs '-server -XX:+AggressiveOpts' .*Benchmark.*