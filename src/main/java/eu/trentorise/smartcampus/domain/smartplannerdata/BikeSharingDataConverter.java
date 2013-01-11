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
import eu.trentorise.smartcampus.services.smartplannerdata.data.message.Smartplannerdata.BikeSharingStation;

public class BikeSharingDataConverter implements DataConverter {

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
				BikeSharingStation s = BikeSharingStation.parseFrom(bs);
				list.add(extractGenericPOI(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(BikeSharingStation s)
			throws ParseException {
		GenericPOI ge = new GenericPOI();
		ge.setTitle("Bike sharing "+s.getId());
		ge.setDescription(createDescription(s));
		ge.setSource("smartplanner-bikesharing");
		ge.setPoiData(createPOIData(s));
		ge.setType("Mobility");
		ge.setId(encode(s.getStationId()));
		
		Map<String,Object> map = new TreeMap<String, Object>();
		if (s.hasAgencyId()) map.put("agencyId", s.getAgencyId());
		if (s.hasId()) map.put("id", s.getId());
		if (s.hasAvailableBikes()) map.put("availableBikes", s.getAvailableBikes());
		if (s.hasAvailableParkingPlaces()) map.put("availableParkingPlaces", s.getAvailableParkingPlaces());
		if (s.hasRealAvailableBikes()) map.put("realAvailableBikes", s.getRealAvailableBikes());
		if (s.hasRealAvailableParkingPlaces()) map.put("realAvailableParkingPlaces", s.getRealAvailableParkingPlaces());

		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
		}

		return ge;
	}
	
	private String createDescription(BikeSharingStation s) {
		String d = "Bike sharing point: "+s.getId()+".";
		if (s.hasAvailableBikes()) d += " "+s.getAvailableBikes()+" bikes available.";
		if (s.hasAvailableParkingPlaces() && s.getAvailableParkingPlaces() > 0) d += " "+s.getAvailableParkingPlaces()+" parking places.";
		return d;
	}


	private POIData createPOIData(BikeSharingStation s) {
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
