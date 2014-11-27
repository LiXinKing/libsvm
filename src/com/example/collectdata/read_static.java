package com.example.collectdata;

import java.io.File;
import com.example.libsvm.R;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;

import com.example.handledata.translatedata;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class read_static extends Activity implements OnTouchListener {
	private volatile int wc = 0; // �������ݼ�¼����/��ͣ�Ŀ��Ʊ�ǣ�1�����¼���ݣ�0������ͣ��¼
	private volatile int wd = 1;
	private SensorManager mySensorManager; // SensorManager��������
	private Sensor myaccelerometer; // ���ٶȴ�����������������
	private Sensor myrotationSensor;
	private Sensor mygrySensor;
	private Button writebu;
	private Button statebButton;
	private float gyrd[]; // ���ڴ�����µ�����������
	private float accd[]; // ���ڴ�����µļ��ٶȴ���������
	private float accdtest[]; // ���ڴ�����µļ��ٶȴ���������
	private float gravity[];
	private float rotation[];
	private float[] mRotationMatrix = new float[9];
	private final Timer timer = new Timer();
	private Handler handler;// ���ݽ��ն�
	private long time;
	private long timeacc;
	private long timerotation;
	private Vibrator vibrator; // ��
	private long timegyr;
	private int labelnum;
	private long bytenum = 0;
	private long lastbyte;
	// �������Ҫ����7λ��������Ƚϻ����
	private int period = 0;
	private boolean state = true;
	// RandomAccessFile randomfile;
	private Button start_stopButton;

	private String tmpString = "//sdcard/train/data/static.tmp";
	private String realString = "//sdcard/train/data/static.real";
	private String featureString = "//sdcard/train/data/staticfeature.txt";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.read_static);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		gyrd = new float[3];
		accd = new float[3];
		accdtest = new float[3];
		gravity = new float[3];
		rotation = new float[3];
		wc = 0; // Ĭ�Ͽ������������ݼ�¼�����Ʊ��Ϊ1
		writebu = (Button) findViewById(R.id.writecon);// ���ڿ������ݼ�¼����/��ͣ��ť����ʾ
		writebu.setOnTouchListener(this);
		statebButton = (Button) findViewById(R.id.state);
		// ���SensorManager����
		mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		// ��ȡȱʡ�����Լ��ٶȴ�����
		myaccelerometer = mySensorManager
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		myrotationSensor = mySensorManager
				.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		mygrySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		File sensortmp = new File(tmpString);
		File sensorreal = new File(realString);
		// timer.schedule(task,0, SAMPLET);
		// //��һ����ʱ0ms��֮��ÿSAMPLETʱ�䣬��¼һ����ٶȴ�����������������
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
				mygrySensor, // ����������
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
	public void onClick_delete(View view) throws IOException // ���������Ķ���
	{
		// File sensor=new File("//sdcard/sensortestaccpre.txt"); //��ȡ�ļ�����
		File sensortmp = new File(tmpString); // ��ȡ�ļ�����
		File sensortestaccreal = new File(realString); // ��ȡ�ļ�����
		// sensor.delete(); //���ļ�ɾ
		sensortmp.delete();
		sensortestaccreal.delete();
		/*
		 * File sensor1=new File("//sdcard/sensortestacc.txt"); //��ȡ�ļ�����\
		 * RandomAccessFile randomfile=new RandomAccessFile(sensor1,"rw");
		 * randomfile.seek(bytenum-lastbyte);
		 */
		// randomfile.
		// sensor1.delete(); //���ļ�ɾ��
		vibrator.vibrate(200);
	}
	public void onClick_state(View view) // ���������Ķ���
	{
		state = !state;
		if (state)
			statebButton.setText("��ֹ");
		else
			statebButton.setText("�˶�");

	}

	private SensorEventListener mySensorListener = new SensorEventListener() {// ����ʵ����SensorEventListener�ӿڵĴ�����������
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		@Override
		public void onSensorChanged(SensorEvent event) {

			// ��������ȡ����������£���ȡ����������
			// ��ϵͳ�տ�ʼ��ȡ���ٶȵ�ʱ�򣬼�¼��ϵͳ��ʱ�䲻�ȶ�����ò��Ǵ�һ����ʼ����
			long time = System.currentTimeMillis();
			time = time - time / 10000000 * 10000000;
			float[] values = event.values;// ��ȡ����������������
			// �����Ǵ������仯
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				// С��ACCMINʱ������Χ
				accd[0] = values[0];
				accd[1] = values[1];
				accd[2] = values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				accdtest[0] = accd[0];
				accdtest[1] = accd[1];
				accdtest[2] = accd[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
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
								tmpString, true); // ���崫�������ݵ������
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
				rotation[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timerotation = time;

			} else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				gyrd[0] = (float) values[0];
				gyrd[1] = (float) values[1];
				gyrd[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
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

	// ��������ʽ�ļ���
	public static float getHL3(float[] input) {
		float unm1 = input[0] * (input[4] * input[8] - input[5] * input[7]);
		float unm2 = -input[1] * (input[3] * input[8] - input[5] * input[6]);
		float unm3 = input[2] * (input[3] * input[7] - input[4] * input[6]);
		return unm1 + unm2 + unm3;
	}

	private static float[][] matrixinversion(float[] input) {
		// ���������ʽ
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
	}// һ������˷�

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			wc = (wc + 1) % 2;
			writebu.setText("��ʼ");
			vibrator.vibrate(200);
			if (wc == 0) {

				writebu.setText("��ͣ");
				final Builder builder = new AlertDialog.Builder(this);
				builder.setMessage("ȷ�����룿");
				builder.setPositiveButton("ȷ��",
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
									sensortmp.delete(); // ���ļ�ɾ

								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} // ���崫�������ݵ������
								catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								translatedata translatedatastatic = new translatedata(
										realString);
								try {
									translatedatastatic.file_array();
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								float[] accxmeanshift_extract = translatedatastatic
										.accxmeanshift_extract(
												translatedatastatic.floatcollectArray,
												false);
								float[] grymean_extract = translatedatastatic
										.grymean_extract(
												translatedatastatic.floatcollectArray,
												false);
								try {
									FileOutputStream train_model = new FileOutputStream(
											featureString, true);
									float[] conn = new float[accxmeanshift_extract.length
											+ grymean_extract.length];
									System.arraycopy(accxmeanshift_extract, 0,
											conn, 0,
											accxmeanshift_extract.length);
									System.arraycopy(grymean_extract, 0, conn,
											accxmeanshift_extract.length,
											grymean_extract.length);
									StringBuilder featurebuilder = new StringBuilder();
									int state_real = 1;
									if (state)
										state_real = 1;
									else
										state_real = 0;
									featurebuilder.append(String
											.valueOf(state_real));
									featurebuilder.append(" ");
									int connlen = conn.length;
									for (int k = 0; k < connlen; k++) {
										featurebuilder.append(String
												.valueOf(k + 1));
										featurebuilder.append(":");
										featurebuilder.append(String
												.valueOf(conn[k]));
										featurebuilder.append(" ");
									}
									featurebuilder.append("\n");
									String feature = featurebuilder.toString();
									byte[] buffer = new byte[feature.length() * 2];
									buffer = feature.getBytes();
									train_model.write(buffer);
									train_model.close();
									File sensortreal = new File(realString);
									sensortreal.delete();
								} catch (FileNotFoundException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						});
				builder.setNegativeButton("ȡ��",
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								// TODO Auto-generated method stub
								File sensor = new File(tmpString); // ��ȡ�ļ�����
								sensor.delete(); // ���ļ�ɾ
							}
						});
				builder.create().show();

			}

		}
		return false;
	}
}
