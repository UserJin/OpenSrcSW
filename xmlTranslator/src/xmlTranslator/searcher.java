package xmlTranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.snu.ids.kkma.index.Keyword;
import org.snu.ids.kkma.index.KeywordExtractor;
import org.snu.ids.kkma.index.KeywordList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import xmlTranslator.indexer.IdFreq;
import xmlTranslator.indexer.IdWeight;

public class searcher
{

	public searcher(String path, String query) throws ClassNotFoundException, IOException, ParserConfigurationException, SAXException
	{
		ArrayList<KeyWeight> qArr = MorAnalyze(query); //입력받은 querry를 형태소와 문장내의 빈도수로 저장함
		CalcSim(path, qArr);  //저장된 데이터를 바탕으로 유사도와 상위 3문서의 제목을 출력함
		
	}
	
	static void CalcSim(String path, ArrayList<KeyWeight> arr) throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException //주어진 querry와 각 문서의 유사도를 출력하고 정렬하여 상위 3문서의 제목을 출력하는 메소드
	{
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		Object object = objectInputStream.readObject();
		objectInputStream.close();
		
		HashMap hashMap = (HashMap)object;
		
		float[] sim = new float[5];
		for(int i=0;i<arr.size();i++)
		{
			float[] indexSim = new float[5];
			if(hashMap.containsKey(arr.get(i).GetKey()))
			{
				ArrayList<IdWeight> idwList = (ArrayList<IdWeight>) hashMap.get(arr.get(i).GetKey()); 
				for(int j=0;j< idwList.size();j++)
				{
					indexSim[idwList.get(j).GetId()] = idwList.get(j).GetWeight();
				}
			}
			for(int j =0;j<sim.length;j++)
			{
		        sim[j] += indexSim[j] * arr.get(i).GetWeight();
			}
		}
		
		for(int i =0;i<sim.length;i++)
		{
			System.out.println(i + "번 문서의 유사성 : " + sim[i]);
		}
		
		PrintTitle(Sort(sim));
	}
	
	public static void PrintTitle(IdWeight[] arr) throws ParserConfigurationException, SAXException, IOException //정렬된 유사도를 바탕으로 3순위 까지의 문서의 제목을 출력함
	{
		String[] title = new String[5];
		
		File file = new File("src/index.xml"); //index.xml파일을 바탕으로 정보를 추출, 절대 경로를 사용함
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		
		Document document = documentBuilder.parse(file);	
		
		Element root = document.getDocumentElement();
		
		NodeList childrenDocs = root.getChildNodes();
		for(int i =0;i<childrenDocs.getLength();i++)
		{
			NodeList childrenId = childrenDocs.item(i).getChildNodes();
			
			Node node = childrenId.item(0); // item(0) = title, item(1) = body
			Element ele = (Element)node;
			Node abc = ele.getFirstChild();
			title[i] = abc.getNodeValue(); //title의 value를 title[]에 저장함
		}
		
		for(int i =0;i<3;i++) //3순위까지의 제목을 출력함
		{
			System.out.println((i+1) + "순위 " + title[arr[i].GetId()]);
		}
	}
	
	private static ArrayList<KeyWeight> MorAnalyze(String str) //특정 문자열을 받아 형태소로 나눈뒤 정해진 형식으로 바꾸는 메소드
	{
		ArrayList<KeyWeight> arr = new ArrayList<>();
		
		KeywordExtractor key = new KeywordExtractor();
		KeywordList ki = key.extractKeyword(str, true);
		
		for(int i= 0;i<ki.size();i++)
		{
			Keyword kwrd = ki.get(i);
			
			arr.add(new KeyWeight(kwrd.getString(), 1));
		}
		
		return arr;
	}
	
	static class KeyWeight
	{
		private String key;
		private int weight;
		
		public KeyWeight(String k, int w)
		{
			key = k;
			weight = w;
		}
		
		public String GetKey()
		{
			return key;
		}
		
		public int GetWeight()
		{
			return weight;
		}
	}
	
	public static IdWeight[] Sort(float[] arr)
	{
		IdWeight[] temp = new IdWeight[arr.length];
		
		for(int i=0;i<arr.length;i++)
		{
			temp[i] = new IdWeight(i, arr[i]);
		}
		
		for(int i =0;i<temp.length-1;i++)
		{
			for(int j =i+1;j<temp.length;j++)
			{
				if(temp[i].GetWeight() < temp[j].GetWeight())
				{
					IdWeight k = temp[i];
					temp[i] = temp[j];
					temp[j] = k;
				}
				else if(temp[i].GetWeight() == temp[j].GetWeight())
				{
					if(temp[i].GetId() > temp[j].GetId())
					{
						IdWeight k = temp[i];
						temp[i] = temp[j];
						temp[j] = k;
					}
				}
			}
		}
		
		return temp;
	}
	
	/*
	static class IDWeight
	{
		private int id;
		private float weight;
		
		public IDWeight(int i, float w)
		{
			id = i;
			weight = w;
		}
		
		public int GetID()
		{
			return id;
		}
		
		public float GetWeight()
		{
			return weight;
		}
	}
	*/
}


