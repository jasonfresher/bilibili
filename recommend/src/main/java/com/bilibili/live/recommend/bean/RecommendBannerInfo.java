package com.bilibili.live.recommend.bean;

import com.bilibili.live.recommend.entity.RecommendEntity;

import java.util.List;

/**
 * <p>
 * 首页Banner推荐
 */

public class RecommendBannerInfo extends RecommendEntity{

  private int code;

  private List<DataBean> data;


  public int getCode() {

    return code;
  }


  public void setCode(int code) {

    this.code = code;
  }


  public List<DataBean> getData() {

    return data;
  }


  public void setData(List<DataBean> data) {

    this.data = data;
  }

  @Override
  public int getItemType() {
    return VIEW_TYPE_BANNER;
  }


  public static class DataBean{

    private String title;

    private String value;

    private String image;

    private int type;

    private int weight;

    private String remark;

    private String hash;

    public DataBean() {
      type = RecommendEntity.VIEW_TYPE_BANNER;
    }

    public String getTitle() {

      return title;
    }


    public void setTitle(String title) {

      this.title = title;
    }


    public String getValue() {

      return value;
    }


    public void setValue(String value) {

      this.value = value;
    }


    public String getImage() {

      return image;
    }


    public void setImage(String image) {

      this.image = image;
    }


    public int getType() {

      return type;
    }


    public void setType(int type) {

      this.type = type;
    }


    public int getWeight() {

      return weight;
    }


    public void setWeight(int weight) {

      this.weight = weight;
    }


    public String getRemark() {

      return remark;
    }


    public void setRemark(String remark) {

      this.remark = remark;
    }


    public String getHash() {

      return hash;
    }


    public void setHash(String hash) {

      this.hash = hash;
    }
  }
}
