---
layout: page
title: Tutorials
permalink: /tutorials/
---

If you haven't already, [install](/toolbox-commons/install) the Toolbox Common plugin into Eclipse.

## Running Examples
The easiest way to try out the Toolbox Commons is to fork or clone the Starter-Toolbox project, which already has a dependency on the Toolbox-Commons configured.

	git clone git@github.com:EnSoftCorp/Starter-Toolbox.git

Import the `toolbox.analysis` and `toolbox.shell` projects into the workspace. Navigate to `Window ` &gt; `Show View` &gt; `Other...` &gt; `Atlas` &gt; `Atlas Shell`. Select the `toolbox.shell` project and press OK. You should also import and map an Eclipse project for analysis (`Atlas` &gt; `Manage Project Settings`).

To run the examples below, copy and paste the code snippets into your toolbox project and invoke from the Atlas Shell.

## Examples
The Toolbox Commons project contains several program analysis utilities that may or may not be applicable to your specific analysis needs. The headings below categorize the examples by general analysis needs. You should jump to the examples that interest you.

### Writing a Program Analyzer
A program Analyzer is a modularized analysis program to analyze a certain aspect of the program graph. The result of an analysis is an "envelope", which is a Q object containing the necessary elements from the program graph to convey the result of the analysis. Typically an envelope is empty if the analysis property is not satisfied (undetected in the program graph), and not empty if the analysis property is satisfied (detected in the program graph).

#### Extending the Analyzer Framework
To create a new `Analyzer` simply extend the `com.ensoftcorp.open.commons.analysis.Analyzer` class and implement the required methods. An example `Analyzer` to detect valid Java main methods is provided in the `Starter-Toolbox` called `DiscoverMainMethods`.

Running your `Analyzer` is simple, just construct a new `DiscoverMainMethods` `Analyzer` object and call the `getEnvelope` method. The first time you call `getEnvelope`, the result is cached in the object so calling `getEnvelope()` again is cheap. Note that the `DiscoverMainMethods` `Analyzer` above is designed to return an empty `Q` object if no valid Java main methods are discovered.

### Formatted SourceCorrespondence
Atlas conveniently provides `SourceCorrespondence` objects for `GraphElement` objects in the program graph, but the corresponding source file information is represented in terms of Eclipse resources and character offsets. For non-interactive human readable output this isn't enough. This utility converts Eclipse resources to standard File objects and relative file path strings, as well as converting resource character offsets to a line number or range of line numbers.

#### Pretty Printing Q and GraphElement Source Locations

	Analyzer analyzer = new DiscoverMainMethods(); // lazily instantiate Analyzer
	Q envelope = analyzer.getEnvelope(); // performs analysis
	FileWriter fw = new FileWriter(new File("output.txt"));
	if(envelope.eval().nodes().size() == 0){
	    fw.write("No valid Java main methods detected!");
	} else {
	    // valid main methods found, write the source file names 
	    // and line numbers of the results to the output file
	    FormattedSourceCorrespondence fsc = FormattedSourceCorrespondence.getSourceCorrespondents(envelope);
	    fw.write(fsc.toString());
	}
	fw.close();

Assuming the project you analyzed had one or more valid Java main methods, the output.txt file would contain something similar to the following.

	[Filename: ExampleProject\src\com\example\ExampleClass.java (line 5)]

A `Q` with multiple file and line number correspondents might look similar to the following.

	[Filename: ExampleProject\src\com\example\ExampleClass.java (lines 5-12, 15), 
	 Filename: ExampleProject\src\com\example\AnotherExampleClass.java (line 3)]

To get a `FormattedSourceCorrespondence` for a single `GraphElement` use the `FormattedSourceCorrespondence.getSourceCorrespondent(GraphElement ge)` method.