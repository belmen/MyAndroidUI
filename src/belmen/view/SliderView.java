package belmen.view;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.widget.SlidingDrawer;
import belmen.ui.R;

/**
 * 一个可以拖动的抽屉，改写自Android的{@link SlidingDrawer}<br/>
 * 使用方法大致与SlidingDrawer相同，在布局文件中需要指定content和handle<br/>
 * <br/>
 * 该类与SlidingDrawer相比有如下改进：<br/>
 * 1. 可以实现上下左右四方向的拖动，SlidingDrawer只能从下至上或从右至左。<br/>
 * -- 在布局中通过position属性指定<br/>
 * 2. 可以指定手柄的位置，而SlidingDrawer只能居中。<br/>
 * -- 在布局文件中通过handlePosition设定手柄的位置（顶部或左侧、居中、底部或右侧），
 * 以及handlePositionOffset设定偏移量<br/>
 * 3. 支持将内容填满整块区域，而SlidingDrawer会为手柄预留一些空间<br/>
 * -- 在布局文件中设置fillContent为true使内容填满整块区域<br/>
 * 4. 支持离开顶部的事件监听，设置{@link OnSlideToEndListener}来监听slider贴着顶部/离开顶部的事件<br/>
 * <br/>
 * 注意：目前不要使用topOffset和bottomOffset属性，会导致问题<br/>
 * @author Belmen
 *
 * @see SlidingDrawer
 */
public class SliderView extends ViewGroup {
	
	public static final String TAG = SliderView.class.getSimpleName();

	private static final int HANDLE_POSITION_TOP = 0;
	private static final int HANDLE_POSITION_MIDDLE = 1;
	private static final int HANDLE_POSITION_BOTTOM = 2;

    private static final int TAP_THRESHOLD = 6;
    private static final float MAXIMUM_TAP_VELOCITY = 100.0f;
    private static final float MAXIMUM_MINOR_VELOCITY = 150.0f;
    private static final float MAXIMUM_MAJOR_VELOCITY = 200.0f;
    private static final float MAXIMUM_ACCELERATION = 2000.0f;
    private static final int VELOCITY_UNITS = 1000;
    private static final int MSG_ANIMATE = 1000;
    private static final int ANIMATION_FRAME_DURATION = 1000 / 60;

    private static final int EXPANDED_FULL_OPEN = -10001;
    private static final int COLLAPSED_FULL_CLOSED = -10002;

    private final int mHandleId;
    private final int mContentId;

    private View mHandle;
    private View mContent;

    private final Rect mFrame = new Rect();
    private final Rect mInvalidate = new Rect();
    private boolean mTracking;
    private boolean mLocked;

    private VelocityTracker mVelocityTracker;

    private boolean mVertical;
    private boolean mReverse;
    private boolean mExpanded;
    private boolean mFillContent;
    private int mBottomOffset;
    private int mTopOffset;
    private int mHandleHeight;
    private int mHandleWidth;
    private int mHandlePosition;
    private int mHandlePositionOffset;

    private OnDrawerOpenListener mOnDrawerOpenListener;
    private OnDrawerCloseListener mOnDrawerCloseListener;
    private OnDrawerScrollListener mOnDrawerScrollListener;
    private OnSlideToEndListener mOnSlideToEndListener;

    private final Handler mHandler = new SlidingHandler(this);
    private float mAnimatedAcceleration;
    private float mAnimatedVelocity;
    private float mAnimationPosition;
    private long mAnimationLastTime;
    private long mCurrentAnimationTime;
    private int mTouchDelta;
    private boolean mAnimating;
    private boolean mAllowSingleTap;
    private boolean mAnimateOnClick;
    private boolean mEnd = false;

    private final int mTapThreshold;
    private final int mMaximumTapVelocity;
    private final int mMaximumMinorVelocity;
    private final int mMaximumMajorVelocity;
    private final int mMaximumAcceleration;
    private final int mVelocityUnits;

    /**
     * Callback invoked when the drawer is opened.
     */
    public static interface OnDrawerOpenListener {
        /**
         * Invoked when the drawer becomes fully open.
         */
        public void onDrawerOpened();
    }

    /**
     * Callback invoked when the drawer is closed.
     */
    public static interface OnDrawerCloseListener {
        /**
         * Invoked when the drawer becomes fully closed.
         */
        public void onDrawerClosed();
    }

    /**
     * Callback invoked when the drawer is scrolled.
     */
    public static interface OnDrawerScrollListener {
        /**
         * Invoked when the user starts dragging/flinging the drawer's handle.
         */
        public void onScrollStarted();

        /**
         * Invoked when the user stops dragging/flinging the drawer's handle.
         */
        public void onScrollEnded();
    }
    
    /**
     * Callback invoked when the slider is sliding to the end
     * @author Belmen
     */
    public static interface OnSlideToEndListener {
    	/**
    	 * Slider完全打开时触发
    	 */
    	public void onSlideToEnd();
    	/**
    	 * Slider离开顶部时触发
    	 */
    	public void onLeaveEnd();
    }
	public SliderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SliderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SliderView, defStyle, 0);

//        int orientation = a.getInt(R.styleable.SliderView_orientation, ORIENTATION_VERTICAL);
//        mVertical = orientation == ORIENTATION_VERTICAL;
		int position = a.getInt(R.styleable.SliderView_position, 0);
		mVertical = (position >> 1 & 1) == 0;
		mReverse = (position & 1) == 0;
        mBottomOffset = (int) a.getDimension(R.styleable.SliderView_bottomOffset, 0.0f);
        mTopOffset = (int) a.getDimension(R.styleable.SliderView_topOffset, 0.0f);
        mAllowSingleTap = a.getBoolean(R.styleable.SliderView_allowSingleTap, true);
        mAnimateOnClick = a.getBoolean(R.styleable.SliderView_animateOnClick, true);
        mHandlePosition = a.getInt(R.styleable.SliderView_handlePosition, HANDLE_POSITION_MIDDLE);
        mHandlePositionOffset = a.getDimensionPixelSize(R.styleable.SliderView_handlePositionOffset, 0);
        mFillContent = a.getBoolean(R.styleable.SliderView_fillContent, false);

        int handleId = a.getResourceId(R.styleable.SliderView_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException("The handle attribute is required and must refer "
                    + "to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.SliderView_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer "
                    + "to a valid child.");
        }

        if (handleId == contentId) {
            throw new IllegalArgumentException("The content and handle attributes must refer "
                    + "to different children.");
        }

        mHandleId = handleId;
        mContentId = contentId;

        final float density = getResources().getDisplayMetrics().density;
        mTapThreshold = (int) (TAP_THRESHOLD * density + 0.5f);
        mMaximumTapVelocity = (int) (MAXIMUM_TAP_VELOCITY * density + 0.5f);
        mMaximumMinorVelocity = (int) (MAXIMUM_MINOR_VELOCITY * density + 0.5f);
        mMaximumMajorVelocity = (int) (MAXIMUM_MAJOR_VELOCITY * density + 0.5f);
        mMaximumAcceleration = (int) (MAXIMUM_ACCELERATION * density + 0.5f);
        mVelocityUnits = (int) (VELOCITY_UNITS * density + 0.5f);

        a.recycle();

        setAlwaysDrawnWithCacheEnabled(false);
	}

	@Override
    protected void onFinishInflate() {
        mHandle = findViewById(mHandleId);
        if (mHandle == null) {
            throw new IllegalArgumentException("The handle attribute is must refer to an"
                    + " existing child.");
        }
        mHandle.setOnClickListener(new DrawerToggler());

        mContent = findViewById(mContentId);
        if (mContent == null) {
            throw new IllegalArgumentException("The content attribute is must refer to an" 
                    + " existing child.");
        }
        mContent.setVisibility(View.GONE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            throw new RuntimeException("SliderView cannot have UNSPECIFIED dimensions");
        }

        final View handle = mHandle;
        measureChild(handle, widthMeasureSpec, heightMeasureSpec);

        if (mVertical) {
            int height = heightSpecSize /*- handle.getMeasuredHeight()*/ - mTopOffset;
            if(!mFillContent) {
            	height -= handle.getMeasuredHeight();
            }
            mContent.measure(MeasureSpec.makeMeasureSpec(widthSpecSize, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        } else {
            int width = widthSpecSize /*- handle.getMeasuredWidth()*/ - mTopOffset;
            if(!mFillContent) {
            	width -= handle.getMeasuredWidth();
            }
            mContent.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(heightSpecSize, MeasureSpec.EXACTLY));
        }

        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        final long drawingTime = getDrawingTime();
        final View handle = mHandle;
        final boolean isVertical = mVertical;

        if (mTracking || mAnimating) {
            final Bitmap cache = mContent.getDrawingCache();
            if (cache != null) {
                if (isVertical) {
                	if(mReverse) {
                		canvas.drawBitmap(cache, 0, handle.getTop() - cache.getHeight(), null);
                	} else {
                		canvas.drawBitmap(cache, 0, handle.getBottom(), null);
                	}
                } else {
                	if(mReverse) {
                		canvas.drawBitmap(cache, handle.getLeft() - cache.getWidth(), 0, null);
                	} else {
                		canvas.drawBitmap(cache, handle.getRight(), 0, null);
                	}
                }
            } else {
                canvas.save();
                if(mReverse) {
                	canvas.translate(isVertical ? 0 : handle.getLeft() - mContent.getWidth() + mTopOffset,
                            isVertical ? handle.getTop() - mContent.getHeight() + mTopOffset: 0);
                } else {
                	canvas.translate(isVertical ? 0 : mFillContent ? handle.getRight() : handle.getLeft()/*.getLeft()*/ - mTopOffset,
                            isVertical ? mFillContent ? handle.getBottom() : handle.getTop()/*.getTop()*/ - mTopOffset : 0);
                }
                drawChild(canvas, mContent, drawingTime);
                canvas.restore();
            }
        } else if (mExpanded) {
            drawChild(canvas, mContent, drawingTime);
        }
        
        drawChild(canvas, handle, drawingTime);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (mTracking) {
            return;
        }

        final int width = r - l;
        final int height = b - t;

        final View handle = mHandle;

        int childWidth = handle.getMeasuredWidth();
        int childHeight = handle.getMeasuredHeight();

        int childLeft;
        int childTop;

        final View content = mContent;

        if (mVertical) {
        	if(mHandlePosition == HANDLE_POSITION_TOP) {
        		childLeft = 0;
        	} else if(mHandlePosition == HANDLE_POSITION_BOTTOM) {
        		childLeft = width - childWidth;
        	} else {
        		childLeft = (width - childWidth) / 2;
        	}
        	childLeft += mHandlePositionOffset;
        	
            if(mReverse) {
            	childTop = mExpanded ? height - childHeight + mBottomOffset : mTopOffset;
            	content.layout(0, mTopOffset, content.getMeasuredWidth(),
                        mTopOffset + content.getMeasuredHeight());
            } else {
            	childTop = mExpanded ? mTopOffset : height - childHeight + mBottomOffset;
            	content.layout(0, mTopOffset + (mFillContent ? 0 : childHeight)/* + childHeight*/, content.getMeasuredWidth(),
                        mTopOffset + (mFillContent ? 0 : childHeight)/* + childHeight*/ + content.getMeasuredHeight());
            }
        } else {
        	if(mHandlePosition == HANDLE_POSITION_TOP) {
        		childTop = 0;
        	} else if(mHandlePosition == HANDLE_POSITION_BOTTOM) {
        		childTop = height - childHeight;
        	} else {
        		childTop = (height - childHeight) / 2;
        	}
        	childTop += mHandlePositionOffset;
        	if(mReverse) {
        		childLeft = mExpanded ? width - childWidth + mBottomOffset : mTopOffset;
        		content.layout(mTopOffset, 0, mTopOffset + content.getMeasuredWidth(),
        				content.getMeasuredHeight());
        	} else {
        		childLeft = mExpanded ? mTopOffset : width - childWidth + mBottomOffset;
	    		content.layout(mTopOffset + (mFillContent ? 0 : childWidth)/* + childWidth*/, 0,
	                    mTopOffset + (mFillContent ? 0 : childWidth)/* + childWidth*/ + content.getMeasuredWidth(),
	                    content.getMeasuredHeight());
        	}
        }

        handle.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        mHandleHeight = handle.getHeight();
        mHandleWidth = handle.getWidth();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mLocked) {
            return false;
        }

        final int action = event.getAction();

        float x = event.getX();
        float y = event.getY();

        final Rect frame = mFrame;
        final View handle = mHandle;

        handle.getHitRect(frame);
        if (!mTracking && !frame.contains((int) x, (int) y)) {
            return false;
        }

        if (action == MotionEvent.ACTION_DOWN) {
            mTracking = true;

            handle.setPressed(true);
            // Must be called before prepareTracking()
            prepareContent();

            // Must be called after prepareContent()
            if (mOnDrawerScrollListener != null) {
                mOnDrawerScrollListener.onScrollStarted();
            }

            if (mVertical) {
                final int top = mHandle.getTop();
                mTouchDelta = (int) y - top;
                prepareTracking(top);
            } else {
                final int left = mHandle.getLeft();
                mTouchDelta = (int) x - left;
                prepareTracking(left);
            }
            mVelocityTracker.addMovement(event);
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mLocked) {
            return true;
        }

        if (mTracking) {
            mVelocityTracker.addMovement(event);
            final int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    moveHandle((int) (mVertical ? event.getY() : event.getX()) - mTouchDelta);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(mVelocityUnits);

                    float yVelocity = velocityTracker.getYVelocity();
                    float xVelocity = velocityTracker.getXVelocity();
                    boolean negative;

                    final boolean vertical = mVertical;
                    if (vertical) {
                        negative = yVelocity < 0;
                        if (xVelocity < 0) {
                            xVelocity = -xVelocity;
                        }
                        if (xVelocity > mMaximumMinorVelocity) {
                            xVelocity = mMaximumMinorVelocity;
                        }
                    } else {
                        negative = xVelocity < 0;
                        if (yVelocity < 0) {
                            yVelocity = -yVelocity;
                        }
                        if (yVelocity > mMaximumMinorVelocity) {
                            yVelocity = mMaximumMinorVelocity;
                        }
                    }

                    float velocity = (float) Math.hypot(xVelocity, yVelocity);
                    if (negative) {
                        velocity = -velocity;
                    }

                    final int top = mHandle.getTop();
                    final int left = mHandle.getLeft();

                    if (Math.abs(velocity) < mMaximumTapVelocity) {
                        if (isSingleTap(vertical, top, left)) {
                            if (mAllowSingleTap) {
                                playSoundEffect(SoundEffectConstants.CLICK);

                                if (mExpanded) {
                                    animateClose(vertical ? top : left);
                                } else {
                                    animateOpen(vertical ? top : left);
                                }
                            } else {
                                performFling(vertical ? top : left, velocity, false);
                            }

                        } else {
                            performFling(vertical ? top : left, velocity, false);
                        }
                    } else {
                        performFling(vertical ? top : left, velocity, false);
                    }
                }
                break;
            }
        }

        return mTracking || mAnimating || super.onTouchEvent(event);
    }
    
    private boolean isSingleTap(boolean vertical, int top, int left) {
    	if(mReverse) {
    		return vertical ? (!mExpanded && top < mTapThreshold + mTopOffset) ||
                (mExpanded && top > mBottomOffset + getBottom() - getTop() -
                        mHandleHeight - mTapThreshold) :
                (!mExpanded && left < mTapThreshold + mTopOffset) ||
                (mExpanded && left > mBottomOffset + getRight() - getLeft() -
                        mHandleWidth - mTapThreshold);
    	} else {
    		return vertical ? (mExpanded && top < mTapThreshold + mTopOffset) ||
                (!mExpanded && top > mBottomOffset + getBottom() - getTop() -
                        mHandleHeight - mTapThreshold) :
                (mExpanded && left < mTapThreshold + mTopOffset) ||
                (!mExpanded && left > mBottomOffset + getRight() - getLeft() -
                        mHandleWidth - mTapThreshold);
    	}
    }

    private void animateClose(int position) {
        prepareTracking(position);
        performFling(position, mReverse ? -mMaximumAcceleration : mMaximumAcceleration, true);
    }

    private void animateOpen(int position) {
        prepareTracking(position);
        performFling(position, mReverse ? mMaximumAcceleration : -mMaximumAcceleration, true);
    }

    private void performFling(int position, float velocity, boolean always) {
        mAnimationPosition = position;
        mAnimatedVelocity = velocity;

        if (mExpanded) {
            if (always || sufficientToRetract(position, velocity)) {
            	// 打开状态：关闭
                // We are expanded, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the expanded position.
            	if(mReverse) {
            		mAnimatedAcceleration = -mMaximumAcceleration;
                    if (velocity > 0) {
                        mAnimatedVelocity = 0;
                    }
            	} else {
            		mAnimatedAcceleration = mMaximumAcceleration;
                    if (velocity < 0) {
                        mAnimatedVelocity = 0;
                    }
            	}
            } else {
            	// 打开状态：复位
                // We are expanded and are now going to animate away.
            	if(mReverse) {
            		mAnimatedAcceleration = mMaximumAcceleration;
	                if (velocity < 0) {
	                    mAnimatedVelocity = 0;
	                }
            	} else {
	                mAnimatedAcceleration = -mMaximumAcceleration;
	                if (velocity > 0) {
	                    mAnimatedVelocity = 0;
	                }
            	}
            }
        } else {
            if (!always && sufficientToExpand(position, velocity)) {
            	// 关闭状态：复位
                // We are collapsed, and they moved enough to allow us to expand.
            	if(mReverse) {
            		mAnimatedAcceleration = -mMaximumAcceleration;
                    if (velocity > 0) {
                        mAnimatedVelocity = 0;
                    }
            	} else {
            		mAnimatedAcceleration = mMaximumAcceleration;
                    if (velocity < 0) {
                        mAnimatedVelocity = 0;
                    }
            	}
            } else {
            	// 关闭状态：打开
                // We are collapsed, but they didn't move sufficiently to cause
                // us to retract.  Animate back to the collapsed position.
            	if(mReverse) {
            		mAnimatedAcceleration = mMaximumAcceleration;
                    if (velocity < 0) {
                        mAnimatedVelocity = 0;
                    }
            	} else {
            		mAnimatedAcceleration = -mMaximumAcceleration;
                    if (velocity > 0) {
                        mAnimatedVelocity = 0;
                    }
            	}
            }
        }

        long now = SystemClock.uptimeMillis();
        mAnimationLastTime = now;
        mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
        mAnimating = true;
        mHandler.removeMessages(MSG_ANIMATE);
        mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE), mCurrentAnimationTime);
        stopTracking();
    }
    
    private boolean sufficientToRetract(int position, float velocity) {
    	if(mReverse) {
    		return velocity < -mMaximumMajorVelocity ||
                    (position < getWidth() - (mBottomOffset + (mVertical ? mHandleHeight : mHandleWidth)) &&
                            velocity < mMaximumMajorVelocity);
    	} else {
    		return velocity > mMaximumMajorVelocity ||
                    (position > mTopOffset + (mVertical ? mHandleHeight : mHandleWidth) &&
                            velocity > -mMaximumMajorVelocity);
    	}
    }
    
    private boolean sufficientToExpand(int position, float velocity) {
    	if(mReverse) {
    		return velocity < -mMaximumMajorVelocity ||
                    (position < (mVertical ? getHeight() : getWidth()) / 2 &&
                            velocity < mMaximumMajorVelocity);
    	} else {
    		return velocity > mMaximumMajorVelocity ||
                    (position > (mVertical ? getHeight() : getWidth()) / 2 &&
                            velocity > -mMaximumMajorVelocity);
    	}
    }

    private void prepareTracking(int position) {
        mTracking = true;
        mVelocityTracker = VelocityTracker.obtain();
        boolean opening = !mExpanded;
        if (opening) {
            mAnimatedAcceleration = mMaximumAcceleration;
            mAnimatedVelocity = mMaximumMajorVelocity;
            mAnimationPosition = mReverse ? mTopOffset :
            	(mBottomOffset + (mVertical ? getHeight() - mHandleHeight : getWidth() - mHandleWidth));
            moveHandle((int) mAnimationPosition);
            mAnimating = true;
            mHandler.removeMessages(MSG_ANIMATE);
            long now = SystemClock.uptimeMillis();
            mAnimationLastTime = now;
            mCurrentAnimationTime = now + ANIMATION_FRAME_DURATION;
            mAnimating = true;
        } else {
            if (mAnimating) {
                mAnimating = false;
                mHandler.removeMessages(MSG_ANIMATE);
            }
            moveHandle(position);
        }
    }

    private void moveHandle(int position) {
        final View handle = mHandle;
//        Logger.i(TAG, "Slider moveHandle=" + position);
        if (mVertical) {
            if (position == EXPANDED_FULL_OPEN) {
            	if(mReverse) {
            		handle.offsetTopAndBottom(mBottomOffset + getBottom() - getTop() -
                            mHandleHeight - handle.getTop());
            	} else {
            		handle.offsetTopAndBottom(mTopOffset - handle.getTop());
            	}
            	if(mOnSlideToEndListener != null) {
                	mOnSlideToEndListener.onSlideToEnd();
                }
                mEnd = true;
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
            	if(mReverse) {
            		handle.offsetTopAndBottom(mTopOffset - handle.getTop());
            	} else {
            		handle.offsetTopAndBottom(mBottomOffset + getBottom() - getTop() -
                            mHandleHeight - handle.getTop());
            	}
                invalidate();
            } else {
                final int top = handle.getTop();
                int deltaY = position - top;
                if (position < mTopOffset) {
                    deltaY = mTopOffset - top;
                } else if (deltaY > mBottomOffset + getBottom() - getTop() - mHandleHeight - top) {
                    deltaY = mBottomOffset + getBottom() - getTop() - mHandleHeight - top;
                }
                handle.offsetTopAndBottom(deltaY);

                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);

                region.union(frame.left, frame.top - deltaY, frame.right, frame.bottom - deltaY);
                if(mReverse) {
                	region.union(0, frame.top - deltaY - mContent.getHeight(), getWidth(),
                            frame.top - deltaY);
                } else {
                	region.union(0, frame.bottom - deltaY, getWidth(),
                            frame.bottom - deltaY + mContent.getHeight());
                }
                
                if(mEnd) {
                	mEnd = false;
                	if(mOnSlideToEndListener != null) {
                		mOnSlideToEndListener.onLeaveEnd();
                	}
                }

                invalidate(region);
            }
        } else {
            if (position == EXPANDED_FULL_OPEN) {
                if(mReverse) {
                	handle.offsetLeftAndRight(mBottomOffset + getRight() - getLeft() -
                            mHandleWidth - handle.getLeft());
                } else {
                	handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
                }
                if(mOnSlideToEndListener != null) {
                	mOnSlideToEndListener.onSlideToEnd();
                }
                mEnd = true;
                invalidate();
            } else if (position == COLLAPSED_FULL_CLOSED) {
            	if(mReverse) {
            		handle.offsetLeftAndRight(mTopOffset - handle.getLeft());
            	} else {
	                handle.offsetLeftAndRight(mBottomOffset + getRight() - getLeft() -
	                        mHandleWidth - handle.getLeft());
            	}
                invalidate();
            } else {
                final int left = handle.getLeft();
                int deltaX = position - left;
                if (position < mTopOffset) {
                    deltaX = mTopOffset - left;
                } else if (deltaX > mBottomOffset + getRight() - getLeft() - mHandleWidth - left) {
                    deltaX = mBottomOffset + getRight() - getLeft() - mHandleWidth - left;
                }
                handle.offsetLeftAndRight(deltaX);

                final Rect frame = mFrame;
                final Rect region = mInvalidate;

                handle.getHitRect(frame);
                region.set(frame);
                
                region.union(frame.left - deltaX, frame.top, frame.right - deltaX, frame.bottom);
                if(mReverse) {
                    region.union(frame.left - deltaX - mContent.getWidth(), 0,
                            frame.left - deltaX, getHeight());
                } else {
                    region.union(frame.right - deltaX, 0,
                            frame.right - deltaX + mContent.getWidth(), getHeight());
                }

                if(mEnd) {
                	mEnd = false;
                	if(mOnSlideToEndListener != null) {
                		mOnSlideToEndListener.onLeaveEnd();
                	}
                }
                
                invalidate(region);
            }
        }
    }

    private void prepareContent() {
        if (mAnimating) {
            return;
        }

        // Something changed in the content, we need to honor the layout request
        // before creating the cached bitmap
        final View content = mContent;
        if (content.isLayoutRequested()) {
            if (mVertical) {
                final int childHeight = mHandleHeight;
                int height = getBottom() - getTop() - (mFillContent ? 0 : childHeight)/* - childHeight*/ - mTopOffset;
                content.measure(MeasureSpec.makeMeasureSpec(getRight() - getLeft(), MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
                if(mReverse) {
                	content.layout(0, mTopOffset, content.getMeasuredWidth(),
                            mTopOffset + content.getMeasuredHeight());
                } else {
                	content.layout(0, mTopOffset + (mFillContent ? 0 : childHeight)/* + childHeight*/, content.getMeasuredWidth(),
                            mTopOffset + (mFillContent ? 0 : childHeight)/* + childHeight*/ + content.getMeasuredHeight());
                }
            } else {
                final int childWidth = mHandle.getWidth();
                int width = getRight() - getLeft() - (mFillContent ? 0 : childWidth)/* - childWidth*/ - mTopOffset;
                content.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(getBottom() - getTop(), MeasureSpec.EXACTLY));
                if(mReverse) {
                	content.layout(mTopOffset, 0, mTopOffset + content.getMeasuredWidth(),
                            content.getMeasuredHeight());
                } else {
                	content.layout((mFillContent ? 0 : childWidth) + /*childWidth + */mTopOffset, 0,
                            mTopOffset + (mFillContent ? 0 : childWidth)/* + childWidth*/ + content.getMeasuredWidth(),
                            content.getMeasuredHeight());
                }
            }
        }
        // Try only once... we should really loop but it's not a big deal
        // if the draw was cancelled, it will only be temporary anyway
        content.getViewTreeObserver().dispatchOnPreDraw();
        content.buildDrawingCache();

        content.setVisibility(View.GONE);        
    }

    private void stopTracking() {
        mHandle.setPressed(false);
        mTracking = false;

        if (mOnDrawerScrollListener != null) {
            mOnDrawerScrollListener.onScrollEnded();
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void doAnimation() {
        if (mAnimating) {
        	final int start = mBottomOffset + (mVertical ? getHeight() : getWidth()) - 1;
        	final int end = mTopOffset;
            incrementAnimation();
//            Logger.i(TAG, "Slider AnimationPosition" + mAnimationPosition);
            if (mAnimationPosition >= start) {
                mAnimating = false;
                if(mReverse) {
                	openDrawer();
                } else {
                	closeDrawer();
                }
            } else if (mAnimationPosition < end) {
                mAnimating = false;
                if(mReverse) {
                	closeDrawer();
                } else {
                	openDrawer();
                }
            } else {
                moveHandle((int) mAnimationPosition);
                mCurrentAnimationTime += ANIMATION_FRAME_DURATION;
                mHandler.sendMessageAtTime(mHandler.obtainMessage(MSG_ANIMATE),
                        mCurrentAnimationTime);
            }
        }
    }

    private void incrementAnimation() {
        long now = SystemClock.uptimeMillis();
        float t = (now - mAnimationLastTime) / 1000.0f;                   // ms -> s
        final float position = mAnimationPosition;
        final float v = mAnimatedVelocity;           // px/s
        final float a = mAnimatedAcceleration;       // px/s/s
        mAnimationPosition = position + (v * t) + (0.5f * a * t * t);     // px
        mAnimatedVelocity = v + (a * t);                                  // px/s
        mAnimationLastTime = now;                                         // ms
    }

    /**
     * Toggles the drawer open and close. Takes effect immediately.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #animateToggle()
     */
    public void toggle() {
        if (!mExpanded) {
            openDrawer();
        } else {
            closeDrawer();
        }
        invalidate();
        requestLayout();
    }

    /**
     * Toggles the drawer open and close with an animation.
     *
     * @see #open()
     * @see #close()
     * @see #animateClose()
     * @see #animateOpen()
     * @see #toggle()
     */
    public void animateToggle() {
        if (!mExpanded) {
            animateOpen();
        } else {
            animateClose();
        }
    }

    /**
     * Opens the drawer immediately.
     *
     * @see #toggle()
     * @see #close()
     * @see #animateOpen()
     */
    public void open() {
        openDrawer();
        invalidate();
        requestLayout();

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);
    }

    /**
     * Closes the drawer immediately.
     *
     * @see #toggle()
     * @see #open()
     * @see #animateClose()
     */
    public void close() {
        closeDrawer();
        invalidate();
        requestLayout();
    }

    /**
     * Closes the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateOpen()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateClose() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateClose(mVertical ? mHandle.getTop() : mHandle.getLeft());

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    /**
     * Opens the drawer with an animation.
     *
     * @see #close()
     * @see #open()
     * @see #animateClose()
     * @see #animateToggle()
     * @see #toggle()
     */
    public void animateOpen() {
        prepareContent();
        final OnDrawerScrollListener scrollListener = mOnDrawerScrollListener;
        if (scrollListener != null) {
            scrollListener.onScrollStarted();
        }
        animateOpen(mVertical ? mHandle.getTop() : mHandle.getLeft());

        sendAccessibilityEvent(AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED);

        if (scrollListener != null) {
            scrollListener.onScrollEnded();
        }
    }

    private void closeDrawer() {
        moveHandle(COLLAPSED_FULL_CLOSED);
        mContent.setVisibility(View.GONE);
        mContent.destroyDrawingCache();

        if (!mExpanded) {
            return;
        }

        mExpanded = false;
        if (mOnDrawerCloseListener != null) {
            mOnDrawerCloseListener.onDrawerClosed();
        }
    }

    private void openDrawer() {
        moveHandle(EXPANDED_FULL_OPEN);
        mContent.setVisibility(View.VISIBLE);

        if (mExpanded) {
            return;
        }

        mExpanded = true;

        if (mOnDrawerOpenListener != null) {
            mOnDrawerOpenListener.onDrawerOpened();
        }
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes open.
     *
     * @param onDrawerOpenListener The listener to be notified when the drawer is opened.
     */
    public void setOnDrawerOpenListener(OnDrawerOpenListener onDrawerOpenListener) {
        mOnDrawerOpenListener = onDrawerOpenListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer becomes close.
     *
     * @param onDrawerCloseListener The listener to be notified when the drawer is closed.
     */
    public void setOnDrawerCloseListener(OnDrawerCloseListener onDrawerCloseListener) {
        mOnDrawerCloseListener = onDrawerCloseListener;
    }

    /**
     * Sets the listener that receives a notification when the drawer starts or ends
     * a scroll. A fling is considered as a scroll. A fling will also trigger a
     * drawer opened or drawer closed event.
     *
     * @param onDrawerScrollListener The listener to be notified when scrolling
     *        starts or stops.
     */
    public void setOnDrawerScrollListener(OnDrawerScrollListener onDrawerScrollListener) {
        mOnDrawerScrollListener = onDrawerScrollListener;
    }

    public void setOnSlideToEndListener(OnSlideToEndListener onSlideToEndListener) {
		mOnSlideToEndListener = onSlideToEndListener;
	}

	/**
     * Returns the handle of the drawer.
     *
     * @return The View reprenseting the handle of the drawer, identified by
     *         the "handle" id in XML.
     */
    public View getHandle() {
        return mHandle;
    }

    /**
     * Returns the content of the drawer.
     *
     * @return The View reprenseting the content of the drawer, identified by
     *         the "content" id in XML.
     */
    public View getContent() {
        return mContent;
    }

    /**
     * Unlocks the SliderView so that touch events are processed.
     *
     * @see #lock() 
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the SliderView so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }

    /**
     * Indicates whether the drawer is currently fully opened.
     *
     * @return True if the drawer is opened, false otherwise.
     */
    public boolean isOpened() {
        return mExpanded;
    }

    /**
     * Indicates whether the drawer is scrolling or flinging.
     *
     * @return True if the drawer is scroller or flinging, false otherwise.
     */
    public boolean isMoving() {
        return mTracking || mAnimating;
    }

    private class DrawerToggler implements OnClickListener {
        public void onClick(View v) {
            if (mLocked) {
                return;
            }
            // mAllowSingleTap isn't relevant here; you're *always*
            // allowed to open/close the drawer by clicking with the
            // trackball.

            if (mAnimateOnClick) {
                animateToggle();
            } else {
                toggle();
            }
        }
    }

    private static class SlidingHandler extends Handler {
    	
    	private WeakReference<SliderView> mReference = null;
    	
    	public SlidingHandler(SliderView view) {
    		mReference = new WeakReference<SliderView>(view);
    	}
    	
        public void handleMessage(Message m) {
        	SliderView view = mReference.get();
        	if(view == null) {
        		return;
        	}
            switch (m.what) {
                case MSG_ANIMATE:
                    view.doAnimation();
                    break;
            }
        }
    }
}
