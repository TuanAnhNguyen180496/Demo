package Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

//import Service.DophinService;





import org.json.simple.JSONObject;
import com.filenet.api.collection.ContentElementList;
import com.filenet.api.collection.IndependentObjectSet;
import com.filenet.api.constants.AutoClassify;
import com.filenet.api.constants.AutoUniqueName;
import com.filenet.api.constants.CheckinType;
import com.filenet.api.constants.ClassNames;
import com.filenet.api.constants.DefineSecurityParentage;
import com.filenet.api.constants.RefreshMode;
import com.filenet.api.core.Connection;
import com.filenet.api.core.ContentTransfer;
import com.filenet.api.core.Document;
import com.filenet.api.core.Domain;
import com.filenet.api.core.DynamicReferentialContainmentRelationship;
import com.filenet.api.core.Factory;
import com.filenet.api.core.Folder;
import com.filenet.api.core.IndependentObject;
import com.filenet.api.core.IndependentlyPersistableObject;
import com.filenet.api.core.ObjectStore;
import com.filenet.api.core.ReferentialContainmentRelationship;
import com.filenet.api.property.Properties;
import com.filenet.api.query.SearchSQL;
import com.filenet.api.query.SearchScope;
import com.filenet.api.util.Id;
import com.filenet.api.util.UserContext;
import com.ibm.ecm.extension.PluginServiceCallbacks;

class UploadFolDer {
	public static ObjectStore getObjectStore() {
		ObjectStore os = null;
		try {
			Connection conn = Factory.Connection
					.getConnection("http://192.168.0.118:9080/wsi/FNCEWS40MTOM/");
			Subject subject = UserContext.createSubject(conn, "p8admin",
					"Admin123", "FileNetP8WSI");
			UserContext.get().pushSubject(subject);
			Domain domain = Factory.Domain.getInstance(conn, null);
			os = Factory.ObjectStore.fetchInstance(domain, "cmtos", null);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return os;
	}

	public static String FolderId(String caseID) {
		String id = "";
		ObjectStore os = getObjectStore();
		String strQuery = String
				.format("SELECT * FROM CmAcmCaseFolder  WHERE CmAcmCaseIdentifier = '%s'",
						caseID);
		org.json.simple.JSONArray jsonArray = new org.json.simple.JSONArray();
		SearchSQL sqlObject = new SearchSQL();
		sqlObject.setQueryString(strQuery.toString());
		SearchScope searchScope = new SearchScope(os);
		IndependentObjectSet objSet = searchScope.fetchObjects(sqlObject, null,
				null, new Boolean(true));
		@SuppressWarnings("unchecked")
		Iterator<IndependentObject> it = objSet.iterator();
		while (it.hasNext()) {
			Folder f = (Folder) it.next();
			Properties properties = f.getProperties();
			id = f.get_Id().toString();
			System.out.println("lyld" + id);
		}
		return id;
	}

	public static String uploadFileDoc() {
		String result = "";
		ObjectStore os = getObjectStore();
		try {
			String docName = "LYLD";
			String docContent = "";
			String folderId = "";
			String docType = "text/plain";
			String classDoc = "";
				byte[] imageByte = DatatypeConverter
						.parseBase64Binary(docContent.trim());
				InputStream is = new ByteArrayInputStream(imageByte);
				com.filenet.api.core.Document doc = Factory.Document
						.createInstance(os, ClassNames.DOCUMENT);
				ContentTransfer contentTransfer = Factory.ContentTransfer
						.createInstance();
				ContentElementList contentElementList = Factory.ContentElement
						.createList();
				contentTransfer.setCaptureSource(is);
				contentElementList.add(contentTransfer);
				doc.set_ContentElements(contentElementList);
				contentTransfer.set_RetrievalName(docName);
				contentTransfer.set_ContentType(docType);
				doc.set_MimeType(docType);
				doc.checkin(AutoClassify.DO_NOT_AUTO_CLASSIFY,
						CheckinType.MAJOR_VERSION);
				com.filenet.api.property.Properties prop = doc.getProperties();
				prop.putValue("DocumentTitle", docName);
				doc.save(RefreshMode.REFRESH);
				// updatePropertiesDoc(os, mapProperties, doc);
				Properties caseMgmtProperties = doc.getProperties();
				doc.save(RefreshMode.REFRESH);
				Folder folder = Factory.Folder.fetchInstance(os, new Id(FolderId("PD_SCS_000000100004")), null);
				ReferentialContainmentRelationship rc = folder.file(doc,
								AutoUniqueName.AUTO_UNIQUE,
								doc.get_Name(),
								DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
				rc.save(RefreshMode.REFRESH);
				result = "Ok";
				System.out.println("Ok");

			
		} catch (Exception ex) {
			System.out.println("Create document exception:" + ex.toString());
			ex.printStackTrace();
			//logger.info(ex.getMessage());
			result = ex.getMessage();
		}
		return result;
	}
	
	private static Folder createFolderByPath() {
		ObjectStore os = getObjectStore();
		Folder fParent = Factory.Folder.fetchInstance(os, "/Khách hàng", null);
		String sFoldeName = "LyLd";
        try {
            String fPath = fParent.get_PathName() + "/" + sFoldeName;
            return Factory.Folder.fetchInstance(os, fPath, null);
        } catch (Exception e) {
            Folder f = Factory.Folder.createInstance(os, "CIFModel");
            f.set_FolderName(sFoldeName);
            f.set_Parent(fParent);
            f.save(RefreshMode.REFRESH);
            return f;
        }
    }
	public static void SaveDocuments(){
		ObjectStore os = getObjectStore();
		String fSave = "LyLd";
		 String strQuery = "SELECT * FROM CmAcmCaseFolder  WHERE CmAcmCaseIdentifier = 'PD_SCS_000000100004'";
	        SearchSQL sqlObject = new SearchSQL();
	        SearchScope searchScope = new SearchScope(os);
	        sqlObject.setQueryString(strQuery);
	        IndependentObjectSet objSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
	        Iterator<IndependentObject> it = objSet.iterator();
	        while (it.hasNext()) {
	            Folder f = (Folder) it.next();
	            Iterator<Document> lstDoc = f.get_ContainedDocuments().iterator();
	            Folder sFolder = Factory.Folder.fetchInstance(os, "/Khách hàng/" + fSave, null);
	            while (lstDoc.hasNext()) {
	                Document d = lstDoc.next();
	                d.save(RefreshMode.REFRESH);
	                DynamicReferentialContainmentRelationship drcr = (DynamicReferentialContainmentRelationship) sFolder.file((IndependentlyPersistableObject) d,
	                        AutoUniqueName.AUTO_UNIQUE, d.getProperties().getStringValue("Name"), DefineSecurityParentage.DO_NOT_DEFINE_SECURITY_PARENTAGE);
	                drcr.save(RefreshMode.REFRESH);
	                drcr.refresh();
	            }
	            return;
	        }
	}
	private static void requestProperties() throws IOException{
		ObjectStore os = getObjectStore();
		String caseID = "PD_SCS_000000100004";
		String strQuery = String.format("SELECT * FROM CmAcmCaseFolder  WHERE CmAcmCaseIdentifier = '%s'",
				caseID);
		org.json.simple.JSONArray jsonArray = new org.json.simple.JSONArray();
		SearchSQL sqlObject = new SearchSQL();
        sqlObject.setQueryString(strQuery);
        System.out.println( "anhnt"+strQuery);
        SearchScope searchScope = new SearchScope(os);
        IndependentObjectSet objSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
        Iterator<IndependentObject> it = objSet.iterator();
        while (it.hasNext()) {
        	Folder f = (Folder) it.next();
        	org.json.simple.JSONObject jsonResponse = new JSONObject();
        	Properties properties = f.getProperties();
        	if(properties.find("PD_Name") != null){
			jsonResponse.put("Name", f.getProperties().getObjectValue("PD_Name"));}
        	if(properties.find("PD_Age") != null){
			jsonResponse.put("Age", f.getProperties().getObjectValue("PD_Age"));}
        	if(properties.find("PD_MoneyLoan") != null){
			jsonResponse.put("MoneyLoan", f.getProperties().getObjectValue("PD_MoneyLoan"));}
			if(properties.find("PD_gender") != null){
			jsonResponse.put("Gender", f.getProperties().getObjectValue("PD_gender"));}
			if(properties.find("PD_phone") != null){
			jsonResponse.put("Phone", f.getProperties().getObjectValue("PD_phone"));}
			jsonArray.add(jsonResponse);
			
			System.out.println( "anhnt"+jsonResponse);
        }
        /*PrintWriter output = response.getWriter();
		output.print(jsonArray.toJSONString());
		output.flush();*/
	}
	private static void WorkDetail() throws IOException{
		ObjectStore os = getObjectStore();
		String caseID = "PD_SCS_%";
		String strQuery = String.format("SELECT * FROM CmAcmCaseFolder  WHERE CmAcmCaseIdentifier like '%s'",
				caseID);
		org.json.simple.JSONArray jsonArray = new org.json.simple.JSONArray();
		SearchSQL sqlObject = new SearchSQL();
        sqlObject.setQueryString(strQuery);
        System.out.println( "anhnt"+strQuery);
        SearchScope searchScope = new SearchScope(os);
        IndependentObjectSet objSet = searchScope.fetchObjects(sqlObject, null, null, new Boolean(true));
        Iterator<IndependentObject> it = objSet.iterator();
        while (it.hasNext()) {
        	Folder f = (Folder) it.next();
        	org.json.simple.JSONObject jsonResponse = new JSONObject();
        	Properties properties = f.getProperties();
        	if(properties.find("CmAcmCaseIdentifier") != null){
			jsonResponse.put("CaseID", f.getProperties().getObjectValue("CmAcmCaseIdentifier"));}
			jsonArray.add(jsonResponse);
			System.out.println( "anhnt"+jsonResponse);
        }
        /*PrintWriter output = response.getWriter();
		output.print(jsonArray.toJSONString());
		output.flush();*/
	}
	
	
	

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FolderId("DP_DOPHIN_000000100001");
		/*uploadFileDoc();
		createFolderByPath();
		SaveDocuments();*/
		requestProperties();
		WorkDetail();
	}

}
