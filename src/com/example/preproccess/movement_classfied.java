package com.example.preproccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.exec.svm_predict;
import com.example.handledata.translatedata;

public class movement_classfied {

	private ArrayList<float[]> floatcollectArray = new ArrayList<float[]>();
	private translatedata translatedatapre;
	private int window_length;
	private int window_shift;
	// train和predict的输出格式不一样，其中train是作为一个容器输出，predict是作为一个文件输出。
	// private boolean train_predict;//训练和预测的格式不一样，true是预测，false是训练
	public movement_classfied(String path, int window_length, int window_shift)
			throws IOException {
		translatedatapre = new translatedata(path);
		translatedatapre.file_array();
		floatcollectArray = translatedatapre.floatcollectArray;
		this.window_length = window_length;
		this.window_shift = window_shift;
		Data_predict(false);
	}
	public movement_classfied(ArrayList<float[]> floatcollectArray,
			int window_length, int window_shift) throws IOException {
		this.floatcollectArray = floatcollectArray;
		this.window_length = window_length;
		this.window_shift = window_shift;
		Data_predict(true);
	}

	private void Data_predict(boolean train_predict) throws IOException {
		String pathString;
		if (train_predict)
			pathString = "//sdcard/train/train_startpoint.txt";
		else
			pathString = "//sdcard/train/predict_startpoint.txt";
		int length;
		if (floatcollectArray.size() > 120) {
			length = (floatcollectArray.size() - 120) / 20 + 2;
		} else {
			length = 1;
		}
		for (int i = 0; i < length; i++) {
			int end = window_length + i * window_shift;
			if (i == (length - 1)) {
				end = floatcollectArray.size();
			}
			List<float[]> floatinoutlist = floatcollectArray.subList(i
					* window_shift, end);
			ArrayList<float[]> floatinout = new ArrayList<float[]>();
			int listlength = floatinoutlist.size();
			for (int list = 0; list < listlength; list++) {
				floatinout.add(floatinoutlist.get(list));
			}
			float[] accxmeanshift_extract = translatedata
					.accxmeanshift_extract(floatinout, train_predict);
			float[] grymean_extract = translatedata.grymean_extract(floatinout,
					train_predict);
			try {
				FileOutputStream train_model = new FileOutputStream(pathString,
						true);
				float[] conn = new float[accxmeanshift_extract.length
						+ grymean_extract.length];
				System.arraycopy(accxmeanshift_extract, 0, conn, 0,
						accxmeanshift_extract.length);
				System.arraycopy(grymean_extract, 0, conn,
						accxmeanshift_extract.length, grymean_extract.length);
				StringBuilder featurebuilder = new StringBuilder();
				int connlen = conn.length;
				featurebuilder.append(String.valueOf(1));
				featurebuilder.append(" ");
				for (int k = 0; k < connlen; k++) {
					featurebuilder.append(String.valueOf(k + 1));
					featurebuilder.append(":");
					featurebuilder.append(String.valueOf(conn[k]));
					featurebuilder.append(" ");
				}
				featurebuilder.append("\n");
				String feature = featurebuilder.toString();
				byte[] buffer = new byte[feature.length() * 2];
				buffer = feature.getBytes();
				train_model.write(buffer);
				train_model.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		File modelFile = new File("/sdcard/train/train_out.txt");
		if (!modelFile.exists()) {
			return;
		}
		String[] arg = {pathString, "/sdcard/train/train_out.txt",
				pathString.replace(".txt", "_pd.txt")};

		try {
			svm_predict.main(arg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 训练割出一个段
	public ArrayList<float[]> Data_splited_return() throws IOException {
		ArrayList<float[]> floatArrayListreturn = new ArrayList<float[]>();
		int movementflag = 0;
		boolean sublistflag = false;
		translatedata translatepredict = new translatedata(
				"//sdcard/train/train_startpoint_pd.txt");
		translatepredict.file_array();
		ArrayList<float[]> floatcollectArraypre = translatepredict.floatcollectArray;
		int j = 0;
		for (int i = 0; i < floatcollectArraypre.size(); i++) {
			if (floatcollectArraypre.get(i)[0] == 0) {
				if (j % 2 == 0) {
					movementflag = i;
					j++;
				}
				// 第二次进入这个循环就用movementflag和i来切原始数据
				sublistflag = true;
				continue;
			}
			if (sublistflag) {
				sublistflag = false;
				j++;
				// 在这里切割
				ArrayList<float[]> floatinoutpreArrayList = new ArrayList<float[]>();
				List<float[]> floatcollectArrayreal = floatcollectArray
						.subList(movementflag * window_shift, window_length
								+ (i - 1) * window_shift);
				int listlen = floatcollectArrayreal.size();
				for (int list = 0; list < listlen; list++) {
					floatinoutpreArrayList.add(floatcollectArrayreal.get(list));
				}
				new File("//sdcard/train/train_startpoint_pd.txt").delete();
				new File("//sdcard/train/train_startpoint.txt").delete();
				floatArrayListreturn = floatinoutpreArrayList;

			}
		}
		// 如果j是奇数的话，说明最后一段都是运动且还没有被采集过来
		if (j % 2 == 1) {
			// 在这里切割
			ArrayList<float[]> floatinoutpreArrayList = new ArrayList<float[]>();
			List<float[]> floatcollectArrayreal = floatcollectArray.subList(
					movementflag * window_shift, floatcollectArray.size());
			int listlen = floatcollectArrayreal.size();
			for (int list = 0; list < listlen; list++) {
				floatinoutpreArrayList.add(floatcollectArrayreal.get(list));
			}
			floatArrayListreturn = floatinoutpreArrayList;
		}
		new File("//sdcard/train/train_startpoint_pd.txt").delete();
		new File("//sdcard/train/train_startpoint.txt").delete();
		return floatArrayListreturn;

	}
	// 预测可以割出多个段，训练只能割出一个段
	public void Data_splited() throws IOException {
		int movementflag = 0;
		boolean sublistflag = false;
		translatedata translatepredict = new translatedata(
				"//sdcard/train/predict_startpoint_pd.txt");
		translatepredict.file_array();
		ArrayList<float[]> floatcollectArraypre = translatepredict.floatcollectArray;
		int j = 0;
		for (int i = 0; i < floatcollectArraypre.size(); i++) {
			if (floatcollectArraypre.get(i)[0] == 0) {
				if (j % 2 == 0) {
					movementflag = i;
					j++;
				}
				// 第二次进入这个循环就用movementflag和i来切原始数据
				sublistflag = true;
				continue;
			}
			if (sublistflag) {
				sublistflag = false;
				j++;
				splitting(movementflag * window_shift, window_length + (i - 1)
						* window_shift);

			}
		}
		// 如果j是奇数的话，说明最后一段都是运动且还没有被采集过来
		if (j % 2 == 1) {
			splitting(movementflag * window_shift, floatcollectArray.size());
		}
		new File("//sdcard/train/predict_startpoint_pd.txt").delete();
		new File("//sdcard/train/predict_startpoint.txt").delete();
	}

	private void splitting(int start, int end) throws IOException {
		if ((end - start) < 4) {
			return;
		}
		// 在这里切割
		ArrayList<float[]> floatinoutpreArrayList = new ArrayList<float[]>();
		List<float[]> floatcollectArrayreal = floatcollectArray.subList(start,
				end);
		int listlen = floatcollectArrayreal.size();
		for (int list = 0; list < listlen; list++) {
			floatinoutpreArrayList.add(floatcollectArrayreal.get(list));
		}
		StringBuilder Databuilder = new StringBuilder();
		Databuilder.append(1);
		Databuilder.append("\n");
		for (float[] buffer : floatinoutpreArrayList) {
			for (int k = 0; k < buffer.length; k++) {
				Databuilder.append(String.valueOf(buffer[k]));
				Databuilder.append(" ");
			}
			Databuilder.append("\n");
		}
		String realdata = Databuilder.toString();
		byte[] bufferdata = new byte[realdata.length() * 2];
		bufferdata = realdata.getBytes();
		FileOutputStream predict_real = new FileOutputStream(
				"//sdcard/train/predict_real", true);
		predict_real.write(bufferdata);
		predict_real.close();
	}

}
