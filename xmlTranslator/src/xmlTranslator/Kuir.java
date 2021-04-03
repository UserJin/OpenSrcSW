package xmlTranslator;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class Kuir
{

	public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException, ClassNotFoundException
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
		else if(args[0].equals("-s"))
		{
			if(args[2].equals("-q"))
			{
				searcher search = new searcher(args[1], args[3]);
			}
		}		
	}

}
