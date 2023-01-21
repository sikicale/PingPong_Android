package com.hypersoft.pingpong;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Random;
import java.util.logging.Level;

public class MainActivity extends AppCompatActivity {

    private Button paddle,leftButton,rightButton,ball;
    private int screenWidth,screenHeight;

    private boolean continueIncrementing;

    private int xVelocity;
    private int yVelocity;
    private int initialSpeed = 2;
    private CharSequence text = "Izgubio si od Androida :)";
    private int duration = Toast.LENGTH_SHORT;
    private Toast toast;

    Random random;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rightButton = (Button) findViewById(R.id.right);
        leftButton = (Button) findViewById(R.id.left);
        paddle = (Button) findViewById(R.id.button);
        ball = (Button) findViewById(R.id.ball);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;//širina ekrana u pikselima
        screenHeight = displayMetrics.heightPixels;// visina ekrana u pikselima
        toast = Toast.makeText(this,text,duration);



        View.OnTouchListener handleTouch = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {   //kad stisneš dugme ide desno ili levo padle
                    if (view.getId() == R.id.right)
                        rightIncrementing();
                    else
                        leftDecrementing();
                }else if(motionEvent.getAction() == MotionEvent.ACTION_UP) {  //kad pistiš digme stane kretanje padle
                    stopIncrementing();
                }

                return false;
            }
        };
        rightButton.setOnTouchListener(handleTouch);
        leftButton.setOnTouchListener(handleTouch);
        direction_ball();
        ballMoving();
    }
   private void direction_ball(){
        random = new Random();
        int randomXDirection = random.nextInt(2);//-1 kreće se gore a 1 kreće se dole
        if (randomXDirection==0)
            randomXDirection--;
       setXDirection(randomXDirection*initialSpeed);

       int randomYDirection = random.nextInt(2);
       if(randomYDirection==0)
           randomYDirection--;
       setYDirection(randomYDirection*initialSpeed);

    }
    synchronized private void setXDirection(int randomXDirection){
        xVelocity = randomXDirection;
        notify();
    }
    synchronized private int getXDirection(){return xVelocity;}
    synchronized private void setYDirection(int randomYDirection){
        yVelocity = randomYDirection;
        notify();
    }
    synchronized private int getYDirection(){return yVelocity;}

    synchronized private void move(){
        ball.setLeft(ball.getLeft()+getXDirection());
        ball.setRight(ball.getRight()+getXDirection());

        ball.setTop(ball.getTop()+getYDirection());
        ball.setBottom(ball.getBottom()+getYDirection());
        notify();
    }
    private void ballMoving(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.interrupted()){
                    try {
                        Thread.sleep(3);
                        synchronized (this){while(ball == null) wait();}
                        move();
                        if (ball.getBottom() <= 50)
                            setYDirection(-getYDirection());
                        synchronized (this){while(paddle == null) wait();}
                        // dodir padle i lopte
                        if ((ball.getBottom() >= paddle.getBottom()) && (((ball.getLeft()>= paddle.getLeft())&&(ball.getLeft()<= paddle.getRight())) || ((ball.getRight()<= paddle.getRight())&&(ball.getRight()>= paddle.getLeft()))))
                            setYDirection(-getYDirection());
                        //izgubio, resetuj
                        if (ball.getTop() >= screenHeight-250){
                            toast.show();
                            setYDirection(-getYDirection());
                        }


                        if (ball.getLeft() <= 0)
                            setXDirection(-getXDirection());
                        if (ball.getRight() >= screenWidth)
                            setXDirection(-getXDirection());
                    }catch (InterruptedException e) {
                        Log.e(TAG, "Uncaught exception,loptica", e);// mora ovako ili izaziva prekid(e.printStackTrace();)
                    }
                }
            }
        }).start();
    }
    private void rightIncrementing(){
        setIsIncrementing(true);
        new Thread(new Runnable() { // nit koja se stalno vrti
            @Override
            public void run() {
                while (isIncrementing()){
                    if (paddle.getRight()<screenWidth) {    // pomera padle desno
                        try {
                            Thread.sleep(1);
                            paddle.setTop(paddle.getTop());
                            paddle.setBottom((paddle.getBottom()));
                            paddle.setLeft(paddle.getLeft() + 1);
                            paddle.setRight(paddle.getRight() + 1);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Uncaught exception, desno dugme", e);
                        }
                    }
                }
            }
        }).start();
    }
    private void leftDecrementing(){
        setIsIncrementing(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isIncrementing()){
                    if (paddle.getLeft()>0) {   // pomera padle levo
                        try {
                            Thread.sleep(1);
                            paddle.setTop(paddle.getTop());
                            paddle.setBottom((paddle.getBottom()));
                            paddle.setLeft(paddle.getLeft() - 1);
                            paddle.setRight(paddle.getRight() - 1);
                        } catch (InterruptedException e) {
                            Log.e(TAG, "Uncaught exception, levo dugme", e);
                        }
                    }
                }
            }
        }).start();
    }
    synchronized private void stopIncrementing(){
        setIsIncrementing(false);
        notify();
    }


    synchronized private boolean isIncrementing(){ return continueIncrementing;  }

    synchronized void setIsIncrementing(boolean newSetting){
        continueIncrementing = newSetting;
        notify();
    }

}