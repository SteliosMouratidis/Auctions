import java.util.HashMap;
import java.util.Map;

public class Capability
{
	// the whole MAS
	static private String[] agentList = {
			"agent01",
			"agent02",
			"agent03",
			"agent04",
			"agent05",
			"agent06",
			"agent07",
			"agent08",
			"agent09",
			"agent11",
			"agent12",
			"agent13",
			"agent14",
			"agent15",
			"agent16",
			"agent17",
			"agent18",
			"agent19",
			"agent20",
			"agent21",
			"agent22",
			"agent23",
			"agent24",
			"agent25"
	};
	
	static private String [][] taskhierarchy = {
			{"1000", "Task1"    },
			{"1100", "Task1.1"  },
			{"1200", "Task1.2"  },
			{"1110", "Task1.1.1"},
			{"1120", "Task1.1.2"},
			{"1210", "Task1.2.1"},
			{"1220", "Task1.2.2"},
			{"1230", "Task1.2.3"}
	} ;

	
	static private String [][] agentCapabilityList = {
			{"agent01", "1000", "10.0"},
			{"agent02", "1100", "10.0"},
			{"agent09", "1100", "10.0"},
			{"agent03", "1200", "10.0"},
			{"agent10", "1200", "10.0"},
			{"agent11", "1200", "10.0"},
			{"agent12", "1200", "10.0"},
			{"agent04", "1110", "10.0"},
			{"agent13", "1110", "10.0"},
			{"agent14", "1110", "10.0"},
			{"agent05", "1120", "10.0"},
			{"agent15", "1120", "10.0"},
			{"agent16", "1120", "10.0"},
			{"agent17", "1120", "10.0"},
			{"agent06", "1210", "10.0"},
			{"agent18", "1210", "10.0"},
			{"agent19", "1210", "10.0"},
			{"agent20", "1210", "10.0"},
			{"agent07", "1220", "10.0"},
			{"agent21", "1220", "10.0"},
			{"agent22", "1220", "10.0"},
			{"agent23", "1220", "10.0"},
			{"agent08", "1230", "10.0"},
			{"agent24", "1230", "10.0"},
			{"agent25", "1230", "10.0"}
	} ;
	
	static public Map<String,Capability> getCapabilityList ( String agentName )
	{
		//List<Capability> capabilityList = new ArrayList<Capability>() ;
		Map<String,Capability> capabilityList = new HashMap<String,Capability>() ;
		
		for (int i = 0; i < agentCapabilityList.length; i++)
		{
			if ( agentCapabilityList[i][0].equalsIgnoreCase(agentName) )
			{
				capabilityList.put(agentName, new Capability ( agentCapabilityList[i] ) ) ;
			}
				
		}
		
		return capabilityList ;
	}
	
	
	
	public String getCapability() {
		return capability;
	}

	public void setCapability(String capability) {
		this.capability = capability;
	}

	public String getAgentname() {
		return agentname;
	}

	public void setAgentname(String agentname) {
		this.agentname = agentname;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	static public String[] getAgentNameList ()
	{
		return agentList ;
	}
	
	
	
	private String agentname  = null ;
	private String capability = null ;
	private double price      = 0;

	public Capability( String[] capabilityDescriptor )
	{
		this.agentname  = capabilityDescriptor[0] ;
		this.capability = capabilityDescriptor[1] ;
		this.price      = Double.parseDouble( capabilityDescriptor[2]) ;
	}
	
	

}
