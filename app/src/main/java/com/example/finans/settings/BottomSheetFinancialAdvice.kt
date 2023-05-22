package com.example.finans.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.airbnb.lottie.LottieAnimationView
import com.example.finans.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File


class BottomSheetFinancialAdvice : BottomSheetDialogFragment() {

    private lateinit var layoutDots: LinearLayout
    private lateinit var next: TextView
    private lateinit var preview: TextView
    private lateinit var pageDataList: ArrayList<PageData>
    private lateinit var sharedPreferencesLanguage: SharedPreferences
    private lateinit var sharedPreferences : SharedPreferences
    private var switchState : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_financial_advice, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_financial_advice, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.financialAdviceExit).setOnClickListener {
            dismiss()
        }

        val viewPager = view.findViewById<ViewPager>(R.id.viewPager)
        layoutDots = view.findViewById(R.id.layoutDots)

        next = view.findViewById(R.id.introNext)
        preview = view.findViewById(R.id.introPreview)
        preview.visibility = View.GONE

        sharedPreferencesLanguage = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        pageDataList = ArrayList()
        val adapter = CustomPagerAdapter(pageDataList)

        FirebaseFirestore.getInstance().collection("recommendations").get()
            .addOnSuccessListener { result ->


                for (document in result) {
                    val headingRus = document.getString("headingRus") ?: ""
                    val headingEng = document.getString("headingEng") ?: ""
                    val descriptionRus = document.getString("descriptionRus") ?: ""
                    val descriptionEng = document.getString("descriptionEng") ?: ""
                    val image = document.getString("image") ?: ""

                    val pageData =
                        PageData(headingRus, headingEng, descriptionRus, descriptionEng, image)
                    pageDataList.add(pageData)
                }

                viewPager.adapter = adapter

                viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        if(position > 0) {
                            preview.visibility = View.VISIBLE
                        }
                        else {
                            preview.visibility = View.GONE
                        }

                        if(position == pageDataList.size-1){
                            next.visibility = View.GONE
                        }
                        else{
                            next.visibility = View.VISIBLE
                        }
                    }

                    override fun onPageSelected(position: Int) {
                        addBottomDots(position)
                    }

                    override fun onPageScrollStateChanged(state: Int) {
                    }
                })

                addBottomDots(0)
            }
            .addOnFailureListener { exception -> }


        next.setOnClickListener {
            val current = viewPager.currentItem
            if (current < adapter.count - 1) {
                viewPager.setCurrentItem(current + 1, true)
            }
        }
        preview.setOnClickListener {
            val current = viewPager.currentItem
            if (current > 0) {
                viewPager.setCurrentItem(current - 1, true)
            }
        }

    }

    inner class CustomPagerAdapter(private val pageDataList: List<PageData>) : PagerAdapter() {

        override fun getCount(): Int {
            return pageDataList.size
        }

        @SuppressLint("SetTextI18n")
        override fun instantiateItem(container: ViewGroup, position: Int): Any {

            val view: View = if(switchState){
                LayoutInflater.from(container.context).inflate(R.layout.intro_dark, container, false)
            } else{
                LayoutInflater.from(container.context).inflate(R.layout.intro, container, false)
            }

            val headingTextView = view.findViewById<TextView>(R.id.introHeadingTxt)
            val descriptionTextView = view.findViewById<TextView>(R.id.introDescriptionTxt)
            val pageImagesImageView = view.findViewById<LottieAnimationView>(R.id.introImg)

            val sharedPref =  sharedPreferencesLanguage.getString("locale", "")
            if (sharedPref == "ru"){
                headingTextView.text = "    " + pageDataList[position].headingRus
                descriptionTextView.text = pageDataList[position].descriptionRus
            } else {
                headingTextView.text = "    " + pageDataList[position].headingEng
                descriptionTextView.text = pageDataList[position].descriptionEng
            }

            pageImagesImageView.setAnimationFromUrl(pageDataList[position].image)
            pageImagesImageView.playAnimation()



            container.addView(view)

            return view
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            container.removeView(obj as View)
        }

        override fun isViewFromObject(view: View, obj: Any): Boolean {
            return view == obj
        }
    }

    private fun addBottomDots(currentPage: Int) {
        layoutDots.removeAllViews()

        val dots = Array(pageDataList.size) { TextView(requireContext()) }

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val margin = resources.getDimensionPixelSize(R.dimen.dot_margin)
        params.setMargins(margin, 0, margin, 0)

        for (i in dots.indices) {
            dots[i] = TextView(requireContext())
            dots[i].text = Html.fromHtml("&#8226;")
            dots[i].textSize = 50f
            dots[i].setTextColor(
                if (i == currentPage) {
                    ContextCompat.getColor(requireContext(), R.color.active_dot_color)
                } else {
                    ContextCompat.getColor(requireContext(), R.color.inactive_dot_color)
                }
            )
            layoutDots.addView(dots[i], params)
        }
    }
}
