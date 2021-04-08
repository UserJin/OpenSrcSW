package xmlTranslator;

import java.io.File;
import java.io.FileInputStream;
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

import xmlTranslator.indexer.IdWeight;

public class searcher
{

	public searcher(String path, String query) throws ClassNotFoundException, IOException, ParserConfigurationException, SAXException
	{
		ArrayList<KeyWeight> qArr = MorAnalyze(query); //입력받은 querry를 형태소와 문장내의 빈도수로 저장함
		ReadSim(path, qArr);  //저장된 데이터를 바탕으로 유사도와 상위 3문서의 제목을 출력함
		
	}
	
	static void ReadSim(String path, ArrayList<KeyWeight> arr) throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException //주어진 querry와 각 문서의 유사도를 출력하고 정렬하여 상위 3문서의 제목을 출력하는 메소드
	{
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		Object object = objectInputStream.readObject();
		objectInputStream.close();
		
		HashMap hashMap = (HashMap)object;

		ArrayList<WWeDWe>[] docSim = new ArrayList[5]; //단어의 가중치와 문서 내 가중치를 담는 리스트 배열, 문서가 5개이므로 배열의 길이도 5개
		
		for(int i =0;i<docSim.length;i++)
		{
			docSim[i] = new ArrayList<WWeDWe>();
		}
		for(int i=0 ;i<arr.size();i++)
		{
			float[] indexSim = new float[5]; //특정 단어의 문서별 가중치를 저장하는 함수
			if(hashMap.containsKey(arr.get(i).GetKey())) //해쉬맵에 특정 단어가 포함되어 있을 경우
			{
				ArrayList<IdWeight> idwList = (ArrayList<IdWeight>) hashMap.get(arr.get(i).GetKey()); //특정 단어의 문서별 가중치 리스트를 가져옴
				for(int j=0;j< idwList.size();j++)
				{
					indexSim[idwList.get(j).GetId()] = idwList.get(j).GetWeight();
				}
			}
			for(int j=0;j<docSim.length;j++)
			{
				docSim[j].add(new WWeDWe(arr.get(i).GetWeight(), indexSim[j]));
			}
		}
		CalcSim(docSim); //가중치를 바탕으로 유사도 계산
	}
	
	public static void CalcSim(ArrayList<WWeDWe>[] arr) throws ParserConfigurationException, SAXException, IOException //단어의 가중치와 문서 내 가중치를 바탕으로 코사인 유사도를 계산
	{
		float[] cosSim = new float[arr.length]; //코사인 유사도를 담을 float배열
		float[] innerSim = InnerProduct(arr); //내적치
		for(int i=0;i<arr.length;i++) //리스트 배열의 길이 만큼 반복함
		{
			float sizeWord = 0;
			float sizeDoc = 0;
			float denom;
			for(int j=0;j<arr[i].size();j++)
			{
				sizeWord += Math.pow(arr[i].get(j).GetWordWeight(), 2); //각 단어 가중치의 제곱의 Sum
				sizeDoc += Math.pow(arr[i].get(j).GetDocWeight(), 2); //각 문서 가중치의 제곱의 Sum
			}
			denom = (float) Math.sqrt(sizeWord) + (float) Math.sqrt(sizeDoc); //
			cosSim[i] = innerSim[i] / denom; // 1f = InnerProduct
		}
		
		for(int i=0;i<cosSim.length;i++)
		{
			System.out.println(i+"번 문서의 유사도 : " + String.format("%.2f", cosSim[i])); //코사인 유사도를 소수점 2자리 까지 출력함
		}
		
		PrintTitle(Sort(cosSim)); //계산된 가중치를 정렬하고 출력
	}
	
	public static float[] InnerProduct(ArrayList<WWeDWe>[] arr) //가중치를 바탕으로 내적을 계산하여 그 값의 배열을 반환
	{
		float[] innerSim = new float[arr.length];
		for(int i =0;i<innerSim.length;i++)
		{
			for(int j =0;j<arr[i].size();j++)
			{
				innerSim[i] += arr[i].get(j).GetWordWeight() *  arr[i].get(j).GetDocWeight(); //내적 계산
			}
		}
		
		return innerSim;
	}
	
	public static void PrintTitle(IdWeight[] arr) throws ParserConfigurationException, SAXException, IOException //정렬된 유사도를 바탕으로 3순위 까지의 문서의 제목을 출력하는 메소드
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
			if(arr[i].GetWeight() != 0) //가중치가 만약 0이 아닐 경우만 출력함
			{
				System.out.println((i+1) + "순위 " + title[arr[i].GetId()]);
			}
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
			arr.add(new KeyWeight(kwrd.getString(), 1f));

		}
		
		return arr;
	}
	
	static class KeyWeight
	{
		private String key;
		private float weight;
		
		public KeyWeight(String k, float w)
		{
			key = k;
			weight = w;
		}
		
		public String GetKey()
		{
			return key;
		}
		

		public float GetWeight()
		{
			return weight;
		}
	}
	
	static class WWeDWe
	{
		private float wordWeight;
		private float docWeight;
		
		public WWeDWe(float word, float doc)
		{
			wordWeight = word;
			docWeight = doc;
		}
		
		public float GetWordWeight()
		{
			return wordWeight;
		}
		public float GetDocWeight()
		{
			return docWeight;
		}
	}
	
	public static IdWeight[] Sort(float[] arr) //가중치의 대소를 비교하여 큰 것이 앞에 오도록 정렬함, 값이 같으면 ID번호가 빠른 것이 앞에 오도록 정렬
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
}


