package com.gpii.jsonld;

import com.google.gson.Gson;
import com.gpii.ontology.OntologyManager;
import com.gpii.transformer.TransformerManager;
import com.gpii.utils.Utils;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;
import java.io.*;
import java.util.List;
import java.util.Properties;
import org.json.JSONException;

/**
 *
 * @author nkak
 * @author Claudia Loitsch
 */
 
public class JsonLDManager 
{
    public static final int FINAL_SERVER_DEPLOYMENT_MODE = 0;
    public static final int JETTY_INTEGRATION_TESTS_MODE = 1;
    public int currentDeploymentMode;
    public String WEBINF_PATH;

    public boolean PERFORM_INTEGRATION_TESTS;
    public boolean INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD; 
    public boolean USE_THE_REAL_ONTOLOGY;
    
    //static input files
    public String [] semantics;
    public String semanticsGeneratedFromOwlFilePath;
    public String [] rules; 
    public String [] queries; 
    public String solutionsOntology;
    
    public String querryCondPath;
    public String querryAppsPath;
    public String querryMetaDataPath;
    
    //files for debugging
    public String initialJsonInputFilepath;
    public String transformedJsonLDInputFilepath;
    public String initialOntModelFilepath;
    public String inferredOntModelFilepath;
    public String rbmmJsonOutputFilepath;
    //-files for debugging
    
    public Gson gson;
    
    private static JsonLDManager instance = null;
    
    private JsonLDManager() 
    {
        //read properties file
        Properties prop = new Properties();
        InputStream configInputStream = null;
        
        File f = new File(System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/config.properties");
 
        try
        {
            if(f.exists())  //Deployment mode
            {
                currentDeploymentMode = FINAL_SERVER_DEPLOYMENT_MODE;
                configInputStream = new FileInputStream(System.getProperty("user.dir") + "/../webapps/CLOUD4All_RBMM_Restful_WS/WEB-INF/config.properties");
            
                //read properties file
                prop.load(configInputStream);
                
                WEBINF_PATH = prop.getProperty("DEPLOYMENT_WEBINF_PATH");
            }
            else            //Jetty integration tests
            {
                currentDeploymentMode = JETTY_INTEGRATION_TESTS_MODE;
                configInputStream = new FileInputStream(System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/config.properties");
            
                //read properties file
                prop.load(configInputStream);
                
                WEBINF_PATH = prop.getProperty("JETTY_WEBINF_PATH");
            }
                    
            //static input files
            semantics = prop.getProperty("semantics").split(";");
            semanticsGeneratedFromOwlFilePath = prop.getProperty("semanticsGeneratedFromOwlFilePath");
            
            /**
             * TODO find a new location, not in folder test data; split ontology alignment rules from matching rules 
             */
            rules = prop.getProperty("rules").split(";");
            // queries 
            queries = prop.getProperty("queries").split(";");
            solutionsOntology = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("solutionsOntology");
            
            //debug
            initialJsonInputFilepath = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("initialJsonInputFilepath");
            transformedJsonLDInputFilepath = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("transformedJsonLDInputFilepath");
            initialOntModelFilepath = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("initialOntModelFilepath");
            inferredOntModelFilepath = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("inferredOntModelFilepath");
            rbmmJsonOutputFilepath = System.getProperty("user.dir") + WEBINF_PATH + prop.getProperty("rbmmJsonOutputFilepath");
            //-debug
            
            PERFORM_INTEGRATION_TESTS = Boolean.parseBoolean(prop.getProperty("PERFORM_INTEGRATION_TESTS"));
            INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD = Boolean.parseBoolean(prop.getProperty("INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD"));
            USE_THE_REAL_ONTOLOGY = Boolean.parseBoolean(prop.getProperty("USE_THE_REAL_ONTOLOGY"));
            
            if(USE_THE_REAL_ONTOLOGY)
                semantics[0] = semanticsGeneratedFromOwlFilePath;
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        finally {
            if (configInputStream != null) {
		try {
                    configInputStream.close();
		} catch (IOException e) {
                    e.printStackTrace();
		}
            }
	} 
        
        gson = new Gson();
    }
    
    public static JsonLDManager getInstance() 
    {
        if(instance == null) 
            instance = new JsonLDManager();
        return instance;
    }
    
    public String runJSONLDTests(String tmpInputJsonStr) throws IOException, JSONException 
    {
        String resJsonStr = "";
        
        /*************************/
        /* PREPROCESSING - Start */
        /*************************/
        
        //debug - write initial JSON input to file
        Utils.getInstance().writeFile(initialJsonInputFilepath, Utils.getInstance().jsonPrettyPrint(tmpInputJsonStr));
        
        //transform input to JSON-LD
        String transIn =  TransformerManager.getInstance().transformInput(tmpInputJsonStr);
        
        //debug - write transformed JSON-LD input to file
        Utils.getInstance().writeFile(transformedJsonLDInputFilepath, Utils.getInstance().jsonPrettyPrint(transIn));
    	
    	//populate all JSON-LD input to a model 
    	OntologyManager.getInstance().populateJSONLDInput(transIn, semantics);
    	
        //debug - write initial model to .owl
        Utils.getInstance().writeOntologyModelToFile(OntologyManager._dmodel, initialOntModelFilepath);
        
        /***********************/
        /* PREPROCESSING - End */
        /***********************/
        
        /*******************************/
    	/* INFER CONFIGURATION - Start */
        /*******************************/
        //run the rules
    	Model imodel = inferConfiguration(OntologyManager._dmodel, rules);
        
        //debug - write inferred model to .owl
        Utils.getInstance().writeOntologyModelToFile(imodel, inferredOntModelFilepath);
        
        /*****************************/
    	/* INFER CONFIGURATION - End */
        /*****************************/
        
        /***************************/
        /* POST PROCESSING - Start */
        /***************************/
        
        // create MM output

    	resJsonStr = TransformerManager.getInstance().transformOutput(imodel, queries);
        
        //debug - write final RBMM JSON output to file
        Utils.getInstance().writeFile(rbmmJsonOutputFilepath, Utils.getInstance().jsonPrettyPrint(resJsonStr));        
        
        /*************************/
        /* POST PROCESSING - End */
        /*************************/
        
        return resJsonStr;
    }
    
    public Model inferConfiguration(Model model, String[] rulePths)
    {
        for (String path : rulePths) 
        {
            File f = new File(System.getProperty("user.dir")+ WEBINF_PATH + path);        	
            if (f.exists()) 
            {
	            List<Rule> rules = Rule.rulesFromURL("file:"+f.getPath());
	            
	            //System.out.println(rules.toString());
	
	            GenericRuleReasoner r = new GenericRuleReasoner(rules);
	
	            InfModel infModel = ModelFactory.createInfModel(r, model);		    
	            
	            infModel.prepare();					
	
	            Model deducedModel = infModel.getDeductionsModel();  
	            
	            model.add(deducedModel);
	            
	          // deducedModel.write(System.out, "N-TRIPLE");
	          // model.write(System.out, "N-TRIPLE");
            }
            else System.out.println("Rule file does not exist: " + path);
        }
    	return model;    	
    }
}