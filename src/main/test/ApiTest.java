import com.como.util.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by commando on 2019/1/21.
 */
public class ApiTest {
    public static void main(String[] args) {
        String host = "http://ali-hospital.showapi.com";
        String path = "/hospitalList";
        String method = "GET";
        String appcode = "6a4a59e683a94a73b2b50b85ec7e5f14";
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("cityName", "");
        querys.put("hosName", "友好");
        querys.put("page", "1");
        querys.put("provinceName", "北京市");


        try {
            /**
             * 重要提示如下:
             * HttpUtils请从
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
             * 下载
             *
             * 相应的依赖请参照
             * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
             */
            HttpResponse response = HttpUtils.doGet(host, path, method, headers, querys);
            System.out.println(response.toString());
            //获取response的body
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void compute(){
        double d = 15.53;
        DecimalFormat df = new DecimalFormat("0");
        String result = df.format(d);
        System.out.println(result);
    }
}
