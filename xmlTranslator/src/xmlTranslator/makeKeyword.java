package xmlTranslator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class makeKeyword 
{
	public makeKeyword(String path) throws FileNotFoundException, TransformerException, SAXException, IOException, ParserConfigurationException
	{
		XmlInstatiate(kkmaAnalyzer(path), "index.xml");
	}
	
	private static Document kkmaAnalyzer(String path) throws SAXException, IOException, ParserConfigurationException //Ư�� ����� ������ �������� ���¼Ҹ� �м��� Document��ü�� ����
	{
		File file = new File(path); //������ ����� ���
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		
		Document document = documentBuilder.parse(file);	
		Document doc = documentBuilder.newDocument(); //��ȯ�� Document ��ü
		
		Element root = document.getDocumentElement(); //docs
		
		Element docs = doc.createElement(root.getNodeName());
		doc.appendChild(docs);

		NodeList childrenDocs = root.getChildNodes(); //docid 1~5
		for(int i =0;i<childrenDocs.getLength();i++)
		{
			Node idNode = childrenDocs.item(i);
			Element idEle = (Element)idNode;

			Element docid = doc.createElement(idNode.getNodeName());
			docid.setAttribute("id", idEle.getAttribute("id"));
			docs.appendChild(docid);
			
			NodeList childrenId = childrenDocs.item(i).getChildNodes();

			for(int j =0;j<childrenId.getLength();j++)
			{
				Node node = childrenId.item(j);
				if(node.getNodeType() == Node.ELEMENT_NODE)
				{
					Element ele = (Element)node;
					String nodeName = ele.getNodeName();
					if(nodeName == "title") //�ڽĳ���� �̸��� title�� ���
					{
						Node abc = ele.getFirstChild();
						
						Element title = doc.createElement(nodeName);
						title.appendChild(doc.createTextNode(abc.getNodeValue()));
						docid.appendChild(title);
					}
					else if(nodeName == "body") //�ڽĳ���� �̸��� body�� ���
					{
						Node abc = ele.getFirstChild();
						
						Element body = doc.createElement("body");
						body.appendChild(doc.createTextNode(MorAnalyze(abc.getNodeValue()))); //body�� ������ ���¼ҷ� ������ ����
						docid.appendChild(body);
					}
				}
			}
		}
		
		return doc;
	}
	
	private static String MorAnalyze(String str) //Ư�� ���ڿ��� �޾� ���¼ҷ� ������ ������ �������� �ٲٴ� �޼ҵ�
	{
		String content = "";
		
		KeywordExtractor key = new KeywordExtractor();
		KeywordList ki = key.extractKeyword(str, true);
		
		for(int i= 0;i<ki.size();i++)
		{
			Keyword kwrd = ki.get(i);
			content += kwrd.getString() + ":" + kwrd.getCnt() + "#";
		}
		
		return content;
	}

 	static void XmlInstatiate(Document doc, String name) throws TransformerException, FileNotFoundException //xml���� ������
	{
		TransformerFactory transformerFactory =TransformerFactory.newInstance();
		
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(new File("src/" + name)));
		
		transformer.transform(source, result);
	}

}
