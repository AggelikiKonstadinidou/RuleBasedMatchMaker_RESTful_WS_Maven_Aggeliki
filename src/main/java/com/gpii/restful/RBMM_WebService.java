package com.gpii.restful;

import com.gpii.jsonld.JsonLDManager;
import com.gpii.ontology.OntologyManager;
import com.gpii.transformer.TransformerManager;
import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.json.JSONException;



@Path("/RBMM")
public class RBMM_WebService
{
    //http://localhost:8080/CLOUD4All_RBMM_Restful_WS/RBMM/transformOwlToJSONLD
    @GET
    @Path("/transformOwlToJSONLD")
    public Response transformOwlToJSONLD() throws IOException, JSONException
    { 
        //transform .owl to JSON-LD
        TransformerManager.getInstance().transformOwlToJSONLD();
        return Response.status(200).entity("OK - '" + JsonLDManager.getInstance().semanticsGeneratedFromOwlFilePath + "' file was generated.\n").build();
    }
    
    //http://localhost:8080/CLOUD4All_RBMM_Restful_WS/RBMM/runJSONLDRules
    @POST
    @Path("/runJSONLDRules")
    @Consumes("application/json")
    public Response runJSONLDRules(Object tmpInput) throws IOException, JSONException
    {
        OntologyManager.getInstance().debug = "";        
        String inputJsonStr = JsonLDManager.getInstance().gson.toJson(tmpInput);        
        String outputJsonStr = JsonLDManager.getInstance().runJSONLDTests(inputJsonStr);        
        return Response.status(200).entity(outputJsonStr).build();
    }
}
