package com.example.handledata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.preproccess.movement_classfied;

import android.R.integer;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

public class translatedata {
	public ArrayList<float[]> floatcollectArray = new ArrayList<float[]>();
	private final static int accslt = 0;
	private final static int gryslt = 4;
	private final static int timepp = 3;
	// private final int time=10;
	private String path_origin;
	private int window_length = 120;
	private int window_shift = 20;
	public translatedata(String path_originpass) {

		path_origin = path_originpass;
		File dirFile = new File("//sdcard/train/feature/");
		if (!dirFile.exists()) {
			dirFile.mkdirs();
		}

	}

	public void file_array() throws IOException {
		File path_file = new File(path_origin);
		if (!(path_file.exists())) {
			path_file.createNewFile();
		}
		FileInputStream fis;

		fis = new FileInputStream(path_origin);

		byte[] buff = new byte[24];
		int hasread = 0;
		StringBuilder sBuilder = new StringBuilder();
		String receiveString;
		while ((hasread = fis.read(buff)) > 0)// 读取文件中的数据，多次调用read函数光标始终在往下走
		{
			sBuilder.append(new String(buff, 0, hasread));

		}
		receiveString = sBuilder.toString();
		int measure = receiveString.length();
		StringBuilder bufferbuild = new StringBuilder();
		ArrayList<Float> floatbuffer = new ArrayList<Float>();
		
		for (int i = 0, k = 0; i < measure; i++) {
			char c = receiveString.charAt(i);
			if ((c == 32)) {// 32的时候是空格将记的数据存入数组中

				String collect = bufferbuild.toString();
				bufferbuild.delete(0, collect.length());
				Log.v("strtest", collect);

				try {
					float m = Float.parseFloat(collect);
					Log.v("numtest", String.valueOf(m));
					floatbuffer.add(m);
				} catch (Exception NumberFormatException) {

					Log.v("NumberFormatException", "OK");
				}
				// 这里try和catch必须要用，否则会出现错误，用了之后不用改也有效果
				// 可能是系统为了避免可能存在的错误
			} else if (c == 10) {// 10的时候是换行
				String collect = bufferbuild.toString();
				bufferbuild.delete(0, collect.length());
				try {
					float m = Float.parseFloat(collect);
					floatbuffer.add(m);
				} catch (Exception NumberFormatException) {

					Log.v("NumberFormatException", "OK");
				}
				Float[] arraybuffer = new Float[floatbuffer.size()];
				for (int j = 0; j < floatbuffer.size(); j++) {
					arraybuffer[j] = floatbuffer.get(j);
				}

				float[] buffer = new float[arraybuffer.length + 3];
				for (int j = 0; j < arraybuffer.length; j++) {
					buffer[j] = arraybuffer[j].floatValue();
				}

				floatcollectArray.add(buffer);

				floatbuffer.clear();
			} else if (((47 < c) && (c < 58)) || c == 46 || c == 45 || c == 69)
				bufferbuild.append(c);
		}
		if (floatcollectArray.size() > 2)// 为了splited_return而加的
			if (floatcollectArray.get(floatcollectArray.size() - 1).length != floatcollectArray
					.get(floatcollectArray.size() - 2).length)
				floatcollectArray.remove(floatcollectArray.size() - 1);
	}
	public void extract(boolean train_predict) throws IOException {

//		long len = floatcollectArray.size();
//		int recycle = 0;
//		int flagb = 0;
//		for (int l = 0; l < len; l++) {
//			if (isint(floatcollectArray.get(l)[0])) {
//				recycle++;
//				if (recycle == 1)
//					flagb = l;
//				else {
//					write_infile(flagb, l, train_predict);
//					flagb = l;
//				}
//
//			}
//
//		}
		write_infile(0, floatcollectArray.size(), train_predict);

	}
	// 如果是train的话先splited一下，predict由于已经splited了所以这里不用了,TRUE表示train
	private void write_infile(int flagb, int l, boolean train_predict)
			throws IOException {
		// 在采集的时候必须要把标志位放在第一个float[]中，所以其实floatinout中的有效数据是floatinout.size()-1
		// sublist是一个包前不包后的切割
		ArrayList<float[]> floatinout = new ArrayList<float[]>();
		ArrayList<float[]> floatinout_forsplited = new ArrayList<float[]>();
		List<float[]> floatinoutlist = floatcollectArray.subList(flagb, l);
		int listlen = floatinoutlist.size();
		for (int list = 0; list < listlen; list++) {
			floatinout.add(floatinoutlist.get(list));
		}
		// 要删除第一个元素，由于第一个元素是label位，会影响运动静止分类的，下面仍然用floatinout
		floatinout_forsplited = floatinout;
		float[] first_element = floatinout.get(0);
		floatinout_forsplited.remove(0);
		// printFloatArrarylist(floatinout_forsplited,"/sdcard/1122");
		if (train_predict) {
			movement_classfied movementclassfied = new movement_classfied(
					floatinout_forsplited, window_length, window_shift);
			floatinout_forsplited = movementclassfied.Data_splited_return();
		}
		floatinout = floatinout_forsplited;
		floatinout.add(0, first_element);
		// printFloatArrarylist(floatinout_forsplited,"/sdcard/112233");
		// 提取相关的特征
		float[] accextract = accxmean_extract(floatinout, true);
		// float[] grytract=grymean_extract(floatinout,true);
		// float[] spdextract=speedmean_extract(floatinout,true);
		// float[] Vartract=Variance_extract(floatinout,true);
		float[] energy = null;
		float[] IRQ = null;
		float[] corr = null;
		float[] rms = null;
		try {
			energy = energy_extract(floatinout, true);
			IRQ = IRQ_extract(floatinout, true);
			corr = corr_extract(floatinout, true);
			rms = RMS_extract(floatinout, true);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		float[] MAD = accxmeanshift_extract(floatinout, true);

		float[] conn = new float[accextract.length * 7];
		System.arraycopy(accextract, 0, conn, 0, 3);
		System.arraycopy(energy, 0, conn, 3, 3);
		System.arraycopy(MAD, 0, conn, 6, 3);
		System.arraycopy(IRQ, 0, conn, 9, 3);
		System.arraycopy(corr, 0, conn, 12, 3);
		System.arraycopy(rms, 0, conn, 15, 3);

		String[] RFtract = RF_extract_2D(floatinout, true);

		try {
			File sensorFile;
			FileOutputStream foStream;
			if (train_predict) {
				sensorFile = new File(
						"//sdcard/train/feature/features_extract_train.txt");
				foStream = new FileOutputStream(
						"//sdcard/train/feature/features_extract_train.txt",
						true); // 定义传感器数据的输出流
			} else {
				sensorFile = new File(
						"//sdcard//train/feature/features_extract_pre.txt");
				foStream = new FileOutputStream(
						"//sdcard//train/feature/features_extract_pre.txt",
						true); // 定义传感器数据的输出流
			}

			if (!sensorFile.isFile()) {
				sensorFile.createNewFile();
			}
			StringBuilder featurebuilder = new StringBuilder();
			featurebuilder.append(String.valueOf(target_extract(floatinout)));
			Log.v("target_extract", String.valueOf(target_extract(floatinout)));
			featurebuilder.append(" ");
			int connlen = conn.length;
			for (int k = 0; k < connlen; k++) {
				featurebuilder.append(String.valueOf(k + 1));
				featurebuilder.append(":");
				featurebuilder.append(String.valueOf(conn[k]));
				featurebuilder.append(" ");
			}
			/*
			 * for(int k=0;k<RFtract.length;k++) {
			 * featurebuilder.append(String.valueOf(++connlen));
			 * featurebuilder.append(":"); featurebuilder.append(RFtract[k]);
			 * featurebuilder.append(" "); }
			 */
			featurebuilder.append("\n");
			String feature = featurebuilder.toString();
			byte[] buffer = new byte[feature.length() * 2];
			buffer = feature.getBytes();
			foStream.write(buffer);
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
	public static float[] accxmean_extract(
			ArrayList<float[]> floatcollectArray, Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		// i=0为标志位，下同
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num - 1; i++) {
			float[] buffer = floatcollectArray.get(i);
			float[] buffer1 = floatcollectArray.get(i + 1);
			sumx += (buffer[accslt] + buffer1[accslt])
					* (buffer1[timepp] - buffer[timepp]) / 2;
			sumy += (buffer[accslt + 1] + buffer1[accslt + 1])
					* (buffer1[timepp] - buffer[timepp]) / 2;
			sumz += (buffer[accslt + 2] + buffer1[accslt + 2])
					* (buffer1[timepp] - buffer[timepp]) / 2;
		}

		float avrx = sumx
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float avry = sumy
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float avrz = sumz
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float acc[] = {avrx, avry, avrz};
		return acc;
	}
	public static float[] grymean_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += Math.abs(buffer[gryslt]);
			sumy += Math.abs(buffer[gryslt + 1]);
			sumz += Math.abs(buffer[gryslt + 2]);
		}
		float avrx = sumx / (num - iflag);
		float avry = sumy / (num - iflag);
		float avrz = sumz / (num - iflag);
		float gry[] = {avrx, avry, avrz};
		return gry;

	}
	/*
	 * RF特征：该特征主要表达的是加速度旋转的方向。Cxy来表示三维加速度映射到Z轴的分量，x>0,y>0时以编码0表示；x>0,y<0时以编码1来表示；
	 * x<0,y>0时以编码2来表示；x<0,y<0时以编码3来表示；然后Cxz和Cyz类推。
	 */
	public String[] RF_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		StringBuilder bufferCxy = new StringBuilder();
		StringBuilder bufferCxz = new StringBuilder();
		StringBuilder bufferCyz = new StringBuilder();
		int num = floatcollectArray.size();
		int Cxy = 0, Cxyl = 0;
		int Cxz = 0, Cxzl = 0;
		int Cyz = 0, Cyzl = 0;
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		// 提取Cxy
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			if (i == iflag + 1) {
				Cxyl = Cxy;
				Cxzl = Cxz;
				Cyzl = Cyz;
			}
			if ((buffer[accslt] > 0.5) && (buffer[accslt + 1] > 0.5))
				Cxy = 0;
			else if ((buffer[accslt] > 0.5) && (buffer[accslt + 1] < -0.5))
				Cxy = 1;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 1] > 0.5))
				Cxy = 2;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 1] < -0.5))
				Cxy = 3;

			if ((buffer[accslt] > 0.5) && (buffer[accslt + 2] > 0.5))
				Cxz = 0;
			else if ((buffer[accslt] > 0.5) && (buffer[accslt + 2] < -0.5))
				Cxz = 1;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 2] > 0.5))
				Cxz = 2;
			else if ((buffer[accslt] < -0.5) && (buffer[accslt + 2] < -0.5))
				Cxz = 3;

			if ((buffer[accslt + 1] > 0.5) && (buffer[accslt + 2] > 0.5))
				Cyz = 0;
			else if ((buffer[accslt + 1] > 0.5) && (buffer[accslt + 2] < -0.5))
				Cyz = 1;
			else if ((buffer[accslt + 1] < -0.5) && (buffer[accslt + 2] > 0.5))
				Cyz = 2;
			else if ((buffer[accslt + 1] < -0.5) && (buffer[accslt + 2] < -0.5))
				Cyz = 3;
			if (i == iflag) {
				bufferCxy.append(String.valueOf(Cxy));
				bufferCxz.append(String.valueOf(Cxz));
				bufferCyz.append(String.valueOf(Cyz));
				continue;
			}

			if (!(Cxy == Cxyl)) {
				bufferCxy.append(String.valueOf(Cxy));
				Cxyl = Cxy;
			}
			if (!(Cxz == Cxzl)) {
				bufferCxz.append(String.valueOf(Cxz));
				Cxzl = Cxz;
			}
			if (!(Cyz == Cyzl)) {
				bufferCyz.append(String.valueOf(Cyz));
				Cyzl = Cyz;
			}

		}
		String[] bufferStrings = {bufferCxy.toString(), bufferCxz.toString(),
				bufferCyz.toString()};
		return bufferStrings;
	}

	/*
	 * public String[] RF_extract_2D(ArrayList<float[]>
	 * floatcollectArray,Boolean flag) { StringBuilder bufferCxy = new
	 * StringBuilder(); int num=floatcollectArray.size(); int Cxy=0,Cxyl=0; int
	 * iflag; if(flag){ iflag=1; } else { iflag=0; } //提取Cxy for(int
	 * i=iflag;i<num;i++) { float[] buffer=floatcollectArray.get(i);
	 * if(i==iflag+1){Cxyl=Cxy;}
	 * if((buffer[accslt]>1)&&(buffer[accslt+1]>1))Cxy=0; else if
	 * ((buffer[accslt]>1)&&(buffer[accslt+1]<-1)) Cxy=1; else if
	 * ((buffer[accslt]<-1)&&(buffer[accslt+1]>1)) Cxy=2; else if
	 * ((buffer[accslt]<-2)&&(buffer[accslt+1]<-1)) Cxy=3;
	 * 
	 * if(i==iflag){ bufferCxy.append(String.valueOf(Cxy)); continue; }
	 * 
	 * if(!(Cxy==Cxyl)) { bufferCxy.append(String.valueOf(Cxy)); Cxyl=Cxy; }
	 * 
	 * } String[] bufferStrings={bufferCxy.toString()}; return bufferStrings; }
	 */

	public String[] RF_extract_2D(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		ArrayList<float[]> getposition = getposition(floatcollectArray, flag);
		float[] Mx = getposition.get(0);
		float[] My = getposition.get(1);
		float[] Mz = getposition.get(2);
		StringBuilder bufferx = new StringBuilder();
		StringBuilder buffery = new StringBuilder();
		float x = 0, y = 0, z = 0;
		// 不动是0，向左是1向右是2
		int moveflag = 0, moveflagl = 0;
		bufferx.append(0);
		for (int i = 0; i < Mx.length; i++) {
			if (Mx[i] > x)
				moveflagl = 2;
			if (Mx[i] < x)
				moveflagl = 1;
			// if(Mx[i]==x)moveflagl=0;
			if (moveflagl != moveflag) {
				bufferx.append(moveflagl);
				moveflag = moveflagl;
			}
			x = Mx[i];
		}
		moveflag = 0;
		moveflagl = 0;
		buffery.append(0);
		for (int i = 0; i < My.length; i++) {
			if (My[i] > y)
				moveflagl = 2;
			if (My[i] < y)
				moveflagl = 1;
			// if(My[i]==y)moveflagl=0;
			if (moveflagl != moveflag) {
				buffery.append(moveflagl);
				moveflag = moveflagl;
			}
			y = My[i];
		}

		String[] bufferStrings = new String[20];
		// if(bufferx.length()>10||buffery.length()>10)return bufferStrings;
		for (int i = 0; i < bufferx.length(); i++) {
			bufferStrings[i] = String.valueOf(bufferx.charAt(i));
		}
		if (bufferx.length() < 10) {
			for (int i = bufferx.length(); i < 10; i++) {
				bufferStrings[i] = String.valueOf(0);
			}

		}

		for (int i = 0; i < buffery.length(); i++) {
			bufferStrings[i + 10] = String.valueOf(buffery.charAt(i));
		}
		if (buffery.length() < 10) {
			for (int i = buffery.length() + 10; i < 20; i++) {
				bufferStrings[i] = String.valueOf(0);
			}

		}
		return bufferStrings;
	}
	public void FFT_extract(ArrayList<float[]> floatcollectArray, Boolean flag) {

	}
	// 平均速率,不是速度
	public float[] speedmean_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;

		float sumxold = 0;
		float sumyold = 0;
		float sumzold = 0;

		float sumxl = 0;
		float sumyl = 0;
		float sumzl = 0;

		int num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num - 1; i++) {
			float[] buffer = floatcollectArray.get(i);
			float[] buffer1 = floatcollectArray.get(i + 1);
			float time = buffer1[timepp] - buffer[timepp];
			if (buffer[accslt] * buffer1[accslt] > 0)
				sumx = Math.abs(buffer[accslt] + buffer1[accslt]) * time / 2
						+ sumxold;
			else
				sumx = (buffer[accslt] * buffer[accslt] + buffer1[accslt]
						* buffer1[accslt])
						* time
						/ (2 * (Math.abs(buffer[accslt]) + Math
								.abs(buffer1[accslt]))) + sumxold;
			sumxl += (sumxold + sumx) * time / 2;
			sumxold = sumx;

			if (buffer[accslt + 1] * buffer1[accslt + 1] > 0)
				sumy = Math.abs(buffer[accslt + 1] + buffer1[accslt + 1])
						* time / 2 + sumyold;
			else
				sumy = (buffer[accslt + 1] * buffer[accslt + 1] + buffer1[accslt + 1]
						* buffer1[accslt + 1])
						* time
						/ (2 * (Math.abs(buffer[accslt + 1]) + Math
								.abs(buffer1[accslt + 1]))) + sumyold;
			sumyl += (sumyold + sumy) * time / 2;
			sumyold = sumy;

			if (buffer[accslt + 2] * buffer1[accslt + 2] > 0)
				sumz = Math.abs(buffer[accslt + 2] + buffer1[accslt + 2])
						* time / 2 + sumzold;
			else
				sumz = (buffer[accslt + 2] * buffer[accslt + 2] + buffer1[accslt + 2]
						* buffer1[accslt + 2])
						* time
						/ (2 * (Math.abs(buffer[accslt + 2]) + Math
								.abs(buffer1[accslt + 2]))) + sumzold;
			sumzl += (sumzold + sumz) * time / 2;
			sumzold = sumz;

		}
		sumx = sumxl
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		sumy = sumyl
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		sumz = sumzl
				/ (floatcollectArray.get(iflag)[timepp] - floatcollectArray
						.get((int) (num - 1))[timepp]);
		float[] result = {sumx, sumy, sumz};
		return result;
	}

	public float[] Variance_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		float sumx2 = 0;
		float sumy2 = 0;
		float sumz2 = 0;

		long num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		float[] re = accxmean_extract(floatcollectArray, flag);
		float avrx = re[0];
		float avry = re[1];
		float avrz = re[2];

		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx2 += (buffer[accslt] - avrx) * (buffer[accslt] - avrx);
			sumy2 += (buffer[accslt + 1] - avry) * (buffer[accslt + 1] - avry);
			sumz2 += (buffer[accslt + 2] - avrz) * (buffer[accslt + 2] - avrz);
		}

		sumx2 = sumx2 / (num - iflag);
		sumy2 = sumy2 / (num - iflag);
		sumz2 = sumz2 / (num - iflag);
		float[] result = {sumx2, sumy2, sumz2};
		return result;
	}
	public float target_extract(ArrayList<float[]> floatcollectArray) {
		return floatcollectArray.get(0)[0];

	}
	// MAD的提取
	public static float[] accxmeanshift_extract(
			ArrayList<float[]> floatcollectArray, Boolean flag) {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		long num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		// i=0为标志位，下同
		float[] re = accxmean_extract(floatcollectArray, flag);
		float avrx = re[0];
		float avry = re[1];
		float avrz = re[2];

		for (int i = iflag; i < floatcollectArray.size(); i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += Math.abs(buffer[accslt] - avrx);
			sumy += Math.abs(buffer[accslt + 1] - avry);
			sumz += Math.abs(buffer[accslt + 2] - avrz);
		}
		avrx = sumx / (num - iflag);
		avry = sumy / (num - iflag);
		avrz = sumz / (num - iflag);
		float acc[] = {avrx, avry, avrz};
		return acc;

	}
	void printFloatArrarylist(ArrayList<float[]> floatinout, String path)
			throws IOException {
		for (float[] buffer : floatinout) {
			StringBuilder featurebuilder = new StringBuilder();
			int connlen = buffer.length;
			for (int k = 0; k < connlen; k++) {
				featurebuilder.append(String.valueOf(buffer[k]));
				featurebuilder.append(" ");
			}
			featurebuilder.append("\n");
			String feature = featurebuilder.toString();
			byte[] bufferbyte = new byte[feature.length() * 2];
			bufferbyte = feature.getBytes();
			FileOutputStream Outstream = new FileOutputStream(path, true);
			Outstream.write(bufferbyte);
			Outstream.close();
		}

	}
	public static boolean isint(float test) {
		Float aaFloat = new Float(test);
		int i = aaFloat.intValue();
		float t = aaFloat.floatValue();
		if ((t - i) == 0)
			return true;
		else
			return false;

	}
	public ArrayList<float[]> getposition(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		int num = floatcollectArray.size();
		float[] vx = new float[num - iflag];
		float[] vy = new float[num - iflag];
		float[] vz = new float[num - iflag];

		float[] sx = new float[num - iflag];
		float[] sy = new float[num - iflag];
		float[] sz = new float[num - iflag];

		vx[0] = 0;
		vy[0] = 0;
		vz[0] = 0;

		for (int i = iflag, j = 1; i < num - 1; i++) {
			float[] buffer = floatcollectArray.get(i);
			float[] buffer1 = floatcollectArray.get(i + 1);
			vx[j] = (buffer[accslt] + buffer1[accslt])
					* (buffer1[timepp] - buffer[timepp]) / 2 + vx[j - 1];
			vy[j] = (buffer[accslt + 1] + buffer1[accslt + 1])
					* (buffer1[timepp] - buffer[timepp]) / 2 + vy[j - 1];
			vz[j] = (buffer[accslt + 2] + buffer1[accslt + 2])
					* (buffer1[timepp] - buffer[timepp]) / 2 + vz[j - 1];
			j++;
		}
		sx[0] = 0;
		sy[0] = 0;
		sz[0] = 0;
		for (int i = iflag, j = 1, k = 0; i < num - 1; i++, j++, k++) {
			float[] buffer = floatcollectArray.get(i);
			float[] buffer1 = floatcollectArray.get(i + 1);
			sx[j] = (vx[k] + vx[k + 1]) * (buffer1[timepp] - buffer[timepp])
					/ 2 + sx[j - 1];
			sy[j] = (vy[k] + vy[k + 1]) * (buffer1[timepp] - buffer[timepp])
					/ 2 + sy[j - 1];
			sz[j] = (vz[k] + vz[k + 1]) * (buffer1[timepp] - buffer[timepp])
					/ 2 + sz[j - 1];

		}
		ArrayList<float[]> result = new ArrayList<float[]>();
		result.add(sx);
		result.add(sy);
		result.add(sz);
		return result;
	}
	// 超过10s的数据在这里会出现问题的，注意
	private static float[] energy_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) throws Exception {

		float eneryx = 0;
		float eneryy = 0;
		float eneryz = 0;
		int num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		float[] x = new float[num - iflag];
		float[] y = new float[num - iflag];
		float[] z = new float[num - iflag];
		for (int i = iflag, k = 0; i < num; i++, k++) {
			x[k] = floatcollectArray.get(i)[0];
			y[k] = floatcollectArray.get(i)[1];
			z[k] = floatcollectArray.get(i)[2];
		}
		FFT fftx = new FFT(x);
		eneryx = fftx.energy_cal();

		FFT ffty = new FFT(y);
		eneryy = ffty.energy_cal();

		FFT fftz = new FFT(z);
		eneryz = fftz.energy_cal();

		float acc[] = {eneryx, eneryy, eneryz};
		return acc;

	}
	private static float[] corr_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) throws Exception {
		float[] std = STD_extract(floatcollectArray, flag);
		float[] mean = accxmean_extract(floatcollectArray, flag);
		int num = floatcollectArray.size();

		float stdx = std[0];
		float stdy = std[1];
		float stdz = std[2];

		float meanx = mean[0];
		float meany = mean[1];
		float meanz = mean[2];

		float corrxy = 0;
		float corrxz = 0;
		float corryz = 0;

		float sumxy = 0;
		float sumxz = 0;
		float sumyz = 0;

		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumxy += (buffer[0] - meanx) * (buffer[1] - meany);
			sumxz += (buffer[0] - meanx) * (buffer[2] - meanz);
			sumyz += (buffer[1] - meany) * (buffer[2] - meanz);
		}
		corrxy = sumxy / (num * stdx * stdy);

		corrxz = sumxz / (num * stdx * stdz);

		corryz = sumyz / (num * stdy * stdz);
		float[] corr = {corrxy, corrxz, corryz};
		return corr;

	}
	// 标准差的提取函数
	private static float[] STD_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) throws Exception {
		float[] mean = accxmean_extract(floatcollectArray, flag);
		float meanx = mean[0];
		float meany = mean[1];
		float meanz = mean[2];
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		int num = floatcollectArray.size();
		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += (buffer[0] - meanx) * (buffer[0] - meanx);
			sumy += (buffer[1] - meany) * (buffer[1] - meany);
			sumz += (buffer[2] - meanz) * (buffer[2] - meanz);
		}
		float mtdx = (float) Math.sqrt(sumx / (num - 1));
		float mtdy = (float) Math.sqrt(sumy / (num - 1));
		float mtdz = (float) Math.sqrt(sumz / (num - 1));
		float[] mtd = {mtdx, mtdy, mtdz};
		return mtd;

	}
	// rms提取
	private static float[] RMS_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) throws Exception {
		int num = floatcollectArray.size();
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;

		float rmsx = 0;
		float rmsy = 0;
		float rmsz = 0;

		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		for (int i = iflag; i < num; i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += (buffer[0]) * (buffer[0]);
			sumy += (buffer[1]) * (buffer[1]);
			sumz += (buffer[2]) * (buffer[2]);

		}
		rmsx = sumx / num;
		rmsy = sumy / num;
		rmsz = sumz / num;

		float[] rms = {rmsx, rmsy, rmsz};
		return rms;

	}
	// IRQ四分位差的计算
	private static float[] IRQ_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) throws Exception {
		int num = floatcollectArray.size();

		int iflag;
		if (flag) {
			iflag = 1;
		} else {
			iflag = 0;
		}
		float[] x = new float[num - iflag];
		float[] y = new float[num - iflag];
		float[] z = new float[num - iflag];
		for (int i = iflag, k = 0; i < num; i++, k++) {
			x[k] = floatcollectArray.get(i)[0];
			y[k] = floatcollectArray.get(i)[1];
			z[k] = floatcollectArray.get(i)[2];
		}
		Arrays.sort(x);
		Arrays.sort(y);
		Arrays.sort(z);
		int numreal = num - iflag + 1;
		int q1, q3;// 位置
		float Q1x, Q3x;
		float Q1y, Q3y;
		float Q1z, Q3z;
		if (numreal % 4 == 0) {
			q1 = numreal / 4;
			q3 = 3 * q1;
			Q1x = x[q1 - 1];
			Q3x = x[q3 - 1];

			Q1y = y[q1 - 1];
			Q3y = y[q3 - 1];

			Q1z = z[q1 - 1];
			Q3z = z[q3 - 1];
		} else {
			float q1_f = ((float) numreal) / 4;
			float q3_f = 3 * q1_f;
			q1 = numreal / 4;
			q3 = 3 * q1;
			Q1x = (q1_f - q1) * x[q1 + 1] + (q1 + 1 - q1_f) * x[q1];
			Q3x = (q3_f - q3) * x[q3 + 1] + (q3 + 1 - q3_f) * x[q3];

			Q1y = (q1_f - q1) * y[q1 + 1] + (q1 + 1 - q1_f) * y[q1];
			Q3y = (q3_f - q3) * y[q3 + 1] + (q3 + 1 - q3_f) * y[q3];

			Q1z = (q1_f - q1) * z[q1 + 1] + (q1 + 1 - q1_f) * z[q1];
			Q3z = (q3_f - q3) * z[q3 + 1] + (q3 + 1 - q3_f) * z[q3];
		}
		float IRQx = Q3x - Q1x;
		float IRQy = Q3y - Q1y;
		float IRQz = Q3z - Q1z;

		float[] rms = {IRQx, IRQy, IRQz};
		return rms;

	}

}
