package com.bestgo.adsmoney.bean;


/**
 * @author mengjun
 * @date 2018/8/6 21:49
 * @desc 应用版本信息
 */
public class AppVersionNumber {
    private long id;
    private String appId;
    private String appName;
    private String createTime;
    private String versionNumber;
    private String description;

    public AppVersionNumber() {
    }

    public AppVersionNumber(long id, String appId, String appName, String createTime, String versionNumber, String description) {
        this.id = id;
        this.appId = appId;
        this.appName = appName;
        this.createTime = createTime;
        this.versionNumber = versionNumber;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(String versionNumber) {
        this.versionNumber = versionNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "AppVersionNumber{" +
                "id=" + id +
                ", appId='" + appId + '\'' +
                ", appName='" + appName + '\'' +
                ", createTime='" + createTime + '\'' +
                ", versionNumber='" + versionNumber + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
