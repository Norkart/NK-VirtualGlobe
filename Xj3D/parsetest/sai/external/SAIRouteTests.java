import org.web3d.x3d.sai.ExternalBrowser;
import org.web3d.x3d.sai.X3DRoute;
import org.web3d.x3d.sai.X3DScene;

/**
 * SAIRouteTests
 * @author Brad Vender
 *
 */
public class SAIRouteTests {
	public static void main(String[] args) {
		ExternalBrowser br=SAITestFactory.getBrowser(true);
		X3DScene s=br.createX3DFromString(
			"PROFILE Interactive\n"+
			"DEF A Transform {}\n"+
			"DEF B Transform {}\n"+
			"DEF C Transform {}\n"+
			"DEF D Transform {}\n"+
			"ROUTE A.children TO B.children\n"+
			"ROUTE C.translation TO D.translation\n"
		);
		s.getRootNodes()[1].getField("children").addX3DEventListener(new GenericSAIFieldListener());
		X3DRoute routes[]=s.getRoutes();
		System.out.println("Number of routes:"+routes.length);
		for (int counter=0;counter<routes.length;counter++)
			System.out.println(routes[counter]);
		System.out.println("----");
		s.removeRoute(routes[1]);
		routes=s.getRoutes();
		System.out.println("Number of routes:"+routes.length);
		for (int counter=0;counter<routes.length;counter++)
			System.out.println(routes[counter]);
		
	}
}
