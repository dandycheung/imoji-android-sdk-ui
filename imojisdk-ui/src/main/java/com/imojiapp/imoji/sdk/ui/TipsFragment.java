package com.imojiapp.imoji.sdk.ui;


import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.imojiapp.imoji.sdk.ui.view.DotDotView;


/**
 * A simple {@link } subclass.
 */
public class TipsFragment extends android.support.v4.app.Fragment {


    public static final String FRAGMENT_TAG = TipsFragment.class.getSimpleName();
    private static final String LOG_TAG = TipsFragment.class.getSimpleName();
    View mBottomLayout;
    ImageButton mAdvanceBt;
    DotDotView mDotDotView;
    private ViewPager mPager;

    public static TipsFragment newInstance() {
        TipsFragment f = new TipsFragment();

        Bundle args = new Bundle();
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tips, container, false);
    }

    public static class HintPageFragment extends android.support.v4.app.Fragment {


        public static final String DRAWABLE_RESOURCE_BUNDLE_ARG_KEY = "DRAWABLE_RESOURCE_BUNDLE_ARG_KEY";
        public static final String TITLE_BUNDLE_ARG_KEY = "TITLE_BUNDLE_ARG_KEY";
        public static final String DETAIL_BUNDLE_ARG_KEY = "DETAIL_BUNDLE_ARG_KEY";
        public static final String FRAGMENT_TAG = HintPageFragment.class.getSimpleName();
        private static final String LOG_TAG = HintPageFragment.class.getSimpleName();
        ImageView mImageView;
        TextView mTitleTv;
        TextView mDetailTv;
        CardView mCardView;

        public static HintPageFragment newInstance(int drawableResource, int titleResourceId, int detailResourceId) {
            HintPageFragment f = new HintPageFragment();

            Bundle args = new Bundle();
            args.putInt(DRAWABLE_RESOURCE_BUNDLE_ARG_KEY, drawableResource);
            args.putInt(TITLE_BUNDLE_ARG_KEY, titleResourceId);
            args.putInt(DETAIL_BUNDLE_ARG_KEY, detailResourceId);
            f.setArguments(args);

            return f;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.fragment_hint_page, container, false);
            return v;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            mImageView = (ImageView) view.findViewById(R.id.imoji_iv_hint);
            mCardView = (CardView) view.findViewById(R.id.imoji_hint_card);
            mTitleTv = (TextView) view.findViewById(R.id.imoji_tv_title);
            mDetailTv = (TextView) view.findViewById(R.id.imoji_tv_detail);

            mImageView.setImageResource(getArguments().getInt(DRAWABLE_RESOURCE_BUNDLE_ARG_KEY));
            mTitleTv.setText(getArguments().getInt(TITLE_BUNDLE_ARG_KEY));
            mDetailTv.setText(getArguments().getInt(DETAIL_BUNDLE_ARG_KEY));
        }
    }    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mPager = (ViewPager) view.findViewById(R.id.imoji_viewpager);
        mPager.setAdapter(new HintPagerAdapter(getChildFragmentManager()));
        mBottomLayout = view.findViewById(R.id.imoji_hint_bottom_bar);
        mAdvanceBt = (ImageButton) view.findViewById(R.id.imoji_ib_tips_proceed);
        mDotDotView = (DotDotView) view.findViewById(R.id.imoji_dot_dot_view);

        mAdvanceBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int numHintCards = mPager.getAdapter().getCount();
                int currentIndex = mPager.getCurrentItem();

                if (currentIndex == numHintCards - 1) {
                    if (isResumed()) {
                        getActivity().getSupportFragmentManager().popBackStack();
                    }
                    return;
                }

                mPager.setCurrentItem((numHintCards - 1 > currentIndex) ? ++currentIndex : currentIndex, true);

            }
        });

        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mDotDotView != null) {
                    mDotDotView.setIndex(position);
                }

                if (position == mPager.getAdapter().getCount() - 1) {
                    mAdvanceBt.setImageResource(R.drawable.create_tips_done);
                } else {
                    mAdvanceBt.setImageResource(R.drawable.create_tips_proceed);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private class HintPagerAdapter extends FragmentPagerAdapter {

        public HintPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            int drawableResourceId = 0;
            int titleResId = 0;
            int detailResId = 0;
            switch (position) {
                case 0:
                    drawableResourceId = R.drawable.create_tips_1;
                    titleResId = R.string.imoji_hint_title_trace;
                    detailResId = R.string.imoji_hint_detail_trace;
                    break;
                case 1:
                    drawableResourceId = R.drawable.create_tips_2;
                    titleResId = R.string.imoji_hint_title_push;
                    detailResId = R.string.imoji_hint_detail_push;
                    break;
                case 2:
                    drawableResourceId = R.drawable.create_tips_3;
                    titleResId = R.string.imoji_hint_title_zoom;
                    detailResId = R.string.imoji_hint_detail_zoom;
                    break;
            }
            return HintPageFragment.newInstance(drawableResourceId, titleResId, detailResId);
        }

        @Override
        public int getCount() {
            return 3;
        }
    }


}
