package org.openiec61850.scl;

import java.util.List;

import org.openiec61850.AccessPoint;

public class SclParser {
	public static List<AccessPoint> parse(String sclFilePath) throws SclParseException {
		SclParserObject sclParserObject = new SclParserObject();
		sclParserObject.parse(sclFilePath);
		return sclParserObject.getAccessPoints();
	}
}
