package dev.steelahhh.mapful.presentation

import android.content.Context
import android.location.Geocoder
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import coil.api.load
import coil.transform.CircleCropTransformation
import dev.steelahhh.mapful.R
import dev.steelahhh.mapful.data.User
import dev.steelahhh.mapful.data.getAddress
import kotlinx.android.synthetic.main.view_user_bubble.view.*

class UserBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.view_user_bubble, this)
        orientation = HORIZONTAL
        background = context.getDrawable(R.drawable.rounded_rect)
        isClickable = false
        isFocusable = false
        isFocusableInTouchMode = false
    }

    fun bind(user: User, geocoder: Geocoder) {
        userNameTv.text = user.name
        userAddressTv.text = user.getAddress(geocoder)
        userIV.load(user.avatar) {
            crossfade(true)
            error(R.drawable.ic_error)
            transformations(CircleCropTransformation())
        }
    }
}
