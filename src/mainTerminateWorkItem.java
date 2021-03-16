

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import filenet.vw.api.VWFetchType;
import filenet.vw.api.VWQueue;
import filenet.vw.api.VWRoster;
import filenet.vw.api.VWRosterQuery;
import filenet.vw.api.VWSession;
import filenet.vw.api.VWWorkObject;
import config.FieldFilterObj;
import config.FieldFilterStr;

public class mainTerminateWorkItem {
	public static void main(String[] args) throws Exception {
		terminateWorkItem();
	}
	private static void terminateWorkItem() {
		VWSession m_peSession = new VWSession();
		m_peSession.setBootstrapCEURI("http://192.168.0.118:9080/wsi/FNCEWS40MTOM/");
		m_peSession.logon("p8admin", "Admin123", "Cpoint1");
		VWRoster roster = null;
		//Parameter >>>
//		String rosterName="Panda";
//		String stepName="Trả lại";
		String rosterName="DophinDemo";
		String stepName="Gửi phê duyệt";
		String caseID="{7AA63B18-6D8C-4A07-9430-8724F64C70C3}";
		//Parameter <<<
		roster = m_peSession.getRoster(rosterName);		
		roster.setBufferSize(50000);
		List<FieldFilterObj> filter = new ArrayList<FieldFilterObj>();
		filter.add(new FieldFilterObj("F_CaseFolder",convertGuidToDbFormat(caseID)));
		FieldFilterStr fString =getFilterString(filter);
		VWRosterQuery query = roster.createQuery(null, null, null, VWQueue.QUERY_READ_LOCKED,
				fString.fields, fString.values, VWFetchType.FETCH_TYPE_WORKOBJECT);
		
		while (query.hasNext()) {
			System.out.println("Terminate 1");			
			VWWorkObject vwwo = (VWWorkObject) query.next();
			System.out.println("VSII: "+ vwwo.getStepName());
			Boolean rs = assertAtStep(vwwo,stepName);			
		}
		System.out.println("OK");
	}
	public static String convertGuidToDbFormat(String guid) {
		String s1 = guid.substring(1, 3);
		String s2 = guid.substring(3, 5);
		String s3 = guid.substring(5, 7);
		String s4 = guid.substring(7, 9);
		String s5 = guid.substring(10, 12);
		String s6 = guid.substring(12, 14);
		String s7 = guid.substring(15, 17);
		String s8 = guid.substring(17, 19);
		String s9 = guid.substring(20, 24);
		String s10 = guid.substring(25,37);
		return s4+s3+s2+s1+s6+s5+s8+s7+s9+s10;

		}
	private static FieldFilterStr getFilterString(List<FieldFilterObj> lstFilter) {
		FieldFilterStr filterStr = new FieldFilterStr();
		String filter = "";
		String fieldFilter = "";
		List<String> filterValues = new ArrayList<String>();
		for (FieldFilterObj fieldFilterObj : lstFilter) {
			List<String> lstValue = fieldFilterObj.values;
			fieldFilter = "";
			for (String string : lstValue) {
				filterValues.add(string);
				fieldFilter = fieldFilter.equals("") ? fieldFilterObj.name
						+ " = :A" : fieldFilter + " OR " + fieldFilterObj.name
						+ " = :A";
			}
			filter = filter.equals("") ? "(" + fieldFilter + ")" : filter
					+ " AND " + " (" + fieldFilter + ")";
		}
		
		filterStr.fields = filter;
		filterStr.values = new String[filterValues.size()];		
		for (int i = 0; i < filterValues.size(); i++) {			
			filterStr.values[i] = filterValues.get(i);
		}
		return filterStr;
	}
	public static boolean assertAtStep(VWWorkObject wob, String atResponseName) {
		 try {					
	        boolean isAtStep = false;
	        String foundStepName = wob.getAuthoredStepName();
	        System.out.println(foundStepName);
	        String [] lstRes = wob.getStepResponses();
	        if(lstRes != null && Arrays.asList(lstRes).contains(atResponseName)) {
	        	for (String string : lstRes) {
					System.out.println("VSII: " + string);
				}
	        	wob.doLock(true);
	        	wob.setSelectedResponse(atResponseName);
	        	wob.doDispatch();
	        	isAtStep = true;
	        }
	        return isAtStep;
		 } catch (Exception e) {
				// TODO: handle exception
			 System.out.println("Err assertAtStep: " + e.getMessage());
			 return false;
			}
	    }
}
