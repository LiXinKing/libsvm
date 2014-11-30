package com.example.collectdata;

import java.io.File;

import com.example.libsvm.R;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
	private String tmpString = "//sdcard/train/data/sensortestacc.tmp";
	private String realString = "//sdcard/train/data/sensortestacc.txt";
	
	private String tmpStringOutput= "//sdcard/train/data/sensortestacc.tmp.out";

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
				.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
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
			if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
				// С��ACCMINʱ������Χ
				accd[0] = values[0];
				accd[1] = values[1];
				accd[2] = values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				accdtest[0] = accd[0];
				accdtest[1] = accd[1];
				accdtest[2] = accd[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timeacc = time;
			}

			else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
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
				gyrd[0] = (float) values[0];
				gyrd[1] = (float) values[1];
				gyrd[2] = (float) values[2]; // �����µļ��ٶȴ��������ݴ��ڼ��ٶȴ�����������
				timegyr = time;
				Log.v("1", "a");
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
		{
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
		new File("/sdcard/train/PCA_train").delete();
	}

	public void onClick_delete(View view) throws IOException // ���������Ķ���
	{
		do_num = 0;
		TextView numdis = (TextView) findViewById(R.id.numdis);
		numdis.setText(Integer.toString(do_num));
		File sensor = new File(realString); // ��ȡ�ļ�����
		sensor.delete(); // ���ļ�ɾ
		File sensortmp = new File(tmpString);
		sensortmp.delete();
		/*
		 * File sensor1=new File("//sdcard/sensortestacc.txt"); //��ȡ�ļ�����\
		 * RandomAccessFile randomfile=new RandomAccessFile(sensor1,"rw");
		 * randomfile.seek(bytenum-lastbyte);
		 */
		// randomfile.
		// sensor1.delete(); //���ļ�ɾ��
		vibrator.vibrate(200);
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

	public void  getChangedAcc() throws IOException {
		BufferedReader sb=new BufferedReader(new FileReader(tmpString));
		FileOutputStream foStream = new FileOutputStream(tmpStringOutput, true); // ���崫�������ݵ������
		String s=sb.readLine();//�����һ��������
		s=s+"\n";
		byte[] buffer = new byte[s.length() * 2];
		buffer = s.getBytes();
		foStream.write(buffer);//������д��ȥ
		s=sb.readLine();
		String stringArray[]=s.split(" ");
		float preaccx=0;
		float preaccy=0;
		float preaccz=0;
		long pretime=Integer.parseInt(stringArray[7]);
		while((s=sb.readLine())!=null){
			stringArray=s.split(" ");
			int accTime=Integer.parseInt(stringArray[3]);
			int rotationTime=Integer.parseInt(stringArray[7]);
			float tmpaccx=Float.parseFloat(stringArray[0]);
			float tmpaccy=Float.parseFloat(stringArray[1]); 
			float tmpaccz=Float.parseFloat(stringArray[2]);
			
			tmpaccx=(tmpaccx-preaccx)/(accTime-pretime)*rotationTime+(preaccx*accTime-tmpaccx*pretime)/(accTime-pretime);
			tmpaccy=(tmpaccy-preaccy)/(accTime-pretime)*rotationTime+(preaccy*accTime-tmpaccy*pretime)/(accTime-pretime);
			tmpaccz=(tmpaccz-preaccz)/(accTime-pretime)*rotationTime+(preaccz*accTime-tmpaccz*pretime)/(accTime-pretime);
			        float[][] bufferacc = {{ tmpaccx, 0, 0},
				{tmpaccy, 0, 0}, {tmpaccz, 0, 0}};//ǰ�������Ǽ��ٶ�
		SensorManager.getRotationMatrixFromVector(mRotationMatrix,
				rotation);
		float[][] rotationversion = matrixinversion(mRotationMatrix);
		float[][] mk = {
				{mRotationMatrix[0], mRotationMatrix[1],
						mRotationMatrix[2]},
				{mRotationMatrix[3], mRotationMatrix[4],
						mRotationMatrix[5]},
				{mRotationMatrix[6], mRotationMatrix[7],
						mRotationMatrix[8]}};
		rotationversion = maxtrixmutiply(mk, bufferacc);
		accd[0] = rotationversion[0][0];
		accd[1] = rotationversion[1][0];
		accd[2] = rotationversion[2][0];

			String sensorstr = accd[0] + " " + accd[1] + " "
					+ accd[2] + " " + timeacc + " " + gyrd[0] + " "
					+ gyrd[1] + " " + gyrd[2] + " " + timegyr
					+ "\n";
			byte[] buffer11 = new byte[sensorstr.length() * 2];
			buffer11 = sensorstr.getBytes();
			foStream.write(buffer11);
		}
		sb.close();
		foStream.close();
	}
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		// TODO Auto-generated method stub
		if (event.getAction() == MotionEvent.ACTION_UP) {
			wc = 0;
			vibrator.vibrate(200);
			try {
				getChangedAcc();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			/*
			 * view.setBackgroundResource(R.drawable.button1); final Builder
			 * builder = new AlertDialog.Builder(this);
			 * builder.setMessage("ȷ�����룿");
			 * 
			 * builder.setPositiveButton("ȷ��", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * {
			 * 
			 * // TODO Auto-generated method stub try {
			 * 
			 * FileInputStream Instream = new FileInputStream( tmpString); File
			 * sensortmp = new File(tmpString); long length =
			 * sensortmp.length(); byte[] buffer = new byte[(int) (length * 2)];
			 * Instream.read(buffer); Instream.close(); FileOutputStream
			 * Outstream = new FileOutputStream( realString, true);
			 * Outstream.write(buffer); Outstream.close(); sensortmp.delete();
			 * // ���ļ�ɾ do_num++; TextView numdis = (TextView)
			 * findViewById(R.id.numdis);
			 * numdis.setText(Integer.toString(do_num));
			 * 
			 * } catch (FileNotFoundException e) { // TODO Auto-generated catch
			 * block e.printStackTrace(); } // ���崫�������ݵ������ catch (IOException e)
			 * { // TODO Auto-generated catch block e.printStackTrace(); } }
			 * 
			 * }); builder.setNegativeButton("ȡ��", new
			 * DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { File sensor = new File(tmpString); // ��ȡ�ļ����� sensor.delete();
			 * // ���ļ�ɾ
			 * 
			 * } }); builder.create().show();
			 */
			/*
			 * File sensor1=new File("//sdcard/sensortestacc.txt"); //��ȡ�ļ�����\
			 * lastbyte=sensor1.length();"//sdcard/sensortestacc.tmp"
			 * bytenum+=sensor1.length();
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
