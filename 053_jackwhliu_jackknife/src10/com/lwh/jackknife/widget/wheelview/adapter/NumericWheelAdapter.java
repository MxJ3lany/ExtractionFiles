/*
 * Copyright (C) 2019 The JackKnife Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lwh.jackknife.widget.wheelview.adapter;

public class NumericWheelAdapter implements WheelAdapter {
	
	private int mMinValue;
	private int mMaxValue;

	public NumericWheelAdapter(int mMinValue, int mMaxValue) {
		this.mMinValue = mMinValue;
		this.mMaxValue = mMaxValue;
	}

	@Override
	public Object getItem(int index) {
		if (index >= 0 && index < getItemCount()) {
			int value = mMinValue + index;
			return value;
		}
		return 0;
	}

	@Override
	public int getItemCount() {
		return mMaxValue - mMinValue + 1;
	}
	
	@Override
	public int indexOf(Object o){
		try {
			return (int)o - mMinValue;
		} catch (Exception e) {
			return -1;
		}
	}
}
