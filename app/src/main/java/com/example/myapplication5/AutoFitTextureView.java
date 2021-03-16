/*
 * Copyright 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.myapplication5;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

/**
 * A {@link TextureView} that can be adjusted to a specified aspect ratio.
 */
public class AutoFitTextureView extends TextureView {

    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    public AutoFitTextureView(Context context) {
        this(context, null);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoFitTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be measured based on the ratio
     * calculated from the parameters. Note that the actual sizes of parameters don't matter, that
     * is, calling setAspectRatio(2, 3) and setAspectRatio(4, 6) make the same result.
     *
     * @param width  Relative horizontal size
     * @param height Relative vertical size
     */
    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        if (mRatioWidth == width && mRatioHeight == height) {
            return;
        }
        mRatioWidth = width;
        mRatioHeight = height;
        Log.e("mRatio",width+", "+height);
        requestLayout();
    }


    /**AutoFitTextureView클래스(TextureView를 상속 받음)의 오버라이드 메소드 onMeasure()에서 setMeasuredDimension()메소드로TextureView의 view가로,세로 크기가 정해짐
     이 TextureView의 크기로 확대 되어 보일수도 있고 미리보기화면이 왜곡이 일어날수도 있다. 디바이스 디스플레이 화면 가로 세로 보다 TextureView의 가로 세로 크기가
     더 크면 확대되보이는 형태가 생길것이고 가로,세로 비율이 적절하지 않으면 왜곡이 일어나 보일 수 있다.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//        int width = MeasureSpec.getSize(widthMeasureSpec);
//        int height = MeasureSpec.getSize(heightMeasureSpec);
//        if (0 == mRatioWidth || 0 == mRatioHeight) {
//            setMeasuredDimension(width, height);
//        } else {
//            if (width < height * mRatioWidth / mRatioHeight) {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
//            } else {
//                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
//            }
//        }

        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        /*width, height은 액션바를 제외한 디스플레이 크기*/

        Log.e("onMeasure width, height",width+", "+height);
        Log.e("onMeasure Ra",mRatioWidth+", "+mRatioHeight);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width <= height * mRatioWidth / mRatioHeight) {
//                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
                setMeasuredDimension(height * 70 / 100, height);    /*원래 mRatioWidth / mRatioHeight 처럼 비율을 지정했는데 나는 정적으로 70 / 100으로 숫자로 지정함.    1:1.42 비율*/
                Log.e("확인1",height * mRatioWidth / mRatioHeight+", "+height);
            } else {
//                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
                setMeasuredDimension(width, width * 70 / 100);
                Log.e("확인2",width+", "+width * mRatioHeight / mRatioWidth);
            }

        }
    }

}
