package com.vrv.framework.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Service layer
 * 
 * @author Xun Dai xun.dai@renren-inc.com
 * 
 */
public class Service {

    /**
     * service id
     */
    private String id;

    /**
     * service的版本
     */
    private String version;
    
    /**
     * Service shard map，key为shard的值（目前使用Integer），value为Shard对象。
     */
    private Map<Integer, Shard> shards = new HashMap<Integer, Shard>();
    /*private List<Shard> shards = new ArrayList<Shard>();*/ 

    /**
     * 报警短信，考虑去掉，统一防盗XCS的Configuration对象。
     */
    private Set<String> alarmPhones = new HashSet<String>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Map<Integer, Shard> getShards() {
        return shards;
    }

    public void setShards(Map<Integer, Shard> shards) {
        this.shards = shards;
    }
    
    public Set<String> getAlarmPhones() {
        return alarmPhones;
    }
    
    /*public List<Shard> getShards() {
        return shards;
    }

    public void setShards(List<Shard> shards) {
        this.shards = shards;
    }*/

    public void setAlarmPhones(Set<String> alarmPhones) {
        this.alarmPhones = alarmPhones;
    }

    public Service(String id, String version, Map<Integer, Shard> shards /*List<Shard> shards*/,
            Collection<String> alarmPhones) {
        super();
        this.id = id;
        this.version = version;
        if (shards != null && shards.size() > 0) {
            this.shards.putAll(shards);
            /*this.shards.addAll(shards);*/
        }
        if (alarmPhones != null && alarmPhones.size() > 0) {
            this.alarmPhones.addAll(alarmPhones);
        }
    }

    @Override
    public String toString() {
        return "Service [id=" + id + ", version=" + version + ", shards=" + shards.values()
                + ", alarmPhones=" + alarmPhones + "]";
    }

}
