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

import xmlTranslator.indexer.IdWeight;

public class searcher
{

	public searcher(String path, String query) throws ClassNotFoundException, IOException, ParserConfigurationException, SAXException
	{
		ArrayList<KeyWeight> qArr = MorAnalyze(query); //�Է¹��� querry�� ���¼ҿ� ���峻�� �󵵼��� ������
		ReadSim(path, qArr);  //����� �����͸� �������� ���絵�� ���� 3������ ������ �����
		
	}
	
	static void ReadSim(String path, ArrayList<KeyWeight> arr) throws IOException, ClassNotFoundException, ParserConfigurationException, SAXException //�־��� querry�� �� ������ ���絵�� ����ϰ� �����Ͽ� ���� 3������ ������ ����ϴ� �޼ҵ�
	{
		FileInputStream fileInputStream = new FileInputStream(path);
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		Object object = objectInputStream.readObject();
		objectInputStream.close();
		
		HashMap hashMap = (HashMap)object;
		//arr = �ܾ�� �ܾ��� ����ġ
		/*
		float[] sim = new float[5]; //�� �������� ������ ���絵�� ��� �迭
		for(int i=0;i<arr.size();i++)
		{
			float[] indexSim = new float[5]; //Ư�� �ܾ��� ������ ����ġ�� �����ϴ� �Լ�
			if(hashMap.containsKey(arr.get(i).GetKey())) //�ؽ��ʿ� Ư�� �ܾ ���ԵǾ� ���� ���
			{
				ArrayList<IdWeight> idwList = (ArrayList<IdWeight>) hashMap.get(arr.get(i).GetKey()); //Ư�� �ܾ��� ������ ����ġ ����Ʈ�� ������
				for(int j=0;j< idwList.size();j++)
				{
					indexSim[idwList.get(j).GetId()] = idwList.get(j).GetWeight();
				}
			}
			for(int j =0;j<sim.length;j++)
			{
		        sim[j] += indexSim[j] * arr.get(i).GetWeight(); //�ܾ� ����ġ * ���� ����ġ
			}
		}
		*/
		
		//�Ʒ��� ���� �ۼ�
		
		ArrayList<WWeDWe>[] docSim = new ArrayList[5];
		
		for(int i =0;i<docSim.length;i++)
		{
			docSim[i] = new ArrayList<WWeDWe>();
		}
		for(int i=0 ;i<arr.size();i++)
		{
			float[] indexSim = new float[5]; //Ư�� �ܾ��� ������ ����ġ�� �����ϴ� �Լ�
			if(hashMap.containsKey(arr.get(i).GetKey())) //�ؽ��ʿ� Ư�� �ܾ ���ԵǾ� ���� ���
			{
				ArrayList<IdWeight> idwList = (ArrayList<IdWeight>) hashMap.get(arr.get(i).GetKey()); //Ư�� �ܾ��� ������ ����ġ ����Ʈ�� ������
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
		PrintWeight(docSim);
		
		/*
		for(int i =0;i<sim.length;i++)
		{
			System.out.println(i + "�� ������ ���絵 : " + sim[i]);
		}
		*/
		
		//PrintTitle(Sort(sim));
	}
	
	public static void CalcSim(ArrayList<WWeDWe>[] arr) //�ܾ��� ����ġ�� ���� �� ����ġ�� �������� �ڻ��� ���絵�� ���
	{
		float[] cosSim = new float[arr.length]; //�ڻ��� ���絵�� ���� float�迭
		for(int i=0;i<arr.length;i++) //����Ʈ �迭�� ���� ��ŭ �ݺ���
		{
			float sizeWord = 0;
			float sizeDoc = 0;
			float denom;
			for(int j=0;j<arr[i].size();j++)
			{
				sizeWord += Math.pow(arr[i].get(j).GetWordWeight(), 2); //�� �ܾ� ����ġ�� ������ Sum
				sizeDoc += Math.pow(arr[i].get(j).GetDocWeight(), 2); //�� ���� ����ġ�� ������ Sum
			}
			denom = (float) Math.sqrt(sizeWord) + (float) Math.sqrt(sizeDoc);
			cosSim[i] = 1f / denom; // 1f = InnerProduct
		}
	}
	
	public static void PrintWeight(ArrayList<WWeDWe>[] arr) //����� �� ������ ���絵�� ����ϴ� �޼ҵ�
	{
		float[] sumWeight = new float[arr.length];
		
		for(int i =0;i<sumWeight.length;i++)
		{
			for(int j=0;j<arr[i].size();j++)
			{
				sumWeight[i] += arr[i].get(j).GetWordWeight() * arr[i].get(j).GetDocWeight();
			}
			System.out.println(i + "�� ������ ���絵 : " + sumWeight[i]);
		}
	}
	
	public static void PrintTitle(IdWeight[] arr) throws ParserConfigurationException, SAXException, IOException //���ĵ� ���絵�� �������� 3���� ������ ������ ������ ����ϴ� �޼ҵ�
	{
		String[] title = new String[5];
		
		File file = new File("src/index.xml"); //index.xml������ �������� ������ ����, ���� ��θ� �����
		
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
			title[i] = abc.getNodeValue(); //title�� value�� title[]�� ������
		}
		
		for(int i =0;i<3;i++) //3���������� ������ �����
		{
			System.out.println((i+1) + "���� " + title[arr[i].GetId()]);
		}
	}
	
	private static ArrayList<KeyWeight> MorAnalyze(String str) //Ư�� ���ڿ��� �޾� ���¼ҷ� ������ ������ �������� �ٲٴ� �޼ҵ�
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
	
	public static IdWeight[] Sort(float[] arr) //����ġ�� ��Ҹ� ���Ͽ� ū ���� �տ� ������ ������, ���� ������ ID��ȣ�� ���� ���� �տ� ������ ����
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


