package hello;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.xml.sax.SAXException;
import us.codecraft.xsoup.Xsoup;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hosainfathelbab on 2/24/15.
 */
@Component
@Scope("prototype")
public class ProxyGeneratorEngine extends Thread{


    @Autowired
    private ProxyDAO proxyDAO;

    @Override
    public void run() {
        while(true){
            try {
                updateProxyList("http://proxylist.hidemyass.com/");
                sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e){
                System.err.print("{error}" + e);
            }
        }
    }

    public List<MyProxyServer> updateProxyList(String url) throws IOException, ParserConfigurationException, SAXException {
        Document doc = Jsoup.connect(url).get();
        List<String> ipList = new ArrayList<String>() ;
        for(Element element : Xsoup.compile("//*[@id=\"listable\"]/tbody/tr/td[2]/span").evaluate(doc).getElements()){
            ipList.add(Parser.getIp(element));
        }

        List<String> portList = new ArrayList<String>() ;
        for(Element element : Xsoup.compile("//*[@id=\"listable\"]/tbody/tr/td[3]").evaluate(doc).getElements()){
            portList.add(element.html().replaceAll("\n", "").replaceAll(" ", ""));
        }

        List<String> protocolList = new ArrayList<String>() ;
        for(Element element : Xsoup.compile("//*[@id=\"listable\"]/tbody/tr/td[7]").evaluate(doc).getElements()){
            protocolList.add(element.html().replaceAll("\n","").replaceAll(" ",""));
        }

        List<String> countryList = new ArrayList<String>() ;
        for(Element element : Xsoup.compile("//*[@id=\"listable\"]/tbody/tr/td[4]/span").evaluate(doc).getElements()){
            String countryHtml = element.html();
            countryList.add(countryHtml.substring(countryHtml.lastIndexOf(">")+1));
        }

        List<MyProxyServer> proxies = new ArrayList<MyProxyServer>(countryList.size());
        for (int i =0 ; i< countryList.size() ; i++){
            proxies.add(new MyProxyServer(countryList.get(i), ipList.get(i), Integer.parseInt(portList.get(i)), protocolList.get(i)));
        }

        for(MyProxyServer proxy : proxies){
            proxyDAO.add(proxy);
        }
        return proxies;
    }
}
