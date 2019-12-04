package be.ugent.idlab.csprite.windowing;

import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPAdministrator;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.deploy.EPDeploymentAdmin;
import com.espertech.esper.client.time.CurrentTimeEvent;

import be.ugent.idlab.csprite.sparql.JenaQueryEngine;

public class EsperWindow {
	
	private EPRuntime cepRT;
	private long counter = 0;


	public EsperWindow(int windowSize, int windowSlide, JenaQueryEngine jena) {
		Configuration cep_config = new Configuration();

		cep_config.getEngineDefaults().getThreading().setInternalTimerEnabled(true);
		cep_config.getEngineDefaults().getLogging().setEnableExecutionDebug(true);
		cep_config.getEngineDefaults().getLogging().setEnableTimerDebug(true);
		cep_config.getEngineDefaults().getLogging().setEnableQueryPlan(true);
		cep_config.getEngineDefaults().getMetricsReporting().setEnableMetricsReporting(true);
		cep_config.getEngineDefaults().getLogging().setEnableQueryPlan(true);

		EPServiceProvider cep = EPServiceProviderManager.getDefaultProvider(cep_config);
		this.cepRT = cep.getEPRuntime();
		EPAdministrator cepAdm = cep.getEPAdministrator();

		EPServiceProvider epService = EPServiceProviderManager.getDefaultProvider();
		EPDeploymentAdmin deployAdmin = cepAdm.getDeploymentAdmin();
		cep.getEPAdministrator().getConfiguration().addEventType(GraphEvent.class);

		String eplStatement = String.format("select * from GraphEvent#time(%s sec) output snapshot every %s seconds",windowSize,windowSlide);
		
		try {
			EPStatement statement = cepAdm.createEPL(eplStatement);

			statement.addListener(new UpdateListener() {
				public void update(EventBean[] newEvents, EventBean[] oldEvents) {
					if (newEvents != null){
						
//						System.out.println("IN:\t"+System.currentTimeMillis());
//						System.out.println("#events:\t"+newEvents.length);
						for (EventBean e : newEvents) {
							jena.setupSimpleAdd((String)e.get("s"),(String)e.get("p"),(String)e.get("o"));
							//builder.append((String) e.get("triples"));
						}
					}
					jena.query();
					
				}
			});
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		cepRT.sendEvent(new CurrentTimeEvent(System.currentTimeMillis()));
		
	}
	public void addEvent(String s, String p ,String o) {
		cepRT.sendEvent(new GraphEvent(counter++, s,p,o));
		advanceTime(System.currentTimeMillis());
	}

	public void advanceTime(long time) {
		cepRT.sendEvent(new CurrentTimeEvent(time));
	}

}
