package midTerm;

import java.io.IOException;

public class execution {

	public static void main(String[] args) throws IOException {


		if(args[0].equals("-f"))
		{
			if(args[2].equals("-q"))
			{
				genSnippet gen = new genSnippet(args[1], args[3]);
			}
		}
	}

}
