package com.gpii.transformer;

import com.gpii.jsonld.JsonLDManager;
import com.gpii.ontology.OntologyManager;
import com.gpii.ontology.Setting;
import com.gpii.ontology.Solution;
import com.gpii.utils.Utils;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 *
 * @author Claudia Loitsch
 */
 
public class TransformerManager
{

    private static TransformerManager instance = null;
	
    /**
     * TODO make it configurable
     */
    static String defaultNameSpace = "http://rbmm.org/schemas/cloud4all/0.1/";
    
    private TransformerManager()  {}
    
    public static TransformerManager getInstance() 
    {
        if(instance == null) 
            instance = new TransformerManager();
        return instance;
    }
    
    public void transformOwlToJSONLD()
    {
        String C4A_NS = "c4a:";
        
        OntologyManager.getInstance().loadOntology();
        OntologyManager.getInstance().processSolutionSettings();
        OntologyManager.getInstance().printAllSolutionsAndSettings();
        
        //create JSON-LD
        try
        {
            JSONObject result = new JSONObject();

            //@context
            JSONObject context = new JSONObject();
            context.put("c4a", "http://rbmm.org/schemas/cloud4all/0.1/");
            context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

            //@graph
            JSONArray graph = new JSONArray();

            for(int i=0; i<OntologyManager.getInstance().allSolutions.size(); i++)
            {                
                Solution tmpSolution = OntologyManager.getInstance().allSolutions.get(i);
                
                JSONObject tmpSolutionJsonObj = new JSONObject();
                tmpSolutionJsonObj.put("@id", "http://registry.gpii.net/applications/" + tmpSolution.id);
                tmpSolutionJsonObj.put("@type", C4A_NS + "Solution");
                tmpSolutionJsonObj.put(C4A_NS + "id", "http://registry.gpii.net/applications/" + tmpSolution.id);
                tmpSolutionJsonObj.put(C4A_NS + "name", tmpSolution.id);
                
                boolean addScreenReaderTTSEnabled = false;
                boolean addMagnifierEnabled = false;
                boolean addOnScreenKeyboardEnabled = false;

                //class
                JSONArray tmpSolClassJsonArray = new JSONArray();
               
                if(tmpSolution.id.equals("com.yourdolphin.supernova-as")) //cheat for SuperNova, which is both screenreader and magnifier - it cannot be supported in .owl beacuse each solution must belong only to one class
                {
                    JSONObject tmpSolClassJsonObj1 = new JSONObject();
                    tmpSolClassJsonObj1.put("@ontClassName", "ScreenReaderSoftware");
                    tmpSolClassJsonObj1.put("@type", C4A_NS + "AssistiveTechnology");
                    tmpSolClassJsonObj1.put("@id", C4A_NS + "screenreader");
                    tmpSolClassJsonArray.put(tmpSolClassJsonObj1);
                    
                    JSONObject tmpSolClassJsonObj2 = new JSONObject();
                    tmpSolClassJsonObj2.put("@ontClassName", "MagnifyingSoftware");
                    tmpSolClassJsonObj2.put("@type", C4A_NS + "AssistiveTechnology");
                    tmpSolClassJsonObj2.put("@id", C4A_NS + "magnifier");
                    tmpSolClassJsonArray.put(tmpSolClassJsonObj2);
                    
                    addScreenReaderTTSEnabled = true;
                    addMagnifierEnabled = true;
                }
                else if(tmpSolution.id.equals("es.codefactory.android.app.ma")) //another cheat for http://registry.gpii.net/applications/es.codefactory.android.app.ma because in the .owl it's under SoftwareInterfacesForComputersAndMobileDevices while we want it under screenreaders
                {
                    JSONObject tmpSolClassJsonObj = new JSONObject();
                    tmpSolClassJsonObj.put("@ontClassName", "ScreenReaderSoftware");
                    tmpSolClassJsonObj.put("@type", C4A_NS + "AssistiveTechnology");
                    tmpSolClassJsonObj.put("@id", C4A_NS + "screenreader");
                    tmpSolClassJsonArray.put(tmpSolClassJsonObj);
                    
                    addScreenReaderTTSEnabled = true;
                }
                else if(tmpSolution.id.equals("org.chrome.cloud4chrome")) //another cheat for http://registry.gpii.net/applications/org.chrome.cloud4chrome because in the .owl it's under MagnifyingSoftware while we want it under browser
                {
                    JSONObject tmpSolClassJsonObj = new JSONObject();
                    tmpSolClassJsonObj.put("@ontClassName", "MagnifyingSoftware");
                    tmpSolClassJsonObj.put("@type", C4A_NS + "AccessibilitySolution");
                    tmpSolClassJsonObj.put("@id", C4A_NS + "browser");
                    tmpSolClassJsonArray.put(tmpSolClassJsonObj);
                    
                    addScreenReaderTTSEnabled = true;
                }
                else //all others
                {
                    JSONObject tmpSolClassJsonObj = new JSONObject();
                    String[] tmpClassAndId = getJSONLDClassAndIDFromOntClassName(tmpSolution.className);
                    tmpSolClassJsonObj.put("@ontClassName", tmpSolution.className);
                    tmpSolClassJsonObj.put("@type", tmpClassAndId[0]);
                    tmpSolClassJsonObj.put("@id", tmpClassAndId[1]);
                    tmpSolClassJsonArray.put(tmpSolClassJsonObj);
                    
                    if(tmpClassAndId[1].equals(C4A_NS + "screenreader")) 
                        addScreenReaderTTSEnabled = true;
                    if(tmpClassAndId[1].equals(C4A_NS + "magnifier"))
                        addMagnifierEnabled = true;
                    if(tmpSolution.id.equals("com.microsoft.windows.onscreenKeyboard"))
                        addOnScreenKeyboardEnabled = true;
                }
                
                tmpSolutionJsonObj.put(C4A_NS + "class", tmpSolClassJsonArray);
                
                //settings
                JSONArray tmpSolSettingsJsonArray = new JSONArray();
                
                //add screenReaderTTSEnabled for all screenreaders
                if(addScreenReaderTTSEnabled)
                {
                    JSONObject tmpAddScreenReaderTTSEnabledSettingJsonObj = new JSONObject();
                    tmpAddScreenReaderTTSEnabledSettingJsonObj.put("@type", "c4a:Setting");
                    tmpAddScreenReaderTTSEnabledSettingJsonObj.put("c4a:id", "screenReaderTTSEnabled");
                    tmpAddScreenReaderTTSEnabledSettingJsonObj.put("c4a:refersTo", "http://registry.gpii.net/common/screenReaderTTSEnabled");
                    tmpAddScreenReaderTTSEnabledSettingJsonObj.put("c4a:name", "screenReaderTTSEnabled");
                    tmpSolSettingsJsonArray.put(tmpAddScreenReaderTTSEnabledSettingJsonObj);
                }
                
                //add magnifierEnabled for all magnifiers
                if(addMagnifierEnabled)
                {
                    JSONObject tmpAddMagnifierEnabledSettingJsonObj = new JSONObject();
                    tmpAddMagnifierEnabledSettingJsonObj.put("@type", "c4a:Setting");
                    tmpAddMagnifierEnabledSettingJsonObj.put("c4a:id", "magnifierEnabled");
                    tmpAddMagnifierEnabledSettingJsonObj.put("c4a:refersTo", "http://registry.gpii.net/common/magnifierEnabled");
                    tmpAddMagnifierEnabledSettingJsonObj.put("c4a:name", "magnifierEnabled");
                    tmpSolSettingsJsonArray.put(tmpAddMagnifierEnabledSettingJsonObj);
                }
                
                //add addOnScreenKeyboardEnabled for "com.microsoft.windows.onscreenKeyboard"
                if(addOnScreenKeyboardEnabled)
                {
                    JSONObject tmpAddOnScreenKeyboardEnabledSettingJsonObj = new JSONObject();
                    tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("@type", "c4a:Setting");
                    tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("c4a:id", "onScreenKeyboard");
                    tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("c4a:refersTo", "http://registry.gpii.net/common/onScreenKeyboardEnabled");
                    tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("c4a:name", "onScreenKeyboard");
                    tmpSolSettingsJsonArray.put(tmpAddOnScreenKeyboardEnabledSettingJsonObj);
                }
                
                ArrayList<Setting> allSettings = tmpSolution.settings;
                for(int j=0; j<allSettings.size(); j++)
                {
                    Setting tmpSetting = allSettings.get(j);                
                    if(tmpSetting.ignoreSetting == false
                            && tmpSetting.type != Setting.UNKNOWN
                            && tmpSetting.instanceName.startsWith("EASTIN_") == false)
                    {
                        JSONObject tmpSolSettingJsonObj = new JSONObject();
                        tmpSolSettingJsonObj.put("@type", C4A_NS + "Setting");
                        if(tmpSetting.hasID.equals("") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "id", "http://registry.gpii.net/application/" + tmpSolution.id + "/" + tmpSetting.hasID);
                        
                        if(tmpSetting.hasName.equals("") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "name", tmpSetting.hasName);
                        else
                            tmpSolSettingJsonObj.put(C4A_NS + "name", tmpSetting.instanceName);
                        
                        if(tmpSetting.value.equals("") == false
                                && tmpSetting.value.toLowerCase().trim().equals("unknown") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "defaultValue", tmpSetting.value);
                        
                        /*if(tmpSetting.hasID.contains("common")) 
                            tmpSolSettingJsonObj.put(C4A_NS + "type", "common");
                        else if(tmpSetting.hasID.contains("applications")) 
                            tmpSolSettingJsonObj.put(C4A_NS + "type", "application");*/
                        
                        if(tmpSetting.type == Setting.STRING)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "string");
                        else if(tmpSetting.type == Setting.FLOAT)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "float");
                        else if(tmpSetting.type == Setting.BOOLEAN)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "boolean");
                        else if(tmpSetting.type == Setting.INT)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "int");
                        else if(tmpSetting.type == Setting.TIME)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "time");
                        else if(tmpSetting.type == Setting.DATE)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "date");
                        else if(tmpSetting.type == Setting.DATETIME)
                            tmpSolSettingJsonObj.put(C4A_NS + "primitive_type", "dateTime");

                        
                        if(tmpSetting.hasDescription.equals("") == false
                                && tmpSetting.hasDescription.toLowerCase().trim().equals("missing") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "hasDescription", tmpSetting.hasDescription);
                        if(tmpSetting.type != Setting.BOOLEAN
                                && tmpSetting.hasValueSpace.equals("") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "hasValueSpace", tmpSetting.hasValueSpace);
                        if(tmpSetting.hasConstraints.equals("") == false
                                && tmpSetting.hasConstraints.toLowerCase().trim().equals("no constraints") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "hasConstraints", tmpSetting.hasConstraints);
                        if(tmpSetting.isMappedToRegTerm.equals("") == false)
                        {
                            tmpSolSettingJsonObj.put(C4A_NS + "refersTo", "http://registry.gpii.net/common/" + tmpSetting.isMappedToRegTerm);
                            tmpSolSettingJsonObj.put(C4A_NS + "isExactMatching", tmpSetting.isExactMatching);
                        }
                        if(tmpSetting.hasCommentsForMapping.equals("") == false
                                && tmpSetting.hasCommentsForMapping.toLowerCase().trim().equals("no comments") == false)
                            tmpSolSettingJsonObj.put(C4A_NS + "hasCommentsForMapping", tmpSetting.hasCommentsForMapping);

                        tmpSolSettingsJsonArray.put(tmpSolSettingJsonObj);
                    }
                }
                
                tmpSolutionJsonObj.put(C4A_NS + "settings", tmpSolSettingsJsonArray);
                
                graph.put(tmpSolutionJsonObj);
            }
            
            //cheat for manually adding com.microsoft.windows.displaySettings - it is not included in .owl as it has no settings
            JSONObject tmpDisplaySettingsJsonObj = new JSONObject();
            tmpDisplaySettingsJsonObj.put("@id", "http://registry.gpii.net/applications/com.microsoft.windows.displaySettings");
            tmpDisplaySettingsJsonObj.put("@type", "c4a:Solution");
            tmpDisplaySettingsJsonObj.put("c4a:id", "http://registry.gpii.net/applications/com.microsoft.windows.displaySettings");
            tmpDisplaySettingsJsonObj.put("c4a:name", "com.microsoft.windows.displaySettings");
            JSONArray tmpDisplaySettingsClassJsonArray = new JSONArray();
            JSONObject tmpDisplaySettingsClassJsonObj = new JSONObject();
            tmpDisplaySettingsClassJsonObj.put("@type", "c4a:AccessibilitySetting");
            tmpDisplaySettingsClassJsonObj.put("@id", "c4a:builtin");
            tmpDisplaySettingsClassJsonArray.put(tmpDisplaySettingsClassJsonObj);
            tmpDisplaySettingsJsonObj.put("c4a:class", tmpDisplaySettingsClassJsonArray);
            graph.put(tmpDisplaySettingsJsonObj);
            //-cheat for manually adding com.microsoft.windows.displaySettings
            
            //cheat for manually added some extra json content
            JSONObject tmpExtraJsonObj1 = new JSONObject();
            tmpExtraJsonObj1.put("@id", "c4a:screenreader");
            tmpExtraJsonObj1.put("@type", "c4a:AssistiveTechnology");
            tmpExtraJsonObj1.put("c4a:name", "ScreenReader");
            graph.put(tmpExtraJsonObj1);
            
            JSONObject tmpExtraJsonObj2 = new JSONObject();
            tmpExtraJsonObj2.put("@id", "c4a:magnifier");
            tmpExtraJsonObj2.put("@type", "c4a:AssistiveTechnology");
            tmpExtraJsonObj2.put("c4a:name", "Magnifier");
            graph.put(tmpExtraJsonObj2);
            
            JSONObject tmpExtraJsonObj3 = new JSONObject();
            tmpExtraJsonObj3.put("@id", "c4a:builtin");
            tmpExtraJsonObj3.put("@type", "c4a:AccessibilitySetting");
            tmpExtraJsonObj3.put("c4a:name", "BuiltinFeatures");
            graph.put(tmpExtraJsonObj3);
            
            JSONObject tmpExtraJsonObj4 = new JSONObject();
            tmpExtraJsonObj4.put("@id", "c4a:browser");
            tmpExtraJsonObj4.put("@type", "c4a:AccessibilitySolution");
            tmpExtraJsonObj4.put("c4a:name", "BrowserFeatures");
            graph.put(tmpExtraJsonObj4);
            
            JSONObject tmpExtraJsonObj5 = new JSONObject();
            tmpExtraJsonObj5.put("@id", "c4a:os");
            tmpExtraJsonObj5.put("@type", "c4a:AccessibilitySolution");
            tmpExtraJsonObj5.put("c4a:name", "DesktopSoftware");
            graph.put(tmpExtraJsonObj5);       
            //-cheat for manually added some extra json content

            result.put("@context", context);
            result.put("@graph", graph);

            com.gpii.utils.Utils.getInstance().writeFile(System.getProperty("user.dir") + JsonLDManager.getInstance().WEBINF_PATH + JsonLDManager.getInstance().semanticsGeneratedFromOwlFilePath, result.toString(4));     
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public String[] getJSONLDClassAndIDFromOntClassName(String tmpOntClassName)
    {
        String C4A_NS = "c4a:";
        String[] res = new String[2]; //[0] is for class and [1] is for id
        res[0] = "...unknown...";
        res[1] = "...unknown...";
        
        if(tmpOntClassName.equals("ScreenReaderSoftware"))
        {
            res[0] = C4A_NS + "AssistiveTechnology";
            res[1] = C4A_NS + "screenreader";
        }
        else if(tmpOntClassName.equals("WebBrowsers"))
        {
            res[0] = C4A_NS + "AccessibilitySolution";
            res[1] = C4A_NS + "browser";
        }
        else if(tmpOntClassName.equals("MagnifyingSoftware"))
        {
            res[0] = C4A_NS + "AssistiveTechnology";
            res[1] = C4A_NS + "magnifier";
        }
        else if(tmpOntClassName.equals("SoftwareForAdjustingColorCombinationAndTextSize")
                || tmpOntClassName.equals("OnScreenKeyboard")
                || tmpOntClassName.equals("MouseControlSoftware")
                || tmpOntClassName.equals("SoftwareToModifyThePointerAppearance")
                || tmpOntClassName.equals("SoftwareInterfacesForComputersAndMobileDevices"))
        {
            res[0] = C4A_NS + "AccessibilitySetting";
            res[1] = C4A_NS + "builtin";
        }
        else if(tmpOntClassName.equals("AlternativeInputDevices"))
        {
            res[0] = C4A_NS + "AccessibilitySolution";
            res[1] = C4A_NS + "os";
        }
        
        return res;
    }

    public String transformInput(String in) throws JSONException
    {		
        String inputString = in;
        JSONTokener inputTokener = new JSONTokener(inputString);
        JSONObject mmIn = new JSONObject(inputTokener);		

        JSONObject 	outPreProc 	= new JSONObject();
        JSONObject 	outContext 	= new JSONObject();
        JSONArray 	outGraph 	= new JSONArray();

        if(mmIn.has("preferences"))
        {
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
             *  	"c4a:setting": [
             *  		{
             *  			"c4a:name": "fontSize",
             *  			"c4a:value": "18"
             *  		}
             *  	]
             *  	
             *  }] 
             */
            Iterator<?> cKeys = inContext.keys(); 
            while( cKeys.hasNext() )
            {
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
                while( pKeys.hasNext() )
                {
                    // common, e.g. http://registry.gpii.net/common/highContrastEnabled
                    // application, e.g http://registry.gpii.net/applications/org.chrome.cloud4chrome 
                    String pID = (String)pKeys.next();

                    // get the path of pID 
                    URI uri = null;
                    try {
                        uri = new URI(pID);
                    } catch (URISyntaxException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String path = uri.getPath();

                    // create preference object:
                    JSONObject outPref = new JSONObject();
                    outPref.put("c4a:id", pID);
                    outPref.put("@type", "c4a:Preference");

                    if (pID.contains("common"))
                    {    	            	
                        //get preference name from path
                        String idStr = path.substring(path.lastIndexOf('/') + 1);
                        // common preference value is always a String, e.g. black-white
                        String comPrefVal = cPrefs.get(pID).toString();

                        //System.out.println("value type: " +comPrefVal);

                        outPref.put("c4a:type", "common");   	            	
                        outPref.put("c4a:name", idStr);
                        outPref.put("c4a:value", comPrefVal);
                    } 
                    if (pID.contains("applications"))
                    {    	            	
                        // app-specific preference value is always a JSONObject, e.g. { fontsize: 0.5, invertColours:false}    	            	
                        JSONObject appPrefValueObject = cPrefs.getJSONObject(pID);    	            	
                        Iterator<?> setKeys = appPrefValueObject.keys(); 
                        JSONArray settingSet = new JSONArray();    	    	        
                        while( setKeys.hasNext() )
                        {
                            JSONObject setting = new JSONObject();
                            String appPrefID = (String)setKeys.next();
                            String appPrefValue = appPrefValueObject.get(appPrefID).toString();
                            setting.put("c4a:name", appPrefID);
                            setting.put("c4a:value", appPrefValue);
                            settingSet.put(setting);
                        }
                        outPref.put("c4a:setting", settingSet);    	            	
                        outPref.put("c4a:type", "application");
                    }   	        	
                    outPrefArray.put(outPref);
                }
                outPrefSet.put("c4a:hasPrefs", outPrefArray);

                // translate metadata and add hasMetadata relation 
                if(inContext.getJSONObject(cID).has("metadata"))
                {
                    JSONArray cMetaOuter = inContext.getJSONObject(cID).getJSONArray("metadata");

                    // output array
                    JSONArray outMetaArray = new JSONArray();

                    for(int i = 0; i < cMetaOuter.length(); i++)
                    {
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
                if(inContext.getJSONObject(cID).has("conditions"))
                {
                    JSONArray cCondOuter = inContext.getJSONObject(cID).getJSONArray("conditions");

                    // output array
                    JSONArray outCondArray = new JSONArray();

                    for(int i = 0; i < cCondOuter.length(); i++)
                    {
                        JSONObject cMeta = cCondOuter.getJSONObject(i);	        		 

                        // new JSONObject for each metadata blob
                        JSONObject outMetaObject = new JSONObject();

                        outMetaObject.put("@type", "c4a:Condition");

                        Iterator<?> condKeys = cMeta.keys(); 
                        while(condKeys.hasNext())
                        {
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

        if(mmIn.has("deviceReporter"))
        {
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
            if(inDevice.has("OS"))
            {
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
             * "@id": "http://registry.gpii.org/applications/com.cats.org",
             * "@type": "c4a:InstalledSolution",
             * "c4a:name": "com.cats.org"
             * },
             */
            if(inDevice.has("solutions"))
            {
                JSONArray inSol = inDevice.getJSONArray("solutions");
                for(int i = 0; i < inSol.length(); i++)
                {
                    String solID = inSol.getJSONObject(i).get("id").toString();

                    JSONObject outSol = new JSONObject(); 
                    outSol.put("@type", "c4a:InstalledSolution");
                    outSol.put("@id", "http://registry.gpii.net/applications/"+solID);
                    outSol.put("name", solID);        			

                    outGraph.put(outSol);     
                }   			
            }    				
        }

        outContext.put("c4a", "http://rbmm.org/schemas/cloud4all/0.1/");
        outContext.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");		

        outPreProc.put("@context", outContext);
        outPreProc.put("@graph", outGraph);

        /**
         * make it configurable to spec the indent factor (number of spaces to add to each level of indentation).
         */
        return outPreProc.toString(5);
    }
    
    /**
     * Queries all requiered information from the rdf model and transforms the result the specific C4a JSON Structure. 
     * @param model
     * @param queries
     * @return
     * @throws JSONException
     */
    public String transformOutput(Model model, String [] queries) throws JSONException 
    {			
        /**
         * mmOut - JSON Object spec the matchmaker output
         * 
         */
        JSONObject mmOut = new JSONObject();

        /**
         * infConfig - JSONObject spec the inferred configuration object of the matchmaker output
         */
        JSONObject infConfig = new JSONObject();
        mmOut.put("inferredConfiguration", infConfig);

        for ( String url : queries) 
        {
            Query query = QueryFactory.read(System.getProperty("user.dir")+ JsonLDManager.getInstance().WEBINF_PATH + url);

            QueryExecution qexec = QueryExecutionFactory.create(query, model) ;

            try {
                ResultSet response = qexec.execSelect();

                JSONObject 	contextSet;
                JSONObject 	appSet;
                JSONObject 	solution = null;
                JSONObject 	settings;
                JSONObject  extraWrap;
                JSONArray 	conSet;
                JSONArray 	metaSet;
                JSONObject 	metadata;
                JSONArray 	scopeSet;
                JSONObject  msgSet;


                String contextID = null; 
                String queryType = null;

                while(response.hasNext())
                {
                    QuerySolution soln = response.nextSolution();

                    //System.out.println("soln: " + soln.toString());

                    infConfig = mmOut.getJSONObject("inferredConfiguration");

                    // get context id
                    if(soln.contains("?contextID"))
                        contextID = soln.get("?contextID").toString();

                    // get query type, e.g. Condition
                    if(soln.contains("?type"))
                        queryType = soln.get("?type").toString();

                    /**
                     * Context - create a new context object if not exists
                     *  
                     */
                    if(infConfig.has(contextID.toString()))
                        contextSet = infConfig.getJSONObject(contextID.toString());
                    else 
                    {
                        appSet = new JSONObject();
                        infConfig.put(contextID.toString(), appSet);
                        contextSet = infConfig.getJSONObject(contextID.toString());
                    }
                    /**
                     * Application - create a new application object if not exists
                     * 
                     */
                    if(queryType.equals(defaultNameSpace+"Configuration"))
                    {
                        if(contextSet.has("applications"))
                                appSet = contextSet.getJSONObject("applications");
                        else 
                        {
                            appSet = new JSONObject();
                            contextSet.put("applications", appSet);
                            appSet = contextSet.getJSONObject("applications");
                        }

                        // add application name if not exists
                        if(soln.contains("?appName"))
                        {
                            String appName 	= soln.get("?appName").toString();

                            if(appSet.has(appName))										
                                solution = appSet.getJSONObject(appName);
                            else
                            {
                                solution = new JSONObject();
                                appSet.put(appName, solution);
                                solution = appSet.getJSONObject(appName);
                            }	
                        }

                        // add activation of application
                        if(soln.contains("?appActive"))
                        {
                            Boolean appActive = new Boolean(soln.get("?appActive").toString());

                            if(!solution.has("active"))
                                solution.put("active", appActive);
                        }

                        // add settings to separate settings block. 							
                        if(soln.contains("?setID") && soln.contains("setValue") && soln.contains("setName") )
                        {
                            String setId = soln.get("?setID").toString();
                            String setName = soln.get("?setName").toString();
                            Object setValue = soln.get("?setValue"); 

                            try {
                                int i = Integer.parseInt(setValue.toString());
                                setValue = i;
                            } 
                            catch (NumberFormatException e) {
                            }

                            try {
                                double i = Double.parseDouble(setValue.toString());
                                setValue = i;
                            } 
                            catch (NumberFormatException e){
                            }								    

                            if(setValue.toString().equals("true") || setValue.toString().equals("false"))
                                setValue = new Boolean(setValue.toString());

                            if(solution.has("settings"))
                                settings = solution.getJSONObject("settings");
                            else 
                            {
                                settings = new JSONObject(); 
                                solution.put("settings", settings);
                                settings = solution.getJSONObject("settings");
                            }

                            if(setId.contains("registry.gpii.net/applications/"))
                            {
                                if(settings.has(setId))
                                    extraWrap = settings.getJSONObject(setId);
                                else
                                {
                                    extraWrap = new JSONObject();
                                    settings.put(setId, extraWrap);
                                    extraWrap = settings.getJSONObject(setId);
                                }
                                extraWrap.put(setName, setValue);

                            }
                            else
                                settings.put(setId, setValue);       
                        }	
                    }						

                    /**
                     * Condition - create a new condition array if not exists
                     * 
                     */
                    if(queryType.equals(defaultNameSpace+"Condition"))
                    {
                        if(contextSet.has("conditions"))
                            conSet = contextSet.getJSONArray("conditions");
                        else 
                        {
                            conSet = new JSONArray();
                            contextSet.put("conditions", conSet);
                            conSet = contextSet.getJSONArray("conditions");
                        }
                        // create a new condition object and put it to the condition array
                        JSONObject condition = new JSONObject (); 
                        /**
                         * TODO: condition payload does not use generic keys. min or max are specific to a specific type of condition. 
                         * This should be improved in general. 
                         *  
                         */
                        condition.put("type", soln.get("?condOp").toString());
                        condition.put("inputPath", soln.get("?condPa").toString());
                        
                        Object setMin = soln.get("?condMi"); 
                        Object setMax = soln.get("?condMa");
                       
                        condition.put("min", Utils.getInstance().transformValueSpace(setMin));
                        condition.put("max", Utils.getInstance().transformValueSpace(setMax));

                        conSet.put(condition);								
                    }

                    /**
                     * Metadata - create a new metadata array if not exists
                     * 
                     */
                    if(queryType.equals(defaultNameSpace+"Metadata"))
                    {
                        // add metadata section to the context block
                        if(contextSet.has("metadata"))
                            metaSet = contextSet.getJSONArray("metadata");			
                        else 
                        {
                            metaSet = new JSONArray();
                            contextSet.put("metadata", metaSet);
                            metaSet = contextSet.getJSONArray("metadata");
                        }

                        //check if there is already an object for type "helpMessage" in the scope of an application
                        String metaType = soln.get("?metaType").toString();
                        String scope = soln.get("?metaScope").toString();
                        /**
                         * TODO
                         * 
                         * scope and message type make a meta data object unique for now. 
                         * But does this work if you would have one helpMessage for solution A 
                         * and one helpMessage for Solution A and B ???     
                         * 
                         */
                        metadata = objectContains(metaSet, metaType, scope);

                        if(metadata == null)
                        {
                            System.out.println("does something matches:" +metadata);
                            metadata = new JSONObject();
                            // type
                            metadata.put("type", metaType);
                            // scope
                            if(metadata.has("scope"))
                                scopeSet = metadata.getJSONArray("scope");
                            else
                            {
                                scopeSet = new JSONArray();
                                metadata.put("scope", scopeSet);
                                scopeSet = metadata.getJSONArray("scope");
                            }
                            scopeSet.put(scope);
                            metaSet.put(metadata);
                        }

                        // message
                        if(metadata.has("message"))
                            msgSet = metadata.getJSONObject("message");			
                        else 
                        {
                            msgSet = new JSONObject();
                            metadata.put("message", msgSet);
                            msgSet = metadata.getJSONObject("message");
                        }

                        JSONObject msg = new JSONObject();
                        msg.put("message", soln.get("?msgText").toString());
                        msg.put("learnMore", "http://wwwpub.zih.tu-dresden.de/~loitsch/review/nvdaTutorial.html");
                        msgSet.put(soln.get("?msgLang").toString(), msg);
                    }
                }
            } 
            catch (JSONException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } 
            finally 
            { 
                qexec.close();
            }	
        }

        /**
         * make it configurable to spec the indent factor (number of spaces to add to each level of indentation).
         */
        return mmOut.toString(5);
    }
	
    private JSONObject objectContains(JSONArray metaData, 
                                      String metaType,
                                      String metaScope) throws JSONException 
    {
        boolean typeSupported 	= false; 
        boolean scopeSupported 	= false; 
        JSONObject match = null;
        JSONObject next = null;

        if(metaData.length() > 0) 
        {		
            for (int i = 0; i < metaData.length(); i++) 
            {		
                next = metaData.getJSONObject(i); 

                // type
                if(next.get("type").toString().equals(metaType)) 
                    typeSupported = true; 

                // scope
                JSONArray scopeSet = next.getJSONArray("scope");

                for(int j=0; j < scopeSet.length(); j++) 
                {			
                    if(scopeSet.get(j).toString().equals(metaScope)) 
                        scopeSupported = true;		
                }
            }
            if(typeSupported && scopeSupported) 
                match = next;
        }	
        return match;
    }

    public static boolean contains(JSONArray array, String string) throws JSONException 
    {	
        boolean r = false; 

        if(array.length() > 0)
        {
            for(int i=0; i<array.length(); i++) 
            {
                if(array.getString(i).equals(string)) 
                    r = true;
                else
                    r = false;
            }
        }
        else
            r = false;
        return r;		
    }
	
}