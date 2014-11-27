package com.example.preproccess;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.opengl.Matrix;
import android.util.Log;

import com.example.handledata.translatedata;
//训练分割前的数据和预测分割后的数据分别进行投影,他们的数据格式是相同的，一个label和一堆数据
public class PCA_done {
	private final static int accslt = 0;
	private ArrayList<float[]> floatcollectArray = new ArrayList<float[]>();
	private String placeString;

	public PCA_done(String path, String place) throws IOException {
		/*
		 * float []tt0={1,2,3}; float []tt1={2,3,4}; ArrayList<float[]>
		 * ttList=new ArrayList<float[]>(); ttList.add(tt0); ttList.add(tt1);
		 * generate_matrix(ttList);
		 */

		placeString = place;
		/*
		 * File dirFile=new File(placeString); if (!dirFile.exists()) {
		 * dirFile.mkdirs(); }
		 */
		translatedata translatedata = new translatedata(path);
		translatedata.file_array();
		this.floatcollectArray = translatedata.floatcollectArray;
		int length = floatcollectArray.size();
		int recycle = 0;
		int flagb = 0;
		for (int l = 0; l < length; l++) {
			if (isint(floatcollectArray.get(l)[0])) {
				recycle++;
				if (recycle == 1)
					flagb = l;
				else {
					write_infile(flagb, l);
					flagb = l;
				}

			}

		}
		write_infile(flagb, translatedata.floatcollectArray.size());
	}

	private void write_infile(int flagb, int l) throws IOException {
		// 在采集的时候必须要把标志位放在第一个float[]中，所以其实floatinout中的有效数据是floatinout.size()-1
		ArrayList<float[]> floatinout = new ArrayList<float[]>();
		ArrayList<float[]> floatinout_forsplited = new ArrayList<float[]>();
		List<float[]> floatinoutlist = floatcollectArray.subList(flagb, l);
		int listlen = floatinoutlist.size();
		for (int list = 0; list < listlen; list++) {
			floatinout.add(floatinoutlist.get(list));
		}
		// 要删除第一个元素，由于第一个元素是label位，会影响运动静止分类的，下面仍然用floatinout
		float[] first = new float[4];
		first = floatinout.get(0);
		floatinout.remove(0);
		// printFloatArrarylist(floatinout_forsplited,"/sdcard/1122");

		// printFloatArrarylist(floatinout_forsplited,"/sdcard/112233");
		// 提取相关的特征
		float[][] eigenvalues = new float[3][3];
		float[][] matrix = generate_matrix(floatinout);
		eigenvalues_extarct(matrix, 3, eigenvalues, 0.000001f, 100);
		int select = min_eigenvalues(matrix);
		float[] realz = new float[3];
		float[] zax = {0, 0, 1};
		for (int i = 0; i < 3; i++) {
			realz[i] = eigenvalues[i][select];
		}
		float[] realx = VecCross(realz, zax);
		float[] realy = VecCross(realx, realz);
		float[] mInv = new float[16];
		float[] m = new float[16];
		for (int i = 0, j = 0; i < 3; i++) {
			m[j] = realx[i];
			m[j + 1] = realy[i];
			m[j + 2] = realz[i];
			m[j + 3] = 0;
			j = j + 4;
		}
		m[12] = 0;
		m[13] = 0;
		m[14] = 0;
		m[15] = 1;
		Matrix.invertM(mInv, 0, m, 0);
		for (int i = 0; i < floatinout.size(); i++) {
			float[] buffer = floatinout.get(i);
			float[] inbuffer = new float[4];
			float[] resultVec = new float[4];
			for (int j = 0; j < 3; j++) {
				inbuffer[j] = buffer[j];
			}
			inbuffer[3] = 1;
			Matrix.multiplyMV(resultVec, 0, mInv, 0, inbuffer, 0);
			for (int j = 0; j < 3; j++) {
				buffer[j] = resultVec[j];
			}
			floatinout.remove(i);
			floatinout.add(i, buffer);
		}
		floatinout.add(0, first);

		printFloatArrarylist(floatinout, placeString);
	}

	private void printFloatArrarylist(ArrayList<float[]> floatinout, String path)
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

	private float[] VecCross(float[] u, float[] v) {
		// TODO Auto-generated method stub
		float x = u[1] * v[2] - u[2] * v[1];

		float y = u[2] * v[0] - u[0] * v[2];

		float z = u[0] * v[1] - u[1] * v[0];
		float[] result = {x, y, z};
		return result;
	}

	private int min_eigenvalues(float[][] eigenvalues) {
		// TODO Auto-generated method stub
		if ((eigenvalues[1][1] > eigenvalues[0][0])
				&& (eigenvalues[2][2] > eigenvalues[0][0]))
			return 0;
		else if (eigenvalues[1][1] > eigenvalues[2][2]) {
			return 2;
		} else {
			return 1;
		}

	}

	private float[][] generate_matrix(ArrayList<float[]> floatinout) {
		float[] accextract = accxmean_extract(floatinout, false);
		int length = floatinout.size();
		float[] bufferx = new float[length];
		float[] buffery = new float[length];
		float[] bufferz = new float[length];
		for (int i = 1; i < length; i++) {
			float[] buffer = floatinout.get(i);
			bufferx[i - 1] = buffer[0] - accextract[0];
			buffery[i - 1] = buffer[1] - accextract[1];
			bufferz[i - 1] = buffer[2] - accextract[2];
		}
		float[][] result = generate_matrix_9(bufferx, buffery, bufferz);
		return result;

	}

	private float[][] generate_matrix_9(float[] bufferx, float[] buffery,
			float[] bufferz) {

		float A11 = multiply_matrix(bufferx, bufferx);
		float A22 = multiply_matrix(buffery, buffery);
		float A33 = multiply_matrix(bufferz, bufferz);
		float A12 = multiply_matrix(bufferx, buffery);
		float A13 = multiply_matrix(bufferx, bufferz);
		float A23 = multiply_matrix(buffery, bufferz);
		float[][] result = {{A11, A12, A13}, {A12, A22, A23}, {A13, A23, A33}};
		return result;

	}

	private float multiply_matrix(float[] bufferf, float[] bufferb) {
		// TODO Auto-generated method stub
		int length = bufferb.length;
		float sum = 0;
		for (int i = 0; i < length; i++) {
			sum += bufferf[i] * bufferb[i];

		}
		return sum;
	}

	private float[] accxmean_extract(ArrayList<float[]> floatcollectArray,
			Boolean flag) {
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
		for (int i = iflag; i < floatcollectArray.size(); i++) {
			float[] buffer = floatcollectArray.get(i);
			sumx += buffer[accslt];
			sumy += buffer[accslt + 1];
			sumz += buffer[accslt + 2];
		}
		float avrx = sumx / (num - iflag);
		float avry = sumy / (num - iflag);
		float avrz = sumz / (num - iflag);
		float acc[] = {avrx, avry, avrz};
		return acc;

	}
	// 用雅可比法求出矩阵的特征值和特征向量,已测试，无问题
	private int eigenvalues_extarct(float a[][], int n, float v[][], float eps,
			int jt) {

		int p = 0, q = 0, l = 1;
		float fm, cn, sn, omega, x, y, d;

		for (int i = 0; i < n; i++) {
			v[i][i] = 1.0f;
			for (int j = 0; j < n; j++)
				if (i != j)
					v[i][j] = 0.0f;
		}

		while (1 == 1) {
			fm = 0.0f;
			for (int i = 1; i < n; i++)
				for (int j = 0; j < i; j++) {
					d = Math.abs(a[i][j]);
					if ((i != j) && (d > fm)) {
						fm = d;
						p = i;
						q = j;
					}
				}
			if (fm < eps)
				return (1);
			if (l > jt)
				return (-1);
			l = l + 1;
			x = -a[p][q];
			y = (a[q][q] - a[p][p]) / 2;
			omega = (float) (x / Math.sqrt(x * x + y * y));
			if (y < 0.0)
				omega = -omega;
			sn = (float) (1.0 + Math.sqrt(1.0 - omega * omega));
			sn = (float) (omega / Math.sqrt(2.0 * sn));
			cn = (float) Math.sqrt(1.0 - sn * sn);
			fm = a[p][p];
			a[p][p] = fm * cn * cn + a[q][q] * sn * sn + a[p][q] * omega;
			a[q][q] = fm * sn * sn + a[q][q] * cn * cn - a[p][q] * omega;
			a[p][q] = 0.0f;
			a[q][p] = 0.0f;
			for (int j = 0; j < n; j++)
				if ((j != p) && (j != q)) {
					fm = a[p][j];
					a[p][j] = fm * cn + a[q][j] * sn;
					a[q][j] = -fm * sn + a[q][j] * cn;
				}
			for (int i = 0; i < n; i++)
				if ((i != p) && (i != q)) {
					fm = a[i][p];
					a[i][p] = fm * cn + a[i][q] * sn;
					a[i][q] = -fm * sn + a[i][q] * cn;
				}
			for (int i = 0; i < n; i++) {
				fm = v[i][p];
				v[i][p] = fm * cn + v[i][q] * sn;
				v[i][q] = -fm * sn + v[i][q] * cn;
			}
		}
		// return(1);
		// return (1);
	}
	private boolean isint(float test) {
		Float aaFloat = new Float(test);
		int i = aaFloat.intValue();
		float t = aaFloat.floatValue();
		if ((t - i) == 0)
			return true;
		else
			return false;

	}

}
