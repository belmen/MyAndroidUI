package belmen.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

/**
 * 大图浏览
 * 实现了多点触碰、拖动、两指缩放、双击缩放
 * (目前该控件适合全屏使用)
 * @author Robot
 */
public class TouchView extends ImageView {
	static final int NONE = 0;
    static final int DRAG = 1;	   //拖动中
    static final int ZOOM = 2;     //缩放中
    static final int BIGGER = 3;   //放大ing
    static final int SMALLER = 4;  //缩小ing
    private int mode = NONE;	   //当前的事件 

    private float beforeLenght;   //两触点距离
    private float afterLenght;    //两触点距离
    private float scale = 0.04f;  //缩放的比例 X Y方向都是这个值 越大缩放的越快
   
    private int screenW;
    private int screenH;
    
    /*处理拖动 变量 */
    private int start_x;
    private int start_y;
	private int stop_x ;
	private int stop_y ;
	
    private TranslateAnimation trans; //处理超出边界的动画
	private ScaleAnimation scaleAnim; //放大缩小动画
	
	private long mTimeOfFirstPointerDown;//前一次按下时间
	private long mTimeOfLastClick;//后一次离开时间
	
    public TouchView(Context context) {
		super(context);
		init(context);
	}
	
    /**
	 * @param context
	 * @param attrs
	 */
	public TouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	private void init(Context context) {
		Log.d("TouchView", "----------------------------init[Start]-------------------------------");
		this.setPadding(0, 0, 0, 0);
		this.setScaleType(ScaleType.CENTER_INSIDE);
		this.setAdjustViewBounds(true);
		this.setBackgroundColor(Color.alpha(122));
		screenW = ((Activity)context).getWindowManager().getDefaultDisplay().getWidth();
		screenH = ((Activity)context).getWindowManager().getDefaultDisplay().getHeight();
		Log.d("TouchView", "Screen Width: " + screenW + " Height: " + screenH);
		Log.d("TouchView", "----------------------------init[End]-------------------------------");
	}
	
	/**
	 * 两点间的距离
	 */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return FloatMath.sqrt(x * x + y * y);
    }
    
    /**
     * 处理触碰
     */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{	
		Log.d("TouchView", "Mode Type: " + mode);
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
        		mTimeOfFirstPointerDown = System.currentTimeMillis();
        		mode = DRAG;
    	    	stop_x = (int) event.getRawX();
    	    	stop_y = (int) event.getRawY();
        		start_x = (int) event.getX();
            	start_y = stop_y - this.getTop();
            	if(event.getPointerCount()==2)
            		beforeLenght = spacing(event);
                break;
        case MotionEvent.ACTION_POINTER_DOWN:
        		mTimeOfFirstPointerDown = mTimeOfLastClick = 0;
                if (spacing(event) > 10f) {
                        mode = ZOOM;
                		beforeLenght = spacing(event);
                }
                break;
        case MotionEvent.ACTION_UP:
				if (System.currentTimeMillis() - mTimeOfFirstPointerDown <= 300){		       		
		       		if (mTimeOfLastClick > 0  &&  
		       			System.currentTimeMillis() - mTimeOfLastClick <= 300){
		       			//双击
		       			Log.d("TouchView", "=======DoubleClick=======");
		       			//双击时, 让图片大小超过屏幕
		       			int maxWidth = screenW + 500;
		       			int maxHeight = screenH + 500;
		       			if(getWidth() <= maxWidth && getHeight() <= maxHeight) {
		       				//图片小于屏幕, 自动放大
			       			while(getWidth() < maxWidth || getHeight() < maxHeight) {
			    				setScale(scale, BIGGER);
			    			}
		       			} else {
		       				//图片大于屏幕, 自动缩小
		       				while(getWidth() > screenW || getHeight() > screenH) {
		        				setScale(scale, SMALLER);
		        			}
		       			}
		       			mTimeOfLastClick = 0;
		       		} else {
		       			mTimeOfLastClick = System.currentTimeMillis();
		       			//单击
		       			Log.d("TouchView", "=======SingleClick=======");
		       		}
			    }
        		/*判断是否超出范围     并处理*/
        		int disX = 0;
        		int disY = 0;
        		//图片宽高
        		Log.d("TouchView", "Image Width: " + getWidth() + " Height: " + getHeight());
	        	if(getHeight() <= screenH) {
		        	if(this.getTop()<0)
		        	{
		        		int dis = getTop();
	                	this.layout(this.getLeft(), 0, this.getRight(), 0 + this.getHeight());
	            		disY = dis - getTop();
		        	}
		        	else if(this.getBottom()>screenH)
		        	{
		        		disY = getHeight()- screenH+getTop();
	                	this.layout(this.getLeft(), screenH-getHeight(), this.getRight(), screenH);
		        	}
	        	} else {
	        		if(this.getTop() < 0 && this.getBottom() < screenH) {
	        			disY = getHeight()- screenH+getTop();
	        			this.layout(this.getLeft(), screenH-getHeight(), this.getRight(), screenH);
	        		} else if(this.getTop() > 0 && this.getBottom() > screenH) {
	        			int dis = getTop();
	        			this.layout(this.getLeft(), 0, this.getRight(), 0 + this.getHeight());
	        			disY = dis - getTop();
	        		}
	        	}
	        	if(getWidth() <= screenW)
	        	{
		        	if(this.getLeft()<0)
		        	{
		        		disX = getLeft();
	                	this.layout(0, this.getTop(), 0+getWidth(), this.getBottom());
		        	}
		        	else if(this.getRight()>screenW)
		        	{
		        		disX = getWidth()-screenW+getLeft();
	                	this.layout(screenW-getWidth(), this.getTop(), screenW, this.getBottom());
		        	}
	        	} else {
	        		Log.d("TouchView", "Left: " + getLeft() + " Right: " + getRight());
	        		if(this.getLeft() > 0 && this.getRight() > screenW) {
	        			disX = getLeft();
	                	this.layout(0, this.getTop(), 0+getWidth(), this.getBottom());
	        		} else if(this.getLeft() < 0 && this.getRight() < screenW) {
	        			disX = getWidth()-screenW+getLeft();
	                	this.layout(screenW-getWidth(), this.getTop(), screenW, this.getBottom());
	        		}
	        	}
	        	if(disX!=0 || disY!=0)
	        	{
            		trans = new TranslateAnimation(disX, 0, disY, 0);
            		trans.setDuration(300);
            		this.startAnimation(trans);
	        	}
	        	mode = NONE;
        		break;
        case MotionEvent.ACTION_POINTER_UP:
        		Log.d("TouchView","-------ACTION_POINTER_UP-------");
    			//图片缩的比屏幕小,自动放大
    			while(getWidth() < screenW && getHeight() < screenH) {
    				setScale(scale, BIGGER);
    			}
                mode = NONE;
                break;
        case MotionEvent.ACTION_MOVE:
        		/*处理拖动*/
                if (mode == DRAG) {
                	if(Math.abs(stop_x-start_x-getLeft())<88 && Math.abs(stop_y - start_y-getTop())<85)
                	{
                    	this.setPosition(stop_x - start_x, stop_y - start_y, stop_x + this.getWidth() - start_x, stop_y - start_y + this.getHeight());           	
                    	stop_x = (int) event.getRawX();
                    	stop_y = (int) event.getRawY();
                	}
                }
                /*处理缩放*/
                else if (mode == ZOOM) {
                	if(spacing(event)>10f)
                	{
                        afterLenght = spacing(event);
                        float gapLenght = afterLenght - beforeLenght;                     
                        if(gapLenght == 0) {  
                           break;
                        }
                        else if(Math.abs(gapLenght)>5f)
                        {
                            if(gapLenght>0) { 
                                this.setScale(scale,BIGGER);   
                            }else {  
                                this.setScale(scale,SMALLER);   
                            }                             
                            beforeLenght = afterLenght; 
                        }
                	}
                }
                break;
        }
        return true;	
	}
	
	/**
	 * 实现处理缩放
	 */
	private void setScale(float temp, int flag) {
		if (flag == BIGGER) {
			this.setFrame(this.getLeft() - (int) (temp * this.getWidth()),
					this.getTop() - (int) (temp * this.getHeight()),
					this.getRight() + (int) (temp * this.getWidth()),
					this.getBottom() + (int) (temp * this.getHeight()));
//			scaleAnim = new ScaleAnimation(1.0f, 2.0f, 1.0f, 2.0f);
		} else if (flag == SMALLER) {
//			if((getRight() - getLeft() - (int) (2*temp * getWidth())) >= screenW) {
				this.setFrame(this.getLeft() + (int) (temp * this.getWidth()),
						this.getTop() + (int) (temp * this.getHeight()),
						this.getRight() - (int) (temp * this.getWidth()),
						this.getBottom() - (int) (temp * this.getHeight()));
//			}
//			scaleAnim = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f);
		}
		//scaleAnim = new ScaleAnimation(1.0f, 1.1f, 1.0f, 1.1f, Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f);
//		scaleAnim.setFillAfter(true);
//		scaleAnim.setDuration(2000);
//		this.startAnimation(scaleAnim);
	}
    
	/**
	 * 实现处理拖动
	 */
    private void setPosition(int left,int top,int right,int bottom) {  
    	this.layout(left,top,right,bottom);           	
    }

}
