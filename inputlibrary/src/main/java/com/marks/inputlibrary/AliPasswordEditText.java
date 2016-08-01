package com.marks.inputlibrary;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.List;

/**
 * @author marks.luo
 *         Alipay or  wechat pay  password input view
 *         (仿支付宝或微信支付密码输入框)
 */
public class AliPasswordEditText extends View {
    private List<Integer> resultList;//结果集合
    private int count;//密码个数
    private InputMethodManager inputMethodManager;//输入法管理
    private int size;//默认每格的大小
    private Paint mBorderPaint;//边界画笔
    private Paint mDotPaint;//点的画笔
    private Paint mLinePaint;//线的画笔
    private int mBorderColor;//边界颜色
    private int mDotColor;//点的颜色
    private int mLineColor;//分割线的颜色
    private int mRoundRadius;//圆角矩形的角度
    private RectF mRoundRectF;//外层的圆角矩形
    private InputCallBack inputCallBack;//输入完成监听
    private static final int NUMBER_ONE = -1;
    private static final int DEFAULT_COUNT = 6;//defaut count
    private static final int DEFAULT_LATTICE_LENGHT = 30;//默认每格的长度
    private static final int DEFAULT_RADIUS = 2;//默认每格的长度

    public AliPasswordEditText(Context context) {
        this(context, null);
    }

    public AliPasswordEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AliPasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        this.setFocusable(true);//设置获取焦点
        this.setFocusableInTouchMode(true);//设置获取焦点模式
        inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        resultList = new ArrayList<>();
        mRoundRectF = new RectF();

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AliPasswordEditText, defStyleAttr, 0);
        count = a.getInt(R.styleable.AliPasswordEditText_payEdit_count, DEFAULT_COUNT);//获取显示圆点个数
        mBorderColor = a.getColor(R.styleable.AliPasswordEditText_payEdit_backgroud, Color.LTGRAY);//获取背景颜色
        mLineColor = a.getColor(R.styleable.AliPasswordEditText_payEdit_lineColor, Color.GREEN);//获取分割线颜色
        mDotColor = a.getColor(R.styleable.AliPasswordEditText_payEdit_dotColor, Color.GRAY);//获取圆点颜色
        size = a.getDimensionPixelOffset(R.styleable.AliPasswordEditText_payEdit_latticeLenght, DEFAULT_LATTICE_LENGHT);//获取每格的边长
        mRoundRadius = a.getDimensionPixelOffset(R.styleable.AliPasswordEditText_payEdit_cornersRadius, DEFAULT_RADIUS);//获取外层边框弧度
        a.recycle();
        initPaint();//初始化画笔
        this.setOnKeyListener(new MyKeyListener());//设置键盘监听
    }


    //初始化画笔
    private void initPaint() {
        mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBorderPaint.setStrokeWidth(4);//设置画笔宽度
        mBorderPaint.setStyle(Paint.Style.FILL);
        mBorderPaint.setColor(mBorderColor);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStrokeWidth(2);
        mLinePaint.setStyle(Paint.Style.FILL);
        mLinePaint.setColor(mLineColor);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStrokeWidth(2);//设置画笔宽度
        mDotPaint.setStyle(Paint.Style.FILL);
        mDotPaint.setColor(mDotColor);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量view
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);
        int w = measuredWidth(widthMeasureSpec);
        int h = measuredHeight(heightMeasureSpec);
        /**
         *                              未指定高度（使用默认的格子大小<size>,使用默认的高度，计算宽度）
         *                 未指定宽度--
         *                              已指定高度(使用高度动态计算宽度，设置每个格子高度。)
         *
         * 是否指定宽度---
         *                 已指定宽度--高度不知道（动态计算高度，设置每个格子的大小）；
         */

        if (w == -1) {//宽度没有指定
            if (h == -1) {//宽和高都不知道，使用默认的size和个数
                w = size * count;
                h = size;
            } else {
                w = h * count;//宽=高*count
                size = h;
            }
        } else {//宽度已知
            if (h == -1) {
                h = w / count;
                size = h;
            }
        }
        setMeasuredDimension(Math.min(w, wsize), Math.min(h, hsize));
    }

    private int measuredWidth(int widthMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        //判断当前宽度设置的mode是否包裹内容（wrap_content）
        return widthMode == MeasureSpec.AT_MOST ? NUMBER_ONE :
                widthSize;
    }

    private int measuredHeight(int heightMeasureSpec) {
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        //判断高度是否为包裹内容(wrap_content)
        return heightMode == MeasureSpec.AT_MOST ? NUMBER_ONE :
                heightSize;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth() - 2;
        int height = getHeight() - 2;
        //画圆角矩阵
        mRoundRectF.set(0, 0, width, height);
        canvas.drawRoundRect(mRoundRectF, mRoundRadius, mRoundRadius, mBorderPaint);
        //画分割线
        drawLine(canvas);
        //画点
        drawDot(canvas);
    }

    private void drawDot(Canvas canvas) {
        //获取圆的半径
        int dotRadius = size / 6;//圆点占每个格子1/3
        for (int i = 0; i < resultList.size(); i++) {
            //计算点的x.y的坐标
            float x = (float) (size * (i + 0.5));//0.5是线的宽度
            float y = size / 2;//size是每个格子的边
            canvas.drawCircle(x, y, dotRadius, mDotPaint);
        }
    }

    private void drawLine(Canvas canvas) {
        for (int i = 1; i < count; i++) {
            int x = i * size;//计算x的位置
            //绘制线条应该减去边框的高度
            canvas.drawLine(x, 0, x, getHeight() - 2, mLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //重写触摸监听
        if (event.getAction() == MotionEvent.ACTION_DOWN) {//点击控件弹出软件盘
            //设置获取焦点
            requestFocus();
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
        } else {
            inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }

    @Override//用户点击home键，获取切换到后台关闭软键盘
    public void onWindowFocusChanged(boolean hasWindowFocus) {//view 焦点改变监听
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus()) {
            inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
        }
    }


    /**
     * 自定义keyListener
     */
    class MyKeyListener implements OnKeyListener {
        @Override
        public boolean onKey(View view, int code, KeyEvent keyEvent) {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {//监听键盘输入
                Log.i("lbxx", "down----code==" + code);
                if (code >= KeyEvent.KEYCODE_0 && code <= KeyEvent.KEYCODE_9) {//只处理数字
                    if (resultList.size() < count) {//判断是否输入最大输入值
                        resultList.add(code - 7);//-7是因为0的按钮是keycode值为7
                        invalidate();
                        ensureFinishInput();
                    }
                    return true;
                }
                if (code == KeyEvent.KEYCODE_DEL) {//判断是否点击删除
                    if (!resultList.isEmpty()) {
                        resultList.remove(resultList.size() - 1);//删除数据
                        invalidate();
                    }
                    return true;
                }
                if (code == KeyEvent.KEYCODE_ENTER) {//是否点击Enter按钮
                    ensureFinishInput();//检测是否输入完
                    return true;
                }
            }
            return false;
        }
    }

    //判断用户输入输入完,输入完后调用callback
    private void ensureFinishInput() {
        if (resultList.size() == count && inputCallBack != null) {
            StringBuilder builder = new StringBuilder();
            for (int res : resultList) {
                builder.append(res);
            }
            inputCallBack.onInputFinish(builder.toString());
        }
    }

    //设置键盘的输入类型
    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//输入类型为数字
        outAttrs.imeOptions = EditorInfo.IME_ACTION_DONE;//设置键盘右下角的图标
        return new MyInputconnection(this, false);
    }

    class MyInputconnection extends BaseInputConnection {

        public MyInputconnection(View targetView, boolean fullEditor) {
            super(targetView, fullEditor);
        }

       /* @Override
        public boolean commitText(CharSequence text, int newCursorPosition) {
            return true;
            return super.commitText(text, newCursorPosition);
        }*/

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {
            //软键盘的删除键del 无法直接监听，自己发送del事件
            if (beforeLength == 1 && afterLength == 0) {
                return super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                        && super.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    public void setInputCallBack(InputCallBack inputCallBack) {
        this.inputCallBack = inputCallBack;
    }

    /**
     * 输入完成后的回调
     */
    public interface InputCallBack {
        void onInputFinish(String result);
    }
}
