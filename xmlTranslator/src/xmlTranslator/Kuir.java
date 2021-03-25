package xmlTranslator;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class Kuir
{

	public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException, ClassNotFoundException
	{
		if(args.length != 2)
		{
			System.out.println("입력 방식 : java Kuir -c/-k [파일 경로]");
		}
		else
		{
			if(args[0].equals("-c"))
			{
				makeCollection col = new makeCollection(args[1]);
			}
			else if(args[0].equals("-k"))
			{
				makeKeyword key = new makeKeyword(args[1]);
			}
			else if(args[0].equals("-i"))
			{
				indexer map = new indexer(args[1]);
			}
		}


		
	}

}
