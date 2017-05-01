package com.ensoftcorp.open.commons.ui.views.dashboard;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.ensoftcorp.atlas.core.db.graph.Graph;
import com.ensoftcorp.atlas.core.query.Q;
import com.ensoftcorp.atlas.core.script.Common;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;
import com.ensoftcorp.open.commons.dashboard.WorkItem;

public class WorkItemViewComponent {

	private boolean expanded = false;
	private boolean reviewed = false;
	private boolean empty = false;
	private WorkItem workItem;
	private List<Result> results = new LinkedList<Result>();
	private Graph allResults = Common.empty().eval();
	private boolean initialized = false;
	
	public WorkItemViewComponent(WorkItem workItem) {
		this.workItem = workItem;
	}
	
	public synchronized void initialize(Q context) throws InterruptedException {
		Job job = new Job("Analyzing " + workItem.getName()){
			@Override
			protected IStatus run(IProgressMonitor mon) {
				results = workItem.getResults(context);
				allResults = workItem.getAllResults(context).eval();
				initialized = true;
				return Status.OK_STATUS;
			}
		};
		job.join(); // block until save is complete
	}
	
	public boolean isInitialized(){
		return initialized;
	}

	public boolean isExpanded() {
		return expanded;
	}
	
	public boolean isReviewed() {
		return reviewed;
	}
	
	public boolean isEmpty() {
		return empty;
	}
	
	public WorkItem getWorkItem() {
		return workItem;
	}
	
	public Graph getAllResults(){
		return allResults;
	}
	
	public List<Result> getResults(){
		return results;
	}

	public String getAssumptionsText() {
		StringBuilder result = new StringBuilder();
		int assumptions = 1;
		for(String assumption : workItem.getAssumptions()){
			result.append((assumptions++) + ") " + assumption + "\n");
		}
		String text = result.toString().trim();
		return text.equals("") ? "No assumptions." : text;
	}
	
}
