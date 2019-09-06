package me.daylight.filestation.util;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class Cache {
    private static Map<String,Entity> map = new HashMap<>();

    private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private Cache(){

    }

    static {
        readCache();
        executor.scheduleWithFixedDelay(Cache::writeCache,2000,2000,TimeUnit.MILLISECONDS);
    }

    public synchronized static <T extends Serializable> void put(String key,T value){
        Cache.put(key, value,0);
    }

    public synchronized static <T extends Serializable> void put(String key,T value,long expire){
        Cache.remove(key);
        if (expire > 0){
            ScheduledFuture future = executor.schedule(() -> {
                synchronized (Cache.class){
                    map.remove(key);
                }
            },expire, TimeUnit.MILLISECONDS);
            map.put(key, new Entity(value, future));
        } else {
            map.put(key,new Entity(value,null));
        }
    }

    public synchronized static Object get(String key){
        Entity entity = map.get(key);
        return entity == null? null:entity.getValue();
    }

    public synchronized static <T> T get(String key,Class<T> clazz){
        return clazz.cast(Cache.get(key));
    }

    public synchronized static Object remove(String key){
        Entity entity = map.remove(key);
        if (entity == null)
            return null;
        Future future = entity.getFuture();
        if (future != null)
            future.cancel(true);
        return entity.getValue();
    }

    public synchronized static int size(){
        return map.size();
    }

    public synchronized static long getExpire(String key){
        Entity entity = map.get(key);
        if (entity != null){
            ScheduledFuture future = entity.getFuture();
            if (future != null)
                return future.getDelay(TimeUnit.MILLISECONDS);
            return -1;
        }
        return 0;
    }

    public synchronized static void setExpire(String key,long expire){
        Entity entity = map.get(key);
        if (entity != null){
            ScheduledFuture future = entity.getFuture();
            if (future != null){
                future.cancel(true);
                if (expire > 0){
                    future = executor.schedule(()->{
                        synchronized (Cache.class){
                            map.remove(key);
                        }
                    },expire,TimeUnit.MILLISECONDS);
                    entity.setFuture(future);
                }
            }
        }
    }

    public synchronized static boolean hasKey(String key){
        return map.containsKey(key)&&map.get(key)!=null&&map.get(key).getValue()!=null;
    }

    private synchronized static void writeCache(){
        map.forEach((key,value)-> value.setExpire(value.getFuture()==null?0:value.getFuture().getDelay(TimeUnit.MILLISECONDS)));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("cache"))){
            oos.writeObject(map);
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private synchronized static void readCache(){
        if (!new File("cache").exists())
            return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("cache"))){
            map = (HashMap<String, Entity>) ois.readObject();
            map.forEach((key,value)-> {
                if (value.getExpire() > 0){
                    ScheduledFuture future = executor.schedule(() -> {
                        synchronized (Cache.class){
                            map.remove(key);
                        }
                    },value.getExpire(), TimeUnit.MILLISECONDS);
                    map.get(key).setFuture(future);
                }
            });
        }catch (IOException|ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    private static class Entity implements Serializable {
        private Object value;

        private transient ScheduledFuture future;

        private long expire;

        Entity(Object value,ScheduledFuture future){
            this.value = value;
            this.future = future;
        }

        Object getValue(){
            return value;
        }

        void setFuture(ScheduledFuture future){
            this.future = future;
        }

        ScheduledFuture getFuture(){
            return future;
        }

        void setExpire(long expire){
            this.expire = expire;
        }

        long getExpire(){
            return expire;
        }
    }

}
