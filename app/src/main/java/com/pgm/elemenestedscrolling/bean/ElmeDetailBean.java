package com.pgm.elemenestedscrolling.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * @author: pengguanming
 * @data: 2020-01-19
 * @function:
 */
public class ElmeDetailBean implements MultiItemEntity {
    public static final int FIRST_TITLE=0;
    public static final int TITLE=1;
    public static final int COUPON=2;
    public static final int PUBLISH=3;
    public static final int VIP=4;

    private int type;

    private String title;
    private String couponTitle;
    private String couponContent;
    private String publishContent;

    public ElmeDetailBean(int type) {
        this.type = type;
    }

    public ElmeDetailBean(String publishContent) {
        this.type=PUBLISH;
        this.publishContent = publishContent;
    }

    public ElmeDetailBean(int type, String title) {
        this.type = type;
        this.title=title;
    }


    public ElmeDetailBean(String couponTitle, String couponContent) {
        this.type = COUPON;
        this.couponTitle = couponTitle;
        this.couponContent = couponContent;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCouponTitle() {
        return couponTitle;
    }

    public void setCouponTitle(String couponTitle) {
        this.couponTitle = couponTitle;
    }

    public String getCouponContent() {
        return couponContent;
    }

    public void setCouponContent(String couponContent) {
        this.couponContent = couponContent;
    }

    public String getPublishContent() {
        return publishContent;
    }

    public void setPublishContent(String publishContent) {
        this.publishContent = publishContent;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
