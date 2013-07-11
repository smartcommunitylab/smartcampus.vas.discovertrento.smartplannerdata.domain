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
import java.util.Collections;
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

	// TODO move the real IDs into the smartplanner directly
	private static final Map<String, String> parkingsNames;
	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("Circonvallazione Nuova, Ponte S. Lorenzo - Trento", "Parcheggio vecchia tangenziale di Piedicastello");
		map.put("P1", "Parcheggio area ex SIT via Canestrini");
		map.put("P2", "Garage Centro Europa");
		map.put("P3", "Garage Autosilo Buonconsiglio");
		map.put("P4", "Garage piazza Fiera");
		map.put("P5", "Garage Parcheggio Duomo");
		map.put("P6", "Parcheggio CTE via Bomporto");
		map.put("P7", "Parcheggio piazzale Sanseverino");
		map.put("ex-Zuffo - Trento", "Parcheggio Area ex Zuffo");
		map.put("via Asiago, Stazione FS Villazzano - Trento", "Parcheggio via Asiago - Villazzano STAZIONE FS");
		map.put("via Fersina - Trento", "Parcheggio Ghiaie via Fersina");
		map.put("via Maccani - Trento", "Parcheggio Campo Coni via E. Maccani");
		map.put("via Roggia Grande,16 - Trento", "Garage Autorimessa Europa");
		map.put("via Roggia Grande,16-Trento", "Garage Autorimessa Europa");
		map.put("via Torre Verde, 40 - Trento", "Garage Torre Verde");
		map.put("via valentina Zambra - Trento", "Garage Parcheggio Palazzo Onda");
		parkingsNames = Collections.unmodifiableMap(map);
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
		String mappedId = parkingsNames.containsKey(s.getId()) ? parkingsNames.get(s.getId()) : ("Parcheggio "+s.getId());
		ge.setTitle(mappedId);
		ge.setDescription(createDescription(s, mappedId));
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

	private String createDescription(CarParkingStation s, String mappedId) {
		String d = mappedId+".";
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
