package com.example.creationownviewtest.screenTwoComponentView

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.creationownviewtest.R
import com.example.creationownviewtest.databinding.CastComponentFromBaseViewBinding

enum class BottomButtonAction {
    POSITIVE, NEGATIVE
}

typealias OnBottomButtonsActionListener = (BottomButtonAction) -> Unit

//есть макетный файл констрейнт поетому от него и наследуемся
class CastComponentInFromBaseView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        0
    )

    constructor(context: Context, attrs: AttributeSet?) : this(
        context, attrs, R.attr.bottomButtonStyle
    )

    constructor(context: Context) : this(context, null)

    private val binding: CastComponentFromBaseViewBinding
    private var listener: OnBottomButtonsActionListener? = null

    var isProgressMode: Boolean = false
        set(value) {
            field = value
            if (value) {
                binding.positiveB.visibility = View.INVISIBLE
                binding.negativeB.visibility = View.INVISIBLE
                binding.progressBar.visibility = View.VISIBLE
            } else {
                binding.positiveB.visibility = View.VISIBLE
                binding.negativeB.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
        }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.cast_component_from_base_view, this, true)
        binding = CastComponentFromBaseViewBinding.bind(this)
        initializeAttributes(attrs, defStyleAttr, defStyleRes)
        initListeners()
    }

    // логика отработки атрибутов
    private fun initializeAttributes(
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) {
        if (attrs == null) return

        val typedArray = context.obtainStyledAttributes(
            attrs,
            R.styleable.CastComponentInFromBaseView, defStyleAttr, defStyleRes
        )

        with(binding) {

            val positiveButtonText = typedArray
                .getString(
                    R.styleable.CastComponentInFromBaseView_buttonPositiveButtonText
                )
            setPositiveButtonText(positiveButtonText)

            val negativeButtonText = typedArray
                .getString(
                    R.styleable.CastComponentInFromBaseView_buttonNegativeButtonText
                )
            setNegativeButtonText(negativeButtonText)

            val positiveButtonBgColor = typedArray
                .getColor(
                    R.styleable.CastComponentInFromBaseView_buttonPositiveBackgroundColor,
                    Color.BLACK
                )
            positiveB.backgroundTintList = ColorStateList
                .valueOf(positiveButtonBgColor)

            val negativeButtonBgColor = typedArray
                .getColor(
                    R.styleable.CastComponentInFromBaseView_buttonNegativeBackgroundColor,
                    Color.WHITE
                )
            negativeB.backgroundTintList = ColorStateList
                .valueOf(negativeButtonBgColor)

            this@CastComponentInFromBaseView.isProgressMode = typedArray
                .getBoolean(
                    R.styleable.CastComponentInFromBaseView_buttonProgressMode,
                    false
                )
        }

        typedArray.recycle()

    }

    private fun initListeners() {
        binding.positiveB.setOnClickListener {
            this.listener?.invoke(BottomButtonAction.POSITIVE)
        }
        binding.negativeB.setOnClickListener {
            this.listener?.invoke(BottomButtonAction.NEGATIVE)
        }
    }

    fun setListener(listener: OnBottomButtonsActionListener?) {
        this.listener = listener
    }

    fun setPositiveButtonText(text: String?) {
        binding.positiveB.text = text ?: "Ok"
    }

    fun setNegativeButtonText(text: String?) {
        binding.negativeB.text = text ?: "Cancel"
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()!!
        val savedState = SavedState(superState)
        savedState.positiveButtonText = binding.positiveB.text.toString()
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState
        super.onRestoreInstanceState(savedState.superState)
        binding.positiveB.text = savedState.positiveButtonText
    }

    class SavedState : BaseSavedState {

        var positiveButtonText: String? = null

        constructor(superState: Parcelable) : super(superState)
        constructor(parcel: Parcel) : super(parcel) {
            positiveButtonText = parcel.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(positiveButtonText)
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState> =
                object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState {
                    return SavedState(source)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return Array(size) { null }
                }
            }
        }
    }
}