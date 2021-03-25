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
		HashMap<String, ArrayList<IdFreq>> frequencyMap = new HashMap<>(); //�ܾ keyword��, id�� �󵵼��� �̷���� ��ü�� value�� ���� HashMap��ü ����
		HashMap<String, ArrayList<IdWeight>> indexMap = new HashMap<>(); //�ܾ keyword��, id�� ����ġ�� �̷���� ��ü�� value�� ���� HashMap��ü ����
		
		File file = new File(path); //index.xml������ �������� ������ ����
		
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
			String[] bodyValue = abc.getNodeValue().split("#"); //body�� ������ #���� �����Ͽ� �ڸ�
			
			for(int j = 0;j<bodyValue.length;j++) //bodyValue = �߷��� "�ܾ�:�󵵼�" ������ String �迭
			{
				String[] keySplit = bodyValue[j].split(":"); //:���� �����Ͽ� �ܾ�� �󵵼��� �ڸ�
				
				if(frequencyMap.containsKey(keySplit[0])) //���� �ܾ �̹� frequencyMap�� ������ ���
				{
					ArrayList<IdFreq> list = frequencyMap.get(keySplit[0]); //������ list�� �ҷ���
					list.add(new IdFreq(id,Integer.parseInt(keySplit[1]))); //list�� ���ο� ��ü�� �߰���
					frequencyMap.put(keySplit[0], list); //����� list�� �ٽ� frequencyMap�� ����
				}
				else //ó�� ������ �ܾ��� ���
				{
					ArrayList<IdFreq> list = new ArrayList<>();
					list.add(new IdFreq(id,Integer.parseInt(keySplit[1])));
					frequencyMap.put(keySplit[0], list);
				}
			}
		}
		
		//frequencyMap�� �������� IndexMap�� ������
		
        Iterator<String> keys = frequencyMap.keySet().iterator();
        while( keys.hasNext() )
        {
            String key = keys.next();
            ArrayList<IdFreq> value = frequencyMap.get(key);
            for(int i =0;i < value.size();i++)
            {
            	float weightValue = WeightCal(value.size(), value.get(i).GetFrequency());
            	IdWeight wValue = new IdWeight(value.get(i).GetId(), weightValue);
            	
            	if(indexMap.containsKey(key)) //�ܾ �̹� indexMap�� ������ ���
            	{
            		ArrayList<IdWeight> list = indexMap.get(key); //������ list�� �ҷ���
            		list.add(wValue); //list�� ���ο� ��ü�� �߰���
            		indexMap.put(key, list); //����� list�� �ٽ� indexMap�� ����
            	}
            	else //ó�� ������ �ܾ��� ���
            	{
            		ArrayList<IdWeight> list = new ArrayList<>();
            		list.add(wValue);
            		indexMap.put(key, list);
            	}
            }
        }      
        return indexMap;
	}
	
	static void makePost(HashMap map) throws IOException //HashMap�� �������� post������ ����� �Լ�
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
		
		System.out.println("�о�� ��ü�� type �� " + object.getClass());
		
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
            System.out.println(key + " �� " + value);
        }
	}
	
	static class IdFreq //�ܾ �����ϴ� ������ id�� �ܾ��� �󵵼��� ������ ��ü
	{
		private int id; //���� id
		private int frequency; //�󵵼�
		
		public IdFreq(int id2, int freq)
		{
			id = id2;
			frequency = freq;
		}
		
		public String toString()
		{
			return Integer.toString(id) + " " + Integer.toString(frequency) + " / ";
		}
		
		public int GetId() //��ü�� id�� ��ȯ
		{
			return id;
		}
		
		public int GetFrequency() //��ü�� �󵵼��� ��ȯ
		{
			return frequency;
		}


	}
	
	static class IdWeight implements Serializable //�ܾ �����ϴ� ������ id�� ����ġ�� ������ ��ü
	{
		int id; //������ id
		float weight; //���������� �ܾ��� ����ġ
		
		public IdWeight(int id2, float wei)
		{
			id = id2; 
			weight = wei;
		}
		
		public String toString()
		{
        	String num = String.format("%.2f", weight); //����ġ�� �Ҽ��� 2�ڸ������� ǥ��
			
			return Integer.toString(id) + " " + num + " ";
		}
		

	}
	
	static public float WeightCal(int docSum, int frequency) //�ܾ �����ϴ� ������ ������ Ư�� ���������� �󵵼��� ���� ����ġ�� �����
	{
		return frequency * (float)(Math.log(5f/docSum)); // �󵵼� * �ڿ��α�(��ü ���� �� / ���� ���� ��)
	}
}
