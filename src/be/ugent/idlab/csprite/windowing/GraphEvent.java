package be.ugent.idlab.csprite.windowing;

/**
 * 
 */



/**
 * @author pbonte
 *
 */
public class GraphEvent {

	private long id;
	private String s;
	private String p;
	private String o;

	public GraphEvent(long id, String s, String p, String o){
		this.id = id;
		this.s = s;
		this.p = p;
		this.o = o;
	}
	
	public String getS() {
		return s;
	}

	public String getP() {
		return p;
	}

	public String getO() {
		return o;
	}

	public long getId(){
		return id;
	}
}
