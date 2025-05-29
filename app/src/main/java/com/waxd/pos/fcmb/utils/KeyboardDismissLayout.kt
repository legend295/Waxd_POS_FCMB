package com.waxd.pos.fcmb.utils

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout

/**
 * A custom layout that automatically hides the keyboard when the user touches outside of any EditText or LabeledEditText.
 *
 * - Supports both regular and "long form" modes for efficient input field detection.
 * - In "long form" mode, input fields are pre-cached for performance.
 * - Can be used as a drop-in replacement for ConstraintLayout in layouts where keyboard dismissal is desired.
 *
 * @constructor Creates a KeyboardDismissLayout.
 * @param context The context of the view.
 * @param attrs The attribute set from XML.
 * @param defStyleAttr The default style attribute.
 */
class KeyboardDismissLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /** InputMethodManager instance for controlling the keyboard. */
    private val inputMethodManager by lazy {
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }
    /** Temporary array for storing view screen coordinates. */
    private val locationArray = IntArray(2)
    /** List of input fields (EditText or LabeledEditText.editText) for pre-caching in long form mode. */
    private val editTexts = mutableListOf<View>() // Stores both EditText and LabeledEditText.editText

    /**
     * Flag to enable "long form" mode, which pre-caches all input fields for faster detection.
     * When set to true and attached to window, input fields are pre-cached.
     */
    var isLongForm: Boolean = false
        set(value) {
            field = value
            if (value && isAttachedToWindow) preCacheInputFields()
        }

    /**
     * Called when the view is attached to the window.
     * Pre-caches input fields if in long form mode.
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isLongForm) preCacheInputFields()
    }

    /**
     * Recursively collects all EditText and LabeledEditText.editText views in the layout and stores them in [editTexts].
     */
    private fun preCacheInputFields() {
        editTexts.clear()
        gatherInputFields(this)
    }

    /**
     * Helper function to recursively gather input fields from a ViewGroup.
     * @param viewGroup The ViewGroup to search.
     */
    private fun gatherInputFields(viewGroup: ViewGroup) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            when (child) {
                is EditText -> editTexts.add(child)
                is ViewGroup -> gatherInputFields(child)
            }
        }
    }

    /**
     * Intercepts touch events to determine if the keyboard should be hidden.
     * @param ev The MotionEvent.
     * @return True if the event should be intercepted, false otherwise.
     */
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            handleTouchEvent(ev)
        }
        return super.onInterceptTouchEvent(ev)
    }

    /**
     * Handles the touch event to check if the user touched outside the currently focused input field.
     * If so, hides the keyboard.
     * @param ev The MotionEvent.
     */
    private fun handleTouchEvent(ev: MotionEvent) {
        val focusedView = findFocus()
        val actualInputField = when {
            focusedView is EditText -> focusedView
            else -> null
        } ?: return

        val isTouchingInputField = if (isLongForm) {
            checkPreCachedFields(ev, actualInputField)
        } else {
            checkOnDemand(ev, actualInputField)
        }

        if (!isTouchingInputField) {
            hideKeyboard(actualInputField)
        }
    }

    /**
     * Checks if the touch event is within any pre-cached input field except the currently focused one.
     * @param ev The MotionEvent.
     * @param currentField The currently focused EditText.
     * @return True if the touch is within another input field, false otherwise.
     */
    private fun checkPreCachedFields(ev: MotionEvent, currentField: EditText): Boolean {
        val x = ev.rawX.toInt()
        val y = ev.rawY.toInt()

        return editTexts.any { field ->
            field.getLocationOnScreen(locationArray)
            val left = locationArray[0]
            val top = locationArray[1]
            val right = left + field.width
            val bottom = top + field.height

            x in left..right && y in top..bottom && field != currentField
        }
    }

    /**
     * Checks on demand if the touch event is within any input field except the currently focused one.
     * @param ev The MotionEvent.
     * @param currentField The currently focused EditText.
     * @return True if the touch is within another input field, false otherwise.
     */
    private fun checkOnDemand(ev: MotionEvent, currentField: EditText): Boolean {
        val x = ev.rawX.toInt()
        val y = ev.rawY.toInt()

        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val targetField = when (child) {
                is EditText -> child
                else -> continue
            }

            if (targetField == currentField) continue

            targetField.getLocationOnScreen(locationArray)
            val left = locationArray[0]
            val top = locationArray[1]
            val right = left + targetField.width
            val bottom = top + targetField.height

            if (x in left..right && y in top..bottom) {
                return true
            }
        }
        return false
    }

    /**
     * Hides the keyboard and clears focus from the given EditText.
     * Adds a small delay to prevent flicker.
     * @param field The EditText to clear focus from.
     */
    private fun hideKeyboard(field: EditText) {
        field.clearFocus()
        postDelayed({
            inputMethodManager.hideSoftInputFromWindow(field.windowToken, 0)
        }, 50) // Small delay prevents flicker
    }
}