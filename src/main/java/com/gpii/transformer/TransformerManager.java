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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.tools.ant.types.CommandlineJava.SysProperties;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * 
 * @author Claudia Loitsch
 */

public class TransformerManager {

	private static TransformerManager instance = null;

	/**
	 * TODO make it configurable
	 */
	static String defaultNameSpace = "http://rbmm.org/schemas/cloud4all/0.1/";

	private TransformerManager() {
	}

	public static TransformerManager getInstance() {
		if (instance == null)
			instance = new TransformerManager();
		return instance;
	}

	public void transformOwlToJSONLD() {
		String C4A_NS = "c4a:";

		OntologyManager.getInstance().loadOntology();
		OntologyManager.getInstance().processSolutionSettings();
		OntologyManager.getInstance().printAllSolutionsAndSettings();

		// create JSON-LD
		try {
			JSONObject result = new JSONObject();

			// @context
			JSONObject context = new JSONObject();
			context.put("c4a", "http://rbmm.org/schemas/cloud4all/0.1/");
			context.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
			context.put("xsd", "http://www.w3.org/2001/XMLSchema#");

			// @graph
			JSONArray graph = new JSONArray();

			for (int i = 0; i < OntologyManager.getInstance().allSolutions
					.size(); i++) {
				Solution tmpSolution = OntologyManager.getInstance().allSolutions
						.get(i);

				JSONObject tmpSolutionJsonObj = new JSONObject();
				tmpSolutionJsonObj.put("@id",
						"http://registry.gpii.net/applications/"
								+ tmpSolution.id);
				tmpSolutionJsonObj.put("@type", C4A_NS + "Solution");
				tmpSolutionJsonObj.put(C4A_NS + "id", tmpSolution.id);
				tmpSolutionJsonObj.put(C4A_NS + "name", tmpSolution.id);

				boolean addScreenReaderTTSEnabled = false;
				boolean addMagnifierEnabled = false;
				boolean addOnScreenKeyboardEnabled = false;

				// class
				JSONArray tmpSolClassJsonArray = new JSONArray();

				if (tmpSolution.id.equals("com.yourdolphin.supernova-as")) // cheat
																			// for
																			// SuperNova,
																			// which
																			// is
																			// both
																			// screenreader
																			// and
																			// magnifier
																			// -
																			// it
																			// cannot
																			// be
																			// supported
																			// in
																			// .owl
																			// beacuse
																			// each
																			// solution
																			// must
																			// belong
																			// only
																			// to
																			// one
																			// class
				{
					JSONObject tmpSolClassJsonObj1 = new JSONObject();
					tmpSolClassJsonObj1.put("@ontClassName",
							"ScreenReaderSoftware");
					tmpSolClassJsonObj1.put("@type", C4A_NS
							+ "AssistiveTechnology");
					tmpSolClassJsonObj1.put("@id", C4A_NS + "screenreader");
					tmpSolClassJsonArray.put(tmpSolClassJsonObj1);

					JSONObject tmpSolClassJsonObj2 = new JSONObject();
					tmpSolClassJsonObj2.put("@ontClassName",
							"MagnifyingSoftware");
					tmpSolClassJsonObj2.put("@type", C4A_NS
							+ "AssistiveTechnology");
					tmpSolClassJsonObj2.put("@id", C4A_NS + "magnifier");
					tmpSolClassJsonArray.put(tmpSolClassJsonObj2);

					addScreenReaderTTSEnabled = true;
					addMagnifierEnabled = true;
				} else if (tmpSolution.id
						.equals("es.codefactory.android.app.ma")) // another
																	// cheat for
																	// http://registry.gpii.net/applications/es.codefactory.android.app.ma
																	// because
																	// in the
																	// .owl it's
																	// under
																	// SoftwareInterfacesForComputersAndMobileDevices
																	// while we
																	// want it
																	// under
																	// screenreaders
				{
					JSONObject tmpSolClassJsonObj = new JSONObject();
					tmpSolClassJsonObj.put("@ontClassName",
							"ScreenReaderSoftware");
					tmpSolClassJsonObj.put("@type", C4A_NS
							+ "AssistiveTechnology");
					tmpSolClassJsonObj.put("@id", C4A_NS + "screenreader");
					tmpSolClassJsonArray.put(tmpSolClassJsonObj);

					addScreenReaderTTSEnabled = true;
				} else if (tmpSolution.id.equals("org.chrome.cloud4chrome")) // another
																				// cheat
																				// for
																				// http://registry.gpii.net/applications/org.chrome.cloud4chrome
																				// because
																				// in
																				// the
																				// .owl
																				// it's
																				// under
																				// MagnifyingSoftware
																				// while
																				// we
																				// want
																				// it
																				// under
																				// browser
				{
					JSONObject tmpSolClassJsonObj = new JSONObject();
					tmpSolClassJsonObj.put("@ontClassName",
							"MagnifyingSoftware");
					tmpSolClassJsonObj.put("@type", C4A_NS
							+ "AccessibilitySolution");
					tmpSolClassJsonObj.put("@id", C4A_NS + "browser");
					tmpSolClassJsonArray.put(tmpSolClassJsonObj);

					addScreenReaderTTSEnabled = true;
				} else // all others
				{
					JSONObject tmpSolClassJsonObj = new JSONObject();
					String[] tmpClassAndId = getJSONLDClassAndIDFromOntClassName(tmpSolution.className);
					tmpSolClassJsonObj.put("@ontClassName",
							tmpSolution.className);
					tmpSolClassJsonObj.put("@type", tmpClassAndId[0]);
					tmpSolClassJsonObj.put("@id", tmpClassAndId[1]);
					tmpSolClassJsonArray.put(tmpSolClassJsonObj);

					if (tmpClassAndId[1].equals(C4A_NS + "screenreader"))
						addScreenReaderTTSEnabled = true;
					if (tmpClassAndId[1].equals(C4A_NS + "magnifier"))
						addMagnifierEnabled = true;
					if (tmpSolution.id
							.equals("com.microsoft.windows.onscreenKeyboard"))
						addOnScreenKeyboardEnabled = true;
				}

				tmpSolutionJsonObj.put(C4A_NS + "class", tmpSolClassJsonArray);

				// settings
				JSONArray tmpSolSettingsJsonArray = new JSONArray();

				// add screenReaderTTSEnabled for all screenreaders
				if (addScreenReaderTTSEnabled) {
					JSONObject tmpAddScreenReaderTTSEnabledSettingJsonObj = new JSONObject();
					tmpAddScreenReaderTTSEnabledSettingJsonObj.put("@type",
							"c4a:Setting");
					tmpAddScreenReaderTTSEnabledSettingJsonObj.put("c4a:id",
							"screenReaderTTSEnabled");
					tmpAddScreenReaderTTSEnabledSettingJsonObj
							.put("c4a:refersTo",
									"http://registry.gpii.net/common/screenReaderTTSEnabled");
					tmpAddScreenReaderTTSEnabledSettingJsonObj.put("c4a:name",
							"screenReaderTTSEnabled");
					tmpSolSettingsJsonArray
							.put(tmpAddScreenReaderTTSEnabledSettingJsonObj);
				}

				// add magnifierEnabled for all magnifiers
				if (addMagnifierEnabled) {
					JSONObject tmpAddMagnifierEnabledSettingJsonObj = new JSONObject();
					tmpAddMagnifierEnabledSettingJsonObj.put("@type",
							"c4a:Setting");
					tmpAddMagnifierEnabledSettingJsonObj.put("c4a:id",
							"magnifierEnabled");
					tmpAddMagnifierEnabledSettingJsonObj.put("c4a:refersTo",
							"http://registry.gpii.net/common/magnifierEnabled");
					tmpAddMagnifierEnabledSettingJsonObj.put("c4a:name",
							"magnifierEnabled");
					tmpSolSettingsJsonArray
							.put(tmpAddMagnifierEnabledSettingJsonObj);
				}

				// add addOnScreenKeyboardEnabled for
				// "com.microsoft.windows.onscreenKeyboard"
				if (addOnScreenKeyboardEnabled) {
					JSONObject tmpAddOnScreenKeyboardEnabledSettingJsonObj = new JSONObject();
					tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("@type",
							"c4a:Setting");
					tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("c4a:id",
							"onScreenKeyboard");
					tmpAddOnScreenKeyboardEnabledSettingJsonObj
							.put("c4a:refersTo",
									"http://registry.gpii.net/common/onScreenKeyboardEnabled");
					tmpAddOnScreenKeyboardEnabledSettingJsonObj.put("c4a:name",
							"onScreenKeyboard");
					tmpSolSettingsJsonArray
							.put(tmpAddOnScreenKeyboardEnabledSettingJsonObj);
				}

				ArrayList<Setting> allSettings = tmpSolution.settings;
				for (int j = 0; j < allSettings.size(); j++) {
					Setting tmpSetting = allSettings.get(j);
					if (tmpSetting.ignoreSetting == false
							&& tmpSetting.type != Setting.UNKNOWN
							&& tmpSetting.instanceName.startsWith("EASTIN_") == false) {
						JSONObject tmpSolSettingJsonObj = new JSONObject();
						tmpSolSettingJsonObj.put("@type", C4A_NS + "Setting");
						if (tmpSetting.hasID.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "id",
									"http://registry.gpii.net/application/"
											+ tmpSolution.id + "/"
											+ tmpSetting.hasID);

						if (tmpSetting.hasName.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "name",
									tmpSetting.hasName);
						else
							tmpSolSettingJsonObj.put(C4A_NS + "name",
									tmpSetting.instanceName);

						if (tmpSetting.value.equals("") == false
								&& tmpSetting.value.toLowerCase().trim()
										.equals("unknown") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "defaultValue",
									tmpSetting.value);

						/*
						 * if(tmpSetting.hasID.contains("common"))
						 * tmpSolSettingJsonObj.put(C4A_NS + "type", "common");
						 * else if(tmpSetting.hasID.contains("applications"))
						 * tmpSolSettingJsonObj.put(C4A_NS + "type",
						 * "application");
						 */

						if (tmpSetting.type == Setting.STRING)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"string");
						else if (tmpSetting.type == Setting.FLOAT)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"float");
						else if (tmpSetting.type == Setting.BOOLEAN)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"boolean");
						else if (tmpSetting.type == Setting.INT)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"int");
						else if (tmpSetting.type == Setting.TIME)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"time");
						else if (tmpSetting.type == Setting.DATE)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"date");
						else if (tmpSetting.type == Setting.DATETIME)
							tmpSolSettingJsonObj.put(C4A_NS + "primitive_type",
									"dateTime");

						if (tmpSetting.hasDescription.equals("") == false
								&& tmpSetting.hasDescription.toLowerCase()
										.trim().equals("missing") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasDescription",
									tmpSetting.hasDescription);
						if (tmpSetting.type != Setting.BOOLEAN
								&& tmpSetting.hasValueSpace.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasValueSpace",
									tmpSetting.hasValueSpace);
						if (tmpSetting.hasConstraints.equals("") == false
								&& tmpSetting.hasConstraints.toLowerCase()
										.trim().equals("no constraints") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasConstraints",
									tmpSetting.hasConstraints);
						if (tmpSetting.isMappedToRegTerm.equals("") == false) {
							tmpSolSettingJsonObj.put(C4A_NS + "refersTo",
									"http://registry.gpii.net/common/"
											+ tmpSetting.isMappedToRegTerm);
							tmpSolSettingJsonObj.put(
									C4A_NS + "isExactMatching",
									tmpSetting.isExactMatching);
						}
						if (tmpSetting.hasCommentsForMapping.equals("") == false
								&& tmpSetting.hasCommentsForMapping
										.toLowerCase().trim()
										.equals("no comments") == false)
							tmpSolSettingJsonObj.put(C4A_NS
									+ "hasCommentsForMapping",
									tmpSetting.hasCommentsForMapping);

						tmpSolSettingsJsonArray.put(tmpSolSettingJsonObj);
					}
				}

				tmpSolutionJsonObj.put(C4A_NS + "settings",
						tmpSolSettingsJsonArray);

				graph.put(tmpSolutionJsonObj);
			}

			// create the solution for the service synthesis that contains all
			// the services
			JSONObject serviceJsonObj = new JSONObject();
			serviceJsonObj.put("@id", "http://registry.gpii.net/applications/"
					+ "com.certh.service-synthesis");
			serviceJsonObj.put("@type", C4A_NS + "Solution");
			serviceJsonObj.put(C4A_NS + "id", "com.certh.service-synthesis");
			serviceJsonObj.put(C4A_NS + "name", "Service Synthesis");

			JSONArray servicesJsonArray = new JSONArray();

			for (int i = 0; i < OntologyManager.getInstance().allServices
					.size(); i++) {

				Solution tmpSolution = OntologyManager.getInstance().allServices
						.get(i);

				JSONObject tmObj = new JSONObject();
				tmObj.put("@type", C4A_NS + "Service");
				tmObj.put(C4A_NS + "id", tmpSolution.id);
				tmObj.put(C4A_NS + "name", tmpSolution.name);
				int serviceOrderOfExecution = -1;
				if(tmpSolution.id.equalsIgnoreCase("FontConverter"))
					serviceOrderOfExecution = 3;
				else if(tmpSolution.id.equalsIgnoreCase("Translatewebpage"))
					serviceOrderOfExecution = 2;
				else if(tmpSolution.id.equalsIgnoreCase("Callwebanywhere"))
					serviceOrderOfExecution = 1;
				
				if(serviceOrderOfExecution!=-1)
					tmObj.put(C4A_NS + "priority", serviceOrderOfExecution);
				

				// settings
				JSONArray tmpSolSettingsJsonArray = new JSONArray();

				ArrayList<Setting> allSettings = tmpSolution.settings;
				for (int j = 0; j < allSettings.size(); j++) {
					Setting tmpSetting = allSettings.get(j);
					if (tmpSetting.ignoreSetting == false
							&& tmpSetting.type != Setting.UNKNOWN
							&& tmpSetting.instanceName.startsWith("EASTIN_") == false) {
						JSONObject tmpSolSettingJsonObj = new JSONObject();
						tmpSolSettingJsonObj.put("@type", C4A_NS + "Setting");
						if (tmpSetting.hasID.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "id",
									"http://registry.gpii.net/application/"
											+ tmpSolution.id + "/"
											+ tmpSetting.hasID);

						if (tmpSetting.hasName.equals("") == false) {
							if (!tmpSetting.hasName.contains("Url"))
								tmpSolSettingJsonObj.put(C4A_NS + "name",
										tmpSetting.hasName);

							else if (tmpSetting.hasName.contains("Url")
									&& tmpSetting.hasCommentsForMapping
											.equals("input"))
								tmpSolSettingJsonObj.put(C4A_NS + "name",
										"inputUrl");
							else
								tmpSolSettingJsonObj.put(C4A_NS + "name",
										tmpSetting.hasName);
						} else
							tmpSolSettingJsonObj.put(C4A_NS + "name",
									tmpSetting.instanceName);

						if (tmpSetting.value.equals("") == false
								&& tmpSetting.value.toLowerCase().trim()
										.equals("unknown") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "defaultValue",
									tmpSetting.value);
						if (tmpSetting.hasDescription.equals("") == false
								&& tmpSetting.hasDescription.toLowerCase()
										.trim().equals("missing") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasDescription",
									tmpSetting.hasDescription);
						if (tmpSetting.type != Setting.BOOLEAN
								&& tmpSetting.hasValueSpace.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasValueSpace",
									tmpSetting.hasValueSpace);
						if (tmpSetting.hasConstraints.equals("") == false
								&& tmpSetting.hasConstraints.toLowerCase()
										.trim().equals("no constraints") == false)
							tmpSolSettingJsonObj.put(C4A_NS + "hasConstraints",
									tmpSetting.hasConstraints);
						if (tmpSetting.isMappedToRegTerm.equals("") == false) {
							tmpSolSettingJsonObj.put(C4A_NS + "refersTo",
									"http://registry.gpii.net/common/"
											+ tmpSetting.isMappedToRegTerm);
							tmpSolSettingJsonObj.put(
									C4A_NS + "isExactMatching",
									tmpSetting.isExactMatching);
						}
						if (tmpSetting.hasCommentsForMapping.equals("") == false)
							tmpSolSettingJsonObj.put(C4A_NS
									+ "type",
									tmpSetting.hasCommentsForMapping);
						
						if (tmpSetting.hasName.contains("Url"))
							tmpSolSettingJsonObj.put(C4A_NS + "refersTo",
									"http://registry.gpii.net/common/URL");

						if (tmpSetting.isMappedToRegTerm.equals("")
								&& tmpSetting.hasCommentsForMapping
										.equals("input")) {
							tmpSolSettingJsonObj.put(C4A_NS + "default", true);

							if (tmpSetting.hasName.contains("Url"))
								tmpSolSettingJsonObj.put(C4A_NS + "value",
										"URL_TO_BE_REPLACED");
						}

						tmpSolSettingsJsonArray.put(tmpSolSettingJsonObj);
					}
				}

				tmObj.put(C4A_NS + "settings", tmpSolSettingsJsonArray);
				servicesJsonArray.put(tmObj);

			}
			serviceJsonObj.put("c4a:service", servicesJsonArray);
			// add the big solution of service synthesis
			graph.put(serviceJsonObj);

			// cheat for manually adding com.microsoft.windows.displaySettings -
			// it is not included in .owl as it has no settings
			JSONObject tmpDisplaySettingsJsonObj = new JSONObject();
			tmpDisplaySettingsJsonObj
					.put("@id",
							"http://registry.gpii.net/applications/com.microsoft.windows.displaySettings");
			tmpDisplaySettingsJsonObj.put("@type", "c4a:Solution");
			tmpDisplaySettingsJsonObj
					.put("c4a:id",
							"http://registry.gpii.net/applications/com.microsoft.windows.displaySettings");
			tmpDisplaySettingsJsonObj.put("c4a:name",
					"com.microsoft.windows.displaySettings");
			JSONArray tmpDisplaySettingsClassJsonArray = new JSONArray();
			JSONObject tmpDisplaySettingsClassJsonObj = new JSONObject();
			tmpDisplaySettingsClassJsonObj.put("@type",
					"c4a:AccessibilitySetting");
			tmpDisplaySettingsClassJsonObj.put("@id", "c4a:builtin");
			tmpDisplaySettingsClassJsonArray
					.put(tmpDisplaySettingsClassJsonObj);
			tmpDisplaySettingsJsonObj.put("c4a:class",
					tmpDisplaySettingsClassJsonArray);
			graph.put(tmpDisplaySettingsJsonObj);
			// -cheat for manually adding com.microsoft.windows.displaySettings

			// cheat for manually added some extra json content
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
			// -cheat for manually added some extra json content

			result.put("@context", context);
			result.put("@graph", graph);

			com.gpii.utils.Utils
					.getInstance()
					.writeFile(
							System.getProperty("user.dir")
									+ JsonLDManager.getInstance().WEBINF_PATH
									+ JsonLDManager.getInstance().semanticsGeneratedFromOwlFilePath,
							result.toString(4));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String[] getJSONLDClassAndIDFromOntClassName(String tmpOntClassName) {
		String C4A_NS = "c4a:";
		String[] res = new String[2]; // [0] is for class and [1] is for id
		res[0] = "...unknown...";
		res[1] = "...unknown...";

		if (tmpOntClassName.equals("ScreenReaderSoftware")) {
			res[0] = C4A_NS + "AssistiveTechnology";
			res[1] = C4A_NS + "screenreader";
		} else if (tmpOntClassName.equals("WebBrowsers")) {
			res[0] = C4A_NS + "AccessibilitySolution";
			res[1] = C4A_NS + "browser";
		} else if (tmpOntClassName.equals("MagnifyingSoftware")) {
			res[0] = C4A_NS + "AssistiveTechnology";
			res[1] = C4A_NS + "magnifier";
		} else if (tmpOntClassName
				.equals("SoftwareForAdjustingColorCombinationAndTextSize")
				|| tmpOntClassName.equals("OnScreenKeyboard")
				|| tmpOntClassName.equals("MouseControlSoftware")
				|| tmpOntClassName
						.equals("SoftwareToModifyThePointerAppearance")
				|| tmpOntClassName
						.equals("SoftwareInterfacesForComputersAndMobileDevices")) {
			res[0] = C4A_NS + "AccessibilitySetting";
			res[1] = C4A_NS + "builtin";
		} else if (tmpOntClassName.equals("AlternativeInputDevices")) {
			res[0] = C4A_NS + "AccessibilitySolution";
			res[1] = C4A_NS + "os";
		}

		return res;
	}

	public String transformInput(String in) throws JSONException {
		String inputString = in;
		JSONTokener inputTokener = new JSONTokener(inputString);
		JSONObject mmIn = new JSONObject(inputTokener);

		JSONObject outPreProc = new JSONObject();
		JSONObject outContext = new JSONObject();
		JSONArray outGraph = new JSONArray();

		if (mmIn.has("preferences")) {
			JSONObject inContext = mmIn.getJSONObject("preferences")
					.getJSONObject("contexts");

			/**
			 * Translate preferences sets IN: "gpii-default": { "name":
			 * "Default preferences", "preferences": {
			 * "http://registry.gpii.net/common/fontSize": 15, } } GOAL: {
			 * "@id": "c4a:nighttime-at-home", "@type": "c4a:PreferenceSet",
			 * "c4a:id": "nighttime-at-home", "c4a:name": "Nighttimeathome",
			 * "c4a:hasPrefs": [{ "c4a:id":
			 * "http://registry.gpii.net/common/fontSize", "@type":
			 * "c4a:Preference", "c4a:type": "common", "c4a:setting": [ {
			 * "c4a:name": "fontSize", "c4a:value": "18" } ]
			 * 
			 * }]
			 */
			Iterator<?> cKeys = inContext.keys();
			while (cKeys.hasNext()) {
				String cID = (String) cKeys.next();
				String cName = inContext.getJSONObject(cID).get("name")
						.toString();

				JSONObject outPrefSet = new JSONObject();
				outPrefSet.put("@id", "c4a:" + cID);
				outPrefSet.put("@type", "c4a:PreferenceSet");
				outPrefSet.put("c4a:id", cID);
				outPrefSet.put("c4a:name", cName);

				// translate preferences and add hasPrefs relation
				JSONObject cPrefs = inContext.getJSONObject(cID).getJSONObject(
						"preferences");

				JSONArray outPrefArray = new JSONArray();
				Iterator<?> pKeys = cPrefs.keys();
				while (pKeys.hasNext()) {
					// common, e.g.
					// http://registry.gpii.net/common/highContrastEnabled
					// application, e.g
					// http://registry.gpii.net/applications/org.chrome.cloud4chrome
					String pID = (String) pKeys.next();

					// get the path of pID
					URI uri = null;
					try {
						uri = new URI(pID);
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String path = uri.getPath();

					// create a preference object:
					JSONObject outPref = new JSONObject();
					outPref.put("c4a:id", pID);
					outPref.put("@type", "c4a:Preference");

					// handle common preferences
					if (pID.contains("common")) {
						// get preference name from path
						String idStr = path
								.substring(path.lastIndexOf('/') + 1);

						// get common preference value
						Object comPrefVal = cPrefs.get(pID);

						// add type and name to the preference object
						outPref.put("c4a:type", "common");
						outPref.put("c4a:name", idStr);

						// transform data type of the preference value
						/**
						 * JSON-LD supports automatically typing of values for
						 * Integer, Double, String and Boolean. This is a
						 * workaround for the loss of type information in
						 * numbers representing decimal or float (1.5) in
						 * JSON-LD.
						 * 
						 */
						try {
							float i = Float.parseFloat(comPrefVal.toString());
							comPrefVal = i;
							JSONObject valObject = new JSONObject();
							valObject.put("@value", comPrefVal.toString());
							valObject.put("@type", "xsd:float");
							comPrefVal = valObject;
						} catch (NumberFormatException e) {
						}

						outPref.put("c4a:value", comPrefVal);
					}

					// handle application-specific preferences
					if (pID.contains("applications")) {
						// app-specific preference value is always a JSONObject,
						// e.g. { fontsize: 0.5, invertColours:false}
						JSONObject appPrefValueObject = cPrefs
								.getJSONObject(pID);
						Iterator<?> setKeys = appPrefValueObject.keys();
						JSONArray settingSet = new JSONArray();
						while (setKeys.hasNext()) {
							JSONObject setting = new JSONObject();
							String appPrefID = (String) setKeys.next();
							String appPrefValue = appPrefValueObject.get(
									appPrefID).toString();
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
				if (inContext.getJSONObject(cID).has("metadata")) {
					JSONArray cMetaOuter = inContext.getJSONObject(cID)
							.getJSONArray("metadata");

					// output array
					JSONArray outMetaArray = new JSONArray();

					for (int i = 0; i < cMetaOuter.length(); i++) {
						JSONObject cMeta = cMetaOuter.getJSONObject(i);

						// new JSONObject for each metadata blob
						JSONObject outMetaObject = new JSONObject();

						outMetaObject.put("@type", "c4a:Metadata");
						outMetaObject.put("c4a:type", cMeta.get("type")
								.toString());
						outMetaObject.put("c4a:value", cMeta.get("value")
								.toString());
						outMetaObject.put("c4a:scope",
								cMeta.getJSONArray("scope"));

						outMetaArray.put(outMetaObject);
					}
					outPrefSet.put("c4a:hasMetadata", outMetaArray);
				}

				// translate condition and add hasCondition relation
				if (inContext.getJSONObject(cID).has("conditions")) {
					JSONArray cCondOuter = inContext.getJSONObject(cID)
							.getJSONArray("conditions");

					// output array
					JSONArray outCondArray = new JSONArray();

					for (int i = 0; i < cCondOuter.length(); i++) {
						JSONObject cMeta = cCondOuter.getJSONObject(i);

						// new JSONObject for each metadata blob
						JSONObject outMetaObject = new JSONObject();

						outMetaObject.put("@type", "c4a:Condition");

						Iterator<?> condKeys = cMeta.keys();
						while (condKeys.hasNext()) {
							String condKey = (String) condKeys.next();
							outMetaObject.put("c4a:" + condKey,
									cMeta.get(condKey).toString());
						}
						outCondArray.put(outMetaObject);
					}
					outPrefSet.put("c4a:hasCondition", outCondArray);
				}
				outGraph.put(outPrefSet);
			}
		}

		if (mmIn.has("deviceReporter")) {
			JSONObject inDevice = mmIn.getJSONObject("deviceReporter");

			/**
			 * Translate operating system; IN: "OS": { "id": "win32", "version":
			 * "5.0.0" }, GOAL: { "@id": "c4a:win32", "@type":
			 * "c4a:OperatingSystem", "c4a:name": "win32" },
			 */
			if (inDevice.has("OS")) {
				JSONObject inOS = inDevice.getJSONObject("OS");
				String osID = inOS.get("id").toString();
				String osVer = inOS.get("version").toString();

				JSONObject outOS = new JSONObject();
				outOS.put("@type", "c4a:OperatingSystem");
				outOS.put("@id", "c4a:" + osID);
				outOS.put("name", osID);
				outOS.put("version", osVer);

				outGraph.put(outOS);
			}

			/**
			 * Translate installed solutions; IN: solutions": [ { "id":
			 * com.cats.org } ] GOAL: { "@id":
			 * "http://registry.gpii.org/applications/com.cats.org", "@type":
			 * "c4a:InstalledSolution", "c4a:name": "com.cats.org" },
			 */
			if (inDevice.has("solutions")) {
				JSONArray inSol = inDevice.getJSONArray("solutions");
				for (int i = 0; i < inSol.length(); i++) {
					String solID = inSol.getJSONObject(i).get("id").toString();

					JSONObject outSol = new JSONObject();
					outSol.put("@type", "c4a:InstalledSolution");
					outSol.put("@id", "http://registry.gpii.net/applications/"
							+ solID);
					outSol.put("name", solID);

					outGraph.put(outSol);
				}
			}
		}

		outContext.put("c4a", "http://rbmm.org/schemas/cloud4all/0.1/");
		outContext.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		outContext.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		outPreProc.put("@context", outContext);
		outPreProc.put("@graph", outGraph);

		/**
		 * make it configurable to spec the indent factor (number of spaces to
		 * add to each level of indentation).
		 */
		return outPreProc.toString(5);
	}
	
	public class Element{
		private String name;
		private JSONObject serviceInput;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public JSONObject getServiceInput() {
			return serviceInput;
		}
		public void setServiceInput(JSONObject serviceInput) {
			this.serviceInput = serviceInput;
		}
		public Element(String name, JSONObject serviceInput) {
			super();
			this.name = name;
			this.serviceInput = serviceInput;
		}
		@Override
		public boolean equals(Object obj) {
			if (this.getName().equals(
					((Element) obj).getName())) {
				return true;
			} else
				return false;
		}
		
		
	}

	public JSONObject createCombinedServiceObjectForJson(
			ArrayList<QuerySolution> allServices,
			ArrayList<String> serviceNames, boolean screenReaderEnabled)
			throws JSONException {

		ArrayList<Element> objectServices = new ArrayList<Element>();
		JSONObject combinedSolution = new JSONObject();
		JSONObject settings = new JSONObject();
		JSONObject settingsInner = new JSONObject();
		JSONObject combinedInput = new JSONObject();
		JSONArray input = new JSONArray();
		JSONArray mappedVariables = new JSONArray();
		Element el = null;
		JSONObject innerService;
		JSONObject serviceInput;
		boolean appActive = true;
		String toServiceName = "";
		String toVariableName = "";
		String fromServiceName = "";
		String fromVariableName = "";
		String name = "";
		String propName = "";
		String propValue = "";
		JSONObject tempObj = null;
		JSONObject innerMapping = null;
		JSONObject tempMapping = null;
		boolean hasMapping = false;
		int position = -1;
		combinedSolution.put("active", true);

		settings.put(
				"http://registry.gpii.net/applications/com.certh.service-synthesis",
				settingsInner);
		settingsInner.put("serviceName", "callCombinedServices");
		settingsInner.put("serviceInput", combinedInput);

		// pre-process for screenReaders
	// TODO test screenReaderEnabled = false;
		int pos = -1;
		for (String s : serviceNames) {
			if (s.equalsIgnoreCase("CallWebAnywhere") && screenReaderEnabled) {
				pos = serviceNames.indexOf(s);
				break;
			}
		}

		if (pos != -1)
			serviceNames.remove(pos);

		ArrayList<QuerySolution> allServicesClone = new ArrayList<QuerySolution>();
		for (QuerySolution temp : allServices) {
			allServicesClone.add(temp);
		}

		for (QuerySolution temp : allServicesClone) {
			if (temp.contains("?serviceName"))
				name = temp.get("?serviceName").toString();
			
			if (name.equalsIgnoreCase("CallWebAnywhere") && screenReaderEnabled) {
				allServices.remove(temp);
			}

		}
		

		// create json objects for all services to add the services
		for (String s : serviceNames) {
			serviceInput = new JSONObject();
			el = new Element(s, serviceInput);
			objectServices.add(el);
		}
		
		System.out.println("screenReaderEnabled "+screenReaderEnabled);

		// add the properties
		for (QuerySolution temp : allServices) {

			if (temp.contains("?appActive"))
				appActive = new Boolean(temp.get("?appActive").toString());
			if (temp.contains("?fromServiceName"))
				fromServiceName = temp.get("?fromServiceName").toString();
			if (temp.contains("?toServiceName"))
				toServiceName = temp.get("?toServiceName").toString();
			if (temp.contains("?toVariableName"))
				toVariableName = temp.get("?toVariableName").toString();
			if (temp.contains("?fromVariableName"))
				fromVariableName = temp.get("?fromVariableName").toString();
			if (temp.contains("?propName"))
				propName = temp.get("?propName").toString();
			if (temp.contains("?serviceName"))
				name = temp.get("?serviceName").toString();
			if (temp.contains("?propValue"))
				propValue = temp.get("?propValue").toString();
			position = -1;
			
			if(appActive)
				if (propName.equals(toVariableName)) {

					if ((screenReaderEnabled
							&& !toServiceName
									.equalsIgnoreCase("CallWebAnywhere") && !fromServiceName
								.equals("CallWebAnywhere"))
							|| !screenReaderEnabled) {
						// create the mapping
						innerMapping = new JSONObject();
						innerMapping.put("fromServiceName", fromServiceName);
						innerMapping.put("fromVariableName", fromVariableName);
						innerMapping.put("toServiceName", toServiceName);

						if (toVariableName.equalsIgnoreCase("originalURL"))
							toVariableName = "inputUrl";

						innerMapping.put("toVariableName", toVariableName);
						// add the mapping to block of mapping objects
						// check if the mapping was already added to the
						// array
						boolean added = false;
						for (int i = 0; i < mappedVariables.length(); i++) {
							tempMapping = mappedVariables.getJSONObject(i);
							if (tempMapping.toString().equals(
									innerMapping.toString())) {
								added = true;
								break;
							}

						}
						if (!added)
							mappedVariables.put(innerMapping);
					}

				} else {

					tempObj = new JSONObject();
					// find the object to add the properties using the position
					for (int i = 0; i < objectServices.size(); i++) {
						if (objectServices.get(i).getName().equals(name)) {
							tempObj = objectServices.get(i).getServiceInput();
							position = i;
							break;
						}
					}

					tempObj.put(propName, propValue);
					objectServices.remove(position);
					objectServices.add(position, new Element(name, tempObj));
				}
		}
		
		//check duplicates
		for (QuerySolution temp : allServices) {

			if (temp.contains("?toServiceName"))
				toServiceName = temp.get("?toServiceName").toString();
			if (temp.contains("?propName"))
				propName = temp.get("?propName").toString();
			if (temp.contains("?serviceName"))
				name = temp.get("?serviceName").toString();

			String s1 = "\"toServiceName\":" + "\"" + name + "\"";
			String s2 = "\"toVariableName\":" + "\"" + propName + "\"";

			for (int i = 0; i < objectServices.size(); i++) {
				if (objectServices.get(i).getName().equals(name)) {
					tempObj = objectServices.get(i).getServiceInput();
					position = i;
					break;
				}
			}

			boolean tempFlag = false;
			for (int i = 0; i < mappedVariables.length(); i++) {
				tempMapping = mappedVariables.getJSONObject(i);

				if (tempMapping.toString().contains(s1)
						&& tempMapping.toString().contains(s2)) {

					tempFlag = true;
					break;
				}

			}

			if (tempFlag) {

				Element test = objectServices.get(position);
				if (test.getServiceInput().has(propName))
					test.getServiceInput().remove(propName);

				objectServices.remove(position);
				objectServices.add(position,
						new Element(name, test.getServiceInput()));
			}

		}
		

		// complete the jsonobjects
		for (Element tempEl : objectServices) {
			innerService = new JSONObject();
			innerService.put("serviceName", tempEl.getName());
			if (!tempEl.getServiceInput().toString().equals("{}"))
				innerService.put("serviceInput", tempEl.getServiceInput());
			input.put(innerService);
		}
		combinedInput.put("input", input);
		combinedInput.put("mappedVariables", mappedVariables);
		combinedSolution.put("settings", settings);

		// System.out.println(combinedSolution.toString(5));
		return combinedSolution;
	}

	/**
	 * Queries all requiered information from the rdf model and transforms the
	 * result the specific C4a JSON Structure.
	 * 
	 * @param model
	 * @param queries
	 * @return
	 * @throws JSONException
	 */
	public String transformOutput(Model model, String[] queries)
			throws JSONException {
		/**
		 * mmOut - JSON Object spec the matchmaker output
		 * 
		 */
		JSONObject mmOut = new JSONObject();
		boolean singleService = true;
		boolean screenReaderEnabled = false;
		ArrayList<QuerySolution> services = new ArrayList<QuerySolution>();
		ArrayList<String> serviceNames = new ArrayList<String>();

		/**
		 * infConfig - JSONObject spec the inferred configuration object of the
		 * matchmaker output
		 */
		JSONObject infConfig = new JSONObject();
		mmOut.put("inferredConfiguration", infConfig);

		for (String url : queries) {
			Query query = QueryFactory.read(System.getProperty("user.dir")
					+ JsonLDManager.getInstance().WEBINF_PATH + url);

			QueryExecution qexec = QueryExecutionFactory.create(query, model);

			try {
				ResultSet response = qexec.execSelect();

				JSONObject contextSet;
				JSONObject appSet;
				JSONObject solution = null;
				JSONObject innerSolution = null;
				JSONObject settings;
				JSONObject extraWrap;
				JSONArray conSet;
				JSONArray metaSet;
				JSONObject metadata = null;
				JSONArray scopeSet;
				JSONObject msgSet;
				ArrayList<String> foundServices = new ArrayList<String>();
				JSONObject combinedServicesObject = null;
				boolean combinedObjectAdded = false;

				String contextID = null;
				String queryType = null;

				// find if the output contains single or combined services
				// and update the flag singleServices

				if (url.contains("outServices")) {

					Query TempQuery = QueryFactory.read(System
							.getProperty("user.dir")
							+ JsonLDManager.getInstance().WEBINF_PATH + url);

					QueryExecution Tempqexec = QueryExecutionFactory.create(
							TempQuery, model);

					ResultSet tempResponse = Tempqexec.execSelect();
					while (tempResponse.hasNext()) {
						QuerySolution soln = tempResponse.nextSolution();
						if (soln.contains("?type"))
							queryType = soln.get("?type").toString();

						if (queryType.equals(defaultNameSpace
								+ "ServiceSetting")) {

						//	System.out.println("soln: " + soln);

							boolean isActive = Boolean.valueOf(soln.get(
									"?appActive").toString());
							String serviceName = soln.get("?serviceName")
									.toString();

							if ((serviceName
									.equalsIgnoreCase("CallWebAnywhere") && !screenReaderEnabled)
									|| !serviceName
											.equalsIgnoreCase("CallWebAnywhere")) {

								// add solution if it is active
								if (!serviceNames.contains(serviceName)
										&& isActive)
									serviceNames.add(soln.get("?serviceName")
											.toString());

								if (isActive)
									services.add(soln);
							}

						}

					}
						
					if (serviceNames.size() > 1) {
						singleService = false;
						combinedServicesObject = createCombinedServiceObjectForJson(
								services, serviceNames, screenReaderEnabled);
					}
				}
				
				

				// end of find singleServices

				while (response.hasNext()) {
					QuerySolution soln = response.nextSolution();

					// System.out.println("soln: " + soln.toString());

					infConfig = mmOut.getJSONObject("inferredConfiguration");

					// get context id
					if (soln.contains("?contextID"))
						contextID = soln.get("?contextID").toString();

					// get query type, e.g. Condition
					if (soln.contains("?type"))
						queryType = soln.get("?type").toString();

					/**
					 * Context - create a new context object if not exists
					 * 
					 */
					if (infConfig.has(contextID.toString()))
						contextSet = infConfig.getJSONObject(contextID
								.toString());
					else {
						appSet = new JSONObject();
						infConfig.put(contextID.toString(), appSet);
						contextSet = infConfig.getJSONObject(contextID
								.toString());
					}
					/**
					 * Application - create a new application object if not
					 * exists
					 * 
					 */
					if (queryType.equals(defaultNameSpace + "Configuration")) {
						if (contextSet.has("applications"))
							appSet = contextSet.getJSONObject("applications");
						else {
							appSet = new JSONObject();
							contextSet.put("applications", appSet);
							appSet = contextSet.getJSONObject("applications");
						}

						// add application name if not exists
						if (soln.contains("?appID")) {
							String appID = soln.get("?appID").toString();

							if (appSet.has(appID))
								solution = appSet.getJSONObject(appID);
							else {
								solution = new JSONObject();
								appSet.put(appID, solution);
								solution = appSet.getJSONObject(appID);
							}
						}

						// add activation of application
						if (soln.contains("?appActive")) {
							Boolean appActive = new Boolean(soln.get(
									"?appActive").toString());

							if (!solution.has("active"))
								solution.put("active", appActive);
						}

						// add settings to separate settings block.
						if (soln.contains("?setID")
								&& soln.contains("setValue")
								&& soln.contains("setName")) {
							String setId = soln.get("?setID").toString();
							String setName = soln.get("?setName").toString();
							Object setValue = soln.get("?setValue");
							
							
							//check if screenReaderTTSEnabled is t
							if (setId.contains("screenReaderTTSEnabled")
									&& Boolean.valueOf(setValue.toString())
									&& Boolean.valueOf(soln.get("?appActive")
											.toString())) {
								screenReaderEnabled = true;
							}

							// Transform value data types
							setValue = Utils.getInstance().transformValueSpace(
									setValue);

							// Create or get a setting object
							if (solution.has("settings"))
								settings = solution.getJSONObject("settings");
							else {
								settings = new JSONObject();
								solution.put("settings", settings);
								settings = solution.getJSONObject("settings");
							}

							// Add settings to setting object
							if (setId
									.contains("registry.gpii.net/applications/")) {
								/**
								 * Add settings in app-specific representation:
								 * "http://registry.gpii.net/applications/org.chrome.cloud4chrome"
								 * : { "fontSize" : "medium" }
								 */
								if (settings.has(setId))
									extraWrap = settings.getJSONObject(setId);
								else {
									extraWrap = new JSONObject();
									settings.put(setId, extraWrap);
									extraWrap = settings.getJSONObject(setId);
								}
								extraWrap.put(setName, setValue);

							} else
								/**
								 * Add setting in common representation:
								 * "http://registry.gpii.net/common/screenResolution"
								 * : "medium"
								 */
								settings.put(setId, setValue);
						}
					}

					/**
					 * Condition - create a new condition array if not exists
					 * 
					 */
					if (queryType.equals(defaultNameSpace + "Condition")) {
						if (contextSet.has("conditions"))
							conSet = contextSet.getJSONArray("conditions");
						else {
							conSet = new JSONArray();
							contextSet.put("conditions", conSet);
							conSet = contextSet.getJSONArray("conditions");
						}
						// create a new condition object and put it to the
						// condition array
						JSONObject condition = new JSONObject();
						/**
						 * TODO: condition payload does not use generic keys.
						 * min or max are specific to a specific type of
						 * condition. This should be improved in general.
						 * 
						 */
						condition.put("type", soln.get("?condOp").toString());
						condition.put("inputPath", soln.get("?condPa")
								.toString());

						Object setMin = soln.get("?condMi");
						Object setMax = soln.get("?condMa");

						condition.put("min", Utils.getInstance()
								.transformValueSpace(setMin));
						condition.put("max", Utils.getInstance()
								.transformValueSpace(setMax));

						conSet.put(condition);
					}
					/**
					 * Metadata - create a new metadata array if not exists
					 * 
					 */
					if (queryType.equals(defaultNameSpace + "Metadata")) {
						// add metadata section to the context block
						if (contextSet.has("metadata"))
							metaSet = contextSet.getJSONArray("metadata");
						else {
							metaSet = new JSONArray();
							contextSet.put("metadata", metaSet);
							metaSet = contextSet.getJSONArray("metadata");
						}

						String metaType = soln.get("?metaType").toString();
						String metaScopeID = soln.get("?metaScopeID")
								.toString();
						String metaScopeName = soln.get("?metaScopeName")
								.toString();
						String metaScopeClass = soln.get("?metaScopeClass")
								.toString();

						// check if there is already an meta data object with
						// scope (solution) AND type (e.g. helpMessage)
						boolean scopeExists = false;
						boolean typeExists = false;
						int i;
						JSONObject tmp_meta = null;
						System.out.println("meta set : " + metaSet.toString());
						for (i = 0; i < metaSet.length(); i++) {

							tmp_meta = metaSet.getJSONObject(i);
							JSONArray tmp_scope = tmp_meta
									.getJSONArray("scope");

							for (int j = 0; j < tmp_scope.length(); j++) {
								if (tmp_scope.getString(j).equals(metaScopeID)) {

									scopeExists = true;

									if (tmp_meta.has("type")) {
										if (tmp_meta.getString("type").equals(
												metaType)) {
											typeExists = true;
											metadata = tmp_meta;
										}
									}
								}
							}
						}

						if (!(scopeExists && typeExists)) {
							metadata = new JSONObject();
							metadata.put("type", metaType);
							JSONArray scope = new JSONArray();
							scope.put(metaScopeID);
							metadata.put("scope", scope);
							metaSet.put(i, metadata);
							metadata = metaSet.getJSONObject(i);
						}

						// message
						if (metadata.has("message"))
							msgSet = metadata.getJSONObject("message");
						else {
							msgSet = new JSONObject();
							metadata.put("message", msgSet);
							msgSet = metadata.getJSONObject("message");
						}

						JSONObject msg = new JSONObject();
						String msgText = soln.get("?msgText").toString();
						String msgLang = soln.get("?msgLang").toString();
						String msgLearnMore = soln.get("?msgLearnMore")
								.toString();

						msgText = msgText.replaceAll("SOLUTION_TO_BE_REPLACED",
								metaScopeName);
						msgText = msgText.replaceAll("CLASS_TO_BE_REPLACED",
								metaScopeClass);
						msg.put("message", msgText);
						if (!(msgLearnMore.equals("LINK_TO_BE_REPLACED")))
							msg.put("learnMore", msgLearnMore);
						msgSet.put(msgLang, msg);
					}

					/**
					 * 
					 * Specific translation for service as they require specific
					 * output structure:
					 * 
					 */
					if (queryType.equals(defaultNameSpace + "ServiceSetting")) {

						String appName = "";
						String appID = "";
						String serviceName = "";
						String propName = "";
						String propValue = "";
						JSONObject serviceInput;
						JSONArray serviceArrWrap = null;

						// get SPARQL attributes
						if (soln.contains("?serviceName")) {
							serviceName = soln.get("?serviceName").toString();
						}
						
						if((serviceName.equals("CallWebAnywhere") && !screenReaderEnabled) ||
								!serviceName.equalsIgnoreCase("CallWebAnywhere")){
						if (soln.contains("?propName")) {
							propName = soln.get("?propName").toString();
						}
						if (soln.contains("?propValue"))
							propValue = soln.get("?propValue").toString();

						if (soln.contains("?appID"))
							appID = soln.get("?appID").toString();

						// create JSON output structure
						/**
						 * create and get appSet - JSONObject with all
						 * configurations + { "inferredConfiguration": {
						 * "gpii-default": { "applications": { ... } } } }
						 */
						if (contextSet.has("applications"))
							appSet = contextSet.getJSONObject("applications");
						else {
							appSet = new JSONObject();
							contextSet.put("applications", appSet);
							appSet = contextSet.getJSONObject("applications");
						}

						/**
						 * create an array for all services {
						 * "inferredConfiguration": { "gpii-default": {
						 * "applications": { "com.certh.service-synthesis": [
						 * ... ],
						 * 
						 */
						if (soln.contains("?appName")) {
							appName = soln.get("?appName").toString();

							if (appSet.has(appName))
								serviceArrWrap = appSet.getJSONArray(appName);
							else {
								serviceArrWrap = new JSONArray();
								appSet.put(appName, serviceArrWrap);
								serviceArrWrap = appSet.getJSONArray(appName);
							}

						}

						/**
						 * create a solution object for each service [{
						 * "active": true, "settings": { ... } }, { "active":
						 * true, "settings": { ... } }]
						 */
						if (singleService) {
							// single scenario with services
							boolean serviceExists = false;
							int i;
							for (i = 0; i < serviceArrWrap.length(); i++) {

								// find the object with the service name
								// "serviceName"
								JSONObject tmp_solution = serviceArrWrap
										.getJSONObject(i);

								if (tmp_solution.has("settings")) {

									JSONObject tmp_setting = tmp_solution
											.getJSONObject("settings");

									if (tmp_setting.has(appID)) {

										JSONObject tmp_properties = tmp_setting
												.getJSONObject(appID);

										if (tmp_properties.has("serviceName")) {

											if (serviceName
													.equals(tmp_properties.get(
															"serviceName")
															.toString())) {
												serviceExists = true;
												solution = serviceArrWrap
														.getJSONObject(i);

											}
										}
									}
								}
							}
							if (!serviceExists) {
								solution = new JSONObject();
								serviceArrWrap.put(i, solution);
								solution = serviceArrWrap.getJSONObject(i);
							}

							// add activation of application
							if (soln.contains("?appActive")) {
								Boolean appActive = new Boolean(soln.get(
										"?appActive").toString());

								if (!solution.has("active"))
									solution.put("active", appActive);
							}

							/**
							 * Add settings in app-specific representation:
							 * "http://registry.gpii.net/applications/com.certh.service-synthesis"
							 * : { "serviceName": "Translatewebpage",
							 * "serviceInput": { "originalURL":
							 * "URL_TO_BE_REPLACED", "targetLanguage": "fr" } }
							 */

							if (solution.has("settings"))
								settings = solution.getJSONObject("settings");
							else {
								settings = new JSONObject();
								solution.put("settings", settings);
								settings = solution.getJSONObject("settings");
							}

							if (settings.has(appID))
								extraWrap = settings.getJSONObject(appID);
							else {
								extraWrap = new JSONObject();
								settings.put(appID, extraWrap);
								extraWrap = settings.getJSONObject(appID);
							}

							extraWrap.put("serviceName", serviceName);

							if (extraWrap.has("serviceInput"))
								serviceInput = extraWrap
										.getJSONObject("serviceInput");
							else {
								serviceInput = new JSONObject();
								extraWrap.put("serviceInput", serviceInput);
								serviceInput = extraWrap
										.getJSONObject("serviceInput");
							}

							serviceInput.put(propName, Utils.getInstance()
									.transformValueSpace(propValue));

						} else if(!singleService && !combinedObjectAdded) {
							// TODO : COMBINED SCENARIO WITH SERVICES
							serviceArrWrap.put(combinedServicesObject);
							combinedObjectAdded = true;
						}

					}
					}
				}
					

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} finally {
				qexec.close();
			}
		}

		/**
		 * make it configurable to spec the indent factor (number of spaces to
		 * add to each level of indentation).
		 */
		return mmOut.toString(5);
	}

	private JSONObject objectContains(JSONArray metaData, String metaType,
			String metaScope) throws JSONException {
		boolean typeSupported = false;
		boolean scopeSupported = false;
		JSONObject match = null;
		JSONObject next = null;

		if (metaData.length() > 0) {
			for (int i = 0; i < metaData.length(); i++) {
				next = metaData.getJSONObject(i);

				// type
				if (next.get("type").toString().equals(metaType))
					typeSupported = true;

				// scope
				JSONArray scopeSet = next.getJSONArray("scope");

				for (int j = 0; j < scopeSet.length(); j++) {
					if (scopeSet.get(j).toString().equals(metaScope))
						scopeSupported = true;
				}
			}
			if (typeSupported && scopeSupported)
				match = next;
		}
		return match;
	}

	public static boolean contains(JSONArray array, String string)
			throws JSONException {
		boolean r = false;

		if (array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				if (array.getString(i).equals(string))
					r = true;
				else
					r = false;
			}
		} else
			r = false;
		return r;
	}

}