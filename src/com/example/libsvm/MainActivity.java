package com.example.libsvm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.example.collectdata.*;
import com.example.exec.svm_predict;
import com.example.exec.svm_train;
import com.example.filechose.FileChooserActivity;
import com.example.handledata.translatedata;
import com.example.preproccess.PCA_done;
import com.example.preproccess.movement_classfied;

public class MainActivity extends Activity implements View.OnClickListener {

	private static String TAG = "MainActivity";
	public static final String EXTRA_FILE_CHOOSER = "file_chooser";
	private Button mBtOpenFile;
	private Button mBtpredict;
	private Button mBtmodel;
	private Intent fileChooserIntent;
	private Intent read_train;
	private Intent read_predict;
	private Intent read_static;
	private static final int REQUEST_CODE_train = 1; // 请求码
	private static final int REQUEST_CODE_model = 2; // 请求码
	private static final int REQUEST_CODE_predict = 3; // 请求码
	private String path_train = "";
	private String path_model = "";
	private String path_predict = "";
	private translatedata translatedata;
	private float[] accextracts;
	private float[] grytracts;
	private float[] spdextracts;
	private float[] vartracts;
	private String[] rFtracts;
	private float[] accxmeanshift_extracts;
	private int window_length = 120;
	private int window_shift = 20;

	private String trainfile = "/sdcard/train/data/sensortestacc.txt";
	private String prefile = "/sdcard/train/data/sensortestaccpre.real";
	private String pre_result = "/sdcard/train/feature/features_extract_pre_pd.txt";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO Auto-generated method stub
		/*
		 * String[] arg = { "//sdcard/svm/heart_scale.txt", // 存放SVM训练模型用的数据的路径
		 * "//sdcard/svm/model_r.txt" }; // 存放SVM通过训练数据训/ //练出来的模型的路径
		 * 
		 * String[] parg = { "//sdcard/svm/heart_scale.txt", // 这个是存放测试数据
		 * "//sdcard/svm/model_r.txt", // 调用的是训练以后的模型 "//sdcard/svm/out_r.txt"
		 * }; // 生成的结果的文件的路径
		 * 
		 * // svm_train t = new svm_train(); // svm_predict p = new
		 * svm_predict(); try { svm_train.main(arg); svm_predict.main(parg); //
		 * 调用 } catch (IOException e) { // TODO Auto-generated catch block
		 * e.printStackTrace(); } // 调用
		 */
		setContentView(R.layout.activity_main);
		mBtOpenFile = (Button) findViewById(R.id.btOpenFile);
		mBtOpenFile.setOnClickListener(this);

		mBtpredict = (Button) findViewById(R.id.btpredict);
		mBtpredict.setOnClickListener(this);

		mBtOpenFile = (Button) findViewById(R.id.btmodel);
		mBtOpenFile.setOnClickListener(this);
		fileChooserIntent = new Intent(this, FileChooserActivity.class);

		// ComponentName componentName=new ComponentName("com.example",
		// "com.example.collectdata.read_train");
		// read_train=new Intent();
		// read_train.setComponent(componentName);
		// read_train.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		read_train = new Intent(this, read_train.class);
		read_predict = new Intent(this, read_predict.class);
		read_static = new Intent(this, read_static.class);

		File dirFile = new File("/sdcard/train/data");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

	}

	public void onClick(View v) {
		switch (v.getId()) {

			case R.id.btOpenFile :
				Log.v("111", "ok0");
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED))
					startActivityForResult(fileChooserIntent,
							REQUEST_CODE_train);
				else
					toast(getText(R.string.sdcard_unmonted_hint));
				break;
			case R.id.btpredict :
				Log.v("111", "ok1");
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED))
					startActivityForResult(fileChooserIntent,
							REQUEST_CODE_predict);
				else
					toast(getText(R.string.sdcard_unmonted_hint));

				break;
			case R.id.btmodel :
				Log.v("111", "ok2");
				if (Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED))
					startActivityForResult(fileChooserIntent,
							REQUEST_CODE_model);
				else
					toast(getText(R.string.sdcard_unmonted_hint));

				break;
			default :
				Log.v("111", "ok3");
				break;
		}
	}

	public void train(View view) {

		int c = path_train.lastIndexOf("/");
		if (c < 0) {
			Toast.makeText(this, "路径不正确", Toast.LENGTH_SHORT).show();
			return;
		}
		String nametxt = path_train.substring(c);
		c = nametxt.lastIndexOf(".");
		String name = nametxt.substring(0, c).concat("_out.txt");
		String path_out = path_train.replaceAll(nametxt, name);
		String[] arg = {path_train, path_out};
		try {
			svm_train.main(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Toast.makeText(this, "训练成功", Toast.LENGTH_SHORT).show();
		// Log.v("train",path_out);
		// Log.v("train",String.valueOf(c));

	}

	public void predict(View view) {

		if (path_model.equals("") | path_predict.equals("")) {
			Toast.makeText(this, "路径不正确", Toast.LENGTH_SHORT).show();
			return;
		}
		int c = path_predict.lastIndexOf("/");
		String nametxt = path_predict.substring(c);
		c = nametxt.lastIndexOf(".");
		String name = nametxt.substring(0, c).concat("_pd.txt");
		String path_pd = path_predict.replaceAll(nametxt, name);
		String[] arg = {path_predict, path_model, path_pd};
		try {
			svm_predict.main(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void read_train(View view) {

		startActivity(read_train);

	}

	public void read_predict(View view) {

		startActivity(read_predict);

	}

	public void train_static(View view) throws IOException {

		startActivity(read_static);
		/*
		 * translatedata translatedata_train=new
		 * translatedata("/sdcard/sensor"); translatedata_train.file_array();
		 * translatedata_train
		 * .getposition(translatedata_train.floatcollectArray, false);
		 */
	}

	// 用作模块正确性测试
	public void start_pre(View view) throws IOException {
		// movement_classfied moveClassfied=new
		// movement_classfied("//sdcard/sensortestaccpre.real",window_length,window_shift);
		// moveClassfied.Data_splited();
		// 预测数据分割在这里
		/*
		 * translatedata translatedata=new
		 * translatedata("/sdcard/sensortestacc.txt");
		 * translatedata.file_array(); translatedata.extract(true);
		 */
		File trainFile = new File(prefile);
		if (!trainFile.exists() || (trainFile.length() == 0)) {
			toast("预测的数据不存在");
			return;
		}
		movement_classfied pre_classfied = new movement_classfied(prefile,
				window_length, window_shift);
		pre_classfied.Data_splited();
		// new
		// PCA_done("//sdcard/train/predict_real","/sdcard/train/data/PCA_pre");//PCA模块
		// translatedata translatedata_train=new
		// translatedata("/sdcard/train/data/PCA_pre");
		translatedata translatedata_train = new translatedata(
				"//sdcard/train/predict_real");
		translatedata_train.file_array();
		if (translatedata_train.floatcollectArray.size() > 1000) {
			toast("数据太长了，请重新输入");
			return;
		}
		translatedata_train.extract(false);
		new File("/sdcard/train/data/PCA_pre").delete();
		new File(prefile).delete();
		new File("//sdcard/train/predict_real").delete();

		path_predict = "//sdcard/train/feature/features_extract_pre.txt";
		path_model = "//sdcard/train/feature/features_extract_train_out.txt";
		int c = path_predict.lastIndexOf("/");
		String nametxt = path_predict.substring(c);
		c = nametxt.lastIndexOf(".");
		String name = nametxt.substring(0, c).concat("_pd.txt");
		String path_pd = path_predict.replaceAll(nametxt, name);
		String[] arg = {path_predict, path_model, path_pd};
		try {
			svm_predict.main(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void start_train(View view) throws IOException {

		File trainFile = new File(trainfile);
		if (!trainFile.exists() || (trainFile.length() == 0)) {
			toast("训练的数据不存在");
			return;
		}
		 new PCA_done(trainfile,"/sdcard/train/data/PCA_train");//PCA模块
		 translatedata translatedata_train=new
		 translatedata("/sdcard/train/data/PCA_train");
//		translatedata translatedata_train = new translatedata(trainfile);
		translatedata_train.file_array();
		/*
		 * if(translatedata_train.floatcollectArray.size()>1000) {
		 * toast("数据太长了，请重新输入"); return; }
		 */
		try {
			translatedata_train.extract(true);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		} finally {
			new File("/sdcard/train/data/PCA_train").delete();
			new File(trainfile).delete();
		}
	}

	public void pre_result(View view) throws IOException {
		File trainFile = new File(pre_result);
		if (!trainFile.exists() || (trainFile.length() == 0)) {
			toast("未产生结果数据");
			return;
		}
		translatedata translatedata_train = new translatedata(pre_result);
		translatedata_train.file_array();
		StringBuilder bufferBuilder = new StringBuilder();
		for (int i = 0; i < translatedata_train.floatcollectArray.size(); i++) {
			bufferBuilder.append(String.valueOf(i + 1));
			bufferBuilder.append(":");
			bufferBuilder.append(String
					.valueOf(translatedata_train.floatcollectArray.get(i)[0]));
			bufferBuilder.append(" ");
		}
		Dialog alertDialog = new AlertDialog.Builder(this).setTitle("预测的结果")
				.setMessage(bufferBuilder.toString()).create();
		alertDialog.show();

	}

	private void toast(CharSequence hint) {
		Toast.makeText(this, hint, Toast.LENGTH_SHORT).show();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.v(TAG, "onActivityResult#requestCode:" + requestCode
				+ "#resultCode:" + resultCode);
		if (resultCode == RESULT_CANCELED) {
			toast(getText(R.string.open_file_none));
			return;
		}
		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_train) {
			// 获取路径名
			String pptPath = data.getStringExtra(EXTRA_FILE_CHOOSER);
			Log.v(TAG, "onActivityResult # pptPath : " + pptPath);
			if (pptPath != null) {
				toast("Choose File : " + pptPath);

				path_train = pptPath;
			} else
				toast(getText(R.string.open_file_failed));
		} else if (resultCode == RESULT_OK
				&& requestCode == REQUEST_CODE_predict) {
			// 获取路径名
			String pptPath = data.getStringExtra(EXTRA_FILE_CHOOSER);
			Log.v(TAG, "onActivityResult # pptPath : " + pptPath);
			if (pptPath != null) {
				toast("Choose File : " + pptPath);

				path_predict = pptPath;
			} else
				toast(getText(R.string.open_file_failed));
		} else if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_model) {
			// 获取路径名
			String pptPath = data.getStringExtra(EXTRA_FILE_CHOOSER);
			Log.v(TAG, "onActivityResult # pptPath : " + pptPath);
			if (pptPath != null) {
				toast("Choose File : " + pptPath);

				path_model = pptPath;
			} else
				toast(getText(R.string.open_file_failed));
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
/*
 * FileOutputStream foStream=new FileOutputStream(
 * "//sdcard/sensortestacc.txt",true); //定义传感器数据的输出流 //File sensor1=new
 * File("//sdcard/sensortestacc.txt"); String
 * sensorstr=accd[0]+" "+accd[1]+" "+accd[2]+" "+timeacc+" "
 * +accdtest[0]+" "+accdtest[1]+" "+accdtest[2]+" "+"0"+" "
 * +rotation[0]+" "+rotation[1]+" "+rotation[2]+" "+timerotation+" "+
 * -Orientation
 * [0]*180/Math.PI+" "+-Orientation[1]*180/Math.PI+" "+-Orientation[2
 * ]*180/Math.PI+" "+"0"+"\n"; String sensorstr=2+" "+1+" "+3+" "+22+" "+
 * 6+" "+7+" "+8+" "+33+"\n"+-2+" "+-1+" "+-3+" "+22+" "+
 * 6+" "+7+" "+8+" "+33+"\n"+5+" "+2+" "+-3+" "+22+" "+
 * 6+" "+7+" "+8+" "+33+"\n";
 * 
 * byte[] buffer11=new byte[sensorstr.length()*2];
 * buffer11=sensorstr.getBytes(); foStream.write(buffer11); foStream.close();
 * 
 * try { translatedata.file_array();
 * 
 * vartracts =
 * translatedata.Variance_extract(translatedata.floatcollectArray,false);
 * rFtracts = translatedata.RF_extract(translatedata.floatcollectArray,false);
 * accxmeanshift_extracts =
 * translatedata.accxmeanshift_extract(translatedata.floatcollectArray,false);
 * 
 * 
 * File file11=new File("//sdcard/sensortestacc.txt"); file11.delete(); } catch
 * (IOException e) { // TODO Auto-generated catch block e.printStackTrace(); }
 */
