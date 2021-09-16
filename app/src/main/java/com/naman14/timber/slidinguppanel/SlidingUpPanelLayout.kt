package com.naman14.timber.slidinguppanel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import androidx.core.view.MotionEventCompat
import androidx.core.view.ViewCompat
import com.naman14.timber.R
import com.naman14.timber.slidinguppanel.SlidingUpPanelLayout
import java.lang.Enum

class SlidingUpPanelLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : ViewGroup(context, attrs, defStyle) {
    /**
     * The paint used to dim the main layout when sliding
     */
    private val mCoveredFadePaint = Paint()

    /**
     * Drawable used to draw the shadow between panes.
     */
    private var mShadowDrawable: Drawable? = null
    private var mDragHelper: ViewDragHelper?
    private val mTmpRect = Rect()

    /**
     * Minimum velocity that will be detected as a fling
     */
    private var mMinFlingVelocity = DEFAULT_MIN_FLING_VELOCITY

    /**
     * The fade color used for the panel covered by the slider. 0 = no fading.
     */
    private var mCoveredFadeColor = DEFAULT_FADE_COLOR

    /**
     * The size of the overhang in pixels.
     */
    private var mPanelHeight = -1

    /**
     * Determines how much to slide the panel off when expanded
     */
    private var mSlidePanelOffset = 0

    /**
     * The size of the shadow in pixels.
     */
    private var mShadowHeight = -1

    /**
     * Paralax offset
     */
    private var mParallaxOffset = -1

    /**
     * Clamps the Main view to the slideable view
     */
    private var mDirectOffset = false

    /**
     * True if the collapsed panel should be dragged up.
     */
    private var mIsSlidingUp = false
    /**
     * Check if the panel is set as an overlay.
     */
    /**
     * Sets whether or not the panel overlays the content
     *
     * @param overlayed
     */
    /**
     * Panel overlays the windows instead of putting it underneath it.
     */
    var isOverlayed = DEFAULT_OVERLAY_FLAG

    /**
     * If provided, the panel can be dragged by only this view. Otherwise, the entire panel can be
     * used for dragging.
     */
    private var mDragView: View? = null

    /**
     * If provided, the panel can be dragged by only this view. Otherwise, the entire panel can be
     * used for dragging.
     */
    private var mDragViewResId = -1

    /**
     * Whether clicking on the drag view will expand/collapse
     */
    private var mDragViewClickable = DEFAULT_DRAG_VIEW_CLICKABLE

    /**
     * The child view that can slide, if any.
     */
    private var mSlideableView: View? = null

    /**
     * The main view
     */
    private var mMainView: View? = null

    /**
     * The background view
     */
    private var mBackgroundView: View? = null
    private var mSlideState: SlideState? = SlideState.COLLAPSED

    /**
     * How far the panel is offset from its expanded position.
     * range [0, 1] where 0 = collapsed, 1 = expanded.
     */
    private var mSlideOffset = 0f

    /**
     * How far in pixels the slideable panel may move.
     */
    private var mSlideRange = 0

    /**
     * A panel view is locked into internal scrolling or another condition that
     * is preventing a drag.
     */
    private var mIsUnableToDrag = false

    /**
     * Flag indicating that sliding feature is enabled\disabled
     */
    private var mIsSlidingEnabled: Boolean

    /**
     * Flag indicating if a drag view can have its own touch events.  If set
     * to true, a drag view can scroll horizontally and have its own click listener.
     *
     *
     * Default is set to false.
     */
    private var mIsUsingDragViewTouchEvents = false
    private var mInitialMotionX = 0f
    private var mInitialMotionY = 0f
    private var mAnchorPoint = 1f
    private var mPanelSlideListener: PanelSlideListener? = null

    /**
     * Stores whether or not the pane was expanded the last time it was slideable.
     * If expand/collapse operations are invoked this state is modified. Used by
     * instance state save/restore.
     */
    private var mFirstLayout = true

    /**
     * Set the Drag View after the view is inflated
     */
    override fun onFinishInflate() {
        super.onFinishInflate()
        if (mDragViewResId != -1) {
            setDragView(findViewById(mDragViewResId))
        }
    }
    /**
     * @return The ARGB-packed color value used to fade the fixed pane
     */
    /**
     * Set the color used to fade the pane covered by the sliding pane out when the pane
     * will become fully covered in the expanded state.
     *
     * @param color An ARGB-packed color value
     */
    var coveredFadeColor: Int
        get() = mCoveredFadeColor
        set(color) {
            mCoveredFadeColor = color
            invalidate()
        }

    /**
     * Set sliding enabled flag
     *
     * @param enabled flag value
     */
    var isSlidingEnabled: Boolean
        get() = mIsSlidingEnabled && mSlideableView != null
        set(enabled) {
            mIsSlidingEnabled = enabled
        }
    /**
     * @return The current collapsed panel height
     */
    /**
     * Set the collapsed panel height in pixels
     *
     * @param val A height in pixels
     */
    var panelHeight: Int
        get() = mPanelHeight
        set(`val`) {
            mPanelHeight = 0
            requestLayout()
        }

    /**
     * Sets the panel offset when collapsed so you can exit
     * the boundaries of the top of the screen
     *
     * @param val Offset in pixels
     */
    fun setSlidePanelOffset(`val`: Int) {
        mSlidePanelOffset = `val`
        requestLayout()
    }

    /**
     * @return The current paralax offset
     */
    val currentParalaxOffset: Int
        get() = if (mParallaxOffset < 0) {
            0
        } else (mParallaxOffset * directionalSlideOffset).toInt()

    /**
     * @return The directional slide offset
     */
    protected val directionalSlideOffset: Float
        protected get() = if (mIsSlidingUp) -mSlideOffset else mSlideOffset

    /**
     * Sets the panel slide listener
     *
     * @param listener
     */
    fun setPanelSlideListener(listener: PanelSlideListener?) {
        mPanelSlideListener = listener
    }

    /**
     * Set the draggable view portion. Use to null, to allow the whole panel to be draggable
     *
     * @param dragView A view that will be used to drag the panel.
     */
    fun setDragView(dragView: View?) {
        if (mDragView != null && mDragViewClickable) {
            mDragView!!.setOnClickListener(null)
        }
        mDragView = dragView
        if (mDragView != null) {
            mDragView!!.isClickable = true
            mDragView!!.isFocusable = false
            mDragView!!.isFocusableInTouchMode = false
            if (mDragViewClickable) {
                mDragView!!.setOnClickListener(OnClickListener {
                    if (!isEnabled) return@OnClickListener
                    if (!isPanelExpanded && !isPanelAnchored) {
                        expandPanel(mAnchorPoint)
                    } else {
                        collapsePanel()
                    }
                })
            }
        }
    }
    /**
     * Gets the currently set anchor point
     *
     * @return the currently set anchor point
     */
    /**
     * Set an anchor point where the panel can stop during sliding
     *
     * @param anchorPoint A value between 0 and 1, determining the position of the anchor point
     * starting from the top of the layout.
     */
    var anchorPoint: Float
        get() = mAnchorPoint
        set(anchorPoint) {
            if (anchorPoint > 0 && anchorPoint <= 1) {
                mAnchorPoint = anchorPoint
            }
        }

    fun dispatchOnPanelSlide(panel: View?) {
        if (mPanelSlideListener != null) {
            mPanelSlideListener!!.onPanelSlide(panel, mSlideOffset)
        }
    }

    fun dispatchOnPanelExpanded(panel: View?) {
        if (mPanelSlideListener != null) {
            mPanelSlideListener!!.onPanelExpanded(panel)
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    fun dispatchOnPanelCollapsed(panel: View?) {
        if (mPanelSlideListener != null) {
            mPanelSlideListener!!.onPanelCollapsed(panel)
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    fun dispatchOnPanelAnchored(panel: View?) {
        if (mPanelSlideListener != null) {
            mPanelSlideListener!!.onPanelAnchored(panel)
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    fun dispatchOnPanelHidden(panel: View?) {
        if (mPanelSlideListener != null) {
            mPanelSlideListener!!.onPanelHidden(panel)
        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED)
    }

    fun updateObscuredViewVisibility() {
        if (childCount == 0) {
            return
        }
        val leftBound = paddingLeft
        val rightBound = width - paddingRight
        val topBound = paddingTop
        val bottomBound = height - paddingBottom
        val left: Int
        val right: Int
        val top: Int
        val bottom: Int
        if (mSlideableView != null && hasOpaqueBackground(mSlideableView!!)) {
            left = mSlideableView!!.left
            right = mSlideableView!!.right
            top = mSlideableView!!.top
            bottom = mSlideableView!!.bottom
        } else {
            bottom = 0
            top = bottom
            right = top
            left = right
        }
        val child = mMainView
        val clampedChildLeft = Math.max(leftBound, child!!.left)
        val clampedChildTop = Math.max(topBound, child.top)
        val clampedChildRight = Math.min(rightBound, child.right)
        val clampedChildBottom = Math.min(bottomBound, child.bottom)
        val vis: Int
        vis = if (clampedChildLeft >= left && clampedChildTop >= top && clampedChildRight <= right && clampedChildBottom <= bottom) {
            INVISIBLE
        } else {
            VISIBLE
        }
        child.visibility = vis
    }

    fun setAllChildrenVisible() {
        var i = 0
        val childCount = childCount
        while (i < childCount) {
            val child = getChildAt(i)
            if (child.visibility == INVISIBLE) {
                child.visibility = VISIBLE
            }
            i++
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        mFirstLayout = true
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mFirstLayout = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        check(widthMode == MeasureSpec.EXACTLY) { "Width must have an exact value or MATCH_PARENT" }
        check(heightMode == MeasureSpec.EXACTLY) { "Height must have an exact value or MATCH_PARENT" }
        val childCount = childCount
        check(!(childCount != 2 && childCount != 3)) { "Sliding up panel layout must have exactly 2 or 3 children!" }
        if (childCount == 2) {
            mMainView = getChildAt(0)
            mSlideableView = getChildAt(1)
        } else {
            mBackgroundView = getChildAt(0)
            mMainView = getChildAt(1)
            mSlideableView = getChildAt(2)
        }
        if (mDragView == null) {
            setDragView(mSlideableView)
        }

        // If the sliding panel is not visible, then put the whole view in the hidden state
        if (mSlideableView?.visibility == GONE) {
            mSlideState = SlideState.HIDDEN
        }
        val layoutHeight = heightSize - paddingTop - paddingBottom

        // First pass. Measure based on child LayoutParams width/height.
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            val lp = child.layoutParams as LayoutParams

            // We always measure the sliding panel in order to know it's height (needed for show panel)
            if (child.visibility == GONE && child === mMainView) {
                continue
            }
            var height = layoutHeight
            if (child === mMainView && !isOverlayed && mSlideState != SlideState.HIDDEN) {
                height -= mPanelHeight
            }
            var childWidthSpec: Int
            childWidthSpec = if (lp.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.AT_MOST)
            } else if (lp.width == ViewGroup.LayoutParams.MATCH_PARENT) {
                MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY)
            } else {
                MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY)
            }
            var childHeightSpec: Int
            childHeightSpec = if (lp.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
            } else if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT) {
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
            } else {
                MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY)
            }
            if (child === mSlideableView) {
                mSlideRange = MeasureSpec.getSize(childHeightSpec) - mPanelHeight + mSlidePanelOffset
                childHeightSpec += mSlidePanelOffset
            }
            child.measure(childWidthSpec, childHeightSpec)
        }
        setMeasuredDimension(widthSize, heightSize)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val paddingLeft = paddingLeft
        val paddingTop = paddingTop
        val childCount = childCount
        if (mFirstLayout) {
            mSlideOffset = when (mSlideState) {
                SlideState.EXPANDED -> 1.0f
                SlideState.ANCHORED -> mAnchorPoint
                SlideState.HIDDEN -> {
                    val newTop = computePanelTopPosition(0.0f) + if (mIsSlidingUp) +mPanelHeight else -mPanelHeight
                    computeSlideOffset(newTop)
                }
                else -> 0f
            }
        }
        for (i in 0 until childCount) {
            val child = getChildAt(i)

            // Always layout the sliding view on the first layout
            if (child.visibility == GONE && (child === mMainView || mFirstLayout)) {
                continue
            }
            val childHeight = child.measuredHeight
            var childTop = paddingTop
            if (child === mSlideableView) {
                childTop = computePanelTopPosition(mSlideOffset)
            }
            if (!mIsSlidingUp) {
                if (child === mMainView && !isOverlayed) {
                    childTop = computePanelTopPosition(mSlideOffset) + mSlideableView!!.measuredHeight
                }
            }
            val childBottom = childTop + childHeight
            val childRight = paddingLeft + child.measuredWidth
            child.layout(paddingLeft, childTop, childRight, childBottom)
        }
        if (mFirstLayout) {
            updateObscuredViewVisibility()
        }
        mFirstLayout = false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Recalculate sliding panes and their details
        if (h != oldh) {
            mFirstLayout = true
        }
    }

    /**
     * Set if the drag view can have its own touch events.  If set
     * to true, a drag view can scroll horizontally and have its own click listener.
     *
     *
     * Default is set to false.
     */
    fun setEnableDragViewTouchEvents(enabled: Boolean) {
        mIsUsingDragViewTouchEvents = enabled
    }

    override fun setEnabled(enabled: Boolean) {
        if (!enabled) {
            collapsePanel()
        }
        super.setEnabled(enabled)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val action = MotionEventCompat.getActionMasked(ev)
        if (!isEnabled || !mIsSlidingEnabled || mIsUnableToDrag && action != MotionEvent.ACTION_DOWN) {
            mDragHelper!!.cancel()
            return super.onInterceptTouchEvent(ev)
        }
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
            mDragHelper!!.cancel()
            return false
        }
        val x = ev.x
        val y = ev.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                mIsUnableToDrag = false
                mInitialMotionX = x
                mInitialMotionY = y
            }
            MotionEvent.ACTION_MOVE -> {
                val adx = Math.abs(x - mInitialMotionX)
                val ady = Math.abs(y - mInitialMotionY)
                val dragSlop = mDragHelper!!.touchSlop

                // Handle any horizontal scrolling on the drag view.
                if (mIsUsingDragViewTouchEvents && adx > dragSlop && ady < dragSlop) {
                    return super.onInterceptTouchEvent(ev)
                }
                if (ady > dragSlop && adx > ady || !isDragViewUnder(mInitialMotionX.toInt(), mInitialMotionY.toInt())) {
                    mDragHelper!!.cancel()
                    mIsUnableToDrag = true
                    return false
                }
            }
        }
        return mDragHelper!!.shouldInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (!isSlidingEnabled) {
            return super.onTouchEvent(ev)
        }
        mDragHelper!!.processTouchEvent(ev)
        return true
    }

    private fun isDragViewUnder(x: Int, y: Int): Boolean {
        if (mDragView == null) return false
        val viewLocation = IntArray(2)
        mDragView!!.getLocationOnScreen(viewLocation)
        val parentLocation = IntArray(2)
        getLocationOnScreen(parentLocation)
        val screenX = parentLocation[0] + x
        val screenY = parentLocation[1] + y
        return screenX >= viewLocation[0] && screenX < viewLocation[0] + mDragView!!.width && screenY >= viewLocation[1] && screenY < viewLocation[1] + mDragView!!.height
    }

    private fun expandPanel(pane: View, initialVelocity: Int, mSlideOffset: Float): Boolean {
        return mFirstLayout || smoothSlideTo(mSlideOffset, initialVelocity)
    }

    private fun collapsePanel(pane: View?, initialVelocity: Int): Boolean {
        return mFirstLayout || smoothSlideTo(0.0f, initialVelocity)
    }

    /*
     * Computes the top position of the panel based on the slide offset.
     */
    private fun computePanelTopPosition(slideOffset: Float): Int {
        val slidingViewHeight = if (mSlideableView != null) mSlideableView!!.measuredHeight else 0
        val slidePixelOffset = (slideOffset * mSlideRange).toInt()
        // Compute the top of the panel if its collapsed
        return if (mIsSlidingUp) measuredHeight - paddingBottom - mPanelHeight - slidePixelOffset else paddingTop - slidingViewHeight + mPanelHeight + slidePixelOffset
    }

    /*
     * Computes the slide offset based on the top position of the panel
     */
    private fun computeSlideOffset(topPosition: Int): Float {
        // Compute the panel top position if the panel is collapsed (offset 0)
        val topBoundCollapsed = computePanelTopPosition(0f)

        // Determine the new slide offset based on the collapsed top position and the new required
        // top position
        return if (mIsSlidingUp) (topBoundCollapsed - topPosition).toFloat() / mSlideRange else (topPosition - topBoundCollapsed).toFloat() / mSlideRange
    }

    /**
     * Collapse the sliding pane if it is currently slideable. If first layout
     * has already completed this will animate.
     *
     * @return true if the pane was slideable and is now collapsed/in the process of collapsing
     */
    fun collapsePanel(): Boolean {
        return if (mFirstLayout) {
            mSlideState = SlideState.COLLAPSED
            true
        } else {
            if (mSlideState == SlideState.HIDDEN || mSlideState == SlideState.COLLAPSED) false else collapsePanel(mSlideableView, 0)
        }
    }

    /**
     * Expand the sliding pane if it is currently slideable.
     *
     * @return true if the pane was slideable and is now expanded/in the process of expading
     */
    fun expandPanel(): Boolean {
        return if (mFirstLayout) {
            mSlideState = SlideState.EXPANDED
            true
        } else {
            expandPanel(1.0f)
        }
    }

    /**
     * Expand the sliding pane to the anchor point if it is currently slideable.
     *
     * @return true if the pane was slideable and is now expanded/in the process of expading
     */
    fun anchorPanel(): Boolean {
        return if (mFirstLayout) {
            mSlideState = SlideState.ANCHORED
            true
        } else {
            expandPanel(mAnchorPoint)
        }
    }

    /**
     * Partially expand the sliding panel up to a specific offset
     *
     * @param mSlideOffset Value between 0 and 1, where 0 is completely expanded.
     * @return true if the pane was slideable and is now expanded/in the process of expanding
     */
    fun expandPanel(mSlideOffset: Float): Boolean {
        if (mSlideableView == null || mSlideState == SlideState.EXPANDED) return false
        mSlideableView!!.visibility = VISIBLE
        return expandPanel(mSlideableView!!, 0, mSlideOffset)
    }

    /**
     * Check if the sliding panel in this layout is fully expanded.
     *
     * @return true if sliding panel is completely expanded
     */
    val isPanelExpanded: Boolean
        get() = mSlideState == SlideState.EXPANDED

    /**
     * Check if the sliding panel in this layout is anchored.
     *
     * @return true if sliding panel is anchored
     */
    val isPanelAnchored: Boolean
        get() = mSlideState == SlideState.ANCHORED

    /**
     * Check if the sliding panel in this layout is currently visible.
     *
     * @return true if the sliding panel is visible.
     */
    val isPanelHidden: Boolean
        get() = mSlideState == SlideState.HIDDEN

    /**
     * Shows the panel from the hidden state
     */
    fun showPanel() {
        if (mFirstLayout) {
            mSlideState = SlideState.COLLAPSED
        } else {
            if (mSlideableView == null || mSlideState != SlideState.HIDDEN) return
            mSlideableView!!.visibility = VISIBLE
            requestLayout()
            smoothSlideTo(0f, 0)
        }
    }

    /**
     * Hides the sliding panel entirely.
     */
    fun hidePanel() {
        if (mFirstLayout) {
            mSlideState = SlideState.HIDDEN
        } else {
            if (mSlideState == SlideState.DRAGGING || mSlideState == SlideState.HIDDEN) return
            val newTop = computePanelTopPosition(0.0f) + if (mIsSlidingUp) +mPanelHeight else -mPanelHeight
            smoothSlideTo(computeSlideOffset(newTop), 0)
        }
    }

    @SuppressLint("NewApi")
    private fun onPanelDragged(newTop: Int) {
        mSlideState = SlideState.DRAGGING
        // Recompute the slide offset based on the new top position
        mSlideOffset = computeSlideOffset(newTop)
        // Update the parallax based on the new slide offset
        if ((mParallaxOffset > 0 || mDirectOffset) && mSlideOffset >= 0) {
            var mainViewOffset = 0
            mainViewOffset = if (mParallaxOffset > 0) {
                currentParalaxOffset
            } else {
                (directionalSlideOffset * mSlideRange).toInt()
            }
            mMainView!!.translationY = mainViewOffset.toFloat()
        }

        // Dispatch the slide event
        dispatchOnPanelSlide(mSlideableView)
        // If the slide offset is negative, and overlay is not on, we need to increase the
        // height of the main content
        if (mSlideOffset <= 0 && !isOverlayed) {
            // expand the main view
            val lp = mMainView!!.layoutParams as LayoutParams
            lp.height = if (mIsSlidingUp) newTop - paddingBottom else height - paddingBottom - mSlideableView!!.measuredHeight - newTop
            mMainView!!.requestLayout()
        }
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val result: Boolean
        val save = canvas.save()
        if (isSlidingEnabled && mMainView === child) {
            // Clip against the slider; no sense drawing what will immediately be covered,
            // Unless the panel is set to overlay content
            if (!isOverlayed) {
                canvas.getClipBounds(mTmpRect)
                if (mIsSlidingUp) {
                    mTmpRect.bottom = Math.min(mTmpRect.bottom, mSlideableView!!.top)
                } else {
                    mTmpRect.top = Math.max(mTmpRect.top, mSlideableView!!.bottom)
                }
                canvas.clipRect(mTmpRect)
            }
        }
        result = super.drawChild(canvas, child, drawingTime)
        canvas.restoreToCount(save)
        if (mCoveredFadeColor != 0 && mSlideOffset > 0) {
            val baseAlpha = mCoveredFadeColor and -0x1000000 ushr 24
            val imag = (baseAlpha * mSlideOffset).toInt()
            val color = imag shl 24 or (mCoveredFadeColor and 0xffffff)
            mCoveredFadePaint.color = color
            canvas.drawRect(mTmpRect, mCoveredFadePaint)
        }
        return result
    }

    /**
     * Smoothly animate mDraggingPane to the target X position within its range.
     *
     * @param slideOffset position to animate to
     * @param velocity    initial velocity in case of fling, or 0.
     */
    fun smoothSlideTo(slideOffset: Float, velocity: Int): Boolean {
        if (!isSlidingEnabled) {
            // Nothing to do.
            return false
        }
        val panelTop = computePanelTopPosition(slideOffset)
        if (mDragHelper!!.smoothSlideViewTo(mSlideableView, mSlideableView!!.left, panelTop)) {
            setAllChildrenVisible()
            ViewCompat.postInvalidateOnAnimation(this)
            return true
        }
        return false
    }

    override fun computeScroll() {
        if (mDragHelper != null && mDragHelper!!.continueSettling(true)) {
            if (!isSlidingEnabled) {
                mDragHelper!!.abort()
                return
            }
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    override fun draw(c: Canvas) {
        super.draw(c)
        if (!isSlidingEnabled) {
            // No need to draw a shadow if we don't have one.
            return
        }
        val right = mSlideableView!!.right
        val top: Int
        val bottom: Int
        if (mIsSlidingUp) {
            top = mSlideableView!!.top - mShadowHeight
            bottom = mSlideableView!!.top
        } else {
            top = mSlideableView!!.bottom
            bottom = mSlideableView!!.bottom + mShadowHeight
        }
        val left = mSlideableView!!.left
        if (mShadowDrawable != null) {
            mShadowDrawable!!.setBounds(left, top, right, bottom)
            mShadowDrawable!!.draw(c)
        }
    }

    /**
     * Tests scrollability within child views of v given a delta of dx.
     *
     * @param v      View to test for horizontal scrollability
     * @param checkV Whether the view v passed should itself be checked for scrollability (true),
     * or just its children (false).
     * @param dx     Delta scrolled in pixels
     * @param x      X coordinate of the active touch point
     * @param y      Y coordinate of the active touch point
     * @return true if child views of v can be scrolled by delta of dx.
     */
    protected fun canScroll(v: View, checkV: Boolean, dx: Int, x: Int, y: Int): Boolean {
        if (v is ViewGroup) {
            val group = v
            val scrollX = v.getScrollX()
            val scrollY = v.getScrollY()
            val count = group.childCount
            // Count backwards - let topmost views consume scroll distance first.
            for (i in count - 1 downTo 0) {
                val child = group.getChildAt(i)
                if (x + scrollX >= child.left && x + scrollX < child.right && y + scrollY >= child.top && y + scrollY < child.bottom &&
                        canScroll(child, true, dx, x + scrollX - child.left,
                                y + scrollY - child.top)) {
                    return true
                }
            }
        }
        return checkV && ViewCompat.canScrollHorizontally(v, -dx)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams()
    }

    override fun generateLayoutParams(p: ViewGroup.LayoutParams): ViewGroup.LayoutParams {
        return if (p is MarginLayoutParams) LayoutParams(p) else LayoutParams(p)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams && super.checkLayoutParams(p)
    }

    override fun generateLayoutParams(attrs: AttributeSet): ViewGroup.LayoutParams {
        return LayoutParams(context, attrs)
    }

    public override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val ss = SavedState(superState)
        ss.mSlideState = mSlideState
        return ss
    }

    public override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        mSlideState = ss.mSlideState
    }

    /**
     * Current state of the slideable view.
     */
    enum class SlideState {
        EXPANDED, COLLAPSED, ANCHORED, HIDDEN, DRAGGING
    }

    /**
     * Listener for monitoring events about sliding panes.
     */
    interface PanelSlideListener {
        /**
         * Called when a sliding pane's position changes.
         *
         * @param panel       The child view that was moved
         * @param slideOffset The new offset of this sliding pane within its range, from 0-1
         */
        fun onPanelSlide(panel: View?, slideOffset: Float)

        /**
         * Called when a sliding panel becomes slid completely collapsed.
         *
         * @param panel The child view that was slid to an collapsed position
         */
        fun onPanelCollapsed(panel: View?)

        /**
         * Called when a sliding panel becomes slid completely expanded.
         *
         * @param panel The child view that was slid to a expanded position
         */
        fun onPanelExpanded(panel: View?)

        /**
         * Called when a sliding panel becomes anchored.
         *
         * @param panel The child view that was slid to a anchored position
         */
        fun onPanelAnchored(panel: View?)

        /**
         * Called when a sliding panel becomes completely hidden.
         *
         * @param panel The child view that was slid to a hidden position
         */
        fun onPanelHidden(panel: View?)
    }

    /**
     * No-op stubs for [PanelSlideListener]. If you only want to implement a subset
     * of the listener methods you can extend this instead of implement the full interface.
     */
    class SimplePanelSlideListener : PanelSlideListener {
        override fun onPanelSlide(panel: View?, slideOffset: Float) {}
        override fun onPanelCollapsed(panel: View?) {}
        override fun onPanelExpanded(panel: View?) {}
        override fun onPanelAnchored(panel: View?) {}
        override fun onPanelHidden(panel: View?) {}
    }

    class LayoutParams : MarginLayoutParams {
        constructor() : super(MATCH_PARENT, MATCH_PARENT) {}
        constructor(width: Int, height: Int) : super(width, height) {}
        constructor(source: ViewGroup.LayoutParams?) : super(source) {}
        constructor(source: MarginLayoutParams?) : super(source) {}
        constructor(source: LayoutParams?) : super(source) {}
        constructor(c: Context, attrs: AttributeSet?) : super(c, attrs) {
            val a = c.obtainStyledAttributes(attrs, ATTRS)
            a.recycle()
        }

        companion object {
            private val ATTRS = intArrayOf(
                    android.R.attr.layout_weight
            )
        }
    }

    internal class SavedState : BaseSavedState {
        var mSlideState: SlideState? = null

        constructor(superState: Parcelable?) : super(superState) {}
        private constructor(`in`: Parcel) : super(`in`) {
            mSlideState = try {
                Enum.valueOf(SlideState::class.java, `in`.readString())
            } catch (e: IllegalArgumentException) {
                SlideState.COLLAPSED
            }
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(mSlideState.toString())
        }

//        companion object {
//            val CREATOR: Parcelable.Creator<SavedState> = object : Parcelable.Creator<SavedState?> {
//                override fun createFromParcel(`in`: Parcel): SavedState? {
//                    return SavedState(`in`)
//                }
//
//                override fun newArray(size: Int): Array<SavedState?> {
//                    return arrayOfNulls(size)
//                }
//            }
//        }
    }

    private inner class DragHelperCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View?, pointerId: Int): Boolean {
            return if (mIsUnableToDrag) {
                false
            } else child === mSlideableView
        }

        override fun onViewDragStateChanged(state: Int) {
            if (mDragHelper!!.viewDragState == ViewDragHelper.STATE_IDLE) {
                mSlideOffset = computeSlideOffset(mSlideableView!!.top)
                if (mSlideOffset == 1f) {
                    if (mSlideState != SlideState.EXPANDED) {
                        updateObscuredViewVisibility()
                        mSlideState = SlideState.EXPANDED
                        dispatchOnPanelExpanded(mSlideableView)
                    }
                } else if (mSlideOffset == 0f) {
                    if (mSlideState != SlideState.COLLAPSED) {
                        mSlideState = SlideState.COLLAPSED
                        dispatchOnPanelCollapsed(mSlideableView)
                    }
                } else if (mSlideOffset < 0) {
                    mSlideState = SlideState.HIDDEN
                    mSlideableView!!.visibility = GONE
                    dispatchOnPanelHidden(mSlideableView)
                } else if (mSlideState != SlideState.ANCHORED) {
                    updateObscuredViewVisibility()
                    mSlideState = SlideState.ANCHORED
                    dispatchOnPanelAnchored(mSlideableView)
                }
            }
        }

        override fun onViewCaptured(capturedChild: View?, activePointerId: Int) {
            setAllChildrenVisible()
        }

        override fun onViewPositionChanged(changedView: View?, left: Int, top: Int, dx: Int, dy: Int) {
            onPanelDragged(top)
            invalidate()
        }

        override fun onViewReleased(releasedChild: View?, xvel: Float, yvel: Float) {
            var target = 0

            // direction is always positive if we are sliding in the expanded direction
            val direction = if (mIsSlidingUp) -yvel else yvel
            target = if (direction > 0) {
                // swipe up -> expand
                computePanelTopPosition(1.0f)
            } else if (direction < 0) {
                // swipe down -> collapse
                computePanelTopPosition(0.0f)
            } else if (mAnchorPoint != 1f && mSlideOffset >= (1f + mAnchorPoint) / 2) {
                // zero velocity, and far enough from anchor point => expand to the top
                computePanelTopPosition(1.0f)
            } else if (mAnchorPoint == 1f && mSlideOffset >= 0.5f) {
                // zero velocity, and far enough from anchor point => expand to the top
                computePanelTopPosition(1.0f)
            } else if (mAnchorPoint != 1f && mSlideOffset >= mAnchorPoint) {
                computePanelTopPosition(mAnchorPoint)
            } else if (mAnchorPoint != 1f && mSlideOffset >= mAnchorPoint / 2) {
                computePanelTopPosition(mAnchorPoint)
            } else {
                // settle at the bottom
                computePanelTopPosition(0.0f)
            }
            mDragHelper!!.settleCapturedViewAt(releasedChild!!.left, target)
            invalidate()
        }

        override fun getViewVerticalDragRange(child: View?): Int {
            return mSlideRange
        }

        override fun clampViewPositionVertical(child: View?, top: Int, dy: Int): Int {
            val collapsedTop = computePanelTopPosition(0f)
            val expandedTop = computePanelTopPosition(1.0f)
            return if (mIsSlidingUp) {
                Math.min(Math.max(top, expandedTop), collapsedTop)
            } else {
                Math.min(Math.max(top, collapsedTop), expandedTop)
            }
        }
    }

    companion object {
        private val TAG = SlidingUpPanelLayout::class.java.simpleName

        /**
         * Default peeking out panel height
         */
        private const val DEFAULT_PANEL_HEIGHT = 68 // dp;

        /**
         * Default anchor point height
         */
        private const val DEFAULT_ANCHOR_POINT = 1.0f // In relative %

        /**
         * Default height of the shadow above the peeking out panel
         */
        private const val DEFAULT_SHADOW_HEIGHT = 4 // dp;

        /**
         * If no fade color is given by default it will fade to 80% gray.
         */
        private const val DEFAULT_FADE_COLOR = -0x67000000

        /**
         * Whether we should hook up the drag view clickable state
         */
        private const val DEFAULT_DRAG_VIEW_CLICKABLE = true

        /**
         * Default Minimum velocity that will be detected as a fling
         */
        private const val DEFAULT_MIN_FLING_VELOCITY = 400 // dips per second

        /**
         * Default is set to false because that is how it was written
         */
        private const val DEFAULT_OVERLAY_FLAG = false

        /**
         * Default attributes for layout
         */
        private val DEFAULT_ATTRS = intArrayOf(
                android.R.attr.gravity
        )

        /**
         * Default paralax length of the main view
         */
        private const val DEFAULT_PARALAX_OFFSET = 0

        /**
         * Default slide panel offset when collapsed
         */
        private const val DEFAULT_SLIDE_PANEL_OFFSET = 0

        /**
         * Default direct offset flag
         */
        private const val DEFAULT_DIRECT_OFFSET_FLAG = false

        /**
         * Default initial state for the component
         */
        private val DEFAULT_SLIDE_STATE = SlideState.COLLAPSED
        private fun hasOpaqueBackground(v: View): Boolean {
            val bg = v.background
            return bg != null && bg.opacity == PixelFormat.OPAQUE
        }
    }

    init {
        if (isInEditMode) {
            mShadowDrawable = null
            mDragHelper = null

        }
        if (attrs != null) {
            val defAttrs = context.obtainStyledAttributes(attrs, DEFAULT_ATTRS)
            if (defAttrs != null) {
                val gravity = defAttrs.getInt(0, Gravity.NO_GRAVITY)
                require(!(gravity != Gravity.TOP && gravity != Gravity.BOTTOM)) { "gravity must be set to either top or bottom" }
                mIsSlidingUp = gravity == Gravity.BOTTOM
            }
            defAttrs!!.recycle()
            val ta = context.obtainStyledAttributes(attrs, R.styleable.SlidingUpPanelLayout)
            if (ta != null) {
                mPanelHeight = ta.getDimensionPixelSize(R.styleable.SlidingUpPanelLayout_panelHeight, -1)
                mSlidePanelOffset = ta.getDimensionPixelSize(R.styleable.SlidingUpPanelLayout_slidePanelOffset, DEFAULT_SLIDE_PANEL_OFFSET)
                mShadowHeight = ta.getDimensionPixelSize(R.styleable.SlidingUpPanelLayout_shadowHeight, -1)
                mParallaxOffset = ta.getDimensionPixelSize(R.styleable.SlidingUpPanelLayout_paralaxOffset, -1)
                mDirectOffset = ta.getBoolean(R.styleable.SlidingUpPanelLayout_directOffset, DEFAULT_DIRECT_OFFSET_FLAG)
                mMinFlingVelocity = ta.getInt(R.styleable.SlidingUpPanelLayout_flingVelocity, DEFAULT_MIN_FLING_VELOCITY)
                mCoveredFadeColor = ta.getColor(R.styleable.SlidingUpPanelLayout_fadeColor, DEFAULT_FADE_COLOR)
                mDragViewResId = ta.getResourceId(R.styleable.SlidingUpPanelLayout_dragView, -1)
                mDragViewClickable = ta.getBoolean(R.styleable.SlidingUpPanelLayout_dragViewClickable, DEFAULT_DRAG_VIEW_CLICKABLE)
                isOverlayed = ta.getBoolean(R.styleable.SlidingUpPanelLayout_overlay, DEFAULT_OVERLAY_FLAG)
                mAnchorPoint = ta.getFloat(R.styleable.SlidingUpPanelLayout_anchorPoint, DEFAULT_ANCHOR_POINT)
                mSlideState = SlideState.values()[ta.getInt(R.styleable.SlidingUpPanelLayout_initialState, DEFAULT_SLIDE_STATE.ordinal)]
            }
            ta!!.recycle()
        }
        val density = context.resources.displayMetrics.density
        if (mPanelHeight == -1) {
            mPanelHeight = (DEFAULT_PANEL_HEIGHT * density + 0.5f).toInt()
        }
        if (mShadowHeight == -1) {
            mShadowHeight = (DEFAULT_SHADOW_HEIGHT * density + 0.5f).toInt()
        }
        if (mParallaxOffset == -1) {
            mParallaxOffset = (DEFAULT_PARALAX_OFFSET * density).toInt()
        }
        // If the shadow height is zero, don't show the shadow
        mShadowDrawable = if (mShadowHeight > 0) {
            if (mIsSlidingUp) {
                ContextCompat.getDrawable(context, R.drawable.above_shadow)
            } else {
                ContextCompat.getDrawable(context, R.drawable.below_shadow)
            }
        } else {
            null
        }
        setWillNotDraw(false)
        mDragHelper = ViewDragHelper.create(this, 0.5f, DragHelperCallback())
        mDragHelper!!.minVelocity = mMinFlingVelocity * density
        mIsSlidingEnabled = true
    }
}