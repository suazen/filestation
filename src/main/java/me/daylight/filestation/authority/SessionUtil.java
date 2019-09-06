package me.daylight.filestation.authority;

import me.daylight.filestation.util.Cache;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class SessionUtil {
    private static volatile SessionUtil instance;

    private static final long EXPIRE_TIME = 30*60*1000;

    private SessionUtil(){

    }

    public static SessionUtil getInstance(){
        if (null == instance)
            synchronized (SessionUtil.class){
                if (Objects.isNull(instance))
                    instance = new SessionUtil();
            }
        return instance;
    }

    private static HttpSession getHttpSession(){
        return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest().getSession();
    }

    public static void newSession(){
        String sessionId = UUID.randomUUID().toString().replace("-","").toUpperCase();
        Cache.put(sessionId,new HashMap<String,Object>(), EXPIRE_TIME);
    }

    public static <T extends Serializable> void setAttribute(String key,T value){
        Cache.get(getHttpSession().getId(),HashMap.class).put(key,value);
    }

    public static Object getAttribute(String key){
        if (Cache.hasKey(getHttpSession().getId()))
            return Cache.get(getHttpSession().getId(),HashMap.class).get(key);
        return null;
    }

    public static long getExpire(){
        return Cache.getExpire(getHttpSession().getId());
    }

    public static void setExpire(){
        Cache.setExpire(getHttpSession().getId(), EXPIRE_TIME);
    }
}
