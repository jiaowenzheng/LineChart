package com.android.line.chart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by wenzheng on 2015/11/4.
 */
public class LineChartView extends View {

    private int width;                  //宽度
    private int height;                 //高度
    private int maxValue = 100;         //Y轴最大高度
    private int previousX;              //上一次X轴的坐标
    private int previousY;              // 上一次Y轴的坐标
    private int[] price = new int[6];   //随机值
    private int mRadius = 10;           //原点半径
    private int mSpace = 10;            //空间

    private Paint mTextPaint;           //文本画笔
    private Paint mLinePaint;           //折线画笔
    private Paint mPointPaint;          //点画笔

    private String[] mShopName = new String[]{"京东全球购","天猫精选","美国亚马逊","日本亚马逊","京东商城","优购网"};
    private Rect mTextBound = new Rect();

    private static final String  BOTTOM_LINE_COLOR = "#9DAFC9";     //底部横线的颜色
    private static final String  CENTER_LINE_COLOR = "#FCD3EF";     //中间横线的颜色
    private static final String  POLY_LINE_COLOR = "#F89E4F";       //折线的颜色

    private int mPriceTextSize = 28;           //价格文本大小
    private int mShopNameTextSize = 40;        //底部商城文本大小

    private int mTextStrokeWidth = 2;          //文本画笔宽度
    private int mPointStrokeWidth = 15;        //点画笔的宽度

    private Rect[] mRectArray;
    private boolean isClickText = false;
    private int mClickNamePosition = 0;
    private ShoppingNameClickListener mListener;


    public static final int[] LIBERTY_COLORS = {
            Color.rgb(232, 131, 123), Color.rgb(254, 157, 86), Color.rgb(248, 179, 52),
            Color.rgb(252, 206, 183), Color.rgb(140, 192, 252), Color.rgb(92, 147, 214)};



    public LineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LineChartView(Context context) {
        super(context);
    }


    public void init(){

        mRectArray = new Rect[6];

        for (int i = 0;i < 6;i++){
            price[i] = (int) (Math.random() * 100);
        }

        //Text Paint
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setStrokeWidth(mTextStrokeWidth);

        // Point Paint
        mPointPaint = new Paint();
        mPointPaint.setColor(Color.RED);
        mPointPaint.setStyle(Paint.Style.FILL);
        mPointPaint.setStrokeWidth(mPointStrokeWidth);

        //Line Paint
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setStrokeWidth(mTextStrokeWidth);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = w;
        height = h * 2 / 3 ;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //画布背景
        canvas.drawColor(Color.WHITE);
        //底部x轴线
        mLinePaint.setColor(Color.parseColor(BOTTOM_LINE_COLOR));
        canvas.drawLine(0, height, width, height, mLinePaint);

        //中间粉红色的线
        mLinePaint.setColor(Color.parseColor(CENTER_LINE_COLOR));
        int secondHeight = height / 2;
        int centerStartY = height - secondHeight / 2;
        canvas.drawLine(0, centerStartY, width, centerStartY, mLinePaint);

        int space = width / price.length;
        int offset = space  / 2;
        for (int i = 0; i < price.length; i++){
            int startX = offset + space * i;
            //根据价格计算点的位置
            int pointY = height - price[i] * secondHeight / maxValue;
            int color = LIBERTY_COLORS[i % LIBERTY_COLORS.length];

            //设置线及点的颜色
            mLinePaint.setColor(color);
            mPointPaint.setColor(color);
            //绘制竖线的长度
            canvas.drawLine(startX, pointY, startX, height, mLinePaint);

            drawLineAndCirclePoint(canvas, i, startX, pointY);
            drawLinePriceText(canvas, i, startX, pointY);
            drawBottomText(canvas,i,startX);
        }

    }

    /**
     * 绘制折线和圆点
     *
     * @param canvas
     * @param i
     * @param startX
     * @param pointY
     */
    public void drawLineAndCirclePoint(Canvas canvas,int i,int startX,int pointY){
        if(i > 0){
            //先绘制线再绘制点
            mLinePaint.setColor(Color.parseColor(POLY_LINE_COLOR));
            canvas.drawLine(previousX,previousY,startX,pointY,mLinePaint);
            //绘制点
            canvas.drawCircle(previousX, previousY, mRadius, mPointPaint);
            if(i == (price.length - 1)){
                canvas.drawCircle(startX,pointY, mRadius, mPointPaint);
            }
        }
    }

    /**
     * 绘制拆线上的价格文字
     *
     * @param canvas
     * @param i
     * @param startX
     * @param pointY
     */
    public void drawLinePriceText(Canvas canvas, int i, int startX, int pointY){
        //绘制价格，当前价格大于前一个和后一个价格的值，则价格文本画在点的上面，否则下面
        mTextPaint.setTextSize(mPriceTextSize);
        String priceText = String.valueOf(price[i]);
        float priceTextCenter = mTextPaint.measureText(priceText) / 2;
        int topSpace = mRadius * 2;
        int bottomSpace = mRadius * 4;
        if(i == 0){
            if(price[i] >= price[i+1]){ //drawText top
                canvas.drawText(priceText,startX - priceTextCenter,pointY - topSpace,mTextPaint);
            }else{                      // drawText bottom
                canvas.drawText(priceText,startX - priceTextCenter,pointY + bottomSpace,mTextPaint);
            }
        }else if(i == price.length -1){
            if(price[i] >= price[i-1]){ //drawText top
                canvas.drawText(priceText,startX - priceTextCenter,pointY - topSpace,mTextPaint);
            }else{                      // drawText bottom
                canvas.drawText(priceText,startX - priceTextCenter,pointY + bottomSpace,mTextPaint);
            }
        }else{
            if(price[i -1] <= price[i] && price[i] >= price[i+1]){ //drawText top
                canvas.drawText(priceText,startX - priceTextCenter,pointY - topSpace,mTextPaint);
            }else{                      // drawText bottom
                canvas.drawText(priceText,startX - priceTextCenter,pointY + bottomSpace,mTextPaint);
            }
        }

        //记录上一个点的 x,y 坐标
        previousX = startX;
        previousY = pointY ;
    }


    /**
     * 绘制底部商城的文字，记录坐标区域
     *
     * @param canvas
     * @param i
     * @param startX
     */
    public void drawBottomText(Canvas canvas,int i,int startX){
        String text = mShopName[i];
        //获取底部文本的Rect
        mTextPaint.getTextBounds(text, 0, text.length(), mTextBound);

        int textHeight = height + mTextBound.height() + mSpace * 2;
        mTextPaint.setTextSize(mShopNameTextSize);
        Rect rect = new Rect();
        for (int j = 0;j < text.length();j++){
            String str = String.valueOf(text.toCharArray()[j]);
            int textWidth = (int) mTextPaint.measureText(str);
            int textStartX = startX - textWidth / 2;
            canvas.drawText(str,textStartX,textHeight,mTextPaint);
            textHeight += mTextBound.height() + mSpace;
            //记录底部每一个商城的坐标区域Rect 用业判断点击区域
            if(j == 0){
                rect.left = textStartX;
                rect.top = textHeight;
                rect.right = textStartX + textWidth;
            }
        }

        rect.bottom = textHeight;
        mRectArray[i] = rect;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                int x = (int)event.getX();
                int y = (int)event.getY();
                hitArea(x,y);
                break;
            case MotionEvent.ACTION_UP:
                if(isClickText){
                    Toast.makeText(getContext(), mShopName[mClickNamePosition], Toast.LENGTH_SHORT).show();
                    if(mListener != null){
                        mListener.onShopNameClick(mClickNamePosition);
                    }
                }
                break;
        }

        return true;
    }


    /**
     * 判断点击区域
     *
     * @param x
     * @param y
     */
    public void hitArea(int x,int y){

        isClickText = false;
        for (int i = 0;i < mRectArray.length;i++){
            Rect rect = mRectArray[i];

            if(rect.contains(x,y)){
                isClickText = true;
                mClickNamePosition = i;
                break;
            }
        }
    }


    public interface ShoppingNameClickListener{
        void onShopNameClick(int position);
    }




}
