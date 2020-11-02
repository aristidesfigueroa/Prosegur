/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hn.bantrab;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.ibm.as400.access.AS400;
import com.ibm.as400.access.AS400Message;
import com.ibm.as400.data.PcmlException;
import com.ibm.as400.data.ProgramCallDocument;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import static utilities.AS400ToolboxJarMaker.AS400;

/**
 * REST Web Service
 *
 * @author Figueroa
 */
@Path("service")
public class prosegur {
    private static AS400 as400;
    static final String HOST ="206.60.106.206";    
    static final String USUARIO ="CREDITODC";
    static final String PASSWORD ="HEB416SL51";

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of prosegur
     */
    public prosegur() {
    }

    
    
    /**
     * POST method for post an instance of prosegur
     * @param jsonvar
     * @return 
     */
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)    
    @Consumes(MediaType.APPLICATION_JSON)
    
    @Path("/CashTransaction/")
    public Response CashTransaction(String jsonvar) {
        
        System.out.println("LEN: " + jsonvar.length());
        
               
        if (isValid(jsonvar)){ // Si jSon correcto entonces responde 
            System.out.println("JSON-IN  es TRUE ");
            String jSOnresponse = "";
            String msgId = "";
            String msgText = "";
            
            as400 = new AS400(HOST,USUARIO,PASSWORD);
            String path = "/QSYS.LIB/BANTRABOBJ.LIB/SRVP009.SRVPGM"; 
            try {
            ProgramCallDocument pcml = new ProgramCallDocument(as400, "SRVP009");
            
            pcml.setPath ("PAY_CASHTRANSACTION", path);              
            
            pcml.setValue("PAY_CASHTRANSACTION.JSONIN", jsonvar);          
            pcml.setValue("PAY_CASHTRANSACTION.JSONOUT", jSOnresponse); 
            
        
            boolean rc = pcml.callProgram("PAY_CASHTRANSACTION");
        
            if (rc){        
                jSOnresponse    = (String) pcml.getValue("PAY_CASHTRANSACTION.JSONOUT");                
                System.out.println("JSON-OUT es TRUE OR FALSE: " + isValid(jSOnresponse)); 
                as400.disconnectAllServices();
                return Response.ok(jSOnresponse)
        .header("Acces-Control-Allow-Origin", "*")
        .header("Acces-Control-Allow-Origin", "POST, OPTIONS")
        .header("Acces-Control-Allow-Origin", "Content-Type, Accept, X-Requested-With").build();
            }else{
                AS400Message[] msgs = pcml.getMessageList("PAY_CASHTRANSACTION");
                for (AS400Message msg : msgs) {
                    msgId = msg.getID();
                    msgText = msg.getText();
                    System.out.println(" Error Función   " + msgId + " - " + msgText);
                }              
                as400.disconnectAllServices();
                return Response.status(Response.Status.NOT_FOUND).entity("BAD CALL FUNCTION PAY_CASHTRANSACTION : " + msgId + " " + msgText).build();

            }
            
        }catch(PcmlException e){
           // System.out.println(e.getLocalizedMessage());    
            e.printStackTrace();
            //System.out.println("*** BB- Ha fallado la llamada a PAY_CASHTRANSACTION. ***");  
            as400.disconnectAllServices();
            return Response.status(Response.Status.NOT_FOUND).entity("BAD CALL FUNCTION PAY_CASHTRANSACTION: " + e).build();
        }             
            
            
            
        }else{ // jSon mal formado 
            System.out.println(" BAD REQUEST 400 ");
            String jSOnresponse = badRequest();
            return Response.status(Response.Status.BAD_REQUEST).entity(jSOnresponse).build();            
            
        }            
        
        
     }// fin CashTransaction
    
    
    /*
    Validar si JSON es correcto
    */
    public boolean isValid(String json){
        try{
            new JsonParser().parse(json);
            
            return true;
        }catch (JsonSyntaxException jse) {
            return false;
        }
        
    } // fin
    
    
    /*
    Retornar json okRequest 200  TEMPORAL ---> LUEGO SERÁ EL iSeries quien responderá
    */
    public String okRequest(){
        
       String json = "{\n" +
"\"Result\":\"0\", \"ErrorData\":{\n" +
"\"Error\":{\n" +
"\"ErrorCode\":\"0\", \"ErrorDescription\":\"OK\"\n" +
"} },\n" +
"\"AdditionalData\":{ \"Data\":[\n" +
"{\n" +
"\"Field\":\"abcde\", \"Value\":\"abcde\"\n" +
"}, {\n" +
"\"Value\":\"abcde\" }\n" +
"] }\n" +
"}";     
       return json;
        
        
    }// fin
    
    /*
    Retornar json BadRequest 400
    */
    public String badRequest(){
        
       String json = "{\"Result\":\"E1101\",\n" +
"\"ErrorData\":{\n" +
"\"Error\":{\n" +
"\"ErrorCode\":\"E1101\",\n" +
"\"ErrorDescription\":\"Petición Incorrecta, revise objeto jSon\"\n" +
"}\n" +
"},\n" +
"\"AdditionalData\":{\n" +
"\"Data\":[\n" +
"{\n" +
"\"Field\":\"Field\",\"Value\":\"Value\"\n" +
"},\n" +
"{\n" +
"\"Field\":\"Field \",\n" +
"\"Value\":\"Value\"\n" +
"}\n" +
"]\n" +
"}\n" +
"}";
       return json;
        
        
    }// fin   
    
    
}
