package com.ensoftcorp.open.commons.ui.views.dashboard;

import com.ensoftcorp.open.commons.analyzers.Analyzer;

public class WorkItem {
	
	private boolean expanded = false;
	private boolean reviewed = false;
	private boolean empty = false;
	private Analyzer analyzer;
	
	public WorkItem(Analyzer analyzer) {
		this.analyzer = analyzer;
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
	
}
