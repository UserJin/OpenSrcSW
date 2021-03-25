package xmlTranslator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class indexer
{
	public indexer(String path) throws ParserConfigurationException, SAXException, IOException, ClassNotFoundException
	{
		makePost(makeHashMap(path));
		ReadPost();
	}
	
	static HashMap makeHashMap(String path) throws ParserConfigurationException, SAXException, IOException
	{
		HashMap<String, ArrayList<IdFreq>> frequencyMap = new HashMap<>(); //단어를 keyword로, id와 빈도수로 이루어진 객체를 value로 갖는 HashMap객체 생성
		HashMap<String, ArrayList<IdWeight>> indexMap = new HashMap<>(); //단어를 keyword로, id와 가중치로 이루어진 객체를 value로 갖는 HashMap객체 생성
		
		File file = new File(path); //index.xml파일을 바탕으로 정보를 추출
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = factory.newDocumentBuilder();
		
		Document document = documentBuilder.parse(file);	
		
		Element root = document.getDocumentElement();
		
		NodeList childrenDocs = root.getChildNodes();
		for(int i =0;i<childrenDocs.getLength();i++)
		{
			Node idNode = childrenDocs.item(i);
			Element idEle = (Element)idNode;
			int id = Integer.parseInt(idEle.getAttribute("id"));

			NodeList childrenId = childrenDocs.item(i).getChildNodes();
			
			Node node = childrenId.item(1); // item(0) = title, item(1) = body
			Element ele = (Element)node;
			Node abc = ele.getFirstChild();
			String[] bodyValue = abc.getNodeValue().split("#"); //body의 내용을 #으로 구분하여 자름
			
			for(int j = 0;j<bodyValue.length;j++) //bodyValue = 잘려진 "단어:빈도수" 형태의 String 배열
			{
				String[] keySplit = bodyValue[j].split(":"); //:으로 구분하여 단어와 빈도수로 자름
				
				if(frequencyMap.containsKey(keySplit[0])) //만약 단어가 이미 frequencyMap에 존재할 경우
				{
					ArrayList<IdFreq> list = frequencyMap.get(keySplit[0]); //기존의 list를 불러옴
					list.add(new IdFreq(id,Integer.parseInt(keySplit[1]))); //list에 새로운 객체를 추가함
					frequencyMap.put(keySplit[0], list); //변경된 list를 다시 frequencyMap에 넣음
				}
				else //처음 나오는 단어일 경우
				{
					ArrayList<IdFreq> list = new ArrayList<>();
					list.add(new IdFreq(id,Integer.parseInt(keySplit[1])));
					frequencyMap.put(keySplit[0], list);
				}
			}
		}
		
		//frequencyMap을 바탕으로 IndexMap을 생성함
		
        Iterator<String> keys = frequencyMap.keySet().iterator();
        while( keys.hasNext() )
        {
            String key = keys.next();
            ArrayList<IdFreq> value = frequencyMap.get(key);
            for(int i =0;i < value.size();i++)
            {
            	float weightValue = WeightCal(value.size(), value.get(i).GetFrequency());
            	IdWeight wValue = new IdWeight(value.get(i).GetId(), weightValue);
            	
            	if(indexMap.containsKey(key)) //단어가 이미 indexMap에 존재할 경우
            	{
            		ArrayList<IdWeight> list = indexMap.get(key); //기존의 list를 불러옴
            		list.add(wValue); //list에 새로운 객체를 추가함
            		indexMap.put(key, list); //변경된 list를 다시 indexMap에 넣음
            	}
            	else //처음 나오는 단어일 경우
            	{
            		ArrayList<IdWeight> list = new ArrayList<>();
            		list.add(wValue);
            		indexMap.put(key, list);
            	}
            }
        }      
        return indexMap;
	}
	
	static void makePost(HashMap map) throws IOException //HashMap을 바탕으로 post파일을 만드는 함수
	{
		FileOutputStream fileStream = new FileOutputStream("src/index.post");
		
		ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileStream);
		
		HashMap indexMap = map;
		
		objectOutputStream.writeObject(indexMap);
		
		objectOutputStream.close();
	}
	
	static void ReadPost() throws IOException, ClassNotFoundException
	{
		FileInputStream fileInputStream = new FileInputStream("src/index.post");
		ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
		
		Object object = objectInputStream.readObject();
		objectInputStream.close();
		
		System.out.println("읽어온 객체의 type → " + object.getClass());
		
		HashMap hashMap = (HashMap)object;
		
        Iterator<String> keys = hashMap.keySet().iterator();
        while(keys.hasNext())
        {
            String key = keys.next();
            ArrayList<IdWeight> valueList = (ArrayList<IdWeight>) hashMap.get(key);   
            String value = "";
            for(int i =0;i<valueList.size();i++)
            {
            	value += valueList.get(i);
            }
            System.out.println(key + " → " + value);
        }
	}
	
	static class IdFreq //단어가 존재하는 문서의 id와 단어의 빈도수를 가지는 객체
	{
		private int id; //문서 id
		private int frequency; //빈도수
		
		public IdFreq(int id2, int freq)
		{
			id = id2;
			frequency = freq;
		}
		
		public String toString()
		{
			return Integer.toString(id) + " " + Integer.toString(frequency) + " / ";
		}
		
		public int GetId() //객체의 id를 반환
		{
			return id;
		}
		
		public int GetFrequency() //객체의 빈도수를 반환
		{
			return frequency;
		}


	}
	
	static class IdWeight implements Serializable //단어가 존재하는 문서의 id와 가중치를 가지는 객체
	{
		int id; //문서의 id
		float weight; //문서내에서 단어의 가중치
		
		public IdWeight(int id2, float wei)
		{
			id = id2; 
			weight = wei;
		}
		
		public String toString()
		{
        	String num = String.format("%.2f", weight); //가중치를 소수점 2자리까지만 표시
			
			return Integer.toString(id) + " " + num + " ";
		}
		

	}
	
	static public float WeightCal(int docSum, int frequency) //단어가 존재하는 문서의 갯수와 특정 문서에서의 빈도수를 통해 가중치를 계산함
	{
		return frequency * (float)(Math.log(5f/docSum)); // 빈도수 * 자연로그(전체 문서 수 / 등장 문서 수)
	}
}
