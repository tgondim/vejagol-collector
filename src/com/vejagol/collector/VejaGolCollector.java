package com.vejagol.collector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.vejagol.controller.CadastroJogo;
import com.vejagol.model.Jogo;
import com.vejagol.util.StringUtils;

public class VejaGolCollector extends HttpServlet {	
    
	private static final long serialVersionUID = -8770144871741483858L;
	private static final int MAX_REPETICOES = 3;
	private static final int MAX_EXCESSOES = 3;	
	private static final long INTERVALO_ATUALIZACAO = (1000 * 60 * 15);
	
	private long intervaloAtualizacao;
	private Timer timer;

	static Log log = LogFactory.getLog(VejaGolCollector.class);
	
//    @SuppressWarnings("rawtypes")
//	static private DomNodeList listaDivs;
    
    public VejaGolCollector() {
    	this.intervaloAtualizacao = INTERVALO_ATUALIZACAO;
    	this.timer = new Timer();
    	this.timer.scheduleAtFixedRate(new CollectorTimerTask(), this.intervaloAtualizacao, this.intervaloAtualizacao);
    }
        
	public static void main(String args[]) {
		VejaGolCollector vejagolColector = new VejaGolCollector();
		vejagolColector.new CollectorTimerTask().run();
	}
	
//	private static void log(String fileName, String info){
//
//        String addInfo;
//        String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
//
//        Calendar cal = Calendar.getInstance();
//        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
//        addInfo = sdf.format(cal.getTime());
//        System.out.println(addInfo + " - " + info);
//        FileWriter fw;
//		try {
//			fw = new FileWriter(new File(fileName), true);
//			fw.write(info);
//			fw.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {	
		doPost(req, resp);
	}
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doPost(req, resp);
	}
	
	class CollectorTimerTask extends TimerTask {
		
		@Override
		public void run() {
			WebClient webClient;
			
			HtmlPage paginaPrincipal;
			HtmlPage paginaJogo;
			
			HtmlBody bodyPrincipal;
			
//			HtmlDivision divPrincipal;
//			HtmlDivision divJogo;
			
//			HtmlAnchor aJogo;	   	
			
			
			
			String url = "http://www.tvgolo.com/football.php";
			
			Jogo jogo;
			
			CadastroJogo cadastroJogo= new CadastroJogo();
			
			int jogosEncontrados = 0;
			int excessoes = 0;
			int maxRepeticoesJogos;
			int maxExececoes;
			int j = 0;
			String toNavigateUrl;
			
			//try {
			//	j = Integer.valueOf(args[0]);
			//} catch (Exception e) {
			
			//}
			
			File file = new File("VejaGolCollector.properties");
			if (!file.exists()) {
				maxRepeticoesJogos = MAX_REPETICOES;
				maxExececoes = MAX_EXCESSOES;				
			} else { 
				Properties prop = new Properties();
				try {
					prop.load(new FileInputStream(file));
					maxRepeticoesJogos = Integer.valueOf(prop.getProperty("max_repeticoes_jogos"));
					maxExececoes = Integer.valueOf(prop.getProperty("max_excecoes"));
				} catch (Exception e) {
					maxRepeticoesJogos = MAX_REPETICOES;
					maxExececoes = MAX_EXCESSOES;
				}
			}
			
			while (excessoes < maxExececoes) {
				try {
					log.info("Initializing VejaGolTests");
					webClient = new WebClient(BrowserVersion.FIREFOX_3_6);
					webClient.setJavaScriptEnabled(false);
					webClient.setCssEnabled(true);        	
					boolean hasMorePages = true;
					//290
					while (hasMorePages) {        		        		
						
						toNavigateUrl = url + "?start_from=" + j + "&ucat=&archive=&subaction=&id=&";
						log.info("toNavigateUrl=" + toNavigateUrl);
						paginaPrincipal = (HtmlPage) webClient.getPage(toNavigateUrl);
						
						bodyPrincipal = (HtmlBody)paginaPrincipal.getBody();
//						divPrincipal = bodyPrincipal.getElementById("content");
						
						Iterable<HtmlElement> list = bodyPrincipal.getElementsByTagName("div");   
						List<HtmlAnchor> listaJogosA = new ArrayList<HtmlAnchor>();
						String classe;
						for (HtmlElement html : list) {
							if ((classe = html.getAttribute("class")) != null) {
								if (classe.equals("listajogos")) {
									listaJogosA.add((HtmlAnchor)html.getFirstChild());
								}
							}
						}
//						listaDivs = bodyPrincipal.getElementsByTagName("div");        		
						
						if (listaJogosA.size() < 30) {
							hasMorePages = false;
						}
						
//						for (int index = 1; index < listaJogosA.size(); index++) {
						for (HtmlAnchor aJogo : listaJogosA) {
							
//							divJogo = (HtmlDivision)listaDivs.get(index-1);            	
							
//							aJogo = (HtmlAnchor)divJogo.getFirstChild();
							
							paginaJogo = (HtmlPage) webClient.getPage(url + aJogo.getHrefAttribute());
							excessoes = 0;
							String htmlText = paginaJogo.getWebResponse().getContentAsString();        		
							
//	        				Pattern descricaoRegex = Pattern.compile("(Match:\\s*)((18|19|20|21)\\d{2}).(0[1-9]|[1][012]).(0[1-9]|[12][0-9]|3[01])\\s*(\\(\\d{2}h\\d{2}\\))\\s*-\\s*([a-zA-Z 0-9-./��&;]*)\\s*(\\d{1}|\\d{2})-(\\d{1}|\\d{2})\\s*(\\([a-zA-Z 0-9-./��&;]*\\))*([a-zA-Z 0-9-./��&;]*)\\s*(\\([a-zA-Z 0-9-./��&;]*\\))*\\s*(- League:)\\s*([a-zA-Z 0-9]*)", Pattern.CASE_INSENSITIVE);
							//(Match:\s*)((18|19|20|21)\d{2}).(0[1-9]|[1][012]).(0[1-9]|[12][0-9]|3[01])\s*(\(\d{2}h\d{2}\))\s*-\s*([a-zA-Z 0-9-./��&��;]*)\s*(\d{1}|\d{2})-(\d{1}|\d{2})\s*(\([a-zA-Z 0-9-./��&��;]*\))*([a-zA-Z 0-9-./��&��;]*)\s*(\([a-zA-Z 0-9-./��&��;]*\))*\s*(\([a-zA-Z 0-9-./��&��;]*\))*\s*(- League:)\s*([a-zA-Z 0-9]*)
							//Pattern descricaoRegex = Pattern.compile("(Match:\\s*)((18|19|20|21)\\d{2}).(0[1-9]|[1][012]).(0[1-9]|[12][0-9]|3[01])\\s*(\\(\\d{2}h\\d{2}\\))\\s*-\\s*([a-zA-Z 0-9-./��&��;]*)\\s*(\\d{1}|\\d{2})-(\\d{1}|\\d{2})\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*([a-zA-Z 0-9-./��&��;]*)\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*\\s*(- League:)\\s*([a-zA-Z 0-9]*)", Pattern.CASE_INSENSITIVE);
							Pattern descricaoRegex = Pattern.compile("((18|19|20|21)\\d{2}).(0[1-9]|[1][012]).(0[1-9]|[12][0-9]|3[01])\\s*(\\(\\d{2}h\\d{2}\\))\\s*-\\s*([a-zA-Z 0-9-./��&��;]*)\\s*(\\d{1}|\\d{2})-(\\d{1}|\\d{2})\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*([a-zA-Z 0-9-./��&��;]*)\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*\\s*(\\([a-zA-Z 0-9-./��&��;]*\\))*\\s*(- League:)*\\s*([a-zA-Z 0-9]*)", Pattern.CASE_INSENSITIVE);
							Pattern ligaRegex = Pattern.compile("alt=\"(\\w*\\s*\\w*) icon\"");
							Pattern youtubeLinkRegex = Pattern.compile("(http:\\/\\/w{0,3}\\.youtube[^' '\"]+)");
							Pattern dailymotionLinkRegex = Pattern.compile("(ht|f)tp:\\/\\/w{0,3}.dailymotion[a-zA-Z0-9_\\-.:#/~}]+");
							Pattern videaLinkRegex = Pattern.compile("(http://videa.hu/[\\w.?=]*)");
							Pattern rutubeLinkRegex = Pattern.compile("(http://video.rutube.ru/[\\w.?=]*)");
							Pattern sapoLinkRegex = Pattern.compile("(http://rd3.videos.sapo.pt/[\\w.:/?=]*)");
							Pattern mediaservicesLinkRegex = Pattern.compile("(http://mediaservices.myspace.com/[\\w.:/?=,]*)");
							Pattern yandexLinkRegex = Pattern.compile("(http://static.video.yandex.ru/[\\w.:/?=,]*)");
							
							Matcher descricaoMatcher = descricaoRegex.matcher(htmlText);
							Matcher ligaMatcher = ligaRegex.matcher(htmlText);
							Matcher youtubeLinkMatcher = youtubeLinkRegex.matcher(htmlText);
							Matcher dailymotionLinkMatcher = dailymotionLinkRegex.matcher(htmlText);
							Matcher videaLinkMatcher = videaLinkRegex.matcher(htmlText);
							Matcher rutubeLinkMatcher = rutubeLinkRegex.matcher(htmlText);
							Matcher sapoLinkMatcher = sapoLinkRegex.matcher(htmlText);
							Matcher mediaservicesLinkMatcher = mediaservicesLinkRegex.matcher(htmlText);
							Matcher yandexLinkMatcher = yandexLinkRegex.matcher(htmlText);
							
							jogo = new Jogo();
							descricaoMatcher.find();
							
							if (descricaoMatcher.find()) {
								log.info(aJogo.getTextContent());
								if (descricaoMatcher.find()) {
									Calendar newData = Calendar.getInstance();
									newData.set(Integer.valueOf(descricaoMatcher.group(1)), 
											Integer.valueOf(descricaoMatcher.group(3))-1, 
											Integer.valueOf(descricaoMatcher.group(4)),
											Integer.valueOf(descricaoMatcher.group(5).substring(1, 3)),
											Integer.valueOf(descricaoMatcher.group(5).substring(4, 6)));
									
									newData.set(Calendar.SECOND, 0);
									newData.set(Calendar.MILLISECOND, 0);
									
									jogo.setData(newData);
									
									jogo.setTimeCasa(descricaoMatcher.group(6) != null ? StringUtils.unescapeHTML(descricaoMatcher.group(6).trim()) : "");
									jogo.setTimeVisitante(descricaoMatcher.group(10) != null ? StringUtils.unescapeHTML(descricaoMatcher.group(10).trim()) : "");
									jogo.setPlacarCasa(Integer.valueOf(descricaoMatcher.group(7) != null ? descricaoMatcher.group(7) : "0"));
									jogo.setPlacarVisitante(Integer.valueOf(descricaoMatcher.group(8) != null ? descricaoMatcher.group(8) : "0"));
									
									int camp;
									if (descricaoMatcher.group(12) == null) {
										camp = 11;
									} else {
										camp = 12;
									}
									
									jogo.setCampeonato(descricaoMatcher.group(camp) != null ? StringUtils.unescapeHTML(descricaoMatcher.group(camp).trim().substring(1, descricaoMatcher.group(11).trim().length()-1)) : "");
									if (ligaMatcher.find()) {										
										//jogo.setLiga(descricaoMatcher.group(14) != null ? StringUtils.unescapeHTML(descricaoMatcher.group(14).trim()) : "");
										jogo.setLiga(ligaMatcher.group(1));
									}
								}
							}
							
							if (youtubeLinkMatcher.find()) {
								
								String youtubeLink = "http://www.youtube.com/embed/";
								String auxLink = youtubeLinkMatcher.group() != null ? youtubeLinkMatcher.group().trim() : "";
								
								Pattern youtubeIdRegex = Pattern.compile("\\b(?<=v.|/)[a-zA-Z0-9_-]{11,}\\b");
								Matcher youtubeIdMatcher = youtubeIdRegex.matcher(auxLink);
								if (youtubeIdMatcher.find()) {
									youtubeLink += (youtubeIdMatcher.group() != null ? youtubeIdMatcher.group().trim() : "");
								} else {
									youtubeLink = auxLink;
								}
								
								log.info(youtubeLink);
								jogo.setLink(youtubeLink);
							} else if (dailymotionLinkMatcher.find()) {
								log.info(dailymotionLinkMatcher.group() != null ? dailymotionLinkMatcher.group().trim() : "");
								jogo.setLink(dailymotionLinkMatcher.group() != null ? dailymotionLinkMatcher.group().trim() : "");
							} else if (videaLinkMatcher.find()) {
								log.info(videaLinkMatcher.group() != null ? videaLinkMatcher.group().trim() : "");
								jogo.setLink(videaLinkMatcher.group() != null ? videaLinkMatcher.group().trim() : "");
							} else if (rutubeLinkMatcher.find()) {
								log.info(rutubeLinkMatcher.group() != null ? rutubeLinkMatcher.group().trim() : "");
								jogo.setLink(rutubeLinkMatcher.group() != null ? rutubeLinkMatcher.group().trim() : "");
							} else if (sapoLinkMatcher.find()) {
								log.info(sapoLinkMatcher.group() != null ? sapoLinkMatcher.group().trim() : "");
								jogo.setLink(sapoLinkMatcher.group() != null ? sapoLinkMatcher.group().trim() : "");
							} else if (mediaservicesLinkMatcher.find()) {
								log.info(mediaservicesLinkMatcher.group() != null ? mediaservicesLinkMatcher.group().trim() : "");
								jogo.setLink(mediaservicesLinkMatcher.group() != null ? mediaservicesLinkMatcher.group().trim() : "");
							} else if (yandexLinkMatcher.find()) {
								log.info(yandexLinkMatcher.group() != null ? yandexLinkMatcher.group().trim() : "");
								jogo.setLink(yandexLinkMatcher.group() != null ? yandexLinkMatcher.group().trim() : "");
							}
							if ((jogo.getData() == null) || (jogo.getTimeCasa() == null) || (jogo.getTimeVisitante() == null)) {
								log.info("Este jogo nao foi inserido. Validar manualmente. link=" + jogo.getLink() + "\n");
							} else {
								if (jogo.getLink() == null) {
									log.info("Este jogo nao foi inserido. Validar manualmente. data=" + jogo.getData() + " timeCasa=" + jogo.getTimeCasa() + " timeVisitante=" + jogo.getTimeVisitante() + "\n");
								}
								if (!cadastroJogo.adicionar(jogo)) {
									jogosEncontrados++;        				
								} else {
									jogosEncontrados = 0;
								}
							}
							if (jogosEncontrados > maxRepeticoesJogos) {
								log.info(maxRepeticoesJogos + " jogos repetidos encontrados. Abortando atualizacao.");
								return;
							}
						}
						j += 29;
					}
					log.info("\n\n...done!\n");
				} catch (Exception e) {
					excessoes++;
					e.printStackTrace();
				}
			}		
		}		
	}
}
