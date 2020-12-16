package com.carrefour.inno.qm.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.RequestEntity.get;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.carrefour.inno.qm.dao.EmployeeDAO;
import com.carrefour.inno.qm.model.Employee;
import com.carrefour.inno.qm.model.Employees;
import com.carrefour.inno.qm.model.Store;
import com.carrefour.inno.qm.model.Token;
import com.carrefour.inno.qm.service.CovidServiceV2;

@RestController
public class EmployeeControllerV2 {

    final Logger logger = LoggerFactory.getLogger(EmployeeControllerV2.class);

    private EmployeeDAO employeeDao;
    private RestTemplate restTemplate;
    private CovidServiceV2 covidService;

    public EmployeeControllerV2(EmployeeDAO employeeDao, RestTemplate restTemplate, CovidServiceV2 covidService) {
        this.employeeDao = employeeDao;
        this.restTemplate = restTemplate;
        this.covidService = covidService;
    }

    
    @GetMapping(path="/test", produces = "application/json")
    public Employees getEmployees() 
    {
        return employeeDao.getAllEmployees();
    }
    
    @PostMapping(path= "/test", consumes = "application/json", produces = "application/json")
    public ResponseEntity<Object> addEmployee(
                        @RequestHeader(name = "X-COM-PERSIST", required = true) String headerPersist,
                        @RequestHeader(name = "X-COM-LOCATION", required = false, defaultValue = "ASIA") String headerLocation,
                        @RequestBody Employee employee) 
                 throws Exception 
    {       
        //Generate resource id
        Integer id = employeeDao.getAllEmployees().getEmployeeList().size() + 1;
        employee.setId(id);
        
        //add resource
        employeeDao.addEmployee(employee);
        
        //Create resource location
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                                    .path("/{id}")
                                    .buildAndExpand(employee.getId())
                                    .toUri();
        
        //Send location in response
        return ResponseEntity.created(location).build();
    }
    @GetMapping(path="/token", produces = "application/json")
    public Token getzak(){
        return covidService.generatePhenixToken();
    }

    @GetMapping(path="/store/{token}", produces = "application/json")
    public String getStore(@PathVariable("token") String token)
    {
        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.mrpf.carrefour.com/production/private/retail/v1/sales/v1/transaction/store/3021081177693/date/20190326/transaction/31-42-1038")
                .build(true).toUri();

        RequestEntity<Void> request = get(uri)
                .accept(APPLICATION_JSON)
                .header("x-ibm-client-id","f18d3024-61d9-45a5-b784-52ab2ce8cf64")
                .header("x-ibm-client-secret","pV8aL3wE6qU0hR8eL4aM2sK2cV6jR8dC1vH8rS8nI4yC2mL2eJ")
                .header("auth-token", token).build();

        try{

            ResponseEntity<String> result = restTemplate.exchange(request, String.class);
            return  result.getBody();
        } catch (HttpStatusCodeException e) {
            System.out.println("failed to sent mail : message" + e.getMessage());
        }
        return null;
    }

    @GetMapping(path="/storedesc/long/{long}/lat/{lat}/dist/{distance}", produces = "application/json")
    public Store storeDesc(@PathVariable("long") double longitude,
                            @PathVariable("lat") double latitude,
                            @PathVariable("distance") double distance) {
        return new Store("3020180269131", "MASSY LABSTOREFLASH", "300");
    }
    
//    @GetMapping(path="/customerscount/store/{store}/datetime/{date}", produces = "application/json")
//    public Store refreshStore(@PathVariable("store") String storeEan,
//                                @PathVariable("date") String date) {
//
//    	return covidService.refreshStore(storeEan);
//    }
    
    @GetMapping(path="/customerscount/store/{store}/counter/{counter}", produces = "application/json")
    public Store incrementAndRefreshStore(@PathVariable("store") String storeEan,
                                @PathVariable("counter") int counter) {

    	return covidService.incrementAndRefreshStore(storeEan, counter);
    }

//    @GetMapping(path="/increment/store/{ean}/step/{step}", produces = "application/json")
//    public Store incrementCustomersCount(@PathVariable("ean") String ean,
//                                         @PathVariable("step") int step) {
//        return covidService.incrementCount(ean, step);
//    }

    @GetMapping(path="/storelocator/{ppsf}/capacity/{capacity}/ratio/{ratio}", produces = "application/json")
    public Store storeLocator(@PathVariable("ppsf") String ppsf,
                              @PathVariable("capacity") String capacity,
                              @PathVariable("ratio") String ratio) {
        
    	return covidService.storeLocator(ppsf.toUpperCase(), capacity, "1");
    }

    @GetMapping(path="/findall", produces = "application/json")
    public List<Store> findAll() {
        return covidService.findAll();
    }

    @GetMapping(path="/findstore/{ean}", produces = "application/json")
    public Store findStore(@PathVariable("ean") String ean)
    {
        return covidService.findOne(ean);
    }

    @GetMapping(path="/infostore/{ppsf}", produces = "application/json")
    public Store findByPpsf(@PathVariable("ppsf") String ppsf)
    {
        return covidService.initAndGetStore(ppsf.toUpperCase());
    }

    @GetMapping(path="/currentstate/store/{ean}/state/{state}", produces = "application/json")
    public Store findStore(@PathVariable("ean") String ean,
                           @PathVariable("state") String state) {
        return covidService.updateStoreWithNewState(ean, state);
    }

    @GetMapping(path="/updateline/store/{ean}/linestate/{state}/datetime/{date}", produces = "application/json")
    public Store findStore(@PathVariable("ean") String ean,
                           @PathVariable("state") String state,
                           @PathVariable("date") String date) {
        logger.info("::: SLIDER STATE " + state );
        return covidService.findOne(ean);
    }

//    public PhenixTrxResponse getTrxCount(String ean, String date){
//        String token = generateToken().getToken();
//        Store store = covidService.findOne(ean);
//        String fromDate = store.getLastUpdate();
//        String requestBody = "{\"filters\":[{\"field\":\"stoEan\",\"keys\":[\"" + ean + "\"]}],\"fromDate\":\"" + fromDate + "\",\"toDate\":\"" + date + "\"}";
//        logger.info(":: TOKEN :: " + token + " ::: BODY ::: " + requestBody);
//
//        URI uri = UriComponentsBuilder.fromHttpUrl("https://api.mrpf.carrefour.com/production/private/retail/v1/sales/v1/transaction")
//                .build(true).toUri();
//
//        RequestEntity<String> request = post(uri)
//                .accept(APPLICATION_JSON)
//                .header("Content-Type","application/json")
//                .header("x-ibm-client-id","f18d3024-61d9-45a5-b784-52ab2ce8cf64")
//                .header("x-ibm-client-secret","pV8aL3wE6qU0hR8eL4aM2sK2cV6jR8dC1vH8rS8nI4yC2mL2eJ")
//                .header("auth-token", token)
//                .body(requestBody);
//
//        try{
//
//            ResponseEntity<String> result = restTemplate.exchange(request, String.class);
//
//            ObjectMapper mapper = new ObjectMapper();
//            PhenixTrxResponse trxResponse = mapper.readValue(result.getBody(), PhenixTrxResponse.class);
//
//            //return  result.getBody();
//            return  trxResponse;
//        } catch (Exception e) {
//            System.out.println("failed to send request : message" + e.getMessage());
//        }
//        return null;
//    }
//
//    public Token generateToken(){
//        URI uri = UriComponentsBuilder.fromHttpUrl("https://api-internal.carrefour.com/production/private/retail/v2/authenticate?env=PROD")
//                .build(true).toUri();
//
//        RequestEntity<String> request = post(uri)
//                .accept(APPLICATION_JSON)
//                .header("x-ibm-client-id","f18d3024-61d9-45a5-b784-52ab2ce8cf64")
//                .header("x-ibm-client-secret","pV8aL3wE6qU0hR8eL4aM2sK2cV6jR8dC1vH8rS8nI4yC2mL2eJ")
//                .body("{\"ldap_user\":\"CS$_PHENIX_TIC_ET_AU_DELA\",\"ldap_password\":\"Ls$MtTk5\"}");
//
//        try{
//
//            ResponseEntity<String> result = restTemplate.exchange(request, String.class);
//
//            ObjectMapper mapper = new ObjectMapper();
//            Token token = mapper.readValue(result.getBody(), Token.class);
//
//            //return  result.getBody();
//            return  token;
//        } catch (Exception e) {
//            System.out.println("failed to send request : message" + e.getMessage());
//        }
//        return null;
//    }

    /*@GetMapping(path="/storelocator/{ean}/", produces = "application/json")
    public Store storeLocator(@PathVariable("ean") String ean) {
        return covidService.storeLocator(ean);
    }*/
}












