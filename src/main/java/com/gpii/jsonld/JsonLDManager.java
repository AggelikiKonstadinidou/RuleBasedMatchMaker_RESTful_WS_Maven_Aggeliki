package com.gpii.jsonld;

import com.google.gson.Gson;
import com.gpii.ontology.OntologyManager;
import com.gpii.transformer.TransformerManager;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author nkak
 * @author Claudia Loitsch
 */
 
public class JsonLDManager 
{
    //input
    public String currentNPSet;
    public String currentDeviceManagerPayload;
  
    //static input files
    public String semanticsSolutionsFilePath;
    //public String explodePrefTermsFilePath;
    public String mappingRulesFilePath;
    public String mmInput;
    
    /**
     * TODO make a global configuration for all C4a specific files
     */
    public String querryCondPath;
    public String querryAppsPath;
    
    //temp preprocessing output files
    public String preprocessingTempfilePath;
    public String postprocessingTempfilePath;

    
    public Gson gson;
    
    private static JsonLDManager instance = null;
    
    private JsonLDManager() 
    {
        File f = new File(System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/testData/rules/basicAlignment.rules");
 
        if(f.exists())  //deployment mode
        {
            //static input files
            semanticsSolutionsFilePath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/semantics/semanticsSolutions.jsonld";
            //explodePrefTermsFilePath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/semantics/explodePreferenceTerms.jsonld";
            
        	/**
        	 * TODO find a new location, not in folder test data; split ontology alingment rules from matching rules 
        	 */
        	mappingRulesFilePath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/testData/rules/basicAlignment.rules";
        	mmInput = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/testData/input/newInput.json";
        	
        	// querries 
        	querryCondPath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/testData/queries/outCondition.sparql";
        	querryAppsPath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/testData/queries/outApplications.sparql";
            
            // temp preprocessing output files
            preprocessingTempfilePath = System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/TEMP/preprocessingOutput.jsonld";
            postprocessingTempfilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/TEMP/postprocessingOutput.json";            

        }
        else            //Jetty integration tests
        {
            //static input files
            semanticsSolutionsFilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/semantics/semanticsSolutions.jsonld";
            //explodePrefTermsFilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/semantics/explodePreferenceTerms.jsonld";
            mappingRulesFilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/rules/basicAlignment.rules";
        	mmInput = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/input/newInput.json";
            
        	querryCondPath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/queries/outCondition.sparql";
        	querryAppsPath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/queries/outApplications.sparql";
        	
            //temp preprocessing output files
            preprocessingTempfilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/TEMP/preprocessingOutput.jsonld";
            postprocessingTempfilePath = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/TEMP/postprocessingOutput.json";

        }
        currentNPSet = "";
        currentDeviceManagerPayload = "";
        
        gson = new Gson();
    }
    
    public static JsonLDManager getInstance() 
    {
        if(instance == null) 
            instance = new JsonLDManager();
        return instance;
    }
    
    public void runJSONLDTests() throws IOException, JSONException 
    {

        try {
			preprocessing(mmInput);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
        /**
         * TODO make it configurable to add various input
         */
    	// populate all JSONLDInput to a model 
    	OntologyManager.getInstance().populateJSONLDInput(new String[] {preprocessingTempfilePath, semanticsSolutionsFilePath});
    	
    	// infer configuration 
    	Model imodel = inferConfiguration(OntologyManager._dmodel, mappingRulesFilePath);
    	
    	// create MM output
    	/**
    	 * TODO make a global configuration for cloud4all to use the specific C4a queries
    	 */
    	String[] queries = {querryCondPath, querryAppsPath};
    	byte [] outToWrite = TransformerManager.getInstance().transformOutput(imodel, queries);
    	writeFile(postprocessingTempfilePath, outToWrite);    	
    }
    
    public Model inferConfiguration(Model model, String ruleFile)
    {
        File f = new File(ruleFile);
        if (f.exists()) 
        {
                List<Rule> rules = Rule.rulesFromURL("file:" + mappingRulesFilePath);

                GenericRuleReasoner r = new GenericRuleReasoner(rules);

                InfModel infModel = ModelFactory.createInfModel(r, model);		    
                infModel.prepare();					

            Model deducedModel = infModel.getDeductionsModel();  
                model.add(deducedModel);
            	model.write(System.out, "N-TRIPLE");
        }
    	return model;    	
    }

    
	/**
	 * TODO: Create a new class performing pre-processing
	 * The function preprocessing translate the input from the Flow Manager, including preference sets, device information, etc from json into json-ld. 
	 * Expected input is defined at: https://code.stypi.com/81qjpbxb
	 * Expected output is defined at: http://code.stypi.com/xygkeupl    
	 * @throws JSONException 
	 * @throws IOException 
	 * @throws URISyntaxException 
	 */
    private void preprocessing(String in) throws JSONException, IOException, URISyntaxException {
    	
		String inputString = readFile(in, StandardCharsets.UTF_8);  
		JSONTokener inputTokener = new JSONTokener(inputString);
		JSONObject mmIn = new JSONObject(inputTokener);		
		
		JSONObject 	outPreProc 	= new JSONObject();
		JSONObject 	outContext 	= new JSONObject();
		JSONArray 	outGraph 	= new JSONArray();

		if(mmIn.has("preferences")){
			JSONObject inContext  = mmIn.getJSONObject("preferences").getJSONObject("contexts");
   			
			/** Translate preferences sets 
			 * IN: 
			 * "gpii-default": {
			 * 		"name": "Default preferences",
			 * 		"preferences": {
			 *  		"http://registry.gpii.net/common/fontSize": 15,
			 *       }
			 * }
			 * GOAL: {
			 *  "@id": "c4a:nighttime-at-home",
			 *  "@type": "c4a:PreferenceSet",
			 *  "c4a:id": "nighttime-at-home",
			 *  "c4a:name": "Nighttimeathome",
			 *  "c4a:hasPrefs": [{
			 *  	"c4a:id": "http://registry.gpii.net/common/fontSize",
			 *  	"@type": "c4a:Preference",
			 *  	"c4a:type": "common",
			 *  	"c4a:name": "fontSize",
			 *  	"c4a:value": "18"
			 *  }] 
			 */
			Iterator<?> cKeys = inContext.keys(); 
	        while( cKeys.hasNext() ){
	        	String cID = (String)cKeys.next();
	        	String cName = inContext.getJSONObject(cID).get("name").toString();
	        	
	        	JSONObject outPrefSet = new JSONObject();
	        	outPrefSet.put("@id", "c4a:"+cID);
	        	outPrefSet.put("@type", "c4a:PreferenceSet");
	        	outPrefSet.put("c4a:id", cID);
	        	outPrefSet.put("c4a:name", cName);

	        	// translate preferences and add hasPrefs relation 
	        	JSONObject cPrefs = inContext.getJSONObject(cID).getJSONObject("preferences");
	        	
	        	JSONArray outPrefArray = new JSONArray(); 
    			Iterator<?> pKeys = cPrefs.keys(); 
    	        while( pKeys.hasNext() ){
    	        	String pID = (String)pKeys.next();
    	        	String pVal = cPrefs.getString(pID);
    	        	
    	        	JSONObject outPref = new JSONObject();
    	        	outPref.put("c4a:id", pID);
    	        	outPref.put("@type", "c4a:Preference");
    	        	
    	            if (pID.contains("common")) outPref.put("c4a:type", "common");
    	            if (pID.contains("applications")) outPref.put("c4a:type", "application");
    	            
    	            URI uri = new URI(pID);
    	            String path = uri.getPath();
    	            String idStr = path.substring(path.lastIndexOf('/') + 1);
    	            outPref.put("c4a:name", idStr);

    	            outPref.put("c4a:value", pVal);
   	        	
    	        	outPrefArray.put(outPref);

    	        }
	        	outPrefSet.put("c4a:hasPrefs", outPrefArray);

	        	// translate metadata and add hasMetadata relation 
	        	if(inContext.getJSONObject(cID).has("metadata")){
	        		JSONArray cMetaOuter = inContext.getJSONObject(cID).getJSONArray("metadata");
		        	
		        	// output array
		        	JSONArray outMetaArray = new JSONArray();
		        	
		        	for(int i = 0; i < cMetaOuter.length(); i++){
		        		
		        		JSONObject cMeta = cMetaOuter.getJSONObject(i);	        		 
		        		
		        		// new JSONObject for each metadata blob
		        		JSONObject outMetaObject = new JSONObject();
		        		
		        		 outMetaObject.put("@type", "c4a:Metadata");
		        		 outMetaObject.put("c4a:type", cMeta.get("type").toString());
		        		 outMetaObject.put("c4a:value", cMeta.get("value").toString());
		        		 outMetaObject.put("c4a:scope", cMeta.getJSONArray("scope"));	        		 
		        		 
		        		 outMetaArray.put(outMetaObject); 
		        	}
		        	outPrefSet.put("c4a:hasMetadata", outMetaArray);	
	        	}
	        	
	        	// translate condition and add hasCondition relation 
	        	if(inContext.getJSONObject(cID).has("conditions")){
	        		
	        		JSONArray cCondOuter = inContext.getJSONObject(cID).getJSONArray("conditions");
		        	
		        	// output array
		        	JSONArray outCondArray = new JSONArray();
		        	
		        	for(int i = 0; i < cCondOuter.length(); i++){
		        		
		        		JSONObject cMeta = cCondOuter.getJSONObject(i);	        		 
		        		
		        		// new JSONObject for each metadata blob
		        		JSONObject outMetaObject = new JSONObject();
		        		
		        		outMetaObject.put("@type", "c4a:Condition");
		        		 
		     			Iterator<?> condKeys = cMeta.keys(); 
		    	        while(condKeys.hasNext()){
		    	        	
		    	        	String condKey = (String)condKeys.next();
		    	        	outMetaObject.put("c4a:"+condKey, cMeta.get(condKey).toString());
		    	        }		        		 
		        		outCondArray.put(outMetaObject); 
		        	}
		        	outPrefSet.put("c4a:hasCondition", outCondArray);	
	        	}
	        	
	        	outGraph.put(outPrefSet);        	        	
	        }			
			
		}
		
		if(mmIn.has("deviceReporter")){
			JSONObject inDevice  = mmIn.getJSONObject("deviceReporter");
			
			/** Translate operating system;
			 * IN:   
			 * "OS": {
			 *  "id": "win32",
			 *  "version": "5.0.0"
			 *  },
			 * GOAL: {
			 * "@id": "c4a:win32",
			 * "@type": "c4a:OperatingSystem",
			 * "c4a:name": "win32"
			 * },
			 */    			
			if(inDevice.has("OS")){
    			JSONObject inOS = inDevice.getJSONObject("OS");
    			String osID = inOS.get("id").toString();
    			String osVer = inOS.get("version").toString();        			

    			JSONObject outOS = new JSONObject(); 
    			outOS.put("@type", "c4a:OperatingSystem");
    			outOS.put("@id", "c4a:"+osID);
    			outOS.put("name", osID);
    			outOS.put("version", osVer);        			
    			
    			outGraph.put(outOS);        			
			}

			/** Translate installed solutions;
			 * IN:   
			 * solutions": [
			 * 	{ "id": com.cats.org }
			 * ]
			 *  GOAL: {
			 * "@id": "c4a:com.cats.org",
			 * "@type": "c4a:InstalledSolution",
			 * "c4a:name": "com.cats.org"
			 * },
			 */
			if(inDevice.has("solutions")){
    			JSONArray inSol = inDevice.getJSONArray("solutions");
    			for(int i = 0; i < inSol.length(); i++){
    				
    				String solID = inSol.getJSONObject(i).get("id").toString();

    				JSONObject outSol = new JSONObject(); 
    				outSol.put("@type", "c4a:InstalledSolution");
    				outSol.put("@id", "c4a:"+solID);
    				outSol.put("name", solID);        			
        			
        			outGraph.put(outSol);     
    			}   			
			}    				
		}
        
		outContext.put("c4a", "http://rbmm.org/schemas/cloud4all/0.1/");
		outContext.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");		
		
		outPreProc.put("@context", outContext);
		outPreProc.put("@graph", outGraph);
		
	    byte dataToWrite[] = outPreProc.toString().getBytes(StandardCharsets.US_ASCII);
	    writeFile(preprocessingTempfilePath, dataToWrite);
    	
    }



    /**
     * TODO create a class for help functions
     * @param path where to write the file
     * @param dataToWrite // data to write in the file
     */
    private void writeFile(String path, byte[] dataToWrite){
            FileOutputStream out = null;
            try {
                    out = new FileOutputStream(path);
            } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
            try {
                    out.write(dataToWrite);
                    out.close();
            } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
    }
    
    /**
     * TODO create a class for help functions
     * @param path where to write the file
     * @param dataToWrite // data to write in the file
     */
	static String readFile(String path, Charset encoding) throws IOException 
	{
	  byte[] encoded = Files.readAllBytes(Paths.get(path));
	  return new String(encoded, encoding);
}
    
    
}