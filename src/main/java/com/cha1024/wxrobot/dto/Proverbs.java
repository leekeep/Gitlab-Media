package com.cha1024.wxrobot.dto;

import java.io.Serializable;

/**
 * 网站箴言对象 proverbs
 * 
 * @author nicolas
 * @date 2020-08-20
 */
public class Proverbs implements Serializable{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 类型 */
    private String category;

    /** 内容 */
    private String content;

    /** 关键字 */
    private String keywords;

    /** 数据状态 */
    private Integer dataState;

    /** 数据来源 */
    private String dataSource;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getKeywords() {
        return keywords;
    }
    public void setDataState(Integer dataState) {
        this.dataState = dataState;
    }

    public Integer getDataState() {
        return dataState;
    }
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getDataSource() {
        return dataSource;
    }
}