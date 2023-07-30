package com.android.apphelper2.utils.httpclient;

import com.android.apphelper2.app.AppHelper2;
import com.android.apphelper2.utils.LogUtil;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 自定义的日志拦截器
 */
public class HttpLogInterceptor2 implements Interceptor {

    private String TAG = "HttpClient";
    // 标记状态，默认只有在debug模式下才会打印数据
    private boolean State = AppHelper2.INSTANCE.isDebug();

    @Override
    public Response intercept(Chain chain) throws IOException {
        // Chain 里包含了request和response

        if (chain != null) {
            Request.Builder builder = chain.request().newBuilder();
            // builder.addHeader("unid", "o9RWl1O-vLNqWZ9FYg7LX3QkOeKE");

            Request request = builder.build();

            if (request != null) {
                //只有在debug模式下才会去打印数据
                if (State) {
                    long t1 = System.nanoTime();//请求发起的时间

                    String parameters = getParameters(request);
                    String heads = getHeards(request);

                    Response response = chain.proceed(request);
                    long t2 = System.nanoTime();//收到响应的时间
                    //不能直接使用response.body（）.string()的方式输出日志,因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一个新的response给应用层处理
                    ResponseBody responseBody = response.peekBody(1024 * 1024);

                    LogUtil.e(TAG, String.format(Locale.CHINA,
                            "请求方式:【 %s 】" +
                                    "%n请求地址:【 %s 】" +
                                    "%n请求头  :【 %s 】" +
                                    "%n请求参数:【 %s 】" +
                                    "%n响应时间:【 %.2fms 】" +
                                    "%n返回内容:【 %s 】 ",
                            request.method(),
                            request.url(),
                            heads,
                            parameters,
                            (t2 - t1) / 1e6d,
                            responseBody.string()));

                    return response;
                } else {
                    return chain.proceed(request);
                }
            }
        }
        return null;
    }

    /**
     * @param request
     * @return 获取post请求方式下的参数信息
     */
    private String getParameters(Request request) {
        String paraments = "";
        StringBuilder sb_parameter = null;

        // 打印请求的参数
        String method = request.method();
        if ("POST".equals(method)) {

            if (request.body() instanceof FormBody) {

                FormBody body = (FormBody) request.body();

                sb_parameter = new StringBuilder();
                for (int i = 0; i < body.size(); i++) {
                    String key = null;
                    String value = null;
                    try {
                        key = URLDecoder.decode(body.encodedName(i), "UTF-8");
                        value = URLDecoder.decode(body.encodedValue(i), "UTF-8");

                        sb_parameter.append(key + " = " + value);

                        if (i != body.size() - 1) {
                            sb_parameter.append(" , ");
                        }

                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }

                if (sb_parameter != null) {
                    paraments = sb_parameter.toString();
                } else {
                    paraments = "";
                }
            }
        }
        return paraments;
    }

    /**
     * @param request
     * @return 获取请求头信息
     */
    private String getHeards(Request request) {
        String heads = "";
        StringBuilder sb_heads = null;

        Headers headers = request.headers();
        int size = headers.size();
        if (size > 0) {
            sb_heads = new StringBuilder();
            for (int i = 0; i < size; i++) {
                String key = headers.name(i);
                String value = headers.get(key);

                sb_heads.append(key + " = ");
                sb_heads.append(value + " ,");
            }
            sb_heads.delete(sb_heads.length() - 1, sb_heads.length());

            heads = sb_heads.toString();
        }

        return heads;

    }

    /**
     * 获取URL的信息，用来重庆向使用
     *
     * @param request 请求对象
     */
    private void getUrlInfo(Request request) {
        if (request != null) {
            // 获取URL的信息
            HttpUrl url = request.url();
            //http://127.0.0.1/test/upload/img?userName=xiaoming&userPassword=12345
            String scheme = url.scheme();//  http https
            String host = url.host();//   127.0.0.1
            String path = url.encodedPath();//  /test/upload/img
            String query = url.encodedQuery();//  userName=xiaoming&userPassword=12345

            LogUtil.e(String.format("协议方式：【 %s 】 %nHost：【 %s 】%nPath:【 %s 】%n完整路径：【 %s 】%n参数：【 %s 】",
                    scheme, host, path, (scheme + "://" + host + path), query));

            //重定向
            //StringBuffer sb = new StringBuffer();
            //String newUrl = sb.append(scheme).append(newHost).append(path).append("?").append(query).toString();

            //Request.Builder builder = request.newBuilder().url(newUrl);

        }
    }
}
