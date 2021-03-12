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

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlTranslator
{

	public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException
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
		
		XmlInstatiate(doc); //doc를 바탕으로 xml문서 생성
	}
	
	static File[] ReadFiles() //지정된 폴더의 파일들을 파일배열에 저장하여 반환하는 메소드
	{
		String path = "C:\\Users\\김남진\\Documents\\Atelier\\Storage\\SimpleIR\\Food"; //파일이 저장된 폴더 경로를 설정
		File dir = new File(path);
		File []fileList = dir.listFiles();
		
		return fileList;
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

 	static void XmlInstatiate(Document doc) throws TransformerException, FileNotFoundException //xml파일 생성기
	{
		TransformerFactory transformerFactory =TransformerFactory.newInstance();
		
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new FileOutputStream(new File("src/collection.xml")));
		
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


}
