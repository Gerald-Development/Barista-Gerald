package main.java.de.voidtech.gerald.service;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ChatbotService {

    @Autowired
    private GeraldConfigService configService;

    @Autowired
    private HttpClientService httpClientService;

    private String httpMethodGET(String requestURL) {
        return httpClientService.getAndReturnString(requestURL);
    }

    private String getModelNameResponse() {
        return httpMethodGET(configService.getGavinURL() + "model_name");
    }

    private String getHparamsResponse() {
        return httpMethodGET(configService.getGavinURL() + "hparams");
    }

    public String getReply(String message, String ID) {
        String payload = new JSONObject().put("data", message).toString();
        JSONObject responseObject = httpClientService.postAndReturnJson(configService.getGavinURL(), payload);
        if (responseObject.has("error")) {
            return "No thoughts head empty";
        } else {
            if (responseObject.has("message")) {
                return responseObject.getString("message");
            } else {
                return "No thoughts head empty";
            }
        }
    }

    public JSONObject getHparams() {
        String hparamsResponse = getHparamsResponse();
        return new JSONObject(hparamsResponse);
    }

    public JSONObject getModelName() {
        String hparamsResponse = getModelNameResponse();
        return new JSONObject(hparamsResponse);
    }
}