package com.student.env.utilities;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.*;

public class XmlWriter {

	private static Logger LOGGER = LogManager.getLogger(MethodHandles.lookup().lookupClass());

	public static void UpdateXMLNodeValue(List<List<String>> data, String sourceFilePath, String targetFilePath)
			throws Exception {

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(sourceFilePath);

		for (int iRow = 0; iRow < data.size(); iRow++) {
			String tagName = data.get(iRow).get(0);
			String childName = data.get(iRow).get(1);
			String value = data.get(iRow).get(2);

			// ge first element by tagName directly
			Node dataHeader = doc.getElementsByTagName(tagName).item(0);
			NamedNodeMap attritbute = dataHeader.getAttributes();
			Node nodeAttr = attritbute.getNamedItem(childName);
			if (nodeAttr != null) {
				nodeAttr.setTextContent(value);
			} else {
				// loop the child node
				NodeList list = dataHeader.getChildNodes();
				if (list != null && list.getLength() > 0) {
					for (int i = 0; i < list.getLength(); i++) {
						Node node = list.item(i);
						if (childName.equals(node.getNodeName())) {
							node.setTextContent(value);
							break;
						}
					}
				}
			}

		}

		// write content to xml file
		TransformerFactory tranFact = TransformerFactory.newInstance();
		Transformer trans = tranFact.newTransformer();
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(targetFilePath));
		trans.transform(source, result);
	}

	public static void UpdateDataHeaderFiles(List<List<String>> data, String sourceFilePath, String targetFilePath,
			boolean bFirstRecord) throws Exception {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(sourceFilePath);

		Node dataheaders = doc.getElementsByTagName("studentDataHeaders").item(0);
		Node dataheader = doc.getElementsByTagName("studentDataHeaders").item(0);
		Node newNode = dataheader;
		// if bFirstRecord= false then first node is copied and content
		// overwritten by "data" param
		// any node updated will br appended back in existing xml file
		// helpful in scenarios where multiple StudentItemsSku needs to be
		// manupulated in single file
		if (!bFirstRecord) {
			newNode = dataheader.cloneNode(true);

			for (int iRow = 0; iRow < data.size(); iRow++) {
				String childName = data.get(iRow).get(1);
				String val = data.get(iRow).get(2);

				// get first element by TAG name

				NamedNodeMap attribute = newNode.getAttributes();
				Node nodeAtt = attribute.getNamedItem(childName);
				if (nodeAtt != null) {
					nodeAtt.setTextContent(val);
				} else {
					// loop the child node
					NodeList list = newNode.getChildNodes();
					if (list != null && list.getLength() > 0) {
						for (int i = 0; i < list.getLength(); i++) {
							Node node = list.item(i);
							if (childName.equals(node.getNodeName())) {
								node.setTextContent(val);
								break;
							}
						}
					}
				}
			}

			dataheaders.appendChild(newNode);

			// write the content into xml file
			TransformerFactory tranFact = TransformerFactory.newInstance();
			Transformer trans = tranFact.newTransformer();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(new File(targetFilePath));
			trans.transform(source, result);

		}

	}
}
