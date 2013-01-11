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
import eu.trentorise.smartcampus.services.smartplannerdata.data.message.Smartplannerdata.RouteTimetable;
import eu.trentorise.smartcampus.services.smartplannerdata.data.message.Smartplannerdata.TransitStop;
import eu.trentorise.smartcampus.services.smartplannerdata.data.message.Smartplannerdata.TripTime;

public class TransitStopsDataConverter implements DataConverter {

	private static final Map<String,String[]> descriptors = new HashMap<String, String[]>(); 
			
	static {
		descriptors.put("10", new String[]{"10","Trento/Male station ", "Train station "});
		descriptors.put("12", new String[]{"12","Trentino Trasporti bus stop ", "Bus stop "});
	}
	
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
				TransitStop s = TransitStop.parseFrom(bs);
				list.add(extractGenericPOI(s));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		res.put("data", list.toArray(new GenericPOI[list.size()]));
		return res;
	}

	private GenericPOI extractGenericPOI(TransitStop s)
			throws ParseException {
		GenericPOI ge = new GenericPOI();
		if (descriptors.get(s.getAgencyId()) != null) {
			ge.setTitle(descriptors.get(s.getAgencyId())[1]+s.getName());
		} else  {
			ge.setTitle(s.getName());
		}
		ge.setDescription(createDescription(s));
		ge.setSource("smartplanner-transitstops");
		ge.setPoiData(createPOIData(s));
		ge.setType("Mobility");
		ge.setId(encode(s.getStopId()));
		
		Map<String,Object> map = new TreeMap<String, Object>();
		if (s.hasAgencyId()) map.put("agencyId", s.getAgencyId());
		if (s.hasId()) map.put("id", s.getId());
//		if (s.getTimetableList() != null && s.getTimetableCount() > 0) {
//			map.put("timetable", convertTimetable(s.getTimetableList()));
//		}
//
		try {
			ge.setCustomData(new ObjectMapper().writeValueAsString(map));
		} catch (Exception e) {
		}

		return ge;
	}
	
	private Object convertTimetable(List<RouteTimetable> timetableList) {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for (RouteTimetable rt : timetableList) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("routeId", rt.getRouteId());
			map.put("routeLongName", rt.getRouteLongName());
			map.put("routeShortName", rt.getRouteShortName());
			if (rt.getTripTimeList() != null && rt.getTripTimeCount() > 0) {
				map.put("tripTime", convertTripTimes(rt.getTripTimeList())); 
			}
			result.add(map);
		}
		return result;
	}

	private Object convertTripTimes(List<TripTime> tripTimeList) {
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		for (TripTime tt : tripTimeList) {
			Map<String,Object> map = new HashMap<String, Object>();
			map.put("tripId", tt.getTripId());
			map.put("time", tt.getTime());
			result.add(map);
		}
		return result;
	}

	private String createDescription(TransitStop s) {
		String d = "";
		if (descriptors.get(s.getAgencyId()) != null) {
			d = descriptors.get(s.getAgencyId())[2];
		}
		d += s.getName();
		if (s.getTimetableList() != null && s.getTimetableCount() > 0) {
			d += "\nRoutes:\n";
			for (RouteTimetable rt : s.getTimetableList()) {
				d += rt.getRouteLongName()+"\n";
			}
		}
		return d.trim();
	}


	private POIData createPOIData(TransitStop s) {
		POIData poiData = new POIData();
		poiData.setStreet(s.getName());
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
