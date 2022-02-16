package com.chty.autocard.utils;

import com.chty.autocard.AutocardApplication;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class AppUtils implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    public static String workDir;
    
    public static void setWorkDir() {
        workDir = AutocardApplication.class.getProtectionDomain().getCodeSource().getLocation().toString();
        workDir = workDir.replaceFirst("^[^/]*file:", "").replaceFirst("!/BOOT-INF/classes!/", "");
        workDir = new File(workDir).getParent();
    }
    
    public static String getWorkDir() {
        if(workDir == null)  setWorkDir();
        return workDir;
    }
    
    public static Object getBean(String name) {
        if(applicationContext == null)  return null;
        return applicationContext.getBean(name);
    }
    
    public static <T> T getBean(Class<T> tClass) {
        if(applicationContext == null)  return null;
        return applicationContext.getBean(tClass);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        AppUtils.applicationContext = applicationContext;
    }
}
