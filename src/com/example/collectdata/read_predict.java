package com.example.collectdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AutoCompleteTextView.Validator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.libsvm.R;
import com.example.handledata.translatedata;

public class read_predict extends Activity implements OnTouchListener {
	private volatile int wc = 0; // 用于数据记录启动/暂停的控制标记，1代表记录数据，0代表暂停记录
	private volatile int wd = 1;
	private SensorManager mySensorManager; // SensorManager对象引用
	private Sensor myaccelerometer; // 加速度传感器（包括重力）
	private Sensor myrotationSensor;
	private Sensor mygrySensor;
	private Button writebu;
	private float gyrd[]; // 用于存放最新的陀螺仪数据
	private float accd[]; // 用于存放最新的加速度传感器数据
	private float accdtest[]; // 用于存放最新的加速度传感器数据
	private float gravity[];
	private float rotation[];
	private float[] mRotationMatrix = new float[9];
	private final Timer timer = new Timer();
	private Handler handler;// 数据接收端
	private long time;
	private long timeacc;
	private long timerotation;
	private Vibrator vibrator; // 震动
	private long timegyr;
	private int labelnum;
	private long bytenum = 0;
	private long lastbyte;
	// 这里必须要化成7位数，否则比较会出错
	private int period = 0;
	// RandomAccessFile randomfile;
	private Button start_stopButton;

	// 输入和输出的路径在这里
	private String tmpString = "//sdcard/train/data/sensortestaccpre.tmp";
	private String realString = "//sdcard/train/data/sensortestaccpre.real";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.read_predict);
		writebu = (Button) findViewById(R.id.writecon);// 用于控制数据记录启动/暂停按钮的显示
		writebu.setOnTouchListener(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		gyrd = new float[3];
		accd = new float[3];
		accdtest = new float[3];
		gravity = new float[3];
		rotation = new float[3];
		wc = 0; // 默认开启传感器数据记录，控制标记为1
		// 获得SensorManager对象
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// 获取缺省的线性加速度传感器
		myaccelerometer = mySensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		myrotationSensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mygrySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		File sensortmp = new File(tmpString);
		File sensorreal = new File(realString);
		// timer.schedule(task,0, SAMPLET);
		// //第一次延时0ms，之后每SAMPLET时间，记录一组加速度传感器和陀螺仪数据
		if (!sensortmp.isFile()) {
			try {
				sensortmp.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!sensorreal.isFile()) {
			try {
				sensorreal.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		File dirFile = new File("/sdcard/train/data");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}
		// translatedata类一定要小心的使用
		/*
		 * handler=new Handler(){ public void handleMessage(Message msgMessage)
		 * { if(msgMessage.what==110){ wd=0; translatedata readtmp=new
		 * translatedata("/sdcard/sensortestaccpre.tmp"); File sensortmp=new
		 * File("//sdcard/sensortestaccpre.tmp"); try { readtmp.file_array();
		 * float[]
		 * grymean=readtmp.grymean_extract(readtmp.floatcollectArray,false);
		 * float[] accxmeanshift_extract=readtmp.accxmeanshift_extract(readtmp.
		 * floatcollectArray,false); float accxsum = 0,accavr; for (int i = 0; i
		 * < accxmeanshift_extract.length; i++) {
		 * accxsum+=accxmeanshift_extract[i]*accxmeanshift_extract[i]; }
		 * accavr=(float) (Math.sqrt(accxsum)/accxmeanshift_extract.length);
		 * if(true){ FileInputStream Instream=new
		 * FileInputStream("//sdcard/sensortestaccpre.tmp"); long
		 * length=sensortmp.length(); byte[] buffer=new byte[(int) (length*2)];
		 * Instream.read(buffer); Instream.close(); FileOutputStream
		 * Outstream=new FileOutputStream("//sdcard/sensortestaccpre.real");
		 * Outstream.write(buffer); Outstream.close(); sensortmp.delete();
		 * //将文件删 wd=1; } else{ sensortmp.delete(); } } catch (IOException e) {
		 * // TODO Auto-generated catch block e.printStackTrace(); }
		 * 
		 * } } }; // File sensor1=new File("//sdcard/sensortestacc.txt");
		 * timer.schedule(new TimerTask() {
		 * 
		 * @Override public void run() { // TODO Auto-generated method stub
		 * period++; if(period==12){//10秒启动另一个activity Message msgMessage=new
		 * Message(); msgMessage.what=110; handler.sendMessage(msgMessage);}
		 * 
		 * } }, 100,100);
		 */

	}

	@Override
	protected void onResume() { // 重写onResume方法
		super.onResume();
		// 监听陀螺仪传感器

		// 监听加速度传感器
		mySensorManager.registerListener(mySensorListener, // 添加监听
				myaccelerometer, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);

		mySensorManager.registerListener(mySensorListener, // 添加监听
				myrotationSensor, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);
		mySensorManager.registerListener(mySensorListener, // 添加监听
				mygrySensor, // 传感器类型
				SensorManager.SENSOR_DELAY_FASTEST // 传感器事件传递的频度
				);
	}
	@Override
	protected void onPause() {// 重写onPause方法
		super.onPause();
		mySensorManager.unregisterListener(mySensorListener);// 取消注册监听器
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	private SensorEventListener mySensorListener = new SensorEventListener() {// 开发实现了SensorEventListener接口的传感器监听器
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		@Override
		public void onSensorChanged(SensorEvent event) {

			// 传感器读取启动的情况下，读取传感器数据
			// 在系统刚开始读取加速度的时候，记录的系统的时间不稳定，最好不是从一个开始运算
			long time = System.currentTimeMillis();
			time = time - time / 10000000 * 10000000;
			float[] values = event.values;// 获取传感器的三个数据
			float offset = (float) Math.sqrt(values[0] * values[0] + values[1]
					* values[1] + values[2] * values[2]);
			// 陀螺仪传感器变化
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				// 小于ACCMIN时属于误差范围
				accd[0] = values[0];
				accd[1] = values[1];
				accd[2] = values[2]; // 将最新的加速度传感器数据存在加速度传感器数组中
				accdtest[0] = accd[0];
				accdtest[1] = accd[1];
				accdtest[2] = accd[2]; // 将最新的加速度传感器数据存在加速度传感器数组中
				timeacc = time;
				if ((wc == 1) & (wd == 1)) {
					float[] accd1 = new float[3];
					float[] n = new float[3];
					n[0] = 1;
					n[1] = 1;
					n[2] = 1;

					float[][] buffer = {{(float) accd[0], 0, 0},
							{(float) accd[1], 0, 0}, {(float) accd[2], 0, 0}};
					float[] Orientation = new float[3];
					SensorManager.getRotationMatrixFromVector(mRotationMatrix,
							rotation);
					SensorManager.getOrientation(mRotationMatrix, Orientation);
					float[][] rotationversion = matrixinversion(mRotationMatrix);
					float[][] mk = {
							{mRotationMatrix[0], mRotationMatrix[1],
									mRotationMatrix[2]},
							{mRotationMatrix[3], mRotationMatrix[4],
									mRotationMatrix[5]},
							{mRotationMatrix[6], mRotationMatrix[7],
									mRotationMatrix[8]}};
					rotationversion = maxtrixmutiply(mk, buffer);
					accd[0] = rotationversion[0][0];
					accd[1] = rotationversion[1][0];
					accd[2] = rotationversion[2][0];
					// if(Math.abs(accd[0])<0.15)accd[0]=0;
					// if(Math.abs(accd[1])<0.15)accd[1]=0;
					// if(Math.abs(accd[2])<0.15)accd[2]=0;
					try {

						FileOutputStream foStream = new FileOutputStream(
								tmpString, true); // 定义传感器数据的输出流
						// File sensor1=new File("//sdcard/sensortestacc.txt");
						String sensorstr = accd[0] + " " + accd[1] + " "
								+ accd[2] + " " + timeacc + " " + gyrd[0] + " "
								+ gyrd[1] + " " + gyrd[2] + " " + timegyr
								+ "\n";
						byte[] buffer11 = new byte[sensorstr.length() * 2];
						buffer11 = sensorstr.getBytes();
						foStream.write(buffer11);
						foStream.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						Log.v("FileNotFoundException", "OK");
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

			else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				rotation[0] = (float) values[0];
				rotation[1] = (float) values[1];
				rotation[2] = (float) values[2]; // 将最新的加速度传感器数据存在加速度传感器数组中
				timerotation = time;

			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				gyrd[0] = (float) values[0];
				gyrd[1] = (float) values[1];
				gyrd[2] = (float) values[2]; // 将最新的加速度传感器数据存在加速度传感器数组中
				timegyr = time;

			}

		}

	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent e) {
		switch (keyCode) {
			case 4 :
				System.exit(0);
				break;
		}
		return true;
	}

	public void onClick_writecon(View view) // 按下数据记录启动/暂停后的动作
	{
	}

	public void onClick_delete(View view) throws IOException // 按下清除后的动作
	{
		// File sensor=new File("//sdcard/sensortestaccpre.txt"); //获取文件对象
		File sensortmp = new File(tmpString); // 获取文件对象
		File sensortestaccreal = new File(realString); // 获取文件对象
		// sensor.delete(); //将文件删
		sensortmp.delete();
		sensortestaccreal.delete();
		/*
		 * File sensor1=new File("//sdcard/sensortestacc.txt"); //获取文件对象\
		 * RandomAccessFile randomfile=new RandomAccessFile(sensor1,"rw");
		 * randomfile.seek(bytenum-lastbyte);
		 */
		// randomfile.
		// sensor1.delete(); //将文件删除
		vibrator.vibrate(200);
	}
	// 三阶行列式的计算
	public static float getHL3(float[] input) {
		float unm1 = input[0] * (input[4] * input[8] - input[5] * input[7]);
		float unm2 = -input[1] * (input[3] * input[8] - input[5] * input[6]);
		float unm3 = input[2] * (input[3] * input[7] - input[4] * input[6]);
		return unm1 + unm2 + unm3;
	}

	private static float[][] matrixinversion(float[] input) {
		// 求代数余子式
		float[] buffer1 = new float[9];

		for (int i = 0; i < input.length; i++) {
			float[] buffer0 = input.clone();
			if (i % 3 == 0) {
				buffer0[i] = 1;
				buffer0[i + 1] = 0;
				buffer0[i + 2] = 0;
			}
			if (i % 3 == 1) {
				buffer0[i - 1] = 0;
				buffer0[i] = 1;
				buffer0[i + 1] = 0;
			}
			if (i % 3 == 2) {
				buffer0[i - 2] = 0;
				buffer0[i - 1] = 0;
				buffer0[i] = 1;
			}
			buffer1[i] = getHL3(buffer0) / getHL3(input);
			if (i % 2 == 1)
				buffer1[i] = -buffer1[i];
		}
		float[][] buffer = {{buffer1[0], buffer1[1], buffer1[2]},
				{buffer1[3], buffer1[4], buffer1[5]},
				{buffer1[6], buffer1[7], buffer1[8]}};
		return buffer;
	}
	private static float[][] maxtrixmutiply(float[][] maxtrileft,
			float[][] maxtriright) {
		float[][] result = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
		// TODO Auto-generated method stub

		for (int i = 0; i < maxtrileft.length; i++)
			for (int j = 0; j < maxtrileft[0].length; j++) {
				result[i][j] = maxtrileft[i][0] * maxtriright[0][j]
						+ maxtrileft[i][1] * maxtriright[1][j]
						+ maxtrileft[i][2] * maxtriright[2][j];
			}
		return result;
	}// 一个矩阵乘法

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			wc = (wc + 1) % 2;
			writebu.setText("开始");
			vibrator.vibrate(200);
			if (wc == 0) {
				writebu.setText("暂停");
				final Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("确认输入？");
				builder.setPositiveButton("确定",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								try {
									FileInputStream Instream = new FileInputStream(
											tmpString);
									File sensortmp = new File(tmpString);
									long length = sensortmp.length();
									byte[] buffer = new byte[(int) (length * 2)];
									Instream.read(buffer);
									Instream.close();
									FileOutputStream Outstream = new FileOutputStream(
											realString);
									Outstream.write(buffer);
									Outstream.close();
									sensortmp.delete(); // 将文件删

								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} // 定义传感器数据的输出流
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}

						});
				builder.setNegativeButton("取消",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								File sensor = new File(tmpString); // 获取文件对象
								sensor.delete(); // 将文件删
							}
						});
				builder.create().show();
			}
			/*
			 * File sensor1=new File("//sdcard/sensortestacc.txt"); //获取文件对象\
			 * lastbyte=sensor1.length(); bytenum+=sensor1.length();
			 */

		}

		return false;
	}

}
