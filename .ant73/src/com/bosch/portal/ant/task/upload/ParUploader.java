package com.bosch.portal.ant.task.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ParUploader extends Task {
	//private static final String ARCHIVE_REMOVER_PATH = "irj/servlet/prt/portal/prteventname/delete/prtroot/com.sap.portal.runtime.system.console.ArchiveRemover";
    private static final String ARCHIVE_UPLOADER_PATH = "irj/servlet/prt/portal/prteventname/upload/prtroot/com.sap.portal.runtime.system.console.ArchiveUploader";
    
    //ant properties, which are needed for this task
    private static final String ANT_PROP_HOST = "PORTAL_HOST";
    private static final String ANT_PROP_USER = "PORTAL_USER";
    private static final String ANT_PROP_PASS = "PORTAL_PASS";
    
	private String portal_host="";
	private String portal_user="";
	private String portal_pass="";
	private String par_name="";
	private String base_dir="";
	private boolean verbose=false;
	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	
	public void execute() throws BuildException {
		portal_host = this.getProject().getProperty(ANT_PROP_HOST);
		portal_user = this.getProject().getProperty(ANT_PROP_USER);
		portal_pass = this.getProject().getProperty(ANT_PROP_PASS);
		
		par_name = this.getProject().getProperty("par.name");
		base_dir = this.getProject().getBaseDir().getAbsolutePath();
		
		String error_msg = "";
		
		if (par_name == null){
			error_msg += "Can't find property in build.xml:  par.name !";
		}
		
		if (portal_host == null || portal_user == null || portal_pass == null){
			error_msg+="\nCan't upload the par file to portal!\n";
			error_msg+="Please make the following global settings first: Window->Preferences->Ant->runtime->Properties \n";
			error_msg+=ANT_PROP_HOST+",\t"+ANT_PROP_USER+",\t"+ANT_PROP_PASS;
			
		}
		
		//this.getProject().addReference("commons-httpclient", "/EclipseClasspathTask/lib/commons-httpclient-3.0.1.jar");
		//this.getProject().addReference("commons-codec", "/EclipseClasspathTask/lib/commons-codec-1.3.jar");
		
		if (error_msg.length()>0){
			throw	new BuildException(error_msg);
		}else{
			upload();
		}
			
	}

	private void upload() throws BuildException{
		// register https with the customized socket factory
        Protocol.registerProtocol("https", 
        		new Protocol("https", (ProtocolSocketFactory) new MySecureProtocolSocketFactory(), 443));
        String query = "?login_submit=on&j_user="+portal_user+"&j_password="+portal_pass+"&j_authscheme=default&uidPasswrodLogon=Log%20on";
        PostMethod filePost = new PostMethod(portal_host+ARCHIVE_UPLOADER_PATH + query);

        filePost.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, false);        
        filePost.getParams().setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        
        try {
        	System.out.println("Uploading to " + portal_host + ARCHIVE_UPLOADER_PATH);
            
			//MimetypesFileTypeMap mmp = new MimetypesFileTypeMap();			
			
        	File par = new File(base_dir +"/"+ par_name+".par");
        	String contentType = "application/x-zip-compressed";
        	System.out.println("Par file : " + par.getAbsolutePath() );
        	
			// create file part
			FilePart fp = new FilePart("thefile", par);
			
			fp.setContentType(contentType);
			
								
			// set the parameters
			Part[] parts = {	fp,
								new StringPart("updateall","true")
							};	
			filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));

			// prepare the client
            HttpClient client = new HttpClient();
                        
            // set timeout
            client.getHttpConnectionManager().getParams().setConnectionTimeout(5000);            
			                
			// execute the upload
            int status = client.executeMethod(filePost);
            if (status == HttpStatus.SC_OK) {
            	System.out.println("Upload successfull!");
            	if (verbose) printToStream(System.out, filePost.getResponseBodyAsStream());                
                
            } else {
            	printToStream(System.out, filePost.getResponseBodyAsStream());
            	throw	new BuildException("\nHTTP " +status+": "+filePost.getStatusText());
                
            }
        } catch (Exception ex) {
        	throw new BuildException(ex.getMessage());      
            
        } finally {
            filePost.releaseConnection();
        }
		
	}
		
	private static void printToStream(OutputStream out, InputStream in) throws IOException {
		   byte[] buffer = new byte[256];
		   int byteCount = 0;
		   
		   while ((byteCount = in.read(buffer)) >= 0) {
		      out.write(buffer, 0, byteCount);
		   }
		   out.flush();
	}
	
}
