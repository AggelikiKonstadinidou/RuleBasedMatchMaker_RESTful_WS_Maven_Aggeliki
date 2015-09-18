package com.gpii.ontology;


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.github.jsonldjava.jena.JenaJSONLD;
import com.gpii.jsonld.JsonLDManager;
import com.gpii.transformer.TransformerManager;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.impl.IndividualImpl;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.StatementImpl;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

/**
 *
 * @author nkak
 * @author Claudia Loitsch
 * 
 */
public class OntologyManager
{
    private static OntologyManager instance = null;
    
    //model initialized from solutions ontology (semanticFrameworkOfContentAndSolutions.owl)
    public OntModel model;
    public static final String SOURCE = "http://www.cloud4all.eu/SemanticFrameworkForContentAndSolutions.owl";
    public static final String NS = SOURCE + "#";
    //ArrayList containing all the solutions as defined in the ontology
    public ArrayList<Solution> allSolutions;
    public ArrayList<Solution> allServices;
    
    // default model automatically initialized with data from JSON-LD  	
    public static Model _dmodel;
    
    boolean printDebugInfo;
    public String debug;
    
    private OntologyManager() 
    {
        debug = "";
        printDebugInfo = false;
        allSolutions = new ArrayList<Solution>();
        allServices = new ArrayList<Solution>();
        
        // create an empty model
        model = ModelFactory.createOntologyModel();
        
        // JenaJSONLD must be initialized so that the readers and writers are registered with Jena.
        JenaJSONLD.init();
    }
    
    public static OntologyManager getInstance() 
    {
        if(instance == null) 
            instance = new OntologyManager();
        return instance;
    }
 //   test the transformation
    public static void main(String args[]) {
		// OntologyManager.getInstance().loadOntology();
		TransformerManager.getInstance().transformOwlToJSONLD();
	}
    
    public void loadOntology()
    {
    	String owlPathStr = JsonLDManager.getInstance().solutionsOntology;
		InputStream in = null;

		try {
			in = new FileInputStream(owlPathStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (in == null) {
			System.out
					.println("File: semanticFrameworkOfContentAndSolutions.owl not found!");
			throw new IllegalArgumentException(
					"File: semanticFrameworkOfContentAndSolutions.owl not found!");
		}

		// read the RDF/XML file
		model.read(in, "");

		try {
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// fill "allSolutions" ArrayList
		ExtendedIterator classes = model.listClasses();
		OntClass solutionsClass = null;
		OntClass servicesClass = null;
		while (classes.hasNext()) {
			OntClass tmpClass = (OntClass) classes.next();
			if (tmpClass.getLocalName() != null) {
				// System.out.println("class local name: " +
				// tmpClass.getLocalName());
				boolean isASubclassOfSolutions = false;
				boolean isASubclassOfServices = false;
				// get parent class
				for (Iterator<OntClass> i = tmpClass.listSuperClasses(false); i
						.hasNext();) {
					OntClass tmpRootClass = i.next();
					// we only need to get classes that are under the
					// "Solutions" class
					if (tmpRootClass.getLocalName().equals("Solutions")) {
						isASubclassOfSolutions = true;
						solutionsClass = tmpClass;

					}
					// System.out.println("\tRoot class local name: " +
					// tmpRootClass.getLocalName());

					// get the classes that are under the "Service" class
					if (tmpRootClass.getLocalName().equals("Service")) {
						servicesClass = tmpClass;
						isASubclassOfServices = true;
					}

					// break if both classes have been found
					if (isASubclassOfServices && isASubclassOfSolutions)
						break;
				}

				// load solutions
				if (isASubclassOfSolutions) {
					// System.out.println("class name: " +
					// tmpClass.getLocalName());
					ExtendedIterator instances = solutionsClass
							.listInstances(true);

					while (instances.hasNext()) {
						Individual tmpInstance = (Individual) instances.next();

						Solution tmpSolution = new Solution();
						tmpSolution.className = tmpInstance.getOntClass()
								.getLocalName();
						tmpSolution.name = tmpInstance.getLocalName();
						tmpSolution.id = tmpInstance
								.getPropertyValue(model.getProperty(NS, "id"))
								.asLiteral().getValue().toString();
						// System.out.println("\t instance: " +
						// tmpInstance.getLocalName());
						// System.out.println("\t\tid: " +
						// tmpInstance.getPropertyValue(model.getProperty(NS,
						// "id")).asLiteral().getValue().toString());

						// get solution settings
						NodeIterator settingsInstances = tmpInstance
								.listPropertyValues(model.getProperty(NS,
										"hasSolutionSpecificSettings"));
						while (settingsInstances.hasNext()) {
							RDFNode tmpSettingsInstanceRDFNode = (RDFNode) settingsInstances
									.next();
							// System.out.println("\t\tsettings instance: " +
							// tmpSettingsInstanceRDFNode.toString());
							Individual tmpSettingsInstance = model
									.getIndividual(tmpSettingsInstanceRDFNode
											.toString());
							StmtIterator allSettingsIntanceProperties = tmpSettingsInstance
									.listProperties();
							while (allSettingsIntanceProperties.hasNext()) {
								Statement tmpStatement = allSettingsIntanceProperties
										.next();

								Setting tmpSetting = new Setting();
								// System.out.println("\t\t\t\tproperty: " +
								// tmpStatement.toString());
								if (tmpStatement.getPredicate() != null) {
									// System.out.println("\t\t\t\t predicate: "
									// +
									// tmpStatement.getPredicate().getLocalName());
									tmpSetting.instanceName = tmpStatement
											.getPredicate().getLocalName();
								}
								if (tmpStatement.getObject() != null) {
									// System.out.println("object: " +
									// tmpStatement.getObject());
									if (tmpStatement.getObject().isLiteral()) {
										String valueStr = tmpStatement
												.getObject()
												.toString()
												.substring(
														0,
														tmpStatement
																.getObject()
																.toString()
																.indexOf("^^"));
										String typeStr = tmpStatement
												.getObject()
												.toString()
												.substring(
														tmpStatement
																.getObject()
																.toString()
																.indexOf("^^") + 2);
										// System.out.println("\t\t\t\t valueStr: "
										// + valueStr + ", type: " + typeStr);

										tmpSetting.value = valueStr;
										if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#string"))
											tmpSetting.type = Setting.STRING;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#boolean"))
											tmpSetting.type = Setting.BOOLEAN;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#float"))
											tmpSetting.type = Setting.FLOAT;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#int"))
											tmpSetting.type = Setting.INT;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#time"))
											tmpSetting.type = Setting.TIME;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#date"))
											tmpSetting.type = Setting.DATE;
										else if (typeStr
												.equals("http://www.w3.org/2001/XMLSchema#dateTime"))
											tmpSetting.type = Setting.DATETIME;
										else
											System.out
													.println("Exception! THIS TYPE IS NOT INCLUDED! -> "
															+ tmpStatement
																	.getObject());

									} else // object property
									{
										// System.out.println("subject: " +
										// tmpStatement.getSubject());
										// System.out.println("predicate: " +
										// tmpStatement.getPredicate());
										// System.out.println("object: " +
										// tmpStatement.getObject());

										// TODO: include also other
										// ObjectProperties

										if (tmpStatement.getPredicate()
												.toString()
												.endsWith("_isMappedToRegTerm")) {
											// get mapped common term
											Individual tmpRegTermInstance = model
													.getIndividual(tmpStatement
															.getObject()
															.toString());
											tmpSetting.isMappedToRegTerm = tmpRegTermInstance
													.getPropertyValue(
															model.getProperty(
																	NS,
																	"RegistryTerm_hasID"))
													.asLiteral().getValue()
													.toString();
										}
									}
								}

								if (tmpSetting.instanceName.equals("type") == false
										&& tmpSetting.instanceName
												.equals("adapting") == false) {
									// System.out.println("\t\t\tsetting -> " +
									// tmpSetting.toString());
									// tmpSetting.process();
									tmpSolution.settings.add(tmpSetting);
								}

							}
						}

						allSolutions.add(tmpSolution);
					}
				}

				// load services
				if (isASubclassOfServices) {

					ArrayList<IndividualImpl> instances = (ArrayList<IndividualImpl>) servicesClass
							.listInstances(true).toList();

					for (int i = 0; i < instances.size(); i++) {
						IndividualImpl tmpInstance = instances.get(i);

						Solution tmpSolution = new Solution();
						tmpSolution.className = tmpInstance.getOntClass()
								.getLocalName();
						tmpSolution.name = "Service_"
								+ tmpInstance.getLocalName();
						tmpSolution.id = tmpInstance
								.getPropertyValue(
										model.getProperty(NS, "serviceId"))
								.asLiteral().getValue().toString();

						System.out.println(tmpSolution.id);

						// get service input parameters
						ObjectProperty hasInput = model.getObjectProperty(NS
								+ "hasInput");
						ObjectProperty hasOutput = model.getObjectProperty(NS
								+ "hasOutput");
						// get the registry term to witch is mapped the input
						// parameter
						ObjectProperty parameterIsMappedToTerm = model
								.getObjectProperty(NS
										+ "parameterIsMappedToTerm");
						DatatypeProperty RegistryTerm_hasID = model
								.getDatatypeProperty(NS
										+ "RegistryTerm_hasID");

						if (tmpInstance.getPropertyResourceValue(hasInput) != null) {

							StmtIterator it2 = tmpInstance
									.listProperties(hasInput);
							while (it2.hasNext()) {

								Setting tmpSetting = new Setting();
								StatementImpl st = (StatementImpl) it2.next();
								Resource r = st.getResource();
								Individual ind = model
										.getIndividual(r.getURI());
								System.out.println(ind.getURI());
								// name, id of parameter
								tmpSetting.instanceName = st.getObject()
										.asResource().getLocalName();
								tmpSetting.hasName = st.getObject()
										.asResource().getLocalName();

								tmpSetting.hasID = st.getObject().asResource()
										.getLocalName();
								// mapped to registry term
								if (ind.getPropertyResourceValue(parameterIsMappedToTerm) != null) {

									StmtIterator it3 = ind
											.listProperties(parameterIsMappedToTerm);
									while (it3.hasNext()) {
										StatementImpl termSt = (StatementImpl) it3
												.next();
										Resource res = termSt.getResource();
										Individual termInd = model
												.getIndividual(res.getURI());

										tmpSetting.isMappedToRegTerm = termInd
												.getPropertyValue(
														RegistryTerm_hasID)
												.asLiteral().getString();
									}

								}
                                tmpSetting.hasCommentsForMapping =  "input";
								tmpSetting.hasValueSpace = "string";
								tmpSetting.ignoreSetting = false;

								// TODO
								tmpSetting.type = Setting.STRING;

								tmpSolution.settings.add(tmpSetting);
							}
						}
						
						if (tmpInstance.getPropertyResourceValue(hasOutput) != null) {

							StmtIterator it2 = tmpInstance
									.listProperties(hasOutput);
							while (it2.hasNext()) {

								Setting tmpSetting = new Setting();
								StatementImpl st = (StatementImpl) it2.next();
								Resource r = st.getResource();
								Individual ind = model
										.getIndividual(r.getURI());
								System.out.println(ind.getURI());
								// name, id of parameter
								tmpSetting.instanceName = st.getObject()
										.asResource().getLocalName();
								tmpSetting.hasName = st.getObject()
										.asResource().getLocalName();

								tmpSetting.hasID = st.getObject().asResource()
										.getLocalName();
								// mapped to registry term
								if (ind.getPropertyResourceValue(parameterIsMappedToTerm) != null) {

									StmtIterator it3 = ind
											.listProperties(parameterIsMappedToTerm);
									while (it3.hasNext()) {
										StatementImpl termSt = (StatementImpl) it3
												.next();
										Resource res = termSt.getResource();
										Individual termInd = model
												.getIndividual(res.getURI());

										tmpSetting.isMappedToRegTerm = termInd
												.getPropertyValue(
														RegistryTerm_hasID)
												.asLiteral().getString();
									}

								}
                                tmpSetting.hasCommentsForMapping =  "output";
								tmpSetting.hasValueSpace = "string";
								tmpSetting.ignoreSetting = false;

								// TODO
								tmpSetting.type = Setting.STRING;

								tmpSolution.settings.add(tmpSetting);
							}
						}
						allServices.add(tmpSolution);
					}
				}
			}
		}

		System.out.println(allServices.size());
		System.out.println(allSolutions.size());
        
    }
    
    public void processSolutionSettings()
    {
        for(int i=0; i<allSolutions.size(); i++)
        {
            Solution tmpSolution = allSolutions.get(i);
            //System.out.println("SOLUTION name: " + tmpSolution.name + ", id: " + tmpSolution.id);
            
            ArrayList<Setting> allSettings = tmpSolution.settings;
            for(int j=0; j<allSettings.size(); j++)
            {
                Setting tmpSetting = allSettings.get(j);
                
                if( tmpSetting.instanceName.endsWith("_hasID")
                        || tmpSetting.instanceName.endsWith("_hasName")
                        || tmpSetting.instanceName.endsWith("_hasDescription")
                        || tmpSetting.instanceName.endsWith("_hasValueSpace")
                        || tmpSetting.instanceName.endsWith("_hasConstraints")
                        || tmpSetting.instanceName.endsWith("_isMappedToRegTerm")
                        || tmpSetting.instanceName.endsWith("_isExactMatching")
                        || tmpSetting.instanceName.endsWith("_hasCommentsForMapping") )
                {
                    tmpSetting.ignoreSetting = true;
                    
                    String originalSettingName = tmpSetting.instanceName.substring(0, tmpSetting.instanceName.lastIndexOf("_"));
                    Setting originalSetting = getSetting(tmpSolution.name, originalSettingName);
                    
                    if(originalSetting != null)
                    {
                        if(tmpSetting.instanceName.endsWith("_hasID"))
                            originalSetting.hasID = tmpSetting.value;
                        if(tmpSetting.instanceName.endsWith("_hasName"))
                            originalSetting.hasName = tmpSetting.value;
                        if(tmpSetting.instanceName.endsWith("_hasDescription"))
                            originalSetting.hasDescription = tmpSetting.value;
                        if(tmpSetting.instanceName.endsWith("_hasValueSpace"))
                            originalSetting.hasValueSpace = tmpSetting.value;
                        if(tmpSetting.instanceName.endsWith("_hasConstraints"))
                            originalSetting.hasConstraints = tmpSetting.value;
                        if(tmpSetting.instanceName.endsWith("_isMappedToRegTerm"))
                            originalSetting.isMappedToRegTerm = tmpSetting.isMappedToRegTerm;
                        if(tmpSetting.instanceName.endsWith("_isExactMatching"))
                            originalSetting.isExactMatching = Boolean.parseBoolean(tmpSetting.value);
                        if(tmpSetting.instanceName.endsWith("_hasCommentsForMapping"))
                            originalSetting.hasCommentsForMapping = tmpSetting.value;     
                    }
                }
                //System.out.println("\t" + tmpSetting.toString());
            }
        }
    }
    
    public Setting getSetting(String solutionName, String settingName)
    {
        for(int i=0; i<allSolutions.size(); i++)
        {
            Solution tmpSolution = allSolutions.get(i);
            if(tmpSolution.name.equals(solutionName))
            {
                ArrayList<Setting> allSettings = tmpSolution.settings;
                for(int j=0; j<allSettings.size(); j++)
                {
                    Setting tmpSetting = allSettings.get(j);
                    if(tmpSetting.instanceName.equals(settingName))
                        return tmpSetting;
                }
            }
        }
        return null;
    }
    
    public void printAllSolutionsAndSettings()
    {
        for(int i=0; i<allSolutions.size(); i++)
        {
            Solution tmpSolution = allSolutions.get(i);
            System.out.println("-SOLUTION- name: " + tmpSolution.name + ", id: " + tmpSolution.id);
            
            ArrayList<Setting> allSettings = tmpSolution.settings;
            for(int j=0; j<allSettings.size(); j++)
            {
                Setting tmpSetting = allSettings.get(j);                
                System.out.println("\t" + tmpSetting.toString());
            }
        }
    }
    
    public void populateJSONLDInput(String transIn, String[] uris)
    {
    	InputStream is = new ByteArrayInputStream( transIn.getBytes() );
        _dmodel = ModelFactory.createOntologyModel().read(is, null, "JSON-LD");

        for (int i=0; i < uris.length; i++)
            _dmodel.read(System.getProperty("user.dir") + JsonLDManager.getInstance().WEBINF_PATH + uris[i]);
    }
    
    public String testHello(String tmpName)
    {
        return "Hello " + tmpName + "!";
    }
    
}