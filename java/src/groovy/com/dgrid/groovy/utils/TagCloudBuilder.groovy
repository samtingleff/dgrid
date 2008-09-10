package com.dgrid.groovy.utils;

/**
 * Log-based tag cloud builder
 */
class TagCloudBuilder {

	public String testCase() {
		def tags = [["hello", 10], ["goodbye", 3], ["brad", 90], ["angelina", 87]];
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		generateTagCloud(tags, 10, { tag, stepping ->
			sb.append("[${tag},${stepping}],");
		});
		sb.append("]");
		return sb.toString();
	}

	public void generateTagCloud(tags, steps, eachClosure) {
		def result = buildTagCloud(tags, steps);
		// sort it
		def mc = [
		         compare: {a,b-> a[0].compareToIgnoreCase(b[0]); }
		] as Comparator;
		result = result.sort(mc);
		result.each {
			eachClosure(it[1], it[0]);
		}
	}

	def buildTagCloud(tags, steps) {
		def temp = [];
		def newThresholds = [];
		def results = [];
		tags.each {
			temp << it[1];
		};
		def maxWeight = Collections.max(temp);
		def minWeight = Collections.min(temp);
		def newDelta = (maxWeight - minWeight)/steps;
		for (int i = 0; i <= steps; ++i) {
			newThresholds << [100 * Math.log((minWeight + (i * newDelta)) + 2), i];
		}
		tags.each {
			def fontset = false;
			for (int i = 1; i < (steps + 1); ++i) {
				def threshold = newThresholds[i];
				if ((100 * Math.log(it[1] + 2)) <= threshold[0] && (!fontset)) {
					results << [it[0], threshold[1]];
					fontset = true;
				}
			}
		};
		return results;
	}
}
