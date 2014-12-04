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
import android.graphics.Matrix;
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
	private Sensor mygraSensor;
	private Sensor mymangSensor;
	private Button writebu;
	private float gyrd[]; // ���ڴ�����µ�����������
	private float accd[]; // ���ڴ�����µļ��ٶȴ���������
	private float accdtest[]; // ���ڴ�����µļ��ٶȴ���������
	private float gravity[];
	private float rotation[];
	private float mang[];
	private float[] mRotationMatrix = new float[9];
	private long time;
	private long timeacc;
	private long timerotation;
	private long timegyr;
	private long timegra;
	private long timemang;
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

	private String Stringgry = "//sdcard/train/data/gry";
	private String Stringgra = "//sdcard/train/data/gra";
	private String Stringacc = "//sdcard/train/data/acc";
	private String Stringmag = "//sdcard/train/data/mag";
	private String Stringrotation = "//sdcard/train/data/rot";
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
		mang = new float[3];
		wc = 0; // Ĭ�Ͽ������������ݼ�¼�����Ʊ��Ϊ1
		// ���SensorManager����
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// ��ȡȱʡ�����Լ��ٶȴ�����
		myaccelerometer = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		myrotationSensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mygyrSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mygraSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
		mymangSensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
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
		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				mygraSensor, // ����������
				SensorManager.SENSOR_DELAY_FASTEST // �������¼����ݵ�Ƶ��
				);
		mySensorManager.registerListener(mySensorListener, // ��Ӽ���
				mymangSensor, // ����������
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
			// �����Ǵ������仯
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// С��ACCMINʱ������Χ
				accd[0] = values[0];
				accd[1] = values[1];
				accd[2] = values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timeacc = time;
				try {
					FileOutputStream foStream = new FileOutputStream(Stringacc,
							true); // ���崫�������ݵ������
					String sensorstr = accd[0] + " " + accd[1] + " " + accd[2]
							+ " " + timeacc + "\n";
					byte[] buffer11 = new byte[sensorstr.length() * 2];
					buffer11 = sensorstr.getBytes();
					foStream.write(buffer11);
					foStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
				// С��ACCMINʱ������Χ
				gravity[0] = values[0];
				gravity[1] = values[1];
				gravity[2] = values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timegra = time;
				try {
					FileOutputStream foStream = new FileOutputStream(Stringgra,
							true); // ���崫�������ݵ������
					String sensorstr = gravity[0] + " " + gravity[1] + " "
							+ gravity[2] + " " + timegra + "\n";
					byte[] buffer11 = new byte[sensorstr.length() * 2];
					buffer11 = sensorstr.getBytes();
					foStream.write(buffer11);
					foStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
				// С��ACCMINʱ������Χ
				mang[0] = values[0];
				mang[1] = values[1];
				mang[2] = values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timemang = time;
				try {
					FileOutputStream foStream = new FileOutputStream(Stringmag,
							true); // ���崫�������ݵ������
					String sensorstr = mang[0] + " " + mang[1] + " " + mang[2]
							+ " " + timemang + "\n";
					byte[] buffer11 = new byte[sensorstr.length() * 2];
					buffer11 = sensorstr.getBytes();
					foStream.write(buffer11);
					foStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				gyrd[0] = (float) values[0];
				gyrd[1] = (float) values[1];
				gyrd[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timegyr = time;
				try {
					FileOutputStream foStream = new FileOutputStream(Stringgry,
							true); // ���崫�������ݵ������
					String sensorstr = gyrd[0] + " " + gyrd[1] + " " + gyrd[2]
							+ " " + timegyr + "\n";
					byte[] buffer11 = new byte[sensorstr.length() * 2];
					buffer11 = sensorstr.getBytes();
					foStream.write(buffer11);
					foStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
				rotation[0] = (float) values[0];
				rotation[1] = (float) values[1];
				rotation[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timerotation = time;
				try {
					FileOutputStream foStream = new FileOutputStream(Stringrotation,
							true); // ���崫�������ݵ������
					String sensorstr = rotation[0] + " " +rotation[1] + " " + rotation[2]
							+ " " + timerotation + "\n";
					byte[] buffer11 = new byte[sensorstr.length() * 2];
					buffer11 = sensorstr.getBytes();
					foStream.write(buffer11);
					foStream.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	};

	public void accTranslation(float[][] R, float[] gravity, float[] geomagnetic) {
		float Ax = gravity[0];
		float Ay = gravity[1];
		float Az = gravity[2];
		final float Ex = geomagnetic[0];
		final float Ey = geomagnetic[1];
		final float Ez = geomagnetic[2];
		float Hx = Ey * Az - Ez * Ay;
		float Hy = Ez * Ax - Ex * Az;
		float Hz = Ex * Ay - Ey * Ax;
		final float normH = (float) Math.sqrt(Hx * Hx + Hy * Hy + Hz * Hz);
		final float invH = 1.0f / normH;
		Hx *= invH;
		Hy *= invH;
		Hz *= invH;
		final float invA = 1.0f / (float) Math
				.sqrt(Ax * Ax + Ay * Ay + Az * Az);
		Ax *= invA;
		Ay *= invA;
		Az *= invA;
		final float Mx = Ay * Hz - Az * Hy;
		final float My = Az * Hx - Ax * Hz;
		final float Mz = Ax * Hy - Ay * Hx;
		if (R != null) {
			R[0][0] = Hx;
			R[0][1] = Hy;
			R[0][2] = Hz;
			R[1][0] = Mx;
			R[1][1] = My;
			R[1][2] = Mz;
			R[2][0] = Ax;
			R[2][1] = Ay;
			R[2][2] = Az;
		}
		return;
	}

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

	public void getChangedAcc() throws IOException {
		BufferedReader sb = new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(tmpStringOutput, true); // ���崫�������ݵ������
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
		long pretime = Long.parseLong(stringArray[3]);
		while ((s = sb.readLine()) != null) {
			stringArray = s.split(" ");
			int accTime = Integer.parseInt(stringArray[3]);
			int rotationTime = Integer.parseInt(stringArray[7]);
			float tmpaccx = Float.parseFloat(stringArray[0]);
			float tmpaccy = Float.parseFloat(stringArray[1]);
			float tmpaccz = Float.parseFloat(stringArray[2]);

			float tmpRotationx = Float.parseFloat(stringArray[4]);
			float tmpRotationy = Float.parseFloat(stringArray[5]);
			float tmpRotationz = Float.parseFloat(stringArray[6]);
			if (accTime != pretime) {
				tmpaccx = (tmpaccx - preaccx) / (accTime - pretime)
						* rotationTime
						+ (preaccx * accTime - tmpaccx * pretime)
						/ (accTime - pretime);
				tmpaccy = (tmpaccy - preaccy) / (accTime - pretime)
						* rotationTime
						+ (preaccy * accTime - tmpaccy * pretime)
						/ (accTime - pretime);
				tmpaccz = (tmpaccz - preaccz) / (accTime - pretime)
						* rotationTime
						+ (preaccz * accTime - tmpaccz * pretime)
						/ (accTime - pretime);
			} else {
				tmpaccx = preaccx;
				tmpaccy = preaccy;
				tmpaccz = preaccz;
			}
			preaccx = Float.parseFloat(stringArray[0]);
			preaccy = Float.parseFloat(stringArray[1]);
			preaccz = Float.parseFloat(stringArray[2]);
			pretime = Long.parseLong(stringArray[3]);
			float[][] bufferacc = {{tmpaccx, 0, 0}, {tmpaccy, 0, 0},
					{tmpaccz, 0, 0}};// ǰ�������Ǽ��ٶ�
			float[] rotationVect = {tmpRotationx, tmpRotationy, tmpRotationz};
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

			String sensorstr = tmpaccx + " " + tmpaccy + " " + tmpaccz + " "
					+ rotationTime + " " + stringArray[8] + " "
					+ stringArray[9] + " " + stringArray[10] + " "
					+ stringArray[11] + "\n";
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
