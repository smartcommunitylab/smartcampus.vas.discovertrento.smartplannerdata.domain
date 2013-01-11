/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 ******************************************************************************/
package eu.trentorise.smartcampus.domain.smartplannerdata;

import it.sayservice.platform.core.domain.actions.DataConverter;
import it.sayservice.platform.core.domain.ext.Tuple;

import java.io.Serializable;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.protobuf.ByteString;

import eu.trentorise.smartcampus.domain.discovertrento.GenericPOI;
import eu.trentorise.smartcampus.domain.discovertrento.POIData;
import eu.trentorise.smartcampus.services.smartplannerdata.data.message.Smartplannerdata.CarParkingStation;

public class CarParkingDataConverter implements DataConverter {

	@Override
	public Serializable toMessage(Map<String, Object> parameters) {
		if (parameters == null)
			return null;
		return new HashMap<String, Object>(parameters);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object fromMessage(Serializable object) {
		List<ByteString> data = (List<ByteString>) object;
		Tuple res = new Tuple();
		List<GenericPOI> list = new ArrayList<GenericPOI>();
		for (ByteString bs : data) {
			try {
				CarParkingStation s = CarParkingStation.parseFrom(bs);
				list.add(extractGenericPOI(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(CarParkingStation s)
			throws ParseException {
		GenericPOI ge = new GenericPOI();
		ge.setTitle("Parking "+s.getId());
		ge.setDescription(createDescription(s));
		ge.setSource("smartplanner-parking");
		ge.setPoiData(createPOIData(s));
		ge.setType("Parking");
		ge.setId(encode(s.getStationId()));
		
		Map<String,Object> map = new TreeMap<String, Object>();
		if (s.hasAgencyId()) map.put("agencyId", s.getAgencyId());
		if (s.hasId()) map.put("id", s.getId());
		if (s.hasAvailablePlaces()) map.put("availablePlaces", s.getAvailablePlaces());
		if (s.hasRealAvailablePlaces()) map.put("realAvailablePlaces", s.getRealAvailablePlaces());

		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
		}

		return ge;
	}

	private String createDescription(CarParkingStation s) {
		String d = "Parking "+s.getId()+".";
		if (s.hasAvailablePlaces()) d += " "+s.getAvailablePlaces()+" places.";
		return d;
	}

	private POIData createPOIData(CarParkingStation s) {
		POIData poiData = new POIData();
		poiData.setStreet(s.getAddress());
		poiData.setLatitude(s.getLat());
		poiData.setLongitude(s.getLng());
		poiData.setCity("Trento");
		poiData.setRegion("TN");
		poiData.setCountry("ITA");
		poiData.setState("Italy");
		return poiData;
	}

	private static String encode(String s) {
		return new BigInteger(s.getBytes()).toString(s.length());
	}

}
