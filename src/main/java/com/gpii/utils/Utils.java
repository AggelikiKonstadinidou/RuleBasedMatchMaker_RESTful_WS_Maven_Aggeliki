package com.gpii.utils;

import com.gpii.jsonld.JsonLDManager;
import com.hp.hpl.jena.rdf.model.Model;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.codehaus.jackson.map.ObjectMapper;

/**
 *
 * @author nkak
 * @author claudia loitsch
 */
public class Utils 
{
    private static Utils instance = null;
       
    private Utils() 
    {
    }
    
    public static Utils getInstance() 
    {
        if(instance == null) 
            instance = new Utils();
        return instance;
    }
    
    public void writeFile(String path, String content)
    {
        try {
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path),"UTF-8"));
            out.write(content);
            out.close();
            System.out.println("* Generated file: " + path);
        }
        catch (IOException e)
        {
            e.printStackTrace();      
        }
    }
    
    public String readFile(String pathname) throws IOException 
    {
        String fileContents = "";
        FileReader fileReader = new FileReader(pathname);   
        int i;
        while((i =  fileReader.read())!=-1)
        {
            char ch = (char)i;
            fileContents = fileContents + ch; 
        }
        return fileContents;
    }
    
    public void writeOntologyModelToFile(Model tmpModel, String tmpFilepath)
    {
        FileWriter out = null;
        try {
            out = new FileWriter(tmpFilepath);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            tmpModel.write(out, "RDF/XML");
        }
        finally 
        {
            try {
                out.close();
                System.out.println("* Generated file: " + tmpFilepath);
            }
            catch (IOException closeException) {
                closeException.printStackTrace();
            }
        }
    }
    
    public String jsonPrettyPrint(String tmpJsonStr)
    {
        String res = "";        
        ObjectMapper mapper = new ObjectMapper();
        try {
            res = mapper.defaultPrettyPrintingWriter().writeValueAsString(JsonLDManager.getInstance().gson.fromJson(tmpJsonStr, Object.class));
        } catch (IOException ex) {
            ex.printStackTrace();
        }          
        return res;
    }
    
    public Object transformValueSpace(Object o)
    {
    	Object transVal = o; 
        
    	try {
            int i = Integer.parseInt(o.toString());
            transVal = i;
        } 
        catch (NumberFormatException e) {
        }

        try {
            double i = Double.parseDouble(o.toString());
            transVal = i;
        } 
        catch (NumberFormatException e){
        }
    	
        return transVal;    	
    }
    
}
