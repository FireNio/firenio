/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.test.others;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.generallycloud.baseio.common.ByteUtil;

/**
 * @author wangkai
 *
 */
@SuppressWarnings("serial")
public class GraphicsTest {

    public static void main(String[] args) {
        /*
         * 在 AWT 的事件队列线程中创建窗口和组件, 确保线程安全,
         * 即 组件创建、绘制、事件响应 需要处于同一线程。
         */
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // 创建窗口对象
                JFrame frame = new MyFrame();
                // 显示窗口
                frame.setVisible(true);
            }
        });
    }

    /**
     * 窗口
     */

    public static class MyFrame extends JFrame {

        public static final String TITLE  = "Java图形绘制";

        public static final int    WIDTH  = 800;
        public static final int    HEIGHT = 600;

        public MyFrame() {
            super();
            initFrame();
        }

        private void initFrame() {
            // 设置 窗口标题 和 窗口大小
            setTitle(TITLE);
            setSize(WIDTH, HEIGHT);

            // 设置窗口关闭按钮的默认操作(点击关闭时退出进程)
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // 把窗口位置设置到屏幕的中心
            setLocationRelativeTo(null);

            // 设置窗口的内容面板
            MyPanel panel = new MyPanel(this);
            setContentPane(panel);
        }

    }

    /**
     * 内容面板
     */
    public static class MyPanel extends JPanel {

        private MyFrame frame;

        public MyPanel(MyFrame frame) {
            super();
            this.frame = frame;
        }

        /**
         * 绘制面板的内容: 创建 JPanel 后会调用一次该方法绘制内容,
         * 之后如果数据改变需要重新绘制, 可调用 updateUI() 方法触发
         * 系统再次调用该方法绘制更新 JPanel 的内容。
         */
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            // 重新调用 Graphics 的绘制方法绘制时将自动擦除旧的内容

            /* 自行打开下面注释查看各绘制效果 */

            // 1. 线段 / 折线
            // drawLine(g);

            // 2. 矩形 / 多边形
            // drawRect(g);

            // 3. 圆弧 / 扇形
            // drawArc(g);

            // 4. 椭圆
            // drawOval(g);

            // 5. 图片
            // drawImage(g);

            // 6. 文本
            // drawString(g);

            drawTest(g);
        }

        /**
         * 1. 线段 / 折线
         */
        void drawLine(Graphics g) {
            frame.setTitle("1. 线段 / 折线");

            // 创建 Graphics 的副本, 需要改变 Graphics 的参数,
            // 这里必须使用副本, 避免影响到 Graphics 原有的设置
            Graphics2D g2d = (Graphics2D) g.create();

            // 抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            // 设置画笔颜色
            g2d.setColor(Color.RED);

            // 1. 两点绘制线段: 点(20, 50), 点(200, 50)
            g2d.drawLine(50, 50, 200, 50);

            // 2. 多点绘制折线: 点(50, 100), 点(100, 130), 点(150, 70), 点(200, 100)
            int[] xPoints = new int[] { 50, 100, 150, 200 };
            int[] yPoints = new int[] { 100, 120, 80, 100 };
            int nPoints = 4;
            g2d.drawPolyline(xPoints, yPoints, nPoints);

            // 3. 两点绘制线段（设置线宽为5px）: 点(50, 150), 点(200, 150)
            BasicStroke bs1 = new BasicStroke(5); // 笔画的轮廓（画笔宽度/线宽为5px）
            g2d.setStroke(bs1);
            g2d.drawLine(50, 150, 200, 150);

            // 4. 绘制虚线: 将虚线分为若干段（ 实线段 和 空白段 都认为是一段）, 实线段 和 空白段 交替绘制,
            //             绘制的每一段（包括 实线段 和 空白段）的 长度 从 dash 虚线模式数组中取值（从首
            //             元素开始循环取值）, 下面数组即表示每段长度分别为: 5px, 10px, 5px, 10px, ...
            float[] dash = new float[] { 5, 10 };
            BasicStroke bs2 = new BasicStroke(1, // 画笔宽度/线宽
                    BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, dash, // 虚线模式数组
                    0.0f);
            g2d.setStroke(bs2);
            g2d.drawLine(50, 200, 200, 200);

            // 自己创建的副本用完要销毁掉
            g2d.dispose();
        }

        /**
         * 2. 矩形 / 多边形
         */
        void drawRect(Graphics g) {
            frame.setTitle("2. 矩形 / 多边形");
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.GRAY);

            // 1. 绘制一个矩形: 起点(30, 20), 宽80, 高100
            g2d.drawRect(30, 20, 80, 100);

            // 2. 填充一个矩形
            g2d.fillRect(140, 20, 80, 100);

            // 3. 绘制一个圆角矩形: 起点(30, 150), 宽80, 高100, 圆角宽30, 圆角高30
            g2d.drawRoundRect(30, 150, 80, 100, 30, 30);

            // 4. 绘制一个多边形(收尾相连): 点(140, 150), 点(180, 250), 点(220, 200)
            int[] xPoints = new int[] { 140, 180, 220 };
            int[] yPoints = new int[] { 150, 250, 200 };
            int nPoints = 3;
            g2d.drawPolygon(xPoints, yPoints, nPoints);

            g2d.dispose();
        }

        /**
         * 3. 圆弧 / 扇形
         */
        void drawArc(Graphics g) {
            frame.setTitle("3. 圆弧 / 扇形");
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.RED);

            // 1. 绘制一条圆弧: 椭圆的外切矩形 左上角坐标为(0, 0), 宽100, 高100,
            //                弧的开始角度为0度, 需要绘制的角度数为-90度,
            //                椭圆右边水平线为0度, 逆时针为正角度, 顺时针为负角度
            g2d.drawArc(0, 0, 100, 100, 0, -90);

            // 2. 绘制一个圆: 圆的外切矩形 左上角坐标为(120, 20), 宽高为100
            g2d.drawArc(120, 20, 100, 100, 0, 360);

            g2d.setColor(Color.GRAY);

            // 3. 填充一个扇形
            g2d.fillArc(80, 150, 100, 100, 90, 270);

            g2d.dispose();
        }
        
        byte [] randomArray(int n){
            byte [] array = new byte [n];
            Random r = new Random();
            for (int i = 0; i < array.length; i++) {
                array[i] = (byte) r.nextInt(256);
            }
            return array;
        }
        
        byte [] randomArray1(int n){
            byte [] seed = "abcdefghijklmnopqrstuvwxyz1234567890".getBytes();
            byte [] array = new byte [n];
            Random r = new Random();
            for (int i = 0; i < array.length; i++) {
                array[i] = seed[r.nextInt(seed.length)];
            }
            return array;
        }

        /**
         * 3. 圆弧 / 扇形
         */
        void drawTest(Graphics gra) {
            frame.setTitle("test");
            Graphics2D g = (Graphics2D) gra.create();

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = 8 * 32 / 8; 
            int height = 8 * 32;
            
            int cheng = 8;
            
            Random r = new Random();
            
            byte [] array = randomArray(width * height);
            boolean [] [] bits = new boolean [(array.length + 1) / width][width * 8];
            int bIndex = 0;
            LOOP: for (int i = 0; i < bits.length; i++) {
                boolean []tar = bits[i];
                for (int j = 0; j < width; j++) {
                    byte b = array[bIndex++];
                    tar[j * cheng + 0] = ByteUtil.getBoolean(b, 0);
                    tar[j * cheng + 1] = ByteUtil.getBoolean(b, 1);
                    tar[j * cheng + 2] = ByteUtil.getBoolean(b, 2);
                    tar[j * cheng + 3] = ByteUtil.getBoolean(b, 3);
                    tar[j * cheng + 4] = ByteUtil.getBoolean(b, 4);
                    tar[j * cheng + 5] = ByteUtil.getBoolean(b, 5);
                    tar[j * cheng + 6] = ByteUtil.getBoolean(b, 6);
                    tar[j * cheng + 7] = ByteUtil.getBoolean(b, 7);
                    
                    tar[j * cheng + 0] = r.nextBoolean();
                    tar[j * cheng + 1] = r.nextBoolean();
                    tar[j * cheng + 2] = r.nextBoolean();
                    tar[j * cheng + 3] = r.nextBoolean();
                    tar[j * cheng + 4] = r.nextBoolean();
                    tar[j * cheng + 5] = r.nextBoolean();
                    tar[j * cheng + 6] = r.nextBoolean();
                    tar[j * cheng + 7] = r.nextBoolean();
                    if (bIndex == array.length) {
                        break LOOP;
                    }
                }
            }
            
            int leftPadding = 50;
            int topPadding = 50;
            int px = 1;
            
            for (int i = 0; i < bits.length; i++) {
                boolean [] row = bits[i];
                for (int j = 0; j < row.length; j++) {
                    if (row[j]) {
                        g.setColor(Color.black);
                    }else{
                        g.setColor(Color.WHITE);
                    }
                    g.drawRect(leftPadding + j * px, topPadding + i * px, px, px);
                    g.fillRect(leftPadding + j * px, topPadding + i * px, px, px);
                }
            }
            
            
            g.dispose();
        }

        /**
         * 4. 椭圆 (实际上通过绘制360度的圆弧/扇形也能达到绘制圆/椭圆的效果)
         */
        void drawOval(Graphics g) {
            frame.setTitle("4. 椭圆");
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.RED);

            // 1. 绘制一个圆: 圆的外切矩形 左上角坐标为(0, 0), 宽高为100
            g2d.drawOval(0, 0, 100, 100);

            g2d.setColor(Color.GRAY);

            // 2. 填充一个椭圆
            g2d.fillOval(120, 100, 100, 150);

            g2d.dispose();
        }

        /**
         * 5. 图片
         */
        void drawImage(Graphics g) {
            frame.setTitle("5. 图片");
            Graphics2D g2d = (Graphics2D) g.create();

            // 从本地读取一张图片
            String filepath = "demo.jpg";
            Image image = Toolkit.getDefaultToolkit().getImage(filepath);

            // 绘制图片（如果宽高传的不是图片原本的宽高, 则图片将会适当缩放绘制）
            g2d.drawImage(image, 50, 50, image.getWidth(this), image.getHeight(this), this);

            g2d.dispose();
        }

        /**
         * 6. 文本
         */
        void drawString(Graphics g) {
            frame.setTitle("6. 文本");
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            // 设置字体样式, null 表示使用默认字体, Font.PLAIN 为普通样式, 大小为 25px
            g2d.setFont(new Font(null, Font.PLAIN, 25));

            // 绘制文本, 其中坐标参数指的是文本绘制后的 左下角 的位置
            // 首次绘制需要初始化字体, 可能需要较耗时
            g2d.drawString("Hello World!", 20, 60);
            g2d.drawString("你好, 世界!", 20, 120);

            g2d.dispose();
        }

    }

}
