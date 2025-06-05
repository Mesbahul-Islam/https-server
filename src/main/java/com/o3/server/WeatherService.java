package com.o3.server;
import java.io.InputStream;
import java.net.URI;



import java.net.URL;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


import org.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;



public class WeatherService {
    private static final String WEATHER_SERVICE_URL = "http://localhost:4001/wfs";

    public static JSONObject getWeatherData(double latitude, double longitude) {
        try{
            String currentTime = MessageDatabase.getCurrentUtcTimestamp();
            StringBuilder urlBuilder = new StringBuilder(WEATHER_SERVICE_URL);
            urlBuilder.append("?latlon=").append(latitude).append(",").append(longitude);
            urlBuilder.append("&starttime=").append(currentTime);

            URI uri = new URI(urlBuilder.toString());
            URL url = uri.toURL();
            InputStream inputStream = url.openStream();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(inputStream);
            
            JSONObject weatherData = new JSONObject();
            if(document != null){
                
                
                NodeList temperatureNodes = document.getElementsByTagName("BsWfs:BsWfsElement");
                double temperature = 0; //realistic data should not have 0K
                double cloudiness = -9999;
                double backgroundLight = -9999;
                
                for (int i = 0; i < temperatureNodes.getLength(); i++) {
                    Element element = (Element) temperatureNodes.item(i);
                    NodeList paramNameList = element.getElementsByTagName("BsWfs:ParameterName");
                    NodeList paramValueList = element.getElementsByTagName("BsWfs:ParameterValue");
                    

                    if (paramNameList.getLength() > 0 && paramValueList.getLength() > 0) {
                        String paramName = paramNameList.item(0).getTextContent();
                        String paramValue = paramValueList.item(0).getTextContent();
                        
                        try {
                            double value = Double.parseDouble(paramValue);
                            
                            switch (paramName) {
                                case "Temperature":
                                    temperature = value + 273.15;
                                    break;
                                case "TotalCloudCover":
                                    cloudiness = value;
                                    break;
                                case "RadiationGlobalAccumulation":
                                    backgroundLight = value;
                                    break;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Cannot parse " + paramName + ": " + paramValue);
                        }
                    }
                }
                System.out.println("Temperature: " + temperature);
                System.out.println("Cloudiness: " + cloudiness);
                System.out.println("Background light: " + backgroundLight);
                weatherData.put("temperatureInKelvins", temperature);
                weatherData.put("cloudinessPercentage", cloudiness);
                weatherData.put("backgroundLightVolume", backgroundLight);
                
                return weatherData;
                } 
                else {
                    System.err.println("Weather status code: ");
                }
            } 
            catch(Exception e){
                e.printStackTrace();
        }
        return null;
    }
}
