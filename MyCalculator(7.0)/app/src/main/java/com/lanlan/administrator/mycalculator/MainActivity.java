package com.lanlan.administrator.mycalculator;

import android.annotation.SuppressLint;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity {

    private SoundPool soundPool;//声明一个SoundPool
    private int soundID;//创建某个声音对应的音频ID
    private final int N = 200;
    private final String ERROR = "Error";
    private Button but_0;
    private Button but_1;
    private Button but_2;
    private Button but_3;
    private Button but_4;
    private Button but_5;
    private Button but_6;
    private Button but_7;
    private Button but_8;
    private Button but_9;
    private Button but_div;
    private Button but_mul;
    private Button but_sub;
    private Button but_add;
    private Button but_del;
    private Button but_cls;
    private Button but_equ;
    private Button but_poi;
    private Button but_lef;
    private Button but_rig;
    private TextView outPut1;
    private TextView outPut;
    private String content = "";
    private String display = "";
    private String[] temp =new String[N]; //计算中转
    private int temp_index = 0;
    private int i= 0;
    private int j= 0;
    private String[] sysmbol = new String[N];
    private String[] result = new String[N];
    //初始化栈
    {
        result[0] ="";
        sysmbol[0] = "";
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @SuppressLint("NewApi")
    private void initSound() {
        soundPool = new SoundPool.Builder().build();
        soundID = soundPool.load(this, R.raw.about, 1);
    }
    private void playSound() {
        soundPool.play(
                soundID,
                0.1f,   //左耳道音量【0~1】
                0.5f,   //右耳道音量【0~1】
                0,     //播放优先级【0表示最低优先级】
                0,     //循环模式【0表示循环一次，-1表示一直循环，其他表示数字+1表示当前数字对应的循环次数】
                1     //播放速度【1是正常，范围从0~2】
        );
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        but_0 = (Button)findViewById(R.id.but_0);
        but_1 = (Button)findViewById(R.id.but_1);
        but_2 = (Button)findViewById(R.id.but_2);
        but_3 = (Button)findViewById(R.id.but_3);
        but_4 = (Button)findViewById(R.id.but_4);
        but_5 = (Button)findViewById(R.id.but_5);
        but_6 = (Button)findViewById(R.id.but_6);
        but_7 = (Button)findViewById(R.id.but_7);
        but_8 = (Button)findViewById(R.id.but_8);
        but_9 = (Button)findViewById(R.id.but_9);
        but_del = (Button)findViewById(R.id.but_del);
        but_div = (Button)findViewById(R.id.but_div);
        but_add = (Button)findViewById(R.id.but_add);
        but_sub = (Button)findViewById(R.id.but_sub);
        but_mul = (Button)findViewById(R.id.but_mul);
        but_cls = (Button)findViewById(R.id.but_cls);
        but_equ = (Button)findViewById(R.id.but_equ);
        but_lef = (Button)findViewById(R.id.but_lef);
        but_rig = (Button)findViewById(R.id.but_rig);
        outPut = (TextView)findViewById(R.id.outPut);
        outPut1 = (TextView)findViewById(R.id.outPut1);
        but_poi =(Button)findViewById(R.id.but_point);
        ButtonListener listener = new ButtonListener();
        but_poi.setOnClickListener(listener);
        but_0.setOnClickListener(listener);
        but_1.setOnClickListener(listener);
        but_2.setOnClickListener(listener);
        but_3.setOnClickListener(listener);
        but_4.setOnClickListener(listener);
        but_5.setOnClickListener(listener);
        but_6.setOnClickListener(listener);
        but_7.setOnClickListener(listener);
        but_8.setOnClickListener(listener);
        but_9.setOnClickListener(listener);
        but_del.setOnClickListener(listener);
        but_div.setOnClickListener(listener);
        but_add.setOnClickListener(listener);
        but_sub.setOnClickListener(listener);
        but_equ.setOnClickListener(listener);
        but_cls.setOnClickListener(listener);
        but_mul.setOnClickListener(listener);
        but_lef.setOnClickListener(listener);
        but_rig.setOnClickListener(listener);
        initSound();
    }
    private class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            playSound();
            switch (v.getId()) {
                case R.id.but_0 :
                case R.id.but_1 :
                case R.id.but_2 :
                case R.id.but_3 :
                case R.id.but_4 :
                case R.id.but_5 :
                case R.id.but_6 :
                case R.id.but_7 :
                case R.id.but_8 :
                case R.id.but_9 :
                case R.id.but_point:
                   // if(content.charAt(0) == '+'||content.charAt(0) == '-')
                    display = display + ((TextView)v).getText();
                    content  = content + ((TextView)v).getText();
                    outPut.setText(display);
                    break;
                case R.id.but_add :
                case R.id.but_sub :
                case R.id.but_rig:
                case R.id.but_lef:
                case R.id.but_mul :
                case R.id.but_div :
                    display = display + ((TextView)v).getText();
                    if(content.length()>0)
                        if(content.charAt(content.length()-1) =='~' )
                        content  = content +((TextView)v).getText() + "~";
                        else
                        content  = content + "~" +((TextView)v).getText() + "~";
                    if((content.equals("")&&(((TextView)v).getText()).equals("-"))||
                    (content.equals("")&&(((TextView)v).getText()).equals("+")))
                        content = ((TextView)v).getText()+"";
                    outPut.setText(display);
                    break;

                case R.id.but_del :
                    if(!display.equals("")&&content.length()>0) {
                        content = content.substring(0, content.length() - 1);
                        display = display.substring(0, display.length() - 1);
                        outPut.setText(display);
                    }
                    break;
                case R.id.but_cls :
                    content = "";
                    temp_index =0;
                    display = "";
                    i=j=0;    //清空栈
                    outPut.setText("");
                    break;
                case R.id.but_equ:
                    if(content.equals(""))
                        break;
                    if(check()) {
                        content = content.replace("(~-~","(~-");
                        content = content.replace("(~+~","(~+");
                        content = content.replace("(~x~","(~x");
                        content = content.replace("(~÷~","(~÷");


                        outPut1.setText(display);
                       // String w = toLater(content);
                        try {
                            toLater(content);
                            calc();
                        }
                        catch (NumberFormatException e){
                            break;
                        }
                        //outPut.setText(w);
                        outPut.setText(result[1]);
                        display = result[1];
                        content = result[1];
                    }
                    break;
            }
        }

    }
    /**
     * 检查表达式是否合法
     */
  private boolean check(){
        if(content.contains("+~+")||content.contains("-~-")||
                content.contains("x~x")||content.contains("÷~÷")||
                content.contains("+~-")||content.contains("-~+")||
                content.contains("x~÷")||content.contains("÷~x")||
                content.contains("+~x")||content.contains("x~+")||
                content.contains("x~-")||content.contains("-~x")||
                content.contains("+~÷")||content.contains("÷~+")||
                content.contains("-~÷")||content.contains("÷~-")||
                content.contains(")~(")||content.contains("÷~0")){
            outPut1.setText(display);
            outPut.setText(ERROR);
            content = "";
            temp_index = 0;
            display = "";
            i = j = 0;
            return false;
        }
        else
            return true;
    }

    /**
     * 转换为后缀表达式
     * @param s 传入带有~的计算表达式 如 3~+~3
     * @return 后缀表达式
     */
    private String toLater(String s){
        String re = "";
        //初始化栈空间
        i = 0;
        j = 0;
        temp_index =0;
        int e = 0;
        String[] s1 = s.split("~");
        if(s.charAt(0) == '~')
            e =1;
        for (;e<s1.length;e++ ) {

            if(s1[e].equals("("))
                    push(2,s1[e]);
            else if(s1[e].equals(")")) {
                while (!sysmbol[j].equals("(") && j > 0) {
                    push(1, pop(2));
                }
                pop(2);
            }
            else if(s1[e].equals("+")||s1[e].equals("-")) {
                while (!sysmbol[j].equals("(")  && j > 0) {
                    push(1, pop(2));
                }
                push(2, s1[e]);
            }
            else if(s1[e].equals("x"))
                    push(2,"*");

            else if(s1[e].equals("÷"))
                    push(2,"/");
            else
             push(1,s1[e]);

        }
        for (int l = 1; l <= i; l++) {
                re += result[l];
                temp[temp_index++] = result[l];
            }
        for (; j > 0; j--) {
                temp[temp_index++] = sysmbol[j];
                re += sysmbol[j];
            }

        return  re;
    }

    /**
     * 计算后缀表达式
     */
    private void calc(){
        //清空栈
        i = 0;
        j = 0;
        for (int l = 0;l<temp_index;l++ ) {
            if (temp[l].equals("+")) {
                double l1,l2;
                l1 = Double.valueOf(pop(1));System.out.print(" "+i);

                l2 = Double.valueOf(pop(1));System.out.print(" "+i);

                push(1, Double.toString(l1+l2));System.out.print(" "+i);

            }
            else if (temp[l].equals("-")){
                double l1,l2;
                l1=Double.valueOf(pop(1));
                l2=Double.valueOf(pop(1));
                push(1,Double.toString(l2-l1));

            }
            else if (temp[l].equals("*")) {
                push(1, Double.toString(Double.valueOf(pop(1)) * Double.valueOf(pop(1))));

            }
            else if (temp[l].equals("/")) {
                double l1,l2;
                l1=Double.valueOf(pop(1));
                l2=Double.valueOf(pop(1));
                push(1,Double.toString(l2/l1));
            }
            else {
                push(1, temp[l]);

            }
            BigDecimal bg = new BigDecimal(Double.valueOf(result[1]));
            double f1 = bg.setScale(8, BigDecimal.ROUND_HALF_UP).doubleValue();
            result[1] =Double.toString(f1);

        }


    }
    /**
     * 栈模拟
     * @param s 压入的字符串
     */
    private void push(int o , String s){
        if(o == 1)
            result[++i] = s;
        else
            sysmbol[++j] = s;

    }
    private String pop(int o){

        if (o == 1)
            return result[i--];
        else
            return sysmbol[j--];

    }

}
