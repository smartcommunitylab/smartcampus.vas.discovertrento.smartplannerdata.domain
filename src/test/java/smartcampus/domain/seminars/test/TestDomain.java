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
package smartcampus.domain.seminars.test;

import it.sayservice.platform.client.ServiceBusAdminClient;
import it.sayservice.platform.client.ServiceBusClient;
import it.sayservice.platform.client.jms.JMSServiceBusAdminClient;
import it.sayservice.platform.client.jms.JMSServiceBusClient;
import it.sayservice.platform.core.message.Core.DODataRequest;
import it.sayservice.platform.core.message.Core.DomainEvent;
import it.sayservice.platform.domain.test.DomainListener;
import it.sayservice.platform.domain.test.DomainTestHelper;

import java.util.List;

import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.jms.client.HornetQJMSConnectionFactory;

import eu.trentorise.smartcampus.domain.discovertrento.POIServiceDOEngine;
import eu.trentorise.smartcampus.domain.smartplannerdata.SmartPlannerBikeSharingServiceDOEngine;
import eu.trentorise.smartcampus.domain.smartplannerdata.SmartPlannerCarParkingServiceDOEngine;
import eu.trentorise.smartcampus.domain.smartplannerdata.SmartPlannerCarSharingServiceDOEngine;


public class TestDomain {

	public static void main(String[] args) throws Exception {
		HornetQJMSConnectionFactory cf = 
			     new HornetQJMSConnectionFactory(false,
			                  new TransportConfiguration(
			                    "org.hornetq.core.remoting.impl.netty.NettyConnectorFactory"));
			  ServiceBusClient client = new JMSServiceBusClient(cf);

			  DomainTestHelper helper = new DomainTestHelper(client,new DomainListener() {
			    public void onDomainEvents(List<DomainEvent> events) {
			    	for (DomainEvent e : events) {
			    		System.err.println(e.getPayload());
			    	}
			    }

			    public void onDataRequest(DODataRequest req) {
			      // DO someth...
			    }
			  });

			  
			  poisCreation(cf, helper);
			  System.err.println();
	}

	private static void initDomain(DomainTestHelper helper) {
		helper.start(
				  new POIServiceDOEngine(),
				  new SmartPlannerBikeSharingServiceDOEngine(),
				  new SmartPlannerCarParkingServiceDOEngine(),
				  new SmartPlannerCarSharingServiceDOEngine()
				  );
	}

	private static void poisCreation(HornetQJMSConnectionFactory cf,
			DomainTestHelper helper) throws Exception {
		  helper.cleanDomainData();
		  initDomain(helper);
		ServiceBusAdminClient admin = new JMSServiceBusAdminClient(cf);
		  admin.restartService("eu.trentorise.smartcampus.services.smartplannerdata.SmartplannerdataService", "GetCarSharing");
		  System.err.println();
	}
}
