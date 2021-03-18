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
		File[] fileList = ReadFiles(); //파일 배열에 각 파일들을 저장
		
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		
		Document doc = docBuilder.newDocument();
		
		Element docs = doc.createElement("docs");
		doc.appendChild(docs);
		
		
		for(int i =0;i<fileList.length;i++) //폴더에 있는 문서 수 만큼 반복
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
		
		XmlInstatiate(doc, "collection.xml"); //doc를 바탕으로 xml문서 생성
		XmlInstatiate(kkmaAnalyzer(), "index.xml"); //collection.xml문서를 바탕으로 index.xml문서 생성
		
	}
	
	static File[] ReadFiles() //지정된 폴더의 파일들을 파일배열에 저장하여 반환하는 메소드
	{
		String path = "C:\\Users\\김남진\\Documents\\Atelier\\Storage\\SimpleIR\\xmlTranslator\\src\\data"; //파일이 저장된 폴더 경로를 설정
		File dir = new File(path);
		File []fileList = dir.listFiles();
		
		return fileList;
	}
	
	static File ReadFile(String path) //지정된 경로의 파일을 불러옴
	{
		File file = new File(path);

		return file;
	}
	
	static String ReadContents(File file) throws IOException //파일의 내용을 읽어서 반환하는 메소드
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
		a = a.trim().replaceAll(" +", " "); //연속된 공백을 하나의 공백으로 변경
		String Contents = substringBetween(a, "<FIrst>", "<Last>"); //html의 내용만 추출
		Contents = Contents.replaceAll("\\<.*?\\>", ""); //태그 제거
		
		return Contents;
	}
	
	static String ReadTitle(File file) throws IOException //파일의 이름을 읽어서 반환하는 메소드
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
		
		String Title = substringBetween(a,"<title>","</title>"); //html의 제목만 추출
		
		return Title;
	}

 	static void XmlInstatiate(Document doc, String name) throws TransformerException, FileNotFoundException //xml파일 생성기
	{
		TransformerFactory transformerFactory =TransformerFactory.newInstance();
		
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(new File("src/" + name)));
		
		transformer.transform(source, result);
		
	}
	
	private static String substringBetween(String str, String open, String close) //특정 문자열 사이의 문자열 추출 메소드
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
	
	private static String replaceLast(String string, String toReplace, String replacement) //특정 문자열 중 가장 마지막에 나온 문자열을 변경하는 메소드
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

	private static String MorAnalyze(String str) //특정 문자열을 받아 형태소로 나눈뒤 정해진 형식으로 바꾸는 메소드
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

	private static Document kkmaAnalyzer() throws SAXException, IOException, ParserConfigurationException //특정 경로의 파일을 바탕으로 형태소를 분석한 Document객체를 생성
	{
		File file = ReadFile("C:\\Users\\김남진\\Documents\\Atelier\\Storage\\SimpleIR\\xmlTranslator\\src\\collection.xml"); //파일이 저장된 경로
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		
		Document document = documentBuilder.parse(file);	
		Document doc = documentBuilder.newDocument(); //반환용 Document 객체
		
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
