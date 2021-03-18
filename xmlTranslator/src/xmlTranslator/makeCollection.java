package xmlTranslator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class makeCollection 
{
	public makeCollection(String path) throws ParserConfigurationException, DOMException, IOException, TransformerException
	{
		File[] fileList = ReadFiles(path); //���� �迭�� �� ���ϵ��� ����
		
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
	}
	
	
	static File[] ReadFiles(String path) //������ ������ ���ϵ��� ���Ϲ迭�� �����Ͽ� ��ȯ�ϴ� �޼ҵ�
	{
		File dir = new File(path);
		File []fileList = dir.listFiles();
		
		return fileList;
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
	
}