package com.ensoftcorp.open.commons.utilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ensoftcorp.atlas.core.db.graph.Node;
import com.ensoftcorp.atlas.core.index.common.SourceCorrespondence;
import com.ensoftcorp.atlas.core.xcsg.XCSG;
import com.ensoftcorp.open.commons.analysis.CommonQueries;
import com.ensoftcorp.open.commons.preferences.CommonsPreferences;

/**
 * Counts source lines of code as logical lines of code, differentiating between blank lines, comments, and code
 * 
 * Jimple is counted manually, assuming comments do no exist, and a non blank line is code
 * All other language delagate line counting to the cloc tool: https://github.com/AlDanial/cloc
 * 
 * @author Ben Holland
 */
public class SLOCCount {

	public static class LinesOfCode {
		private String language;
		private long blank;
		private long comment;
		private long code;
		
		public LinesOfCode(String language, long blank, long comment, long code) {
			this.language = language;
			this.blank = blank;
			this.comment = comment;
			this.code = code;
		}

		public String getLanguage() {
			return language;
		}
		
		public long getBlank() {
			return blank;
		}

		public long getComment() {
			return comment;
		}

		public long getCode() {
			return code;
		}

		@Override
		public String toString() {
			return "LoC [language=" + language + ", blank=" + blank + ", comment=" + comment + ", code=" + code + "]";
		}
	}
	
	public static LinesOfCode count(Node function) throws IOException, ParseException {
		// sort the cfg nodes of the function by source correspondence
		ArrayList<Node> cfgNodes = new ArrayList<Node>();
		for(Node cfgNode : CommonQueries.cfg(function).eval().nodes()) {
			cfgNodes.add(cfgNode);
		}
		Collections.sort(cfgNodes, new NodeSourceCorrespondenceSorter());
		
		// get the starting offset
		SourceCorrespondence startSC = (SourceCorrespondence) function.getAttr(XCSG.sourceCorrespondence);
		long startOffset = startSC.offset;
		
		SourceCorrespondence endSC = (SourceCorrespondence) cfgNodes.get(cfgNodes.size()-1).getAttr(XCSG.sourceCorrespondence);
		long endOffset = endSC.offset + endSC.length;
				
		// jimple is a special case, need to count manually
		if(function.taggedWith(XCSG.Language.Jimple)) {
			long blank = 0;
			long comment = 0;
			long code = 0;
			
			// read the corresponding bytes between the start and end file offsets
			File sourceFile = startSC.sourceFile.getLocation().toFile();
			RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
			raf.seek(startOffset);
			while(raf.getFilePointer() < endOffset) {
				String line = raf.readLine();
				if(line.trim().isEmpty()) {
					blank++;
				} else {
					code++;
				}
			}
			raf.close();
			return new LinesOfCode("Jimple", blank, comment, code);
		} 
		
		// read the corresponding bytes between the start and end file offsets
		File sourceFile = startSC.sourceFile.getLocation().toFile();
		StringBuilder contents = new StringBuilder();
		RandomAccessFile raf = new RandomAccessFile(sourceFile, "r");
		raf.seek(startOffset);
		while(raf.getFilePointer() < endOffset) {
			contents.append(new String(raf.readLine() + "\n"));
		}
		raf.close();
		
		// write the contents to a temporary file and delegate line counting to cloc
		File tempFile;
		String language;
		if(function.taggedWith(XCSG.Language.CPP)) {
			tempFile = File.createTempFile(function.getAttr(XCSG.name).toString(), ".cpp");
			language = "C++";
		} else if(function.taggedWith(XCSG.Language.C)) {
			tempFile = File.createTempFile(function.getAttr(XCSG.name).toString(), ".c");
			language = "C";
		} else if(function.taggedWith(XCSG.Language.Java)) {
			tempFile = File.createTempFile(function.getAttr(XCSG.name).toString(), ".java");
			language = "Java";
		} else {
			throw new RuntimeException("Unsupported Language Type");
		}
		FileWriter fw = new FileWriter(tempFile);
		fw.write(contents.toString());
		fw.close();
		
		// run cloc on the temporary file
		Runtime rt = Runtime.getRuntime();
		String[] commands = { CommonsPreferences.getClocPath().getAbsolutePath() , "--quiet", "--json", tempFile.getAbsolutePath()};
		Process proc = rt.exec(commands);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

		// read the output from the command
		StringBuilder jsonString = new StringBuilder();
		String line = null;
		while ((line = stdInput.readLine()) != null) {
			jsonString.append(line);
		}
		
		// parse cloc result
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject) parser.parse(jsonString.toString());
		JSONObject counts = (JSONObject) json.get("SUM");
		long blank = (long)counts.get("blank");
		long comment = (long)counts.get("comment");
		long code = (long)counts.get("code");
		LinesOfCode result = new LinesOfCode(language, blank, comment, code);

		// clean up the temporary file and return result
		tempFile.delete();
		return result;
	}
	
}
