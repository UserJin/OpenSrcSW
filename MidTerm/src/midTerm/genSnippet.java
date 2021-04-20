package midTerm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class genSnippet {
	
	genSnippet(String name, String str) throws IOException
	{
		File file = new File(name);
		
		FileInputStream input = new FileInputStream(file);
		InputStreamReader reader = new InputStreamReader(input, "UTF-8");
		BufferedReader in = new BufferedReader(reader);
		
		ArrayList<String> arr = new ArrayList<String>();
		
		String k;
		while((k = in.readLine()) != null)
		{
			arr.add(k);
		}
		
		String[] word = str.split(" ");
		
		int best;
		int bestScore = 0;
		
		for(int i =0;i<arr.size();i++)
		{
			String[] wordOfarr = arr.get(i).split(" ");
		}
		
		
		
	}

}
