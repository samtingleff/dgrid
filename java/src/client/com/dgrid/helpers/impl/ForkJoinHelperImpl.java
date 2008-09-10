package com.dgrid.helpers.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import jsr166y.forkjoin.ForkJoinExecutor;
import jsr166y.forkjoin.ForkJoinPool;
import jsr166y.forkjoin.RecursiveAction;

import com.dgrid.gen.Joblet;
import com.dgrid.gen.JobletResult;
import com.dgrid.helpers.ForkJoinHelper;
import com.dgrid.service.DGridClient;

@SuppressWarnings("unchecked")
public class ForkJoinHelperImpl implements ForkJoinHelper {
	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public List executeConcurrently(List<Callable> tasks, int maxThreadCount) {
		log.trace("executeConcurrently()");
		int threadCount = (maxThreadCount > tasks.size()) ? tasks.size()
				: maxThreadCount;
		ForkJoinExecutor fjPool = new ForkJoinPool(threadCount);
		FJAction actions = new FJAction((Callable[]) tasks
				.toArray(new Callable[tasks.size()]));
		if (log.isDebugEnabled()) {
			log.debug(String.format(
					"Calling ForkJoinPool.invoke() with %1$d tasks", tasks
							.size()));
		}
		fjPool.invoke(actions);
		List results = actions.getResults();
		if (log.isDebugEnabled()) {
			log.debug("ForkJoinPool.invoke() returned");
			log.debug(String.format("Results: %1$s", results));
		}
		return results;
	}

	public List<JobletResult> gridExecuteConcurrently(List<Joblet> joblets,
			int maxThreadCount, int retries) {
		List<Callable> tasks = (List<Callable>) new ArrayList<Callable>(joblets
				.size());
		for (Joblet joblet : joblets) {
			Callable<JobletResult> c = new GridCallable(gridClient, joblet,
					retries);
			tasks.add(c);
		}
		List results = executeConcurrently(tasks, maxThreadCount);
		return results;
	}

	private static class GridCallable implements Callable<JobletResult> {
		private DGridClient gridClient;

		private Joblet joblet;

		private int retries;

		private GridCallable(DGridClient gridClient, Joblet joblet, int retries) {
			this.gridClient = gridClient;
			this.joblet = joblet;
			this.retries = retries;
		}

		public JobletResult call() throws Exception {
			return gridClient.gridExecute(joblet, retries);
		}

	}

	private static class FJAction extends RecursiveAction {

		private Callable[] tasks;

		private List results;

		public FJAction(Callable[] tasks) {
			this.tasks = tasks;
		}

		private List getResults() {
			return results;
		}

		@Override
		protected void compute() {
			results = new ArrayList(tasks.length);
			if (tasks.length == 1) {
				try {
					results.add(computeSequentially());
				} catch (Exception e) {
					results.add(e);
				} finally {
				}
			} else {
				FJAction[] actions = new FJAction[tasks.length];
				for (int i = 0; i < actions.length; ++i) {
					actions[i] = new FJAction(new Callable[] { tasks[i] });
				}
				forkJoin(actions);
				results = merge(actions);
			}
		}

		private List merge(FJAction[] actions) {
			List values = new ArrayList();
			for (FJAction action : actions) {
				List results = action.getResults();
				for (Object object : results) {
					values.add(object);
				}
			}
			return values;
		}

		private Object computeSequentially() throws Exception {
			return tasks[0].call();
		}
	}
}
