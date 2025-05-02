package com.example.part35teammonew.domain.article.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class NewsSearch {

  public String getNews(String keyword, int display, int start, String sort){
    String clientId = "8yKaUv3DIR5509bgvnvH"; //애플리케이션 클라이언트 아이디
    String clientSecret = "0LX6uznk7j"; //애플리케이션 클라이언트 시크릿
    StringBuilder apiURL = new StringBuilder();
    apiURL.append("https://openapi.naver.com/v1/search/news.json?query=");
    if( keyword != null || display > 0 || start > 0 || sort != null ){
      try {
        String text = URLEncoder.encode(keyword, "UTF-8");
        apiURL.append(text);
        apiURL.append("&display=");
        apiURL.append(display);
        apiURL.append("&start=");
        apiURL.append(start);
        apiURL.append("&sort=");
        apiURL.append(sort);

      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException("검색어 인코딩 실패",e);
      }
    }
    Map<String, String> requestHeaders = new HashMap<>();
    requestHeaders.put("X-Naver-Client-Id", clientId);
    requestHeaders.put("X-Naver-Client-Secret", clientSecret);
    String responseBody = get(apiURL.toString(),requestHeaders);
    System.out.println("apiURL = " + apiURL);
    System.out.println(responseBody);
    return responseBody;
  }

  private static String get(String apiUrl, Map<String, String> requestHeaders){
    HttpURLConnection con = connect(apiUrl);
    try {
      con.setRequestMethod("GET");
      for(Map.Entry<String, String> header :requestHeaders.entrySet()) {
        con.setRequestProperty(header.getKey(), header.getValue());
      }


      int responseCode = con.getResponseCode();
      System.out.println("responseCode = " + responseCode);
      if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
        return readBody(con.getInputStream());
      } else { // 오류 발생
        return readBody(con.getErrorStream());
      }
    } catch (IOException e) {
      throw new RuntimeException("API 요청과 응답 실패", e);
    } finally {
      con.disconnect();
    }
  }


  private static HttpURLConnection connect(String apiUrl){
    try {
      URL url = new URL(apiUrl);
      System.out.println("API URL 연결되었습니다");
      return (HttpURLConnection)url.openConnection();
    } catch (MalformedURLException e) {
      throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
    } catch (IOException e) {
      throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
    }
  }


  private static String readBody(InputStream body){
    InputStreamReader streamReader = new InputStreamReader(body);

    try (BufferedReader lineReader = new BufferedReader(streamReader)) {
      StringBuilder responseBody = new StringBuilder();

      String line;
      while ((line = lineReader.readLine()) != null) {
        responseBody.append(line);
      }


      return responseBody.toString();
    } catch (IOException e) {
      throw new RuntimeException("API 응답을 읽는 데 실패했습니다.", e);
    }
  }

}
