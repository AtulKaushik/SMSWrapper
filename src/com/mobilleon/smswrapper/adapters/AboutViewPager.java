package com.mobilleon.smswrapper.adapters;


import com.mobilleon.smswrapper.fragment.AboutTheAppFragment;
import com.mobilleon.smswrapper.fragment.AboutTheAuthorFragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class AboutViewPager extends FragmentPagerAdapter {
	private Context _context;
	
	public AboutViewPager(Context context, FragmentManager fm) {
		super(fm);	
		_context=context;
		
		}
	@Override
	public Fragment getItem(int position) {
		Fragment f = new Fragment();
		switch(position){
		case 0:
			f=AboutTheAppFragment.newInstance(_context);	
			break;
		case 1:
			f=AboutTheAuthorFragment.newInstance(_context);	
			break;
		}
		return f;
	}
	@Override
	public int getCount() {
		return 2;
	}

}
