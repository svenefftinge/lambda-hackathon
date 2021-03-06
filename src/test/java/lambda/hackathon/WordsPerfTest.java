/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package lambda.hackathon;

import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.logic.results.Result;
import org.openjdk.jmh.logic.results.RunResult;
import org.openjdk.jmh.output.OutputFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.parameters.TimeValue;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
public class WordsPerfTest {

    Path filename;

    @Setup
    public void prepare() {
        filename = Paths.get("JaneAusten-PrideAndPrejudice.txt");
    }

    @GenerateMicroBenchmark
    public int testImperative() throws IOException {
        return Words.Imperative.processWords(filename).size();
    }

    @GenerateMicroBenchmark
    public int testLambdaSeq() throws IOException {
        return Words.Lambda.processWords(filename, false).size();
    }

    @GenerateMicroBenchmark
    public int testLambdaPar() throws IOException {
        return Words.Lambda.processWords(filename, true).size();
    }

    public static void main(String[] args) throws RunnerException {
        PrintWriter pw = new PrintWriter(System.out, true);

        pw.printf("       imperative = %.2f ns\n", run("testImperative"));
        pw.printf("sequential stream = %.2f ns\n", run("testLambdaSeq"));
        pw.printf("  parallel stream = %.2f ns\n", run("testLambdaPar"));
    }

    public static double run(String test) throws RunnerException {
        Options opts = new OptionsBuilder()
                .include(".*WordsPerfTest.*" + test)
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.NANOSECONDS)
                .warmupIterations(3)
                .warmupTime(TimeValue.milliseconds(100))
                .measurementIterations(5)
                .measurementTime(TimeValue.milliseconds(100))
                .outputFormat(OutputFormatType.Silent)
                .forks(5)
                .build();

        RunResult runResult = new Runner(opts).runSingle();
        Result result = runResult.getPrimaryResult();
        return result.getScore();
    }
}
