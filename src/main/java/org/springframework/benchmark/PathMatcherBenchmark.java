/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.benchmark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.ParsingPathMatcher;

@BenchmarkMode(Mode.Throughput)
public class PathMatcherBenchmark {

	@State(Scope.Benchmark)
	static public class RoutingState {

		List<Route> routes;

		PathMatcher antPathMatcher;

		PathMatcher parsingPathMatcher;

		String lookupPath;

		@Setup(Level.Iteration)
		public void setup() {
			this.routes = RouteGenerator.generateRoutes();
			this.antPathMatcher = new AntPathMatcher();
			this.parsingPathMatcher = new ParsingPathMatcher();
		}

		@Setup(Level.Invocation)
		public void prepare() {
			Random random = new Random();
			int index = random.nextInt(this.routes.size());
			this.lookupPath = this.routes.get(index).generateMatchingPath();
		}

		@TearDown(Level.Iteration)
		public void shutdown() {
			this.antPathMatcher = null;
			this.parsingPathMatcher = null;
		}
	}

	@Benchmark
	public void baseline(Blackhole blackHole, RoutingState routingState) {

		List<Route> matching = new ArrayList<>();
		for (Route route : routingState.routes) {
			blackHole.consume(route.getPattern());
			blackHole.consume(routingState.lookupPath);
		}
		blackHole.consume(matching);
	}

	@Benchmark
	public void antPathMatcher(Blackhole blackHole, RoutingState routingState) {
		matchAndSort(routingState.routes, routingState.lookupPath, routingState.antPathMatcher, blackHole);
	}

	@Benchmark
	public void parsingPathMatcher(Blackhole blackHole, RoutingState routingState) {
		matchAndSort(routingState.routes, routingState.lookupPath, routingState.parsingPathMatcher, blackHole);
	}

	private void matchAndSort(List<Route> routes, String lookupPath,
			PathMatcher pathMatcher, Blackhole blackhole) {
		List<String> matching = new ArrayList<>();
		for (Route route : routes) {
			if (pathMatcher.match(route.getPattern(), lookupPath)) {
				matching.add(route.getPattern());
			}
		}
		Comparator<String> comparator = pathMatcher.getPatternComparator(lookupPath);
		matching.sort(comparator);
		blackhole.consume(matching);
	}

	private interface Route {

		String getPattern();

		String generateMatchingPath();
	}

	private static class RouteGenerator {

		static List<Route> generateRoutes() {
			List<Route> routes = new ArrayList<>();

			routes.add(new StaticRoute("/404"));
			routes.add(new StaticRoute("/500"));
			routes.add(new StaticRoute("/platform"));
			routes.add(new StaticRoute("/services"));
			routes.add(new StaticRoute("/signin"));
			routes.add(new StaticRoute("/**"));
			routes.add(new StaticRoute("/blog.atom"));
			routes.add(new DynamicRoute("/blog/category/{category}.atom",
					"/blog/category/releases.atom",
					"/blog/category/engineering.atom",
					"/blog/category/news.atom"));
			routes.add(new StaticRoute("/blog/broadcasts.atom"));
			routes.add(new StaticRoute("/blog"));
			routes.add(new DynamicRoute("/blog/{year:\\\\d+}/{month:\\\\d+}/{day:\\\\d+}/{slug}",
					"/blog/2017/02/09/spring-cloud-pipelines-1-0-0-m3-released",
					"/blog/2017/02/06/springone-platform-2016-replay-spring-for-apache-kafka",
					"/blog/2017/02/06/spring-for-apache-kafka-1-1-3-available-now",
					"/blog/2017/02/06/spring-cloud-camden-sr5-is-available",
					"/blog/2017/02/01/spring-team-at-devnexus-2017",
					"/blog/2017/02/01/spring-io-platform-athens-sr3"));
			routes.add(new StaticRoute("/blog/broadcasts"));
			routes.add(new DynamicRoute("/blog/{year:\\\\d+}/{month:\\\\d+}",
					"/blog/2017/02", "/blog/2017/01", "/blog/2016/12", "/blog/2016/11"));
			routes.add(new StaticRoute("/docs/reference"));
			routes.add(new StaticRoute("/docs"));
			routes.add(new StaticRoute("/webhook/docs/guides"));
			routes.add(new DynamicRoute("/webhook/docs/guides/{repositoryName}",
					"/webhook/docs/guides/rest-service", "/webhook/docs/guides/scheduling-tasks",
					"/webhook/docs/guides/consuming-rest", "/webhook/docs/guides/relational-data-access"));
			routes.add(new DynamicRoute("/guides/gs/{repositoryName}",
					"/guides/gs/rest-service", "/guides/gs/scheduling-tasks",
					"/guides/gs/consuming-rest", "/guides/gs/relational-data-access"));
			routes.add(new StaticRoute("/guides"));
			routes.add(new StaticRoute("/error"));
			routes.add(new StaticRoute("/tools"));
			routes.add(new StaticRoute("/tools/eclipse"));
			routes.add(new StaticRoute("/tools/sts"));
			routes.add(new StaticRoute("/tools/sts/all"));
			routes.add(new DynamicRoute("/team/{username}",
					"/team/bclozel", "/team/snicoll", "/team/sdeleuze", "/team/rstoyanchev"));
			routes.add(new StaticRoute("/team"));
			routes.add(new StaticRoute("/search"));
			routes.add(new StaticRoute("/project"));
			routes.add(new StaticRoute("/questions"));
			routes.add(new DynamicRoute("/project_metadata/{projectId}",
					"/project_metadata/spring-boot", "/project_metadata/spring-framework",
					"/project_metadata/reactor", "/project_metadata/spring-data",
					"/project_metadata/spring-restdocs", "/project_metadata/spring-batch"));
			routes.add(new DynamicRoute("/badges/{projectId}.svg",
					"/badges/spring-boot.svg", "/badges/spring-framework.svg",
					"/badges/reactor", "/badges/spring-data.svg",
					"/badges/spring-restdocs.svg", "/badges/spring-batch.svg"));
			return routes;
		}

	}

	private static class StaticRoute implements Route {

		private final String pattern;

		public StaticRoute(String route) {
			pattern = route;
		}

		public String getPattern() {
			return pattern;
		}

		public String generateMatchingPath() {
			return this.pattern;
		}

	}

	private static class DynamicRoute implements Route {

		private final String pattern;

		private final List<String> matchingPaths;

		public DynamicRoute(String pattern, String... matchingPaths) {
			this.pattern = pattern;
			this.matchingPaths = Arrays.asList(matchingPaths);
		}

		public String getPattern() {
			return pattern;
		}

		public String generateMatchingPath() {
			Random random = new Random();
			return this.matchingPaths.get(random.nextInt(this.matchingPaths.size()));
		}

	}

}
