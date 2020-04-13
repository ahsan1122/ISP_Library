package utilities.common

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.esp.library.R

class CommonMethodsKotlin {


    companion object {
        @JvmStatic
        fun applyCustomEllipsizeSpanning(maxLines: Int, textToSpan: TextView, context: Context) {
            textToSpan.post {

                textToSpan.maxLines = maxLines

                if (textToSpan.lineCount > maxLines) {
                    val lineStartIndex = textToSpan.layout.getLineStart(0)
                    var lineEndIndex = textToSpan.layout.getLineEnd(maxLines - 1)


                    val actualText = textToSpan.getText().toString()
                    var subStringText: String

                    subStringText = actualText.substring(lineStartIndex, lineEndIndex)

                    lineEndIndex = subStringText.length - 8
                    subStringText = subStringText.substring(lineStartIndex, lineEndIndex)
                    var lastIndex = lineEndIndex

                    for (i in subStringText.length - 1 downTo 0) {

                        val lastIndexChar = subStringText[i]

                        if (!lastIndexChar.toString().matches("[a-zA-Z.?]*".toRegex())) {
                            lastIndex = i
                            break
                        }
                    }

                    subStringText = subStringText.substring(0, lastIndex)
                    subStringText += " [ ... ]"

                    val spannableStringBuilder = SpannableStringBuilder(subStringText);
                    spannableStringBuilder.setSpan(
                            ForegroundColorSpan(
                                    ContextCompat.getColor(context, R.color.green)
                            ), spannableStringBuilder.length - 7, spannableStringBuilder.length, 0
                    )
                    spannableStringBuilder.setSpan(
                            StyleSpan(Typeface.BOLD),
                            spannableStringBuilder.length - 7,
                            spannableStringBuilder.length,
                            0
                    )

                    textToSpan.text = spannableStringBuilder


                }

            }
        }





    }



}