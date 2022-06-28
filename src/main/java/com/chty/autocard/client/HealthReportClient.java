package com.chty.autocard.client;

import com.alibaba.fastjson.JSONObject;
import com.chty.autocard.utils.LoggerUtils;
import lombok.Getter;
import lombok.Setter;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HealthReportClient{
    
    @Value("${app.health-report-client.report-url}")
    private String reportURL;

    @Value("${app.health-report-client.submit-url}")
    private String submitURL;

    @Value("${app.health-report-client.code-url}")
    private String codeURL;
    
    @Getter @Setter
    private HttpClient httpClient;
    
    static {
        //System.load(new File("src/main/resources/opencv_java340-x64.dll").getAbsolutePath());
        //System.load(new File("src/main/resources/libopencv_java320.so").getAbsolutePath());
    }
    
    public void filterImage(File imageFile) {
        Mat src = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
        Mat dst = new Mat(src.width(), src.height(), CvType.CV_8UC1);
        
        Imgproc.boxFilter(src, dst, src.depth(), new Size(3.2, 3.2));
        Imgcodecs.imwrite(imageFile.getAbsolutePath(), dst);

        // 图片阈值处理，二值化
        Mat src1 = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
        Mat dst1 = new Mat(src1.width(), src1.height(), CvType.CV_8UC1);

        Imgproc.threshold(src1, dst1, 165, 200, Imgproc.THRESH_TRUNC);
        Imgcodecs.imwrite(imageFile.getAbsolutePath(), dst1);

        // 图片截取
        Mat src2 = Imgcodecs.imread(imageFile.getAbsolutePath(), Imgcodecs.IMREAD_UNCHANGED);
        Rect roi = new Rect(4, 2, src2.cols() - 7, src2.rows() - 4); // 参数：x坐标，y坐标，截取的长度，截取的宽度
        Mat dst2 = new Mat(src2, roi);

        Imgcodecs.imwrite(imageFile.getAbsolutePath(), dst2);
    }
    
    public String getCode() throws URISyntaxException, IOException, TesseractException {
        CloseableHttpResponse response = httpClient.doGet(codeURL);
        HttpEntity entity = response.getEntity();
        String imageFilePath = "src/main/resources/code_" + (new Date()).getTime() + ".png";
        File imageFile = new File(imageFilePath);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        entity.writeTo(outputStream);
        
        filterImage(imageFile);

        ITesseract iTesseract = new Tesseract();
        iTesseract.setDatapath("src/main/resources/tessdata");
        iTesseract.setLanguage("eng");
        String result = iTesseract.doOCR(imageFile).replaceAll("\\s", "");
        
        outputStream.close();
        imageFile.delete();
        
        LoggerUtils.info("验证码识别结果：" + result);
        return result;
    }
    
    public List<NameValuePair> getInfo() throws URISyntaxException, IOException, TesseractException {
        CloseableHttpResponse response = httpClient.doGet(reportURL);
        String content = EntityUtils.toString(httpClient.getResponseContent(response));
        
        List<NameValuePair> infoList = new ArrayList<>();

        Pattern pattern = Pattern.compile("var def = (\\{.+?\\});", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject defJson = JSONObject.parseObject(matcher.group(1));
        
        pattern = Pattern.compile("\\$\\.extend\\((\\{.+?\\}), def, (\\{.+?\\})\\)", Pattern.DOTALL);
        matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject infoJson1 = JSONObject.parseObject(matcher.group(1));
        JSONObject infoJson2 = JSONObject.parseObject(matcher.group(2));
        
        pattern = Pattern.compile("oldInfo: (\\{.+?\\}),\n");
        matcher = pattern.matcher(content);
        if(!matcher.find())  return null;
        JSONObject oldInfoJson = JSONObject.parseObject(matcher.group(1));
        
        JSONObject infoJsonAll = new JSONObject();
        infoJsonAll.putAll(defJson);
        infoJsonAll.putAll(infoJson1);
        infoJsonAll.putAll(infoJson2);
        infoJsonAll.putAll(oldInfoJson);
        
        //String verifyCode = getCode();
        String verifyCode = "";

        infoJsonAll.forEach((String name, Object value) -> {
            switch (name) {
                case "date":
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
                    value = simpleDateFormat.format(new Date());
                    break;
                case "bztcyy":
                    value = "";
                    break;
                case "verifyCode":
                    value = verifyCode;
                    break;
            }
            if(name.equals("jrdqtlqk") && value.equals(""))  return;
            if(value == null || value.toString().equals("[]"))  return;
            infoList.add(new BasicNameValuePair(name, String.valueOf(value)));
        });
        
        return infoList;
    }
    
    public Pair<Integer,String> submit() throws URISyntaxException, IOException, TesseractException {
        List<NameValuePair> infoList = getInfo();
        if(infoList == null)  {
            return Pair.of(-1, "打卡信息获取失败");
        }
        
        String area = "未知";
        for(NameValuePair info: infoList) {
            if(info.getName().equals("area")) {
                area = info.getValue();
                break;
            }
        }
        
        CloseableHttpResponse response = httpClient.doPost(submitURL, infoList);
        JSONObject responseJson = JSONObject.parseObject(EntityUtils.toString(httpClient.getResponseContent(response)));
        
        return Pair.of(responseJson.getIntValue("e"), responseJson.getString("m") + "(打卡地点: " + area + ")");
    }
    
}
