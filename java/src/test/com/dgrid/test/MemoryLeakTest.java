package com.dgrid.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.gen.InvalidHost;
import com.dgrid.gen.Joblet;
import com.dgrid.gen.NoWorkAvailable;
import com.dgrid.helpers.ForkJoinHelper;
import com.dgrid.service.DGridClient;

public class MemoryLeakTest extends BaseTestCase {

	public void testCauseMemoryLeak() throws Exception {
		DGridClient gridClient = (DGridClient) super.getBean(DGridClient.NAME);
		ForkJoinHelper fj = (ForkJoinHelper) super.getBean(ForkJoinHelper.NAME);
		List<Callable> tasks = new ArrayList<Callable>();
		for (int i = 0; i < 5; ++i) {
			GetWork gw = new GetWork(gridClient);
			tasks.add(gw);
		}
		fj.executeConcurrently(tasks, 2);
	}
	private static class GetWork implements Callable {
		private DGridClient gridClient;
		public GetWork(DGridClient gridClient) {
			this.gridClient = gridClient;
		}

		public Object call() {
			Joblet joblet = null;
			for (int i = 0; i < 300; ++i) {
			try {
				joblet = gridClient.getWork();
				throw new RuntimeException("Work should not be available!");
			} catch(NoWorkAvailable e) {
			} catch (TransportException e) {
				throw new RuntimeException(e);
			} catch (InvalidApiKey e) {
				throw new RuntimeException(e);
			} catch (InvalidHost e) {
				throw new RuntimeException(e);
			}
			}
			return joblet;
		}
		
	}
}
