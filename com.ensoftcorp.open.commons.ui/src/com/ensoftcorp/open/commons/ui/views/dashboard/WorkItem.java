package com.ensoftcorp.open.commons.ui.views.dashboard;

import java.util.List;

import com.ensoftcorp.open.commons.analyzers.Analyzer;
import com.ensoftcorp.open.commons.analyzers.Analyzer.Result;

public class WorkItem implements Comparable<WorkItem> {

	private boolean expanded = false;
	private boolean reviewed = false;
	private boolean empty = false;
	private List<Result> results = null;
	private Analyzer analyzer;

	public WorkItem(Analyzer analyzer){
		this.analyzer = analyzer;
	}
	
	public WorkItem(Analyzer analyzer, List<Result> results) {
		this.analyzer = analyzer;
		initialize(results);
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
	
	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public String getAssumptionsText() {
		StringBuilder result = new StringBuilder();
		int assumptions = 1;
		for(String assumption : analyzer.getAssumptions()){
			result.append((assumptions++) + ") " + assumption + "\n");
		}
		String text = result.toString().trim();
		return text.equals("") ? "No assumptions." : text;
	}

	public void setContentExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}
	
	public boolean isInitialized(){
		return results != null;
	}
	
	public void initialize(List<Result> results){
		this.results = results;
		if(Analyzer.getAllResults(results).eval().nodes().isEmpty()){
			this.empty = true;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((analyzer == null) ? 0 : analyzer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WorkItem other = (WorkItem) obj;
		if (analyzer == null) {
			if (other.analyzer != null)
				return false;
		} else if (!analyzer.equals(other.analyzer))
			return false;
		return true;
	}

	@Override
	public int compareTo(WorkItem o) {
		return this.getAnalyzer().getName().compareTo(o.getAnalyzer().getName());
	}
	
}
