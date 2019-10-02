package cappasoftware.com.bookannotations.nctexteditor

import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.res.ResourcesCompat
import android.text.*
import android.text.style.*
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import cappasoftware.com.bookannotations.R
import kotlinx.android.synthetic.main.customview_nctexteditor.view.*
import java.lang.reflect.Type


/**
 * Created By Nicolas Capparelli on 7/10/2018
 * Special thanks to this tutorial: https://medium.com/@elye.project/building-custom-component-with-kotlin-fc082678b080
 * Inspired by: https://stackoverflow.com/a/43244799
 */

class NCTextEditor @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyle: Int = 0,
        defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyle, defStyleRes), AdapterView.OnItemSelectedListener {

    // TODO: Comment all of this
    // TODO: Figure out way of moving over bullet points
    // TODO: Implement title in font options using RelativeSizeSpan
    // TODO: Add new fonts using TypeFaceSpan
    // TODO: Remove my full name from the header to prevent doxxing
    // TODO: Add functionality to all of Font Ribbon

    private var lastCursorPos = 0
    private var spanStartedAt = 0

    // Font Stuff
    private var currentFont: String = "Roboto"
    private val fontMap: Map<String, Int> = mapOf(
            "Roboto" to 0,
            "Calibri" to R.font.calibri,
            "Times New Roman" to R.font.times_new_roman,
            "Helvetica" to R.font.helvetica
    )

    // Ribbon Options
    var isBoldText = false
    var isItalicText = false
    var isUnderlineText = false
    var isMonospace = false
    var isBulletPoint = false
    var isNumberList = false
    var isSpecialFont = false
    var isTitle = false

    // Span Created

    private var spanCreatedMap: MutableMap<String, Boolean> = mutableMapOf(
            "isNormalSpanCreated" to false,
            "isBoldSpanCreated" to false,
            "isItalicSpanCreated" to false,
            "isUnderlineSpanCreated" to false,
            "isMonospaceSpanCreated" to false,
            "isDoubleMultiplierSpanCreated" to false,
            "isTripleMultiplierSpanCreated" to false,
            "isSpecialFontSpanCreated" to false,
            "isTitleSpanCreated" to false,
            "isFontSingleMultiplierSpanCreated" to false,
            "isFontDoubleMultiplierSpanCreated" to false,
            "isFontTripleMultiplierSpanCreated" to false
    )


    // Keeps track of the Number List items
    var lastNumberListValue = 1

    // "Constructor"
    init {

        // Inflating view and setting to LL to vertical
        LayoutInflater.from(context).inflate(R.layout.customview_nctexteditor, this, true)

        // Grab Custom Attributes from XML
        attrs?.let {

            // The set of attributes retrieved from the attribute XML file
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NCTextEditor, 0, 0)

            val viewHeight = resources.getDimensionPixelSize(typedArray.getResourceId(R.styleable.NCTextEditor_height,
                    R.dimen.ncedittext_default_height))

            val ribbonHeight = resources.getDimensionPixelSize(typedArray.getResourceId(R.styleable.NCTextEditor_ribbon_height,
                            R.dimen.ncedittext_default_ribbon_height))

            val etHint = resources.getString(typedArray.getResourceId(R.styleable.NCTextEditor_et_hint,
                    R.string.ncte_default_hint))


            rl_viewLayout.layoutParams = LayoutParams(MATCH_PARENT, viewHeight)
            sv_ribbonWrapper.layoutParams = LayoutParams(MATCH_PARENT, ribbonHeight)
            et_userInput.hint = etHint

            typedArray.recycle()
        }

        initMainRibbon()
        initTextWatchListener()
        initFontRibbon()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

        currentFont = sr_fontPicker.selectedItem.toString()

        if (currentFont != "Roboto" ) {
            isSpecialFont = !isSpecialFont
            setSpanCreatedFalse("isSpecialFontSpanCreated")
        } else {
            setSpanCreatedFalse("")
            isSpecialFont = false
        }

    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
        return
    }

    // Everything to do with Main Ribbon
    private fun initMainRibbon() {

        /** Main Ribbon Button Listeners
         * These all perform the same tasks for their respective formatting:
         * switch their is___Text boolean, change the color of the button, and set all other _SpanCreated Booleans to false
         * Below is a listener expanded so that the reader can see how they work. The rest are minified for readability purposes
         */

        btn_formatBold.setOnClickListener {

            isBoldText = !isBoldText

            if (isBoldText){
                btn_formatBold.setImageDrawable(context.getDrawable(R.drawable.ic_format_bold_green_24dp))

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    et_userInput.text.setSpan(StyleSpan(Typeface.BOLD), et_userInput.selectionStart, et_userInput.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
                setSpanCreatedFalse("isBoldSpanCreated")
            } else {
                btn_formatBold.setImageDrawable(context.getDrawable(R.drawable.ic_format_bold_white_24dp))
                setSpanCreatedFalse("")

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    val spanArray = et_userInput.text.getSpans(et_userInput.selectionStart, et_userInput.selectionEnd, StyleSpan::class.java)
                    et_userInput.text.removeSpan(spanArray[spanArray.size - 1])
                } else {
                    endSpan(StyleSpan(Typeface.BOLD))
                }
            }
        }
        btn_formatItalics.setOnClickListener {
            isItalicText = !isItalicText
            if (isItalicText){
                btn_formatItalics.setImageDrawable(context.getDrawable(R.drawable.ic_format_italic_green_24dp)); setSpanCreatedFalse("isItalicSpanCreated")

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    et_userInput.text.setSpan(StyleSpan(Typeface.ITALIC), et_userInput.selectionStart, et_userInput.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

            } else {

                btn_formatItalics.setImageDrawable(context.getDrawable(R.drawable.ic_format_italic_white_24dp))
                setSpanCreatedFalse("")

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0 && !isItalicText) {
                    val spanArray = et_userInput.text.getSpans(et_userInput.selectionStart, et_userInput.selectionEnd, StyleSpan::class.java)
                    et_userInput.text.removeSpan(spanArray[spanArray.size - 1])
                }

                else {
                    endSpan(StyleSpan(Typeface.ITALIC))
                }

            }
        }  // TODO: Handle Bold Italic
        btn_formatUnderline.setOnClickListener {
            isUnderlineText = !isUnderlineText
            if (isUnderlineText){
                btn_formatUnderline.setImageDrawable(context.getDrawable(R.drawable.ic_format_underlined_green_24dp))

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    et_userInput.text.setSpan(UnderlineSpan(), et_userInput.selectionStart, et_userInput.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                setSpanCreatedFalse("isUnderlineSpanCreated")

            } else {
                btn_formatUnderline.setImageDrawable(context.getDrawable(R.drawable.ic_format_underlined_white_24dp))
                setSpanCreatedFalse("")

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    val spanArray = et_userInput.text.getSpans(et_userInput.selectionStart, et_userInput.selectionEnd, UnderlineSpan::class.java)
                    et_userInput.text.removeSpan(spanArray[spanArray.size - 1])
                } else {
                    endSpan(UnderlineSpan())
                }


            }
        }
        btn_formatMonospace.setOnClickListener {
            isMonospace = !isMonospace

            if (isMonospace){
                btn_formatMonospace.setImageDrawable(context.getDrawable(R.drawable.ic_code_green_24dp))

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    et_userInput.text.setSpan(UnderlineSpan(), et_userInput.selectionStart, et_userInput.selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                }

                setSpanCreatedFalse("isMonospaceSpanCreated")

            } else {
                btn_formatMonospace.setImageDrawable(context.getDrawable(R.drawable.ic_code_white_24dp))
                setSpanCreatedFalse("")

                if (et_userInput.selectionEnd - et_userInput.selectionStart > 0) {
                    val spanArray = et_userInput.text.getSpans(et_userInput.selectionStart, et_userInput.selectionEnd, UnderlineSpan::class.java)
                    et_userInput.text.removeSpan(spanArray[spanArray.size - 1])
                } else {
                    endSpan(TypefaceSpan("monospace"))
                }
            }
        }

        // The formatBullet and formatNumberList work differently than the other ribbon buttons. The rest of this function is dedicated solely to them
        btn_formatBullet.setOnClickListener {
            isBulletPoint = !isBulletPoint

            // Prevents the adding of another bullet point when the user presses the button to turn bullet list off.
            if (isBulletPoint){
                btn_formatBullet.setImageDrawable(context.getDrawable(R.drawable.ic_format_list_bulleted_green_24dp))
                et_userInput.append(System.getProperty("line.separator") + "    " + "•  ")
            }

            // If the user is ending the bullet list
            else {
                btn_formatBullet.setImageDrawable(context.getDrawable(R.drawable.ic_format_list_bulleted_white_24dp))
                et_userInput.append(System.getProperty("line.separator"))
            }
        }

        btn_formatNumberList.setOnClickListener {
            isNumberList = !isNumberList

            // If the user is starting a new number list
            if (isNumberList) {
                btn_formatNumberList.setImageDrawable(context.getDrawable(R.drawable.ic_format_list_numbered_green_24dp))
                lastNumberListValue = 1
                et_userInput.append(System.getProperty("line.separator") + "    " + lastNumberListValue.toString() + ".  ")
            }

            // If the user is ending it
            else {
                btn_formatNumberList.setImageDrawable(context.getDrawable(R.drawable.ic_format_list_numbered_white_24dp))
                et_userInput.append(System.getProperty("line.separator"))
            }
        }

        // Used for Bullet List and Numbered List
        et_userInput.setOnKeyListener(object : OnKeyListener {
            override fun onKey(p0: View?, keyCode: Int, event: KeyEvent?): Boolean {

                if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    when {
                        isBulletPoint -> et_userInput.append(System.getProperty("line.separator") + "    " + "•  ")
                        isNumberList -> {
                            lastNumberListValue++
                            et_userInput.append(System.getProperty("line.separator") + "    " + lastNumberListValue.toString() + ".  ")
                        }
                        else -> return false
                    }
                }
                return true
            }
        })
    }

    // Handles most edits
    private fun initTextWatchListener() {

        val str: Spannable = et_userInput.text


        // Used for Bold, Italic, Underline, and Monospace
        et_userInput.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {

                lastCursorPos = start

                // Skip all of the following code if the character inputted by the user is a space or backspace (if it's a backspace, p0!![start] will throw an IndexOutOfBoundsException)
                try { if (p0!![start].toString().equals(" ")) return } catch (e: IndexOutOfBoundsException) {return}

                when {

                    isSpecialFont && isBoldText && isItalicText && !spanCreatedMap["isFontDoubleMultiplierSpanCreated"]!! -> {

                        spanCreatedMap["isFontDoubleMultiplierSpanCreated"] = true
                        str.setSpan(CustomTypefaceSpan(ResourcesCompat.getFont(context, fontMap[currentFont]!!)!!), start, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        str.setSpan(CharacterStyle.wrap(StyleSpan(Typeface.BOLD_ITALIC)), start, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        return
                    }

                    isSpecialFont && isBoldText && !spanCreatedMap["isFontSingleMultiplierSpanCreated"]!! ->  {

                        spanCreatedMap["isFontSingleMultiplierSpanCreated"] = true
                        str.setSpan(CustomTypefaceSpan(ResourcesCompat.getFont(context, fontMap[currentFont]!!)!!), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        str.setSpan(CharacterStyle.wrap(StyleSpan(Typeface.BOLD)), start, str.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        return
                    }

                    isItalicText && isBoldText && !spanCreatedMap["isDoubleMultiplierSpanCreated"]!!-> {
                        spanCreatedMap["isDoubleMultiplierSpanCreated"] = true
                        str.setSpan(CharacterStyle.wrap(StyleSpan(Typeface.BOLD_ITALIC)), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }


                    isBoldText && !spanCreatedMap["isBoldSpanCreated"]!! -> {
                        spanCreatedMap["isBoldSpanCreated"] = true
                        str.setSpan(StyleSpan(Typeface.BOLD), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                    }

                    isItalicText && !spanCreatedMap["isItalicSpanCreated"]!! -> {
                        spanCreatedMap["isItalicSpanCreated"] = true
                        str.setSpan(StyleSpan(Typeface.ITALIC), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }

                    isMonospace && !spanCreatedMap["isMonospaceSpanCreated"]!! -> {
                        spanCreatedMap["isMonospaceSpanCreated"] = true
                        str.setSpan(TypefaceSpan("monospace"), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }

                    isUnderlineText && !spanCreatedMap["isUnderlineSpanCreated"]!! -> {
                        spanCreatedMap["isUnderlineSpanCreated"] = true
                        str.setSpan(UnderlineSpan(), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }

                    isTitle && !spanCreatedMap["isTitleSpanCreated"]!! -> {
                        spanCreatedMap["isTitleSpanCreated"] = true
                        str.setSpan(AbsoluteSizeSpan(26, true), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }

                    isSpecialFont && !spanCreatedMap["isSpecialFontSpanCreated"]!!-> {
                        spanCreatedMap["isSpecialFontSpanCreated"] = true
                        str.setSpan(CustomTypefaceSpan(ResourcesCompat.getFont(context, fontMap[currentFont]!!)!!), start, str.length, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                        spanStartedAt = start
                        return
                    }

                    else -> {

                        if (!spanCreatedMap["isNormalSpanCreated"]!!) {
                            spanCreatedMap["isNormalSpanCreated"] = true
                            spanStartedAt = start
                        }
                        return
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
                return
            }
            override fun afterTextChanged(s: Editable?) {
                return
            }
        })

    }

    // Everything to do with Font Ribbon
    private fun initFontRibbon() {

        // Buttons to navigate from main ribbon to font ribbon
        btn_formatFontOptions.setOnClickListener {llt_fontRibbon.visibility = View.VISIBLE}
        btn_returnToMainRibbon.setOnClickListener {llt_fontRibbon.visibility = View.GONE}


        btn_formatFontTitle.setOnClickListener {
            isTitle = !isTitle

            if (isTitle) {
                btn_formatFontTitle.setImageDrawable(context.getDrawable(R.drawable.ic_title_green_24dp))
                setSpanCreatedFalse("isTitleSpanCreated")
            } else {
                btn_formatFontTitle.setImageDrawable(context.getDrawable(R.drawable.ic_title_white_24dp))
                setSpanCreatedFalse("")
            }
        }

        // Setup for spinner (Font Picker)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(context, R.layout.custom_spinner, fontMap.keys.toList())
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sr_fontPicker.adapter = arrayAdapter

        // This class implements OnItemSelectListener, so the spinner's listener is set to this class
        sr_fontPicker.onItemSelectedListener = this
    }

    // Sets all __SpanCreated booleans to false except the one passed in
    private fun setSpanCreatedFalse(exception: String) {

        for (key in spanCreatedMap.keys) {
            if (key != exception)
                spanCreatedMap[key] = false
        }
    }

    // Ends a span by switching it from SPAN_EXCLUSIVE_INCLUSIVE to SPAN_EXCLUSIVE_EXCLUSIVE
    private fun endSpan(what: Any) {

        // Grabbing the span from the EditText and getting the position where it starts and ends
        val spanArray = et_userInput.text.getSpans(spanStartedAt, lastCursorPos, what::class.java)
        val spanStart = et_userInput.text.getSpanStart((spanArray[spanArray.size - 1]))
        val spanEnd = et_userInput.text.getSpanEnd((spanArray[spanArray.size - 1]))

        // Remove the SPAN_EXCLUSIVE_INCLUSIVE
        et_userInput.text.removeSpan(spanArray[0])

        // Place the SPAN_EXCLUSIVE_EXCLUSIVE
        et_userInput.text.setSpan(what, spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

    }

}