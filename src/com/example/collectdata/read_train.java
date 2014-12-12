package com.example.collectdata;

import java.io.File;

import com.example.libsvm.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import com.example.handledata.translatedata;
import com.example.preproccess.PCA_done;

import android.R.integer;
import android.R.string;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class read_train extends Activity implements OnTouchListener {

	private int wc; // �������ݼ�¼����/��ͣ�Ŀ��Ʊ�ǣ�1�����¼���ݣ�0������ͣ��¼
	private SensorManager mySensorManager; // SensorManager��������
	private Sensor myaccelerometer; // ���ٶȴ�����������������
	private Sensor myrotationSensor;
	private Sensor mygyrSensor;
	private Button writebu;
	private float gyrd[]; // ���ڴ�����µ�����������
	private float accd[]; // ���ڴ�����µļ��ٶȴ���������
	private float accdtest[]; // ���ڴ�����µļ��ٶȴ���������
	private float gravity[];
	private float rotation[];
	private float[] mRotationMatrix = new float[9];
	private long time;
	private long timeacc;
	private long timerotation;
	private long timegyr;
	private Vibrator vibrator; // ��
	private int labelnum;
	private int do_num = 0;
	private long bytenum = 0;
	private long lastbyte;
	private ImageView draw;
	private String tmpString = "//sdcard/train/data/sensortestacc.tmp";// ��ԭʼ������
	private String realString = "//sdcard/train/data/sensortestacc.txt";//

	private String tmpStringOutput = "//sdcard/train/data/sensortestacc.tmp.out";// �����ٶ�ƽ����ʱ����������

	private String backOutput = "//sdcard/train/data/";

	// �������Ҫ����7λ��������Ƚϻ����
	// RandomAccessFile randomfile;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.read_train);
		writebu = (Button) findViewById(R.id.writecon);// ���ڿ������ݼ�¼����/��ͣ��ť����ʾ
		writebu.setOnTouchListener(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		gyrd = new float[3];
		accd = new float[3];
		accdtest = new float[3];
		gravity = new float[3];
		rotation = new float[3];
		wc = 0; // Ĭ�Ͽ������������ݼ�¼�����Ʊ��Ϊ1
		// ���SensorManager����
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// ��ȡȱʡ�����Լ��ٶȴ�����
		myaccelerometer = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		myrotationSensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mygyrSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		File sensorFile = new File(realString);
		File sensortmp = new File(tmpString);

		// timer.schedule(task,0, SAMPLET);
		// //��һ����ʱ0ms��֮��ÿSAMPLETʱ�䣬��¼һ����ٶȴ�����������������
		if (!sensorFile.isFile()) {
			try {
				sensorFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!sensortmp.isFile()) {
			try {
				sensortmp.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// File sensor1=new File("//sdcard/sensortestacc.txt");
		File dirFile = new File("/sdcard/train/data");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

	}

	@Override
	protected void onResume() { // ��дonResume����
		super.onResume();
		// ���������Ǵ�����

		// �������ٶȴ�����
		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				myaccelerometer, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);

		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				myrotationSensor, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);

		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				mygyrSensor, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);
	}

	@Override
	protected void onPause() {// ��дonPause����
		super.onPause();
		mySensorManager.unregisterListener(mySensorListener);// ȡ��ע�������
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	private SensorEventListener mySensorListener = new SensorEventListener() {// ����ʵ����SensorEventListener�ӿڵĴ�����������
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Log.v("accuracy1",sensor.getType()+"");
			// Log.v("accuracy2",accuracy+"");
			// Log.v("accuracy1", SensorManager.SENSOR_STATUS_ACCURACY_HIGH+"");
			// Log.v("accuracy2", SensorManager.SENSOR_STATUS_ACCURACY_LOW+"");
			// Log.v("accuracy3",
			// SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM+"");
			// Log.v("accuracy4", SensorManager.SENSOR_STATUS_UNRELIABLE+"");

		}

		@Override
		public void onSensorChanged(SensorEvent event) {

			// ��������ȡ����������£���ȡ����������
			// ��ϵͳ�տ�ʼ��ȡ���ٶȵ�ʱ�򣬼�¼��ϵͳ��ʱ�䲻�ȶ�����ò��Ǵ�һ����ʼ����
			long time = System.currentTimeMillis();
			time = time - time / 10000000 * 10000000;
			float[] values = event.values;// ��ȡ����������������
			float offset = (float) Math.sqrt(values[0] * values[0] + values[1]
					* values[1] + values[2] * values[2]);
			// �����Ǵ������仯
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				Log.v("Sensor.TYPE_LINEAR_ACCELERATION",
						event.sensor.getMinDelay() + "");
				// С��ACCMINʱ������Χ
				accd[0] = Math.abs(values[0]) > 0.4 ? values[0] : 0;
				accd[1] = Math.abs(values[1]) > 0.4 ? values[1] : 0;
				accd[2] = Math.abs(values[2]) > 0.4 ? values[2] : 0;
				timeacc = time;
			}

			else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				Log.v("Sensor.TYPE_ROTATION_VECTOR", event.sensor.getMinDelay()
						+ "");
				rotation[0] = (float) values[0];
				rotation[1] = (float) values[1];
				rotation[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timerotation = time;
				if (wc == 1) {
					try {

						FileOutputStream foStream = new FileOutputStream(
								tmpString, true); // ���崫�������ݵ������
						String sensorstr = accd[0] + " " + accd[1] + " "
								+ accd[2] + " " + timeacc + " " + rotation[0]
								+ " " + rotation[1] + " " + rotation[2] + " "
								+ timerotation + " " + gyrd[0] + " " + gyrd[1]
								+ " " + gyrd[2] + " " + timegyr + "\n";
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

			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				Log.v("Sensor.TYPE_GYROSCOPE", event.sensor.getMinDelay() + "");
				gyrd[0] = Math.abs(values[0]) > 0.02 ? values[0] : 0;
				gyrd[1] = Math.abs(values[1]) > 0.02 ? values[1] : 0;
				gyrd[2] = Math.abs(values[2]) > 0.02 ? values[2] : 0;
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

	public void onClick_writecon(View view) // �������ݼ�¼����/��ͣ��Ķ���
	{
	}

	public void onDraw(View view) throws IOException // ���������Ķ���
	{
		int px;
		int py;
		int distance;
		EditText pxEditText = (EditText) findViewById(R.id.px);
		EditText pyEditText = (EditText) findViewById(R.id.py);
		EditText distanceEditText = (EditText) findViewById(R.id.distance);
		if (pxEditText.getEditableText().toString().equals("")
				|| pyEditText.getEditableText().toString().equals("")
				|| distanceEditText.getEditableText().toString().equals(""))

		{
			Toast.makeText(this, "wrong!", Toast.LENGTH_LONG).show();
			return;
		}
		px = Integer.parseInt(pxEditText.getEditableText().toString());
		py = Integer.parseInt(pyEditText.getEditableText().toString());
		distance = Integer.parseInt(distanceEditText.getEditableText()
				.toString());
		ArrayList<float[]> positionArrayList = new ArrayList<float[]>();
		try {
			File file = new File("/sdcard/train/data/sensortestacc.txt");
			if (file.length() == 0)
				return;
			new PCA_done("/sdcard/train/data/sensortestacc.txt",
					"/sdcard/train/PCA_train");// PCAģ��
			translatedata translatedata_train = new translatedata(
					"/sdcard/train/PCA_train");
			translatedata_train.file_array();
			ArrayList<float[]> AccArrayList = translatedata_train.floatcollectArray;
			// ��ֻ֤��һ��ѵ���ַ��ģ��������ử�����ء�������һ���������ֱ�ӷ���
			int i = 0;
			for (float[] buffer : AccArrayList) {
				if (translatedata.isint(buffer[0]))
					i++;
			}
			if (i != 1)
				return;
			positionArrayList = translatedata_train.getposition(AccArrayList,
					true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			new File("/sdcard/train/PCA_train").delete();
		}
		float[] x = positionArrayList.get(0);
		float[] y = positionArrayList.get(1);
		if (x.length != y.length)
			return;// ����ͬ��ʱ�򷵻�
		draw = (ImageView) findViewById(R.id.draw);
		WindowManager windowManager = getWindowManager();
		Display display = windowManager.getDefaultDisplay();
		int Width = (int) (display.getWidth() * 0.7);
		int Height = (int) (display.getHeight() * 0.7);
		final Bitmap bitmap = Bitmap.createBitmap(Width, Height,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setStrokeWidth(10);

		for (int i = 0; i < x.length - 1; i++) {
			canvas.drawLine(x[i] / distance + px, y[i] / distance + py,
					x[i + 1] / distance + px, y[i + 1] / distance + py, paint);
			if (i > x.length / 2)
				paint.setColor(Color.RED);
		}
		draw.setImageBitmap(bitmap);

	}

	public void onClick_delete(View view) throws IOException // ���������Ķ���
	{
		final TextView numdis = (TextView) findViewById(R.id.numdis);
		final EditText numEditText = (EditText) findViewById(R.id.num);
		final Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("�Ƿ��������");
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String backupPath = backOutput
						+ numEditText.getText().toString();
				String backupPathtmp = backOutput
						+ numEditText.getText().toString() + ".tmp";
				Log.v("numEditText", numEditText.getText().toString());
				File tmpbackupFile = new File(backupPath);
				if (!tmpbackupFile.exists())
					try {
						tmpbackupFile.createNewFile();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				try {
					InputStream is = new FileInputStream(tmpString);
					OutputStream os = new FileOutputStream(backupPath, true);
					int len = 0;
					while ((len = is.read()) != -1) {
						os.write(len);
					}
					is.close();
					os.close();
					is = new FileInputStream(tmpStringOutput);
					os = new FileOutputStream(backupPathtmp, true);
					len = 0;
					while ((len = is.read()) != -1) {
						os.write(len);
					}
					is.close();
					os.close();
					do_num = 0;
					numdis.setText(Integer.toString(do_num));

					start_train();// ��ȡ��������

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				} finally {
					// new File(realString).delete(); // ��ȡ�ļ�����
					new File(tmpString).delete();
					new File(tmpStringOutput).delete();
				}

			}

		});
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				do_num = 0;
				numdis.setText(Integer.toString(do_num));
				new File(realString).delete(); // ��ȡ�ļ�����
				new File(tmpString).delete();
				new File(tmpStringOutput).delete();
			}
		});
		builder.create().show();

		vibrator.vibrate(200);
	}
	// �����Ǵ�MainActivity�а�������һ��train�ĺ�����ʵ��һ����ȡ������������MainActivity��start_train�Ĺ���һ��
	private void start_train() throws IOException {

		File trainFile = new File(realString);
		if (!trainFile.exists() || (trainFile.length() == 0)) {
			Toast.makeText(this, "read_train.java�У���ʾ���ݲ�����", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		// new PCA_done(trainfile,"/sdcard/train/data/PCA_train");//PCAģ��
		// translatedata translatedata_train=new
		// translatedata("/sdcard/train/data/PCA_train");
		translatedata translatedata_train = new translatedata(realString);
		translatedata_train.file_array();
		/*
		 * if(translatedata_train.floatcollectArray.size()>1000) {
		 * toast("����̫���ˣ�����������"); return; }
		 */
		try {
			translatedata_train.extract(true);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			new File("/sdcard/train/data/PCA_train").delete();
			new File(realString).delete();
		}
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
	}// һ������˷�
		// �������Ǽ�����Ԫ�ر仯��������ö��������㷨
	/*
	 * public void pCompensation() throws IOException { BufferedReader sb = new
	 * BufferedReader(new FileReader(tmpString)); FileOutputStream foStream =
	 * new FileOutputStream(tmpStringOutput, true); // ���崫�������ݵ������ String s =
	 * sb.readLine();// �����һ�������� s = s + "\n"; byte[] buffer = new
	 * byte[s.length() * 2]; buffer = s.getBytes(); foStream.write(buffer);//
	 * ������д��ȥ }
	 */

	// �����ٶȺ������ǵ�ֵ��ƽ����Rotation��ʱ����
	/*
	 * public void getChangedAcc() throws IOException { float DT = 1.0f /
	 * 1000000000.0f; BufferedReader sb = new BufferedReader(new
	 * FileReader(tmpString)); FileOutputStream foStream = new
	 * FileOutputStream(tmpStringOutput, true); // ���崫�������ݵ������ String s =
	 * sb.readLine();// �����һ�������� s = s + "\n"; byte[] buffer = new
	 * byte[s.length() * 2]; buffer = s.getBytes(); foStream.write(buffer);//
	 * ������д��ȥ s = sb.readLine(); String stringArray[] = s.split(" "); float
	 * preaccx = Float.parseFloat(stringArray[0]); float preaccy =
	 * Float.parseFloat(stringArray[1]); float preaccz =
	 * Float.parseFloat(stringArray[2]);
	 * 
	 * long pretimeacc = Long.parseLong(stringArray[3]);
	 * 
	 * float pregryx = Float.parseFloat(stringArray[8]); float pregryy =
	 * Float.parseFloat(stringArray[9]); float pregryz =
	 * Float.parseFloat(stringArray[10]);
	 * 
	 * long pretimegry = Long.parseLong(stringArray[11]);
	 * 
	 * float preRotationx = Float.parseFloat(stringArray[4]); float preRotationy
	 * = Float.parseFloat(stringArray[5]); float preRotationz =
	 * Float.parseFloat(stringArray[6]); while ((s = sb.readLine()) != null) {
	 * stringArray = s.split(" "); long accTime =
	 * Integer.parseInt(stringArray[3]); long grytime =
	 * Integer.parseInt(stringArray[11]); long rotationTime =
	 * Integer.parseInt(stringArray[7]);
	 * 
	 * float tmpaccx = Float.parseFloat(stringArray[0]); float tmpaccy =
	 * Float.parseFloat(stringArray[1]); float tmpaccz =
	 * Float.parseFloat(stringArray[2]);
	 * 
	 * float tmpgryx = Float.parseFloat(stringArray[8]); float tmpgryy =
	 * Float.parseFloat(stringArray[9]); float tmpgryz =
	 * Float.parseFloat(stringArray[10]);
	 * 
	 * float tmpRotationx = Float.parseFloat(stringArray[4]); float tmpRotationy
	 * = Float.parseFloat(stringArray[5]); float tmpRotationz =
	 * Float.parseFloat(stringArray[6]);
	 * 
	 * if (accTime != pretimeacc) { tmpaccx = (tmpaccx - preaccx) / (accTime -
	 * pretimeacc) rotationTime + (preaccx * accTime - tmpaccx * pretimeacc) /
	 * (accTime - pretimeacc); tmpaccy = (tmpaccy - preaccy) / (accTime -
	 * pretimeacc) rotationTime + (preaccy * accTime - tmpaccy * pretimeacc) /
	 * (accTime - pretimeacc); tmpaccz = (tmpaccz - preaccz) / (accTime -
	 * pretimeacc) rotationTime + (preaccz * accTime - tmpaccz * pretimeacc) /
	 * (accTime - pretimeacc); } else { tmpaccx = preaccx; tmpaccy = preaccy;
	 * tmpaccz = preaccz; } if (grytime != pretimegry) { tmpgryx = (tmpgryx -
	 * pregryx) / (grytime - pretimegry) rotationTime + (pregryx * grytime -
	 * tmpgryx * pretimegry) / (grytime - pretimegry); tmpgryy = (tmpgryy -
	 * pregryy) / (grytime - pretimegry) rotationTime + (pregryy * grytime -
	 * tmpgryy * pretimegry) / (grytime - pretimegry); tmpgryz = (tmpgryz -
	 * pregryz) / (grytime - pretimegry) rotationTime + (pregryz * grytime -
	 * tmpgryz * pretimegry) / (grytime - pretimegry);
	 * 
	 * } else { tmpgryx = pregryx; tmpgryy = pregryy; tmpgryz = pregryz; } //
	 * �ö��������w=a+2bx
	 * 
	 * long h = (pretimegry + rotationTime) / 2;
	 * 
	 * float wxgain1 = (h - pretimegry) (pregryx + (pregryx + tmpgryx) / 2) / 2;
	 * float wygain1 = (h - pretimegry) (pregryy + (pregryy + tmpgryy) / 2) / 2;
	 * float wzgain1 = (h - pretimegry) (pregryz + (pregryz + tmpgryz) / 2) / 2;
	 * 
	 * float wxgain2 = (h - pretimegry) (tmpgryx + (pregryx + tmpgryx) / 2) / 2;
	 * float wygain2 = (h - pretimegry) (tmpgryy + (pregryy + tmpgryy) / 2) / 2;
	 * float wzgain2 = (h - pretimegry) (tmpgryz + (pregryz + tmpgryz) / 2) / 2;
	 * 
	 * float mx = 2 / 3 * (wygain1 * wxgain2 - wzgain1 * wygain2) + wxgain1 +
	 * wxgain2; float my = 2 / 3 * (wzgain1 * wzgain2 - wxgain1 * wzgain2) +
	 * wygain1 + wygain2; float mz = 2 / 3 * (wxgain1 * wygain2 - wygain1 *
	 * wxgain2) + wzgain1 + wzgain2;
	 * 
	 * float m = (float) Math.sqrt(mx * mx + my * my + mz * mz); float q1, q2,
	 * q3, q4; if (m != 0) { q1 = (float) Math.cos(m / 2 *DT); q2 = (float) (mx
	 * / m * Math.sin(m / 2*DT)); q3 = (float) (my / m * Math.sin(m / 2 *DT));
	 * q4 = (float) (mz / m * Math.sin(m / 2 *DT)); } else { q1 = (float)
	 * Math.cos(m / 2); q2 = 0; q3 = 0; q4 = 0; }
	 * 
	 * float preRotation = 1 - preRotationx * preRotationx - preRotationy
	 * preRotationy - preRotationz * preRotationz;
	 * 
	 * float calRotation = q1 * preRotation - q2 * preRotationx - q3
	 * preRotationy - q4 * preRotationz; float calRotationx = q2 * preRotation +
	 * q1 * preRotationx + q4 preRotationy - q3 * preRotationz; float
	 * calRotationy = q3 * preRotation - q4 * preRotationx + q1 preRotationy +
	 * q2 * preRotationz; float calRotationz = q4 * preRotation + q3 *
	 * preRotationx - q2 preRotationy + q1 * preRotationz;
	 * 
	 * preaccx = Float.parseFloat(stringArray[0]); preaccy =
	 * Float.parseFloat(stringArray[1]); preaccz =
	 * Float.parseFloat(stringArray[2]); pretimeacc = accTime;
	 * 
	 * pregryx = tmpgryx; pregryy = tmpgryy; pregryz = tmpgryz; pretimegry =
	 * rotationTime;
	 * 
	 * preRotationx = calRotationx; preRotationy = calRotationy; preRotationz =
	 * calRotationz;
	 * 
	 * float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0}, {tmpaccz, 0,
	 * 0}};// ǰ�������Ǽ��ٶ� float[] rotationVect = {calRotationx, calRotationy,
	 * calRotationz}; SensorManager.getRotationMatrixFromVector(mRotationMatrix,
	 * rotationVect); float[][] rotationversion = new float[3][]; float[][] mk =
	 * { {mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
	 * {mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
	 * {mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
	 * rotationversion = maxtrixmutiply(mk, bufferacc); tmpaccx =
	 * rotationversion[0][0]; tmpaccy = rotationversion[1][0]; tmpaccz =
	 * rotationversion[2][0];
	 * 
	 * float[] rotationVect1 = {tmpRotationx, tmpRotationy, tmpRotationz};
	 * SensorManager.getRotationMatrixFromVector(mRotationMatrix,
	 * rotationVect1); float[][] mk1 = { {mRotationMatrix[0],
	 * mRotationMatrix[1], mRotationMatrix[2]}, {mRotationMatrix[3],
	 * mRotationMatrix[4], mRotationMatrix[5]}, {mRotationMatrix[6],
	 * mRotationMatrix[7], mRotationMatrix[8]}}; rotationversion =
	 * maxtrixmutiply(mk1, bufferacc); float tmpaccx1 = rotationversion[0][0];
	 * float tmpaccy1 = rotationversion[1][0]; float tmpaccz1 =
	 * rotationversion[2][0];
	 * 
	 * String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " " +
	 * rotationTime + "\n" + tmpaccx1 + " " + tmpaccy1 + " " + tmpaccz1 + " " +
	 * rotationTime + "\n" + tmpRotationx + " " + tmpRotationy + " " +
	 * tmpRotationz + " " + rotationTime + "\n" + calRotationx + " " +
	 * calRotationy + " " + calRotationz + " " + rotationTime + "\n"; byte[]
	 * buffer11 = new byte[sensorstr.length() * 2]; buffer11 =
	 * sensorstr.getBytes(); foStream.write(buffer11);
	 * 
	 * } sb.close(); foStream.close(); }
	 */

	public void getChangedAcc() throws IOException {
		float DT = 1.0f / 1000.0f;
		BufferedReader sb = new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(tmpStringOutput, true); // ���崫�������ݵ��������
		String s = sb.readLine();// �����һ��������
		s = s + "\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		foStream.write(buffer);// ������д��ȥ
		s = sb.readLine();
		String stringArray[] = s.split(" ");
		float preaccx = Float.parseFloat(stringArray[0]);
		float preaccy = Float.parseFloat(stringArray[1]);
		float preaccz = Float.parseFloat(stringArray[2]);

		long pretimeacc = Long.parseLong(stringArray[3]);

		float pregryx = Float.parseFloat(stringArray[8]);
		float pregryy = Float.parseFloat(stringArray[9]);
		float pregryz = Float.parseFloat(stringArray[10]);

		long pretimegry = Long.parseLong(stringArray[11]);

		float preRotationx = Float.parseFloat(stringArray[4]);
		float preRotationy = Float.parseFloat(stringArray[5]);
		float preRotationz = Float.parseFloat(stringArray[6]);
		while ((s = sb.readLine()) != null) {
			stringArray = s.split(" ");
			long accTime = Integer.parseInt(stringArray[3]);
			long grytime = Integer.parseInt(stringArray[11]);
			long rotationTime = Integer.parseInt(stringArray[7]);

			float tmpaccx = Float.parseFloat(stringArray[0]);
			float tmpaccy = Float.parseFloat(stringArray[1]);
			float tmpaccz = Float.parseFloat(stringArray[2]);

			float tmpgryx = Float.parseFloat(stringArray[8]);
			float tmpgryy = Float.parseFloat(stringArray[9]);
			float tmpgryz = Float.parseFloat(stringArray[10]);

			float tmpRotationx = Float.parseFloat(stringArray[4]);
			float tmpRotationy = Float.parseFloat(stringArray[5]);
			float tmpRotationz = Float.parseFloat(stringArray[6]);

			if (accTime != pretimeacc) {
				tmpaccx = (tmpaccx - preaccx) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccx * accTime - tmpaccx * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccy = (tmpaccy - preaccy) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccy * accTime - tmpaccy * pretimeacc)
						/ (accTime - pretimeacc);
				tmpaccz = (tmpaccz - preaccz) / (accTime - pretimeacc)
						* rotationTime
						+ (preaccz * accTime - tmpaccz * pretimeacc)
						/ (accTime - pretimeacc);
			} else {
				tmpaccx = preaccx;
				tmpaccy = preaccy;
				tmpaccz = preaccz;
			}
			if (grytime != pretimegry) {
				tmpgryx = (tmpgryx - pregryx) / (grytime - pretimegry)
						* rotationTime
						+ (pregryx * grytime - tmpgryx * pretimegry)
						/ (grytime - pretimegry);
				tmpgryy = (tmpgryy - pregryy) / (grytime - pretimegry)
						* rotationTime
						+ (pregryy * grytime - tmpgryy * pretimegry)
						/ (grytime - pretimegry);
				tmpgryz = (tmpgryz - pregryz) / (grytime - pretimegry)
						* rotationTime
						+ (pregryz * grytime - tmpgryz * pretimegry)
						/ (grytime - pretimegry);

			} else {
				tmpgryx = pregryx;
				tmpgryy = pregryy;
				tmpgryz = pregryz;
			}
			// �õ��������

			float mx = pregryx * (rotationTime - pretimegry);
			float my = pregryy* (rotationTime - pretimegry);
			float mz =pregryz* (rotationTime - pretimegry);

			float m = (float) Math.sqrt(mx * mx + my * my + mz * mz);
			float q1, q2, q3, q4;
			if (m != 0) {
				q1 = (float) Math.cos(m / 2 * DT);
				q2 = (float) (mx / m * Math.sin(m / 2 * DT));
				q3 = (float) (my / m * Math.sin(m / 2 * DT));
				q4 = (float) (mz / m * Math.sin(m / 2 * DT));
			} else {
				q1 = (float) Math.cos(m / 2);
				q2 = 0;
				q3 = 0;
				q4 = 0;
			}

			float preRotation = 1 - preRotationx * preRotationx - preRotationy
					* preRotationy - preRotationz * preRotationz;

			float calRotation = q1 * preRotation - q2 * preRotationx - q3
					* preRotationy - q4 * preRotationz;
			float calRotationx = q2 * preRotation + q1 * preRotationx + q4
					* preRotationy - q3 * preRotationz;
			float calRotationy = q3 * preRotation - q4 * preRotationx + q1
					* preRotationy + q2 * preRotationz;
			float calRotationz = q4 * preRotation + q3 * preRotationx - q2
					* preRotationy + q1 * preRotationz;

			preaccx = Float.parseFloat(stringArray[0]);
			preaccy = Float.parseFloat(stringArray[1]);
			preaccz = Float.parseFloat(stringArray[2]);
			pretimeacc = accTime;

			pregryx = tmpgryx;
			pregryy = tmpgryy;
			pregryz = tmpgryz;
			pretimegry = rotationTime;

			preRotationx = calRotationx;
			preRotationy = calRotationy;
			preRotationz = calRotationz;

			float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0},
					{tmpaccz, 0, 0}};// ǰ�������Ǽ��ٶ�
			float[] rotationVect = {calRotationx, calRotationy, calRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect);
			float[][] rotationversion = new float[3][];
			float[][] mk = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk, bufferacc);
			tmpaccx = rotationversion[0][0];
			tmpaccy = rotationversion[1][0];
			tmpaccz = rotationversion[2][0];

			float[] rotationVect1 = {tmpRotationx, tmpRotationy, tmpRotationz};
			SensorManager.getRotationMatrixFromVector(mRotationMatrix,
					rotationVect1);
			float[][] mk1 = {
					{mRotationMatrix[0], mRotationMatrix[1], mRotationMatrix[2]},
					{mRotationMatrix[3], mRotationMatrix[4], mRotationMatrix[5]},
					{mRotationMatrix[6], mRotationMatrix[7], mRotationMatrix[8]}};
			rotationversion = maxtrixmutiply(mk1, bufferacc);
			float tmpaccx1 = rotationversion[0][0];
			float tmpaccy1 = rotationversion[1][0];
			float tmpaccz1 = rotationversion[2][0];

			String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " "
					+ rotationTime + "\n" + tmpaccx1 + " " + tmpaccy1 + " "
					+ tmpaccz1 + " " + rotationTime + "\n" + tmpRotationx + " "
					+ tmpRotationy + " " + tmpRotationz + " " + rotationTime
					+ "\n" + calRotationx + " " + calRotationy + " "
					+ calRotationz + " " + rotationTime + "\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			buffer11 = sensorstr.getBytes();
			foStream.write(buffer11);

		}
		sb.close();
		foStream.close();
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (new File(realString).exists()) {
			Toast.makeText(this, "���Ƚ��Ѿ����ɵ����ݼ���", Toast.LENGTH_SHORT);
			return false;
		}
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			wc = 0;
			vibrator.vibrate(200);
			view.setBackgroundResource(R.drawable.button1);

			final Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("ȷ�����룿");
			builder.setPositiveButton("ȷ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							try {
								try {
									getChangedAcc();
									// new File(tmpString).delete();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								FileInputStream Instream = new FileInputStream(
										tmpStringOutput);
								File sensortmp = new File(tmpStringOutput);
								long length = sensortmp.length();
								byte[] buffer = new byte[(int) (length * 2)];
								Instream.read(buffer);
								Instream.close();
								FileOutputStream Outstream = new FileOutputStream(
										realString, true);
								Outstream.write(buffer);
								Outstream.close();
								// sensortmp.delete(); // ���ļ�ɾ
								do_num++;
								TextView numdis = (TextView) findViewById(R.id.numdis);
								numdis.setText(Integer.toString(do_num));
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} // ���崫�������ݵ������
							catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
			builder.setNegativeButton("ȡ��",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							File sensor = new File(tmpString); // ��ȡ�ļ�����
							sensor.delete(); // ���ļ�ɾ
							new File(tmpStringOutput).delete();
						}
					});
			builder.create().show();
			/*
			 * try { getChangedAcc(); } catch (IOException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); }
			 */

		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {

			EditText numEditText = (EditText) findViewById(R.id.num);
			if (numEditText.getEditableText().toString().equals("")) {
				Toast.makeText(this, "label wrong!", Toast.LENGTH_LONG).show();
				return false;
			}
			labelnum = Integer.parseInt(numEditText.getEditableText()
					.toString());
			String numString = numEditText.getEditableText().toString();
			view.setBackgroundResource(R.drawable.button3);
			String sensorstr = numString + "\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			FileOutputStream foStream;
			try {
				foStream = new FileOutputStream(tmpString, true);
				buffer11 = sensorstr.getBytes();
				foStream.write(buffer11);
				foStream.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} // ���崫�������ݵ������
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			wc = 1;

		}
		return false;
	}

}
