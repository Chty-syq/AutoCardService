package com.chty.autocard.utils;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class StringUtils {
    
    public static String HttpEntity2String(HttpEntity httpEntity) throws IOException {
        return httpEntity == null ? null : EntityUtils.toString(httpEntity);
    }
    
}
