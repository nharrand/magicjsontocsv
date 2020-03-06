package se.kth.assertteam;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App 
{
	public static char separator = '¤';
    public static void main( String[] args ) throws IOException {
        if(args.length != 2) {
            System.err.println("Usage: java -jar magic.jar in.json out.csv");
            return;
        }
        File out = new File(args[1]);

        //Build JSON array from input file
        String rawJSON = readFile(args[0]);
        JSONArray root = new JSONArray(rawJSON);

        //Write Header
        Set<String> colsName = getColumnsName(root);
        String header = colsName.stream().collect(Collectors.joining(",")) + "\n";
        FileUtils.write(out,header,StandardCharsets.UTF_8,false);

        //Write Content
	    writeCsv(root,colsName,out);

        System.out.println("Done");
    }

    private static String readFile(String filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

	private static Set<String> getColumnsName(JSONArray in) {
    	Set<String> colsName = new LinkedHashSet<>();
    	for(int i = 0; i < in.length(); i++) {
    		JSONObject element = in.getJSONObject(i);
    		Set<String> keys = element.keySet();
    		for(String key: keys) {
    			Object value = element.get(key);
    			if(value instanceof JSONObject) {
    				Set<String> nestedKeys = ((JSONObject) value).keySet();
    				for (String nestedKey: nestedKeys) {
    					colsName.add(key + separator + nestedKey);
				    }
			    } else {
    				colsName.add(key);
			    }
		    }

    		colsName.addAll(element.keySet());
	    }
	    return colsName;
    }

    private static void writeCsv(JSONArray in, Set<String> colsName, File out) throws IOException {
    	for (int i = 0; i < in.length(); i++) {
    		String line = "";
    		JSONObject element = in.getJSONObject(i);
    		boolean isFirst = true;
    		for(String key: colsName) {
    			if(isFirst) {
    				isFirst = false;
			    } else {
    				line += ",";
			    }
			    String value = "NA";
			    try {
    				if(key.contains(separator + "")) {
    					String key1 = key.split(separator + "")[0];
					    String key2 = key.split(separator + "")[1];
					    JSONObject val = element.getJSONObject(key1);
					    value = val.get(key2).toString();
				    } else {
    					value = element.get(key).toString();
				    }
			    } catch (JSONException e) {
			    }
			    if(!value.startsWith("\"") && value.contains(",")) {
			    	value = "\"" + value.replace("\"", "\\\"") + "\"";
			    }
			    value = value.replace("\n", " ");
			    line += value;
		    }
		    FileUtils.write(out, line + "\n", StandardCharsets.UTF_8, true);
	    }
    }
}
