package com.gpii.restful;


import com.gpii.jsonld.JsonLDManager;
import com.gpii.utils.Utils;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import junit.framework.TestCase;

public class RBMM_WebServiceIT extends TestCase
{/*
    public void test_runJSONLDRules() 
    {
        System.out.println("\n****************************************");
        System.out.println("* Testing 'runJSONLDRules' web service *");
        System.out.println("****************************************");
        
        String inputJsonStr = null;
        String actualOutputStr = null;
        String expectedOutputJsonStr = null;
        
        String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/input_test1.json";
        String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
        String filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/expected_output_test1.json";
        
        // read input & expected output
        try {
            inputJsonStr = Utils.getInstance().readFile(filepathIN);
            expectedOutputJsonStr = Utils.getInstance().readFile(filepathExpectedOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
        ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
        String output = Utils.getInstance().jsonPrettyPrint(response.getEntity(String.class));
        
        System.out.println("\nWeb service output:\n");
        System.out.println(output);
        
        // read actual output
        try {
            actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
               
        assertEquals(actualOutputStr, expectedOutputJsonStr);
    }*/
    
    public void test_vladimirLobby()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Vladimir at the Lobby' *");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/vladimir.json";

            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";

            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/vladimirLobbyOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_vladimirLobby INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);	
        }
        else
            System.out.println("test_vladimirLobby INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_VladimirSubway()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Vladimir at the Subway' ********************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/vladimirSubway.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT;
            if(JsonLDManager.getInstance().USE_THE_REAL_ONTOLOGY == false)
                filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/vladimirSubwayOUT.json";
            else
                filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/vladimirSubwayRealOntologyOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr = Utils.getInstance().readFile(filepathExpectedOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(actualOutputStr.equals(expectedOutputJsonStr) == false)
            {
                System.out.println("\n* ERROR -> test_VladimirSubway INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(actualOutputStr, expectedOutputJsonStr);
        }
        else
            System.out.println("test_VladimirSubway INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_Mary()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Marry' ***************************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/mary.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/maryOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr = Utils.getInstance().readFile(filepathExpectedOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(actualOutputStr.equals(expectedOutputJsonStr) == false)
            {
                System.out.println("\n* ERROR -> test_Mary INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(actualOutputStr, expectedOutputJsonStr);
        }
        else
            System.out.println("test_Mary INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_Manuel()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Manuel' **************************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/manuel.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/manuelOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_Manuel INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_Manuel INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_ChrisWin()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Chris Windows' *******************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/chrisWindows.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/chrisWindowsOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_ChrisWin INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_ChrisWin INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_ChrisAndroid()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Chris Android' *******************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/chrisAndroid.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/chrisAndroidOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_ChrisAndroid INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_ChrisAndroid INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_LiWindows()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Li Windows' **********************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/liWindows.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/liWindowsOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_LiWindows INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_LiWindows INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_LiAndroid()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Li Android' **********************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/liAndroid.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/liAndroidOUT.json";


            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_LiAndroid INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_LiAndroid INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_Franklin()
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            System.out.println("\n*****************************************************");
            System.out.println("* Testing 'Classroom Franklin' **********************");
            System.out.println("*******************************************************");    	

            String inputJsonStr = null;
            String actualOutputStr = null;
            String expectedOutputJsonStr1 = null;

            String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/franklin.json";
            String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
            String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/franklinOUT.json";

            // read input & expected output
            try {
                inputJsonStr = Utils.getInstance().readFile(filepathIN);
                expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Client client = Client.create();
            WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
            ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
            String output = response.getEntity(String.class);

            System.out.println("\nWeb service output:\n");
            System.out.println(output);

            // read actual output
            try {
                actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
            } catch (IOException e) {
                e.printStackTrace();
            }

            boolean outputIsSimilarToOneOfTheExpected = false;
            if(actualOutputStr.equals(expectedOutputJsonStr1))
                outputIsSimilarToOneOfTheExpected = true;
            else
            {
                System.out.println("\n* ERROR -> test_Franklin INTEGRATION TEST FAILED!");
                System.exit(-1);
            }
            assertEquals(outputIsSimilarToOneOfTheExpected, true);
        }
        else
            System.out.println("test_Franklin INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
    public void test_transformOwlToJSONLD() 
    {
        if(JsonLDManager.getInstance().PERFORM_INTEGRATION_TESTS)
        {
            if(JsonLDManager.getInstance().INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD)
            {
                System.out.println("\n*********************************************************************************************************************************");
                System.out.println("* Testing 'transformOwlToJSONLD' web service                                                                                    *");
                System.out.println("*                                                                                                                               *");
                System.out.println("* Please be patient. This process will take some minutes. The whole solutions ontology is being transformed into JSON-LD format.*");
                System.out.println("* You can disable this test by setting in config file -> INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD = false  *");
                System.out.println("*********************************************************************************************************************************");

                Client client = Client.create();
                WebResource webResource = client.resource("http://localhost:8080/RBMM/transformOwlToJSONLD");
                ClientResponse response = webResource.get(ClientResponse.class);
                String output = response.getEntity(String.class);

                System.out.println("\nWeb service output:\n");
                System.out.println(output);

                //assertEquals(output, expectedOutputJsonStr);
            }
            else
            {
                System.out.println("\n******************************************************************************************************************************************************************************");
                System.out.println("* Testing of 'transformOwlToJSONLD' was skipped because it's a time consuming process (config file -> INTEGRATION_TESTS_INCLUDE_ONTOLOGY_TRANSFORMATION_INTO_JSONLD = false) *");
                System.out.println("******************************************************************************************************************************************************************************\n");
            }
        }
        else
            System.out.println("test_transformOwlToJSONLD INTEGRATION TEST WAS IGNORED because 'PERFORM_INTEGRATION_TESTS=false' in config.properties");
    }
    
	/*
    public void test_resolveMSC_OneSolutionPreffered(){
        
    	System.out.println("\n*****************************************************");
        System.out.println("* Testing 'Resolution of Multiple Solution Conflict  ' *");
        System.out.println("* Test criteria: one solution preferred; no AT suits ' *");
        System.out.println("*******************************************************");    	
    	
        String inputJsonStr = null;
        String actualOutputStr = null;
        String expectedOutputJsonStr = null;
        
        String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/MSC_oneInstATpreferred_noATSuite.json";
        String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
        String filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/MSC_oneInstATpreferred_noATSuiteOUT.json";
        
        // read input & expected output
        try {
            inputJsonStr = Utils.getInstance().readFile(filepathIN);
            expectedOutputJsonStr = Utils.getInstance().readFile(filepathExpectedOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
        ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
        String output = response.getEntity(String.class);
        
        System.out.println("\nWeb service output:\n");
        System.out.println(output);
               
        // read actual output
        try {
            actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
               
        //IT FAILS BECAUSE IT NEEDS ANOTHER SEMANTIC SOLUTIONS FILE -> assertEquals(actualOutputStr, expectedOutputJsonStr);
    }

    public void test_resolveMSC_MultiSolutionPreffered(){
        
    	System.out.println("\n*************************************************************************");
        System.out.println("* Testing 'Resolution of Multiple Solution Conflict  ' ********************");
        System.out.println("* Test criteria: multiple ATs of same type installed ' ********************");
        System.out.println("* multiple preferred installed; one installed not preferred; AT suits ' *");
        System.out.println("***************************************************************************");    	
    	
        String inputJsonStr = null;
        String actualOutputStr = null;
        String expectedOutputJsonStr = null;
        
        String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/MSC_multiInstATpreferred_noATSuite.json";
        String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
        String filepathExpectedOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/MSC_multiInstATpreferred_noATSuiteOUT.json";
        
        // read input & expected output
        try {
            inputJsonStr = Utils.getInstance().readFile(filepathIN);
            expectedOutputJsonStr = Utils.getInstance().readFile(filepathExpectedOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
        ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
        String output = response.getEntity(String.class);
        
        System.out.println("\nWeb service output:\n");
        System.out.println(output);
               
        // read actual output
        try {
            actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
               
        //IT FAILS BECAUSE IT NEEDS ANOTHER SEMANTIC SOLUTIONS FILE -> assertEquals(actualOutputStr, expectedOutputJsonStr);
    } 
    
    public void test_BasicAlignment(){
        
    	System.out.println("\n**********************************************************************");
        System.out.println("* Tetsing Basic Alignment of Solutions and Setting *********************");
        System.out.println("** for multiple contetxs. N&P sets:  ***********************************");    	
        System.out.println("** 1. same preference across N&P sets (high contrast)*******************"); 
        System.out.println("** 2. different preference values across N&P sets (cursor size) ********"); 
        System.out.println("** 3. common prefs not matched to app-specific prefs *******************");
    	System.out.println("\n**********************************************************************");
    	
        String inputJsonStr = null;
        String actualOutputStr = null;
        String expectedOutputJsonStr1 = null;
        
        String filepathIN = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/preferences/basicAlignment.json";
        String filepathActualOUT = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/debug/5_RBMMJsonOutput.json";
        String filepathExpectedOUT1 = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/testData/expectedTestOutcomes/basicAlignmentOUT.json";
        
        // read input & expected output
        try {
            inputJsonStr = Utils.getInstance().readFile(filepathIN);
            expectedOutputJsonStr1 = Utils.getInstance().readFile(filepathExpectedOUT1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Client client = Client.create();
        WebResource webResource = client.resource("http://localhost:8080/RBMM/runJSONLDRules");
        ClientResponse response = webResource.accept("application/json").type("application/json").post(ClientResponse.class, inputJsonStr);
        String output = response.getEntity(String.class);
        
        System.out.println("\nWeb service output:\n");
        System.out.println(output);
        
        // read actual output
        try {
            actualOutputStr = Utils.getInstance().readFile(filepathActualOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        boolean outputIsSimilarToOneOfTheExpected = false;
        if(actualOutputStr.equals(expectedOutputJsonStr1))
            outputIsSimilarToOneOfTheExpected = true;
        assertEquals(outputIsSimilarToOneOfTheExpected, true);	
    }*/   
    
}