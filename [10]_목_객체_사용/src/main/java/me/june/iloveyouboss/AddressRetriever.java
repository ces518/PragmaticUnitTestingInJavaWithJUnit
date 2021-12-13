package me.june.iloveyouboss;

import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AddressRetriever {

    public Address retrieve(double latitude, double longitude)
        throws IOException, ParseException {
        String parms = String.format("lat=%.6flon=%.6f", latitude, longitude);
        String response = new HttpImpl().get(
            "http://open.mapquestapi.com/nominatim/v1/reverse?format=json&"
                + parms);

        JSONObject obj = (JSONObject)new JSONParser().parse(response);

        JSONObject address = (JSONObject)obj.get("address");
        String country = (String)address.get("country_code");
        if (!country.equals("us"))
            throw new UnsupportedOperationException(
                "cannot support non-US addresses at this time");

        String houseNumber = (String)address.get("house_number");
        String road = (String)address.get("road");
        String city = (String)address.get("city");
        String state = (String)address.get("state");
        String zip = (String)address.get("postcode");
        return new Address(houseNumber, road, city, state, zip);
    }
}