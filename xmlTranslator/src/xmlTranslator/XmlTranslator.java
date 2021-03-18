package xmlTranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.snu.ids.kkma.ma.MorphemeAnalyzer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlTranslator
{

	public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException
	{
		File[] fileList = ReadFiles(); //���� �迭�� �� ���ϵ��� ����
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element docs = doc.createElement("docs");
		doc.appendChild(docs);
		
		
		for(int i =0;i<fileList.length;i++) //������ �ִ� ���� �� ��ŭ �ݺ�
		{
			Element docid = doc.createElement("doc");
			docid.setAttribute("id", Integer.toString(i));
			docs.appendChild(docid);
			
			Element title = doc.createElement("title");
			title.appendChild(doc.createTextNode(ReadTitle(fileList[i])));
			docid.appendChild(title);
			
			Element body = doc.createElement("body");
			body.appendChild(doc.createTextNode(ReadContents(fileList[i])));
			docid.appendChild(body);
		}
		
		XmlInstatiate(doc, "collection.xml"); //doc�� �������� xml���� ����
		XmlInstatiate(kkmaAnalyzer(), "index.xml"); //collection.xml������ �������� index.xml���� ����
		
	}
	
	static File[] ReadFiles() //������ ������ ���ϵ��� ���Ϲ迭�� �����Ͽ� ��ȯ�ϴ� �޼ҵ�
	{
		String path = "C:\\Users\\�賲��\\Documents\\Atelier\\Storage\\SimpleIR\\xmlTranslator\\src\\data"; //������ ����� ���� ��θ� ����
		File dir = new File(path);
		File []fileList = dir.listFiles();
		
		return fileList;
	}
	
	static File ReadFile(String path) //������ ����� ������ �ҷ���
	{
		File file = new File(path);

		return file;
	}
	
	static String ReadContents(File file) throws IOException //������ ������ �о ��ȯ�ϴ� �޼ҵ�
	{
		String a = "";

		FileInputStream input = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(input, "UTF-8");
		BufferedReader in = new BufferedReader(reader);
		
		String b;
		
		while((b=in.readLine()) != null)
		{
			a += b;
		}
		
		a = a.replaceFirst("<p>", "<FIrst>");
		a = replaceLast(a, "</p>", "<Last>");
		a = a.trim().replaceAll(" +", " "); //���ӵ� ������ �ϳ��� �������� ����
		String Contents = substringBetween(a, "<FIrst>", "<Last>"); //html�� ���븸 ����
		Contents = Contents.replaceAll("\\<.*?\\>", ""); //�±� ����
		
		return Contents;
	}
	
	static String ReadTitle(File file) throws IOException //������ �̸��� �о ��ȯ�ϴ� �޼ҵ�
	{
		String a = null;

		FileInputStream input = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(input, "UTF-8");
		BufferedReader in = new BufferedReader(reader);
		
		String b;
		
		while((b=in.readLine()) != null)
		{
			a += b;
		}
		
		String Title = substringBetween(a,"<title>","</title>"); //html�� ���� ����
		
		return Title;
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
	
	private static String substringBetween(String str, String open, String close) //Ư�� ���ڿ� ������ ���ڿ� ���� �޼ҵ�
	{
	    if (str == null || open == null || close == null) 
	    {
	       return null;
	    }
	    int start = str.indexOf(open);
	    if (start != -1)
	    {
	       int end = str.indexOf(close, start + open.length());
	       if (end != -1) 
	       {
	          return str.substring(start + open.length(), end);
	       }
	    }
	    return null;
	}
	
	private static String replaceLast(String string, String toReplace, String replacement) //Ư�� ���ڿ� �� ���� �������� ���� ���ڿ��� �����ϴ� �޼ҵ�
	{    
		int pos = string.lastIndexOf(toReplace);     

		if (pos > -1) 
			{        

		return string.substring(0, pos)+ replacement + string.substring(pos +   toReplace.length(), string.length());     

		    } 
		else 
		    { 
			   return string;     
		    } 

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

	private static Document kkmaAnalyzer() throws SAXException, IOException, ParserConfigurationException //Ư�� ����� ������ �������� ���¼Ҹ� �м��� Document��ü�� ����
	{
		File file = ReadFile("C:\\Users\\�賲��\\Documents\\Atelier\\Storage\\SimpleIR\\xmlTranslator\\src\\collection.xml"); //������ ����� ���
		
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
					if(nodeName == "title")
					{
						Node abc = ele.getFirstChild();
						
						Element title = doc.createElement(nodeName);
						title.appendChild(doc.createTextNode(abc.getNodeValue()));
						docid.appendChild(title);
					}
					else if(nodeName == "body")
					{
						Node abc = ele.getFirstChild();
						
						Element body = doc.createElement("body");
						body.appendChild(doc.createTextNode(MorAnalyze(abc.getNodeValue())));
						docid.appendChild(body);
					}
				}
			}
		}
		
		return doc;
	}

}
