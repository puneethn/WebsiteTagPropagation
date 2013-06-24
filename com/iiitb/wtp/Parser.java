package com.iiitb.wtp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.http.client.ClientProtocolException;

import com.iiitb.model.SeedHandler;

public class Parser {

	String pageSource = null;
	SeedHandler db = null;
	static WebGraph wg = new WebGraph();
	long serialized_id;
	int count = 0;
	Double DocSimScore;
	MarkovSim ms = new MarkovSim();

	public Set<String> parse(String contentOfPage, String url,
			Map<String, DocumentVector> tagDV) throws SQLException, IOException {

		// Find if there are any hyper links. Add it to the crawl frontier.
		return hyperlinksExtractor(url, contentOfPage, tagDV);
		// return urlList;

	}

	private Set<String> hyperlinksExtractor(String url, String contentOfPage,
			Map<String, DocumentVector> tagDV) throws SQLException, IOException {
		// db = new SeedHandler();
		NormalizeURL nURL = new NormalizeURL();

		// Map<String, Map<String, DocumentVector>> list = new HashMap<>();
		// ArrayList<String> list = new ArrayList<>();
		Set<String> list = new HashSet<String>();
		String link = "";
		addvector(url, contentOfPage, tagDV);
		System.out.println("extracting hyperlinks for " + url);

		String[] ref1 = contentOfPage.split("a href=\"/wiki");
		String[] ref2 = contentOfPage.split("href=\"http://");
		String temp1[], temp2[];
		int i = 1;

		// dbConn.insertSearchedData(url, tag);
		try {
			for (i = 1; i < ref2.length; i++) {
				temp1 = ref2[i].split("\"");
				link = "http://" + temp1[0];

				// if (link.contains("#"))
				// System.out.println("contains #");
				URL connect = new URL(link);
				URLConnection yc = connect.openConnection();
				link = nURL.normalize(link);
				/*
				 * if(connect.getRef() != null){
				 * 
				 * index = link.indexOf(connect.getRef()); link =
				 * link.substring(0, index-1); }
				 */
				if (!link.equals("") && !link.endsWith(".css")
						&& !link.endsWith(".js") && !link.endsWith(".pdf")
						&& !link.endsWith(".ppt") && !link.endsWith("pptx")
						&& !link.endsWith("doc") && !link.endsWith("docx")
						&& !link.endsWith(".ps") && !link.endsWith("xls")
						&& !link.endsWith("xlsx") && !link.endsWith("txt")
						&& !link.endsWith(".rar") && !link.endsWith(".zip")
						&& !link.endsWith(".exe") && !link.endsWith(".EXE")
						&& !link.endsWith(".jpg") && !link.endsWith(".png")
						&& !link.endsWith(".gif") && !link.endsWith(".wmv")
						&& !link.endsWith(".tiff") && !link.endsWith(".mpeg")
						&& !link.endsWith(".svg") && !link.endsWith(".flv")
						&& !link.endsWith(".xml")
						&& !link.contains("twitter.com")
						&& !link.contains("facebook.com")
						&& !link.contains("sina.com")
						&& !link.contains("youtube.com")
						&& !link.contains("waybackmachine.org")
						&& !link.contains("archive.org")) {
					if (wg.outLink(url, link) == 0.0) {

						list.add(link);
						wg.addLink(url, link, 1.0);

					}
				}

			}

			for (i = 1; i < ref1.length; i++) {

				temp1 = ref1[i].split("\"");

				// If temp1[0] has ":" then it is not a valid URL (it must again
				// point to another link!)
				temp2 = temp1[0].split(":");

				if (temp2.length < 2) {

					link = "http://en.wikipedia.org/wiki" + temp1[0];

					URL connect = new URL(link);
					URLConnection yc = connect.openConnection();
					link = nURL.normalize(link);

					if (!link.equals("") && !link.endsWith(".css")
							&& !link.endsWith(".js") && !link.endsWith(".pdf")
							&& !link.endsWith(".ppt") && !link.endsWith("pptx")
							&& !link.endsWith("doc") && !link.endsWith("docx")
							&& !link.endsWith(".ps") && !link.endsWith("xls")
							&& !link.endsWith("xlsx") && !link.endsWith("txt")
							&& !link.endsWith(".rar") && !link.endsWith(".zip")
							&& !link.endsWith(".exe") && !link.endsWith(".EXE")
							&& !link.endsWith(".jpg") && !link.endsWith(".png")
							&& !link.endsWith(".svg") && !link.endsWith(".flv")
							&& !link.endsWith(".gif") && !link.endsWith(".wmv")
							&& !link.endsWith(".tiff")
							&& !link.endsWith(".mpeg")
							&& !link.endsWith(".xml")
							&& !link.contains("twitter.com")
							&& !link.contains("facebook.com")
							&& !link.contains("sina.com")
							&& !link.contains("youtube.com")
							&& !link.contains("waybackmachine.org")
							&& !link.contains("archive.org")) {
						if (wg.outLink(url, link) == 0.0) {

							list.add(link);
							wg.addLink(url, link, 1.0);

						}
					}

				}

			}

		} catch (Exception e1) {
			serialiseWG();
			e1.printStackTrace();

		}
		return list;

	}

	public void addvector(String url, String contentOfPage,
			Map<String, DocumentVector> tagDV) {
		DocumentVector urlDV = ms.getdocVector(contentOfPage);
		for (Map.Entry<String, DocumentVector> tags : tagDV.entrySet()) {
			DocSimScore = urlDV.getCosineSimilarityWith(tags.getValue());
			wg.addVector(url, tags.getKey(), DocSimScore);
		}

	}

	public DocumentVector getWikiDV(String tag) throws ClientProtocolException,
			IOException {

		MarkovSim ms = new MarkovSim();
		String[] split = tag.trim().split("\\s+");
		String str = null;
		String inString = "";
		for (String string : split) {

			str = Character.toString(string.charAt(0)).toUpperCase()
					+ string.substring(1);
			str = str + "_";
			inString = inString + str;
		}
		inString = inString.substring(0, inString.length() - 1);
		String url = "http://en.wikipedia.org/wiki/" + inString;
		try {
			URL connect = new URL(url);
			connect.openConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("tag url " + url);
		String content = ms.getContent(url);
		DocumentVector tagVector = ms.getdocVector(content);
		return tagVector;
	}

	public void serialiseWG() throws SQLException {
		db = new SeedHandler();
		/*
		 * WebGraph wg1; File file = new File(Global.fileName);
		 * if(file.exists()){ wg1 = new WebGraph(); wg1 =
		 * (WebGraph)db.deSerializeJavaObjectFromFile(); wg.merge(wg1); }
		 */
		Map<String, String> seeds = new HashMap<>();
		seeds = db.querySeedData();
		for (Map.Entry<String, String> entry : seeds.entrySet()) {
			wg.setSeed_url(entry.getKey());
			wg.setTags(entry.getValue());
		}

		db.serializeJavaObjectToFile(wg);

	}

	public void deserialiseWG() throws SQLException, ClassNotFoundException,
			IOException {
		db = new SeedHandler();

		wg = (WebGraph) db.deSerializeJavaObjectFromFile();
		System.out.println(wg.numNodes() + "Number of nodes");

	}

	public void calcProb() throws SQLException, ClientProtocolException,
			IOException, ClassNotFoundException {
		Probability prob = new Probability();

		prob.updateDefaultProb(wg);
		
		MarkovSim ms = new MarkovSim();
		ms.runSimulation(wg);
	}

	public void calcProbDeserialized() throws SQLException,
			ClientProtocolException, IOException, ClassNotFoundException {
		db = new SeedHandler();
		Probability prob = new Probability();
		deserialiseWG();
		prob.updateDefaultProb(wg);
		MarkovSim ms = new MarkovSim();
		ms.runSimulation(wg);
		db.queryHITS();
		
	}

	public int getNumNodes() {
		return wg.numNodes();

	}

}
