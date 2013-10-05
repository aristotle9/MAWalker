package net;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;

import walker.Info;


public class Network {
	private static final String Auth = "eWa25vrE";
	private static final String Key = "2DbcAh3G";
	private static final String SERVER_HOST = "web.million-arthurs.com";
	
	private Info info;
	private HttpClient client;
	private CookieStore cookies;
	HttpClientContext context;
	
	public Network(Info info) {
		
		this.info = info;
		Builder reqConfigBuilder = RequestConfig.custom()
				.setSocketTimeout(0x7530)
				.setConnectTimeout(0x7530);

		//set proxy
		if(!info.proxyHost.isEmpty()) {
			reqConfigBuilder.setProxy(
					new HttpHost(
							info.proxyHost, 
							new Integer(info.proxyPort)));
		}
		
		//cookies store
		cookies = new BasicCookieStore();
		
		//要同步, ua也不要变, 这里不是仅 cookie登录
		if(!info.cookieS.isEmpty()) {
			BasicClientCookie cookieS = new BasicClientCookie("S", info.cookieS);
			cookieS.setDomain(SERVER_HOST);
			cookieS.setPath("/");
			cookies.addCookie(cookieS);
		}
		//http client
		client = HttpClients.custom()
				.setDefaultCookieStore(cookies)
				.setDefaultRequestConfig(reqConfigBuilder.build())
				.build();
		
		//context
		context = HttpClientContext.create();
		context.setCredentialsProvider(new BasicCredentialsProvider());
	}
	
	private List<NameValuePair> RequestProcess(List<NameValuePair> source, boolean UseDefaultKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		ArrayList<NameValuePair> result = new ArrayList<NameValuePair>();
		Iterator<NameValuePair> i  = source.iterator();
		while(i.hasNext()) {
			NameValuePair n = i.next();
			if (UseDefaultKey) {
				result.add(new BasicNameValuePair(n.getName(),Crypto.Encrypt2Base64NoKey(n.getValue())));
			} else {
				result.add(new BasicNameValuePair(n.getName(),Crypto.Encrypt2Base64WithKey(n.getValue(), info)));
			}	
		}
		return result;
	}
	
	public byte[] ConnectToServer(String url, List<NameValuePair> content, boolean UseDefaultKey) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClientProtocolException, IOException {
		
		List<NameValuePair> post = RequestProcess(content,UseDefaultKey);
		
		HttpPost hp = new HttpPost(url);
		hp.setHeader("User-Agent", info.UserAgent);
		hp.setHeader("Accept-Encoding", "gzip, deflate");
		hp.setEntity(new UrlEncodedFormEntity(post,"UTF-8"));
		
		context.getCredentialsProvider()
		.setCredentials(
				new AuthScope(hp.getURI().getHost(),hp.getURI().getPort()),
				new UsernamePasswordCredentials(Auth, Key));
	
		byte[] b = client.execute(hp, new HttpResponseHandler(), context);

		/* end */
		if (b != null) {
			if (url.contains("gp_verify_receipt?")) {
				// need to be decoded
				return null;
			}
			try {
				if (UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b, info);
				}
			} catch (Exception ex) {
				if (!UseDefaultKey) {
					return Crypto.DecryptNoKey(b);
				} else {
					return Crypto.DecryptWithKey(b, info);
				}
			}
		} 
		return null;
	}
	
	public String getCurrentCookieS() {
		Cookie cookie = cookies.getCookies().get(0);//
		return cookie.getValue();
	}
}
