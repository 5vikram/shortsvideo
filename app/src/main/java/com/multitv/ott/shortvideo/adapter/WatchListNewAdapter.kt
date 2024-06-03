package com.multitv.ott.shortvideo.adapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.multitv.ott.shortvideo.fragment.EmailFreagment
import com.multitv.ott.shortvideo.fragment.MobileFragment

class WatchListNewAdapter(
    fragmentManager: FragmentManager,
    private var watchList: ArrayList<String> = ArrayList<String>()
) : FragmentPagerAdapter(fragmentManager) {


    override fun getCount(): Int {
        return watchList.size
    }

    override fun getItem(position: Int): Fragment {


        when (position) {
            0 ->  return MobileFragment.newInstance()
            1 ->  return EmailFreagment.newInstance()
            else -> { // Note the block
                return MobileFragment.newInstance()
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
      //  watchList.get(position).name
        return watchList.get(position)
    }
}