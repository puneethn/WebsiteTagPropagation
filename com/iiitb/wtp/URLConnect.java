package com.iiitb.wtp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;

public class URLConnect {
	
	String tag = "";
	MarkovSim ms = new MarkovSim();
	Parser ObjParse = new Parser();

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public void connect(ArrayList<String> urlLinks, Map<String,DocumentVector> tagDV) {

		
		
		//Map<String, Map<String, DocumentVector>> list = new HashMap();
		ArrayList<String> list = new ArrayList<String>();
		// for(int i=0; i<urlList.size(); i++){
		// Iterator iter = urlList.entrySet().iterator();
		for (int i = 0; i < Global.ITERATIONSTEPS; i++) {
			list.clear();
			//for (Map.Entry<String, String> entry : urlList.entrySet()) {
			for (String url : urlLinks) {
		
				
				try {
					HttpHost proxy = new HttpHost("192.16.3.254", 8080, "http");
					HttpClient client = new DefaultHttpClient();
					client.getParams().setParameter(
							ConnRoutePNames.DEFAULT_PROXY, proxy);
					HttpGet get = new HttpGet(url);

					HttpResponse response = client.execute(get);
					InputStreamReader is = new InputStreamReader(response
							.getEntity().getContent(), "UTF8");
					BufferedReader rd = new BufferedReader(is);

					String line;
					String sourceLine = "";

					while ((line = rd.readLine()) != null) {
						sourceLine = sourceLine + line;
					}
				
					
					list.addAll(ObjParse.parse(sourceLine, url, tagDV));
						

					rd.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			urlLinks.clear();
			urlLinks.addAll(list);
			
		}
		try {
		String content;
	
		for (String string : list) {
			
				content = ms.getContent(string);
			ObjParse.addvector(string, content, tagDV);

		}
		
		ObjParse.serialiseWG();
		ObjParse.calcProb();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//ObjParse.runMarkovChain();
		//ObjParse.deserialiseWG();
		
	}
}