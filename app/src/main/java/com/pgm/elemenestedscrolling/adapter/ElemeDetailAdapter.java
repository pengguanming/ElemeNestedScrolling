package com.pgm.elemenestedscrolling.adapter;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.pgm.elemenestedscrolling.R;
import com.pgm.elemenestedscrolling.bean.ElmeDetailBean;

import java.util.ArrayList;
import java.util.List;

import static com.pgm.elemenestedscrolling.bean.ElmeDetailBean.*;


/**
 * @author: pengguanming
 * @data: 2020-01-19
 * @function:
 */
public class ElemeDetailAdapter extends BaseMultiItemQuickAdapter<ElmeDetailBean, BaseViewHolder> {

    public ElemeDetailAdapter() {
        super(getList());
        addItemType(FIRST_TITLE, R.layout.item_text_first_title_layout);
        addItemType(TITLE, R.layout.item_text_title_layout);
        addItemType(COUPON, R.layout.item_text_coupon_layout);
        addItemType(PUBLISH, R.layout.item_text_publish_layout);
        addItemType(VIP, R.layout.item_vip_layout);
    }

    private static List<ElmeDetailBean> getList() {
        ArrayList<ElmeDetailBean> list = new ArrayList<>();
        list.add(new ElmeDetailBean(FIRST_TITLE,"优惠"));
        list.add(new ElmeDetailBean("特价","特价商品15.5元起"));
        list.add(new ElmeDetailBean("会员","超级会员领7元无门槛红包"));
        list.add(new ElmeDetailBean("折扣","折扣商品55着起"));
        list.add(new ElmeDetailBean("限时","限时秒杀甜品饮料商品"));
        list.add(new ElmeDetailBean(TITLE,"公告"));
        list.add(new ElmeDetailBean("春节不打烊，金喜送到家。新品金凤来福鸡排堡、金尊肉酱厚牛堡上线，配上扭扭薯条，等您品尝！"));
        list.add(new ElmeDetailBean(TITLE,"店铺会员卡"));
        list.add(new ElmeDetailBean(VIP));
        return list;
    }

    @Override
    protected void convert(BaseViewHolder helper, ElmeDetailBean item) {
        switch (item.getType()) {
            case FIRST_TITLE:
                helper.setText(R.id.tv_item_text_first_title,item.getTitle());
                helper.addOnClickListener(R.id.iv_item_text_close);
                break;
            case TITLE:
                helper.setText(R.id.tv_item_text_title,item.getTitle());
                break;
            case COUPON:
                helper.setText(R.id.tv_item_text_coupon_title,item.getCouponTitle());
                helper.setText(R.id.tv_item_text_coupon_content,item.getCouponContent());
                break;
            case PUBLISH:
                helper.setText(R.id.tv_item_text_publish,item.getPublishContent());
                break;
        }
    }

}
